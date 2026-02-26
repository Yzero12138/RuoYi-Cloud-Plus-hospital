import type { FormSchemaGetter } from '#/adapter/form';
import type { VxeGridProps } from '#/adapter/vxe-table';

import { getPopupContainer } from '@vben/utils';

const TEXT = {
  code: '台账编码',
  collapse: '折叠',
  createBy: '创建人',
  createTime: '创建时间',
  expand: '展开',
  name: '台账名称',
  nodeType: '节点类型',
  parentId: '父节点',
  queryTarget: '查询目标',
  remark: '备注',
  sortOrder: '排序',
  status: '状态',
  updateBy: '最后修改人',
  updateTime: '最后修改时间',
};

export const querySchema: FormSchemaGetter = () => [
  {
    component: 'Input',
    fieldName: 'ledgerCode',
    label: TEXT.code,
  },
  {
    component: 'Input',
    fieldName: 'ledgerName',
    label: TEXT.name,
  },
  {
    component: 'Select',
    componentProps: {
      options: [
        { label: '正常', value: 0 },
        { label: '停用', value: 1 },
      ],
    },
    fieldName: 'status',
    label: TEXT.status,
  },
];

export const columns: VxeGridProps['columns'] = [
  {
    title: TEXT.code,
    field: 'ledgerCode',
    width: 180,
  },
  {
    title: TEXT.name,
    field: 'ledgerName',
    treeNode: true,
    minWidth: 340,
  },
  {
    title: TEXT.nodeType,
    field: 'nodeType',
    width: 120,
  },
  {
    title: TEXT.queryTarget,
    field: 'queryTarget',
    minWidth: 280,
  },
  {
    title: TEXT.sortOrder,
    field: 'sortOrder',
    width: 100,
  },
  {
    title: TEXT.status,
    field: 'status',
    width: 100,
  },
  {
    title: TEXT.createBy,
    field: 'createBy',
    width: 120,
  },
  {
    title: TEXT.createTime,
    field: 'createTime',
    width: 180,
  },
  {
    title: TEXT.updateBy,
    field: 'updateBy',
    width: 120,
  },
  {
    title: TEXT.updateTime,
    field: 'updateTime',
    width: 180,
  },
  {
    title: TEXT.remark,
    field: 'remark',
    minWidth: 200,
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

export const pageText = TEXT;

export const drawerSchema: FormSchemaGetter = () => [
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
    componentProps: {
      getPopupContainer,
    },
    fieldName: 'parentId',
    label: TEXT.parentId,
  },
  {
    component: 'Input',
    fieldName: 'ledgerCode',
    label: TEXT.code,
    rules: 'required',
  },
  {
    component: 'Input',
    fieldName: 'ledgerName',
    label: TEXT.name,
    rules: 'required',
  },
  {
    component: 'Select',
    componentProps: {
      options: [
        { label: '指标', value: 'I' },
        { label: '分子', value: 'N' },
        { label: '分母', value: 'D' },
      ],
    },
    fieldName: 'nodeType',
    label: TEXT.nodeType,
    rules: 'required',
  },
  {
    component: 'Select',
    componentProps: {
      options: [
        { label: '无', value: 'NONE' },
      ],
    },
    fieldName: 'queryTarget',
    label: TEXT.queryTarget,
    rules: 'required',
  },
  {
    component: 'InputNumber',
    defaultValue: 1,
    fieldName: 'sortOrder',
    label: TEXT.sortOrder,
  },
  {
    component: 'RadioGroup',
    componentProps: {
      buttonStyle: 'solid',
      options: [
        { label: '正常', value: 0 },
        { label: '停用', value: 1 },
      ],
      optionType: 'button',
    },
    defaultValue: 0,
    fieldName: 'status',
    label: TEXT.status,
  },
  {
    component: 'Textarea',
    fieldName: 'remark',
    formItemClass: 'items-start',
    label: TEXT.remark,
  },
];

