import type {
  HospitalQcConnectionTestResult,
  HospitalQcDataSourceForm,
  HospitalQcDataSourceItem,
  HospitalQcDataSourceTestForm,
  HospitalQcOptionItem,
} from './model';

import type { ID, IDS, PageQuery, PageResult } from '#/api/common';

import { requestClient } from '#/api/request';

enum Api {
  changeStatus = '/data-center/hospital-qc/datasource/change-status',
  list = '/data-center/hospital-qc/datasource/list',
  options = '/data-center/hospital-qc/datasource/options',
  root = '/data-center/hospital-qc/datasource',
  test = '/data-center/hospital-qc/datasource/test',
}

export function hospitalQcDatasourceList(params?: PageQuery) {
  return requestClient.get<PageResult<HospitalQcDataSourceItem>>(Api.list, {
    params,
  });
}

export function hospitalQcDatasourceOptions() {
  return requestClient.get<HospitalQcOptionItem[]>(Api.options);
}

export function hospitalQcDatasourceInfo(id: ID) {
  return requestClient.get<HospitalQcDataSourceItem>(`${Api.root}/${id}`);
}

export function hospitalQcDatasourceAdd(data: HospitalQcDataSourceForm) {
  return requestClient.postWithMsg<void>(Api.root, data);
}

export function hospitalQcDatasourceUpdate(data: HospitalQcDataSourceForm) {
  return requestClient.putWithMsg<void>(Api.root, data);
}

export function hospitalQcDatasourceChangeStatus(data: {
  id?: number;
  status?: number;
}) {
  return requestClient.putWithMsg<void>(Api.changeStatus, data);
}

export function hospitalQcDatasourceTest(data: HospitalQcDataSourceTestForm) {
  return requestClient.post<HospitalQcConnectionTestResult>(Api.test, data);
}

export function hospitalQcDatasourceRemove(id: ID | IDS) {
  return requestClient.deleteWithMsg<void>(`${Api.root}/${id}`);
}
