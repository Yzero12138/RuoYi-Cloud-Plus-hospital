import type { FormSchemaGetter } from '#/adapter/form';
import type { VxeGridProps } from '#/adapter/vxe-table';

import dayjs from 'dayjs';

const TEXT = {
  age: '\u5e74\u9f84',
  admissionCondition: '\u5165\u9662\u75c5\u60c5',
  admissionDepartment: '\u5165\u9662\u79d1\u5ba4',
  admissionTime: '\u5165\u9662\u65f6\u95f4',
  birthDate: '\u51fa\u751f\u65e5\u671f',
  complicationCode: '\u5e76\u53d1\u75c7\u8bca\u65ad\u4ee3\u7801',
  complicationDiagnosis: '\u5e76\u53d1\u75c7\u8bca\u65ad',
  dischargeDepartment: '\u51fa\u9662\u79d1\u5ba4',
  dischargeTime: '\u51fa\u9662\u65f6\u95f4',
  dischargeTimeDimension: '\u51fa\u9662\u65f6\u95f4\u7ef4\u5ea6',
  gender: '\u6027\u522b',
  idCardNo: '\u8eab\u4efd\u8bc1\u53f7',
  medicalRecordNo: '\u75c5\u6848\u53f7',
  month: '\u6708\u4efd',
  monthSuffix: '\u6708',
  patientName: '\u59d3\u540d',
  placeholderTime: '\u8bf7\u9009\u62e9\u65f6\u95f4',
  quarter: '\u5b63\u5ea6',
  quarterPrefix: '\u7b2c',
  quarterSuffix: '\u5b63\u5ea6',
  updateTime: '\u66f4\u65b0\u65f6\u95f4',
  year: '\u5e74\u4efd',
  yearSuffix: '\u5e74',
  createTime: '\u521b\u5efa\u65f6\u95f4',
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

export const querySchema: FormSchemaGetter = () => [
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
    defaultValue: 'year',
    fieldName: 'dischargeTimeType',
    label: TEXT.dischargeTimeDimension,
  },
  {
    component: 'Select',
    componentProps: (model) => ({
      options: getDischargeTimeValueOptions(model.dischargeTimeType),
      placeholder: TEXT.placeholderTime,
    }),
    dependencies: {
      triggerFields: ['dischargeTimeType'],
    },
    fieldName: 'dischargeTimeValue',
    label: TEXT.dischargeTime,
  },
];

export const columns: VxeGridProps['columns'] = [
  { type: 'seq', width: 60 },
  { title: TEXT.medicalRecordNo, field: 'medicalRecordNo', width: 160 },
  { title: TEXT.patientName, field: 'patientName', width: 120 },
  { title: TEXT.gender, field: 'gender', width: 80 },
  { title: TEXT.birthDate, field: 'birthDate', width: 140 },
  { title: TEXT.age, field: 'age', width: 80 },
  { title: TEXT.idCardNo, field: 'idCardNo', width: 180 },
  { title: TEXT.admissionDepartment, field: 'admissionDepartment', width: 160 },
  { title: TEXT.dischargeDepartment, field: 'dischargeDepartment', width: 160 },
  { title: TEXT.complicationDiagnosis, field: 'complicationDiagnosis', width: 260 },
  { title: TEXT.complicationCode, field: 'complicationCode', width: 180 },
  { title: TEXT.admissionCondition, field: 'admissionCondition', width: 120 },
  { title: TEXT.admissionTime, field: 'admissionTime', width: 160 },
  { title: TEXT.dischargeTime, field: 'dischargeTime', width: 160 },
  { title: TEXT.createTime, field: 'createTime', width: 160 },
  { title: TEXT.updateTime, field: 'updateTime', width: 160 },
];
