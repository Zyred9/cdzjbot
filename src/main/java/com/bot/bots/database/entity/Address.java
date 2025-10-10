package com.bot.bots.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 地址库实体（单表冗余存储省/市/县）
 *
 * @author zyred
 * @since 1.0
 */
@Setter
@Getter
@Accessors(chain = true)
@TableName("t_address")
public class Address {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String provinceCode;
    private String provinceName;

    private String cityCode;
    private String cityName;

    private String countyCode;
    private String countyName;

    private String fullName;
    private String path;

    private LocalDateTime createdAt;
}