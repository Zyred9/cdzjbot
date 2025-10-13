package com.bot.bots.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
@Setter
@Getter
@Accessors(chain = true)
@TableName("t_config")
public class Config {

    @TableId(type = IdType.INPUT)
    private Long chatId;

    /** 客服自定义 **/
    private String customText;
    private String customKeyboard;

    /** 盘总自定义 **/
    private String selfText;
    private String selfKeyboard;

    /** 页面可编辑&删除的人 **/
    private List<Long> editable;
}
