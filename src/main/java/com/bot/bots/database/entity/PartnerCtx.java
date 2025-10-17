package com.bot.bots.database.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.bot.bots.beans.view.ctx.AcceptanceContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

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
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> models;
    // 是否要求钱包在现场
    private Boolean packet;
    // 补充要求
    private String supplement;
    // 经纬度
    private String location;

    public AcceptanceContext buildContext () {
        return AcceptanceContext.builder()
                .address(this.address)
                .models(this.models)
                .packet(this.packet)
                .supplement(this.supplement)
                .location(this.location)
                .build();
    }


    public static PartnerCtx build (AcceptanceContext ctx, User user) {
        return new PartnerCtx()
                .setUserId(user.getId())
                .setUsername(user.getUserName())
                .setNickname(user.getFirstName())
                .setAddress(ctx.getAllAddress())
                .setModels(ctx.getModels())
                .setPacket(ctx.getPacket())
                .setSupplement(ctx.getSupplement());
    }
}
