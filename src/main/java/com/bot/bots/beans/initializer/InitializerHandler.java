package com.bot.bots.beans.initializer;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.bot.bots.beans.cache.CommonCache;
import com.bot.bots.config.BotProperties;
import com.bot.bots.database.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;

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
public class InitializerHandler {

    @Resource private BotProperties properties;
    @Resource private ConfigService configService;

    public void init (User user) {
        this.configService.queryConfig();
        File file = FileUtil.newFile(BotProperties.filename);
        String json = FileUtil.readString(file, StandardCharsets.UTF_8);

        CommonCache.writeJson(json);
    }
}
