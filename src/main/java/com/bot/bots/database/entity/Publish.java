package com.bot.bots.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bot.bots.database.enums.PublishStatus;
import lombok.Getter;
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

    // 是否免费发布（使用领取的免费机会）
    private Boolean freeFlag;

    public static Publish build(String text, Long userId, Boolean freeFlag) {
        return new Publish()
                .setText(text)
                .setUserId(userId)
                .setFreeFlag(freeFlag)
                .setPass(PublishStatus.AUDIT_WAIT);
    }

    public Publish setText(String text) {
        this.text = text;
        return this;
    }
}
