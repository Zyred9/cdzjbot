package com.bot.bots.database.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bots.database.entity.BroadcastCategory;
import com.bot.bots.database.mapper.BroadcastCategoryMapper;
import com.bot.bots.database.service.BroadcastCategoryGroupService;
import com.bot.bots.database.service.BroadcastCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BroadcastCategoryServiceImpl extends ServiceImpl<BroadcastCategoryMapper, BroadcastCategory> implements BroadcastCategoryService {

    private final BroadcastCategoryGroupService categoryGroupService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BroadcastCategory create(String name, List<Long> chatIds) {
        BroadcastCategory category = new BroadcastCategory().setName(name);
        this.save(category);
        if (CollUtil.isNotEmpty(chatIds)) {
            this.categoryGroupService.saveGroups(category.getId(), chatIds);
        }
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BroadcastCategory update(Long id, String name, List<Long> chatIds) {
        BroadcastCategory category = new BroadcastCategory().setId(id).setName(name);
        this.updateById(category);
        if (chatIds != null) {
            this.categoryGroupService.saveGroups(id, chatIds);
        }
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWithGroups(Long id) {
        this.categoryGroupService.removeGroupsByCategoryId(id);
        this.removeById(id);
    }

    @Override
    public List<Long> getChatIdsByCategoryId(Long categoryId) {
        return this.categoryGroupService.listChatIdsByCategoryId(categoryId);
    }
}
