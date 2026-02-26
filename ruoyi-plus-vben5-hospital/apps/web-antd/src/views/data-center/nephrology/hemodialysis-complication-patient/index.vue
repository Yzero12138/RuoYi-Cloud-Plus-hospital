<script setup lang="ts">
import type { VbenFormProps } from '@vben/common-ui';

import type { VxeGridProps } from '#/adapter/vxe-table';

import { Page } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { hemodialysisComplicationPatientList } from '#/api/data-center/nephrology/hemodialysis-complication-patient';

import { columns, querySchema } from './data';

const formOptions: VbenFormProps = {
  commonConfig: {
    labelWidth: 100,
    componentProps: {
      allowClear: true,
    },
  },
  schema: querySchema(),
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
        return await hemodialysisComplicationPatientList({
          pageNum: page.currentPage,
          pageSize: page.pageSize,
          ...formValues,
        });
      },
    },
  },
  rowConfig: {
    keyField: 'id',
  },
  id: 'data-center-hemodialysis-complication-patient-index',
};

const [BasicTable] = useVbenVxeGrid({
  formOptions,
  gridOptions,
});

const tableTitle =
  '\u5206\u5b50\uff1a132\u8840\u6db2\u900f\u6790\u6240\u81f4\u5e76\u53d1\u75c7\u53d1\u751f\u4f8b\u6570';
</script>

<template>
  <Page :auto-content-height="true">
    <BasicTable :table-title="tableTitle" />
  </Page>
</template>
