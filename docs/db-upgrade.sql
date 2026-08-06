-- ============================
-- 增量升级脚本（待执行部分）
-- 服务器 45.77.4.60 已完成：t_tag / t_broadcast_* 4表 / t_config.query_keyboard / t_user.password / t_acceptance_ctx.customer_type + tag_id
-- 以下为剩余未执行的升级：多标签 tag_ids + 历史脏标签拆分
-- ============================

-- t_acceptance_ctx 标签改为多标签(json数组)：新增 tag_ids 并迁移旧数据
ALTER TABLE `t_acceptance_ctx`
  ADD COLUMN IF NOT EXISTS `tag_ids` json NULL COMMENT '标签ID列表(JSON数组，多标签)';
UPDATE `t_acceptance_ctx`
  SET `tag_ids` = JSON_ARRAY(`tag_id`)
  WHERE `tag_id` IS NOT NULL AND `tag_ids` IS NULL;

-- ============================
-- 历史脏标签拆分：t_tag 中带分隔符的整串标签拆成多个独立标签
-- 分隔符：中英文逗号、中英文分号、顿号、空格
-- 子标签已存在则复用，否则新建（颜色默认黑色）
-- ============================

-- Step A 为每个脏标签的子标签补建独立 tag 记录（先查重，不存在才插入）
INSERT INTO `t_tag` (`name`, `color`, `create_time`)
SELECT DISTINCT TRIM(wd.`word`), 'black', NOW()
FROM `t_tag` dirty
JOIN JSON_TABLE(
    CONCAT('["', REGEXP_REPLACE(dirty.`name`, '[，,、;； ]+', '","'), '"]'),
    '$[*]' COLUMNS (`word` VARCHAR(255) PATH '$')
) wd
WHERE dirty.`name` REGEXP '[，,、;； ]'
  AND TRIM(wd.`word`) <> ''
  AND NOT EXISTS (
      SELECT 1 FROM `t_tag` t
      WHERE t.`name` = TRIM(wd.`word`) AND t.`id` <> dirty.`id`
  );

-- Step B 将 t_acceptance_ctx.tag_ids 中的脏标签 id 替换为拆分后的子标签 id
-- 单条多表 UPDATE 重建 tag_ids：展开 -> 映射子标签 -> 去重聚合
-- 规避：JSON_ARRAYAGG 不支持 DISTINCT；子查询内相关引用 JSON_TABLE 不可靠
UPDATE `t_acceptance_ctx` ctx
JOIN (
    SELECT ctx2.`id` AS ctx_id,
           CONCAT('[',
                  GROUP_CONCAT(DISTINCT COALESCE(sub.`id`, jt.`tid`)
                               ORDER BY COALESCE(sub.`id`, jt.`tid`) SEPARATOR ','),
                  ']') AS new_tag_ids
    FROM `t_acceptance_ctx` ctx2
    JOIN JSON_TABLE(ctx2.`tag_ids`, '$[*]' COLUMNS (`tid` BIGINT PATH '$')) jt
    LEFT JOIN `t_tag` dirty ON dirty.`id` = jt.`tid`
                           AND dirty.`name` REGEXP '[，,、;； ]'
    LEFT JOIN JSON_TABLE(
        CONCAT('["', REGEXP_REPLACE(COALESCE(dirty.`name`, ''), '[，,、;； ]+', '","'), '"]'),
        '$[*]' COLUMNS (`word` VARCHAR(255) PATH '$')
    ) wd ON dirty.`id` IS NOT NULL
    LEFT JOIN `t_tag` sub ON sub.`name` = TRIM(wd.`word`)
                         AND sub.`id` <> dirty.`id`
    WHERE ctx2.`tag_ids` IS NOT NULL
      AND JSON_LENGTH(ctx2.`tag_ids`) > 0
      AND jt.`tid` IS NOT NULL
    GROUP BY ctx2.`id`
) agg ON agg.ctx_id = ctx.`id`
SET ctx.`tag_ids` = CAST(agg.new_tag_ids AS JSON);
