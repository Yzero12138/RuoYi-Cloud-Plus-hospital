import type { HemodialysisComplicationPatient } from './model';

import type { ID, PageQuery, PageResult } from '#/api/common';

import { requestClient } from '#/api/request';

enum Api {
  list = '/data-center/nephrology/hemodialysis-complication-patient/list',
  root = '/data-center/nephrology/hemodialysis-complication-patient',
}

/**
 * Indicator 132 numerator list.
 */
export function hemodialysisComplicationPatientList(params?: PageQuery) {
  return requestClient.get<PageResult<HemodialysisComplicationPatient>>(Api.list, { params });
}

/**
 * Detail.
 */
export function hemodialysisComplicationPatientInfo(id: ID) {
  return requestClient.get<HemodialysisComplicationPatient>(`${Api.root}/${id}`);
}
