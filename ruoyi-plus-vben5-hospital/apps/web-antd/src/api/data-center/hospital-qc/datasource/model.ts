export interface HospitalQcDataSourceItem {
  id?: number;
  sourceName?: string;
  sourceType?: 'MYSQL' | 'ORACLE' | 'SQLSERVER' | string;
  host?: string;
  port?: number;
  databaseName?: string;
  instanceName?: string;
  username?: string;
  passwordMasked?: string;
  passwordConfigured?: boolean;
  status?: number;
  remark?: string;
  createBy?: number;
  createTime?: string;
  updateBy?: number;
  updateTime?: string;
}

export interface HospitalQcDataSourceForm {
  id?: number;
  sourceName?: string;
  sourceType?: 'MYSQL' | 'ORACLE' | 'SQLSERVER' | string;
  host?: string;
  port?: number;
  databaseName?: string;
  instanceName?: string;
  username?: string;
  password?: string;
  status?: number;
  remark?: string;
}

export interface HospitalQcDataSourceTestForm {
  id?: number;
  sourceType?: string;
  host?: string;
  port?: number;
  databaseName?: string;
  instanceName?: string;
  username?: string;
  password?: string;
}

export interface HospitalQcConnectionTestResult {
  success?: boolean;
  elapsedMs?: number;
  message?: string;
}

export interface HospitalQcOptionItem {
  value: string;
  label: string;
}
