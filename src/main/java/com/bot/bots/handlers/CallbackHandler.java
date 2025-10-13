package com.bot.bots.handlers;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bot.bots.beans.cache.CommonCache;
import com.bot.bots.beans.view.ctx.AcceptanceContext;
import com.bot.bots.config.BotProperties;
import com.bot.bots.config.Constants;
import com.bot.bots.database.entity.*;
import com.bot.bots.database.enums.*;
import com.bot.bots.database.service.*;
import com.bot.bots.helper.KeyboardHelper;
import com.bot.bots.sender.AsyncSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

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
@RequiredArgsConstructor
public class CallbackHandler extends AbstractHandler {

    private final UserService userService;
    private final BotProperties properties;
    private final ConfigService configService;
    private final ExposeService exposeService;
    private final AddressService addressService;
    private final PublishService publishService;
    private final RechargeService rechargeService;
    private final AcceptanceCtxService acceptanceCtxService;

    @Override
    public boolean support(Update update) {
        return Objects.nonNull(update.getCallbackQuery())
                && Objects.nonNull(update.getCallbackQuery().getMessage())
                && (update.getCallbackQuery().getMessage().isUserMessage()
                || update.getCallbackQuery().getMessage().isGroupMessage()
                || update.getCallbackQuery().getMessage().isSuperGroupMessage());
    }

    @Override
    protected BotApiMethod<?> execute(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String text = callbackQuery.getData();

        log.info("[回调事件] {}", text);
        Message message = (Message) callbackQuery.getMessage();

        if (StrUtil.equals(text, "delete") ){
            CommonCache.cleanCache(callbackQuery.getFrom().getId());
            return delete(message);
        }

        List<String> commands = StrUtil.split(text, "#");
        if (StrUtil.equals(commands.get(0), "group")) {
            return this.processorGroupCallback(callbackQuery, commands, message);
        }

        if (StrUtil.equals(commands.get(0), "privacy")) {
            return this.processorPrivacyCallback(callbackQuery, commands, message);
        }

        return null;
    }

