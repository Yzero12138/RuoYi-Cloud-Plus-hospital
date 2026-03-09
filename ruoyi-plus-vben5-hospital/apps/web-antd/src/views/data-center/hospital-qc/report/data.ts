import type { FormSchemaGetter } from '#/adapter/form';
import type { VxeGridProps } from '#/adapter/vxe-table';

import dayjs from 'dayjs';

const QUARTER_VALUE_PATTERN = /^\d{4}-Q[1-4]$/;
const MONTH_VALUE_PATTERN = /^\d{4}-(0[1-9]|1[0-2])$/;

export function quarterOptions() {
  const now = dayjs();
  const currentYear = now.year();
  const currentQuarter = Math.ceil((now.month() + 1) / 3);
  const options: Array<{ label: string; value: string }> = [];

  let year = currentYear;
  let quarter = currentQuarter;
  for (let i = 0; i < 12; i++) {
    options.push({
      label: `${year}年Q${quarter}`,
      value: `${year}-Q${quarter}`,
    });
    quarter--;
    if (quarter < 1) {
      quarter = 4;
      year--;
    }
  }
  return options;
}

function monthOptions() {
  return Array.from({ length: 24 }).map((_, index) => {
    const month = dayjs().subtract(index, 'month');
    return {
      label: month.format('YYYY-MM'),
      value: month.format('YYYY-MM'),
    };
  });
}

function timeValueOptions(type?: string) {
  if (type === 'month') {
    return monthOptions();
  }
  return quarterOptions();
}

export const reportQuerySchema: FormSchemaGetter = () => [
  {
    component: 'Select',
    componentProps: (model) => ({
      allowClear: false,
      options: [
        { label: '季度', value: 'quarter' },
        { label: '月份', value: 'month' },
        { label: '自定义', value: 'custom' },
      ],
      onChange: (value: string) => {
        if (value === 'custom') {
          return;
        }
        const currentTimeValue = String(model.timeValue ?? '');
        if (value === 'month') {
          if (!MONTH_VALUE_PATTERN.test(currentTimeValue)) {
            model.timeValue = dayjs().format('YYYY-MM');
          }
          return;
        }
        if (!QUARTER_VALUE_PATTERN.test(currentTimeValue)) {
          const now = dayjs();
          model.timeValue = `${now.year()}-Q${Math.ceil((now.month() + 1) / 3)}`;
        }
      },
    }),
    defaultValue: 'quarter',
    fieldName: 'timeType',
    label: '时间维度',
  },
  {
    component: 'Select',
    componentProps: (model) => ({
      allowClear: false,
      options: timeValueOptions(model.timeType),
    }),
    defaultValue: (() => {
      const now = dayjs();
      return `${now.year()}-Q${Math.ceil((now.month() + 1) / 3)}`;
    })(),
    dependencies: {
      show: (values) => values.timeType !== 'custom',
      triggerFields: ['timeType'],
    },
    fieldName: 'timeValue',
    label: '时间值',
  },
  {
    component: 'RangePicker',
    dependencies: {
      show: (values) => values.timeType === 'custom',
      triggerFields: ['timeType'],
    },
    fieldName: 'customRange',
    label: '时间范围',
  },
  {
    component: 'Select',
    componentProps: {
      mode: 'multiple',
      options: [],
      placeholder: '全部科室',
    },
    fieldName: 'deptIds',
    label: '科室',
  },
  {
    component: 'Select',
    componentProps: {
      mode: 'multiple',
      options: [],
      placeholder: '全部台账',
    },
    fieldName: 'ledgerCodes',
    label: '台账',
  },
];

export const reportColumns: VxeGridProps['columns'] = [
  { type: 'seq', width: 60 },
  { title: '周期', field: 'periodLabel', width: 120 },
  { title: '科室', field: 'deptName', width: 140 },
  { title: '台账编码', field: 'ledgerCode', width: 140 },
  { title: '台账名称', field: 'ledgerName', minWidth: 220 },
  { title: '分子', field: 'numerator', width: 100, slots: { default: 'numerator' } },
  { title: '分母', field: 'denominator', width: 100, slots: { default: 'denominator' } },
  { title: '百分比', field: 'percentDisplay', width: 110, className: 'metric-highlight' },
  { title: '环比增长', field: 'growthDisplay', width: 120, className: 'metric-highlight' },
  { title: '百分点变化', field: 'pointChangeDisplay', width: 120, className: 'metric-highlight' },
  {
    title: '无分母',
    field: 'noDenominator',
    width: 90,
    formatter: ({ cellValue }) => (cellValue ? '是' : '否'),
  },
];
