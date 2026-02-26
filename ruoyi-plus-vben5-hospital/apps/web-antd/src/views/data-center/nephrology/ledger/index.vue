<script setup lang="ts">
import type { VbenFormProps } from '@vben/common-ui';

import type { VxeGridProps } from '#/adapter/vxe-table';
import type { NephrologyLedgerCountItem } from '#/api/data-center/nephrology/ledger/model';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { nephrologyLedgerList } from '#/api/data-center/nephrology/ledger';

import {
  columns,
  defaultDischargeTimeType,
  defaultDischargeTimeValue,
  NODE_TYPE,
  pageText,
  querySchema,
} from './data';

const router = useRouter();

const currentQuery = ref({
  dischargeTimeType: defaultDischargeTimeType,
  dischargeTimeValue: defaultDischargeTimeValue,
});

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
  pagerConfig: {
    enabled: false,
  },
  proxyConfig: {
    ajax: {
      query: async (_, formValues = {}) => {
        const query = {
          dischargeTimeType: defaultDischargeTimeType,
          dischargeTimeValue: defaultDischargeTimeValue,
          ...formValues,
        };
        currentQuery.value = query;
        const rows = await nephrologyLedgerList(query);
        return { rows };
      },
    },
  },
  rowConfig: {
    keyField: 'id',
  },
  treeConfig: {
    parentField: 'parentId',
    rowField: 'id',
    transform: false,
  },
  id: 'data-center-nephrology-ledger-index',
};

const [BasicTable, tableApi] = useVbenVxeGrid({
  formOptions,
  gridOptions,
  gridEvents: {
    toggleTreeExpand: (event) => {
      const { row = {}, expanded } = event;
      row.expand = expanded;
    },
  },
});

function expandAll() {
  tableApi.grid?.setAllTreeExpand(true);
}

function collapseAll() {
  tableApi.grid?.setAllTreeExpand(false);
}

function isIndicatorRow(row: NephrologyLedgerCountItem) {
  return Array.isArray(row.children) && row.children.length > 0;
}

function isExpanded(row: NephrologyLedgerCountItem) {
  return !!row.expand;
}

function toDetail(row?: NephrologyLedgerCountItem) {
  if (!row?.ledgerCode) {
    return;
  }
  router.push({
    path: '/data-center/nephrology/ledger/detail',
    query: {
      ledgerCode: row.ledgerCode,
      ledgerName: row.ledgerName,
      dischargeTimeType: currentQuery.value.dischargeTimeType,
      dischargeTimeValue: currentQuery.value.dischargeTimeValue,
    },
  });
}

function findChildByType(
  row: NephrologyLedgerCountItem,
  nodeType: string,
): NephrologyLedgerCountItem | undefined {
  return row.children?.find(
    (item) => item.nodeType === nodeType && item.queryable,
  );
}

function toSummaryDetail(row: NephrologyLedgerCountItem, nodeType: string) {
  const target = findChildByType(row, nodeType);
  toDetail(target);
}

const tableTitle = '台账';
</script>

<template>
  <Page :auto-content-height="true">
    <BasicTable :table-title="tableTitle">
      <template #toolbar-tools>
        <a-space>
          <a-button @click="collapseAll">{{ pageText.collapse }}</a-button>
          <a-button @click="expandAll">{{ pageText.expand }}</a-button>
        </a-space>
      </template>
      <template #countCell="{ row }">
        <template v-if="isIndicatorRow(row)">
          <template v-if="isExpanded(row)">
            <span>-</span>
          </template>
          <div v-else class="count-stack">
            <a class="count-link" @click="toSummaryDetail(row, NODE_TYPE.numerator)">
              {{ `${pageText.numerator}：${row.numeratorCount ?? 0}` }}
            </a>
            <a
              v-if="(row.denominatorCount ?? 0) > 0"
              class="count-link"
              @click="toSummaryDetail(row, NODE_TYPE.denominator)"
            >
              {{ `${pageText.denominator}：${row.denominatorCount ?? 0}` }}
            </a>
          </div>
        </template>
        <a
          v-if="row.queryable"
          class="count-link"
          @click="toDetail(row)"
        >
          {{ row.countValue ?? 0 }}
        </a>
        <span v-else>{{ row.countValue ?? 0 }}</span>
      </template>
    </BasicTable>
  </Page>
</template>

<style scoped lang="scss">
.count-stack {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.count-link {
  color: #1677ff;
}
</style>
