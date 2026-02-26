export interface NephrologyLedgerQueryItem {
  id?: number;
  deptId?: number;
  deptName?: string;
  queryCode?: string;
  queryName?: string;
  countSql?: string;
  detailSql?: string;
  status?: number;
  remark?: string;
  createBy?: number;
  createTime?: string;
  updateBy?: number;
  updateTime?: string;
}

export interface NephrologyLedgerQueryForm {
  id?: number;
  deptId?: number;
  deptName?: string;
  queryCode?: string;
  queryName?: string;
  countSql?: string;
  detailSql?: string;
  status?: number;
  remark?: string;
}

