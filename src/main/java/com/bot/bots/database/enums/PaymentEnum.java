package com.bot.bots.database.enums;


/**
 *
 *
 * @author zyred
 * @since 2025/10/16 17:04
 */

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentEnum {

    ALL("L", "全部", ""),
    ALIPAY("Z0", "支付宝", "&paymentMethod=alipay"),
    BANK("B0", "银行", "&paymentMethod=bank"),
    ;

    private final String code;
    private final String desc;
    private final String params;


    public static PaymentEnum of(String code) {
        for (PaymentEnum value : PaymentEnum.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return ALL;
    }
}