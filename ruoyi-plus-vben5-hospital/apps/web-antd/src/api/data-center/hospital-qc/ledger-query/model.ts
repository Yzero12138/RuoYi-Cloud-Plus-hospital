export interface HospitalQcLedgerQueryItem {
  id?: number;
  deptId?: number;
  deptName?: string;
  queryCode?: string;
  queryName?: string;
  datasourceId?: number;
  countSql?: string;
  detailSql?: string;
  status?: number;
  remark?: string;
  createBy?: number;
  createTime?: string;
  updateBy?: number;
  updateTime?: string;
}

export interface HospitalQcLedgerQueryForm {
  id?: number;
  deptId?: number;
  deptName?: string;
  queryCode?: string;
  queryName?: string;
  datasourceId?: number;
  countSql?: string;
  detailSql?: string;
  status?: number;
  remark?: string;
}

export interface HospitalQcSqlTestForm {
  datasourceId?: number;
  countSql?: string;
  detailSql?: string;
  startTime?: string;
  endTime?: string;
  deptId?: number;
  deptName?: string;
}

export interface HospitalQcSqlTestResult {
  success?: boolean;
  elapsedMs?: number;
  countValue?: number;
  message?: string;
  columns?: string[];
  sampleRows?: Record<string, any>[];
}

export interface HospitalQcOptionItem {
  value: string;
  label: string;
}
