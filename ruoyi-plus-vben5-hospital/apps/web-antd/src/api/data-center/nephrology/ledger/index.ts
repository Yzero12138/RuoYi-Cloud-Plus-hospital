import type { NephrologyLedgerCountItem, NephrologyLedgerDetailItem } from './model';

import type { PageQuery, PageResult } from '#/api/common';

import { requestClient } from '#/api/request';

enum Api {
  detailList = '/data-center/nephrology/ledger/detail/list',
  list = '/data-center/nephrology/ledger/list',
}

/**
 * Ledger count tree query.
 */
export function nephrologyLedgerList(params?: Record<string, any>) {
  return requestClient.get<NephrologyLedgerCountItem[]>(Api.list, { params });
}

/**
 * Ledger detail page query.
 */
export function nephrologyLedgerDetailList(params?: PageQuery) {
  return requestClient.get<PageResult<NephrologyLedgerDetailItem>>(Api.detailList, { params });
}
