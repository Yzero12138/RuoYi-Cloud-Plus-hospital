<script setup lang="ts">
import type { VbenFormProps } from '@vben/common-ui';

import type { VxeGridProps } from '#/adapter/vxe-table';
import type { HospitalQcReportRow } from '#/api/data-center/hospital-qc/report/model';

import { nextTick, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';

import dayjs from 'dayjs';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { hospitalQcDashboardOptions } from '#/api/data-center/hospital-qc/dashboard';
import { hospitalQcReportSummary } from '#/api/data-center/hospital-qc/report';

import { reportColumns, reportQuerySchema } from './data';

const route = useRoute();
const router = useRouter();

type HospitalQcReportRowWithKey = HospitalQcReportRow & { rowKey: string };
const YEAR_VALUE_PATTERN = /^\d{4}$/;
const QUARTER_VALUE_PATTERN = /^\d{4}-Q[1-4]$/;
const MONTH_VALUE_PATTERN = /^\d{4}-(0[1-9]|1[0-2])$/;

// 定义包含操作列的列配置
const gridColumns: VxeGridProps['columns'] = [
  ...(reportColumns || []),
  {
    title: '操作',
    field: 'action',
    width: 100,
    fixed: 'right',
    slots: { default: 'action' },
  },
];

const formOptions: VbenFormProps = {
  commonConfig: {
    labelWidth: 84,
    componentProps: {
      allowClear: true,
    },
  },
  schema: reportQuerySchema(),
  wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5',
};

const gridOptions: VxeGridProps = {
  columns: gridColumns,
  height: 'auto',
  keepSource: true,
  pagerConfig: {
    enabled: false,
  },
  proxyConfig: {
    ajax: {
      query: async (_, formValues = {}) => {
        const params: any = {
          timeType: formValues.timeType,
          timeValue: formValues.timeValue,
          deptIds: formValues.deptIds,
          ledgerCodes: formValues.ledgerCodes,
        };
        if (formValues.timeType === 'custom' && Array.isArray(formValues.customRange)) {
          const [start, end] = formValues.customRange;
          params.startTime = start
            ? dayjs(start).startOf('day').format('YYYY-MM-DD HH:mm:ss')
            : undefined;
          params.endTime = end
            ? dayjs(end).endOf('day').format('YYYY-MM-DD HH:mm:ss')
            : undefined;
        }
        const rows = await hospitalQcReportSummary(params);
        const list: HospitalQcReportRowWithKey[] = (rows ?? []).map((row, index) => {
          return {
            ...row,
            rowKey: `${row.periodLabel ?? ''}-${row.deptId ?? 'ALL'}-${row.ledgerCode ?? ''}-${index}`,
          };
        });
        return { rows: list };
      },
    },
  },
  rowConfig: {
    keyField: 'rowKey',
  },
  id: 'data-center-hospital-qc-report-index',
};

const [BasicTable, tableApi] = useVbenVxeGrid({
  formOptions,
  gridOptions,
});

async function setupOptions() {
  const options = await hospitalQcDashboardOptions();
  tableApi.formApi.updateSchema([
    {
      componentProps: {
        options: (options.deptOptions ?? []).map((item) => ({
          label: item.label,
          value: Number(item.value),
        })),
      },
      fieldName: 'deptIds',
    },
    {
      componentProps: {
        options: (options.ledgerOptions ?? []).map((item) => ({
          label: item.label,
          value: item.value,
        })),
      },
      fieldName: 'ledgerCodes',
    },
  ]);
}

async function initFromRoute() {
  const currentYear = new Date().getFullYear();
  const rawTimeType = String(route.query.timeType ?? 'quarter');
  const timeType =
    rawTimeType === 'year' || rawTimeType === 'month' || rawTimeType === 'custom' || rawTimeType === 'quarter'
      ? rawTimeType
      : 'quarter';
  const rawTimeValue = String(route.query.timeValue ?? '');
  const currentQuarter = Math.ceil((dayjs().month() + 1) / 3);
  let timeValue = `${currentYear}-Q${currentQuarter}`;
  if (timeType === 'year') {
    timeValue = YEAR_VALUE_PATTERN.test(rawTimeValue)
      ? rawTimeValue
      : String(currentYear);
  } else if (timeType === 'month') {
    timeValue = MONTH_VALUE_PATTERN.test(rawTimeValue)
      ? rawTimeValue
      : dayjs().format('YYYY-MM');
  } else if (timeType === 'quarter') {
    timeValue = QUARTER_VALUE_PATTERN.test(rawTimeValue) ? rawTimeValue : `${currentYear}-Q${currentQuarter}`;
  }
  const deptIds = String(route.query.deptIds ?? '')
    .split(',')
    .filter(Boolean)
    .map((id) => Number(id));
  const ledgerCodes = String(route.query.ledgerCodes ?? '')
    .split(',')
    .filter(Boolean);

  await tableApi.formApi.setValues({
    timeType,
  });
  await nextTick();

  await tableApi.formApi.setValues({
    timeValue,
    deptIds,
    ledgerCodes,
  });
  await nextTick();
}

// 跳转到明细页面
function goDetail(row: HospitalQcReportRow, nodeType?: string) {
  router.push({
    path: '/data-center/hospital-qc/detail',
    query: {
      ledgerCode: row.ledgerCode,
      deptId: row.deptId,
      quarter: row.periodLabel,
      ...(nodeType ? { nodeType } : {}),
    },
  });
}

onMounted(async () => {
  await setupOptions();
  await initFromRoute();
  await tableApi.query();
});
</script>

<template>
  <Page :auto-content-height="true">
    <BasicTable table-title="台账详细报表">
      <template #numerator="{ row }">
        <a-button type="link" size="small" @click="goDetail(row, 'N')">
          {{ row.numerator ?? 0 }}
        </a-button>
      </template>
      <template #denominator="{ row }">
        <a-button
          v-if="!row.noDenominator"
          type="link"
          size="small"
          @click="goDetail(row, 'D')"
        >
          {{ row.denominator ?? 0 }}
        </a-button>
        <span v-else>--</span>
      </template>
      <template #action="{ row }">
        <a-button type="link" size="small" @click="goDetail(row)">
          查看明细
        </a-button>
      </template>
    </BasicTable>
  </Page>
</template>

<style scoped lang="scss">
:deep(.metric-highlight) {
  font-weight: 600;
  color: #0f766e;
}
</style>
