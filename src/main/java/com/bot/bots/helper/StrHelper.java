package com.bot.bots.helper;

import cn.hutool.core.util.StrUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
public class StrHelper {

    public static String nickname (String first, String last) {
        if (StrUtil.isAllBlank(first, last)) {
            return "";
        }
        String name = first;
        if (StrUtil.isAllNotBlank(first, last)) {
            name = first + " " + last;
        }
        return specialChar(name);
    }


    public static String specialChar(String input) {
        String specialChar = "[_*~`>#+\\-=|{}.!]";
        return input.replaceAll(specialChar, "\\\\$0");
    }

    public static String specialResult(String input) {
        String specialChar = "[_~>#+\\-=|{}.!]";
        return input.replaceAll(specialChar, "\\\\$0");
    }

    public static String specialLong(Long value) {
        return specialChar(String.valueOf(value));
    }


    public static String getKey(Long userId) {
        return userId + "";
    }

    public static int extractPort(String jdbcUrl) {
        String regex = ":(\\d+)/";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(jdbcUrl);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }

    public static Map<String, String> parsePublishText(String text) {
        List<String> split = StrUtil.split(text, "\n");

        Map<String, String> map = new HashMap<>(split.size());
        for (String line : split) {
            String[] kv = line.split("[:：]");
            if (kv.length >= 2) {
                map.put(StrUtil.trim(kv[0]), StrUtil.trim(kv[1]));
            }
        }

        return map;
    }

    /**
     * 解析骗子曝光文本
     * 键包含：骗子ID、骗子昵称、被骗经过、骗子U地址
     */
    public static Map<String, String> parsePzExposeText(String text) {
        List<String> split = StrUtil.split(text, "
");
        Map<String, String> map = new HashMap<>(split.size());
        for (String line : split) {
            String[] kv = line.split("[:：]");
            if (kv.length >= 2) {
                map.put(StrUtil.trim(kv[0]), StrUtil.trim(kv[1]));
            }
        }
        return map;
    }
}
