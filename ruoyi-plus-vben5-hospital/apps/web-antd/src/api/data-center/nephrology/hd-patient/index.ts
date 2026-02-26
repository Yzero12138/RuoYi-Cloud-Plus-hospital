import type { HdPatient } from './model';

import type { ID, PageQuery, PageResult } from '#/api/common';

import { requestClient } from '#/api/request';

enum Api {
  list = '/data-center/nephrology/hd-patient/list',
  root = '/data-center/nephrology/hd-patient',
}

/**
 * Hemodialysis patient list.
 */
export function hdPatientList(params?: PageQuery) {
  return requestClient.get<PageResult<HdPatient>>(Api.list, { params });
}

/**
 * Hemodialysis patient detail.
 */
export function hdPatientInfo(id: ID) {
  return requestClient.get<HdPatient>(`${Api.root}/${id}`);
}
