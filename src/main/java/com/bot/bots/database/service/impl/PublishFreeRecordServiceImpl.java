package com.bot.bots.database.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bots.database.entity.PublishFreeRecord;
import com.bot.bots.database.mapper.PublishFreeRecordMapper;
import com.bot.bots.database.service.PublishFreeRecordService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供需免费发布领取记录服务实现
 *
 * @author zyred
 * @since 1.0
 */
@Service
public class PublishFreeRecordServiceImpl extends ServiceImpl<PublishFreeRecordMapper, PublishFreeRecord>
        implements PublishFreeRecordService {

    @Override
    public boolean hasAvailable(Long userId) {
        return this.lambdaQuery()
                .eq(PublishFreeRecord::getUserId, userId)
                .eq(PublishFreeRecord::getClaimDate, LocalDate.now())
                .eq(PublishFreeRecord::getUsed, Boolean.FALSE)
                .exists();
    }

    @Override
    public boolean claim(Long userId) {
        try {
            return this.save(PublishFreeRecord.build(userId));
        } catch (DuplicateKeyException e) {
            // 当天已领取（唯一键 user_id + claim_date 冲突），并发下同样返回已领取
            return false;
        }
    }

    @Override
    public boolean consume(Long userId) {
        return this.lambdaUpdate()
                .set(PublishFreeRecord::getUsed, Boolean.TRUE)
                .set(PublishFreeRecord::getUseTime, LocalDateTime.now())
                .eq(PublishFreeRecord::getUserId, userId)
                .eq(PublishFreeRecord::getClaimDate, LocalDate.now())
                .eq(PublishFreeRecord::getUsed, Boolean.FALSE)
                .update();
    }
}
