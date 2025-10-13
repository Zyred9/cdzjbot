package com.bot.bots.database.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bots.database.entity.TeamCtx;
import com.bot.bots.database.mapper.TeamCtxMapper;
import com.bot.bots.database.service.TeamCtxService;
import org.springframework.stereotype.Service;

/**
 * TeamCtxServiceImpl
 *
 * TeamCtx 的默认服务实现，基于 MyBatis-Plus ServiceImpl。
 *
 * @author zyred
 * @since 1.0
 */
@Service
public class TeamCtxServiceImpl extends ServiceImpl<TeamCtxMapper, TeamCtx> implements TeamCtxService {
}