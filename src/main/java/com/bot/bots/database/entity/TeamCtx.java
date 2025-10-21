package com.bot.bots.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bot.bots.beans.view.ctx.AcceptanceContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Map;

/**
 * 承兑报备表
 *
 * @author zyred
 * @since 1.0
 */
@Setter
@Getter
@Accessors(chain = true)
@TableName(value = "t_team_ctx", autoResultMap = true)
public class TeamCtx {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String username;
    private String nickname;

    // 底料
    private String base;
    // 车队类型
    private String teamType;
    // 安全员
    private String hasSafe;
    // 具体金额
    private String amount;
    // 所在省市
    private String address;
    // 联系方式
    private String phone;
    // 是否跟车
    private String followCar;
    // 备注
    private String marks;
    // 经纬度
    private String location;

    public AcceptanceContext buildContext () {
        return AcceptanceContext.builder()
                .address(this.address)
                .location(this.location)
                .build();
    }


    public static TeamCtx build(Map<String, String> map, User from) {
        return new TeamCtx()
                .setUserId(from.getId())
                .setUsername(from.getUserName())
                .setNickname(from.getFirstName())
                .setBase(map.getOrDefault("底料", ""))
                .setTeamType(map.getOrDefault("车队类型", ""))
                .setHasSafe(map.getOrDefault("安全员", ""))
                .setAmount(map.getOrDefault("具体金额", ""))
                .setAddress(map.getOrDefault("所在省市", ""))
                .setPhone(map.getOrDefault("联系方式", ""))
                .setFollowCar(map.getOrDefault("是否需要跟车", ""))
                .setMarks(map.getOrDefault("备注", ""));
    }
}