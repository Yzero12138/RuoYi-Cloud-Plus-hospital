import type {
  HospitalQcLedgerQueryForm,
  HospitalQcLedgerQueryItem,
  HospitalQcOptionItem,
  HospitalQcSqlTestForm,
  HospitalQcSqlTestResult,
} from './model';

import type { ID, IDS, PageQuery, PageResult } from '#/api/common';

import { requestClient } from '#/api/request';

enum Api {
  list = '/data-center/hospital-qc/ledger-query/list',
  options = '/data-center/hospital-qc/ledger-query/options',
  root = '/data-center/hospital-qc/ledger-query',
  testSql = '/data-center/hospital-qc/ledger-query/test-sql',
}

export function hospitalQcLedgerQueryList(params?: PageQuery) {
  return requestClient.get<PageResult<HospitalQcLedgerQueryItem>>(Api.list, {
    params,
  });
}

export function hospitalQcLedgerQueryOptions() {
  return requestClient.get<HospitalQcOptionItem[]>(Api.options);
}

export function hospitalQcLedgerQueryInfo(id: ID) {
  return requestClient.get<HospitalQcLedgerQueryItem>(`${Api.root}/${id}`);
}

export function hospitalQcLedgerQueryAdd(data: HospitalQcLedgerQueryForm) {
  return requestClient.postWithMsg<void>(Api.root, data);
}

export function hospitalQcLedgerQueryUpdate(data: HospitalQcLedgerQueryForm) {
  return requestClient.putWithMsg<void>(Api.root, data);
}

export function hospitalQcLedgerQueryRemove(id: ID | IDS) {
  return requestClient.deleteWithMsg<void>(`${Api.root}/${id}`);
}

export function hospitalQcLedgerQueryTestSql(data: HospitalQcSqlTestForm) {
  return requestClient.post<HospitalQcSqlTestResult>(Api.testSql, data);
}
