package com.bot.bots.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
public class DecimalHelper {

    public static String decimalParse (BigDecimal bigDecimal) {
        if (Objects.isNull(bigDecimal)) {
            return "";
        }
        return bigDecimal.stripTrailingZeros().toPlainString();
    }

    /**
     *
     * balance 小于 cost 则返回 true
     *
     * @param balance       余额
     * @param cost          成本
     * @return              true or false
     */
    public static boolean lessThan(BigDecimal balance, BigDecimal cost) {
        if (Objects.equals(balance, BigDecimal.ZERO)) {
            return true;
        }
        return balance.compareTo(cost) < 0;
    }

    public static BigDecimal pointer() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int a = random.nextInt(10);
        int b = random.nextInt(10);

        int c = random.nextInt(1, 10);
        int fourDigitNumber = a * 1000 + b * 100 + c * 10;
        BigDecimal numerator = new BigDecimal(fourDigitNumber);
        BigDecimal denominator = new BigDecimal(10000);
        return numerator.divide(denominator, 3, RoundingMode.DOWN);
    }
}
