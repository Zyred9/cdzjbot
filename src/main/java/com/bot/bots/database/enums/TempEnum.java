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
    ;

    @EnumValue
    private final Integer code;
    private final String desc;
}
