package com.bot.bots.database.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bots.database.entity.Config;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
public interface ConfigService extends IService<Config> {

    Config queryConfig();

}
