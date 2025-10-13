package com.bot.bots.database.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bots.database.entity.TeamCtx;
import com.bot.bots.database.mapper.TeamCtxMapper;
import com.bot.bots.database.service.TeamCtxService;
import org.springframework.stereotype.Service;

/**
 * 地址库服务实现（冗余字段模糊查询 + 分页）
 *
 * @author zyred
 * @since 1.0
 */
@Service
public class TeamCtxServiceImpl extends ServiceImpl<TeamCtxMapper, TeamCtx> implements TeamCtxService {

}