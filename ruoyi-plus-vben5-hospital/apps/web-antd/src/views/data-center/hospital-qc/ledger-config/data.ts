import type { FormSchemaGetter } from '#/adapter/form';
import type { VxeGridProps } from '#/adapter/vxe-table';

import { DictEnum } from '@vben/constants';

import { getDictOptions } from '#/utils/dict';
import { renderDict } from '#/utils/render';

export const nodeTypeOptions = [
  { label: '指标(I)', value: 'I' },
  { label: '分子(N)', value: 'N' },
  { label: '分母(D)', value: 'D' },
];

export const queryGridSchema: FormSchemaGetter = () => [
  {
    component: 'Input',
    fieldName: 'queryCode',
    label: '查询编码',
  },
  {
    component: 'Input',
    fieldName: 'queryName',
    label: '查询名称',
  },
  {
    component: 'Input',
    fieldName: 'deptName',
    label: '科室',
  },
  {
    component: 'Select',
    componentProps: {
      options: getDictOptions(DictEnum.SYS_NORMAL_DISABLE),
    },
    fieldName: 'status',
    label: '状态',
  },
];

export const queryGridColumns: VxeGridProps['columns'] = [
  {
    title: '查询编码',
    field: 'queryCode',
    width: 180,
  },
  {
    title: '查询名称',
    field: 'queryName',
    minWidth: 220,
  },
  {
    title: '科室',
    field: 'deptName',
    width: 160,
  },
  {
    title: '数据源ID',
    field: 'datasourceId',
    width: 110,
  },
  {
    title: '状态',
    field: 'status',
    width: 110,
    slots: {
      default: ({ row }) => {
        return renderDict(row.status, DictEnum.SYS_NORMAL_DISABLE);
      },
    },
  },
  {
    title: '备注',
    field: 'remark',
    minWidth: 180,
  },
  {
    title: '最后修改时间',
    field: 'updateTime',
    width: 180,
  },
  {
    field: 'action',
    fixed: 'right',
    slots: { default: 'action' },
    title: '操作',
    resizable: false,
    width: 'auto',
  },
];

export const itemDrawerSchema: FormSchemaGetter = () => [
  {
    component: 'Input',
    dependencies: {
      show: () => false,
      triggerFields: [''],
    },
    fieldName: 'id',
  },
  {
    component: 'TreeSelect',
    fieldName: 'parentId',
    label: '父节点',
  },
  {
    component: 'Input',
    fieldName: 'ledgerCode',
    label: '台账编码',
    rules: 'required',
  },
  {
    component: 'Input',
    fieldName: 'ledgerName',
    label: '台账名称',
    rules: 'required',
  },
  {
    component: 'Select',
    componentProps: {
      options: nodeTypeOptions,
    },
    fieldName: 'nodeType',
    label: '节点类型',
    rules: 'required',
  },
  {
    component: 'Select',
    componentProps: {
      options: [{ label: '无', value: 'NONE' }],
    },
    fieldName: 'queryCode',
    label: '查询编码',
  },
  {
    component: 'InputNumber',
    defaultValue: 1,
    fieldName: 'sortOrder',
    label: '排序',
  },
  {
    component: 'RadioGroup',
    componentProps: {
      buttonStyle: 'solid',
      options: getDictOptions(DictEnum.SYS_NORMAL_DISABLE),
      optionType: 'button',
    },
    defaultValue: '0',
    fieldName: 'status',
    label: '状态',
  },
  {
    component: 'Textarea',
    fieldName: 'remark',
    formItemClass: 'items-start',
    label: '备注',
  },
];

export const queryModalSchema: FormSchemaGetter = () => [
  {
    component: 'Input',
    dependencies: {
      show: () => false,
      triggerFields: [''],
    },
    fieldName: 'id',
  },
  {
    component: 'Input',
    dependencies: {
      show: () => false,
      triggerFields: [''],
    },
    fieldName: 'deptName',
  },
  {
    component: 'TreeSelect',
    fieldName: 'deptId',
    label: '科室',
  },
  {
    component: 'Input',
    fieldName: 'queryCode',
    label: '查询编码',
    rules: 'required',
  },
  {
    component: 'Input',
    fieldName: 'queryName',
    label: '查询名称',
    rules: 'required',
  },
  {
    component: 'Select',
    fieldName: 'datasourceId',
    label: '数据源',
    rules: 'required',
  },
  {
    component: 'Textarea',
    fieldName: 'countSql',
    formItemClass: 'col-span-2 items-start',
    label: '计数SQL',
    componentProps: {
      autoSize: { minRows: 4, maxRows: 10 },
      placeholder: '必须包含 :startTime 和 :endTime',
    },
    rules: 'required',
  },
  {
    component: 'Textarea',
    fieldName: 'detailSql',
    formItemClass: 'col-span-2 items-start',
    label: '明细SQL',
    componentProps: {
      autoSize: { minRows: 8, maxRows: 14 },
      placeholder: '必须包含 :startTime 和 :endTime，禁止 LIMIT',
    },
    rules: 'required',
  },
  {
    component: 'RadioGroup',
    componentProps: {
      buttonStyle: 'solid',
      options: getDictOptions(DictEnum.SYS_NORMAL_DISABLE),
      optionType: 'button',
    },
    defaultValue: '0',
    fieldName: 'status',
    label: '状态',
  },
  {
    component: 'Textarea',
    fieldName: 'remark',
    formItemClass: 'col-span-2 items-start',
    label: '备注',
    componentProps: {
      autoSize: { minRows: 2, maxRows: 4 },
    },
  },
  {
    component: 'Input',
    dependencies: {
      show: () => false,
      triggerFields: [''],
    },
    fieldName: 'detailFieldMapping',
  },
];
