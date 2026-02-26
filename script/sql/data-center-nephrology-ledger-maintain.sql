-- 肾内科台账维护表（独立脚本）
DROP TABLE IF EXISTS `nephrology_ledger_item`;

CREATE TABLE `nephrology_ledger_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父节点ID，0为根节点',
  `ledger_code` varchar(64) NOT NULL COMMENT '台账编码',
  `ledger_name` varchar(255) NOT NULL COMMENT '台账名称',
  `node_type` char(1) NOT NULL COMMENT '节点类型：I指标 N分子 D分母',
  `query_target` varchar(64) NOT NULL DEFAULT 'NONE' COMMENT '查询目标：NONE/HD_COMPLICATION_NUMERATOR/HD_DISCHARGE_DENOMINATOR',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='肾内科台账维护表';

INSERT INTO `nephrology_ledger_item`
(`id`, `parent_id`, `ledger_code`, `ledger_name`, `node_type`, `query_target`, `sort_order`, `status`, `is_deleted`, `remark`, `create_dept`, `create_by`)
VALUES
(1, 0, '132', '132血液透析所致并发症发生例数', 'I', 'NONE', 1, 0, 0, '132指标', 103, 1),
(2, 1, '132_NUMERATOR', '分子：血液透析所致并发症发生例数', 'N', 'HD_COMPLICATION_NUMERATOR', 1, 0, 0, '132分子', 103, 1),
(3, 0, '133', '133血液透析所致并发症发生例率', 'I', 'NONE', 2, 0, 0, '133指标', 103, 1),
(4, 3, '133_NUMERATOR', '分子：出院患者血液透析所致并发症发生例数', 'N', 'HD_COMPLICATION_NUMERATOR', 1, 0, 0, '133分子', 103, 1),
(5, 3, '133_DENOMINATOR', '分母：同期血液透析出院患者人次数', 'D', 'HD_DISCHARGE_DENOMINATOR', 2, 0, 0, '133分母', 103, 1);

-- 如果库中已有数据需要保留，可删除本脚本中的 DROP TABLE 语句，并仅执行 CREATE/INSERT 之后的增量部分。
