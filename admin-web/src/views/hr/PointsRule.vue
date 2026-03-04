<template>
  <PageContainer title="积分规则" subTitle="按护理模板/评分/异常配置">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <template #extra>
        <a-button type="primary" @click="openDrawer()">新增规则</a-button>
        <a-button :disabled="!selectedSingleRecord" @click="editSelected">编辑</a-button>
        <a-button :disabled="!canEnableSingle" @click="enableSelected">启用</a-button>
        <a-button :disabled="!canDisableSingle" @click="disableSelected">停用</a-button>
        <a-button :disabled="!selectedSingleRecord" danger @click="removeSelected">删除</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" @click="batchEnable">批量启用</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" @click="batchDisable">批量停用</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchRemove">批量删除</a-button>
        <span class="selection-tip">已勾选 {{ selectedRowKeys.length }} 条</span>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :row-selection="rowSelection"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'default'">
            {{ record.status === 1 ? '启用' : '停用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'templateName'">
          <span>{{ record.templateName || '默认规则' }}</span>
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="drawerOpen" :title="drawerTitle" width="520">
      <a-form :model="form" layout="vertical">
        <a-form-item label="护理模板">
          <a-select
            v-model:value="form.templateId"
            allow-clear
            :options="templateOptions"
            placeholder="留空表示默认规则"
          />
        </a-form-item>
        <a-form-item label="基础积分">
          <a-input-number v-model:value="form.basePoints" style="width: 100%" />
        </a-form-item>
        <a-form-item label="评分权重">
          <a-input-number v-model:value="form.scoreWeight" style="width: 100%" :step="0.1" />
        </a-form-item>
        <a-form-item label="可疑扣减">
          <a-input-number v-model:value="form.suspiciousPenalty" style="width: 100%" />
        </a-form-item>
        <a-form-item label="失败积分">
          <a-input-number v-model:value="form.failPoints" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="3" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="drawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="submit">保存</a-button>
        </a-space>
      </template>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getPointsRulePage, upsertPointsRule, deletePointsRule } from '../../api/hr'
import { getTemplatePage } from '../../api/care'
import type { StaffPointsRule, CareTaskTemplate, PageResult } from '../../types'

const query = reactive({ pageNo: 1, pageSize: 10 })
const rows = ref<StaffPointsRule[]>([])
const loading = ref(false)
const saving = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<string[]>([])

const columns = [
  { title: '模板', dataIndex: 'templateName', key: 'templateName', width: 180 },
  { title: '基础积分', dataIndex: 'basePoints', key: 'basePoints', width: 120 },
  { title: '评分权重', dataIndex: 'scoreWeight', key: 'scoreWeight', width: 120 },
  { title: '可疑扣减', dataIndex: 'suspiciousPenalty', key: 'suspiciousPenalty', width: 120 },
  { title: '失败积分', dataIndex: 'failPoints', key: 'failPoints', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 }
]

const drawerOpen = ref(false)
const form = reactive<Partial<StaffPointsRule>>({ status: 1 })
const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]
const templates = ref<CareTaskTemplate[]>([])
const templateOptions = computed(() =>
  templates.value.map((t) => ({ label: t.taskName, value: String(t.id) }))
)

const drawerTitle = computed(() => (form.id ? '编辑规则' : '新增规则'))
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
  }
}))
const selectedRecords = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(String(item.id))))
const selectedSingleRecord = computed(() => (selectedRecords.value.length === 1 ? selectedRecords.value[0] : null))
const canEnableSingle = computed(() => !!selectedSingleRecord.value && Number(selectedSingleRecord.value.status) !== 1)
const canDisableSingle = computed(() => !!selectedSingleRecord.value && Number(selectedSingleRecord.value.status) === 1)

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<StaffPointsRule> = await getPointsRulePage(query)
    rows.value = (res.list || []).map((item) => ({
      ...item,
      id: String(item.id),
      templateId: item.templateId == null ? item.templateId : String(item.templateId)
    }))
    pagination.total = res.total || res.list.length
    selectedRowKeys.value = []
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

