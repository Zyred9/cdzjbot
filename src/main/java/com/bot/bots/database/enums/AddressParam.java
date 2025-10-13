package com.bot.bots.database.enums;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 *
 *
 * @author zyred
 * @since 2025/10/10 22:30
 */
@Getter
@AllArgsConstructor
public enum AddressParam {

    EXCHANGE(0, "承兑报备"),
    QUERY_EXCHANGE(1, "承兑所在地查询"),
    QUERY_TEAM(2, "车队所在地查询"),
    UNLOADING_PARTNER(3, "卸货合作商"),
    UNLOADING_LOCATION(4, "卸货所在地"),
    ;

    @EnumValue
    private final int code;
    private final String desc;

    public static AddressParam of(String categoryCode) {
        return Arrays.stream(AddressParam.values())
                .filter(a -> StrUtil.equals(String.valueOf(a.getCode()), categoryCode))
                .findFirst().orElse(null);
    }
}
