<script setup lang="ts">
import type { VbenFormProps } from '@vben/common-ui';

import type { VxeGridProps } from '#/adapter/vxe-table';
import type { HospitalQcLedgerItem } from '#/api/data-center/hospital-qc/ledger-maintain/model';
import type { HospitalQcLedgerQueryItem } from '#/api/data-center/hospital-qc/ledger-query/model';

import { computed, onMounted, ref } from 'vue';

import { Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { getVxePopupContainer, listToTree } from '@vben/utils';

import { Card, Empty, Popconfirm, Space, Tree, message } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  hospitalQcLedgerMaintainList,
  hospitalQcLedgerMaintainRemove,
} from '#/api/data-center/hospital-qc/ledger-maintain';
import {
  hospitalQcLedgerQueryList,
  hospitalQcLedgerQueryRemove,
} from '#/api/data-center/hospital-qc/ledger-query';

import { queryGridColumns, queryGridSchema } from './data';
import ledgerItemDrawer from './ledger-item-drawer.vue';
import ledgerQueryModal from './ledger-query-modal.vue';

const selectedLedger = ref<HospitalQcLedgerItem>();
const treeData = ref<any[]>([]);

const formOptions: VbenFormProps = {
  commonConfig: {
    labelWidth: 86,
    componentProps: {
      allowClear: true,
    },
  },
  schema: queryGridSchema(),
  wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4',
};

