package com.bot.bots.beans.caffeine;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bot.bots.beans.view.Scheduled;
import com.bot.bots.beans.view.TransferBean;
import com.bot.bots.config.BotProperties;
import com.bot.bots.config.Constants;
import com.bot.bots.database.entity.Config;
import com.bot.bots.database.entity.Recharge;
import com.bot.bots.database.entity.User;
import com.bot.bots.database.enums.RechargeStatus;
import com.bot.bots.database.enums.TaskType;
import com.bot.bots.database.service.ConfigService;
import com.bot.bots.database.service.RechargeService;
import com.bot.bots.database.service.UserService;
import com.bot.bots.helper.DecimalHelper;
import com.bot.bots.helper.TimeHelper;
import com.bot.bots.sender.AsyncSender;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
@Slf4j
@Component
public class ExpireListener implements RemovalListener<String, Scheduled > {


    @Resource private OkHttpClient client;
    @Resource private UserService userService;
    @Resource private BotProperties properties;
    @Resource private ConfigService configService;
    @Resource private RechargeService rechargeService;

    @Override
    public void onRemoval(@Nullable String key, @Nullable Scheduled scheduled, RemovalCause cause) {
        if (Objects.isNull(key) || Objects.isNull(scheduled) || !RemovalCause.EXPIRED.equals(cause)) {
            return;
        }
        if (Objects.equals(scheduled.getType(), TaskType.RECHARGE)) {
            this.processorRecharge(key, scheduled);
        }
    }

