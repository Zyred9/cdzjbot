package com.bot.bots.helper;

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
     * @return          结果
     */
    public List<PriceBean> doQueryOkx (PaymentEnum payment, String side) {
        String url = StrUtil.format(Constants.OKX_BOOTS, side);
        if (!Objects.equals(PaymentEnum.ALL, payment)) {
            url += PaymentEnum.ALIPAY.getParams();
        }
        String responseBody = this.doGet(url);
        if (StrUtil.isBlank(responseBody)) {
            return null;
        }
        JSONObject jsonObject = JSONUtil.parseObj(responseBody);
        JSONObject dataObject = (JSONObject)jsonObject.getObj("data");
        JSONArray buy = dataObject.getJSONArray("sell");
        return JSONUtil.toList(buy, PriceBean.class);
    }

    private String doGet (String url) {
        try {
            Response response = this.okHttpClient.newCall(
                    new Request.Builder()
                            .get()
                            .url(HttpUrl.get(url))
                            .build()
            ).execute();

            if (Objects.isNull(response.body())) {
                response.close();
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
