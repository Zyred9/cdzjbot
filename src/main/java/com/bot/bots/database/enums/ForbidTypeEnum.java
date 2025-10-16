package com.bot.bots.database.enums;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 *
 *
 * @author zyred
 * @since 2025/10/10 22:30
 */
@Getter
@AllArgsConstructor
public enum ForbidTypeEnum {

    WOMAN(0, "privacy#forbid#0", "禁女性"),
    FISH(1, "privacy#forbid#1", "禁鱼送"),
    BRIDGE(2, "privacy#forbid#2", "禁桥送"),
    CAR(3, "privacy#forbid#3", "禁车头人头送"),
    Minors(4, "privacy#forbid#4", "禁未成年"),
    WRINKLY(5, "privacy#forbid#5", "禁45岁以上中年人"),
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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ForbidTypeEnum fromJson(String v) {
        if (v == null) return null;
        try { return ForbidTypeEnum.valueOf(v); } catch (Exception ignore) {}
        ForbidTypeEnum byDesc = Arrays.stream(ForbidTypeEnum.values())
                .filter(e -> StrUtil.equals(e.getDesc(), v))
                .findFirst().orElse(null);
        if (byDesc != null) return byDesc;
        return Arrays.stream(ForbidTypeEnum.values())
                .filter(e -> StrUtil.equals(String.valueOf(e.getCode()), v))
                .findFirst().orElse(null);
    }

    @JsonValue
    public String jsonValue() {
        return this.getDesc();
    }
}
