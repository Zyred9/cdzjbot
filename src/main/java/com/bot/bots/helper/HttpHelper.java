package com.bot.bots.helper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bot.bots.beans.view.trx.PriceBean;
import com.bot.bots.config.Constants;
import com.bot.bots.database.enums.PaymentEnum;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
@Slf4j
@Component
public class HttpHelper {

    @Resource private OkHttpClient okHttpClient;

    /**
     * 查询欧易实时交易记录
     *
     * @param payment   支付方式
     * @param side      买入或卖出
     * @return          结果，失败返回空列表（永不返回 null）
     */
    public List<PriceBean> doQueryOkx (PaymentEnum payment, String side) {
        if (Objects.isNull(payment)) {
            return Collections.emptyList();
        }
        try {
            String url = StrUtil.format(Constants.OKX_BOOTS, side);
            url += payment.getParams();
            String responseBody = this.doGet(url);
            if (StrUtil.isBlank(responseBody)) {
                return Collections.emptyList();
            }
            JSONObject jsonObject = JSONUtil.parseObj(responseBody);
            JSONObject dataObject = jsonObject.getJSONObject("data");
            if (Objects.isNull(dataObject)) {
                return Collections.emptyList();
            }
            JSONArray sell = dataObject.getJSONArray("sell");
            if (CollUtil.isEmpty(sell)) {
                return Collections.emptyList();
            }
            List<PriceBean> priceBeans = JSONUtil.toList(sell, PriceBean.class);
            priceBeans.removeIf(priceBean -> Objects.isNull(priceBean) || StrUtil.isBlank(priceBean.getPrice()));
            if (CollUtil.isEmpty(priceBeans)) {
                return Collections.emptyList();
            }
            // 按价格数值升序排序（price 为字符串，直接字典序会出错）
            priceBeans.sort(Comparator.comparing(priceBean -> this.parsePrice(priceBean.getPrice())));
            return priceBeans;
        } catch (Exception ex) {
            log.error("[查询欧易汇率] 失败，支付方式：{}，异常信息：{}", payment, ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    /**
     * 价格字符串解析为 BigDecimal，解析失败返回极大值排到列表末尾，保证整体排序可用
     */
    private BigDecimal parsePrice(String price) {
        try {
            return new BigDecimal(price);
        } catch (Exception ex) {
            return BigDecimal.valueOf(Double.MAX_VALUE);
        }
    }

    private String doGet (String url) {
        try (Response response = this.okHttpClient.newCall(
                new Request.Builder()
                        .get()
                        .url(HttpUrl.get(url))
                        .build()
        ).execute()) {
            if (!response.isSuccessful() || Objects.isNull(response.body())) {
                return null;
            }
            BufferedSource source = response.body().source();
            return source.readUtf8();
        } catch (IOException ex) {
            log.error("[执行请求失败] 请求地址：{} 异常信息：{}", url, ex.getMessage(), ex);
            return null;
        }
    }
}
