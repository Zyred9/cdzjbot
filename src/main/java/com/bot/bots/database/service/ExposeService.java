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

    void updateStatusAndAudit(Long id, ExposeStatus status);

}