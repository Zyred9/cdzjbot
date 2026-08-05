package com.bot.bots.database.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bots.database.entity.BroadcastLog;
import com.bot.bots.database.mapper.BroadcastLogMapper;
import com.bot.bots.database.service.BroadcastLogService;
import org.springframework.stereotype.Service;

@Service
public class BroadcastLogServiceImpl extends ServiceImpl<BroadcastLogMapper, BroadcastLog> implements BroadcastLogService {
}
