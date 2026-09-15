package com.bot.bots.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 供需免费发布授权群
 *
 * @author zyred
 * @since 1.0
 */
@Setter
@Getter
@Accessors(chain = true)
@TableName("t_publish_auth_group")
public class PublishAuthGroup {

    // 群ID
    @TableId(type = IdType.INPUT)
    private Long chatId;

    // 授权时间
    private LocalDateTime createTime;
}
