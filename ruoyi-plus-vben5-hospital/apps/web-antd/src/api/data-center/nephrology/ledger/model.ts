export interface NephrologyLedgerCountItem {
  id?: number;
  parentId?: number;
  ledgerCode?: string;
  ledgerName?: string;
  nodeType?: string;
  queryTarget?: string;
  sortOrder?: number;
  countValue?: number;
  numeratorCount?: number;
  denominatorCount?: number;
  queryable?: boolean;
  expand?: boolean;
  children?: NephrologyLedgerCountItem[];
}

export interface NephrologyLedgerDetailItem {
  id?: number;
  medicalRecordNo?: string;
  patientName?: string;
  idCardNo?: string;
  dischargeDepartment?: string;
  dischargeTime?: string;
  diagnosisName?: string;
  diagnosisCode?: string;
  sourceType?: string;
}
