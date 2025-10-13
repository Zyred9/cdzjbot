package com.bot.bots.beans.cache;

import cn.hutool.core.util.StrUtil;

import cn.hutool.json.JSONUtil;
import com.bot.bots.beans.view.ctx.AcceptanceContext;
import com.bot.bots.database.enums.TempEnum;
import lombok.NonNull;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
public class CommonCache {

    private static final Map<Long, TempEnum> CACHE = new HashMap<>(128);
    public static void put(Long id, TempEnum tempEnum) {
        CACHE.put(id, tempEnum);
    }
    public static boolean containsKey(Long id) {
        return CACHE.containsKey(id);
    }
    public static TempEnum getIfRemove(Long id) {
        return CACHE.remove(id);
    }
    public static TempEnum get(Long id) {
        return CACHE.get(id);
    }


    private static final Map<Long, AcceptanceContext> ACCEPTANCE_CONTEXT = new ConcurrentHashMap<>(128);
    public static void putAccCtx(Long userId, AcceptanceContext ctx) {
        ACCEPTANCE_CONTEXT.put(userId, ctx);
    }
    public static AcceptanceContext getRemoveAccCtx(Long userId) {
        return ACCEPTANCE_CONTEXT.remove(userId);
    }
    public static AcceptanceContext getAccCtx(Long userId) {
        return ACCEPTANCE_CONTEXT.get(userId);
    }
    public static String accCtxText (Long userId) {
        return AcceptanceContext.buildCtxText(CommonCache.getAccCtx(userId));
    }


    /**
     * 清空全部缓存
     * @param userId    用户id
     */
    public static void cleanCache(@NonNull Long userId) {
        CACHE.remove(userId);
        ACCEPTANCE_CONTEXT.remove(userId);
    }


    public static String toJson () {
        return JSONUtil.toJsonStr(ACCEPTANCE_CONTEXT);
    }
    @SuppressWarnings("all")
    public static void writeJson(String json) {
        if (StrUtil.isBlank(json)) {
            return;
        }
        if (!JSONUtil.isTypeJSONObject(json)) {
            return;
        }
        JSONUtil.parseObj(json).forEach((k, v) -> {
            try {
                final Long userId = Long.parseLong(k);
                final var obj = JSONUtil.parseObj(v);
                // 宽松模式反序列化，避免枚举在 Map->Bean 链路报错
                final AcceptanceContext ctx = JSONUtil.toBean(obj, AcceptanceContext.class, true);
                // 手动兜底解析 material，兼容 name 与 code
                if (obj.containsKey("material")) {
                    final Object materialVal = obj.get("material");
                    if (materialVal != null) {
                        com.bot.bots.database.enums.MaterialEnum parsed = null;
                        final String mv = String.valueOf(materialVal);
                        parsed = com.bot.bots.database.enums.MaterialEnum.ofName(mv);
                        if (parsed == null) {
                            parsed = com.bot.bots.database.enums.MaterialEnum.ofCode(mv);
                        }
                        if (parsed != null) {
                            ctx.setMaterial(parsed);
                        }
                    }
                }
                ACCEPTANCE_CONTEXT.put(userId, ctx);
            } catch (NumberFormatException ignore) {
                // 忽略无法转换为 Long 的 key
            }
        });
    }
}
