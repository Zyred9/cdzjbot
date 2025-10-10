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
public enum PublishStatus {

    AUDIT_WAIT(0, "等待审核"),
    AUDIT_PASS(1, "审核通过"),
    AUDIT_REJECT(2, "审核失败"),
    ;

    @EnumValue
    private final Integer code;
    private final String desc;

}
