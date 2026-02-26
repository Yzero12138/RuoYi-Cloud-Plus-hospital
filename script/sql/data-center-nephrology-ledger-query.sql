-- 肾内科台账查询配置表（独立脚本）
DROP TABLE IF EXISTS `nephrology_ledger_query`;

CREATE TABLE `nephrology_ledger_query` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dept_id` bigint DEFAULT NULL COMMENT '科室ID',
  `dept_name` varchar(128) DEFAULT NULL COMMENT '科室名称',
  `query_code` varchar(64) NOT NULL COMMENT '查询编码',
  `query_name` varchar(255) NOT NULL COMMENT '查询名称',
  `count_sql` text NOT NULL COMMENT '计数SQL（需包含:startTime/:endTime）',
  `detail_sql` text NOT NULL COMMENT '明细SQL（需包含:startTime/:endTime）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0正常 1停用',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0否 1是',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_dept` bigint DEFAULT NULL COMMENT '创建部门',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '最后修改人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_query_code` (`query_code`),
  KEY `idx_status_deleted` (`status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='肾内科台账查询配置表';

INSERT INTO `nephrology_ledger_query`
(`id`, `dept_id`, `dept_name`, `query_code`, `query_name`, `count_sql`, `detail_sql`, `status`, `is_deleted`, `remark`, `create_dept`, `create_by`)
VALUES
(
  1,
  103,
  '肾内科',
  'HD_COMPLICATION_NUMERATOR',
  '血液透析并发症发生例数（分子）',
  'SELECT COUNT(1)\nFROM hemodialysis_complication_patient\nWHERE is_deleted = 0\n  AND discharge_time BETWEEN :startTime AND :endTime\n  AND (:medicalRecordNo IS NULL OR :medicalRecordNo = \'\' OR medical_record_no = :medicalRecordNo)\n  AND (:idCardNo IS NULL OR :idCardNo = \'\' OR id_card_no = :idCardNo)',
  'SELECT\n  id,\n  medical_record_no AS medicalRecordNo,\n  patient_name AS patientName,\n  id_card_no AS idCardNo,\n  discharge_department AS dischargeDepartment,\n  discharge_time AS dischargeTime,\n  complication_diagnosis AS diagnosisName,\n  complication_code AS diagnosisCode,\n  \'COMPLICATION\' AS sourceType\nFROM hemodialysis_complication_patient\nWHERE is_deleted = 0\n  AND discharge_time BETWEEN :startTime AND :endTime\n  AND (:medicalRecordNo IS NULL OR :medicalRecordNo = \'\' OR medical_record_no = :medicalRecordNo)\n  AND (:idCardNo IS NULL OR :idCardNo = \'\' OR id_card_no = :idCardNo)\nORDER BY discharge_time DESC, id DESC',
  0,
  0,
  '默认查询：并发症分子',
  103,
  1
),
(
  2,
  103,
  '肾内科',
  'HD_DISCHARGE_DENOMINATOR',
  '血液透析出院患者人次数（分母）',
  'SELECT COUNT(1)\nFROM hd_patient\nWHERE discharge_date IS NOT NULL\n  AND discharge_date BETWEEN :startTime AND :endTime\n  AND (:medicalRecordNo IS NULL OR :medicalRecordNo = \'\' OR inpatient_no = :medicalRecordNo OR outpatient_no = :medicalRecordNo)\n  AND (:idCardNo IS NULL OR :idCardNo = \'\' OR id_card = :idCardNo)',
  'SELECT\n  id,\n  COALESCE(inpatient_no, outpatient_no) AS medicalRecordNo,\n  name AS patientName,\n  id_card AS idCardNo,\n  discharge_dept AS dischargeDepartment,\n  discharge_date AS dischargeTime,\n  diag_name AS diagnosisName,\n  diag_code AS diagnosisCode,\n  \'HD_DISCHARGE\' AS sourceType\nFROM hd_patient\nWHERE discharge_date IS NOT NULL\n  AND discharge_date BETWEEN :startTime AND :endTime\n  AND (:medicalRecordNo IS NULL OR :medicalRecordNo = \'\' OR inpatient_no = :medicalRecordNo OR outpatient_no = :medicalRecordNo)\n  AND (:idCardNo IS NULL OR :idCardNo = \'\' OR id_card = :idCardNo)\nORDER BY discharge_date DESC, id DESC',
  0,
  0,
  '默认查询：出院分母',
  103,
  1
);

-- 如果库中已有数据需要保留，可删除本脚本中的 DROP TABLE 语句，并仅执行 CREATE/INSERT 之后的增量部分。
