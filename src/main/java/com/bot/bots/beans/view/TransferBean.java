package com.bot.bots.beans.view;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

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
@Accessors(chain = true)
public class TransferBean {

    /** 时间戳 **/
    private Long blockTs;

    private String transactionId;

    private String toAddress;

    private String fromAddress;

    private BigDecimal quant;

    private Boolean confirmed;

    private String contractType;

    private TokenInfoBean tokenInfo;


    /**
     * 计算真实的价格
     *
     * @return  价格
     */
    public BigDecimal parseValue () {
        BigDecimal pow = BigDecimal.TEN.pow(Objects.isNull(this.tokenInfo.getTokenDecimal()) ? 6 : this.tokenInfo.getTokenDecimal());
        return this.quant.divide(pow, 12, RoundingMode.HALF_UP);
    }

    public boolean isTransferIn (String address) {
        return StrUtil.equals(address, this.toAddress);
    }

    public static TransferBean mock (String address) {
        int i = RandomUtil.randomInt(1000, 9999);
        TransferBean mock = new TransferBean();
        mock.setBlockTs(System.currentTimeMillis());
        mock.setTransactionId("5aec39dd1863e5dbdb0b5673651240c0e44ef9c429dc56c904e77ea6142f5" + i);
        if (i % 2 == 0) {
            mock.setToAddress(address);
            mock.setFromAddress("TRzbTDtxTQfBHptD99CZ8qdLyaVws21s3g");
        } else {
            mock.setToAddress("TRzbTDtxTQfBHptD99CZ8qdLyaVws21s3g");
            mock.setFromAddress(address);
        }
        mock.setQuant(BigDecimal.valueOf(i * 10000L));
        mock.setConfirmed(true);
        mock.setTokenInfo(new TokenInfoBean().setTokenDecimal(6).setTokenAbbr("USDT"));
        return mock;
    }
}
