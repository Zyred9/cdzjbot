package com.bot.bots.database.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 骗子曝光审核状态枚举
 *
 * @author zyred
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public enum ExposeStatus {

    WAIT(0, "待审核"),
    APPROVED(1, "已发布"),
    REJECTED(2, "已拒绝");

    @EnumValue
    private final Integer code;
    private final String desc;
}