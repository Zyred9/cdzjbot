package com.bot.bots.helper;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析 resources/json/addr.json 并生成 INSERT SQL 脚本（单表 t_address）
 *
 * @author zyred
 * @since 1.0
 */
public class AddrJsonImporter {

    public static void main(String[] args) {
        // 生成 INSERT 脚本并追加到 resources/sql/t_address.sql
        writeInsertSqlToScript();
        System.out.println("Insert SQL appended to src/main/resources/sql/t_address.sql");
    }

    /**
     * <pre>
     * 约定 JSON 结构：
     * [
     *   {
     *     "provinceCode":"", "provinceName":"",
     *     "cities":[
     *       {"cityCode":"", "cityName":"", "counties":[{"countyCode":"", "countyName":""}]}
     *     ]
     *   }
     * ]
     * 实际字段名以 addr.json 为准；若不同，请在 buildRecords 里调整映射。
     * </pre>
     */
    public static String generateInsertSql() {
        String json = ResourceUtil.readStr("json/addr.json", StandardCharsets.UTF_8);
        JSONArray provinces = JSONUtil.parseArray(json);

        List<Record> records = new ArrayList<>(provinces.size() * 32);
        for (int i = 0; i < provinces.size(); i++) {
            var p = provinces.getJSONObject(i);
            String pCode = pickStr(p, "code", "provinceCode");
            String pName = pickStr(p, "name", "provinceName");

            var cities = pickArray(p, "children", "cities", "cityList");
            if (cities == null || cities.isEmpty()) {
                // 仅省级记录
                records.add(new Record()
                        .setProvinceCode(n(pCode)).setProvinceName(n(pName))
                        .setCityCode("").setCityName("")
                        .setCountyCode("").setCountyName("")
                        .setFullName(StrUtil.format("{} ", n(pName)))
                        .setPath(n(pCode)));
                continue;
            }

            for (int j = 0; j < cities.size(); j++) {
                var c = cities.getJSONObject(j);
                String cCode = pickStr(c, "code", "cityCode");
                String cName = pickStr(c, "name", "cityName");

                var counties = pickArray(c, "children", "counties", "areas", "areaList");
                if (counties == null || counties.isEmpty()) {
                    // 省-市记录
                    records.add(new Record()
                            .setProvinceCode(n(pCode)).setProvinceName(n(pName))
                            .setCityCode(n(cCode)).setCityName(n(cName))
                            .setCountyCode("").setCountyName("")
                            .setFullName(StrUtil.format("{} / {}", n(pName), n(cName)))
                            .setPath(StrUtil.format("{}/{}", n(pCode), n(cCode))));
                    continue;
                }

                for (int k = 0; k < counties.size(); k++) {
                    var a = counties.getJSONObject(k);
                    String aCode = pickStr(a, "code", "countyCode", "areaCode");
                    String aName = pickStr(a, "name", "countyName", "areaName");
                    records.add(new Record()
                            .setProvinceCode(n(pCode)).setProvinceName(n(pName))
                            .setCityCode(n(cCode)).setCityName(n(cName))
                            .setCountyCode(n(aCode)).setCountyName(n(aName))
                            .setFullName(StrUtil.format("{} / {} / {}", n(pName), n(cName), n(aName)))
                            .setPath(StrUtil.format("{}/{}/{}", n(pCode), n(cCode), n(aCode))));
                }
            }
        }

        StringBuilder sql = new StringBuilder(1024);
        sql.append("INSERT INTO t_address (province_code, province_name, city_code, city_name, county_code, county_name, full_name, path) VALUES\n");
        for (int i = 0; i < records.size(); i++) {
            Record r = records.get(i);
            sql.append(StrUtil.format("('{}','{}','{}','{}','{}','{}','{}','{}')",
                    esc(r.getProvinceCode()), esc(r.getProvinceName()),
                    esc(r.getCityCode()), esc(r.getCityName()),
                    esc(r.getCountyCode()), esc(r.getCountyName()),
                    esc(r.getFullName()), esc(r.getPath())
            ));
            if (i < records.size() - 1) {
                sql.append(",\n");
            } else {
                sql.append(";\n");
            }
        }
        return sql.toString();
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("'", "''");
    }

    private static String n(String s) {
        return StrUtil.emptyToDefault(s, "");
    }

    /**
     * 将生成的 INSERT 脚本追加写入到 src/main/resources/sql/t_address.sql
     */
    public static void writeInsertSqlToScript() {
        String insert = generateInsertSql();
        // 使用项目根路径，确保路径正确
        String projectDir = System.getProperty("user.dir");
        java.nio.file.Path target = java.nio.file.Paths.get(projectDir, "src", "main", "resources", "sql", "t_address.sql");
        // 确保父目录存在
        FileUtil.mkdir(target.getParent().toFile());
        // 追加写入（若文件不存在会创建）
        FileUtil.appendString("\n" + insert, target.toFile(), StandardCharsets.UTF_8);
    }

    private static String pickStr(JSONObject obj, String... keys) {
        for (String k : keys) {
            String v = obj.getStr(k);
            if (StrUtil.isNotBlank(v)) return v;
        }
        // 兼容 xxxCode/xxxName（如省/市/县层级）
        for (String k : keys) {
            if (k.endsWith("Code") || k.endsWith("Name")) continue;
            String v1 = obj.getStr(k + "Code");
            String v2 = obj.getStr(k + "Name");
            if (StrUtil.isNotBlank(v1)) return v1;
            if (StrUtil.isNotBlank(v2)) return v2;
        }
        return "";
    }

    private static JSONArray pickArray(JSONObject obj, String... keys) {
        for (String k : keys) {
            JSONArray arr = obj.getJSONArray(k);
            if (arr != null && !arr.isEmpty()) return arr;
        }
        return null;
    }

    /**
     * 用于内存暂存 JSON 展开后的记录
     */
    @Setter
    @Getter
    @Accessors(chain = true)
    public static class Record {
        private String provinceCode;
        private String provinceName;
        private String cityCode;
        private String cityName;
        private String countyCode;
        private String countyName;
        private String fullName;
        private String path;
    }
}