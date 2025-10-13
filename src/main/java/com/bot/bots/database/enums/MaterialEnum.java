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
public enum MaterialEnum {

    COUNTER(1, "privacy#material#1", "柜台"),
    DI_DI(2, "privacy#material#2", "滴滴"),
    OLD_MAN(3, "privacy#material#3", "老人"),
    SAN_HEI(4, "privacy#material#4", "三黑"),
    ;

    @EnumValue
    private final int code;
    private final String callback;
    private final String desc;

    public static MaterialEnum ofName (String name) {
        return Arrays.stream(MaterialEnum.values())
                .filter(a -> StrUtil.equals(a.name(), name))
                .findFirst().orElse(null);
    }

    public static MaterialEnum ofCode(String code) {
        return Arrays.stream(MaterialEnum.values())
                .filter(a -> StrUtil.equals(String.valueOf(a.getCode()), code))
                .findFirst().orElse(null);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static MaterialEnum fromJson(String v) {
        if (v == null) return null;
        try { return MaterialEnum.valueOf(v); } catch (Exception ignore) {}
        MaterialEnum byDesc = Arrays.stream(MaterialEnum.values())
                .filter(e -> StrUtil.equals(e.getDesc(), v))
                .findFirst().orElse(null);
        if (byDesc != null) return byDesc;
        return Arrays.stream(MaterialEnum.values())
                .filter(e -> StrUtil.equals(String.valueOf(e.getCode()), v))
                .findFirst().orElse(null);
    }

    @JsonValue
    public String jsonValue() {
        return this.getDesc();
    }
}
