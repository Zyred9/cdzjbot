package com.bot.bots.database.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bot.bots.database.entity.Address;

/**
 * 地址库服务
 *
 * @author zyred
 * @since 1.0
 */
public interface AddressService extends IService<Address> {

    IPage<Address> pageByProvince(String provinceCode, Page<Address> page);

    IPage<Address> pageByCity(String cityCode, Page<Address> page);

    IPage<Address> pageByCounty(String countyCode, Page<Address> page);
}