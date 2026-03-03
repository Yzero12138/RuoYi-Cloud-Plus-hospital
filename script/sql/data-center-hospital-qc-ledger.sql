-- 医院数据质控中心-台账配置表（独立脚本）
DROP TABLE IF EXISTS `hospital_qc_ledger_item`;
DROP TABLE IF EXISTS `hospital_qc_ledger_query`;

CREATE TABLE `hospital_qc_ledger_query` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dept_id` bigint DEFAULT NULL COMMENT '科室ID（为空表示全局）',
  `dept_name` varchar(128) DEFAULT NULL COMMENT '科室名称',
  `query_code` varchar(64) NOT NULL COMMENT '查询编码',
  `query_name` varchar(255) NOT NULL COMMENT '查询名称',
  `datasource_id` bigint NOT NULL COMMENT '数据源ID（关联 hospital_qc_datasource.id）',
  `count_sql` text NOT NULL COMMENT '计数SQL（必须包含:startTime/:endTime）',
  `detail_sql` text NOT NULL COMMENT '明细SQL（必须包含:startTime/:endTime，禁止LIMIT）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0正常 1停用',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0否 1是',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_query_code_dept` (`query_code`, `dept_id`),
  KEY `idx_datasource` (`datasource_id`),
  KEY `idx_status_deleted` (`status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医院数据质控中心-台账查询SQL配置';

CREATE TABLE `hospital_qc_ledger_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父节点ID，0为根节点',
  `ledger_code` varchar(64) NOT NULL COMMENT '台账编码',
  `ledger_name` varchar(255) NOT NULL COMMENT '台账名称',
  `node_type` char(1) NOT NULL COMMENT '节点类型：I指标 N分子 D分母',
  `query_code` varchar(64) NOT NULL DEFAULT 'NONE' COMMENT '绑定查询编码（指标默认NONE）',
  `sort_order` int NOT NULL DEFAULT '1' COMMENT '排序号',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0正常 1停用',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0否 1是',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ledger_code` (`ledger_code`),
  KEY `idx_parent_sort` (`parent_id`, `sort_order`),
  KEY `idx_status_deleted` (`status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医院数据质控中心-台账树配置';

-- 示例台账（演示无分母场景）
INSERT INTO `hospital_qc_ledger_item`
(`id`, `parent_id`, `ledger_code`, `ledger_name`, `node_type`, `query_code`, `sort_order`, `status`, `is_deleted`, `remark`, `create_dept`, `create_by`)
VALUES
(1, 0, 'QC_001', '住院患者术前评估完成率', 'I', 'NONE', 1, 0, 0, '百分比指标（有分母）', 103, 1),
(2, 1, 'QC_001_N', '分子：术前评估完成人数', 'N', 'QC_001_N', 1, 0, 0, '', 103, 1),
(3, 1, 'QC_001_D', '分母：术前应评估人数', 'D', 'QC_001_D', 2, 0, 0, '', 103, 1),
(4, 0, 'QC_002', '危急值闭环处置例数', 'I', 'NONE', 2, 0, 0, '无分母指标（仅分子）', 103, 1),
(5, 4, 'QC_002_N', '分子：危急值闭环处置例数', 'N', 'QC_002_N', 1, 0, 0, '', 103, 1);

-- 示例查询仅给出模板，占位 datasource_id=1，请按实际数据源修改并通过页面维护
INSERT INTO `hospital_qc_ledger_query`
(`id`, `dept_id`, `dept_name`, `query_code`, `query_name`, `datasource_id`, `count_sql`, `detail_sql`, `status`, `is_deleted`, `remark`, `create_dept`, `create_by`)
VALUES
(
  1,
  103,
  '示例科室',
  'QC_001_N',
  '术前评估完成人数（分子）',
  1,
  'SELECT COUNT(1) FROM your_table WHERE complete_time BETWEEN :startTime AND :endTime AND (:deptId IS NULL OR dept_id = :deptId) AND (:deptName IS NULL OR dept_name = :deptName)',
  'SELECT id, patient_no, patient_name, dept_name, complete_time FROM your_table WHERE complete_time BETWEEN :startTime AND :endTime AND (:deptId IS NULL OR dept_id = :deptId) AND (:deptName IS NULL OR dept_name = :deptName)',
  0,
  0,
  '请按真实库表调整',
  103,
  1
),
(
  2,
  103,
  '示例科室',
  'QC_001_D',
  '术前应评估人数（分母）',
  1,
  'SELECT COUNT(1) FROM your_table WHERE admit_time BETWEEN :startTime AND :endTime AND (:deptId IS NULL OR dept_id = :deptId) AND (:deptName IS NULL OR dept_name = :deptName)',
  'SELECT id, patient_no, patient_name, dept_name, admit_time FROM your_table WHERE admit_time BETWEEN :startTime AND :endTime AND (:deptId IS NULL OR dept_id = :deptId) AND (:deptName IS NULL OR dept_name = :deptName)',
  0,
  0,
  '请按真实库表调整',
  103,
  1
),
(
  3,
  103,
  '示例科室',
  'QC_002_N',
  '危急值闭环处置例数（无分母）',
  1,
  'SELECT COUNT(1) FROM your_table WHERE close_time BETWEEN :startTime AND :endTime AND (:deptId IS NULL OR dept_id = :deptId) AND (:deptName IS NULL OR dept_name = :deptName)',
  'SELECT id, case_no, dept_name, close_time FROM your_table WHERE close_time BETWEEN :startTime AND :endTime AND (:deptId IS NULL OR dept_id = :deptId) AND (:deptName IS NULL OR dept_name = :deptName)',
  0,
  0,
  '请按真实库表调整',
  103,
  1
);
