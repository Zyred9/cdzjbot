package com.bot.bots.database.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bots.database.entity.Expose;
import com.bot.bots.database.entity.Recharge;
import com.bot.bots.database.enums.ExposeStatus;
import com.bot.bots.database.mapper.ExposeMapper;
import com.bot.bots.database.mapper.RechargeMapper;
import com.bot.bots.database.service.ExposeService;
import com.bot.bots.database.service.RechargeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 骗子曝光领域服务实现
 *
 * @author zyred
 * @since 1.0
 */
@Service
public class ExposeServiceImpl extends ServiceImpl<ExposeMapper, Expose> implements ExposeService  {

    @Override
    public void updateStatusAndAudit(Long id, ExposeStatus status) {
        this.baseMapper.update(
                Wrappers.<Expose>lambdaUpdate()
                        .eq(Expose::getId, id)
                        .set(Expose::getStatus, status)
        );
    }
}