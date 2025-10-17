package com.bot.bots.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.bot.bots.beans.view.ctx.AcceptanceContext;
import com.bot.bots.database.enums.CategoryEnum;
import com.bot.bots.database.enums.ForbidTypeEnum;
import com.bot.bots.database.enums.MaterialEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * 承兑报备表
 *
 * @author zyred
 * @since 1.0
 */
@Setter
@Getter
@Accessors(chain = true)
@TableName(value = "t_acceptance_ctx", autoResultMap = true)
public class AcceptanceCtx {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String username;
    private String nickname;

    // 地址
    private String address;
    // 承兑分类
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<CategoryEnum> categories;
    // 接单额度区间
    private String intervalInput;
    // 汇率上浮
    private BigDecimal rate;
    // 料性备注
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<MaterialEnum> materials;
    // 禁止类型
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ForbidTypeEnum> forbids;
    // 是否空降
    private Boolean airborne;
    // 是否驻点
    private Boolean station;
    // 是否移动
    private Boolean move;
    // 是否跟车
    private Boolean follow;
    // 经纬度
    private String location;

    @TableField(exist = false)
    private Integer distance;


    public AcceptanceContext buildContext () {
        return AcceptanceContext.builder()
                .address(this.address)
                .categories(this.categories)
                .intervalInput(this.intervalInput)
                .rate(this.rate)
                .materials(this.materials)
                .forbids(this.forbids)
                .airborne(this.airborne)
                .station(this.station)
                .move(this.move)
                .follow(this.follow)
                .location(this.location)
                .distance(this.distance)
                .build();
    }
}