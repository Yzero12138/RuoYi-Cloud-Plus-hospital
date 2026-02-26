import type {
  NephrologyLedgerMaintainForm,
  NephrologyLedgerMaintainItem,
} from './model';

import type { ID, IDS } from '#/api/common';

import { requestClient } from '#/api/request';

enum Api {
  list = '/data-center/nephrology/ledger-maintain/list',
  root = '/data-center/nephrology/ledger-maintain',
}

export function nephrologyLedgerMaintainList(params?: Record<string, any>) {
  return requestClient.get<NephrologyLedgerMaintainItem[]>(Api.list, { params });
}

export function nephrologyLedgerMaintainInfo(id: ID) {
  return requestClient.get<NephrologyLedgerMaintainItem>(`${Api.root}/${id}`);
}

export function nephrologyLedgerMaintainAdd(data: NephrologyLedgerMaintainForm) {
  return requestClient.postWithMsg<void>(Api.root, data);
}

export function nephrologyLedgerMaintainUpdate(data: NephrologyLedgerMaintainForm) {
  return requestClient.putWithMsg<void>(Api.root, data);
}

export function nephrologyLedgerMaintainRemove(id: ID | IDS) {
  return requestClient.deleteWithMsg<void>(`${Api.root}/${id}`);
}
