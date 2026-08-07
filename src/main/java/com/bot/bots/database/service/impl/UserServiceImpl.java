package com.bot.bots.database.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bots.database.entity.User;
import com.bot.bots.database.mapper.UserMapper;
import com.bot.bots.database.service.UserService;
import com.bot.bots.helper.PasswordHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
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
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Override
    public User queryUser(org.telegram.telegrambots.meta.api.objects.User from) {
        User user = this.baseMapper.selectById(from.getId());
        if (Objects.nonNull(user)) {
            return user;
        }
        user = User.buildDefault(from.getId(), from.getUserName(), from.getFirstName())
                .setPassword(PasswordHelper.hash("123456"));
        try {
            this.baseMapper.insert(user);
        } catch (DuplicateKeyException e) {
            log.info("[queryUser] 用户并发注册主键冲突，重新查询，用户id：{}", from.getId());
            return this.baseMapper.selectById(from.getId());
        }
        return user;
    }


}
