<script setup lang="ts">
import type { EchartsUIType } from '@vben/plugins/echarts';
import type { HospitalQcTrendSeries } from '#/api/data-center/hospital-qc/dashboard/model';

import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

const props = defineProps<{
  trend: HospitalQcTrendSeries;
  deptId?: number;
  quarter?: string;
}>();

const router = useRouter();
const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

// 计算最新指标数据
const latestMetric = computed(() => {
  const metrics = props.trend.quarterMetrics ?? [];
  return metrics.length > 0 ? metrics[metrics.length - 1] : null;
});

// 是否无分母台账
const isNoDenominator = computed(() => {
  const metrics = props.trend.quarterMetrics ?? [];
  return metrics.length > 0 && metrics[0]?.noDenominator === true;
});

// 计算标题显示文本（包含最新百分比或数值）
const cardTitle = computed(() => {
  const name = props.trend.ledgerName ?? '';
  if (isNoDenominator.value) {
    const num = latestMetric.value?.numerator;
    return num !== undefined && num !== null ? `${name} (${num})` : name;
  }
  const percent = latestMetric.value?.percentDisplay;
  return percent && percent !== '--' ? `${name} (${percent})` : name;
});

function renderChart() {
  const metrics = props.trend.quarterMetrics ?? [];
  
  if (metrics.length === 0) {
    renderEcharts({
      title: { text: '暂无数据', textStyle: { fontSize: 12 } },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: [],
    });
    return;
  }

  renderEcharts({
    color: ['#0ea5e9'],
    grid: {
      bottom: 25,
      containLabel: true,
      left: 5,
      right: 5,
      top: 10,
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const data = params[0];
        const metric = metrics[data.dataIndex];
        if (isNoDenominator.value) {
          return `${data.name}<br/>数值: ${metric?.numerator ?? 0}`;
        }
        return `${data.name}<br/>百分比: ${metric?.percentDisplay ?? '--'}<br/>分子: ${metric?.numerator ?? 0}<br/>分母: ${metric?.denominator ?? 0}`;
      },
    },
    xAxis: {
      type: 'category',
      data: metrics.map((item) => item.quarter).filter(Boolean) as string[],
      axisLabel: { fontSize: 10, interval: 0 },
    },
    yAxis: {
      type: 'value',
      name: isNoDenominator.value ? '' : '%',
      nameTextStyle: { fontSize: 10 },
      axisLabel: {
        fontSize: 10,
        formatter: isNoDenominator.value ? '{value}' : '{value}%',
      },
    },
    series: [
      {
        name: isNoDenominator.value ? '数值' : '百分比',
        data: isNoDenominator.value
          ? metrics.map((item) => item.numerator ?? null)
          : metrics.map((item) => item.indicatorPercent ?? null),
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          width: 2,
        },
        areaStyle: {
          opacity: 0.1,
        },
      },
    ],
  });
}

function goDetail() {
  if (!props.trend.ledgerCode) return;
  router.push({
    path: '/data-center/hospital-qc/detail',
    query: {
      ledgerCode: props.trend.ledgerCode,
      deptId: props.deptId,
      quarter: props.quarter,
    },
  });
}

watch(() => props.trend, () => {
  renderChart();
}, { deep: true });

onMounted(() => {
  renderChart();
});
</script>

<template>
  <a-card 
    :bordered="false" 
    class="ledger-card"
    size="small"
    :title="cardTitle"
  >
    <template #extra>
      <a-button type="link" size="small" @click="goDetail">
        明细
      </a-button>
    </template>
    <EchartsUI ref="chartRef" style="height: 150px" />
    <div class="ledger-subtitle">{{ props.trend.ledgerName ?? '' }}</div>
  </a-card>
</template>

<style scoped lang="scss">
.ledger-card {
  width: 100%;
  
  :deep(.ant-card-head) {
    padding: 6px 10px;
    min-height: 36px;
    border-bottom: 1px solid #f0f0f0;
    
    .ant-card-head-title {
      font-size: 13px;
      font-weight: 500;
      padding: 0;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    
    .ant-card-extra {
      padding: 0;
    }
  }
  
  :deep(.ant-card-body) {
    padding: 8px;
  }
}

.ledger-subtitle {
  text-align: center;
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
  padding-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
