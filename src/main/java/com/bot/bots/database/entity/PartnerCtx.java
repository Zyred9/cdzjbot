package com.bot.bots.database.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 *
 * @author zyred
 * @since 2025/10/13 21:46
 */
@Setter
@Getter
@Accessors(chain = true)
@TableName(value = "t_partner_ctx", autoResultMap = true)
public class PartnerCtx {

    @TableId(type = IdType.AUTO)
    private Long id;

    // 用户id
    private Long userId;
    // 用户名
    private String username;
    // 昵称
    private String nickname;

    // 地址
    private String address;
    // 模式
    private String model;
    // 是否要求钱包在现场
    private Boolean packet;
    // 补充要求
    private String supplement;

}
