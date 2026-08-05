package com.bot.bots.sender;

import cn.hutool.json.JSONUtil;
import com.bot.bots.config.BotProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;


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
public class AsyncSender {

    @Resource private BotProperties properties;
    @Resource private TelegramClient telegramClient;

    private static final LinkedBlockingQueue<PartialBotApiMethod<?>> QUEUE = new LinkedBlockingQueue<>();
    private static final long MIN_INTERVAL_MS = 50L;

    public static void async(PartialBotApiMethod<?> message) {
        if (Objects.isNull(message)) {
            return;
        }
        QUEUE.add(message);
    }

    public AsyncSender() {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                PartialBotApiMethod<?> take = null;
                try {
                    long start = System.currentTimeMillis();
                    take = QUEUE.take();
                    if (properties.isLogs()) {
                        log.info("【异步】发送：{}", JSONUtil.toJsonStr(take));
                    }
                    processorSend(take);
                    long elapsed = System.currentTimeMillis() - start;
                    if (elapsed < MIN_INTERVAL_MS) {
                        Thread.sleep(MIN_INTERVAL_MS - elapsed);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (TelegramApiException e) {
                    log.error("【异步发送异常】消息内容：{}，错误信息：{}", JSONUtil.toJsonStr(take), e.getMessage(), e);
                }
            }
        }).start();
    }

    private void processorSend(PartialBotApiMethod<?> take) throws TelegramApiException {
        if (take instanceof SendMessage message) {
            try {
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                if (e.getMessage() != null && e.getMessage().contains("message to be replied not found")) {
                    message.setReplyToMessageId(null);
                    telegramClient.execute(message);
                    return;
                }
                throw e;
            }
            return;
        }
        if (take instanceof DeleteMessage delete) {
            telegramClient.execute(delete);
            return;
        }
        if (take instanceof SendPhoto photo) {
            telegramClient.execute(photo);
            return;
        }
        if (take instanceof SendVideo video) {
            telegramClient.execute(video);
            return;
        }
        if (take instanceof EditMessageText edit) {
            telegramClient.execute(edit);
            return;
        }
        if (take instanceof EditMessageCaption caption) {
            telegramClient.execute(caption);
            return;
        }
        if (take instanceof EditMessageReplyMarkup markup) {
            telegramClient.execute(markup);
            return;
        }
        if (take instanceof AnswerCallbackQuery answer) {
            telegramClient.execute(answer);
            return;
        }
        if (take instanceof SendAnimation animation) {
            telegramClient.execute(animation);
            return;
        }
        if (take instanceof SetMyCommands cmd) {
            telegramClient.execute(cmd);
            return;
        }
        if (take instanceof SendDocument doc) {
            telegramClient.execute(doc);
        }
    }
}
