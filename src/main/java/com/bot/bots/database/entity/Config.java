package com.bot.bots.database.entity;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collection;
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
@TableName(value = "t_config", autoResultMap = true)
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
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> editable;


    public boolean hasEdit (Long userId) {
        return CollUtil.contains(this.editable, userId);
    }
}
