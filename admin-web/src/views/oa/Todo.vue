<template>
  <PageContainer title="待办事项" subTitle="个人待办管理">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="标题/内容/负责人" allow-clear style="width: 240px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增待办</a-button>
        <a-button :disabled="!selectedSingleRecord || !isSelectedSingleOpen" @click="editSelected">编辑</a-button>
        <a-button :disabled="!selectedSingleRecord || !isSelectedSingleOpen" @click="doneSelected">完成</a-button>
        <a-button :disabled="!selectedSingleRecord" danger @click="removeSelected">删除</a-button>
        <a-button :disabled="selectedOpenCount === 0" @click="batchDone">批量完成</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchRemove">批量删除</a-button>
        <a-button @click="downloadExport">导出CSV</a-button>
        <span class="selection-tip">已勾选 {{ selectedRowKeys.length }} 条，批量完成仅对“待处理”生效</span>
      </template>
    </SearchForm>

    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 12px">
      <a-space wrap>
        <a-tag color="blue">代办新增/完成会自动同步到行政日历</a-tag>
        <a-form layout="inline">
          <a-form-item label="自动更新周期">
            <a-select v-model:value="policyForm.cycle" style="width: 180px">
              <a-select-option value="DAILY">每天</a-select-option>
              <a-select-option value="WEEKLY">每周</a-select-option>
              <a-select-option value="MONTHLY">每月</a-select-option>
              <a-select-option value="YEARLY">每年</a-select-option>
              <a-select-option value="MANUAL">手动</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="自动清理已完成">
            <a-switch v-model:checked="policyForm.autoClearDone" />
          </a-form-item>
          <a-form-item>
            <a-button @click="savePolicy">保存策略</a-button>
          </a-form-item>
          <a-form-item>
            <a-button danger @click="runPolicyNow">立即执行清理</a-button>
          </a-form-item>
        </a-form>
      </a-space>
    </a-card>

    <StatefulBlock :loading="summaryLoading" :error="summaryError" :empty="false" @retry="fetchData">
      <a-row :gutter="[12, 12]" style="margin-bottom: 12px">
        <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="总待办" :value="summary.totalCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="待处理" :value="summary.openCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="已完成" :value="summary.doneCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="今日到期" :value="summary.dueTodayCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="已逾期" :value="summary.overdueCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="未分配" :value="summary.unassignedCount || 0" /></a-card></a-col>
      </a-row>
      <a-alert
        v-if="(summary.overdueCount || 0) > 0"
        type="warning"
        show-icon
        style="margin-bottom: 12px"
        :message="`待办提醒：当前存在 ${summary.overdueCount || 0} 条逾期待办，请优先处理。`"
      />
    </StatefulBlock>

    <StatefulBlock :loading="loading" :error="tableError" :empty="!loading && !tableError && rows.length === 0" empty-text="暂无待办记录" @retry="fetchData">
      <DataTable
        rowKey="id"
        :row-selection="rowSelection"
        :columns="columns"
        :data-source="rows"
        :loading="false"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'DONE' ? 'green' : 'orange'">
              {{ record.status === 'DONE' ? '已完成' : '待处理' }}
            </a-tag>
          </template>
        </template>
      </DataTable>
    </StatefulBlock>

    <a-modal v-model:open="editOpen" title="待办" @ok="submit" :confirm-loading="saving" width="600px">
      <a-form layout="vertical">
        <a-form-item label="标题" required>
          <a-input v-model:value="form.title" />
        </a-form-item>
        <a-form-item label="内容">
          <a-textarea v-model:value="form.content" :rows="3" />
        </a-form-item>
        <a-form-item label="截止时间">
          <a-date-picker v-model:value="form.dueTime" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="负责人">
          <a-input v-model:value="form.assigneeName" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="editableStatusOptions" disabled />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import {
  getTodoSummary,
  getTodoPage,
  createTodo,
  updateTodo,
  completeTodo,
  deleteTodo,
  batchCompleteTodo,
  batchDeleteTodo,
  exportTodo
} from '../../api/oa'
import type { OaTodo, OaTodoSummary, PageResult } from '../../types'

const loading = ref(false)
const tableError = ref('')
const summaryLoading = ref(false)
const summaryError = ref('')
const rows = ref<OaTodo[]>([])
const query = reactive({ keyword: '', status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<string[]>([])
const summary = reactive<OaTodoSummary>({
  totalCount: 0,
  openCount: 0,
  doneCount: 0,
  dueTodayCount: 0,
  overdueCount: 0,
  unassignedCount: 0
})

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 200 },
  { title: '负责人', dataIndex: 'assigneeName', key: 'assigneeName', width: 120 },
  { title: '截止时间', dataIndex: 'dueTime', key: 'dueTime', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as string | undefined,
  title: '',
  content: '',
  dueTime: undefined as any,
  assigneeName: '',
  status: 'OPEN'
})

const statusOptions = [
  { label: '待处理', value: 'OPEN' },
  { label: '已完成', value: 'DONE' }
]
const editableStatusOptions = [{ label: '待处理', value: 'OPEN' }]
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
  }
}))
const selectedRecords = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(String(item.id))))
const selectedSingleRecord = computed(() => (selectedRecords.value.length === 1 ? selectedRecords.value[0] : null))
const isSelectedSingleOpen = computed(() => selectedSingleRecord.value?.status === 'OPEN')
const selectedOpenIds = computed(() => selectedRecords.value.filter((item) => item.status === 'OPEN').map((item) => String(item.id)))
const selectedOpenCount = computed(() => selectedOpenIds.value.length)
const policyStorageKey = 'oa-todo-policy-v1'
const policyForm = reactive({
  cycle: 'WEEKLY',
  autoClearDone: false
})

