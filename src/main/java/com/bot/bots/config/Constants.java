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
    String TRANSFER_QUERY = """
            https://apilist.tronscanapi.com/api/filter/trc20/transfers?sort=-timestamp&count=true&limit=20&start=0&filterTokenValue=0&relatedAddress={}&start_timestamp={}
            """;
    String OKX_BOOTS = """
            https://www.okx.com/v3/c2c/tradingOrders/books?quoteCurrency=cny&baseCurrency=usdt&side={}&limit=15&sort=price&order=desc
            """;

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
            骗子ID：
            骗子昵称：
            -----------------------------
            被骗经过：
            
            骗子U地址：`
            """;

    String START_TEXT = """
            欢迎使用“承兑之家”机器人,本机器人致力于各大【承兑/车队】能快速匹配到彼此，节约双方时间
            
            【承兑/车队报备】 功能：U商/核销 提交按钮，按照自身所需真实填写
            【承兑/车队所在地】功能：车队/承兑可通过地区查询周边有无所需商家，然后进行预约
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

    String CAT_TEAM_TEXT = """
            本功能车队可提交作业信息， 方便承兑主动联系或查看所在位置，进行范围订单匹配！
            ———————————
            请选择你所在的作业省份，详情到区县
            
            车队报备  请将下列内容编辑后提交即可！
            
            `底料：       （自己填）
            车队类型：    （柜台/滴滴/上门/U商/证券/老人）
            安全员：      （有/无）
            保量金额：    （一般保量为10万起）
            所在省市：    （填写到区县）
            联系方式：    （自己飞机ID）
            是否需要跟车：（是或否）
            备注：`
            """;
    String ACCEPTANCE_FILING_TEXT = """
            本功能为  【现金/实物核销】报备 ：
            现金承兑为传统承兑模式
            实物核销承兑分为  （黄金/手机/笔记本/烟酒/其他）  等大宗商品核销
            ———————————
            请选择您所在的省份，详情到区县，以方便匹配订单
            """;

    String REPORT_COMMITED_TEXT = """
            您的报备已经提交，感谢您成为承兑之家合作伙伴，请向客服 @CDZJkefu 发送任意信息，届时周边有订单客服小二会第一时间通知您！
            """;

    String REPORT_COMMITED_BACK_TEXT = """
            承兑有人提交了报备，第{}条
            """;

    String PROVINCE_LOCATION_TEXT = """
           本功能可查询哪些地区有报备过的承兑或者核销承兑，并可按范围搜索承兑或者核销承兑，范围可以自定，按照提示查询即可，输入自己所在地区查询即可！
           ———————————
           请输入您的所在地 查看周边是否有承兑，请详情到区县
           """;

    String CAR_TEAM_REPORT_SUCCESS_TEXT = """
            您的报备已经提交，感谢您成为承兑之家合作伙伴！请向客服 @CDZJkefu 发送需求，当有业务匹配时，客服会主动联系您！
            """;

    String CAT_TEAM_ADDRESS_TEXT = """
            本功能为承兑或是核销承兑主动查找车队位置，可主动联系车队！
            ———————————
            请输入您的所在地 查看周边是否有作业车队，请详情到区县
            """;

    String UNLOADING_PARTNER_TEXT = """
            本功能是为各大承兑提供卸货渠道，需是承兑老板自己带现金换U  ，不直接对接车队，如是车队买U  请查询承兑业务！！！
            ————————————————-
            请选择你所在的作业省份，详情到区县
            """;

    String UNLOADING_PARTNER_SUCCESS_TEXT = """
            您的报备已经提交，感谢您成为承兑之家合作伙伴！请向客服 @CDZJkefu 发送需求，当有业务匹配时，客服会主动联系您！
            """;

    String UNLOADING_LOCATION_TEXT = """
            本版块为方便承兑卸货所建设，承兑可在这里查询卸货商家位置，然后到就近的位置进行现金买U业务
            ————————————————-
            请选择您所在的地区，机器人为自动为您匹配最近的卸货商家！
            """;
    String CHECK_EXCHANGE_RATE_TEXT = """
            输入 `L`  查询 欧意商家所有实时汇率
            输入 `Z0` 查询 欧意商家支付宝汇率
            """;
}


