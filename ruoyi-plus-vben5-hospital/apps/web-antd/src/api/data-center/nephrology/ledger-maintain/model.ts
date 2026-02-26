export interface NephrologyLedgerMaintainItem {
  id?: number;
  parentId?: number;
  ledgerCode?: string;
  ledgerName?: string;
  nodeType?: string;
  queryTarget?: string;
  sortOrder?: number;
  status?: number;
  remark?: string;
  createBy?: number;
  createTime?: string;
  updateBy?: number;
  updateTime?: string;
  children?: NephrologyLedgerMaintainItem[];
}

export interface NephrologyLedgerMaintainForm {
  id?: number;
  parentId?: number;
  ledgerCode?: string;
  ledgerName?: string;
  nodeType?: string;
  queryTarget?: string;
  sortOrder?: number;
  status?: number;
  remark?: string;
}
