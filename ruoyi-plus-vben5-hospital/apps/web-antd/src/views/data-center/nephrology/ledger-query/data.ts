import type { FormSchemaGetter } from '#/adapter/form';
import type { VxeGridProps } from '#/adapter/vxe-table';

import { DictEnum } from '@vben/constants';
import { getPopupContainer } from '@vben/utils';

import { getDictOptions } from '#/utils/dict';
import { renderDict } from '#/utils/render';

const TEXT = {
  code: '查询编码',
  name: '查询名称',
  dept: '科室',
  countSql: '计数SQL',
  detailSql: '明细SQL',
  status: '状态',
  remark: '备注',
};

export const querySchema: FormSchemaGetter = () => [
  {
    component: 'Input',
    fieldName: 'queryCode',
    label: TEXT.code,
  },
  {
    component: 'Input',
    fieldName: 'queryName',
    label: TEXT.name,
  },
  {
    component: 'Input',
    fieldName: 'deptName',
    label: TEXT.dept,
  },
  {
    component: 'Select',
    componentProps: {
      getPopupContainer,
      options: getDictOptions(DictEnum.SYS_NORMAL_DISABLE),
    },
    fieldName: 'status',
    label: TEXT.status,
  },
];

export const columns: VxeGridProps['columns'] = [
  { type: 'checkbox', width: 60 },
  {
    title: TEXT.code,
    field: 'queryCode',
    width: 180,
  },
  {
    title: TEXT.name,
    field: 'queryName',
    minWidth: 220,
  },
  {
    title: TEXT.dept,
    field: 'deptName',
    width: 160,
  },
  {
    title: TEXT.status,
    field: 'status',
    width: 120,
    slots: {
      default: ({ row }) => {
        return renderDict(row.status, DictEnum.SYS_NORMAL_DISABLE);
      },
    },
  },
  {
    title: TEXT.remark,
    field: 'remark',
    minWidth: 200,
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

export const modalSchema: FormSchemaGetter = () => [
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
    componentProps: {
      getPopupContainer,
    },
    fieldName: 'deptId',
    label: TEXT.dept,
  },
  {
    component: 'Input',
    fieldName: 'queryCode',
    label: TEXT.code,
    rules: 'required',
  },
  {
    component: 'Input',
    fieldName: 'queryName',
    label: TEXT.name,
    rules: 'required',
  },
  {
    component: 'Textarea',
    fieldName: 'countSql',
    formItemClass: 'col-span-2 items-start',
    label: TEXT.countSql,
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
    label: TEXT.detailSql,
    componentProps: {
      autoSize: { minRows: 6, maxRows: 12 },
      placeholder: '必须包含 :startTime 和 :endTime（无需写 LIMIT）',
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
    label: TEXT.status,
  },
  {
    component: 'Textarea',
    fieldName: 'remark',
    formItemClass: 'col-span-2 items-start',
    label: TEXT.remark,
    componentProps: {
      autoSize: { minRows: 2, maxRows: 4 },
    },
  },
];

