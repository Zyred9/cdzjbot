package com.bot.bots.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "bots.config")
public class BotProperties {

    private boolean logs;

    /** 代理 **/
    private boolean enableProxy;
    private String proxyType = "";
    private Integer proxyPort = 0;
    private String proxyHostName = "";
    /** 代理 **/
    private String botUsername;
    /** 机器人自身ID（启动时通过 GetMe 获取） **/
    private Long botId;
    private Map<String, String> tokens;


    /** 后台群ID **/
    private Long backgroundId;
    /** 后台命令管理员ID白名单（空则拒绝所有后台命令） **/
    private List<Long> managerIds = new ArrayList<>();
    /** 供需申请通过群ID **/
    private Long auditId;
    /** 供需发布群ID **/
    private Long publishId;
    /** 曝光群组ID **/
    private Long exposureId;

    /** 地址 **/
    private String address;
    private Long channel;
    private String apiKey;
    private String webUrl;


    public String getToken () {
        return this.tokens.get(Constants.TOKEN_KEY);
    }

    public boolean fromBackground (Long chatId) {
        return this.backgroundId.equals(chatId);
    }

    public boolean isManager (Long userId) {
        return CollUtil.isNotEmpty(this.managerIds) && CollUtil.contains(this.managerIds, userId);
    }

    public void setBotUsername(String botUsername) {
        if (StrUtil.contains(botUsername, "_")) {
            botUsername = StrUtil.replace(botUsername, "_", "\\_");
        }
        this.botUsername = botUsername;
    }
}
