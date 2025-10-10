package com.bot.bots.handlers;

import cn.hutool.core.util.StrUtil;
import com.bot.bots.config.BotProperties;
import com.bot.bots.config.Constants;
import com.bot.bots.database.entity.Expose;
import com.bot.bots.database.entity.Publish;
import com.bot.bots.database.entity.User;
import com.bot.bots.database.enums.ExposeStatus;
import com.bot.bots.database.service.*;
import com.bot.bots.helper.KeyboardHelper;
import com.bot.bots.sender.AsyncSender;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import javax.annotation.Resource;
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
@Component
public class CallbackHandler extends AbstractHandler {

    @Resource private UserService userService;
    @Resource private BotProperties properties;
    @Resource private ConfigService configService;
    @Resource private PublishService publishService;
    @Resource private RechargeService rechargeService;
    @Resource private ExposeService exposeService;

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
        Message message = (Message) callbackQuery.getMessage();

        if (StrUtil.equals(text, "delete") ){
            return delete(message);
        }

        List<String> commands = StrUtil.split(text, "#");
        if (StrUtil.equals(commands.get(0), "group")) {
            return this.processorGroupCallback(callbackQuery, commands, message);
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
