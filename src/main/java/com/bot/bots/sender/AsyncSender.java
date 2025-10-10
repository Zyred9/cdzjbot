package com.bot.bots.sender;

import cn.hutool.core.thread.GlobalThreadPool;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.bot.bots.helper.ThreadHelper;
import lombok.extern.slf4j.Slf4j;
import com.bot.bots.config.BotProperties;
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
    private static final LinkedBlockingQueue<PartialBotApiMethod<?>> DELAY_QUEUE = new LinkedBlockingQueue<>();

    public static void async (PartialBotApiMethod<?> message) {
        if (Objects.isNull(message)) {
            return;
        }
        if (QUEUE.size() >= 20) {
            DELAY_QUEUE.add(message);
            log.info("[主队列消息超过20条] 加入到子队列，子队列大小：{}， 消息内容：{}", DELAY_QUEUE.size(), JSONUtil.toJsonStr(message));
        } else {
            QUEUE.add(message);
        }
    }

    public AsyncSender() {
        this.run();
        this.delayRun();
    }

    private void delayRun() {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                PartialBotApiMethod<?> take = null;
                try {
                    ThreadHelper.sleepMs(RandomUtil.randomInt(200, 500));
                    take = DELAY_QUEUE.take();
                    this.processorSend(take);
                } catch (InterruptedException | TelegramApiException e) {
                    log.error("【异步发送异常】消息内容：{}，错误信息：{}", JSONUtil.toJsonStr(take), e.getMessage(), e);
                }
            }
        }).start();
    }

    private void run() {
        ThreadHelper.execute(() -> {
            while (!Thread.interrupted()) {
                PartialBotApiMethod<?> take = null;
                try {
                    take = QUEUE.take();
                    if (this.properties.isLogs()) {
                        log.info("【异步】发送：{}", JSONUtil.toJsonStr(take));
                    }
                    this.processorSend(take);
                } catch (InterruptedException | TelegramApiException e) {
                    log.error("【异步发送异常】消息内容：{}，错误信息：{}", JSONUtil.toJsonStr(take), e.getMessage(), e);
                }
            }
        });
    }

    private void processorSend(PartialBotApiMethod<?> take) throws TelegramApiException {
        if (take instanceof SendMessage message) {
            telegramClient.execute(message);
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
