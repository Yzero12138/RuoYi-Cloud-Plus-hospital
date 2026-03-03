export interface HospitalQcLedgerItem {
  id?: number;
  parentId?: number;
  ledgerCode?: string;
  ledgerName?: string;
  nodeType?: 'I' | 'N' | 'D' | string;
  queryCode?: string;
  sortOrder?: number;
  status?: number;
  remark?: string;
  createBy?: number;
  createTime?: string;
  updateBy?: number;
  updateTime?: string;
  children?: HospitalQcLedgerItem[];
}

export interface HospitalQcLedgerItemForm {
  id?: number;
  parentId?: number;
  ledgerCode?: string;
  ledgerName?: string;
  nodeType?: 'I' | 'N' | 'D' | string;
  queryCode?: string;
  sortOrder?: number;
  status?: number;
  remark?: string;
}
