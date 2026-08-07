package com.bot.bots.handlers;

import cn.hutool.core.util.StrUtil;
import com.bot.bots.config.BotProperties;
import com.bot.bots.database.entity.Config;
import com.bot.bots.database.enums.PaymentEnum;
import com.bot.bots.database.service.BroadcastGroupService;
import com.bot.bots.database.service.ConfigService;
import com.bot.bots.helper.JexlCalculator;
import com.bot.bots.helper.KeyboardHelper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.regex.Pattern;

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

    private static final Pattern MATH_PATTERN = Pattern.compile("^[\\d\\s+\\-*/().（）×÷]+$");

    @Resource private BotProperties properties;
    @Resource private ConfigService configService;
    @Resource private PrivateChatHandler privateChatHandler;
    @Resource private BroadcastGroupService broadcastGroupService;

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

        try {
            this.broadcastGroupService.createIfAbsent(message.getChatId(), message.getChat().getTitle());
        } catch (DuplicateKeyException ignore) {
            // 并发下另一线程已登记该群，chat_id 主键冲突忽略
        }

        PaymentEnum payment = PaymentEnum.of(text);
        if (Objects.nonNull(payment)) {
            Config config = this.configService.queryConfig();
            String query = this.privateChatHandler.parseAndQuery(text);
            if (Objects.isNull(query)) {
                return null;
            }
            InlineKeyboardMarkup keyboard = KeyboardHelper.keyboard(config.getQueryKeyboard());
            return markdownReply(message, query, keyboard);
        }

        if (this.isMathExpression(text)) {
            return this.handleMath(message, text);
        }

        return null;
    }

    private boolean isMathExpression(String text) {
        if (StrUtil.isBlank(text) || text.length() > 200) {
            return false;
        }
        if (!MATH_PATTERN.matcher(text).matches()) {
            return false;
        }
        return text.matches(".*\\d.*") && text.matches(".*[+\\-*/].*");
    }

    private BotApiMethod<?> handleMath(Message message, String expression) {
        try {
            String normalized = normalizeExpression(expression);
            String result = JexlCalculator.calculateStr(normalized);
            return markdownReply(message, "`" + expression + "=" + result + "`");
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeExpression(String expression) {
        return expression
                .replace('（', '(')
                .replace('）', ')')
                .replace('×', '*')
                .replace('÷', '/');
    }

}
