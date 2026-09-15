package com.bot.bots.database.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bots.database.entity.PublishAuthGroup;

/**
 * 供需免费发布授权群服务
 *
 * @author zyred
 * @since 1.0
 */
public interface PublishAuthGroupService extends IService<PublishAuthGroup> {

    /**
     * 是否是已授权的免费发布群
     *
     * @param chatId 群ID
     * @return true 已授权
     */
    boolean isAuthorized(Long chatId);

    /**
     * 授权群（已授权返回 false）
     *
     * @param chatId 群ID
     * @return true 授权成功
     */
    boolean authorize(Long chatId);
}
