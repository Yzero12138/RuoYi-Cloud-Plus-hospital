import type { HospitalQcReportQuery, HospitalQcReportRow } from './model';

import { requestClient } from '#/api/request';

enum Api {
  summary = '/data-center/hospital-qc/report/summary',
}

export function hospitalQcReportSummary(data: HospitalQcReportQuery) {
  return requestClient.post<HospitalQcReportRow[]>(Api.summary, data);
}
