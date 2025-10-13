package com.bot.bots.beans.initializer;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.bot.bots.beans.cache.CommonCache;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 *
 *
 * @author zyred
 * @since 2025/10/11 14:37
 */
@Component
public class ShutdownHandler implements ApplicationListener<ContextClosedEvent> {

    @Override
    public void onApplicationEvent(@NotNull ContextClosedEvent ignore) {
        String json = CommonCache.toJson();

        if (StrUtil.isBlank(json) || StrUtil.equals(JSONUtil.toJsonStr(Collections.emptyMap()), json)) {
            return;
        }

        File file = FileUtil.newFile("./cache.json");
        FileUtil.writeString(json, file, StandardCharsets.UTF_8);
    }
}
