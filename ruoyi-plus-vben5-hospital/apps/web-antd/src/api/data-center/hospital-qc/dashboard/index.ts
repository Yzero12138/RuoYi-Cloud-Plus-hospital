import type {
  HospitalQcDashboardFilterOptions,
  HospitalQcDashboardOverview,
  HospitalQcDashboardQuery,
} from './model';

import { requestClient } from '#/api/request';

enum Api {
  options = '/data-center/hospital-qc/dashboard/options',
  overview = '/data-center/hospital-qc/dashboard/overview',
}

export function hospitalQcDashboardOptions() {
  return requestClient.get<HospitalQcDashboardFilterOptions>(Api.options);
}

export function hospitalQcDashboardOverview(data: HospitalQcDashboardQuery) {
  return requestClient.post<HospitalQcDashboardOverview>(Api.overview, data);
}
