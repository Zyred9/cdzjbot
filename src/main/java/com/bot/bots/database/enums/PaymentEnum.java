package com.bot.bots.database.enums;


/**
 *
 *
 * @author zyred
 * @since 2025/10/16 17:04
 */

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Locale;

@Getter
@AllArgsConstructor
public enum PaymentEnum {

    ALL("L", "全部", ""),
    ALL_L0("L0", "全部", ""),
    ALIPAY("Z", "支付宝", "&paymentMethod=alipay"),
    ALIPAY_Z0("Z0", "支付宝", "&paymentMethod=alipay"),
    BANK("B", "银行", "&paymentMethod=bank"),
    BANK_B0("B0", "银行", "&paymentMethod=bank"),
    ;

    private final String code;
    private final String desc;
    private final String params;


    public static PaymentEnum of(String code) {
        code = code.toUpperCase(Locale.ROOT);
        for (PaymentEnum value : PaymentEnum.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return ALL;
    }

}