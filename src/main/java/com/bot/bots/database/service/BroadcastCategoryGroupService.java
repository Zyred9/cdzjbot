package com.bot.bots.database.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bots.database.entity.BroadcastCategoryGroup;

import java.util.List;

public interface BroadcastCategoryGroupService extends IService<BroadcastCategoryGroup> {

    void saveGroups(Long categoryId, List<Long> chatIds);

    void removeGroupsByCategoryId(Long categoryId);

    List<Long> listChatIdsByCategoryId(Long categoryId);
}
