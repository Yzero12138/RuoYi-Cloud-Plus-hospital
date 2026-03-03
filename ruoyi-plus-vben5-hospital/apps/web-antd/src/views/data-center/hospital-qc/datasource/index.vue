<script setup lang="ts">
import type { VbenFormProps } from '@vben/common-ui';

import type { VxeGridProps } from '#/adapter/vxe-table';
import type { HospitalQcDataSourceItem } from '#/api/data-center/hospital-qc/datasource/model';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { getVxePopupContainer } from '@vben/utils';

import { message, Popconfirm, Space } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  hospitalQcDatasourceChangeStatus,
  hospitalQcDatasourceList,
  hospitalQcDatasourceRemove,
  hospitalQcDatasourceTest,
} from '#/api/data-center/hospital-qc/datasource';

import datasourceDrawer from './datasource-drawer.vue';
import { columns, querySchema } from './data';

const formOptions: VbenFormProps = {
  commonConfig: {
    labelWidth: 88,
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
        return await hospitalQcDatasourceList({
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
  id: 'data-center-hospital-qc-datasource-index',
};

const [BasicTable, tableApi] = useVbenVxeGrid({
  formOptions,
  gridOptions,
});

const [DatasourceDrawer, drawerApi] = useVbenDrawer({
  connectedComponent: datasourceDrawer,
});

function handleAdd() {
  drawerApi.setData({ update: false });
  drawerApi.open();
}

function handleEdit(record: HospitalQcDataSourceItem) {
  drawerApi.setData({ id: record.id, update: true });
  drawerApi.open();
}

async function handleDelete(row: HospitalQcDataSourceItem) {
  if (!row.id) {
    return;
  }
  await hospitalQcDatasourceRemove([row.id]);
  await tableApi.query();
}

async function handleTest(row: HospitalQcDataSourceItem) {
  if (!row.id) {
    return;
  }
  const result = await hospitalQcDatasourceTest({ id: row.id });
  if (result.success) {
    message.success(`${result.message}（${result.elapsedMs}ms）`);
  } else {
    message.error(`${result.message}（${result.elapsedMs}ms）`);
  }
}

async function handleChangeStatus(row: HospitalQcDataSourceItem) {
  if (!row.id) {
    return;
  }
  const status = row.status === 0 ? 1 : 0;
  await hospitalQcDatasourceChangeStatus({ id: row.id, status });
  await tableApi.query();
}
</script>

<template>
  <Page :auto-content-height="true">
    <BasicTable table-title="数据源配置">
      <template #toolbar-tools>
        <Space>
          <a-button
            type="primary"
            v-access:code="['data-center:hospital-qc:datasource:add']"
            @click="handleAdd"
          >
            {{ $t('pages.common.add') }}
          </a-button>
        </Space>
      </template>

      <template #action="{ row }">
        <Space>
          <ghost-button
            v-access:code="['data-center:hospital-qc:datasource:edit']"
            @click="handleEdit(row)"
          >
            {{ $t('pages.common.edit') }}
          </ghost-button>
          <ghost-button
            class="btn-success"
            v-access:code="['data-center:hospital-qc:datasource:test']"
            @click="handleTest(row)"
          >
            测试连接
          </ghost-button>
          <ghost-button
            v-access:code="['data-center:hospital-qc:datasource:edit']"
            @click="handleChangeStatus(row)"
          >
            {{ row.status === 0 ? '停用' : '启用' }}
          </ghost-button>
          <Popconfirm
            :get-popup-container="getVxePopupContainer"
            placement="left"
            title="确认删除吗？"
            @confirm="handleDelete(row)"
          >
            <ghost-button
              danger
              v-access:code="['data-center:hospital-qc:datasource:remove']"
              @click.stop=""
            >
              {{ $t('pages.common.delete') }}
            </ghost-button>
          </Popconfirm>
        </Space>
      </template>
    </BasicTable>

    <DatasourceDrawer @reload="tableApi.query()" />
  </Page>
</template>
