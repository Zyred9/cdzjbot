package com.bot.bots.handlers;

import cn.hutool.core.collection.CollUtil;
import com.bot.bots.config.BotProperties;
import com.bot.bots.database.service.BroadcastGroupService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 *     机器人进群/退群监听：进群登记群组，退群移除群组
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
@Component
public class GroupMemberHandler extends AbstractHandler {

    @Resource private BotProperties properties;
    @Resource private BroadcastGroupService broadcastGroupService;

    @Override
    public boolean support(Update update) {
        if (!update.hasMessage()) {
            return false;
        }
        Message message = update.getMessage();
        if (!(message.getChat().isGroupChat() || message.getChat().isSuperGroupChat())) {
            return false;
        }
        Long botId = this.properties.getBotId();
        List<User> newChatMembers = message.getNewChatMembers();
        if (CollUtil.isNotEmpty(newChatMembers)) {
            return newChatMembers.stream().anyMatch(u -> Objects.equals(u.getId(), botId));
        }
        User leftChatMember = message.getLeftChatMember();
        return Objects.nonNull(leftChatMember) && Objects.equals(leftChatMember.getId(), botId);
    }

    @Override
    protected BotApiMethod<?> execute(Update update) {
        Message message = update.getMessage();
        if (CollUtil.isNotEmpty(message.getNewChatMembers())) {
            try {
                this.broadcastGroupService.createIfAbsent(message.getChatId(), message.getChat().getTitle());
            } catch (DuplicateKeyException ignore) {
                // 并发进群消息下另一线程已登记该群，chat_id 主键冲突忽略
            }
        } else {
            this.broadcastGroupService.removeByChatId(message.getChatId());
        }
        return null;
    }
}