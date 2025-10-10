package com.bot.bots.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bot.bots.database.enums.ExposeStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 骗子曝光实体，冗余存储用户提交文本与解析字段，减少关联查询
 *
 * @author zyred
 * @since 1.0
 */
@Setter
@Getter
@Accessors(chain = true)
@TableName("t_expose")
public class Expose {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提交用户ID **/
    private Long userId;

    /** 原始文本（用户提交的完整模版内容） **/
    private String textRaw;

    /** 骗子ID（用户名） **/
    private String pzUserId;

    /** 骗子昵称（用户名） **/
    private String pzNickname;

    /** 被骗经过 **/
    private String story;

    /** 骗子U地址 **/
    private String pzAddress;

    /** 审核状态 **/
    private ExposeStatus status;

    /** 预留 JSON 存储字段，兼容 MyBatis-Plus JSON 能力（可选） **/
    private String jsonData;
}