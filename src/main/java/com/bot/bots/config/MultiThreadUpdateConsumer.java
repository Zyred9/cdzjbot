package com.bot.bots.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.util.StrUtil;
import com.bot.bots.helper.ThreadHelper;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
public interface MultiThreadUpdateConsumer extends LongPollingUpdateConsumer {

    Map<String, List<Update>> TEMP_UPDATES = new java.util.concurrent.ConcurrentHashMap<>();

    ThreadPoolExecutor EX = ExecutorBuilder.create()
            .setCorePoolSize(2)
            .setMaxPoolSize(100)
            .setThreadFactory(ThreadFactoryBuilder.create()
                    .setNamePrefix("long_poll_")
                    .build())
            .build();

    default void consume(List<Update> updates) {
        boolean hasMedia = false;
        List<Update> normalUpdates = new ArrayList<>();
        for (Update update : updates) {
            if (update.hasMessage() && (update.getMessage().hasPhoto()
                    || update.getMessage().hasVideo())
                    && StrUtil.isNotBlank(update.getMessage().getMediaGroupId())) {
                String mediaGroupId = update.getMessage().getMediaGroupId();
                TEMP_UPDATES.computeIfAbsent(mediaGroupId, k -> new ArrayList<>()).add(update);
                hasMedia = true;
            } else {
                normalUpdates.add(update);
            }
        }

        if (CollUtil.isNotEmpty(normalUpdates)) {
            EX.execute(() -> {
                CollUtil.sort(normalUpdates, (o1, o2) -> o1.getUpdateId() - o2.getUpdateId());
                for (Update update : normalUpdates) {
                    this.consume(update);
                }
            });
        }

        if (hasMedia) {
            ThreadHelper.execute(() -> {
                ThreadHelper.sleep(2);
                for (Map.Entry<String, List<Update>> entry : TEMP_UPDATES.entrySet()) {
                    List<Update> mergeUpdates = TEMP_UPDATES.remove(entry.getKey());
                    if (CollUtil.isEmpty(mergeUpdates)) {
                        continue;
                    }
                    CollUtil.sort(mergeUpdates, (o1, o2) -> o1.getUpdateId() - o2.getUpdateId());
                    for (Update update : mergeUpdates) {
                        this.consume(update);
                    }
                }
            });
        }
    }

    void consume(Update updates);
}
