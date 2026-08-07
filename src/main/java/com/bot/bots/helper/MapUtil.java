package com.bot.bots.helper;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.bots.beans.view.ctx.AcceptanceContext;
import com.bot.bots.config.BotProperties;
import com.bot.bots.database.entity.AcceptanceCtx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

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

    // 全局限流：统一调度，1秒最多3个请求
    private final BlockingQueue<Runnable> drivingJobs = new LinkedBlockingQueue<>(MAX_QUEUE);
    private ScheduledExecutorService drivingScheduler;
    private static final int MAX_QPS = 3;
    private static final int MAX_QUEUE = 2000;
    /**
     * 驾车路径规划最多尝试次数（含首次调用），网络瞬时超时自动重试
     */
    private static final int DRIVING_MAX_ATTEMPTS = 2;
    // 地理编码独立限流（与驾车共用 1 秒 3 个的 QPS 上限），避免高德 429
    private final Semaphore locationLimiter = new Semaphore(MAX_QPS);


    @PostConstruct
    private void initDrivingScheduler() {
        // 固定频率调度，按 1000ms / MAX_QPS 的步长出队执行
        long intervalMs = Math.max(1, 1000L / MAX_QPS);
        this.drivingScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "map-driving-qps");
            t.setDaemon(true);
            return t;
        });


        this.drivingScheduler.scheduleAtFixedRate(() -> {
            try {
                Runnable job = drivingJobs.poll();
                if (Objects.nonNull(job)) {
                    job.run();
                };
            } catch (Exception ex) {
                log.warn("[MapUtil] 驾车调度异常", ex);
            }
        }, intervalMs, intervalMs, TimeUnit.MILLISECONDS);

        if (this.properties.isLogs()) {
            log.info("[MapUtil] 驾车QPS限流调度启动，间隔 {} ms (≈{} QPS)", intervalMs, MAX_QPS);
        }
    }

    @PreDestroy
    private void shutdownDrivingScheduler() {
        if (this.drivingScheduler != null) {
            this.drivingScheduler.shutdownNow();
        }
    }

    /**
     * 查询从 originLocation 到 ctxList 每个元素中 location 之间的距离；全局限流 1 秒最多 3 个请求；
     * 全部完成后回调 consumer.accept(resultList)。
     *
     * @param originLocation 起点，经纬度 "lon,lat"
     * @param scope          范围
     * @param ctxList        终点列表，读取 AcceptanceCtx.getAddress()/getUserId 等自行扩展
     * @param consumer       完成后的回调，参数为与 ctxList 顺序一致的距离（米，可能为 null）列表
     */
    public void multiDriving(int scope, String originLocation, List<AcceptanceContext> ctxList, Consumer<List<AcceptanceContext>> consumer) {
        if (CollUtil.isEmpty(ctxList)) {
            consumer.accept(Collections.emptyList());
            return;
        }
        // 防御式拷贝，避免调用方并发修改列表导致统计偏差；使用固定总数进行完成判断
        // 为本次调用创建独立的批次上下文，确保并发多次调用时互不干扰
        final List<AcceptanceContext> batchAccepted = Collections.synchronizedList(new ArrayList<>());
        final AtomicInteger batchDone = new AtomicInteger(0);

        for (AcceptanceContext item : ctxList) {
            Runnable job = () -> {
                try {
                    if (Objects.nonNull(item) && StrUtil.isNotBlank(item.getLocation())) {
                        Integer dist = this.driving(originLocation, item.getLocation());
                        if (Objects.nonNull(dist) && dist <= (scope * 1000)) {
                            item.setDistance(dist);
                            batchAccepted.add(item);
                        }
                    }
                } catch (Exception ex) {
                    log.warn("[MapUtil] 单次驾车规划失败", ex);
                } finally {
                    this.countDone(batchDone, ctxList.size(), batchAccepted, consumer);
                }
            };

            try {
                if (!this.drivingJobs.offer(job)) {
                    // 队列已满/入队失败：该项直接按失败计入完成计数，保证最终计数达成、回调必然触发
                    log.warn("[MapUtil] 任务入队失败（队列已满，容量 {}），跳过该地址：{}",
                            MAX_QUEUE, Objects.isNull(item) ? null : item.getAddress());
                    this.countDone(batchDone, ctxList.size(), batchAccepted, consumer);
                }
            } catch (Exception e) {
                // 极端异常同样计入完成计数，保证回调必然触发
                log.warn("[MapUtil] 任务入队异常：{}", e.getMessage());
                this.countDone(batchDone, ctxList.size(), batchAccepted, consumer);
            }
        }
    }


    /**
     * 完成计数：批次内全部项（含入队失败项）处理完后，统一回调一次
     */
    private void countDone(AtomicInteger batchDone, int total, List<AcceptanceContext> batchAccepted, Consumer<List<AcceptanceContext>> consumer) {
        if (batchDone.incrementAndGet() == total) {
            try {
                consumer.accept(batchAccepted);
            } catch (Exception cbEx) {
                log.warn("[MapUtil] 回调异常", cbEx);
            }
        }
    }


    private Integer driving(String origin, String destination) {
        // 注意：高德要求经纬度为 "lon,lat"，中间逗号不能编码，否则会造成解析失败，这里不对逗号做编码处理。
        final String url = StrUtil.format(DIRECTION_DRIVING, origin, destination, this.properties.getApiKey());
        JSONObject root = this.queryWithRetry(url);
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
        try {
            return Integer.parseInt(distance);
        } catch (NumberFormatException ex) {
            log.error("[驾车路径规划] 距离解析失败，distance={}", distance, ex);
            return null;
        }
    }

    /**
     * 高德查询带重试：网络异常或返回空响应时最多尝试 {@link #DRIVING_MAX_ATTEMPTS} 次，避免瞬时超时导致路径规划失败
     */
    private JSONObject queryWithRetry(String url) {
        for (int attempt = 1; attempt <= DRIVING_MAX_ATTEMPTS; attempt++) {
            try {
                JSONObject root = this.doHttpQuery(url);
                if (Objects.nonNull(root)) {
                    return root;
                }
                log.warn("[驾车路径规划] 第 {} 次调用返回空响应", attempt);
            } catch (Exception ex) {
                if (attempt == DRIVING_MAX_ATTEMPTS) {
                    log.error("[驾车路径规划] 调用失败，已重试 {} 次", DRIVING_MAX_ATTEMPTS, ex);
                } else {
                    log.warn("[驾车路径规划] 第 {} 次调用失败：{}", attempt, ex.getMessage());
                }
            }
        }
        return null;
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
        boolean acquired;
        try {
            acquired = this.locationLimiter.tryAcquire(2, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("[MapUtil] 地理编码限流等待被中断：{}", ie.getMessage());
            return null;
        }
        if (!acquired) {
            log.warn("[MapUtil] 地理编码请求过载，等待限流超时，跳过地址：{}", address);
            return null;
        }
        try {
            final String encAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            final String url = StrUtil.format(GEO_CODE, this.properties.getApiKey(), encAddress);
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
            return first.getStr("location");
        } catch (Exception ex) {
            log.error("[查询经纬度] 失败", ex);
            return null;
        } finally {
            this.locationLimiter.release();
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
