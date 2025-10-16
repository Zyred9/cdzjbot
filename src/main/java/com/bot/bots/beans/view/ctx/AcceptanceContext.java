package com.bot.bots.beans.view.ctx;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.bot.bots.database.entity.AcceptanceCtx;
import com.bot.bots.database.enums.CategoryEnum;
import com.bot.bots.database.enums.ForbidTypeEnum;
import com.bot.bots.database.enums.MaterialEnum;
import com.bot.bots.helper.DecimalHelper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 承兑报备的上下文
 *
 * @author zyred
 * @since 2025/10/10 22:43
 */
@Setter
@Getter
@Builder
@Accessors(chain = true)
public class AcceptanceContext {

    // 地址id
    private String provinceName;
    private String cityName;
    private String countyName;
    // 分类
    private List<CategoryEnum> categories;
    // 输入区间
    private Boolean waitInputInterval;
    // 已输入区间
    private Boolean doneInputInterval;
    // 区间值
    private String intervalInput;
    // 汇率值
    private BigDecimal rate;
    // 料性
    private List<MaterialEnum> materials;
    // 多选禁止
    private List<ForbidTypeEnum> forbids;
    // 是否空降
    private boolean showAirborne;
    private Boolean airborne;
    // 是否驻点
    private boolean showStation;
    private Boolean station;
    // 是否移动
    private boolean showMove;
    private Boolean move;
    // 是否跟车
    private boolean showFollow;
    private Boolean follow;


    ////// 承兑所在地 范围查询等待输入  //////////
    private boolean showScope;
    private Integer scope;

    ////// 卸货合作商/卸货所在地  //////////
    // 模式
    private List<String> models;
    // 是否要求钱包在现场
    private Boolean packet;
    // 补充要求
    private boolean showSupplement;
    private String supplement;


    public AcceptanceCtx buildCtx () {
        return new AcceptanceCtx()
                .setAddress(this.provinceName+this.cityName+this.countyName)
                .setCategories(this.categories)
                .setIntervalInput(this.intervalInput)
                .setRate(this.rate)
                .setMaterials(this.materials)
                .setForbids(this.forbids)
                .setAirborne(this.airborne)
                .setStation(this.station)
                .setMove(this.move)
                .setFollow(this.follow);
    }


    public void setCategories(CategoryEnum of) {
        if (CollUtil.isEmpty(this.categories)) {
            this.categories = new ArrayList<>();
        }

        boolean exist = false;
        if (this.categories.contains(of)) {
            exist = this.categories.removeIf(a -> Objects.equals(a, of));
        }

        if (!exist) {
            this.categories.add(of);
        }
    }

    public void setForbids(ForbidTypeEnum of) {
        if (CollUtil.isEmpty(this.forbids)) {
            this.forbids = new ArrayList<>();
        }

        boolean exist = false;
        if (this.forbids.contains(of)) {
            exist = this.forbids.removeIf(a -> Objects.equals(a, of));
        }

        if (!exist) {
            this.forbids.add(of);
        }
    }

    public void incrementRate(double v) {
        BigDecimal value = BigDecimal.valueOf(v);
        rate = Objects.nonNull(rate) ? rate : value;
        this.rate = rate.add(value);
    }

    public void subtractRate(double v) {
        BigDecimal value = BigDecimal.valueOf(v);
        rate = Objects.nonNull(rate) ? rate : value;
        this.rate = rate.subtract(value);
    }

