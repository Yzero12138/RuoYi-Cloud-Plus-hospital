import type { PdPatient } from './model';

import type { ID, PageQuery, PageResult } from '#/api/common';

import { requestClient } from '#/api/request';

enum Api {
  list = '/data-center/nephrology/pd-patient/list',
  root = '/data-center/nephrology/pd-patient',
}

/**
 * Peritoneal dialysis patient list.
 */
export function pdPatientList(params?: PageQuery) {
  return requestClient.get<PageResult<PdPatient>>(Api.list, { params });
}

/**
 * Peritoneal dialysis patient detail.
 */
export function pdPatientInfo(id: ID) {
  return requestClient.get<PdPatient>(`${Api.root}/${id}`);
}
