import type { HospitalQcLedgerItem, HospitalQcLedgerItemForm } from './model';

import type { ID, IDS } from '#/api/common';

import { requestClient } from '#/api/request';

enum Api {
  list = '/data-center/hospital-qc/ledger-maintain/list',
  root = '/data-center/hospital-qc/ledger-maintain',
}

export function hospitalQcLedgerMaintainList(params?: Record<string, any>) {
  return requestClient.get<HospitalQcLedgerItem[]>(Api.list, { params });
}

export function hospitalQcLedgerMaintainInfo(id: ID) {
  return requestClient.get<HospitalQcLedgerItem>(`${Api.root}/${id}`);
}

export function hospitalQcLedgerMaintainAdd(data: HospitalQcLedgerItemForm) {
  return requestClient.postWithMsg<void>(Api.root, data);
}

export function hospitalQcLedgerMaintainUpdate(data: HospitalQcLedgerItemForm) {
  return requestClient.putWithMsg<void>(Api.root, data);
}

export function hospitalQcLedgerMaintainRemove(id: ID | IDS) {
  return requestClient.deleteWithMsg<void>(`${Api.root}/${id}`);
}