    private BotApiMethod<?> processorPrivacyCallback(CallbackQuery callbackQuery, List<String> commands, Message message) {
        if (StrUtil.equals(commands.get(1), "province")) {
            int query = Integer.parseInt(commands.get(4));
            List<Address> address = this.addressService.selectProvince();
            InlineKeyboardMarkup markup = KeyboardHelper.buildProvinceKeyboard(address, query);
            return editMarkdown(message, "（请详细到区县）例：浙江省杭州市西湖区", markup);
        }

        if (StrUtil.equals(commands.get(1), "city")) {
            String provinceCode = commands.get(2);
            int index = Integer.parseInt(commands.get(3));
            int query = Integer.parseInt(commands.get(4));
            Address addr = this.addressService.selectOneProvince(provinceCode);

            AcceptanceContext ctx = AcceptanceContext.builder()
                    .provinceName(addr.getProvinceName())
                    .build();
            CommonCache.putAccCtx(callbackQuery.getFrom().getId(), ctx);

            Page<Address> cityList = this.addressService.selectCity(index, provinceCode);
            InlineKeyboardMarkup markup = KeyboardHelper.buildCityKeyboard(cityList, provinceCode, query);

            String text = CommonCache.accCtxText(callbackQuery.getFrom().getId());
            return editMarkdown(message, text, markup);
        }

        if (StrUtil.equals(commands.get(1), "county")) {
            String cityCode = commands.get(2);
            int index = Integer.parseInt(commands.get(3));
            int query = Integer.parseInt(commands.get(4));
            Address address = this.addressService.selectOneCity(cityCode);

            Page<Address> countyList = this.addressService.selectCounty(index, cityCode);
            InlineKeyboardMarkup markup = KeyboardHelper.buildCountyKeyboard(countyList, cityCode, address.getProvinceCode(), query);

            CommonCache.getAccCtx(callbackQuery.getFrom().getId()).setCityName(address.getCityName());
            String text = CommonCache.accCtxText(callbackQuery.getFrom().getId());
            return editMarkdown(message, text, markup);
        }

        if (StrUtil.equals(commands.get(1), "category")) {
            String countyCode = commands.get(2);
            int query = Integer.parseInt(commands.get(4));
            AcceptanceContext ctx = CommonCache.getAccCtx(callbackQuery.getFrom().getId());

            // 承兑和承兑所在地
            if (Objects.equals(query, AddressParam.EXCHANGE.getCode()) || Objects.equals(query, AddressParam.QUERY_EXCHANGE.getCode())){
                Address address = this.addressService.selectOneCounty(countyCode);
                ctx.setCountyName(address.getCountyName());
                InlineKeyboardMarkup markup = KeyboardHelper.buildCategoryKeyboard(ctx.getCategories(), query);

                String text = CommonCache.accCtxText(callbackQuery.getFrom().getId());
                return editMarkdown(message, text, markup);
            }

            // 车队所在地
            if (Objects.equals(query, AddressParam.QUERY_TEAM.getCode())){
                ctx.setShowScope(true);
                InlineKeyboardMarkup markup = KeyboardHelper.buildScopeKeyboard(query, ctx.getScope());
                String text = CommonCache.accCtxText(callbackQuery.getFrom().getId());
                return editMarkdown(message, text, markup);
            }

        }

        //
        if (StrUtil.equals(commands.get(1), "scope")) {
            String scope = commands.get(2);
            int query = Integer.parseInt(commands.get(3));
            AcceptanceContext ctx = CommonCache.getAccCtx(callbackQuery.getFrom().getId());

            if (StrUtil.equals(scope, "custom")) {
                CommonCache.put(callbackQuery.getFrom().getId(), TempEnum.EXCHANGE_INPUT_CUSTOM_SCOPE);
                return editMarkdown(message, "请输入自定义的范围值（整数类型）");
            } else if (StrUtil.equals(scope, "confirm")) {
                String address = ctx.getAllAddress();
                // TODO 使用高德地图查询距离
                String text = CommonCache.accCtxText(callbackQuery.getFrom().getId());
                InlineKeyboardMarkup markup = KeyboardHelper.buildLoadKeyboard();
                return editMarkdown(message, text, markup);
            } else {
                int i = Integer.parseInt(scope);
                ctx.setScope(i);

                String text = CommonCache.accCtxText(callbackQuery.getFrom().getId());
                InlineKeyboardMarkup markup = KeyboardHelper.buildScopeKeyboard(query, ctx.getScope());
                return editMarkdown(message, text, markup);
            }
        }

        if (StrUtil.equals(commands.get(1), "category_change")) {
            String categoryCode = commands.get(2);
            int query = Integer.parseInt(commands.get(3));
            CategoryEnum of = CategoryEnum.of(categoryCode);
            if (Objects.isNull(of)) {
                return answer(callbackQuery, "本条消息已经过期，请重新发送获取新的报备数据！！");
            }

            AcceptanceContext ctx = CommonCache.getAccCtx(callbackQuery.getFrom().getId());
            // 承兑
            if (Objects.equals(query, AddressParam.EXCHANGE.getCode())){
                ctx.setCategories(of);
                List<CategoryEnum> categories = CommonCache.getAccCtx(callbackQuery.getFrom().getId()).getCategories();

                InlineKeyboardMarkup markup = KeyboardHelper.buildCategoryKeyboard(categories, query);
                String text = CommonCache.accCtxText(callbackQuery.getFrom().getId());
                return editMarkdown(message, text, markup);
            }

            // 承兑所在地
            if (Objects.equals(query, AddressParam.QUERY_EXCHANGE.getCode())){
                ctx.setShowScope(true);
                InlineKeyboardMarkup markup = KeyboardHelper.buildScopeKeyboard(query, ctx.getScope());
                String text = CommonCache.accCtxText(callbackQuery.getFrom().getId());
                return editMarkdown(message, text, markup);
            }

        }

        if (StrUtil.equals(commands.get(1), "input")) {
            String inputType = commands.get(2);
            String value = commands.get(3);
            // 金额的区间
            if (StrUtil.equals(inputType, "interval")) {
                if (StrUtil.equals(value, "show")) {

                    AcceptanceContext ctx = CommonCache.getAccCtx(callbackQuery.getFrom().getId());

                    if (CollUtil.isEmpty(ctx.getCategories())) {
                        return answerAlert(callbackQuery, "❌请选择一个或多个物品分类！");
                    }

                    ctx.setWaitInputInterval(Boolean.TRUE);
                    InlineKeyboardMarkup markup = KeyboardHelper.buildInputIntervalKeyboard();
                    String text = CommonCache.accCtxText(callbackQuery.getFrom().getId());
                    return editMarkdown(message, text, markup);
                }

                if (StrUtil.equals(value, "input")) {
                    CommonCache.put(callbackQuery.getFrom().getId(), TempEnum.INPUT_INTERVAL);
                    return editMarkdown(message, "✍️请输入你的范围");
                }
            }

            if (StrUtil.equals(inputType, "rate")) {
                InlineKeyboardMarkup markup = null;
                AcceptanceContext ctx = CommonCache.getAccCtx(callbackQuery.getFrom().getId());
                if (StrUtil.equals(value, "increment")) {
                    ctx.incrementRate(0.05);
                    markup = KeyboardHelper.buildRateIncrementKeyboard();
                }

                if (StrUtil.equals(value, "subtract")) {
                    ctx.subtractRate(0.05);
                    markup = KeyboardHelper.buildRateIncrementKeyboard();
                }

                if (StrUtil.equals(value, "confirm")) {
                    markup = KeyboardHelper.buildMaterialRemarksKeyboard(ctx.getMaterial());
                }

                String text = CommonCache.accCtxText(callbackQuery.getFrom().getId());
                return editMarkdown(message, text, markup);
            }
        }

        // 料性
        if (StrUtil.equals(commands.get(1), "material")){
            String val = commands.get(2);
            AcceptanceContext ctx = CommonCache.getAccCtx(callbackQuery.getFrom().getId());
            InlineKeyboardMarkup markup;
            if (StrUtil.equals(val, "confirm")) {
                if (Objects.isNull(ctx.getMaterial())) {
                    return answerAlert(callbackQuery, "❌请选择一个料性！");
                }
                markup = KeyboardHelper.buildForbidKeyboard(ctx.getForbids());
            } else {
                MaterialEnum materialEnum = MaterialEnum.ofCode(val);
                ctx.setMaterial(materialEnum);
                markup = KeyboardHelper.buildMaterialRemarksKeyboard(materialEnum);
            }
            String text = CommonCache.accCtxText(callbackQuery.getFrom().getId());
            return editMarkdown(message, text, markup);
        }

        // 禁止
        if (StrUtil.equals(commands.get(1), "forbid")) {
            String val = commands.get(2);
            AcceptanceContext ctx = CommonCache.getAccCtx(callbackQuery.getFrom().getId());
            InlineKeyboardMarkup markup;
            if (StrUtil.equals(val, "confirm")) {
                if (CollUtil.isEmpty(ctx.getForbids())) {
                    return answerAlert(callbackQuery, "❌请选择一个或多个禁止类型！");
                }
                ctx.setShowAirborne(true);
                markup = KeyboardHelper.buildAirborneKeyboard(ctx.getAirborne());
            } else {
                ctx.setForbids(ForbidTypeEnum.of(val));
                markup = KeyboardHelper.buildForbidKeyboard(ctx.getForbids());
            }
            String text = CommonCache.accCtxText(callbackQuery.getFrom().getId());
            return editMarkdown(message, text, markup);
        }

        // 空降
        if (StrUtil.equals(commands.get(1), "airborne")) {
            String val = commands.get(2);
            AcceptanceContext ctx = CommonCache.getAccCtx(callbackQuery.getFrom().getId());
            InlineKeyboardMarkup markup;
            if (StrUtil.equals(val, "confirm")) {
                if (Objects.isNull(ctx.getAirborne())) {
                    return answerAlert(callbackQuery, "❌请选择是否空降！");
                }
                ctx.setShowStation(true);
                markup = KeyboardHelper.buildStationKeyboard(ctx.getStation());
            } else {
                ctx.setAirborne(Boolean.valueOf(val));
                markup = KeyboardHelper.buildAirborneKeyboard(ctx.getAirborne());
            }
            String text = CommonCache.accCtxText(callbackQuery.getFrom().getId());
            return editMarkdown(message, text, markup);
        }

        // 驻点
        if (StrUtil.equals(commands.get(1), "station")) {
            String val = commands.get(2);
            AcceptanceContext ctx = CommonCache.getAccCtx(callbackQuery.getFrom().getId());
            InlineKeyboardMarkup markup;
            if (StrUtil.equals(val, "confirm")) {
                if (Objects.isNull(ctx.getStation())) {
                    return answerAlert(callbackQuery, "❌请选择是否驻点！");
                }
                ctx.setShowMove(true);
                markup = KeyboardHelper.buildMoveKeyboard(ctx.getMove());
            } else {
                ctx.setStation(Boolean.valueOf(val));
                markup = KeyboardHelper.buildStationKeyboard(ctx.getStation());
            }
            String text = CommonCache.accCtxText(callbackQuery.getFrom().getId());
            return editMarkdown(message, text, markup);
        }

        // 移动
        if (StrUtil.equals(commands.get(1), "move")) {
            String val = commands.get(2);
            AcceptanceContext ctx = CommonCache.getAccCtx(callbackQuery.getFrom().getId());
            InlineKeyboardMarkup markup;
            if (StrUtil.equals(val, "confirm")) {
                if (Objects.isNull(ctx.getMove())) {
                    return answerAlert(callbackQuery, "❌请选择是否移动！");
                }
                ctx.setShowFollow(true);
                markup = KeyboardHelper.buildFollowKeyboard(ctx.getFollow());
            } else {
                ctx.setMove(Boolean.valueOf(val));
                markup = KeyboardHelper.buildMoveKeyboard(ctx.getMove());
            }
            String text = CommonCache.accCtxText(callbackQuery.getFrom().getId());
            return editMarkdown(message, text, markup);
        }

        // 跟车
        if (StrUtil.equals(commands.get(1), "follow")) {
            String val = commands.get(2);
            AcceptanceContext ctx = CommonCache.getAccCtx(callbackQuery.getFrom().getId());
            InlineKeyboardMarkup markup;
            if (StrUtil.equals(val, "confirm")) {
                if (Objects.isNull(ctx.getFollow())) {
                    return answerAlert(callbackQuery, "❌请选择是否跟车！");
                }
                markup = KeyboardHelper.buildExchangeReportKeyboard();
            } else {
                ctx.setFollow(Boolean.valueOf(val));
                markup = KeyboardHelper.buildFollowKeyboard(ctx.getFollow());
            }
            String text = CommonCache.accCtxText(callbackQuery.getFrom().getId());
            return editMarkdown(message, text, markup);
        }

        // 提交（公共）
        if (StrUtil.equals(commands.get(1), "commit")) {
            String val = commands.get(2);

            // 提交承兑报备
            if (StrUtil.equals(val, "exchange_report")) {
                String text = CommonCache.accCtxText(callbackQuery.getFrom().getId());

                AcceptanceContext ctx = CommonCache.getRemoveAccCtx(callbackQuery.getFrom().getId());
                AcceptanceCtx acceptanceCtx = ctx.buildCtx();
                acceptanceCtx.setUserId(callbackQuery.getFrom().getId());
                acceptanceCtx.setUsername(callbackQuery.getFrom().getUserName());
                acceptanceCtx.setNickname(callbackQuery.getFrom().getFirstName());

                this.acceptanceCtxService.save(acceptanceCtx);
                return editMarkdown(message, text);
            }

            return null;
        }

        if (StrUtil.equals(commands.get(1), "loading")) {
            return answerAlert(callbackQuery, "正在查询中，请耐心等待查询结果...");
        }


        return null;
    }

