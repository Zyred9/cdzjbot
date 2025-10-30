package com.bot.bots.handlers;

import cn.hutool.core.util.StrUtil;
import com.bot.bots.beans.cache.CommonCache;
import com.bot.bots.beans.caffeine.CountdownCaffeine;
import com.bot.bots.beans.view.Scheduled;
import com.bot.bots.beans.view.ctx.AcceptanceContext;
import com.bot.bots.beans.view.trx.PriceBean;
import com.bot.bots.config.BotProperties;
import com.bot.bots.config.Constants;
import com.bot.bots.database.entity.*;
import com.bot.bots.database.enums.*;
import com.bot.bots.database.service.*;
import com.bot.bots.helper.*;
import com.bot.bots.sender.AsyncSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 *      私聊
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
@Component
@RequiredArgsConstructor
public class PrivateChatHandler extends AbstractHandler{

    private final MapUtil mapUtil;
    private final HttpHelper httpHelper;
    private final UserService userService;
    private final BotProperties properties;
    private final ConfigService configService;
    private final ExposeService exposeService;
    private final PublishService publishService;
    private final AddressService addressService;
    private final TeamCtxService teamCtxService;
    private final RechargeService rechargeService;
//    private final ChatQueryHandler chatQueryHandler;

    @Override
    public boolean support(Update update) {
        return update.hasMessage()
                && update.getMessage().hasText()
            && update.getMessage().isUserMessage();
    }

    @Override
    protected BotApiMethod<?> execute(Update update) {
        Message message = update.getMessage();
        String text = message.getText();

        if (StrUtil.equals(text, "/start")) {
            this.userService.queryUser(message.getFrom());
//            boolean b = this.chatQueryHandler.checkUserInGroup(
//                    this.properties.getChannel(),
//                    message.getFrom().getId()
//            );
//            if (!b) {
//                return ok(message, Constants.START_SUBSCRIBE_TEXT);
//            }
            return reply(message, Constants.START_TEXT, KeyboardHelper.buildStartKeyboard());
        }

        if (StrUtil.equals(text, "\uD83D\uDCAC联系客服")) {
            Config config = this.configService.queryConfig();
            if (StrUtil.isBlank(config.getCustomText())) {
                return null;
            }
            InlineKeyboardMarkup keyboard = KeyboardHelper.keyboard(config.getCustomKeyboard());
            return markdownReply(message, config.getCustomText(), keyboard);
        }

        if (StrUtil.equals(text, "\uD83D\uDCF2合作洽谈")) {
            Config config = this.configService.queryConfig();
            if (StrUtil.isBlank(config.getSelfText())) {
                return null;
            }
            InlineKeyboardMarkup keyboard = KeyboardHelper.keyboard(config.getSelfKeyboard());
            return markdownReply(message, config.getSelfText(), keyboard);
        }

        if (StrUtil.equals(text, "\uD83D\uDD0E骗子曝光")) {
            // 进入骗子曝光流程：回复模版与功能键盘，设置缓存等待用户提交
            CommonCache.put(message.getFrom().getId(), TempEnum.INPUT_PZ_EXPOSE_TEXT);
            return markdownReply(message, Constants.PZ_EXPOSE_TEMPLATE);
        }

        if (StrUtil.equals(text, "❤️我的余额")) {
            User user = this.userService.queryUser(message.getFrom());
            String selfText = user.buildSelfText();
            return markdownReply(message, selfText);
        }

        if (StrUtil.equals(text, "\uD83D\uDFE2供需发布")) {
            User user = this.userService.queryUser(message.getFrom());

            // 余额不足
            if (DecimalHelper.lessThan(user.getBalance(), Constants.COST)) {
                BigDecimal subtract = Constants.COST.subtract(user.getBalance());
                Recharge recharge = Recharge.build(user, subtract, this.properties.getAddress());
                this.rechargeService.save(recharge);
                CountdownCaffeine.set(
                        String.valueOf(recharge.getId()),
                        Scheduled.build(TaskType.RECHARGE, TaskNode.RECHARGE)
                );
                return markdownReply(message, recharge.buildText(user.getBalance()));
            }
            CommonCache.put(message.getFrom().getId(), TempEnum.INPUT_PUBLISH_TEXT);
            return markdown(message, Constants.P_C_PUBLISH_TEXT);
        }

        if (StrUtil.equals(text, "\uD83D\uDC81\uD83C\uDFFB\u200d♂️承兑报备")) {
            List<Address> address = this.addressService.selectProvince();
            InlineKeyboardMarkup markup = KeyboardHelper.buildProvinceKeyboard(address, AddressParam.EXCHANGE.getCode());
            return ok(message, Constants.ACCEPTANCE_FILING_TEXT, markup);
        }

        if (StrUtil.equals(text, "\uD83D\uDE97车队报备")) {
            CommonCache.put(message.getFrom().getId(), TempEnum.CAR_TEAM_INPUT);
            return markdownReply(message, Constants.CAT_TEAM_TEXT);
        }


        if (StrUtil.equals(text, "\uD83D\uDC81\uD83C\uDFFC\u200d♀️承兑所在地")) {
            List<Address> address = this.addressService.selectProvince();
            InlineKeyboardMarkup markup = KeyboardHelper.buildProvinceKeyboard(address, AddressParam.QUERY_EXCHANGE.getCode());
            return markdownReply(message, Constants.PROVINCE_LOCATION_TEXT, markup);
        }

        if (StrUtil.equals(text, "\uD83D\uDE95车队所在地")) {
            List<Address> address = this.addressService.selectProvince();
            InlineKeyboardMarkup markup = KeyboardHelper.buildProvinceKeyboard(address, AddressParam.QUERY_TEAM.getCode());
            return markdownReply(message, Constants.CAT_TEAM_ADDRESS_TEXT, markup);
        }

        if (StrUtil.equals(text, "\uD83D\uDCB9U商报备")) {
            List<Address> address = this.addressService.selectProvince();
            InlineKeyboardMarkup markup = KeyboardHelper.buildProvinceKeyboard(address, AddressParam.UNLOADING_PARTNER.getCode());
            return markdownReply(message, Constants.UNLOADING_PARTNER_TEXT, markup);
        }

        if (StrUtil.equals(text, "\uD83C\uDE2F️U商所在地")) {
            List<Address> address = this.addressService.selectProvince();
            InlineKeyboardMarkup markup = KeyboardHelper.buildProvinceKeyboard(address, AddressParam.UNLOADING_LOCATION.getCode());
            return markdownReply(message, Constants.UNLOADING_LOCATION_TEXT, markup);
        }

        if (StrUtil.equals(text, "\uD83D\uDD0D查汇率")) {
            CommonCache.put(message.getFrom().getId(), TempEnum.CHECK_EXCHANGE_RATE_INPUT);
            return markdownReply(message, Constants.CHECK_EXCHANGE_RATE_TEXT);
        }

        // 用户有缓存
        if (CommonCache.containsKey(message.getFrom().getId())) {
            return this.processorCache(message);
        }

        return null;
    }

