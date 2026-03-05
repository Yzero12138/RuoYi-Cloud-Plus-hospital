import type { HospitalQcDetailQuery, HospitalQcDetailPageResult } from './model';

import { requestClient } from '#/api/request';

enum Api {
  detailPage = '/data-center/hospital-qc/report/detail/page',
  detailColumns = '/data-center/hospital-qc/report/detail/columns',
}

export function hospitalQcDetailPage(data: HospitalQcDetailQuery) {
  return requestClient.post<HospitalQcDetailPageResult>(Api.detailPage, data);
}

export function hospitalQcDetailColumns(ledgerCode: string) {
  return requestClient.get<string[]>(Api.detailColumns, { params: { ledgerCode } });
}
