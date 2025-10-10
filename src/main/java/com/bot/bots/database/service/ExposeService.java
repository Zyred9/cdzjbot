package com.bot.bots.database.service;

import com.bot.bots.database.entity.Expose;
import com.bot.bots.database.enums.ExposeStatus;

/**
 * 骗子曝光领域服务
 *
 * @author zyred
 * @since 1.0
 */
public interface ExposeService {

    /**
     * 保存曝光记录
     */
    boolean save(Expose expose);

    /**
     * 按ID获取
     */
    Expose getById(Long id);

    /**
     * 更新状态与审计信息
     * <pre>
     * Expose expose = new Expose().setId(id);
     * service.updateStatusAndAudit(id, ExposeStatus.APPROVED, operatorId, "2025-10-10 12:00:00");
     * </pre>
     */
    boolean updateStatusAndAudit(Long id, ExposeStatus status, Long auditBy, String auditAt);

    /**
     * 更新频道消息ID
     */
    boolean updateChannelMessageId(Long id, Integer messageId);
}