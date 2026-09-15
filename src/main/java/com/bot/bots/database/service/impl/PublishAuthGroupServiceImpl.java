package com.bot.bots.database.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bots.database.entity.PublishAuthGroup;
import com.bot.bots.database.mapper.PublishAuthGroupMapper;
import com.bot.bots.database.service.PublishAuthGroupService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 供需免费发布授权群服务实现
 *
 * @author zyred
 * @since 1.0
 */
@Service
public class PublishAuthGroupServiceImpl extends ServiceImpl<PublishAuthGroupMapper, PublishAuthGroup>
        implements PublishAuthGroupService {

    @Override
    public boolean isAuthorized(Long chatId) {
        return this.lambdaQuery()
                .eq(PublishAuthGroup::getChatId, chatId)
                .exists();
    }

    @Override
    public boolean authorize(Long chatId) {
        try {
            return this.save(new PublishAuthGroup()
                    .setChatId(chatId)
                    .setCreateTime(LocalDateTime.now()));
        } catch (DuplicateKeyException e) {
            // 并发/重复授权，主键冲突视为已授权
            return false;
        }
    }
}
