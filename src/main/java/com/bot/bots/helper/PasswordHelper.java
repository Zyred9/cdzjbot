package com.bot.bots.helper;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.crypto.digest.DigestUtil;

/**
 * 后台管理登录密码工具类
 * <p>新密码统一使用 BCrypt 加盐哈希；旧版无盐 MD5 仅用于登录校验兼容，不在此处自动升级（升级由登录成功处处理）</p>
 *
 * @author zyred
 * @since 1.0
 */
public final class PasswordHelper {

    /** BCrypt 哈希前缀，用于区分新旧格式 */
    private static final String BCRYPT_PREFIX = "$2";

    private PasswordHelper() {
    }

    /**
     * 对明文密码进行 BCrypt 加盐哈希
     *
     * @param raw 明文密码
     * @return BCrypt 哈希值
     */
    public static String hash(String raw) {
        return BCrypt.hashpw(raw, BCrypt.gensalt());
    }

    /**
     * 校验明文密码是否与库中存储值匹配
     * <p>stored 以 "$2" 开头视为 BCrypt，走 checkpw；否则视为旧版无盐 MD5 兼容校验</p>
     *
     * @param raw    明文密码
     * @param stored 库中存储的密码哈希
     * @return 是否匹配
     */
    public static boolean matches(String raw, String stored) {
        if (StrUtil.isBlank(stored)) {
            return false;
        }
        if (stored.startsWith(BCRYPT_PREFIX)) {
            return BCrypt.checkpw(raw, stored);
        }
        return StrUtil.equals(DigestUtil.md5Hex(raw), stored);
    }
}
