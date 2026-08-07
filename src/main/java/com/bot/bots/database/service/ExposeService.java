package com.bot.bots.database.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bots.database.entity.Expose;
import com.bot.bots.database.enums.ExposeStatus;

/**
 * 骗子曝光领域服务
 *
 * @author zyred
 * @since 1.0
 */
public interface ExposeService extends IService<Expose> {

    /**
     * 待审核状态下原子更新审核状态，返回是否更新成功（已审核过则返回 false）
     */
    boolean updateStatusAndAudit(Long id, ExposeStatus status);

}