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
    private MaterialEnum material;
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

    // 附加 承兑所在地 范围查询等待输入
    private boolean showScope;
    private Integer scope;

    public AcceptanceCtx buildCtx () {
        return new AcceptanceCtx()
                .setAddress(this.provinceName+this.cityName+this.countyName)
                .setCategories(this.categories)
                .setIntervalInput(this.intervalInput)
                .setRate(this.rate)
                .setMaterial(this.material)
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
            sb.append("所选分类：");
            List<String> list = ctx.getCategories().stream().map(CategoryEnum::getDesc).toList();
            sb.append(StrUtil.join(",", list)).append("\n");
        }

        if (Boolean.TRUE.equals(ctx.getWaitInputInterval()) && Boolean.FALSE.equals(ctx.getDoneInputInterval())) {
            sb.append("----------------------").append("\n").append("点击下面按钮输入接单额度区间：");
        }

        if (Boolean.TRUE.equals(ctx.getDoneInputInterval())) {
            sb.append("区间：").append(ctx.getIntervalInput()).append("\n");
        }

        if (Objects.nonNull(ctx.getRate())) {
            sb.append("汇率：").append(DecimalHelper.decimalParse(ctx.getRate())).append("\n");
        }

        if (Objects.nonNull(ctx.getMaterial())) {
            sb.append("料性：").append(ctx.getMaterial().getDesc()).append("\n");
        }

        if (CollUtil.isNotEmpty(ctx.getForbids())) {
            sb.append("禁止：");
            List<String> list = ctx.getForbids().stream().map(ForbidTypeEnum::getDesc).toList();
            sb.append(StrUtil.join(",", list)).append("\n");
        }

        if (Objects.isNull(ctx.getAirborne())) {
            if (ctx.isShowAirborne()) {
                sb.append("是否空降：偏远地区承兑少，包机票）超推荐！").append("\n");
            }
        } else {
            sb.append("是否空降：").append(ctx.getAirborne() ? "是" : "否").append("\n");
        }

        if (Objects.isNull(ctx.getStation())) {
            if (ctx.isShowStation()) {
                sb.append("是否驻点：（驻点承兑一般不移动，固定地址，车队送到手里）").append("\n");
            }
        } else {
            sb.append("是否驻点：").append(ctx.getStation() ? "是" : "否").append("\n");
        }

        if (Objects.isNull(ctx.getMove())) {
            if (ctx.isShowMove()) {
                sb.append("是否移动：（车队出车马费，可以移动一段距离，比空降近一些）").append("\n");
            }
        } else {
            sb.append("是否移动：").append(ctx.getMove() ? "是" : "否").append("\n");
        }

        if (Objects.isNull(ctx.getFollow())) {
            if (ctx.isShowFollow()) {
                sb.append("是否跟车：（跟车单独规则，有保底一般当天跑空补贴300U）").append("\n");
            }
        } else {
            sb.append("是否跟车：").append(ctx.getFollow() ? "是" : "否").append("\n");
        }

        if (Objects.isNull(ctx.getScope())) {
            if (ctx.isShowScope()) {
                sb.append("范围：请选择范围或自定义").append("\n");
            }
        } else {
            sb.append("范围：").append(ctx.getScope()).append("\n");
        }

        return sb.toString();
    }


    public String getAllAddress() {
        return provinceName + cityName + countyName;
    }
}
