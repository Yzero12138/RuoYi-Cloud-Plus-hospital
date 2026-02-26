import type { LabReport } from './model';

import type { ID, PageQuery, PageResult } from '#/api/common';

import { requestClient } from '#/api/request';

enum Api {
  list = '/data-center/nephrology/lab-report/list',
  root = '/data-center/nephrology/lab-report',
}

/**
 * Lab report list.
 */
export function labReportList(params?: PageQuery) {
  return requestClient.get<PageResult<LabReport>>(Api.list, { params });
}

/**
 * Lab report detail.
 */
export function labReportInfo(id: ID) {
  return requestClient.get<LabReport>(`${Api.root}/${id}`);
}
