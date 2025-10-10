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
        List<TransferBean> doneQuery = this.doQuery(properties.getAddress());
        Recharge recharge = this.rechargeService.getById(Long.parseLong(key));
        if (Objects.nonNull(recharge)){
            LocalDateTime expireTime = recharge.getExpireTime();
            if (LocalDateTime.now().isAfter(expireTime)) {
                recharge.setRechargeStatus(RechargeStatus.EXPIRED);
                this.rechargeService.updateById(recharge);
                AsyncSender.async(
                        SendMessage.builder().text(Constants.TIMEOUT_TEXT)
                                .chatId(recharge.getUserId()).build()
                );
                return;
            }
        }
        if (CollUtil.isNotEmpty(doneQuery)) {
            for (TransferBean transferBean : doneQuery) {
                BigDecimal number = transferBean.parseValue();
                BigDecimal fractionalPart = number.remainder(BigDecimal.ONE);
                Recharge unreceived = this.rechargeService.getOne(
                        Wrappers.lambdaQuery(Recharge.class)
                                .eq(Recharge::getPointer, fractionalPart)
                                .eq(Recharge::getRechargeStatus, RechargeStatus.UNRECEIVED)
                                .last(" limit 1")
                );

                if (Objects.isNull(unreceived)) {
                    return;
                }

                unreceived.setRechargeStatus(RechargeStatus.RECEIVED);
                unreceived.setReceiveTime(LocalDateTime.now());
                this.rechargeService.updateById(unreceived);

                // 给充值的用户设置余额、次数、总充值的金额
                User user = this.userService.getById(unreceived.getUserId());
                user.setBalance(user.getBalance().add(unreceived.getAmount()));
                this.userService.updateById(user);

                String format = StrUtil.format(Constants.RECHARGE_SUCCESS_TEXT,
                        unreceived.getId(),
                        DecimalHelper.decimalParse(number),
                        TimeHelper.format(LocalDateTime.now())
                );
                AsyncSender.async(
                        SendMessage.builder()
                                .chatId(user.getUserId())
                                .text(format)
                                .parseMode(ParseMode.MARKDOWN)
                                .build()
                );
            }
            return;
        }
        CountdownCaffeine.set(key, scheduled);
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
        transferBeans.sort(Comparator.comparing(TransferBean::getBlockTs));
        return transferBeans;
    }

    @SuppressWarnings("all")
    private JSONObject doHttpQuery(String address, Long prevTimestamp) throws IOException {
        String url = StrUtil.format(Constants.TRANSFER_QUERY, address, prevTimestamp);
        Request request = new Request.Builder().url(url).build();
        Response response = this.client.newCall(request).execute();
        if (!response.isSuccessful() || Objects.isNull(response.body())) {
            response.close();
            return null;
        }
        String body = response.body().string();
        return JSONUtil.parseObj(body);
    }
}