const gridOptions: VxeGridProps = {
  columns: queryGridColumns,
  height: 560,
  keepSource: true,
  pagerConfig: {},
  proxyConfig: {
    ajax: {
      query: async ({ page }, formValues = {}) => {
        return await hospitalQcLedgerQueryList({
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
  id: 'data-center-hospital-qc-ledger-config-query-index',
};

const [QueryTable, queryTableApi] = useVbenVxeGrid({
  formOptions,
  gridOptions,
});

const [LedgerItemDrawer, itemDrawerApi] = useVbenDrawer({
  connectedComponent: ledgerItemDrawer,
});

const [LedgerQueryModal, queryModalApi] = useVbenModal({
  connectedComponent: ledgerQueryModal,
});

const hasSelectedNode = computed(() => !!selectedLedger.value?.id);

function toTree(list: HospitalQcLedgerItem[] = []) {
  return list.map((item) => ({
    title: `${item.ledgerName ?? ''} [${item.nodeType ?? ''}]`,
    key: item.id,
    raw: item,
    children: item.children?.length ? toTree(item.children) : undefined,
  }));
}

function normalizeLedgerTree(list: HospitalQcLedgerItem[] = []) {
  if (!list?.length) {
    return [];
  }
  const hasNestedChildren = list.some((item) => (item.children?.length ?? 0) > 0);
  if (hasNestedChildren) {
    return list;
  }
  return listToTree(list as any[], { id: 'id', pid: 'parentId' }) as HospitalQcLedgerItem[];
}

function resolveLedgerList(payload: unknown): HospitalQcLedgerItem[] {
  if (Array.isArray(payload)) {
    return payload as HospitalQcLedgerItem[];
  }
  if (payload && typeof payload === 'object') {
    const obj = payload as Record<string, unknown>;
    if (Array.isArray(obj.data)) {
      return obj.data as HospitalQcLedgerItem[];
    }
    if (Array.isArray(obj.rows)) {
      return obj.rows as HospitalQcLedgerItem[];
    }
  }
  return [];
}

async function reloadTree() {
  try {
    const payload = await hospitalQcLedgerMaintainList();
    const list = resolveLedgerList(payload);
    treeData.value = toTree(normalizeLedgerTree(list));
  } catch (error: any) {
    treeData.value = [];
    message.error(error?.message ?? '加载台账树失败');
  }
}

function handleTreeSelect(_: any[], info: any) {
  selectedLedger.value = info?.node?.raw ?? info?.node?.dataRef?.raw;
}

function handleAddRoot() {
  itemDrawerApi.setData({ update: false, parentId: 0 });
  itemDrawerApi.open();
}

function handleAddChild() {
  if (!selectedLedger.value?.id) {
    return;
  }
  itemDrawerApi.setData({ update: false, parentId: selectedLedger.value.id });
  itemDrawerApi.open();
}

function handleEditNode() {
  if (!selectedLedger.value?.id) {
    return;
  }
  itemDrawerApi.setData({ update: true, id: selectedLedger.value.id });
  itemDrawerApi.open();
}

async function handleDeleteNode() {
  if (!selectedLedger.value?.id) {
    return;
  }
  await hospitalQcLedgerMaintainRemove(selectedLedger.value.id);
  selectedLedger.value = undefined;
  await reloadTree();
}

function handleAddQuery() {
  queryModalApi.setData({});
  queryModalApi.open();
}

function handleEditQuery(row: HospitalQcLedgerQueryItem) {
  queryModalApi.setData({ id: row.id });
  queryModalApi.open();
}

async function handleDeleteQuery(row: HospitalQcLedgerQueryItem) {
  if (!row.id) {
    return;
  }
  await hospitalQcLedgerQueryRemove([row.id]);
  await queryTableApi.query();
}

onMounted(async () => {
  await reloadTree();
});
</script>

<template>
  <Page :auto-content-height="true">
    <div class="grid grid-cols-12 gap-3">
      <Card class="col-span-12 lg:col-span-4" title="台账树配置" :bordered="false">
        <div class="mb-3 flex flex-wrap gap-2">
          <a-button
            size="small"
            type="primary"
            v-access:code="['data-center:hospital-qc:ledger-maintain:add']"
            @click="handleAddRoot"
          >
            新增根节点
          </a-button>
          <a-button size="small" @click="reloadTree">刷新</a-button>
        </div>
        <div class="mb-2 text-xs text-gray-500">节点数：{{ treeData.length }}</div>

        <Tree
          v-if="treeData.length > 0"
          class="hospital-qc-ledger-tree"
          :field-names="{ children: 'children', key: 'key', title: 'title' }"
          :tree-data="treeData"
          :virtual="false"
          block-node
          default-expand-all
          @select="handleTreeSelect"
        />
        <Empty
          v-else
          :description="'暂无台账节点，请先点击“新增根节点”创建台账树'"
        />

        <div class="mt-3 flex flex-wrap gap-2">
          <a-button
            :disabled="!hasSelectedNode"
            v-access:code="['data-center:hospital-qc:ledger-maintain:add']"
            @click="handleAddChild"
          >
            新增子节点
          </a-button>
          <a-button
            :disabled="!hasSelectedNode"
            v-access:code="['data-center:hospital-qc:ledger-maintain:edit']"
            @click="handleEditNode"
          >
            编辑节点
          </a-button>
          <Popconfirm title="确认删除选中节点及其子节点吗？" @confirm="handleDeleteNode">
            <a-button
              :disabled="!hasSelectedNode"
              danger
              v-access:code="['data-center:hospital-qc:ledger-maintain:remove']"
            >
              删除节点
            </a-button>
          </Popconfirm>
        </div>
      </Card>

      <Card class="col-span-12 lg:col-span-8" title="SQL 查询配置" :bordered="false">
        <QueryTable>
          <template #toolbar-tools>
            <a-button
              type="primary"
              v-access:code="['data-center:hospital-qc:ledger-query:add']"
              @click="handleAddQuery"
            >
              新增查询配置
            </a-button>
          </template>

          <template #action="{ row }">
            <Space>
              <ghost-button
                v-access:code="['data-center:hospital-qc:ledger-query:edit']"
                @click.stop="handleEditQuery(row)"
              >
                {{ $t('pages.common.edit') }}
              </ghost-button>
              <Popconfirm
                :get-popup-container="getVxePopupContainer"
                placement="left"
                title="确认删除吗？"
                @confirm="handleDeleteQuery(row)"
              >
                <ghost-button
                  danger
                  v-access:code="['data-center:hospital-qc:ledger-query:remove']"
                  @click.stop=""
                >
                  {{ $t('pages.common.delete') }}
                </ghost-button>
              </Popconfirm>
            </Space>
          </template>
        </QueryTable>
      </Card>
    </div>

    <LedgerItemDrawer
      @reload="
        async () => {
          await reloadTree();
          await queryTableApi.query();
        }
      "
    />
    <LedgerQueryModal @reload="queryTableApi.query()" />
  </Page>
</template>

<style scoped>
.hospital-qc-ledger-tree {
  min-height: 120px;
}

.hospital-qc-ledger-tree :deep(.ant-tree-node-content-wrapper) {
  color: rgba(0, 0, 0, 0.88);
}

.hospital-qc-ledger-tree :deep(.ant-tree-title) {
  color: inherit;
}
</style>
