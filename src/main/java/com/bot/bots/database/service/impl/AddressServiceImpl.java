package com.bot.bots.database.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bot.bots.database.entity.Address;
import com.bot.bots.database.mapper.AddressMapper;
import com.bot.bots.database.service.AddressService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 地址库服务实现（冗余字段模糊查询 + 分页）
 *
 * @author zyred
 * @since 1.0
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    public IPage<Address> pageByProvince(String provinceCode, Page<Address> page) {
        var qw = Wrappers.<Address>lambdaQuery()
                .like(StrUtil.isNotBlank(provinceCode), Address::getProvinceCode, provinceCode);
        return this.baseMapper.selectPage(page, qw);
    }

    @Override
    public IPage<Address> pageByCity(String cityCode, Page<Address> page) {
        var qw = Wrappers.<Address>lambdaQuery()
                .like(StrUtil.isNotBlank(cityCode), Address::getCityCode, cityCode);
        return this.baseMapper.selectPage(page, qw);
    }

    @Override
    public IPage<Address> pageByCounty(String countyCode, Page<Address> page) {
        var qw = Wrappers.<Address>lambdaQuery()
                .like(StrUtil.isNotBlank(countyCode), Address::getCountyCode, countyCode);
        return this.baseMapper.selectPage(page, qw);
    }

    @Override
    public List<Address> selectProvince() {
        return this.baseMapper.selectProvince();
    }

    @Override
    public Page <Address> selectCity(int index, String provinceCode) {
        return this.baseMapper.selectCity(
                Page.of(index, 10),
                provinceCode
        );
    }

    @Override
    public Page<Address> selectCounty(int index, String cityCode) {
        return this.baseMapper.selectCounty(
                Page.of(index, 10),
                cityCode
        );
    }

    @Override
    public Address selectOneCounty(String countyCode) {
        return this.baseMapper.selectOne(
                Wrappers.<Address>lambdaQuery()
                        .eq(Address::getCountyCode, countyCode)
                        .last("limit 1")
        );
    }

    @Override
    public Address selectOneProvince(String provinceCode) {
        return this.baseMapper.selectOne(
                Wrappers.<Address>lambdaQuery()
                        .eq(Address::getProvinceCode, provinceCode)
                        .last("limit 1")
        );
    }

    @Override
    public Address selectOneCity(String cityCode) {
        return this.baseMapper.selectOne(
                Wrappers.<Address>lambdaQuery()
                        .eq(Address::getCityCode, cityCode)
                        .last("limit 1")
        );
    }
}