package com.bot.bots.helper;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.bots.config.BotProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * 高德地图 Web 服务工具类
 * <p>
 * 功能概述：
 * - 地理编码（地址->经纬度）
 * - 路径规划（驾车）获取首条路径距离（米）
 *
 * @author zyred
 * @since 2025/10/14 14:30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MapUtil {

    private static final String GEO_CODE = "https://restapi.amap.com/v3/geocode/geo?key={}&address={}";
    private static final String DIRECTION_DRIVING = "https://restapi.amap.com/v5/direction/driving?origin={}&destination={}&key={}";

    private final BotProperties properties;
    private final OkHttpClient okHttpClient;


    /**
     * 路径规划（驾车）
     * <pre>
     * 调用示例：
     * String distanceMeters = mapUtil.driving("116.481028,39.989643", "116.434446,39.90816");
     * </pre>
     * @param origin       起点经纬度，"lon,lat"
     * @param destination  终点经纬度，"lon,lat"
     * @return             距离（单位：米）,失败返回 null
     */
    public Integer driving(String origin, String destination) {
        // 注意：高德要求经纬度为 "lon,lat"，中间逗号不能编码，否则会造成解析失败，这里不对逗号做编码处理。
        final String url = StrUtil.format(DIRECTION_DRIVING, origin, destination, this.properties.getApiKey());
        try {
            JSONObject root = this.doHttpQuery(url);
            if (Objects.isNull(root)) {
                return null;
            }
            String status = root.getStr("status", "0");
            if (!"1".equals(status)) {
                log.warn("[驾车路径规划] 调用失败，status={}, info={}", status, root.getStr("info"));
                return null;
            }
            JSONObject route = root.getJSONObject("route");
            if (route == null) {
                return null;
            }
            JSONArray paths = route.getJSONArray("paths");
            if (CollUtil.isEmpty(paths)) {
                return null;
            }
            JSONObject first = (JSONObject) paths.get(0);
            String distance = first.getStr("distance");
            log.info("[驾车路径规划] {} -> {}，距离：{} 米", origin, destination, distance);
            return Integer.parseInt(distance);
        } catch (Exception ex) {
            log.error("[驾车路径规划] 失败：{}", ex.getMessage());
            return null;
        }
    }


    /**
     * 地理编码：地址转经纬度
     * <pre>
     * 示例：String loc = mapUtil.location("北京市朝阳区阜通东大街6号");
     * 返回："116.481488,39.990464"
     * </pre>
     * @param address 地址
     * @return        "lon,lat"；失败返回 null
     */
    public String location (String address) {
        final String encAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        final String url = StrUtil.format(GEO_CODE, this.properties.getApiKey(), encAddress);
        try {
            JSONObject root = this.doHttpQuery(url);
            if (Objects.isNull(root)) {
                return null;
            }
            String status = root.getStr("status", "0");
            if (!"1".equals(status)) {
                log.warn("[查询经纬度] 调用失败，status={}, info={}", status, root.getStr("info"));
                return null;
            }

            JSONArray geocodes = root.getJSONArray("geocodes");
            if (CollUtil.isEmpty(geocodes)) {
                return null;
            }
            JSONObject first = (JSONObject)geocodes.get(0);
            String location = first.getStr("location");
            log.info("[查询经纬度] 地址：{}，经纬度：{}", address, location);
            return location;
        } catch (Exception ex) {
            log.error("[查询经纬度] 失败：{}", ex.getMessage());
            return null;
        }
    }


    @SuppressWarnings("all")
    private JSONObject doHttpQuery(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = this.okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || Objects.isNull(response.body())) {
                return null;
            }
            String bodyStr = response.body().string();
            return JSONUtil.parseObj(bodyStr);
        }
    }
}
