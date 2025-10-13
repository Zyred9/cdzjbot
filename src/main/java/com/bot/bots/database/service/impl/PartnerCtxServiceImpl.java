package com.bot.bots.database.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bots.database.entity.PartnerCtx;
import com.bot.bots.database.mapper.PartnerCtxMapper;
import com.bot.bots.database.service.PartnerCtxService;
import org.springframework.stereotype.Service;

/**
 * PartnerCtxServiceImpl
 *
 * PartnerCtx 的默认服务实现，基于 MyBatis-Plus ServiceImpl。
 *
 * @author zyred
 * @since 1.0
 */
@Service
public class PartnerCtxServiceImpl extends ServiceImpl<PartnerCtxMapper, PartnerCtx> implements PartnerCtxService {
}
