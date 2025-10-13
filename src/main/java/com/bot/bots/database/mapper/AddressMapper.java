package com.bot.bots.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bot.bots.database.entity.Address;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 地址库 Mapper
 *
 * @author zyred
 * @since 1.0
 */
public interface AddressMapper extends BaseMapper<Address> {

    @SuppressWarnings("all")
    @Select("select province_code, province_name from t_address GROUP BY province_code, province_name")
    List<Address> selectProvince();

    @SuppressWarnings("all")
    @Select("select city_code, city_name from t_address where province_code = #{provinceCode} GROUP BY city_code, city_name")
    Page<Address> selectCity(@Param("page") IPage<Address> page, @Param("provinceCode") String provinceCode);

    @SuppressWarnings("all")
    @Select("select county_code, county_name from t_address where city_code = #{cityCode} GROUP BY county_code, county_name")
    Page<Address> selectCounty(@Param("page") IPage<Address> of, @Param("cityCode") String cityCode);
}