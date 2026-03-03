<script setup lang="ts">
import type { HospitalQcSqlTestResult } from '#/api/data-center/hospital-qc/ledger-query/model';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { $t } from '@vben/locales';
import { addFullName, cloneDeep, getPopupContainer, listToTree } from '@vben/utils';

import { useVbenForm } from '#/adapter/form';
import {
  hospitalQcLedgerQueryAdd,
  hospitalQcLedgerQueryInfo,
  hospitalQcLedgerQueryTestSql,
  hospitalQcLedgerQueryUpdate,
} from '#/api/data-center/hospital-qc/ledger-query';
import { hospitalQcDatasourceOptions } from '#/api/data-center/hospital-qc/datasource';
import { deptList } from '#/api/system/dept';
import { defaultFormValueGetter, useBeforeCloseDiff } from '#/utils/popup';

import { queryModalSchema } from './data';

import { message } from 'ant-design-vue';
import dayjs from 'dayjs';

const emit = defineEmits<{ reload: [] }>();

const isUpdate = ref(false);
const testResult = ref<HospitalQcSqlTestResult>();

const title = computed(() => {
  return isUpdate.value ? $t('pages.common.edit') : $t('pages.common.add');
});

const deptNameMap = new Map<number, string>();

const [BasicForm, formApi] = useVbenForm({
  commonConfig: {
    labelWidth: 92,
  },
  schema: queryModalSchema(),
  showDefaultActions: false,
  wrapperClass: 'grid-cols-2',
});

const sampleColumns = computed(() => {
  return (testResult.value?.columns ?? []).map((col) => ({
    title: col,
    dataIndex: col,
    key: col,
    ellipsis: true,
    width: 180,
  }));
});

function toOptionalNumber(value: unknown) {
  if (value === null || value === undefined || value === '') {
    return undefined;
  }
  const parsed = Number(value);
  return Number.isNaN(parsed) ? undefined : parsed;
}

function formatSqlText(sql?: string) {
  if (!sql) {
    return sql;
  }
  return sql
    .replace(/\s+/g, ' ')
    .replace(/\b(select|from|where|group by|order by|left join|right join|inner join|outer join|and|or)\b/gi, (keyword) => {
      const upper = keyword.toUpperCase();
      return ['AND', 'OR'].includes(upper) ? `\n  ${upper}` : `\n${upper}`;
    })
    .trim();
}

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

async function setupDatasourceSelect() {
  const options = await hospitalQcDatasourceOptions();
  formApi.updateSchema([
    {
      componentProps: {
        options: options.map((item) => ({
          label: item.label,
          value: Number(item.value),
        })),
        onChange: (value: number | string | undefined) => {
          formApi.setFieldValue('datasourceId', toOptionalNumber(value));
        },
      },
      fieldName: 'datasourceId',
    },
  ]);
}

const { onBeforeClose, markInitialized, resetInitialized } = useBeforeCloseDiff({
  initializedGetter: defaultFormValueGetter(formApi),
  currentGetter: defaultFormValueGetter(formApi),
});

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
    testResult.value = undefined;

    const { id } = modalApi.getData() as { id?: number };
    isUpdate.value = !!id;

    await Promise.all([setupDeptSelect(), setupDatasourceSelect()]);

    if (id) {
      const record = await hospitalQcLedgerQueryInfo(id);
      await formApi.setValues({
        ...record,
        id: toOptionalNumber(record.id),
        deptId: toOptionalNumber(record.deptId),
        datasourceId: toOptionalNumber(record.datasourceId),
        status: toOptionalNumber(record.status) ?? 0,
      });
    } else {
      await formApi.setValues({
        status: 0,
      });
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
    const payload = {
      ...data,
      id: toOptionalNumber(data.id),
      deptId: toOptionalNumber(data.deptId),
      datasourceId: toOptionalNumber(data.datasourceId),
      status: toOptionalNumber(data.status) ?? 0,
    };
    await (isUpdate.value
      ? hospitalQcLedgerQueryUpdate(payload)
      : hospitalQcLedgerQueryAdd(payload));
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
  testResult.value = undefined;
  resetInitialized();
}

async function handleFormatSql() {
  const values = await formApi.getValues();
  await formApi.setValues({
    countSql: formatSqlText(values.countSql),
    detailSql: formatSqlText(values.detailSql),
  });
}

async function handleTestSql() {
  const values = cloneDeep(await formApi.getValues());
  const data = {
    ...values,
    deptId: toOptionalNumber(values.deptId),
    datasourceId: toOptionalNumber(values.datasourceId),
    startTime: dayjs().startOf('quarter').format('YYYY-MM-DD HH:mm:ss'),
    endTime: dayjs().endOf('quarter').format('YYYY-MM-DD HH:mm:ss'),
  };
  const result = await hospitalQcLedgerQueryTestSql(data);
  testResult.value = result;
  if (result.success) {
    message.success(result.message ?? 'SQL 测试成功');
  } else {
    message.error(result.message ?? 'SQL 测试失败');
  }
}
</script>

<template>
  <BasicModal :title="title" class="w-[980px]">
    <div class="mb-3 flex gap-2">
      <a-button @click="handleFormatSql">格式化 SQL</a-button>
      <a-button
        type="primary"
        v-access:code="['data-center:hospital-qc:ledger-query:test']"
        @click="handleTestSql"
      >
        测试 SQL
      </a-button>
    </div>

    <BasicForm />

    <div v-if="testResult" class="mt-3">
      <a-alert
        :type="testResult.success ? 'success' : 'error'"
        :message="`${testResult.message ?? ''}，耗时 ${testResult.elapsedMs ?? 0}ms，计数 ${testResult.countValue ?? 0}`"
        show-icon
      />
      <a-table
        v-if="(testResult.sampleRows?.length ?? 0) > 0"
        :columns="sampleColumns"
        :data-source="testResult.sampleRows"
        :pagination="false"
        class="mt-2"
        size="small"
        bordered
        :scroll="{ x: 900, y: 240 }"
      />
    </div>
  </BasicModal>
</template>
