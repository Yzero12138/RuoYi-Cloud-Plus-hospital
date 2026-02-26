<script setup lang="ts">
import type { VbenFormProps } from '@vben/common-ui';

import type { VxeGridProps } from '#/adapter/vxe-table';

import dayjs from 'dayjs';
import { computed } from 'vue';
import { useRoute } from 'vue-router';

import { Page } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { nephrologyLedgerDetailList } from '#/api/data-center/nephrology/ledger';

import { columns, querySchema } from './data';

const route = useRoute();

const ledgerCode = computed(() => String(route.query.ledgerCode ?? ''));
const ledgerName = computed(() => String(route.query.ledgerName ?? '台账详情'));
const initDischargeTimeType = computed(
  () => String(route.query.dischargeTimeType ?? 'year'),
);
const initDischargeTimeValue = computed(
  () => String(route.query.dischargeTimeValue ?? `${dayjs().year()}`),
);

const formOptions: VbenFormProps = {
  commonConfig: {
    labelWidth: 100,
    componentProps: {
      allowClear: true,
    },
  },
  schema: querySchema(
    initDischargeTimeType.value,
    initDischargeTimeValue.value,
  )(),
  wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4',
};

const gridOptions: VxeGridProps = {
  columns,
  height: 'auto',
  keepSource: true,
  pagerConfig: {},
  proxyConfig: {
    ajax: {
      query: async ({ page }, formValues = {}) => {
        return await nephrologyLedgerDetailList({
          pageNum: page.currentPage,
          pageSize: page.pageSize,
          ledgerCode: ledgerCode.value,
          dischargeTimeType: initDischargeTimeType.value,
          dischargeTimeValue: initDischargeTimeValue.value,
          ...formValues,
        });
      },
    },
  },
  rowConfig: {
    keyField: 'id',
  },
  id: 'data-center-nephrology-ledger-detail-index',
};

const [BasicTable] = useVbenVxeGrid({
  formOptions,
  gridOptions,
});

const tableTitle = computed(() => `${ledgerName.value} 详情`);
</script>

<template>
  <Page :auto-content-height="true">
    <BasicTable :table-title="tableTitle" />
  </Page>
</template>
