<script setup lang="ts">
import type {
  HospitalQcDashboardFilterOptions,
  HospitalQcDashboardOverview,
} from '#/api/data-center/hospital-qc/dashboard/model';

import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';

import {
  hospitalQcDashboardOptions,
  hospitalQcDashboardOverview,
} from '#/api/data-center/hospital-qc/dashboard';

import LedgerChartCard from './LedgerChartCard.vue';
import {
  type TimeDimension,
  buildTimeValueOptions,
  getDefaultTimeValue,
  timeDimensionOptions,
} from './data';

const router = useRouter();

const loading = ref(false);
const options = ref<HospitalQcDashboardFilterOptions>({
  deptOptions: [],
  ledgerOptions: [],
});

const queryForm = ref({
  timeDimension: 'quarter' as TimeDimension,
  timeValue: getDefaultTimeValue('quarter'),
  ledgerCodes: [] as string[],
});

const timeValueOptionsList = computed(() => {
  return buildTimeValueOptions(queryForm.value.timeDimension);
});

const overview = ref<HospitalQcDashboardOverview>();

const currentDeptDisplay = computed(() => {
  return options.value.currentDeptName || '当前科室';
});

const currentDeptId = computed(() => {
  return options.value.currentDeptId;
});

const queryYear = computed(() => {
  return parseInt(queryForm.value.timeValue, 10) || new Date().getFullYear();
});

const latestQuarter = computed(() => {
  const quarters = overview.value?.quarters ?? [];
  return quarters.length ? quarters[quarters.length - 1] : `${queryYear.value}-Q4`;
});

async function loadOptions() {
  const data = await hospitalQcDashboardOptions();
  options.value = {
    isAdmin: data.isAdmin,
    currentDeptId: data.currentDeptId,
    currentDeptName: data.currentDeptName,
    allowedDeptIds: data.allowedDeptIds,
    deptOptions: data.deptOptions ?? [],
    ledgerOptions: data.ledgerOptions ?? [],
  };
}

async function loadOverview() {
  if (!currentDeptId.value) {
    return;
  }

  loading.value = true;
  try {
    const data = await hospitalQcDashboardOverview({
      year: queryYear.value,
      deptIds: [currentDeptId.value],
      ledgerCodes: queryForm.value.ledgerCodes.length > 0 ? queryForm.value.ledgerCodes : undefined,
    });
    overview.value = data;
  } finally {
    loading.value = false;
  }
}

function handleReset() {
  queryForm.value = {
    timeDimension: 'quarter',
    timeValue: getDefaultTimeValue('quarter'),
    ledgerCodes: [],
  };
  loadOverview();
}

function onTimeDimensionChange(val: TimeDimension) {
  queryForm.value.timeValue = getDefaultTimeValue(val);
}

function goReport() {
  router.push({
    path: '/data-center/hospital-qc/report',
    query: {
      timeType: queryForm.value.timeDimension,
      timeValue: queryForm.value.timeValue,
      deptIds: String(currentDeptId.value),
      ledgerCodes: queryForm.value.ledgerCodes.join(','),
    },
  });
}

onMounted(async () => {
  await loadOptions();
  await loadOverview();
});
</script>

<template>
  <Page :auto-content-height="true">
    <a-card :bordered="false" class="mb-3">
      <a-form layout="inline">
        <a-form-item label="时间维度">
          <a-select
            v-model:value="queryForm.timeDimension"
            :options="timeDimensionOptions()"
            style="width: 100px"
            @change="onTimeDimensionChange"
          />
        </a-form-item>
        <a-form-item label="时间">
          <a-select
            v-model:value="queryForm.timeValue"
            :options="timeValueOptionsList"
            style="width: 140px"
          />
        </a-form-item>
        <a-form-item label="台账筛选">
          <a-select
            v-model:value="queryForm.ledgerCodes"
            mode="multiple"
            :options="(options.ledgerOptions ?? []).map((item) => ({ label: item.label, value: item.value }))"
            style="min-width: 260px"
            placeholder="全部台账"
            allow-clear
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

    <a-card :bordered="false" class="mb-3" size="small">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <span class="text-sm text-gray-500">当前科室:</span>
          <a-tag color="blue" size="large">{{ currentDeptDisplay }}</a-tag>
          <span class="text-xs text-gray-400">
            数据仅展示当前登录科室
          </span>
        </div>
        <div v-if="overview?.kpi" class="kpi-quick-view">
          <span class="kpi-item">
            <span class="label">分子:</span>
            <span class="value">{{ overview.kpi.numerator ?? 0 }}</span>
          </span>
          <span class="kpi-item">
            <span class="label">分母:</span>
            <span class="value">{{ overview.kpi.denominator ?? 0 }}</span>
          </span>
          <span class="kpi-item">
            <span class="label">百分比:</span>
            <span class="value highlight">{{ overview.kpi.percentDisplay ?? '--' }}</span>
          </span>
        </div>
      </div>
    </a-card>

    <div class="ledger-flex-container">
      <LedgerChartCard 
        v-for="trend in overview?.trends ?? []" 
        :key="trend.ledgerCode"
        :trend="trend" 
        :dept-id="currentDeptId"
        :quarter="latestQuarter"
        class="ledger-flex-item"
      />
    </div>

    <a-empty 
      v-if="!loading && (overview?.trends?.length ?? 0) === 0" 
      description="暂无数台数据"
      class="mt-10"
    />
  </Page>
</template>

<style scoped lang="scss">
.kpi-quick-view {
  display: flex;
  gap: 20px;
  
  .kpi-item {
    display: flex;
    align-items: center;
    gap: 4px;
    
    .label {
      font-size: 13px;
      color: #64748b;
    }
    
    .value {
      font-size: 14px;
      font-weight: 600;
      color: #0f172a;
      
      &.highlight {
        color: #0ea5e9;
      }
    }
  }
}

.ledger-flex-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 0 0 20px 0;
}

.ledger-flex-item {
  flex: 0 0 calc(25% - 9px);
  min-width: 280px;
  max-width: 400px;
}
</style>
