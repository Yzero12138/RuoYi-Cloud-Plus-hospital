<script setup lang="ts">
import type { FieldMappingItem, HospitalQcSqlTestResult } from '#/api/data-center/hospital-qc/ledger-query/model';

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

import { Input, message, Switch, Table, Tooltip } from 'ant-design-vue';
import dayjs from 'dayjs';

const emit = defineEmits<{ reload: [] }>();

const isUpdate = ref(false);
const testResult = ref<HospitalQcSqlTestResult>();
const fieldMappings = ref<FieldMappingItem[]>([]);

const hasTestColumns = computed(() => (testResult.value?.columns?.length ?? 0) > 0);

const fieldMappingColumns = [
  { title: '原始字段名（SQL 列名）', dataIndex: 'field', width: '35%' },
  { title: '展示名称', dataIndex: 'label', width: '35%' },
  { title: '展示', dataIndex: 'visible', width: '15%', align: 'center' as const },
  { title: '操作', dataIndex: 'action', width: '15%', align: 'center' as const },
];

function importFromTestResult() {
  const cols = testResult.value?.columns ?? [];
  if (cols.length === 0) return;
  const existingFields = new Set(fieldMappings.value.map((m) => m.field));
  const newMappings = cols
    .filter((col) => !existingFields.has(col))
    .map((col) => ({ field: col, label: col, visible: true }));
  fieldMappings.value = [...fieldMappings.value, ...newMappings];
}

function addMappingRow() {
  fieldMappings.value.push({ field: '', label: '', visible: true });
}

function removeMappingRow(index: number) {
  fieldMappings.value.splice(index, 1);
}

const title = computed(() => {
  return isUpdate.value ? $t('pages.common.edit') : $t('pages.common.add');
});

const deptNameMap = new Map<number, string>();

const [BasicForm, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
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
      if (record.detailFieldMapping) {
        try {
          fieldMappings.value = JSON.parse(record.detailFieldMapping);
        } catch {
          fieldMappings.value = [];
        }
      } else {
        fieldMappings.value = [];
      }
    } else {
      await formApi.setValues({
        status: 0,
      });
      fieldMappings.value = [];
    }

    await markInitialized();
    modalApi.modalLoading(false);
  },
});

async function handleConfirm() {
  try {
    modalApi.lock(true);
    // Serialize field mappings into hidden form field before validation
    const mappingJson = fieldMappings.value.length > 0
      ? JSON.stringify(fieldMappings.value)
      : null;
    await formApi.setFieldValue('detailFieldMapping', mappingJson);
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
  fieldMappings.value = [];
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
    startTime: dayjs().startOf('month' as any).format('YYYY-MM-DD HH:mm:ss'),
    endTime: dayjs().endOf('month' as any).format('YYYY-MM-DD HH:mm:ss'),
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

    <!-- 明细字段映射配置 -->
    <div class="mt-4 rounded border border-gray-200 p-3">
      <div class="mb-2 flex items-center justify-between">
        <span class="text-sm font-medium">明细字段映射配置</span>
        <div class="flex gap-2">
          <Tooltip title="先执行「测试 SQL」，再点此按钮导入列名">
            <a-button size="small" :disabled="!hasTestColumns" @click="importFromTestResult">
              从测试结果导入字段
            </a-button>
          </Tooltip>
          <a-button size="small" @click="addMappingRow">+ 添加字段</a-button>
        </div>
      </div>

      <div v-if="fieldMappings.length === 0" class="py-3 text-center text-sm text-gray-400">
        未配置字段映射，台账明细将使用 SQL 原始列名展示
      </div>

      <Table
        v-else
        :columns="fieldMappingColumns"
        :data-source="fieldMappings"
        :pagination="false"
        size="small"
        bordered
        :row-key="(_r: any, idx: number) => idx"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.dataIndex === 'field'">
            <Input v-model:value="record.field" size="small" placeholder="如 patient_id" />
          </template>
          <template v-else-if="column.dataIndex === 'label'">
            <Input v-model:value="record.label" size="small" placeholder="如 患者ID" />
          </template>
          <template v-else-if="column.dataIndex === 'visible'">
            <Switch v-model:checked="record.visible" size="small" />
          </template>
          <template v-else-if="column.dataIndex === 'action'">
            <a-button type="link" danger size="small" @click="removeMappingRow(index)">删除</a-button>
          </template>
        </template>
      </Table>
    </div>
  </BasicModal>
</template>

<style scoped lang="scss">
:deep(.ant-table-cell) {
  padding: 4px 8px !important;
}
</style>
