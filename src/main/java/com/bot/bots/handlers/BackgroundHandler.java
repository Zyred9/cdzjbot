package com.bot.bots.handlers;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.bots.config.BotProperties;
import com.bot.bots.database.entity.Config;
import com.bot.bots.database.entity.User;
import com.bot.bots.database.service.ConfigService;
import com.bot.bots.database.service.UserService;
import com.bot.bots.helper.KeyboardHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
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
@RequiredArgsConstructor
public class BackgroundHandler extends AbstractHandler {

    private final UserService userService;
    private final BotProperties properties;
    private final ConfigService configService;


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

            盘总回复#
            &内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容
            &按钮1#https://t.me/xmkfjqr|按钮2#https://t.me/xmkfjqr$按钮3#https://t.me/xmkfjqr
        */
        Config config = this.configService.queryConfig();

        if (StrUtil.equalsAny("设置客服", "盘总回复", commands.get(0))) {
            boolean custom = StrUtil.equalsAny("设置客服", commands.get(0));

            String content = StrUtil.trim(commands.get(1));
            List<String> contentAndKeyboard = StrUtil.split(content, "&");
            String contentText = StrUtil.trim(contentAndKeyboard.get(0));
            String contentKeyboard = StrUtil.trim(contentAndKeyboard.get(1));

            if (custom) {
                config.setCustomText(contentText);
                config.setCustomKeyboard(KeyboardHelper.parseKeyboard(contentKeyboard));
            } else {
                config.setSelfText(contentText);
                config.setSelfKeyboard(KeyboardHelper.parseKeyboard(contentKeyboard));
            }
            this.configService.updateById(config);
            return reply(message);
        }

        // 查看可编辑
        if (StrUtil.equalsAny("查看可编辑", commands.get(0))) {
            List<Long> editable = config.getEditable();
            StringBuilder sb = new StringBuilder("页面可编辑用户：\n");
            for (Long l : editable) {
                sb.append(l).append("\n");
            }
            return reply(message, sb.toString());
        }

        // 设置可编辑#用户id1,用户id2....
        // 删除可编辑#用户id1,用户id2....
        if (StrUtil.equalsAny("设置可编辑", "删除可编辑", commands.get(0))) {

            boolean add = StrUtil.equalsAny("设置可编辑", commands.get(0));

            String ids = commands.get(1);
            String[] userIds = ids.split("[,，]");

            List<Long> userIdList = new ArrayList<>(userIds.length);
            for (String userId : userIds) {
                userIdList.add(Long.parseLong(userId));
            }

            List<Long> editable = config.getEditable();
            if (add) {
                editable.addAll(userIdList);
            } else {
                editable.removeAll(userIdList);
            }
            return reply(message);
        }

        // 余额#用户id#+100
        // 余额#用户id#-100

        if (StrUtil.equals(commands.get(0), "余额")) {
            long userId = Long.parseLong(commands.get(1));
            BigDecimal amount = new BigDecimal(commands.get(2));

            User user = this.userService.getById(userId);
            user.setBalance(user.getBalance().add(amount));
            this.userService.updateById(user);
            return reply(message);
        }





        return null;
    }
}
