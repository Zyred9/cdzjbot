package com.bot.bots.database.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bots.database.entity.BroadcastGroup;

import java.util.List;

public interface BroadcastGroupService extends IService<BroadcastGroup> {

    BroadcastGroup addGroup(Long chatId, String groupName);

    List<BroadcastGroup> listAll();

    void removeByChatId(Long chatId);
}