async function loadTemplates() {
  const res: PageResult<CareTaskTemplate> = await getTemplatePage({ pageNo: 1, pageSize: 200 })
  templates.value = res.list
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openDrawer(record?: StaffPointsRule) {
  Object.keys(form).forEach((k) => ((form as any)[k] = undefined))
  Object.assign(form, record || { status: 1 })
  drawerOpen.value = true
}

async function submit() {
  saving.value = true
  try {
    await upsertPointsRule({
      ...form,
      id: form.id == null ? form.id : String(form.id),
      templateId: form.templateId == null || form.templateId === '' ? null : String(form.templateId)
    })
    message.success('保存成功')
    drawerOpen.value = false
    fetchData()
  } catch {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function remove(record: StaffPointsRule) {
  if (!record.id) return
  try {
    await deletePointsRule(String(record.id))
    message.success('删除成功')
    fetchData()
  } catch {
    message.error('删除失败')
  }
}

function requireSingleSelection(action: string) {
  if (!selectedSingleRecord.value) {
    message.info(`请先勾选 1 条规则后再${action}`)
    return null
  }
  return selectedSingleRecord.value
}

function editSelected() {
  const record = requireSingleSelection('编辑')
  if (!record) return
  openDrawer(record)
}

async function toggleStatus(record: StaffPointsRule, status: number) {
  await upsertPointsRule({
    ...record,
    id: String(record.id),
    templateId: record.templateId == null || record.templateId === '' ? null : String(record.templateId),
    status
  })
}

async function enableSelected() {
  const record = requireSingleSelection('启用')
  if (!record) return
  if (Number(record.status) === 1) {
    message.info('所选规则已是启用状态')
    return
  }
  try {
    await toggleStatus(record, 1)
    message.success('启用成功')
    fetchData()
  } catch {
    message.error('启用失败')
  }
}

async function disableSelected() {
  const record = requireSingleSelection('停用')
  if (!record) return
  if (Number(record.status) !== 1) {
    message.info('所选规则已是停用状态')
    return
  }
  try {
    await toggleStatus(record, 0)
    message.success('停用成功')
    fetchData()
  } catch {
    message.error('停用失败')
  }
}

async function removeSelected() {
  const record = requireSingleSelection('删除')
  if (!record) return
  Modal.confirm({
    title: '确认删除积分规则？',
    content: `将删除规则「${record.templateName || '未命名规则'}」，删除后不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      await remove(record)
    }
  })
}

async function batchEnable() {
  if (selectedRowKeys.value.length === 0) return
  const validRecords = selectedRecords.value.filter((item) => Number(item.status) !== 1)
  if (validRecords.length === 0) {
    message.info('勾选项中没有可启用规则')
    return
  }
  try {
    await Promise.all(validRecords.map((item) => toggleStatus(item, 1)))
    message.success(`批量启用成功，共处理 ${validRecords.length} 条`)
    fetchData()
  } catch {
    message.error('批量启用失败')
  }
}

async function batchDisable() {
  if (selectedRowKeys.value.length === 0) return
  const validRecords = selectedRecords.value.filter((item) => Number(item.status) === 1)
  if (validRecords.length === 0) {
    message.info('勾选项中没有可停用规则')
    return
  }
  try {
    await Promise.all(validRecords.map((item) => toggleStatus(item, 0)))
    message.success(`批量停用成功，共处理 ${validRecords.length} 条`)
    fetchData()
  } catch {
    message.error('批量停用失败')
  }
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  Modal.confirm({
    title: '确认批量删除积分规则？',
    content: `将删除 ${selectedRecords.value.length} 条规则，删除后不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await Promise.all(selectedRecords.value.map((item) => deletePointsRule(String(item.id))))
        message.success(`批量删除成功，共处理 ${selectedRecords.value.length} 条`)
        fetchData()
      } catch {
        message.error('批量删除失败')
      }
    }
  })
}

loadTemplates()
fetchData()
</script>

<style scoped>
.selection-tip {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
</style>
