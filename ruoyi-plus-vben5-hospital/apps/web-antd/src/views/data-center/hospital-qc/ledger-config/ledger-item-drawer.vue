<script setup lang="ts">
import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { $t } from '@vben/locales';
import { addFullName, cloneDeep, getPopupContainer, listToTree } from '@vben/utils';

import { useVbenForm } from '#/adapter/form';
import {
  hospitalQcLedgerMaintainAdd,
  hospitalQcLedgerMaintainInfo,
  hospitalQcLedgerMaintainList,
  hospitalQcLedgerMaintainUpdate,
} from '#/api/data-center/hospital-qc/ledger-maintain';
import { hospitalQcLedgerQueryOptions } from '#/api/data-center/hospital-qc/ledger-query';
import { deptList } from '#/api/system/dept';
import { defaultFormValueGetter, useBeforeCloseDiff } from '#/utils/popup';

import { itemDrawerSchema } from './data';

interface DrawerProps {
  id?: number;
  parentId?: number;
  deptId?: number;
  update: boolean;
}

interface TreeNode {
  id?: number;
  ledgerCode?: string;
  ledgerName?: string;
  nodeType?: string;
  deptId?: number;
  children?: TreeNode[];
}

const emit = defineEmits<{ reload: [] }>();

const isUpdate = ref(false);
const title = computed(() => {
  return isUpdate.value ? $t('pages.common.edit') : $t('pages.common.add');
});

const [BasicForm, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 92,
  },
  schema: itemDrawerSchema(),
  showDefaultActions: false,
  wrapperClass: 'grid-cols-2',
});

function buildTreeOptions(list: TreeNode[] = []) {
  return list.map((item) => {
    return {
      label: `${item.ledgerName ?? ''}${item.ledgerCode ? ` (${item.ledgerCode})` : ''}`,
      value: Number(item.id ?? 0),
      children: item.children?.length ? buildTreeOptions(item.children) : undefined,
    };
  });
}

function extractItemsFromDeptNodes(list: TreeNode[]): TreeNode[] {
  const items: TreeNode[] = [];
  for (const item of list) {
    if (item.nodeType === 'DEPT') {
      if (item.children) {
        items.push(...item.children);
      }
    } else {
      items.push(item);
    }
  }
  return items;
}

const deptNameMap = new Map<number, string>();

async function setupDeptSelect() {
  const deptArray = await deptList({});
  const treeData = listToTree(deptArray, { id: 'deptId', pid: 'parentId' });
  addFullName(treeData, 'deptName', ' / ');
  deptNameMap.clear();

  const fillMap = (nodes?: any[]) => {
    if (!nodes) return;
    nodes.forEach((node) => {
      if (node?.deptId) {
        deptNameMap.set(node.deptId, node.fullName ?? node.deptName);
      }
      if (node?.children?.length) {
        fillMap(node.children);
      }
    });
  };
  fillMap(treeData);

  formApi.updateSchema([
    {
      componentProps: {
        fieldNames: {
          label: 'deptName',
          value: 'deptId',
        },
        getPopupContainer,
        listHeight: 300,
        showSearch: true,
        treeData,
        treeDefaultExpandAll: false,
        treeLine: { showLeafIcon: false },
        treeNodeFilterProp: 'deptName',
        treeNodeLabelProp: 'fullName',
        onChange: (value: number | null) => {
          if (!value) {
            formApi.setFieldValue('deptName', undefined);
            return;
          }
          formApi.setFieldValue('deptName', deptNameMap.get(value));
        },
      },
      fieldName: 'deptId',
    },
  ]);
}

async function setupParentSelect() {
  const list = await hospitalQcLedgerMaintainList();
  const items = extractItemsFromDeptNodes(list as TreeNode[]);
  const treeData = [
    {
      label: '根节点',
      value: 0,
      children: buildTreeOptions(items),
    },
  ];
  formApi.updateSchema([
    {
      componentProps: {
        fieldNames: {
          label: 'label',
          value: 'value',
          children: 'children',
        },
        getPopupContainer,
        showSearch: true,
        treeData,
        treeDefaultExpandAll: true,
        treeNodeFilterProp: 'label',
      },
      fieldName: 'parentId',
    },
  ]);
}

async function setupQueryOptions() {
  const options = await hospitalQcLedgerQueryOptions();
  formApi.updateSchema([
    {
      componentProps: {
        options,
      },
      fieldName: 'queryCode',
    },
    {
      componentProps: {
        onChange: async (value: string) => {
          if (value === 'I') {
            await formApi.setFieldValue('queryCode', 'NONE');
          }
        },
      },
      fieldName: 'nodeType',
    },
  ]);
}

const { onBeforeClose, markInitialized, resetInitialized } = useBeforeCloseDiff({
  initializedGetter: defaultFormValueGetter(formApi),
  currentGetter: defaultFormValueGetter(formApi),
});

const [BasicDrawer, drawerApi] = useVbenDrawer({
  onBeforeClose,
  onClosed: handleClosed,
  onConfirm: handleConfirm,
  async onOpenChange(isOpen) {
    if (!isOpen) {
      return null;
    }
    drawerApi.drawerLoading(true);

    const { id, parentId, deptId, update } = drawerApi.getData() as DrawerProps;
    isUpdate.value = update;

    await Promise.all([setupDeptSelect(), setupParentSelect(), setupQueryOptions()]);

    if (id && update) {
      const record = await hospitalQcLedgerMaintainInfo(id);
      await formApi.setValues(record);
    } else {
      await formApi.setValues({
        parentId: parentId ?? 0,
        deptId: deptId,
        queryCode: 'NONE',
        status: 0,
      });
    }

    await markInitialized();
    drawerApi.drawerLoading(false);
  },
});

async function handleConfirm() {
  try {
    drawerApi.lock(true);
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    const data = cloneDeep(await formApi.getValues());
    await (isUpdate.value
      ? hospitalQcLedgerMaintainUpdate(data)
      : hospitalQcLedgerMaintainAdd(data));
    resetInitialized();
    emit('reload');
    drawerApi.close();
  } catch (error) {
    console.error(error);
  } finally {
    drawerApi.lock(false);
  }
}

async function handleClosed() {
  await formApi.resetForm();
  resetInitialized();
}
</script>

<template>
  <BasicDrawer :title="title" class="w-[700px]">
    <BasicForm />
  </BasicDrawer>
</template>
