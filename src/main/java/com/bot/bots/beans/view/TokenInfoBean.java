package com.bot.bots.beans.view;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
public class TokenInfoBean {

    private Integer tokenDecimal;

    private String tokenAbbr;
}
