package com.bot.bots.database.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bots.database.entity.BroadcastGroup;

import java.util.List;

public interface BroadcastGroupService extends IService<BroadcastGroup> {

    /**
     * 群组不存在时登记
     *
     * @param chatId    Telegram群ID
     * @param groupName 群名称
     */
    void createIfAbsent(Long chatId, String groupName);

    List<BroadcastGroup> listAll();

    void removeByChatId(Long chatId);
}
