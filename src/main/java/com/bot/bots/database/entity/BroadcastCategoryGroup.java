package com.bot.bots.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@TableName("t_broadcast_category_group")
public class BroadcastCategoryGroup {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private Long chatId;
}
