package com.bot.bots.beans.initializer;

import com.bot.bots.database.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import javax.annotation.Resource;

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

    @Resource private ConfigService configService;


    public void init (User user) {
        this.configService.queryConfig();

    }
}
