package com.bot.bots.database.entity;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bot.bots.config.Constants;
import com.bot.bots.helper.DecimalHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 *
 *
 * @author zyred
 * @since 2025/10/9 15:36
 */
@Setter
@Getter
@Accessors(chain = true)
@TableName("t_user")
public class User {

    @TableId(type = IdType.INPUT)
    private Long userId;
    private String username;
    private String nickname;

    /** usdt 余额 **/
    private BigDecimal balance;

    public static User buildDefault(Long userId, String userName, String firstName) {
        return new User()
                .setUserId(userId)
                .setUsername(userName)
                .setNickname(firstName)
                .setBalance(BigDecimal.ZERO);
    }

    public String buildSelfText() {
        return StrUtil.format(Constants.USER_SELF_TEXT,
                this.userId, "@" + this.username, this.nickname, DecimalHelper.decimalParse(this.balance)
        );
    }
}
