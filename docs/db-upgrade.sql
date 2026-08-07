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

-- ============================
-- 承兑驻地查询 NPE 修复：Excel 导入的记录 categories 为 NULL，
-- 用户按分类查询时全表遍历命中 NULL 即抛 NPE（CallbackHandler.queryAcceptanceCtx），
-- 刷成空数组 [] 兜底（无分类的客户在按分类查询时自然被过滤）
-- ============================
UPDATE `t_acceptance_ctx`
  SET `categories` = JSON_ARRAY()
  WHERE `categories` IS NULL OR JSON_TYPE(`categories`) = 'NULL';

-- ============================
-- 存量 Excel 导入数据全面修复（针对修复前导入的历史数据）
-- 识别特征：正常 bot 报备必选分类（categories 非空），Excel 导入记录无分类
-- 历史导入遗留问题：
--   ① categories 为 NULL   → 驻地查询 NPE（上面已修）
--   ② location 为 NULL     → 驻地查询永远查不到（代码已加「补定位」命令，后台群发即可）
--   ③ customerType 错标    → 旧逻辑"未合作"被误判为已合作，原始文本已丢失，无法用 SQL 区分，
--                             只能重新导入原始 Excel 或后台人工核对（见下方重导方案）
--   ④ userId 随机正数撞号  → 与真实 TG 用户 ID 同量级随机，可能撞真实用户/内部重复，下方修复
-- ============================

-- 1) 检测：Excel 导入数据总量
SELECT COUNT(*) AS excel_imported_total
FROM `t_acceptance_ctx`
WHERE `categories` IS NULL OR JSON_LENGTH(`categories`) = 0;

-- 2) 检测：userId 与真实用户(t_user)撞号的 Excel 记录
SELECT ctx.`id`, ctx.`user_id`, ctx.`username`, ctx.`nickname`,
       u.`user_id` AS real_user_id, u.`username` AS real_username
FROM `t_acceptance_ctx` ctx
JOIN `t_user` u ON u.`user_id` = ctx.`user_id`
WHERE ctx.`categories` IS NULL OR JSON_LENGTH(ctx.`categories`) = 0;

-- 3) 检测：Excel 记录内部 userId 重复
SELECT `user_id`, COUNT(*) AS cnt
FROM `t_acceptance_ctx`
WHERE `categories` IS NULL OR JSON_LENGTH(`categories`) = 0
GROUP BY `user_id` HAVING cnt > 1;

-- 4) 修复：Excel 记录 userId 与真实用户撞号 → 重分配为 t_user 最大 userId 之后递增值
--    仅处理无分类(Excel 导入)记录，正常报备用户不受影响
UPDATE `t_acceptance_ctx` ctx
JOIN (
    SELECT `id`,
           (SELECT COALESCE(MAX(`user_id`), 1000000000) FROM `t_user`)
               + ROW_NUMBER() OVER (ORDER BY `id`) AS new_user_id
    FROM `t_acceptance_ctx`
    WHERE (`categories` IS NULL OR JSON_LENGTH(`categories`) = 0)
      AND `user_id` IN (SELECT `user_id` FROM `t_user`)
) tmp ON tmp.`id` = ctx.`id`
SET ctx.`user_id` = tmp.`new_user_id`;

-- 5) 修复：Excel 记录内部 userId 重复 → 保留最小 id 那条，其余重分配
UPDATE `t_acceptance_ctx` ctx
JOIN (
    SELECT `id`,
           (SELECT COALESCE(MAX(`user_id`), 1000000000) FROM `t_user`)
               + ROW_NUMBER() OVER (ORDER BY `id`) AS new_user_id
    FROM `t_acceptance_ctx`
    WHERE (`categories` IS NULL OR JSON_LENGTH(`categories`) = 0)
      AND `user_id` IN (
          SELECT `user_id` FROM `t_acceptance_ctx`
          WHERE `categories` IS NULL OR JSON_LENGTH(`categories`) = 0
          GROUP BY `user_id` HAVING COUNT(*) > 1
      )
      AND `id` NOT IN (
          SELECT MIN(`id`) FROM `t_acceptance_ctx`
          WHERE `categories` IS NULL OR JSON_LENGTH(`categories`) = 0
          GROUP BY `user_id`
      )
) tmp ON tmp.`id` = ctx.`id`
SET ctx.`user_id` = tmp.`new_user_id`;

-- 执行 4/5 后重新跑 2/3 检测，确认无残留

-- ============================
-- 彻底重导方案（推荐，前提：保留原始 Excel 文件）
-- 新导入逻辑已修复全部问题：userId 递增不撞号、客户类型顺序正确、导入后自动补定位
-- 步骤：① 清空旧 Excel 数据（无分类记录；正常报备必有分类，不会被误删）
--       ② 后台群重新上传原始 Excel
-- ============================
-- DELETE FROM `t_acceptance_ctx` WHERE `categories` IS NULL OR JSON_LENGTH(`categories`) = 0;
-- 重新上传 Excel 后，自动补定位完成后，后台群发「补定位」处理无 location 的残留（如有）

-- ============================
-- 群发支持图片：t_broadcast_log 增加 has_image 标记，content 允许为空（纯图片消息）
-- ============================
ALTER TABLE `t_broadcast_log`
  ADD COLUMN IF NOT EXISTS `has_image` tinyint NOT NULL DEFAULT 0 COMMENT '是否含图片：0否 1是' AFTER `content`;
ALTER TABLE `t_broadcast_log`
  MODIFY COLUMN `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '发送内容';

-- ============================
-- 私聊用户列表新增创建时间：t_user 增加 create_time 字段
-- 新用户由 DEFAULT CURRENT_TIMESTAMP 自动落时间；历史用户统一回填为升级执行时间
-- ============================
ALTER TABLE `t_user`
  ADD COLUMN IF NOT EXISTS `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `password`;
