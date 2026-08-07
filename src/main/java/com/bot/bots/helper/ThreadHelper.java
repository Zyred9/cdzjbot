package com.bot.bots.helper;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
@Slf4j
public class ThreadHelper {

    static final ThreadPoolExecutor EX = ExecutorBuilder.create()
            .setCorePoolSize(2)
            .setMaxPoolSize(100)
            .setWorkQueue(new LinkedBlockingQueue<>(2048))
            .setHandler(new ThreadPoolExecutor.CallerRunsPolicy())
            .setThreadFactory(ThreadFactoryBuilder.create()
                    .setNamePrefix("temp-thread-")
                    .setUncaughtExceptionHandler((t, e) ->
                            log.error("【线程池任务异常】线程：{}，异常信息：{}", t.getName(), e.getMessage(), e))
                    .build())
            .build();

    public static void execute(Runnable runnable) {
        EX.execute(runnable);
    }


    @SneakyThrows
    public static void sleep (int time) {
        TimeUnit.SECONDS.sleep(time);
    }

    @SneakyThrows
    public static void sleepMs(int time) {
        TimeUnit.MILLISECONDS.sleep(time);
    }
}
