package com.bot.bots.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bot.bots.database.enums.PublishStatus;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
@Setter
@Getter
@Accessors(chain = true)
@TableName("t_publish")
public class Publish {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String text;
    private PublishStatus pass;

    public static Publish build(String text, Long userId) {
        return new Publish()
                .setText(text)
                .setUserId(userId)
                .setPass(PublishStatus.AUDIT_WAIT);
    }
}
