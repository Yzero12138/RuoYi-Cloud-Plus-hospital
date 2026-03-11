<script setup lang="ts">
import type { VbenFormProps } from '@vben/common-ui';

import type { VxeGridProps } from '#/adapter/vxe-table';
import type { HospitalQcLedgerItem } from '#/api/data-center/hospital-qc/ledger-maintain/model';
import type { HospitalQcLedgerQueryItem } from '#/api/data-center/hospital-qc/ledger-query/model';

import { computed, onMounted, ref } from 'vue';

import { ColPage, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { getVxePopupContainer, listToTree } from '@vben/utils';

import {
  Card,
  Divider,
  Empty,
  Popconfirm,
  Space,
  Tooltip,
  Tree,
  message,
} from 'ant-design-vue';

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
  height: 'auto',
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
const isSelectedDeptNode = computed(
  () => selectedLedger.value?.nodeType === 'DEPT',
);
const canEditOrDelete = computed(
  () => hasSelectedNode.value && !isSelectedDeptNode.value,
);

function toTree(list: HospitalQcLedgerItem[] = []) {
  return list.map((item) => {
    const isDept = item.nodeType === 'DEPT';
    return {
      title: isDept
        ? `${item.deptName ?? item.ledgerName ?? ''}`
        : `${item.ledgerName ?? ''} [${item.nodeType ?? ''}]`,
      key: item.id,
      raw: item,
      selectable: true,
      children: item.children?.length ? toTree(item.children) : undefined,
    };
  });
}

function normalizeLedgerTree(list: HospitalQcLedgerItem[] = []) {
  if (!list?.length) {
    return [];
  }
  const hasNestedChildren = list.some(
    (item) => (item.children?.length ?? 0) > 0,
  );
  if (hasNestedChildren) {
    return list;
  }
  return listToTree(list as any[], {
    id: 'id',
    pid: 'parentId',
  }) as HospitalQcLedgerItem[];
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
  if (!selectedLedger.value) {
    return;
  }
  const selected = selectedLedger.value;
  if (selected.nodeType === 'DEPT') {
    itemDrawerApi.setData({
      update: false,
      parentId: 0,
      deptId: selected.deptId,
    });
  } else {
    itemDrawerApi.setData({
      update: false,
      parentId: selected.id,
      deptId: selected.deptId,
    });
  }
  itemDrawerApi.open();
}

function handleEditNode() {
  if (!selectedLedger.value?.id || selectedLedger.value?.nodeType === 'DEPT') {
    return;
  }
  itemDrawerApi.setData({ update: true, id: selectedLedger.value.id });
  itemDrawerApi.open();
}

async function handleDeleteNode() {
  if (!selectedLedger.value?.id || selectedLedger.value?.nodeType === 'DEPT') {
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
  <ColPage
    :auto-content-height="true"
    :left-min-width="18"
    :left-max-width="45"
    :left-width="28"
    :right-width="72"
    :split-handle="true"
    :split-line="true"
  >
    <template #left>
      <Card
        class="ledger-tree-card mr-2 h-full"
        title="台账树配置"
        :bordered="false"
        size="small"
      >
        <template #extra>
          <Tooltip title="刷新">
            <a-button size="small" type="text" @click="reloadTree">
              刷新
            </a-button>
          </Tooltip>
        </template>

        <div class="mb-3 flex flex-wrap items-center gap-1">
          <a-button
            size="small"
            type="primary"
            v-access:code="['data-center:hospital-qc:ledger-maintain:add']"
            @click="handleAddRoot"
          >
            新增根节点
          </a-button>
          <a-button
            size="small"
            :disabled="!hasSelectedNode"
            v-access:code="['data-center:hospital-qc:ledger-maintain:add']"
            @click="handleAddChild"
          >
            新增子节点
          </a-button>

          <Divider type="vertical" class="!mx-0.5" />

          <a-button
            size="small"
            :disabled="!canEditOrDelete"
            v-access:code="['data-center:hospital-qc:ledger-maintain:edit']"
            @click="handleEditNode"
          >
            编辑
          </a-button>
          <Popconfirm
            title="确认删除选中节点及其子节点吗？"
            @confirm="handleDeleteNode"
          >
            <a-button
              size="small"
              :disabled="!canEditOrDelete"
              danger
              v-access:code="['data-center:hospital-qc:ledger-maintain:remove']"
            >
              删除
            </a-button>
          </Popconfirm>
        </div>

        <div class="ledger-tree-wrapper">
          <Tree
            v-if="treeData.length > 0"
            class="hospital-qc-ledger-tree"
            :field-names="{
              children: 'children',
              key: 'key',
              title: 'title',
            }"
            :tree-data="treeData"
            :virtual="false"
            block-node
            default-expand-all
            @select="handleTreeSelect"
          />
          <Empty
            v-else
            description="暂无台账节点，请点击「新增根节点」创建"
            :image="Empty.PRESENTED_IMAGE_SIMPLE"
          />
        </div>
      </Card>
    </template>

    <Card
      class="ledger-query-card ml-2 h-full"
      title="SQL 查询配置"
      :bordered="false"
      size="small"
    >
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

    <LedgerItemDrawer
      @reload="
        async () => {
          await reloadTree();
          await queryTableApi.query();
        }
      "
    />
    <LedgerQueryModal @reload="queryTableApi.query()" />
  </ColPage>
</template>

<style scoped>
.ledger-tree-card :deep(.ant-card-body) {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: calc(100% - 40px);
}

.ledger-tree-wrapper {
  flex: 1;
  overflow: auto;
  min-height: 0;
}

.hospital-qc-ledger-tree {
  min-height: 80px;
}

.hospital-qc-ledger-tree :deep(.ant-tree-node-content-wrapper) {
  color: rgba(0, 0, 0, 0.88);
}

.hospital-qc-ledger-tree :deep(.ant-tree-title) {
  color: inherit;
}

.ledger-query-card :deep(.ant-card-body) {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: calc(100% - 40px);
}
</style>