    private void processorRecharge(String key, Scheduled scheduled) {
        try {
            Recharge recharge = this.rechargeService.getById(Long.parseLong(key));
            // 充值单已被删除：直接结束，不再轮询
            if (Objects.isNull(recharge)) {
                return;
            }
            // 已被确认或已过期：结束轮询，不再入队
            if (!Objects.equals(recharge.getRechargeStatus(), RechargeStatus.UNRECEIVED)) {
                return;
            }
            // expireTime 脏数据（null）按未过期处理，继续轮询
            LocalDateTime expireTime = recharge.getExpireTime();
            boolean expired = Objects.nonNull(expireTime) && LocalDateTime.now().isAfter(expireTime);
            if (expired) {
                // 原子抢占过期标记，避免与并发确认竞争
                boolean marked = this.rechargeService.lambdaUpdate()
                        .eq(Recharge::getId, recharge.getId())
                        .eq(Recharge::getRechargeStatus, RechargeStatus.UNRECEIVED)
                        .set(Recharge::getRechargeStatus, RechargeStatus.EXPIRED)
                        .update();
                if (marked) {
                    AsyncSender.async(
                            SendMessage.builder().text(Constants.TIMEOUT_TEXT)
                                    .chatId(recharge.getUserId()).build()
                    );
                }
                return;
            }
            // 仅当 key 对应的充值单仍为 UNRECEIVED 时才需要继续轮询
            boolean needPolling = true;
            List<TransferBean> doneQuery = this.doQuery(properties.getAddress());
            if (CollUtil.isNotEmpty(doneQuery)) {
                for (TransferBean transferBean : doneQuery) {
                    // 未确认上链的转账跳过
                    if (Objects.isNull(transferBean) || !Boolean.TRUE.equals(transferBean.getConfirmed())) {
                        continue;
                    }
                    // 只匹配转入本机器人地址的转账，避免转出/无关转账误匹配
                    if (!transferBean.isTransferIn(properties.getAddress())) {
                        continue;
                    }
                    BigDecimal number = transferBean.parseValue();
                    if (Objects.isNull(number) || number.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    BigDecimal fractionalPart = number.remainder(BigDecimal.ONE);
                    BigDecimal integerPart = number.subtract(fractionalPart);
                    Recharge unreceived = this.rechargeService.getOne(
                            Wrappers.lambdaQuery(Recharge.class)
                                    .eq(Recharge::getPointer, fractionalPart)
                                    .eq(Recharge::getAmount, integerPart)
                                    .eq(Recharge::getRechargeStatus, RechargeStatus.UNRECEIVED)
                                    .orderByDesc(Recharge::getId)
                                    .last(" limit 1")
                    );
                    // 未命中：continue 继续匹配下一笔，不能 return 中断整批
                    if (Objects.isNull(unreceived)) {
                        continue;
                    }
                    // 金额为空属于脏数据：回滚状态，等待人工处理
                    if (Objects.isNull(unreceived.getAmount())) {
                        log.error("[充值确认] 充值单金额为空，回滚状态待处理，充值单id：{}", unreceived.getId());
                        this.rollbackToUnreceived(unreceived.getId());
                        continue;
                    }
                    // 原子抢占：仅当仍为 UNRECEIVED 时标记已到账，并发/重复轮询抢不到
                    boolean claimed = this.rechargeService.lambdaUpdate()
                            .eq(Recharge::getId, unreceived.getId())
                            .eq(Recharge::getRechargeStatus, RechargeStatus.UNRECEIVED)
                            .set(Recharge::getRechargeStatus, RechargeStatus.RECEIVED)
                            .set(Recharge::getReceiveTime, LocalDateTime.now())
                            .update();
                    if (!claimed) {
                        continue;
                    }
                    if (Objects.equals(unreceived.getId(), recharge.getId())) {
                        needPolling = false;
                    }
                    // 抢占成功后再给用户加余额
                    boolean balanceAdded = this.userService.lambdaUpdate()
                            .eq(User::getUserId, unreceived.getUserId())
                            .setSql("balance = balance + {0}", unreceived.getAmount())
                            .update();
                    if (!balanceAdded) {
                        // 用户不存在加余额失败：回滚状态，等待重试
                        log.error("[充值确认] 加余额失败（用户可能已删除），回滚充值单状态待重试，充值单id：{}，用户id：{}",
                                unreceived.getId(), unreceived.getUserId());
                        this.rollbackToUnreceived(unreceived.getId());
                        needPolling = true;
                        continue;
                    }
                    String format = StrUtil.format(Constants.RECHARGE_SUCCESS_TEXT,
                            unreceived.getId(),
                            DecimalHelper.decimalParse(number),
                            TimeHelper.format(LocalDateTime.now())
                    );
                    AsyncSender.async(
                            SendMessage.builder()
                                    .chatId(unreceived.getUserId())
                                    .text(format)
                                    .parseMode(ParseMode.MARKDOWN)
                                    .build()
                    );
                }
            }
            // 未确认完成则重新入队继续轮询，保证不丢单
            if (needPolling) {
                CountdownCaffeine.set(key, scheduled);
            }
        } catch (Exception ex) {
            // 任何异常路径都重新入队，避免款项永不到账
            log.error("[充值确认] 轮询处理异常，重新入队等待下次轮询，充值单key：{}", key, ex);
            CountdownCaffeine.set(key, scheduled);
        }
    }

    /**
     * 已到账状态回滚为未到账，等待重试
     */
    private void rollbackToUnreceived(Long rechargeId) {
        this.rechargeService.lambdaUpdate()
                .eq(Recharge::getId, rechargeId)
                .eq(Recharge::getRechargeStatus, RechargeStatus.RECEIVED)
                .set(Recharge::getRechargeStatus, RechargeStatus.UNRECEIVED)
                .update();
    }

    @SneakyThrows
    public List<TransferBean> doQuery(String address) {
        JSONObject jsonObject = doHttpQuery(address, (System.currentTimeMillis() - 30000));
        if (jsonObject == null) return Collections.emptyList();
        JSONArray tokenTransfers = (JSONArray) jsonObject.get("token_transfers");
        List<TransferBean> transferBeans = JSONUtil.toList(tokenTransfers, TransferBean.class);
        if (CollUtil.isEmpty(transferBeans)) {
            return Collections.emptyList();
        }
        log.info("[查询结果] 地址：{}，结果：{}", address, JSONUtil.toJsonStr(transferBeans));
        transferBeans.sort(Comparator.comparing(TransferBean::getBlockTs, Comparator.nullsFirst(Long::compareTo)));
        return transferBeans;
    }

    @SuppressWarnings("all")
    private JSONObject doHttpQuery(String address, Long prevTimestamp) throws IOException {
        String url = StrUtil.format(Constants.TRANSFER_QUERY, address, prevTimestamp);
        Request request = new Request.Builder().url(url).build();
        try (Response response = this.client.newCall(request).execute()) {
            if (!response.isSuccessful() || Objects.isNull(response.body())) {
                return null;
            }
            String body = response.body().string();
            return JSONUtil.parseObj(body);
        }
    }
}