    private BotApiMethod<?> processorGroupCallback(CallbackQuery callbackQuery, List<String> commands, Message message) {

        if (StrUtil.equals(commands.get(1), "publish")) {
            boolean value = Boolean.parseBoolean(commands.get(2));
            Publish p = this.publishService.getById(commands.get(3));
            if (Objects.isNull(p)) {
                return answer(callbackQuery, "发起的审批不存在！！");
            }

            User user = this.userService.getById(p.getUserId());
            if (value) {
                user.setBalance(user.getBalance().subtract(Constants.COST));
                this.userService.updateById(user);

                Long publishId = this.properties.getPublishId();
                // 响应给用户
                AsyncSender.async(this.ok(user.getUserId(), "✅你的发布已通过！！"));
                // 响应到频道
                AsyncSender.async(this.markdown(publishId, p.getText(), KeyboardHelper.buildPublishChannelKeyboard()));
                // 响应给审核群
                return this.editMarkdown(message, message.getText(), KeyboardHelper.buildCommonDel("✅完成(点击删除本消息)"));
            }

            AsyncSender.async(ok(user.getUserId(), "❌你的发布不通过，有问题联系客服"));
            return this.editMarkdown(message, message.getText(), KeyboardHelper.buildCommonDel("❌拒绝(点击删除本消息)"));
        }

        // 骗子曝光审核：group#expose#{true|false}#{id}
        if (StrUtil.equals(commands.get(1), "expose")) {
            boolean value = Boolean.parseBoolean(commands.get(2));
            Long exposeId = Long.valueOf(commands.get(3));
            Expose expose = this.exposeService.getById(exposeId);
            if (Objects.isNull(expose)) {
                return answer(callbackQuery, "发起的审批不存在！！");
            }

            User user = this.userService.getById(expose.getUserId());

            if (value) {
                Long channelId = this.properties.getExposureId();
                // 发布到频道，附三按钮
                AsyncSender.async(this.markdown(channelId, expose.getTextRaw(), KeyboardHelper.buildPublishChannelKeyboard()));
                // 更新状态与审计信息
                this.exposeService.updateStatusAndAudit(exposeId, ExposeStatus.APPROVED);
                // 通知用户
                AsyncSender.async(this.ok(user.getUserId(), "骗子曝光已发布"));
                // 编辑审核群消息为“完成”
                return this.editMarkdown(message, message.getText(), KeyboardHelper.buildCommonDel("✅完成(点击删除本消息)"));
            }

            // 拒绝
            this.exposeService.updateStatusAndAudit(exposeId, ExposeStatus.REJECTED);
            AsyncSender.async(ok(user.getUserId(), "骗子发布已拒绝，请填写的更加详细哦，有问题联系客服"));
            return this.editMarkdown(message, message.getText(), KeyboardHelper.buildCommonDel("❌拒绝(点击删除本消息)"));
        }

        return null;
    }

}
