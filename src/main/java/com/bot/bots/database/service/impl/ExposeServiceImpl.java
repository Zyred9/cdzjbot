package com.bot.bots.database.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bot.bots.database.entity.Expose;
import com.bot.bots.database.enums.ExposeStatus;
import com.bot.bots.database.mapper.ExposeMapper;
import com.bot.bots.database.service.ExposeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 骗子曝光领域服务实现
 *
 * @author zyred
 * @since 1.0
 */
@Service
public class ExposeServiceImpl implements ExposeService {

    @Resource
    private ExposeMapper exposeMapper;

    @Override
    public boolean save(Expose expose) {
        return this.exposeMapper.insert(expose) > 0;
    }

    @Override
    public Expose getById(Long id) {
        return this.exposeMapper.selectById(id);
    }

    @Override
    public boolean updateStatusAndAudit(Long id, ExposeStatus status, Long auditBy, String auditAt) {
        LambdaUpdateWrapper<Expose> uw = new LambdaUpdateWrapper<>();
        uw.eq(Expose::getId, id)
          .set(Expose::getStatus, status)
          .set(Expose::getAuditBy, auditBy)
          .set(Expose::getAuditAt, auditAt);
        return this.exposeMapper.update(null, uw) > 0;
    }

    @Override
    public boolean updateChannelMessageId(Long id, Integer messageId) {
        LambdaUpdateWrapper<Expose> uw = new LambdaUpdateWrapper<>();
        uw.eq(Expose::getId, id)
          .set(Expose::getChannelMessageId, messageId);
        return this.exposeMapper.update(null, uw) > 0;
    }
}