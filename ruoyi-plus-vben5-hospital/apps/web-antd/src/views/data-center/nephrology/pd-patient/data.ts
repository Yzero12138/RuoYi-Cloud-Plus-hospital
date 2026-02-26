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
    fieldName: 'idcardHash',
    label: '身份证HASH',
  },
  {
    component: 'Input',
    fieldName: 'sourcePatientId',
    label: '来源患者ID',
  },
];

export const columns: VxeGridProps['columns'] = [
  { type: 'seq', width: 60 },
  { title: '住院号', field: 'inpatientNo', width: 140 },
  { title: '门诊号', field: 'outpatientNo', width: 140 },
  { title: '姓名', field: 'name', width: 120 },
  { title: '性别', field: 'gender', width: 80 },
  { title: '年龄', field: 'age', width: 80 },
  { title: '出生日期', field: 'birthDate', width: 140 },
  { title: '身份证脱敏', field: 'idcardMask', width: 160 },
  { title: '身份证HASH', field: 'idcardHash', width: 200 },
  { title: '入院时间', field: 'admitDate', width: 160 },
  { title: '出院时间', field: 'dischargeDate', width: 160 },
  { title: '入院科室', field: 'admitDept', width: 160 },
  { title: '出院科室', field: 'dischargeDept', width: 160 },
  { title: '诊断名称', field: 'diagName', width: 200 },
  { title: '诊断编码', field: 'diagCode', width: 140 },
  { title: '来源患者ID', field: 'sourcePatientId', width: 180 },
  { title: '来源更新时间', field: 'sourceUpdatedAt', width: 160 },
  { title: '创建时间', field: 'createdAt', width: 160 },
  { title: '更新时间', field: 'updatedAt', width: 160 },
  { title: '身份证号', field: 'idCard', width: 160 },
  { title: '最小入院时间', field: 'minAdmitDate', width: 160 },
];
