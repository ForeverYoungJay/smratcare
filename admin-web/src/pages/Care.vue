<template>
  <PageContainer title="护理任务" subTitle="任务执行与异常监控">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="日期">
        <a-date-picker v-model:value="query.date" value-format="YYYY-MM-DD" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 160px">
          <a-select-option value="PENDING">待执行</a-select-option>
          <a-select-option value="DONE">已完成</a-select-option>
          <a-select-option value="EXCEPTION">异常</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="老人/房间" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="openGenerate">生成任务</a-button>
          <a-button type="primary" @click="generateNow">一键生成今日</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable
      rowKey="taskDailyId"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'staffId'">
          <a-tag v-if="record.staffId" color="blue">{{ record.staffId }}</a-tag>
          <span v-else>-</span>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="generateOpen" title="生成任务" @ok="submitGenerate">
      <a-form layout="vertical">
        <a-form-item label="日期" required>
          <a-date-picker v-model:value="generateForm.date" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="覆盖生成">
          <a-switch v-model:checked="generateForm.force" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../components/PageContainer.vue'
import SearchForm from '../components/SearchForm.vue'
import DataTable from '../components/DataTable.vue'
import { getTodayTasks, getTaskPage, generateTodayTasks } from '../api/care'
import type { CareTaskItem, ApiResult, PageResult } from '../types/api'

const query = reactive({
  date: undefined as string | undefined,
  status: undefined as string | undefined,
  keyword: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const loading = ref(false)
const rows = ref<CareTaskItem[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '房间', dataIndex: 'roomNo', key: 'roomNo', width: 120 },
  { title: '床位', dataIndex: 'bedId', key: 'bedId', width: 80 },
  { title: '护理员ID', dataIndex: 'staffId', key: 'staffId', width: 120 },
  { title: '任务', dataIndex: 'taskName', key: 'taskName' },
  { title: '计划时间', dataIndex: 'planTime', key: 'planTime', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 }
]

const generateOpen = ref(false)
const generateForm = reactive({ date: undefined as string | undefined, force: false })

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNo: query.pageNo, pageSize: query.pageSize }
    const res: ApiResult<PageResult<CareTaskItem>> = await getTaskPage(params)
    let list = res.data.records || []
    if (query.status) list = list.filter((r) => r.status === query.status)
    if (query.keyword) {
      list = list.filter((r) => String(r.elderName || '').includes(query.keyword!) || String(r.roomNo || '').includes(query.keyword!))
    }
    rows.value = list
    pagination.total = res.data.total || list.length
  } catch {
    rows.value = []
    pagination.total = 0
  } finally {
    loading.value = false
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
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function statusText(status: string) {
  if (status === 'DONE') return '已完成'
  if (status === 'EXCEPTION') return '异常'
  return '待执行'
}

function statusColor(status: string) {
  if (status === 'DONE') return 'green'
  if (status === 'EXCEPTION') return 'red'
  return 'blue'
}

function openGenerate() {
  generateOpen.value = true
}

async function generateNow() {
  try {
    await generateTodayTasks(undefined, true)
    message.success('已生成今日任务')
    fetchData()
  } catch {
    message.error('生成失败')
  }
}

async function submitGenerate() {
  try {
    await generateTodayTasks(generateForm.date, generateForm.force)
    message.success('已生成任务')
    generateOpen.value = false
    fetchData()
  } catch {
    message.error('生成失败')
  }
}

fetchData()
</script>
