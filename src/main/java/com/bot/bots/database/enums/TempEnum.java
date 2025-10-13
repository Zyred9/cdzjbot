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
public enum TempEnum {

    INPUT_PUBLISH_TEXT(0, "输入供需发布"),
    INPUT_PZ_EXPOSE_TEXT(1, "输入骗子曝光"),

    INPUT_INTERVAL(2, "输入接单额区间"),
    CAR_TEAM_INPUT(3, "输入车队报备信息"),

    EXCHANGE_INPUT_CUSTOM_SCOPE(4, "承兑所在地查询自定义输入范围"),
    PARTNER_SUPPLEMENT_INPUT(5, "卸货合作商，输入补充要求"),
    LOCATION_SUPPLEMENT_INPUT(6, "卸货所在地，输入补充要求")

    ;

    @EnumValue
    private final Integer code;
    private final String desc;
}
