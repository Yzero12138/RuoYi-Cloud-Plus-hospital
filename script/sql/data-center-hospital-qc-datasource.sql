-- 医院数据质控中心-数据源配置表（独立脚本）
DROP TABLE IF EXISTS `hospital_qc_datasource`;

CREATE TABLE `hospital_qc_datasource` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `source_name` varchar(128) NOT NULL COMMENT '数据源名称',
  `source_type` varchar(32) NOT NULL COMMENT '数据源类型：MYSQL/ORACLE/SQLSERVER',
  `host` varchar(255) NOT NULL COMMENT '主机地址',
  `port` int NOT NULL COMMENT '端口',
  `database_name` varchar(128) DEFAULT NULL COMMENT '库名/服务名',
  `instance_name` varchar(128) DEFAULT NULL COMMENT '实例名（如Oracle SID）',
  `username` varchar(128) NOT NULL COMMENT '用户名',
  `password_cipher` varchar(1024) NOT NULL COMMENT '加密后的密码',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0正常 1停用',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0否 1是',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_source_name` (`source_name`),
  KEY `idx_status_deleted` (`status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医院数据质控中心-外部数据源配置';

-- 说明：密码由后端通过 AES-GCM 加密后写入 password_cipher，脚本不提供明文初始化数据。
