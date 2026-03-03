<script setup lang="ts">
import type { EchartsUIType } from '@vben/plugins/echarts';

import type {
  HospitalQcDashboardOverview,
  HospitalQcOptionItem,
  HospitalQcTrendSeries,
} from '#/api/data-center/hospital-qc/dashboard/model';

import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import {
  hospitalQcDashboardOptions,
  hospitalQcDashboardOverview,
} from '#/api/data-center/hospital-qc/dashboard';

import { buildYearOptions, trendColor, trendText } from './data';

const router = useRouter();

const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

const loading = ref(false);
const options = ref<{ deptOptions: HospitalQcOptionItem[]; ledgerOptions: HospitalQcOptionItem[] }>({
  deptOptions: [],
  ledgerOptions: [],
});

const queryForm = ref({
  year: new Date().getFullYear(),
  deptIds: [] as number[],
  ledgerCodes: [] as string[],
});

const overview = ref<HospitalQcDashboardOverview>();
const selectedTrendCode = ref<string>();

const trendOptions = computed(() => {
  return (overview.value?.trends ?? []).map((item) => ({
    label: `${item.ledgerName ?? ''} (${item.ledgerCode ?? ''})`,
    value: item.ledgerCode ?? '',
  }));
});

const currentTrend = computed<HospitalQcTrendSeries | undefined>(() => {
  const trends = overview.value?.trends ?? [];
  if (!trends.length) {
    return undefined;
  }
  const target = trends.find((item) => item.ledgerCode === selectedTrendCode.value);
  return target ?? trends[0];
});

const rankingColumns = [
  { title: '科室', dataIndex: 'deptName', key: 'deptName', width: 160 },
  { title: '分子', dataIndex: 'numerator', key: 'numerator', width: 100 },
  { title: '分母', dataIndex: 'denominator', key: 'denominator', width: 100 },
  { title: '百分比', dataIndex: 'percentDisplay', key: 'percentDisplay', width: 100 },
  { title: '环比增长', dataIndex: 'growthDisplay', key: 'growthDisplay', width: 110 },
  { title: '百分点变化', dataIndex: 'pointChangeDisplay', key: 'pointChangeDisplay', width: 120 },
  { title: '趋势', dataIndex: 'trend', key: 'trend', width: 90 },
];

async function loadOptions() {
  const data = await hospitalQcDashboardOptions();
  options.value = {
    deptOptions: data.deptOptions ?? [],
    ledgerOptions: data.ledgerOptions ?? [],
  };
}

async function loadOverview() {
  loading.value = true;
  try {
    const data = await hospitalQcDashboardOverview(queryForm.value);
    overview.value = data;
    const firstCode = data.trends?.[0]?.ledgerCode;
    if (firstCode) {
      selectedTrendCode.value = firstCode;
    }
    await nextTick();
    renderTrendChart();
  } finally {
    loading.value = false;
  }
}

function renderTrendChart() {
  const trend = currentTrend.value;
  if (!trend) {
    renderEcharts({
      title: { text: '暂无趋势数据' },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: [],
    });
    return;
  }
  const metrics = trend.quarterMetrics ?? [];
  renderEcharts({
    color: ['#0ea5e9', '#f59e0b', '#10b981'],
    grid: {
      bottom: 28,
      containLabel: true,
      left: 14,
      right: 14,
      top: 20,
    },
    legend: {
      data: ['分子', '分母', '百分比'],
    },
    tooltip: {
      trigger: 'axis',
    },
    xAxis: {
      type: 'category',
      data: metrics.map((item) => item.quarter),
    },
    yAxis: [
      {
        type: 'value',
        name: '数量',
      },
      {
        type: 'value',
        name: '%',
      },
    ],
    series: [
      {
        name: '分子',
        data: metrics.map((item) => item.numerator ?? 0),
        type: 'bar',
      },
      {
        name: '分母',
        data: metrics.map((item) => item.denominator ?? 0),
        type: 'bar',
      },
      {
        name: '百分比',
        data: metrics.map((item) => item.indicatorPercent ?? null),
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
      },
    ],
  });
}

