/**
 * 骗子曝光表结构（MySQL 5.7 - 8.x 通用）
 *
 * @author zyred
 * @since 1.0
 */
CREATE TABLE IF NOT EXISTS `t_expose` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '提交用户ID',
  `text_raw` TEXT COMMENT '用户提交的原始文本',
  `id_no` VARCHAR(128) DEFAULT '' COMMENT '骗子ID（用户名）',
  `nick_name` VARCHAR(128) DEFAULT '' COMMENT '骗子昵称（用户名）',
  `story` TEXT COMMENT '被骗经过',
  `u_link` VARCHAR(256) DEFAULT '' COMMENT '骗子U地址',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0待审核、1已发布、2已拒绝',
  `audit_by` BIGINT DEFAULT NULL COMMENT '审核人ID（操作者）',
  `audit_at` VARCHAR(64) DEFAULT NULL COMMENT '审核时间（字符串存储，避免时区问题）',
  `channel_message_id` INT DEFAULT NULL COMMENT '发布到频道后的消息ID',
  `json_data` JSON NULL COMMENT '预留JSON字段（MyBatis-Plus支持JSON存储）',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='骗子曝光记录';