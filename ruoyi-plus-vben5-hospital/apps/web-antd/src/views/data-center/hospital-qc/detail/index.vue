<script setup lang="ts">
import type { VbenFormProps } from '@vben/common-ui';
import type { VxeGridProps } from '#/adapter/vxe-table';

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import dayjs from 'dayjs';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { hospitalQcDashboardOptions } from '#/api/data-center/hospital-qc/dashboard';
import { hospitalQcDetailColumns, hospitalQcDetailPage } from '#/api/data-center/hospital-qc/detail';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const columns = ref<any[]>([]);
const options = ref<{
  deptOptions: { label: string; value: string }[];
  ledgerOptions: { label: string; value: string }[];
}>({
  deptOptions: [],
  ledgerOptions: [],
});

// 从路由参数初始化
const queryForm = ref({
  ledgerCode: '',
  deptId: undefined as number | undefined,
  quarter: '',
});

// 解析季度为时间范围（只显示日期，不显示时分秒）
const timeRange = computed(() => {
  const quarter = queryForm.value.quarter;
  if (!quarter || !quarter.match(/^\d{4}-Q[1-4]$/)) {
    const now = dayjs();
    return {
      startTime: now.startOf('quarter').format('YYYY-MM-DD'),
      endTime: now.endOf('quarter').format('YYYY-MM-DD'),
    };
  }
  const [year, q] = quarter.split('-Q');
  const startMonth = (parseInt(q) - 1) * 3;
  const start = dayjs(`${year}-01-01`).add(startMonth, 'month').startOf('month');
  const end = start.add(2, 'month').endOf('month');
  return {
    startTime: start.format('YYYY-MM-DD'),
    endTime: end.format('YYYY-MM-DD'),
  };
});

const formOptions: VbenFormProps = {
  commonConfig: {
    labelWidth: 84,
    componentProps: {
      allowClear: true,
    },
  },
  schema: [
    {
      component: 'Select',
      fieldName: 'ledgerCode',
      label: '台账',
      componentProps: {
        options: [],
        placeholder: '请选择台账',
        style: { width: '200px' },
      },
      rules: 'required',
    },
    {
      component: 'Select',
      fieldName: 'deptId',
      label: '科室',
      componentProps: {
        options: [],
        placeholder: '请选择科室',
        style: { width: '180px' },
        allowClear: true,
      },
    },
    {
      component: 'DatePicker',
      fieldName: 'startTime',
      label: '开始日期',
      componentProps: {
        format: 'YYYY-MM-DD',
        style: { width: '200px' },
      },
      rules: 'required',
    },
    {
      component: 'DatePicker',
      fieldName: 'endTime',
      label: '结束日期',
      componentProps: {
        format: 'YYYY-MM-DD',
        style: { width: '200px' },
      },
      rules: 'required',
    },
  ],
  wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-4',
};

const gridOptions: VxeGridProps = {
  columns: [],
  height: 'auto',
  keepSource: true,
  pagerConfig: {
    enabled: true,
    pageSize: 20,
    pageSizes: [10, 20, 50, 100],
  },
  proxyConfig: {
    ajax: {
      query: async ({ page }, formValues = {}) => {
        if (!formValues.ledgerCode) {
          return { rows: [], total: 0 };
        }
        loading.value = true;
        try {
          const res = await hospitalQcDetailPage({
            ledgerCode: formValues.ledgerCode,
            deptIds: formValues.deptId ? [formValues.deptId] : undefined,
            startTime: formValues.startTime,
            endTime: formValues.endTime,
            pageNum: page.currentPage,
            pageSize: page.pageSize,
          });
          return {
            rows: res.records ?? [],
            total: res.total ?? 0,
          };
        } finally {
          loading.value = false;
        }
      },
    },
  },
  id: 'data-center-hospital-qc-detail-index',
};

const [BasicTable, tableApi] = useVbenVxeGrid({
  formOptions,
  gridOptions,
});

async function setupOptions() {
  const data = await hospitalQcDashboardOptions();
  options.value = {
    deptOptions: (data.deptOptions ?? []).map((item) => ({
      label: item.label,
      value: String(item.value),
    })),
    ledgerOptions: (data.ledgerOptions ?? []).map((item) => ({
      label: item.label,
      value: item.value,
    })),
  };

  // 更新表单选项
  tableApi.formApi.updateSchema([
    {
      fieldName: 'ledgerCode',
      componentProps: {
        options: options.value.ledgerOptions,
      },
    },
    {
      fieldName: 'deptId',
      componentProps: {
        options: options.value.deptOptions.map((item) => ({
          label: item.label,
          value: Number(item.value),
        })),
      },
    },
  ]);
}

async function loadColumns() {
  if (!queryForm.value.ledgerCode) return;
  try {
    const cols = await hospitalQcDetailColumns(queryForm.value.ledgerCode);
    // 根据返回的列名构建表格列
    const tableColumns = cols.map((col) => ({
      title: col,
      field: col,
      minWidth: 120,
      showOverflow: 'tooltip',
    }));
    columns.value = tableColumns;
    tableApi.gridApi.setGridOptions({ columns: tableColumns });
  } catch {
    // 使用默认列
  }
}

async function initFromRoute() {
  const ledgerCode = String(route.query.ledgerCode ?? '');
  const deptId = route.query.deptId ? Number(route.query.deptId) : undefined;
  const quarter = String(route.query.quarter ?? '');

  queryForm.value = {
    ledgerCode,
    deptId,
    quarter,
  };

  // 设置表单初始值
  await tableApi.formApi.setValues({
    ledgerCode,
    deptId,
    ...timeRange.value,
  });

  if (ledgerCode) {
    await loadColumns();
    await tableApi.query();
  }
}

function goBack() {
  router.back();
}

onMounted(async () => {
  await setupOptions();
  await initFromRoute();
});
</script>

<template>
  <Page :auto-content-height="true">
    <BasicTable table-title="台账明细数据">
      <template #toolbar-tools>
        <a-button @click="goBack">返回</a-button>
      </template>
    </BasicTable>
  </Page>
</template>
