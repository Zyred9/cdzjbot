package com.bot.bots.handlers;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
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
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
            CommonCache.put(message.getFrom().getId(), TempEnum.INPUT_PZ_EXPOSE_TEXT);
            return markdownReply(message, Constants.PZ_EXPOSE_TEMPLATE);
        }

        if (StrUtil.equals(text, "\u2764\uFE0F\uD83D\uDCBB我的余额")) {
            User user = this.userService.queryUser(message.getFrom());
            String selfText = user.buildSelfText();
            return markdownReply(message, selfText);
        }

        if (StrUtil.equals(text, "\uD83D\uDFE2供需发布")) {
            User user = this.userService.queryUser(message.getFrom());

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

        if (StrUtil.equals(text, "\uD83D\uDC81\uD83C\uDFFB\u200d\u2642\uFE0F承兑报备")) {
            List<Address> address = this.addressService.selectProvince();
            InlineKeyboardMarkup markup = KeyboardHelper.buildProvinceKeyboard(address, AddressParam.EXCHANGE.getCode());
            return ok(message, Constants.ACCEPTANCE_FILING_TEXT, markup);
        }

        if (StrUtil.equals(text, "\uD83D\uDE97车队报备")) {
            CommonCache.put(message.getFrom().getId(), TempEnum.CAR_TEAM_INPUT);
            return markdownReply(message, Constants.CAT_TEAM_TEXT);
        }


        if (StrUtil.equals(text, "\uD83D\uDC81\uD83C\uDFFC\u200d\u2640\uFE0F承兑驻地")) {
            List<Address> address = this.addressService.selectProvince();
            InlineKeyboardMarkup markup = KeyboardHelper.buildProvinceKeyboard(address, AddressParam.QUERY_EXCHANGE.getCode());
            return markdownReply(message, Constants.PROVINCE_LOCATION_TEXT, markup);
        }

        if (StrUtil.equals(text, "\uD83D\uDE95车队驻地")) {
            List<Address> address = this.addressService.selectProvince();
            InlineKeyboardMarkup markup = KeyboardHelper.buildProvinceKeyboard(address, AddressParam.QUERY_TEAM.getCode());
            return markdownReply(message, Constants.CAT_TEAM_ADDRESS_TEXT, markup);
        }

        if (StrUtil.equals(text, "\uD83D\uDCB9U商报备")) {
            List<Address> address = this.addressService.selectProvince();
            InlineKeyboardMarkup markup = KeyboardHelper.buildProvinceKeyboard(address, AddressParam.UNLOADING_PARTNER.getCode());
            return markdownReply(message, Constants.UNLOADING_PARTNER_TEXT, markup);
        }

        if (StrUtil.equals(text, "\uD83C\uDE2F\uFE0FU商驻地")) {
            List<Address> address = this.addressService.selectProvince();
            InlineKeyboardMarkup markup = KeyboardHelper.buildProvinceKeyboard(address, AddressParam.UNLOADING_LOCATION.getCode());
            return markdownReply(message, Constants.UNLOADING_LOCATION_TEXT, markup);
        }

        if (StrUtil.equals(text, "\uD83D\uDD0D查汇率")) {
            CommonCache.put(message.getFrom().getId(), TempEnum.CHECK_EXCHANGE_RATE_INPUT);
            return markdownReply(message, Constants.CHECK_EXCHANGE_RATE_TEXT);
        }

        if (StrUtil.startWith(text, "\u4FEE\u6539\u5BC6\u7801")) {
            List<String> parts = StrUtil.split(text, " ");
            if (parts.size() < 3) {
                return reply(message, "\u683C\u5F0F\uFF1A\u4FEE\u6539\u5BC6\u7801 \u65E7\u5BC6\u7801 \u65B0\u5BC6\u7801");
            }
            String oldPwd = parts.get(1);
            String newPwd = parts.get(2);
            User user = this.userService.queryUser(message.getFrom());

            if (StrUtil.isBlank(user.getPassword())) {
                return reply(message, "\u8D26\u53F7\u5C1A\u672A\u8BBE\u7F6E\u5BC6\u7801\uFF0C\u8BF7\u8054\u7CFB\u7BA1\u7406\u5458");
            }
            if (!StrUtil.equals(DigestUtil.md5Hex(oldPwd), user.getPassword())) {
                return reply(message, "\u65E7\u5BC6\u7801\u9519\u8BEF");
            }
            user.setPassword(DigestUtil.md5Hex(newPwd));
            this.userService.updateById(user);
            return reply(message, "\u5BC6\u7801\u4FEE\u6539\u6210\u529F");
        }

        PaymentEnum payment = PaymentEnum.of(text);
        if (Objects.nonNull(payment)) {
            Config config = this.configService.queryConfig();
            String query = this.parseAndQuery(text);
            if (Objects.isNull(query)) {
                return null;
            }
            InlineKeyboardMarkup keyboard = KeyboardHelper.keyboard(config.getQueryKeyboard());
            return markdownReply(message, query, keyboard);
        }

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
            boolean a = parsed.containsKey("\u4F9B\u9700\u65B9");
            boolean b = parsed.containsKey("\u9879\u76EE\u540D\u79F0");
            boolean c = parsed.containsKey("\u9879\u76EE\u4ECB\u7ECD");
            boolean d = parsed.containsKey("\u91D1\u989D");

            boolean e = a & b & c & d;
            if (!e) {
                return reply(message, "\uFF08\u4F9B\u9700\u65B9\u3001\u9879\u76EE\u540D\u79F0\u3001\u9879\u76EE\u4ECB\u7ECD\u3001\u91D1\u989D\uFF09\u5FC5\u586B\uFF01\uFF01");
            }

            Long auditId = this.properties.getAuditId();
            Publish p = Publish.build(text, message.getFrom().getId());
            this.publishService.save(p);

            InlineKeyboardMarkup markup = KeyboardHelper.buildAuditPublishKeyboard(p.getId());

            AsyncSender.async(this.markdown(auditId, text, markup));
            return reply(message, "\u2705\u8BF7\u8010\u5FC3\u7B49\u5F85\uFF0C\u5BA1\u6838\u4E2D...");
        }


        if (Objects.equals(temp, TempEnum.INPUT_PZ_EXPOSE_TEXT)) {
            String text = message.getText();
            Map<String, String> parsed = StrHelper.parseStrToMap(text);

            boolean needId = parsed.containsKey("\u9A97\u5B50ID");
            boolean needNick = parsed.containsKey("\u9A97\u5B50\u6635\u79F0");
            if (!(needId && needNick)) {
                return reply(message, "\uFF08\u9A97\u5B50ID\u3001\u9A97\u5B50\u6635\u79F0\uFF09\u5FC5\u586B\uFF01\uFF01");
            }

            Expose expose = new Expose()
                    .setUserId(message.getFrom().getId())
                    .setTextRaw(text)
                    .setPzUserId(parsed.getOrDefault("\u9A97\u5B50ID", ""))
                    .setPzNickname(parsed.getOrDefault("\u9A97\u5B50\u6635\u79F0", ""))
                    .setStory(parsed.getOrDefault("\u88AB\u9A97\u7ECF\u8FC7", ""))
                    .setPzAddress(parsed.getOrDefault("\u9A97\u5B50U\u5730\u5740", ""))
                    .setStatus(ExposeStatus.WAIT);

            this.exposeService.save(expose);

            Long auditGroupId = this.properties.getAuditId();
            InlineKeyboardMarkup auditKeyboard = KeyboardHelper.buildPzExposeAuditKeyboard(expose.getId());
            AsyncSender.async(this.markdown(auditGroupId, text, auditKeyboard));

            return reply(message, "\u2705\u5DF2\u63D0\u4EA4\u5BA1\u6838\uFF0C\u5BA1\u6838\u4E2D...");
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

            boolean a = map.containsKey("\u5E95\u6599");
            boolean b = map.containsKey("\u8F66\u961F\u7C7B\u578B");
            boolean c = map.containsKey("\u6240\u5728\u7701\u5E02");

            boolean e = a & b & c;
            if (!e) {
                return reply(message, "\uFF08\u5E95\u6599\u3001\u8F66\u961F\u7C7B\u578B\u3001\u6240\u5728\u7701\u5E02\uFF09\u5FC5\u586B\uFF01\uFF01");
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
                return ok(message, "\u8BF7\u8F93\u5165\u6574\u6570");
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
