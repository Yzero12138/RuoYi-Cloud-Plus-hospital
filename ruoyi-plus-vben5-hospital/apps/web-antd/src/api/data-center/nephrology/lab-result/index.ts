import type { LabResult } from './model';

import type { ID, PageQuery, PageResult } from '#/api/common';

import { requestClient } from '#/api/request';

enum Api {
  list = '/data-center/nephrology/lab-result/list',
  root = '/data-center/nephrology/lab-result',
}

/**
 * Lab result detail list.
 */
export function labResultList(params?: PageQuery) {
  return requestClient.get<PageResult<LabResult>>(Api.list, { params });
}

/**
 * Lab result detail.
 */
export function labResultInfo(id: ID) {
  return requestClient.get<LabResult>(`${Api.root}/${id}`);
}
