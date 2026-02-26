<script setup lang="ts">
import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { $t } from '@vben/locales';
import { addFullName, cloneDeep, getPopupContainer, listToTree } from '@vben/utils';

import { useVbenForm } from '#/adapter/form';
import {
  nephrologyLedgerQueryAdd,
  nephrologyLedgerQueryInfo,
  nephrologyLedgerQueryUpdate,
} from '#/api/data-center/nephrology/ledger-query';
import { deptList } from '#/api/system/dept';
import { defaultFormValueGetter, useBeforeCloseDiff } from '#/utils/popup';

import { modalSchema } from './data';

const emit = defineEmits<{ reload: [] }>();

const isUpdate = ref(false);
const title = computed(() => {
  return isUpdate.value ? $t('pages.common.edit') : $t('pages.common.add');
});

const deptNameMap = new Map<number, string>();

const [BasicForm, formApi] = useVbenForm({
  commonConfig: {
    labelWidth: 90,
  },
  schema: modalSchema(),
  showDefaultActions: false,
  wrapperClass: 'grid-cols-2',
});

async function setupDeptSelect() {
  const deptArray = await deptList({});
  const treeData = listToTree(deptArray, { id: 'deptId', pid: 'parentId' });
  addFullName(treeData, 'deptName', ' / ');
  deptNameMap.clear();
  const fillMap = (nodes?: any[]) => {
    if (!nodes) {
      return;
    }
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

const { onBeforeClose, markInitialized, resetInitialized } = useBeforeCloseDiff(
  {
    initializedGetter: defaultFormValueGetter(formApi),
    currentGetter: defaultFormValueGetter(formApi),
  },
);

const [BasicModal, modalApi] = useVbenModal({
  fullscreenButton: false,
  onBeforeClose,
  onClosed: handleClosed,
  onConfirm: handleConfirm,
  onOpenChange: async (isOpen) => {
    if (!isOpen) {
      return null;
    }
    modalApi.modalLoading(true);

    const { id } = modalApi.getData() as { id?: number | string };
    isUpdate.value = !!id;

    await setupDeptSelect();
    if (isUpdate.value && id) {
      const record = await nephrologyLedgerQueryInfo(id);
      await formApi.setValues(record);
    }
    await markInitialized();

    modalApi.modalLoading(false);
  },
});

async function handleConfirm() {
  try {
    modalApi.lock(true);
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    const data = cloneDeep(await formApi.getValues());
    await (isUpdate.value
      ? nephrologyLedgerQueryUpdate(data)
      : nephrologyLedgerQueryAdd(data));
    resetInitialized();
    emit('reload');
    modalApi.close();
  } catch (error) {
    console.error(error);
  } finally {
    modalApi.lock(false);
  }
}

async function handleClosed() {
  await formApi.resetForm();
  resetInitialized();
}
</script>

<template>
  <BasicModal :title="title" class="w-[900px]">
    <BasicForm />
  </BasicModal>
</template>

