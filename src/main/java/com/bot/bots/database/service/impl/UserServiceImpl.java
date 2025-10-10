package com.bot.bots.database.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bots.database.entity.User;
import com.bot.bots.database.mapper.UserMapper;
import com.bot.bots.database.service.UserService;
import org.springframework.stereotype.Service;

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
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Override
    public User queryUser(org.telegram.telegrambots.meta.api.objects.User from) {
        User user = this.baseMapper.selectById(from.getId());
        if (Objects.nonNull(user)) {
            return user;
        }
        user = User.buildDefault(from.getId(), from.getUserName(), from.getFirstName());
        this.baseMapper.insert(user);
        return user;
    }


}
