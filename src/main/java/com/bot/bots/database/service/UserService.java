package com.bot.bots.database.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bots.database.entity.User;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
public interface UserService extends IService<User> {

    User queryUser(org.telegram.telegrambots.meta.api.objects.User from);

}
