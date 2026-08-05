package com.bot.bots.database.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bots.database.entity.Tag;
import com.bot.bots.database.mapper.TagMapper;
import com.bot.bots.database.service.TagService;
import org.springframework.stereotype.Service;

/**
 * 标签服务实现
 *
 * @author zyred
 * @since 1.0
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
}
