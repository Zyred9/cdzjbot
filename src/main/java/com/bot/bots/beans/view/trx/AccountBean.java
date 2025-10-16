package com.bot.bots.beans.view.trx;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
public class AccountBean {

    private String address;

    /** 交易总笔数 **/
    private Integer transactions;

    /** 交易支出笔数 **/
    private Integer transactionsOut;

    /** 交易收入笔数 **/
    private Integer transactionsIn;

    /** 帐户余额 **/
    private List<WithPriceTokensBean> withPriceTokens;

}