async function fetchData() {
  loading.value = true
  summaryLoading.value = true
  tableError.value = ''
  summaryError.value = ''
  try {
    const params = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status,
      keyword: query.keyword || undefined
    }
    const [res, sum] = await Promise.all([
      getTodoPage(params),
      getTodoSummary(params)
    ])
    rows.value = (res.list || []).map((item) => ({
      ...item,
      id: String(item.id)
    }))
    pagination.total = res.total || res.list.length
    selectedRowKeys.value = []
    Object.assign(summary, sum || {})
  } catch (error: any) {
    const text = error?.message || '加载失败，请稍后重试'
    tableError.value = text
    summaryError.value = text
  } finally {
    loading.value = false
    summaryLoading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.keyword = ''
  query.status = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.title = ''
  form.content = ''
  form.dueTime = undefined
  form.assigneeName = ''
  form.status = 'OPEN'
  editOpen.value = true
}

function openEdit(record: OaTodo) {
  form.id = String(record.id)
  form.title = record.title
  form.content = record.content || ''
  form.dueTime = record.dueTime ? dayjs(record.dueTime) : undefined
  form.assigneeName = record.assigneeName || ''
  form.status = record.status || 'OPEN'
  editOpen.value = true
}

async function submit() {
  const payload = {
    title: form.title,
    content: form.content,
    dueTime: form.dueTime ? dayjs(form.dueTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
    assigneeName: form.assigneeName,
    status: 'OPEN'
  }
  saving.value = true
  try {
    if (form.id) {
      await updateTodo(form.id, payload)
    } else {
      await createTodo(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function done(record: OaTodo) {
  if (record.status !== 'OPEN') return
  await completeTodo(String(record.id))
  fetchData()
}

async function remove(record: OaTodo) {
  await deleteTodo(String(record.id))
  fetchData()
}

function requireSingleSelection(action: string) {
  if (!selectedSingleRecord.value) {
    message.info(`请先勾选 1 条待办后再${action}`)
    return null
  }
  return selectedSingleRecord.value
}

function editSelected() {
  const record = requireSingleSelection('编辑')
  if (!record) return
  if (record.status !== 'OPEN') {
    message.warning('仅待处理待办可编辑')
    return
  }
  openEdit(record)
}

async function doneSelected() {
  const record = requireSingleSelection('完成')
  if (!record) return
  await done(record)
}

async function removeSelected() {
  const record = requireSingleSelection('删除')
  if (!record) return
  await remove(record)
}

async function batchDone() {
  if (selectedOpenIds.value.length === 0) {
    message.info('勾选项中没有“待处理”记录')
    return
  }
  const affected = await batchCompleteTodo(selectedOpenIds.value)
  message.success(`批量完成，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchDeleteTodo(selectedRowKeys.value)
  message.success(`批量删除，共处理 ${affected || 0} 条`)
  fetchData()
}

async function downloadExport() {
  const blob = await exportTodo({
    keyword: query.keyword || undefined,
    status: query.status
  })
  const href = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = href
  link.download = `oa-todo-${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(href)
}

function loadPolicy() {
  try {
    const saved = JSON.parse(localStorage.getItem(policyStorageKey) || '{}')
    if (saved?.cycle) policyForm.cycle = saved.cycle
    if (saved?.autoClearDone !== undefined) policyForm.autoClearDone = Boolean(saved.autoClearDone)
  } catch {
    policyForm.cycle = 'WEEKLY'
    policyForm.autoClearDone = false
  }
}

function savePolicy() {
  localStorage.setItem(policyStorageKey, JSON.stringify(policyForm))
  message.success('更新策略已保存')
}

function policyShouldRun(cycle: string) {
  if (cycle === 'MANUAL') return false
  const today = new Date()
  const markerKey = `${policyStorageKey}-last`
  const last = localStorage.getItem(markerKey)
  const marker = cycleMarker(cycle, today)
  if (last === marker) return false
  localStorage.setItem(markerKey, marker)
  return true
}

function cycleMarker(cycle: string, date: Date) {
  const year = date.getFullYear()
  if (cycle === 'YEARLY') return `${year}`
  if (cycle === 'MONTHLY') return `${year}-${String(date.getMonth() + 1).padStart(2, '0')}`
  if (cycle === 'WEEKLY') {
    const jan1 = new Date(year, 0, 1)
    const days = Math.floor((date.getTime() - jan1.getTime()) / (24 * 3600 * 1000))
    const week = Math.ceil((days + jan1.getDay() + 1) / 7)
    return `${year}-W${week}`
  }
  return `${year}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

async function clearDoneTodos() {
  const donePage: PageResult<OaTodo> = await getTodoPage({ pageNo: 1, pageSize: 1000, status: 'DONE' })
  const ids = (donePage.list || []).map((item) => item.id).filter(Boolean)
  if (!ids.length) {
    message.info('暂无可清理的已完成待办')
    return
  }
  const affected = await batchDeleteTodo(ids)
  message.success(`已清理 ${affected || 0} 条已完成待办`)
  await fetchData()
}

async function runPolicyNow() {
  if (!policyForm.autoClearDone) {
    message.info('当前未开启自动清理，已跳过')
    return
  }
  await clearDoneTodos()
}

onMounted(async () => {
  loadPolicy()
  await fetchData()
  if (policyForm.autoClearDone && policyShouldRun(policyForm.cycle)) {
    await clearDoneTodos()
  }
})
</script>

<style scoped>
.selection-tip {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
</style>
