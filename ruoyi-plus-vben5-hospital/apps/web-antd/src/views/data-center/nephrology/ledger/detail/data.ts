import type { FormSchemaGetter } from '#/adapter/form';
import type { VxeGridProps } from '#/adapter/vxe-table';

import dayjs from 'dayjs';

const TEXT = {
  diagnosisCode: '诊断编码',
  diagnosisName: '诊断名称',
  dischargeDepartment: '出院科室',
  dischargeTime: '出院时间',
  dischargeTimeDimension: '出院时间维度',
  idCardNo: '身份证号',
  medicalRecordNo: '病案号',
  month: '月份',
  monthSuffix: '月',
  patientName: '姓名',
  placeholderTime: '请选择时间',
  quarter: '季度',
  quarterPrefix: '第',
  quarterSuffix: '季度',
  sourceType: '来源',
  year: '年份',
  yearSuffix: '年',
};

const DISCHARGE_TIME_TYPE_OPTIONS = [
  { label: TEXT.year, value: 'year' },
  { label: TEXT.quarter, value: 'quarter' },
  { label: TEXT.month, value: 'month' },
];

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

export const querySchema = (
  dischargeTimeType: string,
  dischargeTimeValue: string,
): FormSchemaGetter => {
  return () => [
    {
      component: 'Input',
      fieldName: 'medicalRecordNo',
      label: TEXT.medicalRecordNo,
    },
    {
      component: 'Input',
      fieldName: 'idCardNo',
      label: TEXT.idCardNo,
    },
    {
      component: 'Select',
      componentProps: (model) => ({
        allowClear: false,
        options: DISCHARGE_TIME_TYPE_OPTIONS,
        onChange: () => {
          model.dischargeTimeValue = undefined;
        },
      }),
      defaultValue: dischargeTimeType,
      fieldName: 'dischargeTimeType',
      label: TEXT.dischargeTimeDimension,
    },
    {
      component: 'Select',
      componentProps: (model) => ({
        options: getDischargeTimeValueOptions(model.dischargeTimeType),
        placeholder: TEXT.placeholderTime,
      }),
      defaultValue: dischargeTimeValue,
      dependencies: {
        triggerFields: ['dischargeTimeType'],
      },
      fieldName: 'dischargeTimeValue',
      label: TEXT.dischargeTime,
    },
  ];
};

export const columns: VxeGridProps['columns'] = [
  { type: 'seq', width: 60 },
  { title: TEXT.medicalRecordNo, field: 'medicalRecordNo', width: 180 },
  { title: TEXT.patientName, field: 'patientName', width: 140 },
  { title: TEXT.idCardNo, field: 'idCardNo', width: 200 },
  { title: TEXT.dischargeDepartment, field: 'dischargeDepartment', width: 180 },
  { title: TEXT.dischargeTime, field: 'dischargeTime', width: 180 },
  { title: TEXT.diagnosisName, field: 'diagnosisName', minWidth: 260 },
  { title: TEXT.diagnosisCode, field: 'diagnosisCode', width: 180 },
  { title: TEXT.sourceType, field: 'sourceType', width: 160 },
];