    public static String buildCtxText (AcceptanceContext ctx) {
        StringBuilder sb = new StringBuilder();
        if (StrUtil.isNotBlank(ctx.getProvinceName()) || StrUtil.isNotBlank(ctx.getCityName()) || StrUtil.isNotBlank(ctx.getCountyName())) {
            sb.append("所选地区：");
            if (StrUtil.isNotBlank(ctx.getProvinceName())) {
                sb.append(ctx.getProvinceName());
            }
            if (StrUtil.isNotBlank(ctx.getCityName())) {
                sb.append(ctx.getCityName());
            }
            if (StrUtil.isNotBlank(ctx.getCountyName())) {
                sb.append(ctx.getCountyName());
            }
            sb.append("\n");
        }

        if (CollUtil.isNotEmpty(ctx.getCategories())) {
            sb.append("当前选择物品：");
            List<String> list = ctx.getCategories().stream().map(CategoryEnum::getDesc).toList();
            sb.append(StrUtil.join(",", list)).append("（多选）").append("\n");
        }

        if (Boolean.TRUE.equals(ctx.getWaitInputInterval()) && Boolean.FALSE.equals(ctx.getDoneInputInterval())) {
            sb.append("----------------------").append("\n").append("点击下面按钮输入接单额度区间：");
        }

        if (Boolean.TRUE.equals(ctx.getDoneInputInterval())) {
            sb.append("区间：").append(ctx.getIntervalInput()).append("\n");
        }

        if (Objects.nonNull(ctx.getRate())) {
            sb.append("汇率：").append(DecimalHelper.decimalParse(ctx.getRate())).append("（汇率越低越好接单）").append("\n");
        }

        if (CollUtil.isNotEmpty(ctx.getMaterials())) {
            List<String> materialDescList = ctx.getMaterials().stream().map(MaterialEnum::getDesc).toList();
            String desc = StrUtil.join(",", materialDescList);
            sb.append("料性：").append(desc).append("（可多选）").append("\n");
        }

        if (CollUtil.isNotEmpty(ctx.getForbids())) {
            sb.append("禁止：");
            List<String> list = ctx.getForbids().stream().map(ForbidTypeEnum::getDesc).toList();
            sb.append(StrUtil.join(",", list)).append("（可多选）").append("\n");
        }

        if (Objects.isNull(ctx.getAirborne())) {
            if (ctx.isShowAirborne()) {
                sb.append("是否空降：（全国飞，一般常驻机场附近，机票全包，落地再飞，步跑空）").append("\n");
            }
        } else {
            sb.append("是否空降：").append(ctx.getAirborne() ? "是" : "否").append("\n");
        }

        if (Objects.isNull(ctx.getStation())) {
            if (ctx.isShowStation()) {
                sb.append("是否驻点：（固定地点接单，送到指定位置）").append("\n");
            }
        } else {
            sb.append("是否驻点：").append(ctx.getStation() ? "是" : "否").append("\n");
        }

        if (Objects.isNull(ctx.getMove())) {
            if (ctx.isShowMove()) {
                sb.append("是否移动：（可移动接单，一般是自身接单范围几百公里范围内）").append("\n");
            }
        } else {
            sb.append("是否移动：").append(ctx.getMove() ? "是" : "否").append("\n");
        }

        if (Objects.isNull(ctx.getFollow())) {
            if (ctx.isShowFollow()) {
                sb.append("是否跟车：（跟着车队走，包车票）").append("\n");
            }
        } else {
            sb.append("是否跟车：").append(ctx.getFollow() ? "是" : "否").append("\n");
        }

        if (Objects.isNull(ctx.getScope())) {
            if (ctx.isShowScope()) {
                sb.append("范围：请选择承兑所在范围或自定义公里数").append("\n");
            }
        } else {
            sb.append("范围：").append(ctx.getScope()).append("公里").append("\n");
        }

        ////// 卸货合作商/卸货所在地  //////////
        if (CollUtil.isNotEmpty(ctx.getModels())) {
            sb.append("选择你交易的方式：").append(StrUtil.join(",", ctx.getModels())).append("（可多选）").append("\n");
        }

        if (Objects.nonNull(ctx.getPacket())) {
            sb.append("是否要求钱包在现场：").append(ctx.getPacket() ? "是" : "否").append("\n");
        }

        if (StrUtil.isBlank(ctx.getSupplement())) {
            if (ctx.isShowSupplement()) {
                sb.append("补充要求：选填(无要求则点击跳过)").append("\n");
            }
        } else {
            sb.append("补充要求：").append(ctx.getSupplement()).append("\n");
        }
        return sb.toString();
    }


    public String getAllAddress() {
        return provinceName + cityName + countyName;
    }
}
