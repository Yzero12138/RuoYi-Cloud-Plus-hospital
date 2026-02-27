<script setup lang="ts">
import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { $t } from '@vben/locales';
import { cloneDeep } from '@vben/utils';

import { useVbenForm } from '#/adapter/form';
import {
  nephrologyLedgerMaintainAdd,
  nephrologyLedgerMaintainInfo,
  nephrologyLedgerMaintainList,
  nephrologyLedgerMaintainUpdate,
} from '#/api/data-center/nephrology/ledger-maintain';
import { nephrologyLedgerQueryOptions } from '#/api/data-center/nephrology/ledger-query';
import { defaultFormValueGetter, useBeforeCloseDiff } from '#/utils/popup';

import { drawerSchema } from './data';

interface DrawerProps {
  id?: number;
  parentId?: number;
  update: boolean;
}

interface LedgerTreeNode {
  id?: number;
  ledgerCode?: string;
  ledgerName?: string;
  children?: LedgerTreeNode[];
}

interface TreeOption {
  label: string;
  value: number;
  children?: TreeOption[];
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
    labelWidth: 90,
  },
  schema: drawerSchema(),
  showDefaultActions: false,
  wrapperClass: 'grid-cols-2',
});

function buildTreeOptions(list: LedgerTreeNode[] = []): TreeOption[] {
  return list.map((item): TreeOption => {
    const children = item.children?.length ? buildTreeOptions(item.children) : undefined;
    return {
      label: `${item.ledgerName ?? ''}${item.ledgerCode ? ` (${item.ledgerCode})` : ''}`,
      value: Number(item.id ?? 0),
      children,
    };
  });
}

async function setupParentSelect() {
  const treeList = await nephrologyLedgerMaintainList();
  const options = buildTreeOptions(treeList);
  const treeData = [
    {
      label: '根节点',
      value: 0,
      children: options,
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
        listHeight: 300,
        showSearch: true,
        treeData,
        treeDefaultExpandAll: true,
        treeLine: { showLeafIcon: false },
        treeNodeFilterProp: 'label',
      },
      fieldName: 'parentId',
    },
  ]);
}

async function setupQueryOptions() {
  const list = await nephrologyLedgerQueryOptions();
  const options = [
    { label: '无', value: 'NONE' },
    ...list.map((item) => ({
      label: item.queryName
        ? `${item.queryName} (${item.queryCode ?? ''})`
        : item.queryCode ?? '',
      value: item.queryCode ?? 'NONE',
    })),
  ];
  formApi.updateSchema([
    {
      componentProps: {
        options,
      },
      fieldName: 'queryTarget',
    },
  ]);
}

const { onBeforeClose, markInitialized, resetInitialized } = useBeforeCloseDiff(
  {
    initializedGetter: defaultFormValueGetter(formApi),
    currentGetter: defaultFormValueGetter(formApi),
  },
);

const [BasicDrawer, drawerApi] = useVbenDrawer({
  onBeforeClose,
  onClosed: handleClosed,
  onConfirm: handleConfirm,
  async onOpenChange(isOpen) {
    if (!isOpen) {
      return null;
    }
    drawerApi.drawerLoading(true);

    const { id, parentId, update } = drawerApi.getData() as DrawerProps;
    isUpdate.value = update;

    await Promise.all([setupParentSelect(), setupQueryOptions()]);

    if (id && update) {
      const record = await nephrologyLedgerMaintainInfo(id);
      await formApi.setValues(record);
    } else {
      await formApi.setFieldValue('parentId', parentId ?? 0);
      await formApi.setFieldValue('queryTarget', 'NONE');
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
      ? nephrologyLedgerMaintainUpdate(data)
      : nephrologyLedgerMaintainAdd(data));
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
  <BasicDrawer :title="title" class="w-[620px]">
    <BasicForm />
  </BasicDrawer>
</template>

