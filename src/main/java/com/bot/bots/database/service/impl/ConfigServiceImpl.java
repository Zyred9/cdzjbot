package com.bot.bots.database.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bots.config.BotProperties;
import com.bot.bots.database.entity.Config;
import com.bot.bots.database.mapper.ConfigMapper;
import com.bot.bots.database.service.ConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {

    @Resource private BotProperties properties;

    @Override
    public Config queryConfig() {
        Long backgroundId = this.properties.getBackgroundId();
        Config config = this.baseMapper.selectById(backgroundId);

        if (Objects.isNull(config)) {
            config = new Config()
                    .setCustomText("")
                    .setCustomKeyboard("{}")
                    .setChatId(this.properties.getBackgroundId());

            this.baseMapper.insert(config);
        }
        return config;
    }
}
