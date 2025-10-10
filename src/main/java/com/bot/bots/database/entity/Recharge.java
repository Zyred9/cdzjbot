package com.bot.bots.database.entity;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bot.bots.config.Constants;
import com.bot.bots.database.enums.RechargeStatus;
import com.bot.bots.helper.DecimalHelper;
import com.bot.bots.helper.TimeHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@TableName("t_recharge")
public class Recharge {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 交易编号 **/
    private String trade;
    /** 用户信息 **/
    private Long userId;
    private String username;
    private String nickname;
    /** 充值金额 **/
    private BigDecimal amount;
    /** 小数点 **/
    private BigDecimal pointer;
    /** 充值地址 **/
    private String address;
    /** 充值状态 **/
    private RechargeStatus rechargeStatus;
    /** 收款时间 **/
    private LocalDateTime receiveTime;
    /** 创建时间 **/
    private LocalDateTime createTime;
    /** 过期时间 **/
    private LocalDateTime expireTime;

    public static Recharge build(User user, BigDecimal amount, String address) {
        BigDecimal pointer = DecimalHelper.pointer();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusMinutes(30);
        return new Recharge()
                .setTrade(UUID.fastUUID().toString())
                .setUserId(user.getUserId())
                .setNickname(user.getNickname())
                .setUsername(user.getUsername())
                .setAmount(amount)
                .setPointer(pointer)
                .setRechargeStatus(RechargeStatus.UNRECEIVED)
                .setAddress(address)
                .setCreateTime(now)
                .setExpireTime(expireTime);
    }

    public String buildText(BigDecimal balance) {
        BigDecimal pay = this.amount.add(this.pointer);
        return  StrUtil.format(Constants.RECHARGE_BILL_TEXT,
                DecimalHelper.decimalParse(balance),
                DecimalHelper.decimalParse(Constants.COST),
                DecimalHelper.decimalParse(pay),
                this.address,
                TimeHelper.format(LocalDateTime.now()),
                TimeHelper.format(this.expireTime)
        );
    }
}
