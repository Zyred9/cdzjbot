package com.bot.bots.database.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 客户类型枚举
 *
 * @author zyred
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public enum CustomerTypeEnum {

    COOPERATION(1, "合作"),
    NON_COOPERATION(2, "未合作"),
    RESTING(3, "休息中"),
    ;

    @EnumValue
    private final int code;
    private final String desc;

    public static CustomerTypeEnum of(int code) {
        return Arrays.stream(CustomerTypeEnum.values())
                .filter(e -> e.getCode() == code)
                .findFirst().orElse(null);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CustomerTypeEnum fromJson(String v) {
        if (v == null) return null;
        try { return CustomerTypeEnum.valueOf(v); } catch (Exception ignore) {}
        return Arrays.stream(CustomerTypeEnum.values())
                .filter(e -> String.valueOf(e.getCode()).equals(v))
                .findFirst().orElse(null);
    }

    @JsonValue
    public String jsonValue() {
        return this.getDesc();
    }
}
