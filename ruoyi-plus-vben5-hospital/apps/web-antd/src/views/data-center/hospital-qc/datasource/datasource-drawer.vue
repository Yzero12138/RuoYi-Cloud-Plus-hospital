<script setup lang="ts">
import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { $t } from '@vben/locales';
import { cloneDeep } from '@vben/utils';

import { useVbenForm } from '#/adapter/form';
import {
  hospitalQcDatasourceAdd,
  hospitalQcDatasourceInfo,
  hospitalQcDatasourceTest,
  hospitalQcDatasourceUpdate,
} from '#/api/data-center/hospital-qc/datasource';
import { defaultFormValueGetter, useBeforeCloseDiff } from '#/utils/popup';

import { drawerSchema } from './data';

import { message } from 'ant-design-vue';

interface DrawerProps {
  id?: number;
  update: boolean;
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
    labelWidth: 96,
  },
  schema: drawerSchema(),
  showDefaultActions: false,
  wrapperClass: 'grid-cols-2',
});

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

    const { id, update } = drawerApi.getData() as DrawerProps;
    isUpdate.value = update;

    if (id && update) {
      const record = await hospitalQcDatasourceInfo(id);
      await formApi.setValues(record);
    } else {
      await formApi.setValues({
        port: 3306,
        sourceType: 'MYSQL',
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
    if (isUpdate.value && !data.password) {
      delete data.password;
    }
    await (isUpdate.value
      ? hospitalQcDatasourceUpdate(data)
      : hospitalQcDatasourceAdd(data));
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

async function handleTestConnection() {
  const values = cloneDeep(await formApi.getValues());
  const result = await hospitalQcDatasourceTest(values);
  if (result.success) {
    message.success(`${result.message}（${result.elapsedMs}ms）`);
  } else {
    message.error(`${result.message}（${result.elapsedMs}ms）`);
  }
}
</script>

<template>
  <BasicDrawer :title="title" class="w-[720px]">
    <template #append-footer>
      <a-button
        v-access:code="['data-center:hospital-qc:datasource:test']"
        @click="handleTestConnection"
      >
        测试连接
      </a-button>
    </template>
    <BasicForm />
  </BasicDrawer>
</template>
