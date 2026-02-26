<script setup lang="ts">
import type { VbenFormProps } from '@vben/common-ui';

import type { VxeGridProps } from '#/adapter/vxe-table';
import type { NephrologyLedgerMaintainItem } from '#/api/data-center/nephrology/ledger-maintain/model';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { getVxePopupContainer } from '@vben/utils';

import { Popconfirm, Space } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  nephrologyLedgerMaintainList,
  nephrologyLedgerMaintainRemove,
} from '#/api/data-center/nephrology/ledger-maintain';

import ledgerMaintainDrawer from './ledger-maintain-drawer.vue';
import { columns, pageText, querySchema } from './data';

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
        const rows = await nephrologyLedgerMaintainList({
          ...formValues,
        });
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
  id: 'data-center-nephrology-ledger-maintain-index',
};

const [BasicTable, tableApi] = useVbenVxeGrid({
  formOptions,
  gridOptions,
});

const [LedgerMaintainDrawer, drawerApi] = useVbenDrawer({
  connectedComponent: ledgerMaintainDrawer,
});

function expandAll() {
  tableApi.grid?.setAllTreeExpand(true);
}

function collapseAll() {
  tableApi.grid?.setAllTreeExpand(false);
}

function handleAdd() {
  drawerApi.setData({ update: false });
  drawerApi.open();
}

function handleSubAdd(row: NephrologyLedgerMaintainItem) {
  drawerApi.setData({ parentId: row.id, update: false });
  drawerApi.open();
}

function handleEdit(record: NephrologyLedgerMaintainItem) {
  drawerApi.setData({ id: record.id, update: true });
  drawerApi.open();
}

async function handleDelete(row: NephrologyLedgerMaintainItem) {
  if (!row.id) {
    return;
  }
  await nephrologyLedgerMaintainRemove(row.id);
  await tableApi.query();
}

const tableTitle = '台账维护';
</script>

<template>
  <Page :auto-content-height="true">
    <BasicTable :table-title="tableTitle">
      <template #toolbar-tools>
        <Space>
          <a-button @click="collapseAll">{{ pageText.collapse }}</a-button>
          <a-button @click="expandAll">{{ pageText.expand }}</a-button>
          <a-button
            type="primary"
            v-access:code="['data-center:nephrology:ledger-maintain:add']"
            @click="handleAdd"
          >
            {{ $t('pages.common.add') }}
          </a-button>
        </Space>
      </template>
      <template #action="{ row }">
        <Space>
          <ghost-button
            v-access:code="['data-center:nephrology:ledger-maintain:edit']"
            @click="handleEdit(row)"
          >
            {{ $t('pages.common.edit') }}
          </ghost-button>
          <ghost-button
            class="btn-success"
            v-access:code="['data-center:nephrology:ledger-maintain:add']"
            @click="handleSubAdd(row)"
          >
            {{ $t('pages.common.add') }}
          </ghost-button>
          <Popconfirm
            :get-popup-container="getVxePopupContainer"
            placement="left"
            title="确认删除吗？"
            @confirm="handleDelete(row)"
          >
            <ghost-button
              danger
              v-access:code="['data-center:nephrology:ledger-maintain:remove']"
              @click.stop=""
            >
              {{ $t('pages.common.delete') }}
            </ghost-button>
          </Popconfirm>
        </Space>
      </template>
    </BasicTable>
    <LedgerMaintainDrawer @reload="tableApi.query()" />
  </Page>
</template>

