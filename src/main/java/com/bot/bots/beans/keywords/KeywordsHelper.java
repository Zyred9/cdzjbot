package com.bot.bots.beans.keywords;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import toolgood.words.StringSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
public class KeywordsHelper {

    private static final Map<Long, Map<String, String>> MAP_SEARCH = new ConcurrentHashMap<>(128);
    private static final Set<String> ALL_KEYWORDS = ConcurrentHashMap.newKeySet();
    private static final StringSearch SEARCH = new StringSearch();


    public static synchronized void add (List<String> kws) {
        if (CollUtil.isEmpty(kws)) {
            return;
        }
        ALL_KEYWORDS.addAll(kws);
        SEARCH.SetKeywords(new ArrayList<>(ALL_KEYWORDS));
    }
    public static String illegal(String k) {
        if (StrUtil.isNotBlank(k)) {
            return SEARCH.FindFirst(k);
        }
        return null;
    }

    public static synchronized void addKeywords (Long chatId, String key, String content) {
        Map<String, String> keywords = MAP_SEARCH.computeIfAbsent(chatId, k -> new ConcurrentHashMap<>(128));
        keywords.put(key, content);
        KeywordsHelper.add(List.of(key));
    }

    public static String getContent (Long chatId, String key) {
        String illegal = KeywordsHelper.illegal(key);
        if (StrUtil.isNotBlank(illegal)) {
            Map<String, String> keywords = MAP_SEARCH.get(chatId);
            return Objects.isNull(keywords) ? null : keywords.get(key);
        }
        return null;
    }

}
