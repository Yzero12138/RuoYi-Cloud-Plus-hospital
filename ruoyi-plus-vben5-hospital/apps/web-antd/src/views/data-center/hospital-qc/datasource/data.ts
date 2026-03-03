import type { FormSchemaGetter } from '#/adapter/form';
import type { VxeGridProps } from '#/adapter/vxe-table';

import { DictEnum } from '@vben/constants';

import { getDictOptions } from '#/utils/dict';
import { renderDict } from '#/utils/render';

const TEXT = {
  databaseName: '库名',
  host: '主机',
  instanceName: '实例名',
  password: '密码',
  port: '端口',
  remark: '备注',
  sourceName: '数据源名称',
  sourceType: '类型',
  status: '状态',
  username: '用户名',
};

export const sourceTypeOptions = [
  { label: 'MYSQL', value: 'MYSQL' },
  { label: 'ORACLE', value: 'ORACLE' },
  { label: 'SQLSERVER', value: 'SQLSERVER' },
];

export const querySchema: FormSchemaGetter = () => [
  {
    component: 'Input',
    fieldName: 'sourceName',
    label: TEXT.sourceName,
  },
  {
    component: 'Select',
    componentProps: {
      options: sourceTypeOptions,
    },
    fieldName: 'sourceType',
    label: TEXT.sourceType,
  },
  {
    component: 'Select',
    componentProps: {
      options: getDictOptions(DictEnum.SYS_NORMAL_DISABLE),
    },
    fieldName: 'status',
    label: TEXT.status,
  },
];

export const columns: VxeGridProps['columns'] = [
  {
    title: TEXT.sourceName,
    field: 'sourceName',
    minWidth: 180,
  },
  {
    title: TEXT.sourceType,
    field: 'sourceType',
    width: 120,
  },
  {
    title: TEXT.host,
    field: 'host',
    width: 160,
  },
  {
    title: TEXT.port,
    field: 'port',
    width: 100,
  },
  {
    title: TEXT.databaseName,
    field: 'databaseName',
    minWidth: 140,
  },
  {
    title: TEXT.instanceName,
    field: 'instanceName',
    minWidth: 120,
  },
  {
    title: TEXT.username,
    field: 'username',
    width: 140,
  },
  {
    title: TEXT.password,
    field: 'passwordMasked',
    width: 110,
  },
  {
    title: TEXT.status,
    field: 'status',
    width: 110,
    slots: {
      default: ({ row }) => {
        return renderDict(row.status, DictEnum.SYS_NORMAL_DISABLE);
      },
    },
  },
  {
    title: '最后修改时间',
    field: 'updateTime',
    width: 180,
  },
  {
    title: TEXT.remark,
    field: 'remark',
    minWidth: 180,
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
    component: 'Input',
    fieldName: 'sourceName',
    label: TEXT.sourceName,
    rules: 'required',
  },
  {
    component: 'Select',
    componentProps: {
      options: sourceTypeOptions,
    },
    fieldName: 'sourceType',
    label: TEXT.sourceType,
    rules: 'required',
  },
  {
    component: 'Input',
    fieldName: 'host',
    label: TEXT.host,
    rules: 'required',
  },
  {
    component: 'InputNumber',
    fieldName: 'port',
    label: TEXT.port,
    rules: 'required',
  },
  {
    component: 'Input',
    fieldName: 'databaseName',
    label: TEXT.databaseName,
  },
  {
    component: 'Input',
    fieldName: 'instanceName',
    label: TEXT.instanceName,
  },
  {
    component: 'Input',
    fieldName: 'username',
    label: TEXT.username,
    rules: 'required',
  },
  {
    component: 'InputPassword',
    fieldName: 'password',
    label: TEXT.password,
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
    label: TEXT.status,
  },
  {
    component: 'Textarea',
    fieldName: 'remark',
    formItemClass: 'items-start',
    label: TEXT.remark,
  },
];
