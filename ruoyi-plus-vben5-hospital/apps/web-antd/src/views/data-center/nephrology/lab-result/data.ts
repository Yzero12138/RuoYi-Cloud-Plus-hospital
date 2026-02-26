import type { FormSchemaGetter } from '#/adapter/form';
import type { VxeGridProps } from '#/adapter/vxe-table';

export const querySchema: FormSchemaGetter = () => [
  {
    component: 'Input',
    fieldName: 'sourceReportNo',
    label: '报告号',
  },
  {
    component: 'Input',
    fieldName: 'itemCode',
    label: '项目代码',
  },
  {
    component: 'Input',
    fieldName: 'itemName',
    label: '项目名称',
  },
  {
    component: 'Input',
    fieldName: 'abnormalFlag',
    label: '异常标识',
  },
];

export const columns: VxeGridProps['columns'] = [
  { type: 'seq', width: 60 },
  { title: '报告号', field: 'sourceReportNo', width: 160 },
  { title: '项目代码', field: 'itemCode', width: 140 },
  { title: '项目名称', field: 'itemName', width: 200 },
  { title: '结果值', field: 'resultValue', width: 140 },
  { title: '结果单位', field: 'resultUnit', width: 120 },
  { title: '参考范围', field: 'refRange', width: 160 },
  { title: '异常标识', field: 'abnormalFlag', width: 120 },
  { title: '创建时间', field: 'createdAt', width: 160 },
];
