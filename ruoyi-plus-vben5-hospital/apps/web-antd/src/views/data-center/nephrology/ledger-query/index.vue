<script setup lang="ts">
import type { VbenFormProps } from '@vben/common-ui';

import type { VxeGridProps } from '#/adapter/vxe-table';
import type { NephrologyLedgerQueryItem } from '#/api/data-center/nephrology/ledger-query/model';

import { Page, useVbenModal } from '@vben/common-ui';
import { getVxePopupContainer } from '@vben/utils';

import { Modal, Popconfirm, Space } from 'ant-design-vue';

import { useVbenVxeGrid, vxeCheckboxChecked } from '#/adapter/vxe-table';
import {
  nephrologyLedgerQueryList,
  nephrologyLedgerQueryRemove,
} from '#/api/data-center/nephrology/ledger-query';

import ledgerQueryModal from './ledger-query-modal.vue';
import { columns, querySchema } from './data';

const formOptions: VbenFormProps = {
  commonConfig: {
    labelWidth: 80,
    componentProps: {
      allowClear: true,
    },
  },
  schema: querySchema(),
  wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4',
};

const gridOptions: VxeGridProps = {
  checkboxConfig: {
    highlight: true,
    reserve: true,
  },
  columns,
  height: 'auto',
  keepSource: true,
  pagerConfig: {},
  proxyConfig: {
    ajax: {
      query: async ({ page }, formValues = {}) => {
        return await nephrologyLedgerQueryList({
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
  id: 'data-center-nephrology-ledger-query-index',
};

const [BasicTable, tableApi] = useVbenVxeGrid({
  formOptions,
  gridOptions,
});

const [LedgerQueryModal, modalApi] = useVbenModal({
  connectedComponent: ledgerQueryModal,
});

function handleAdd() {
  modalApi.setData({});
  modalApi.open();
}

function handleEdit(record: NephrologyLedgerQueryItem) {
  modalApi.setData({ id: record.id });
  modalApi.open();
}

async function handleDelete(row: NephrologyLedgerQueryItem) {
  if (!row.id) {
    return;
  }
  await nephrologyLedgerQueryRemove([row.id]);
  await tableApi.query();
}

function handleMultiDelete() {
  const rows = tableApi.grid.getCheckboxRecords();
  const ids = rows.map((row: NephrologyLedgerQueryItem) => row.id);
  if (!ids.length) {
    return;
  }
  Modal.confirm({
    title: '提示',
    okType: 'danger',
    content: `确认删除选中的${ids.length}条记录吗？`,
    onOk: async () => {
      await nephrologyLedgerQueryRemove(ids as number[]);
      await tableApi.query();
    },
  });
}
</script>

<template>
  <Page :auto-content-height="true">
    <BasicTable table-title="台账查询配置">
      <template #toolbar-tools>
        <Space>
          <a-button
            :disabled="!vxeCheckboxChecked(tableApi)"
            danger
            type="primary"
            v-access:code="['data-center:nephrology:ledger-query:remove']"
            @click="handleMultiDelete"
          >
            {{ $t('pages.common.delete') }}
          </a-button>
          <a-button
            type="primary"
            v-access:code="['data-center:nephrology:ledger-query:add']"
            @click="handleAdd"
          >
            {{ $t('pages.common.add') }}
          </a-button>
        </Space>
      </template>
      <template #action="{ row }">
        <Space>
          <ghost-button
            v-access:code="['data-center:nephrology:ledger-query:edit']"
            @click.stop="handleEdit(row)"
          >
            {{ $t('pages.common.edit') }}
          </ghost-button>
          <Popconfirm
            :get-popup-container="getVxePopupContainer"
            placement="left"
            title="确认删除吗？"
            @confirm="handleDelete(row)"
          >
            <ghost-button
              danger
              v-access:code="['data-center:nephrology:ledger-query:remove']"
              @click.stop=""
            >
              {{ $t('pages.common.delete') }}
            </ghost-button>
          </Popconfirm>
        </Space>
      </template>
    </BasicTable>
    <LedgerQueryModal @reload="tableApi.query()" />
  </Page>
</template>