    private BotApiMethod<?> processorCache(Message message) {

        TempEnum tempEnum = CommonCache.get(message.getFrom().getId());
        if (Objects.equals(tempEnum, TempEnum.CHECK_EXCHANGE_RATE_INPUT)) {
            String query = this.parseAndQuery(message.getText());
            if (Objects.isNull(query)) {
                return null;
            }

            Config config = this.configService.queryConfig();
            InlineKeyboardMarkup keyboard = KeyboardHelper.keyboard(config.getQueryKeyboard());
            return markdown(message, query, keyboard);
        }


        TempEnum temp = CommonCache.getIfRemove(message.getFrom().getId());
        if (Objects.equals(temp, TempEnum.INPUT_PUBLISH_TEXT)) {
            String text = message.getText();
            Map<String, String> parsed = StrHelper.parseStrToMap(text);
            boolean a = parsed.containsKey("供需方");
            boolean b = parsed.containsKey("项目名称");
            boolean c = parsed.containsKey("项目介绍");
            boolean d = parsed.containsKey("金额");

            boolean e = a & b & c & d;
            if (!e) {
                return reply(message, "（供需方、项目名称、项目介绍、金额）必填！！");
            }

            Long auditId = this.properties.getAuditId();
            Publish p = Publish.build(text, message.getFrom().getId());
            this.publishService.save(p);

            InlineKeyboardMarkup markup = KeyboardHelper.buildAuditPublishKeyboard(p.getId());

            AsyncSender.async(this.markdown(auditId, text, markup));
            return reply(message, "✅请耐心等待，审核中...");
        }


        if (Objects.equals(temp, TempEnum.INPUT_PZ_EXPOSE_TEXT)) {
            String text = message.getText();
            Map<String, String> parsed = StrHelper.parseStrToMap(text);

            boolean needId = parsed.containsKey("骗子ID");
            boolean needNick = parsed.containsKey("骗子昵称");
            if (!(needId && needNick)) {
                return reply(message, "（骗子ID、骗子昵称）必填！！");
            }

            Expose expose = new Expose()
                    .setUserId(message.getFrom().getId())
                    .setTextRaw(text)
                    .setPzUserId(parsed.getOrDefault("骗子ID", ""))
                    .setPzNickname(parsed.getOrDefault("骗子昵称", ""))
                    .setStory(parsed.getOrDefault("被骗经过", ""))
                    .setPzAddress(parsed.getOrDefault("骗子U地址", ""))
                    .setStatus(ExposeStatus.WAIT);

            this.exposeService.save(expose);

            Long auditGroupId = this.properties.getAuditId();
            InlineKeyboardMarkup auditKeyboard = KeyboardHelper.buildPzExposeAuditKeyboard(expose.getId());
            AsyncSender.async(this.markdown(auditGroupId, text, auditKeyboard));

            return reply(message, "✅已提交审核，审核中...");
        }

        if (Objects.equals(temp, TempEnum.INPUT_INTERVAL)) {
            String interval = message.getText();
            CommonCache.getAccCtx(message.getFrom().getId())
                    .setWaitInputInterval(Boolean.FALSE)
                    .setDoneInputInterval(Boolean.TRUE)
                    .setIntervalInput(interval)
                    .setRate(BigDecimal.valueOf(0.05));

            InlineKeyboardMarkup markup = KeyboardHelper.buildRateIncrementKeyboard();
            String text = CommonCache.accCtxText(message.getFrom().getId());
            AsyncSender.async(delete(message));
            return markdown(message, text, markup);
        }

        if (Objects.equals(temp, TempEnum.CAR_TEAM_INPUT)) {
            Map<String, String> map = StrHelper.parseStrToMap(message.getText());

            boolean a = map.containsKey("底料");
            boolean b = map.containsKey("车队类型");
            boolean c = map.containsKey("所在省市");

            boolean e = a & b & c;
            if (!e) {
                return reply(message, "（底料、车队类型、所在省市）必填！！");
            }

            TeamCtx tc = TeamCtx.build(map, message.getFrom());
            String location = this.mapUtil.location(tc.getAddress());
            tc.setLocation(location);

            long count = this.teamCtxService.count() + 1;
            this.teamCtxService.save(tc);
            AsyncSender.async(ok(this.properties.getBackgroundId(),
                    StrUtil.format(Constants.CAT_REPORT_COMMITED_BACK_TEXT, count)));
            return markdownReply(message, Constants.CAR_TEAM_REPORT_SUCCESS_TEXT);
        }

        // 承兑所在地查询自定义输入范围
        if (Objects.equals(temp, TempEnum.EXCHANGE_INPUT_CUSTOM_SCOPE)) {
            try {
                int scope = Integer.parseInt(message.getText());
                AcceptanceContext ctx = CommonCache.getAccCtx(message.getFrom().getId());
                ctx.setScope(scope);

                InlineKeyboardMarkup markup = KeyboardHelper.buildScopeKeyboard(
                        AddressParam.QUERY_EXCHANGE.getCode(), ctx.getScope());

                String text = CommonCache.accCtxText(message.getFrom().getId());
                return markdown(message, text, markup);
            } catch (NumberFormatException ex) {
                return ok(message, "请输入整数");
            }
        }

        if (Objects.equals(temp, TempEnum.PARTNER_SUPPLEMENT_INPUT)
                || Objects.equals(temp, TempEnum.LOCATION_SUPPLEMENT_INPUT)) {
            AcceptanceContext ctx = CommonCache.getAccCtx(message.getFrom().getId());
            ctx.setSupplement(message.getText()).setShowSupplement(false);

            InlineKeyboardMarkup markup;
            if (Objects.equals(temp, TempEnum.PARTNER_SUPPLEMENT_INPUT)) {
                markup = KeyboardHelper.buildCommitSupplementReportKeyboard(AddressParam.UNLOADING_PARTNER.getCode());
            } else {
                markup = KeyboardHelper.buildCommitSupplementQueryKeyboard(AddressParam.UNLOADING_LOCATION.getCode());
            }
            String text = CommonCache.accCtxText(message.getFrom().getId());
            return markdown(message, text, markup);
        }
        return null;
    }


    public String parseAndQuery(String text) {
        PaymentEnum payment = PaymentEnum.of(text);
        if (Objects.isNull(payment)) {
            return null;
        }

        List<PriceBean> priceBeans = this.httpHelper.doQueryOkx(payment, "sell");
        priceBeans.sort(Comparator.comparing(PriceBean::getPrice));

        StringBuilder sb = new StringBuilder()
                .append("*OTC商家实时价格*").append("\n")
                        .append("筛选：").append(payment.getDesc()).append("欧意").append("\n");

        for (PriceBean priceBean : priceBeans) {
            sb.append("`").append(priceBean.getPrice()).append("\t\t").append(priceBean.getNickName()).append("`\n");
        }
        return sb.toString();
    }










}
