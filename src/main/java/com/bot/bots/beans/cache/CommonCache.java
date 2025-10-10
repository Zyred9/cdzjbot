package com.bot.bots.beans.cache;

import com.bot.bots.database.enums.TempEnum;

import java.util.HashMap;
import java.util.Map;

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
}
