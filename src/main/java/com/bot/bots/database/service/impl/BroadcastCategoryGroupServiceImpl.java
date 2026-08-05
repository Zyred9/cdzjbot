package com.bot.bots.database.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bots.database.entity.BroadcastCategoryGroup;
import com.bot.bots.database.mapper.BroadcastCategoryGroupMapper;
import com.bot.bots.database.service.BroadcastCategoryGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BroadcastCategoryGroupServiceImpl extends ServiceImpl<BroadcastCategoryGroupMapper, BroadcastCategoryGroup> implements BroadcastCategoryGroupService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGroups(Long categoryId, List<Long> chatIds) {
        this.removeGroupsByCategoryId(categoryId);
        if (CollUtil.isNotEmpty(chatIds)) {
            List<BroadcastCategoryGroup> groups = chatIds.stream()
                    .map(chatId -> new BroadcastCategoryGroup()
                            .setCategoryId(categoryId)
                            .setChatId(chatId))
                    .collect(Collectors.toList());
            this.saveBatch(groups);
        }
    }

    @Override
    public void removeGroupsByCategoryId(Long categoryId) {
        this.lambdaUpdate()
                .eq(BroadcastCategoryGroup::getCategoryId, categoryId)
                .remove();
    }

    @Override
    public List<Long> listChatIdsByCategoryId(Long categoryId) {
        List<BroadcastCategoryGroup> list = this.lambdaQuery()
                .eq(BroadcastCategoryGroup::getCategoryId, categoryId)
                .list();
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(BroadcastCategoryGroup::getChatId)
                .collect(Collectors.toList());
    }
}
