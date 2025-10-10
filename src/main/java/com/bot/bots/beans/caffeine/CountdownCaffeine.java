package com.bot.bots.beans.caffeine;

import com.bot.bots.beans.view.Scheduled;
import com.bot.bots.database.enums.TaskNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Scheduler;
import lombok.SneakyThrows;
import org.checkerframework.checker.index.qual.NonNegative;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
@Component
public class CountdownCaffeine extends Thread {

    private static Cache<String, Scheduled> DESTORY_CACHE;

    public CountdownCaffeine(ExpireListener listener) {
        DESTORY_CACHE = Caffeine.newBuilder()
                .initialCapacity(100)
                .expireAfter(new DynamicExpire())
                .removalListener(listener)
                .scheduler(Scheduler.systemScheduler())
                .maximumSize(10_000_000)
                .build();
        super.start();
    }

    public static void set (String key, Scheduled scheduled) {
        DESTORY_CACHE.put(key, scheduled);
    }

    @Override
    @SneakyThrows
    public void run() {
        while (!Thread.interrupted()) {
            TimeUnit.SECONDS.sleep(1);
            DESTORY_CACHE.cleanUp();
        }
    }

    private static class DynamicExpire implements Expiry<String, Scheduled> {

        @Override
        public long expireAfterCreate(String chatId, Scheduled expire, long currentTime) {
            int sleep = TaskNode.getSleep(expire.getNode());
            return TimeUnit.SECONDS.toNanos(sleep);
        }

        @Override
        public long expireAfterUpdate(String chatId, Scheduled expire, long currentTime, @NonNegative long currentDuration) {
            return currentDuration;
        }

        @Override
        public long expireAfterRead(String chatId, Scheduled expire, long currentTime, @NonNegative long currentDuration) {
            return currentDuration;
        }
    }
}
