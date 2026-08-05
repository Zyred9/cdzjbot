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
@TableName("t_broadcast_group")
public class BroadcastGroup {

    @TableId(type = IdType.INPUT)
    private Long chatId;
    private String groupName;
    private LocalDateTime createTime;
}
