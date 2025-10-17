package com.bot.bots.handlers;

import com.bot.bots.config.BotProperties;
import com.bot.bots.database.entity.Config;
import com.bot.bots.database.enums.PaymentEnum;
import com.bot.bots.database.service.ConfigService;
import com.bot.bots.helper.KeyboardHelper;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * <p>
 *     普通群
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
@Component
public class PublicChatHandler extends AbstractHandler {

    @Resource private BotProperties properties;
    @Resource private ConfigService configService;
    @Resource private PrivateChatHandler privateChatHandler;

    @Override
    public boolean support(Update update) {
        return update.hasMessage()
                && update.getMessage().hasText()
                && (update.getMessage().getChat().isGroupChat() || update.getMessage().getChat().isSuperGroupChat())
                && !properties.fromBackground(update.getMessage().getChatId());
    }

    @Override
    protected BotApiMethod<?> execute(Update update) {
        Message message = update.getMessage();
        String text = message.getText();

        PaymentEnum payment = PaymentEnum.of(text);
        if (Objects.nonNull(payment)) {
            Config config = this.configService.queryConfig();
            String query = this.privateChatHandler.parseAndQuery(text);
            InlineKeyboardMarkup keyboard = KeyboardHelper.keyboard(config.getQueryKeyboard());
            return markdownReply(message, query, keyboard);
        }

        return null;
    }

}
