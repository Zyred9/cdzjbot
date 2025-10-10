package com.bot.bots.handlers;

import cn.hutool.core.util.StrUtil;
import com.bot.bots.config.BotProperties;
import com.bot.bots.database.entity.Config;
import com.bot.bots.database.service.ConfigService;
import com.bot.bots.helper.KeyboardHelper;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
@Component
public class BackgroundHandler extends AbstractHandler {

    @Resource private BotProperties properties;
    @Resource private ConfigService configService;

    @Override
    public boolean support(Update update) {
        return update.hasMessage()
                && update.getMessage().hasText()
                && (update.getMessage().getChat().isGroupChat() || update.getMessage().getChat().isSuperGroupChat())
                && this.properties.fromBackground(update.getMessage().getChatId());
    }

    @Override
    protected BotApiMethod<?> execute(Update update) {
        Message message = update.getMessage();
        String text = message.getText();
        List<String> commands = StrUtil.split(text, "#");

        /*
            客服回复#
            &内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容
            &按钮1#https://t.me/xmkfjqr|按钮2#https://t.me/xmkfjqr$按钮3#https://t.me/xmkfjqr
        */
        if (StrUtil.equals("设置客服", commands.get(0))) {
            Config config = this.configService.queryConfig();

            String content = StrUtil.trim(commands.get(1));
            List<String> contentAndKeyboard = StrUtil.split(content, "&");
            String contentText = StrUtil.trim(contentAndKeyboard.get(0));
            String contentKeyboard = StrUtil.trim(contentAndKeyboard.get(1));

            config.setCustomText(contentText);
            config.setCustomKeyboard(KeyboardHelper.parseKeyboard(contentKeyboard));
            this.configService.updateById(config);
            return reply(message);
        }

         /*
            盘总回复#
            &内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容
            &按钮1#https://t.me/xmkfjqr|按钮2#https://t.me/xmkfjqr$按钮3#https://t.me/xmkfjqr
        */
        if (StrUtil.equals("盘总回复", commands.get(0))) {
            Config config = this.configService.queryConfig();

            String content = StrUtil.trim(commands.get(1));
            List<String> contentAndKeyboard = StrUtil.split(content, "&");
            String contentText = StrUtil.trim(contentAndKeyboard.get(0));
            String contentKeyboard = StrUtil.trim(contentAndKeyboard.get(1));

            config.setSelfText(contentText);
            config.setSelfKeyboard(KeyboardHelper.parseKeyboard(contentKeyboard));
            this.configService.updateById(config);
            return reply(message);
        }

        return null;
    }
}
