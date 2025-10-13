package com.bot.bots.beans.view.ctx;


import cn.hutool.core.collection.CollUtil;
import com.bot.bots.database.enums.CategoryEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 *
 * @author zyred
 * @since 2025/10/11 17:40
 */
@Setter
@Getter
@Builder
@Accessors(chain = true)
public class AcceptQueryContext {

    private String provinceName;
    private String cityName;
    private String countyName;

    /** 分类 **/
    private List<CategoryEnum> categories;

    /** 范围 **/
    private Integer scope;


    @SuppressWarnings("")
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
}