function handleReset() {
  queryForm.value = {
    year: new Date().getFullYear(),
    deptIds: [],
    ledgerCodes: [],
  };
  loadOverview();
}

function goReport() {
  const quarters = overview.value?.quarters ?? [];
  const targetQuarter = quarters.length
    ? quarters[quarters.length - 1]
    : `${queryForm.value.year}-Q4`;
  router.push({
    path: '/data-center/hospital-qc/report',
    query: {
      timeType: 'quarter',
      timeValue: targetQuarter,
      deptIds: queryForm.value.deptIds.join(','),
      ledgerCodes: queryForm.value.ledgerCodes.join(','),
    },
  });
}

watch(currentTrend, () => {
  renderTrendChart();
});

onMounted(async () => {
  await loadOptions();
  await loadOverview();
});
</script>

<template>
  <Page :auto-content-height="true">
    <a-card :bordered="false" class="mb-3">
      <a-form layout="inline">
        <a-form-item label="年份">
          <a-select
            v-model:value="queryForm.year"
            :options="buildYearOptions()"
            style="width: 120px"
          />
        </a-form-item>
        <a-form-item label="科室">
          <a-select
            v-model:value="queryForm.deptIds"
            mode="multiple"
            :options="options.deptOptions.map((item) => ({ label: item.label, value: Number(item.value) }))"
            style="min-width: 220px"
            placeholder="全部科室"
          />
        </a-form-item>
        <a-form-item label="台账">
          <a-select
            v-model:value="queryForm.ledgerCodes"
            mode="multiple"
            :options="options.ledgerOptions.map((item) => ({ label: item.label, value: item.value }))"
            style="min-width: 260px"
            placeholder="全部台账"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" :loading="loading" @click="loadOverview">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
            <a-button @click="goReport">查看详细报表</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="12" class="mb-3">
      <a-col :span="6">
        <a-card :bordered="false" class="kpi-card">
          <div class="kpi-title">分子</div>
          <div class="kpi-value">{{ overview?.kpi?.numerator ?? 0 }}</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="kpi-card">
          <div class="kpi-title">分母</div>
          <div class="kpi-value">{{ overview?.kpi?.denominator ?? 0 }}</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="kpi-card">
          <div class="kpi-title">百分比</div>
          <div class="kpi-value">{{ overview?.kpi?.percentDisplay ?? '--' }}</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="kpi-card">
          <div class="kpi-title">环比增长</div>
          <div class="kpi-value">{{ overview?.kpi?.growthDisplay ?? '--' }}</div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="12">
      <a-col :span="16">
        <a-card :bordered="false" title="季度趋势">
          <template #extra>
            <a-select
              v-model:value="selectedTrendCode"
              :options="trendOptions"
              placeholder="选择台账"
              style="width: 260px"
            />
          </template>
          <div class="mb-2 text-xs text-gray-500">{{ overview?.noDenominatorRule }}</div>
          <EchartsUI ref="chartRef" style="height: 360px" />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card :bordered="false" title="科室排名（最新季度）">
          <a-table
            :columns="rankingColumns"
            :data-source="overview?.rankings ?? []"
            :pagination="false"
            size="small"
            :scroll="{ y: 360 }"
            row-key="deptId"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'trend'">
                <span :style="{ color: trendColor(record.trend) }">{{ trendText(record.trend) }}</span>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </Page>
</template>

<style scoped lang="scss">
.kpi-card {
  background: linear-gradient(120deg, #f8fafc 0%, #eef2ff 100%);
}

.kpi-title {
  color: #64748b;
  font-size: 13px;
}

.kpi-value {
  color: #0f172a;
  font-size: 24px;
  font-weight: 700;
  margin-top: 4px;
}
</style>
