package com.bot.bots.database.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
@Getter
@AllArgsConstructor
public enum RechargeStatus {

    // 0:未到账 1:已到账 2:过期
    UNRECEIVED("0", "未到账"),
    RECEIVED("1", "已到账"),
    EXPIRED("2", "过期"),
    ;

    @EnumValue
    private final String code;
    private final String desc;

}
