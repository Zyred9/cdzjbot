package com.bot.bots.config;

import java.math.BigDecimal;

/**
 * <p>
 * 常量池
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
public interface Constants {

    BigDecimal COST = BigDecimal.valueOf(50);


    String TOKEN_KEY = "BOT_TOKEN";

    String SUCCESS = "✅操作成功";

    String FAILED = "❌操作失败";

    String START_GROUP_URL = "https://t.me/{}?startgroup=true";

    String START_SUBSCRIBE_TEXT = """
            请关注下面频道后，才能使用功能
            资源发布 @CDZJfabu
            骗子曝光 @CDZJpianzi
            供需频道 @CDZJgongxu
            联系客服 @CDZJkefu
            """;

    String PZ_EXPOSE_TEMPLATE = """
            `承兑之家骗子曝光
            ------------------------------------
            骗子ID :
            骗子昵称：
            -----------------------------
            被骗经过
            
            骗子U地址`
            """;

    String START_TEXT = """
            【承兑/车队】机器人 如不懂如何使用 可联系客服 @OFOuuu 系统会陆续更新功能
            如果您是：U商
            请选择“供方’
            如果您是：车队 
            请选择〝需方”
            承兑如更换位置，需及时更新自身所在地
            全网第一家 承兑业务一网打尽 免去每天到处找承兑烦恼
            """;

    String P_C_PUBLISH_TEXT = """
            供需方发布：
            点击下面文本（复制模版）
            
            ➖➖➖➖➖➖➖➖➖➖➖
            `供需方：必须填写（供）方或者（需）方
            项目名称：
            项目介绍：
            金额：
            时间：
            押金要求：
            是否支持专群：
            担保费谁承担：
            业务负责人：`
            ➖➖➖➖➖➖➖➖➖➖➖
            """;

    String USER_SELF_TEXT = """
            ID: `{}`
            用户名：{}
            昵称：{}
            余额：{}U
            """;

    String RECHARGE_BILL_TEXT = """
            您的余额：`{}`U
            本次发布需：*{}U*
            您实际支付金额：`{}`U（*请一定按照金额后面小数点转账*）
            
            收款地址：`{}`
            
            ❗️❗️❗请一定按照金额后面小数点转账，否则未到账概不负责❗️❗️❗️
            
            创建时间：`{}`
            结束时间：`{}`
            
            请在30分钟内支付完成，否则订单失效
            """;

    String TIMEOUT_TEXT = "❌订单超时，该笔订单已取消，如需充值请重新发起充值";
    String RECHARGE_SUCCESS_TEXT = """
            *充值已到账*
            充值订单：{}
            ➖➖➖➖➖➖➖➖
            🟢支付金额：`{}`U
            🟢到账时间：`{}`
            ➖➖➖➖➖➖➖➖
            """;
    String TRANSFER_QUERY = """
            https://apilist.tronscanapi.com/api/filter/trc20/transfers?sort=-timestamp&count=true&limit=20&start=0&filterTokenValue=0&relatedAddress={}&start_timestamp={}
            """;
}


