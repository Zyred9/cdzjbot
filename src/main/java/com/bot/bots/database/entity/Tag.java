package com.bot.bots.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 标签表
 *
 * @author zyred
 * @since 1.0
 */
@Setter
@Getter
@Accessors(chain = true)
@TableName(value = "t_tag")
public class Tag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String color;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
