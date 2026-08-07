package com.bot.bots.database.entity;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bot.bots.config.Constants;
import com.bot.bots.helper.DecimalHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    /** 后台管理登录密码（BCrypt，兼容旧 MD5），敏感字段不参与 JSON 序列化输出 **/
    @JsonIgnore
    private String password;

    /** 创建时间 **/
    private LocalDateTime createTime;

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
