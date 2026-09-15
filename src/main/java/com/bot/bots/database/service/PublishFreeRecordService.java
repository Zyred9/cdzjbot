package com.bot.bots.database.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bots.database.entity.PublishFreeRecord;

/**
 * 供需免费发布领取记录服务
 *
 * @author zyred
 * @since 1.0
 */
public interface PublishFreeRecordService extends IService<PublishFreeRecord> {

    /**
     * 当天是否还有未使用的免费发布机会
     *
     * @param userId 用户ID
     * @return true 可用
     */
    boolean hasAvailable(Long userId);

    /**
     * 领取当天免费发布机会（当天已领取返回 false）
     *
     * @param userId 用户ID
     * @return true 领取成功
     */
    boolean claim(Long userId);

    /**
     * 消耗当天免费发布机会
     *
     * @param userId 用户ID
     * @return true 消耗成功（本次发布免费）
     */
    boolean consume(Long userId);
}
