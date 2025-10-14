package com.bot.bots.beans.view.ctx;


import com.bot.bots.database.enums.AddressParam;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 *
 * @author zyred
 * @since 2025/10/14 16:05
 */
@Setter
@Getter
@Accessors(chain = true)
public class QueryCtx {
    private AddressParam param;



}
