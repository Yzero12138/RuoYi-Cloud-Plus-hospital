import type { FormSchemaGetter } from '#/adapter/form';
import type { VxeGridProps } from '#/adapter/vxe-table';

import dayjs from 'dayjs';

const TEXT = {
  collapse: '折叠',
  count: '计数',
  denominator: '分母',
  dischargeTime: '出院时间',
  dischargeTimeDimension: '出院时间维度',
  expand: '展开',
  ledgerCode: '台账编码',
  ledgerName: '台账名称',
  month: '月份',
  monthSuffix: '月',
  numerator: '分子',
  placeholderTime: '请选择时间',
  quarter: '季度',
  quarterPrefix: '第',
  quarterSuffix: '季度',
  sourceType: '来源',
  year: '年份',
  yearSuffix: '年',
};

export const NODE_TYPE = {
  denominator: 'D',
  numerator: 'N',
};

const DISCHARGE_TIME_TYPE_OPTIONS = [
  { label: TEXT.year, value: 'year' },
  { label: TEXT.quarter, value: 'quarter' },
  { label: TEXT.month, value: 'month' },
];

function getDefaultDischargeTimeValue(type?: string) {
  if (type === 'month') {
    return dayjs().format('YYYY-MM');
  }
  if (type === 'quarter') {
    const year = dayjs().year();
    const month = dayjs().month() + 1;
    const quarter = Math.floor((month - 1) / 3) + 1;
    return `${year}-Q${quarter}`;
  }
  return `${dayjs().year()}`;
}

function buildYearOptions() {
  const currentYear = dayjs().year();
  return Array.from({ length: 10 }).map((_, index) => {
    const year = currentYear - index;
    return { label: `${year}${TEXT.yearSuffix}`, value: `${year}` };
  });
}

function buildQuarterOptions() {
  const currentYear = dayjs().year();
  const options: Array<{ label: string; value: string }> = [];
  for (let year = currentYear; year >= currentYear - 2; year--) {
    for (let quarter = 1; quarter <= 4; quarter++) {
      options.push({
        label: `${year}${TEXT.yearSuffix}${TEXT.quarterPrefix}${quarter}${TEXT.quarterSuffix}`,
        value: `${year}-Q${quarter}`,
      });
    }
  }
  return options;
}

function buildMonthOptions() {
  return Array.from({ length: 24 }).map((_, index) => {
    const month = dayjs().subtract(index, 'month');
    return {
      label: `${month.format('YYYY-MM')}${TEXT.monthSuffix}`,
      value: month.format('YYYY-MM'),
    };
  });
}

function getDischargeTimeValueOptions(type?: string) {
  if (type === 'quarter') {
    return buildQuarterOptions();
  }
  if (type === 'month') {
    return buildMonthOptions();
  }
  return buildYearOptions();
}

export const defaultDischargeTimeType = 'year';
export const defaultDischargeTimeValue = `${dayjs().year()}`;

export const querySchema: FormSchemaGetter = () => [
  {
    component: 'Select',
    componentProps: (model) => ({
      allowClear: false,
      options: DISCHARGE_TIME_TYPE_OPTIONS,
      onChange: (value: string) => {
        model.dischargeTimeValue = getDefaultDischargeTimeValue(value);
      },
    }),
    defaultValue: defaultDischargeTimeType,
    fieldName: 'dischargeTimeType',
    label: TEXT.dischargeTimeDimension,
  },
  {
    component: 'Select',
    componentProps: (model) => ({
      allowClear: false,
      options: getDischargeTimeValueOptions(model.dischargeTimeType),
      placeholder: TEXT.placeholderTime,
    }),
    defaultValue: defaultDischargeTimeValue,
    dependencies: {
      triggerFields: ['dischargeTimeType'],
    },
    fieldName: 'dischargeTimeValue',
    label: TEXT.dischargeTime,
    rules: 'required',
  },
];

export const columns: VxeGridProps['columns'] = [
  {
    title: TEXT.ledgerCode,
    field: 'ledgerCode',
    width: 180,
  },
  {
    title: TEXT.ledgerName,
    field: 'ledgerName',
    treeNode: true,
    minWidth: 340,
  },
  {
    title: TEXT.count,
    field: 'countValue',
    slots: { default: 'countCell' },
    minWidth: 260,
  },
  {
    title: TEXT.sourceType,
    field: 'queryTarget',
    minWidth: 280,
  },
];

export const pageText = TEXT;
