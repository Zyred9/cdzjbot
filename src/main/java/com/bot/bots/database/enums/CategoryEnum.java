package com.bot.bots.database.enums;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 *
 * @author zyred
 * @since 2025/10/10 22:30
 */
@Getter
@AllArgsConstructor
public enum CategoryEnum {

    CASH(0, "privacy#category_change#0#", "现金"),
    GOLD(1, "privacy#category_change#1#", "黄金"),
    PHONE(2, "privacy#category_change#2#", "手机"),
    LIQUOR(3, "privacy#category_change#3#", "酒水"),
    OTHER(4, "privacy#category_change#4#", "其他物品"),
    ;

    @EnumValue
    private final int code;
    private final String callback;
    private final String desc;

    public static CategoryEnum of(String categoryCode) {
        return Arrays.stream(CategoryEnum.values())
                .filter(a -> StrUtil.equals(String.valueOf(a.getCode()), categoryCode))
                .findFirst().orElse(null);
    }
}
