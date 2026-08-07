package com.bot.bots.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Setter
@Getter
@Accessors(chain = true)
@TableName("t_broadcast_log")
public class BroadcastLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private String content;
    private Integer hasImage;
    private Long senderId;
    private Integer groupCount;
    private Integer successCount;
    private Integer failCount;
    private LocalDateTime createTime;
}
