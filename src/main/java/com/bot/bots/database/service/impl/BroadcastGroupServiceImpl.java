package com.bot.bots.database.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bots.database.entity.BroadcastCategoryGroup;
import com.bot.bots.database.entity.BroadcastGroup;
import com.bot.bots.database.mapper.BroadcastGroupMapper;
import com.bot.bots.database.service.BroadcastCategoryGroupService;
import com.bot.bots.database.service.BroadcastGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BroadcastGroupServiceImpl extends ServiceImpl<BroadcastGroupMapper, BroadcastGroup> implements BroadcastGroupService {

    private final BroadcastCategoryGroupService broadcastCategoryGroupService;

    @Override
    public void createIfAbsent(Long chatId, String groupName) {
        long count = this.lambdaQuery().eq(BroadcastGroup::getChatId, chatId).count();
        if (count > 0) {
            return;
        }
        BroadcastGroup group = new BroadcastGroup()
                .setChatId(chatId)
                .setGroupName(groupName);
        this.save(group);
    }

    @Override
    public List<BroadcastGroup> listAll() {
        return this.lambdaQuery().orderByDesc(BroadcastGroup::getCreateTime).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByChatId(Long chatId) {
        this.broadcastCategoryGroupService.lambdaUpdate()
                .eq(BroadcastCategoryGroup::getChatId, chatId)
                .remove();
        this.lambdaUpdate().eq(BroadcastGroup::getChatId, chatId).remove();
    }
}
