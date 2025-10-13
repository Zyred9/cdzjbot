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
public enum ForbidTypeEnum {

    WOMAN(0, "privacy#forbid#0", "女性"),
    FISH(1, "privacy#forbid#1", "鱼送"),
    BRIDGE(2, "privacy#forbid#2", "桥送"),
    CAR(3, "privacy#forbid#3", "车头人头送"),
    Minors(4, "privacy#forbid#4", "未成年"),
    WRINKLY(5, "privacy#forbid#5", "45岁以上中年人"),
    ;

    @EnumValue
    private final int code;
    private final String callback;
    private final String desc;

    public static ForbidTypeEnum of(String code) {
        return Arrays.stream(ForbidTypeEnum.values())
                .filter(a -> StrUtil.equals(String.valueOf(a.getCode()), code))
                .findFirst().orElse(null);
    }
}
