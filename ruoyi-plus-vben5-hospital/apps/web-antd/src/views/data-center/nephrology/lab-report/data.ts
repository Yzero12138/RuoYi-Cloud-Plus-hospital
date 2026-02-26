import type { FormSchemaGetter } from '#/adapter/form';
import type { VxeGridProps } from '#/adapter/vxe-table';

export const querySchema: FormSchemaGetter = () => [
  {
    component: 'Input',
    fieldName: 'name',
    label: '姓名',
  },
  {
    component: 'Input',
    fieldName: 'reportName',
    label: '报告名称',
  },
  {
    component: 'Input',
    fieldName: 'inpatientNo',
    label: '住院号',
  },
  {
    component: 'Input',
    fieldName: 'outpatientNo',
    label: '门诊号',
  },
  {
    component: 'Input',
    fieldName: 'sourceReportNo',
    label: '报告号',
  },
];

export const columns: VxeGridProps['columns'] = [
  { type: 'seq', width: 60 },
  { title: '住院号', field: 'inpatientNo', width: 140 },
  { title: '门诊号', field: 'outpatientNo', width: 140 },
  { title: '报告时间', field: 'reportTime', width: 160 },
  { title: '姓名', field: 'name', width: 120 },
  { title: '申请科室', field: 'applyDept', width: 160 },
  { title: '报告名称', field: 'reportName', width: 200 },
  { title: '身份证脱敏', field: 'idcardMask', width: 160 },
  { title: '身份证HASH', field: 'idcardHash', width: 200 },
  { title: '报告号', field: 'sourceReportNo', width: 160 },
  { title: '来源更新时间', field: 'sourceUpdatedAt', width: 160 },
  { title: '创建时间', field: 'createdAt', width: 160 },
  { title: '更新时间', field: 'updatedAt', width: 160 },
];
