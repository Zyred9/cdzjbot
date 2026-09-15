package com.bot.bots.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供需免费发布领取记录（每用户每天一条，24 点清零）
 *
 * @author zyred
 * @since 1.0
 */
@Setter
@Getter
@Accessors(chain = true)
@TableName("t_publish_free_record")
public class PublishFreeRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    // 领取用户ID
    private Long userId;

    // 领取日期（当天有效）
    private LocalDate claimDate;

    // 是否已使用
    private Boolean used;

    // 使用时间（提交发布时消耗）
    private LocalDateTime useTime;

    // 领取时间
    private LocalDateTime createTime;

    public static PublishFreeRecord build(Long userId) {
        return new PublishFreeRecord()
                .setUserId(userId)
                .setClaimDate(LocalDate.now())
                .setUsed(Boolean.FALSE)
                .setCreateTime(LocalDateTime.now());
    }
}
