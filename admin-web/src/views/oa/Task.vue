<template>
  <PageContainer title="日程与任务" subTitle="任务列表与日历视图">
    <a-tabs v-model:activeKey="activeKey">
      <a-tab-pane key="list" tab="任务列表">
        <SearchForm :model="query" @search="fetchData" @reset="onReset">
          <a-form-item label="关键字">
            <a-input v-model:value="query.keyword" placeholder="标题/描述/负责人" allow-clear style="width: 240px" />
          </a-form-item>
          <a-form-item label="状态">
            <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
          </a-form-item>
          <template #extra>
            <a-button type="primary" @click="openCreate">新增任务</a-button>
            <a-button :disabled="selectedRowKeys.length === 0" @click="batchDone">批量完成</a-button>
            <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchRemove">批量删除</a-button>
            <a-button @click="downloadExport">导出CSV</a-button>
          </template>
        </SearchForm>

        <StatefulBlock :loading="summaryLoading" :error="summaryError" :empty="false" @retry="fetchData">
          <a-row :gutter="[12, 12]" style="margin-bottom: 12px">
            <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="总任务" :value="summary.totalCount || 0" /></a-card></a-col>
            <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="进行中" :value="summary.openCount || 0" /></a-card></a-col>
            <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="已完成" :value="summary.doneCount || 0" /></a-card></a-col>
            <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="高优先级" :value="summary.highPriorityCount || 0" /></a-card></a-col>
            <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="今日到期" :value="summary.dueTodayCount || 0" /></a-card></a-col>
            <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="已逾期" :value="summary.overdueCount || 0" /></a-card></a-col>
          </a-row>
          <a-alert
            v-if="(summary.overdueCount || 0) > 0 || (summary.highPriorityCount || 0) > 0"
            type="warning"
            show-icon
            style="margin-bottom: 12px"
            :message="`任务提醒：逾期 ${summary.overdueCount || 0} 条，高优先级 ${summary.highPriorityCount || 0} 条。`"
          />
        </StatefulBlock>

        <StatefulBlock :loading="loading" :error="tableError" :empty="!loading && !tableError && rows.length === 0" empty-text="暂无任务记录" @retry="fetchData">
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
                  {{ record.status === 'DONE' ? '已完成' : '进行中' }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'priority'">
                <a-tag :color="record.priority === 'HIGH' ? 'red' : record.priority === 'LOW' ? 'blue' : 'gold'">
                  {{ priorityLabel(record.priority) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" :disabled="record.status !== 'OPEN'" @click="openEdit(record)">编辑</a-button>
                  <a-button type="link" :disabled="record.status !== 'OPEN'" @click="done(record)">完成</a-button>
                  <a-button type="link" danger @click="remove(record)">删除</a-button>
                </a-space>
              </template>
            </template>
          </DataTable>
        </StatefulBlock>
      </a-tab-pane>
      <a-tab-pane key="calendar" tab="日历视图">
        <a-card class="card-elevated" :bordered="false">
          <FullCalendar :options="calendarOptions" />
        </a-card>
      </a-tab-pane>
    </a-tabs>

    <a-modal v-model:open="editOpen" title="任务" @ok="submit" :confirm-loading="saving" width="640px">
      <a-form layout="vertical">
        <a-form-item label="标题" required>
          <a-input v-model:value="form.title" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="form.description" :rows="3" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始时间">
              <a-date-picker v-model:value="form.startTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束时间">
              <a-date-picker v-model:value="form.endTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="负责人">
              <a-input v-model:value="form.assigneeName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="优先级">
              <a-select v-model:value="form.priority" :options="priorityOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="editableStatusOptions" disabled />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import {
  getOaTaskSummary,
  getOaTaskPage,
  createOaTask,
  updateOaTask,
  completeOaTask,
  deleteOaTask,
  batchCompleteOaTask,
  batchDeleteOaTask,
  exportOaTask
} from '../../api/oa'
import type { OaTask, OaTaskSummary, PageResult } from '../../types'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'

const activeKey = ref('list')
const loading = ref(false)
const tableError = ref('')
const summaryLoading = ref(false)
const summaryError = ref('')
const rows = ref<OaTask[]>([])
const query = reactive({ keyword: '', status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<number[]>([])
const summary = reactive<OaTaskSummary>({
  totalCount: 0,
  openCount: 0,
  doneCount: 0,
  highPriorityCount: 0,
  dueTodayCount: 0,
  overdueCount: 0,
  unassignedCount: 0
})

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 200 },
  { title: '负责人', dataIndex: 'assigneeName', key: 'assigneeName', width: 120 },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 160 },
  { title: '结束时间', dataIndex: 'endTime', key: 'endTime', width: 160 },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 180 }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  title: '',
  description: '',
  startTime: undefined as any,
  endTime: undefined as any,
  assigneeName: '',
  priority: 'NORMAL',
  status: 'OPEN'
})

const statusOptions = [
  { label: '进行中', value: 'OPEN' },
  { label: '已完成', value: 'DONE' }
]
const editableStatusOptions = [{ label: '进行中', value: 'OPEN' }]
const priorityOptions = [
  { label: '低', value: 'LOW' },
  { label: '普通', value: 'NORMAL' },
  { label: '高', value: 'HIGH' }
]

function priorityLabel(priority?: string) {
  if (priority === 'HIGH') return '高'
  if (priority === 'LOW') return '低'
  return '普通'
}

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  events: rows.value.map((task) => ({
    title: task.title,
    date: task.startTime ? task.startTime.slice(0, 10) : undefined
  }))
}))
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => Number(item))
  }
}))

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
      getOaTaskPage(params),
      getOaTaskSummary(params)
    ])
    rows.value = res.list
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
  form.description = ''
  form.startTime = undefined
  form.endTime = undefined
  form.assigneeName = ''
  form.priority = 'NORMAL'
  form.status = 'OPEN'
  editOpen.value = true
}

function openEdit(record: OaTask) {
  form.id = record.id
  form.title = record.title
  form.description = record.description || ''
  form.startTime = record.startTime ? dayjs(record.startTime) : undefined
  form.endTime = record.endTime ? dayjs(record.endTime) : undefined
  form.assigneeName = record.assigneeName || ''
  form.priority = record.priority || 'NORMAL'
  form.status = record.status || 'OPEN'
  editOpen.value = true
}

async function submit() {
  const payload = {
    title: form.title,
    description: form.description,
    startTime: form.startTime ? dayjs(form.startTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
    endTime: form.endTime ? dayjs(form.endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
    assigneeName: form.assigneeName,
    priority: form.priority,
    status: 'OPEN'
  }
  saving.value = true
  try {
    if (form.id) {
      await updateOaTask(form.id, payload)
    } else {
      await createOaTask(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function done(record: OaTask) {
  if (record.status !== 'OPEN') return
  await completeOaTask(record.id)
  fetchData()
}

async function remove(record: OaTask) {
  await deleteOaTask(record.id)
  fetchData()
}

async function batchDone() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchCompleteOaTask(selectedRowKeys.value)
  message.success(`批量完成，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchDeleteOaTask(selectedRowKeys.value)
  message.success(`批量删除，共处理 ${affected || 0} 条`)
  fetchData()
}

async function downloadExport() {
  const blob = await exportOaTask({
    keyword: query.keyword || undefined,
    status: query.status
  })
  const href = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = href
  link.download = `oa-task-${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(href)
}

fetchData()
</script>
