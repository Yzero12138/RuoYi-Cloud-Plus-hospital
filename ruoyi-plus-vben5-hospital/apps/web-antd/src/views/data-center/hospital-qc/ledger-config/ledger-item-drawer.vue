<script setup lang="ts">
import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { $t } from '@vben/locales';
import { cloneDeep, getPopupContainer } from '@vben/utils';

import { useVbenForm } from '#/adapter/form';
import {
  hospitalQcLedgerMaintainAdd,
  hospitalQcLedgerMaintainInfo,
  hospitalQcLedgerMaintainList,
  hospitalQcLedgerMaintainUpdate,
} from '#/api/data-center/hospital-qc/ledger-maintain';
import { hospitalQcLedgerQueryOptions } from '#/api/data-center/hospital-qc/ledger-query';
import { defaultFormValueGetter, useBeforeCloseDiff } from '#/utils/popup';

import { itemDrawerSchema } from './data';

interface DrawerProps {
  id?: number;
  parentId?: number;
  update: boolean;
}

interface TreeNode {
  id?: number;
  ledgerCode?: string;
  ledgerName?: string;
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

async function setupParentSelect() {
  const list = await hospitalQcLedgerMaintainList();
  const treeData = [
    {
      label: '根节点',
      value: 0,
      children: buildTreeOptions(list),
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

    const { id, parentId, update } = drawerApi.getData() as DrawerProps;
    isUpdate.value = update;

    await Promise.all([setupParentSelect(), setupQueryOptions()]);

    if (id && update) {
      const record = await hospitalQcLedgerMaintainInfo(id);
      await formApi.setValues(record);
    } else {
      await formApi.setValues({
        parentId: parentId ?? 0,
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
