import type {
  NephrologyLedgerQueryForm,
  NephrologyLedgerQueryItem,
} from './model';

import type { ID, IDS, PageQuery, PageResult } from '#/api/common';

import { requestClient } from '#/api/request';

enum Api {
  list = '/data-center/nephrology/ledger-query/list',
  options = '/data-center/nephrology/ledger-query/options',
  root = '/data-center/nephrology/ledger-query',
}

export function nephrologyLedgerQueryList(params?: PageQuery) {
  return requestClient.get<PageResult<NephrologyLedgerQueryItem>>(Api.list, {
    params,
  });
}

export function nephrologyLedgerQueryOptions() {
  return requestClient.get<NephrologyLedgerQueryItem[]>(Api.options);
}

export function nephrologyLedgerQueryInfo(id: ID) {
  return requestClient.get<NephrologyLedgerQueryItem>(`${Api.root}/${id}`);
}

export function nephrologyLedgerQueryAdd(data: NephrologyLedgerQueryForm) {
  return requestClient.postWithMsg<void>(Api.root, data);
}

export function nephrologyLedgerQueryUpdate(data: NephrologyLedgerQueryForm) {
  return requestClient.putWithMsg<void>(Api.root, data);
}

export function nephrologyLedgerQueryRemove(id: ID | IDS) {
  return requestClient.deleteWithMsg<void>(`${Api.root}/${id}`);
}

