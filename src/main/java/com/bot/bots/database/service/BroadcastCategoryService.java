package com.bot.bots.database.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bots.database.entity.BroadcastCategory;

import java.util.List;

public interface BroadcastCategoryService extends IService<BroadcastCategory> {

    BroadcastCategory create(String name, List<Long> chatIds);

    BroadcastCategory update(Long id, String name, List<Long> chatIds);

    void deleteWithGroups(Long id);

    List<Long> getChatIdsByCategoryId(Long categoryId);
}
