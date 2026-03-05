-- ========================================================
-- 医院数据质控中心-科室维度改造增量脚本
-- 文件名: data-center-hospital-qc-ledger-dept-scope.sql
-- 说明: 为台账配置增加科室维度支持，新增字段映射配置
-- ========================================================

-- --------------------------------------------------------
-- 1. 台账项表添加科室字段
-- --------------------------------------------------------
ALTER TABLE `hospital_qc_ledger_item`
    ADD COLUMN `dept_id` bigint DEFAULT NULL COMMENT '科室ID（为空表示全局）' AFTER `query_code`,
    ADD COLUMN `dept_name` varchar(128) DEFAULT NULL COMMENT '科室名称' AFTER `dept_id`,
    ADD KEY `idx_dept_id` (`dept_id`),
    ADD KEY `idx_dept_status_deleted` (`dept_id`, `status`, `is_deleted`);

-- --------------------------------------------------------
-- 2. 台账查询配置表添加字段映射配置
-- --------------------------------------------------------
ALTER TABLE `hospital_qc_ledger_query`
    ADD COLUMN `detail_field_mapping` json DEFAULT NULL COMMENT '明细字段映射配置（JSON格式）' AFTER `detail_sql`;

-- --------------------------------------------------------
-- 3. 历史数据迁移：为现有台账项设置默认科室
--    策略：根据create_dept设置dept_id
-- --------------------------------------------------------
UPDATE `hospital_qc_ledger_item`
SET `dept_id` = `create_dept`,
    `dept_name` = (SELECT `dept_name` FROM `sys_dept` WHERE `dept_id` = `hospital_qc_ledger_item`.`create_dept` LIMIT 1)
WHERE `dept_id` IS NULL AND `create_dept` IS NOT NULL;

-- 对于无法关联到科室的历史数据，设置为默认科室（需要根据实际调整）
-- UPDATE `hospital_qc_ledger_item` SET `dept_id` = 103, `dept_name` = '示例科室' WHERE `dept_id` IS NULL;

-- --------------------------------------------------------
-- 4. 添加联合唯一索引（同一科室下台账编码唯一）
-- 注意：先删除旧唯一索引，再添加新的联合唯一索引
-- --------------------------------------------------------
ALTER TABLE `hospital_qc_ledger_item`
    DROP INDEX `uk_ledger_code`,
    ADD UNIQUE KEY `uk_ledger_code_dept` (`ledger_code`, `dept_id`);

-- --------------------------------------------------------
-- 5. 更新示例数据
-- --------------------------------------------------------
UPDATE `hospital_qc_ledger_item` SET `dept_id` = 103, `dept_name` = '示例科室' WHERE `id` IN (1, 2, 3, 4, 5);

-- --------------------------------------------------------
-- 6. 验证语句
-- --------------------------------------------------------
-- SELECT * FROM `hospital_qc_ledger_item` WHERE `is_deleted` = 0 ORDER BY `dept_id`, `sort_order`;
-- SELECT * FROM `hospital_qc_ledger_query` WHERE `is_deleted` = 0 ORDER BY `dept_id`;
