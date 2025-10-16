package com.bot.bots.beans.view.trx;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
@Setter
@Getter
public class WithPriceTokensBean {

    /**
     * 链名称
     **/
    private String tokenName;
    /**
     * 余额
     **/
    private Long balance;
    /**
     * 余额计算精度
     **/
    private Integer tokenDecimal;
    /**
     * 金额
     **/
    private BigDecimal amount;
    /**
     * 币种缩写
     **/
    private String tokenAbbr;


    public String calcBalance() {
        if (balance == 0) {
            return "0";
        }
        return BigDecimal.valueOf(balance).divide(
                BigDecimal.TEN.pow(tokenDecimal), 12, RoundingMode.HALF_UP
        ).stripTrailingZeros().toPlainString();
    }
}
