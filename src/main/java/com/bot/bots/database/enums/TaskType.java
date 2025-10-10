package com.bot.bots.database.enums;

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
public enum TaskType {

    RECHARGE(0, "充值");

    private final int code;
    private final String type;
}
