<template>
  <PageContainer title="活动管理" subTitle="院内活动安排与记录">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="活动名称/组织者" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增活动</a-button>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 'DONE' ? 'green' : record.status === 'CANCELLED' ? 'red' : 'blue'">
            {{ statusLabel(record.status) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" title="活动" @ok="submit" :confirm-loading="saving" width="640px">
      <a-form layout="vertical">
        <a-form-item label="活动名称" required>
          <a-input v-model:value="form.title" />
        </a-form-item>
        <a-form-item label="日期" required>
          <a-date-picker v-model:value="form.eventDate" style="width: 100%" />
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
        <a-form-item label="地点">
          <a-input v-model:value="form.location" />
        </a-form-item>
        <a-form-item label="组织者">
          <a-input v-model:value="form.organizer" />
        </a-form-item>
        <a-form-item label="内容">
          <a-textarea v-model:value="form.content" :rows="3" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getActivityPage, createActivity, updateActivity, deleteActivity } from '../../api/life'
import type { ActivityEvent, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<ActivityEvent[]>([])
const query = reactive({
  range: undefined as [Dayjs, Dayjs] | undefined,
  keyword: '',
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '日期', dataIndex: 'eventDate', key: 'eventDate', width: 120 },
  { title: '活动名称', dataIndex: 'title', key: 'title', width: 160 },
  { title: '组织者', dataIndex: 'organizer', key: 'organizer', width: 120 },
  { title: '地点', dataIndex: 'location', key: 'location', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 140 }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  title: '',
  eventDate: dayjs(),
  startTime: undefined as Dayjs | undefined,
  endTime: undefined as Dayjs | undefined,
  location: '',
  organizer: '',
  content: '',
  status: 'PLANNED',
  remark: ''
})
const statusOptions = [
  { label: '计划中', value: 'PLANNED' },
  { label: '已完成', value: 'DONE' },
  { label: '已取消', value: 'CANCELLED' }
]

function statusLabel(status?: string) {
  if (status === 'DONE') return '已完成'
  if (status === 'CANCELLED') return '已取消'
  return '计划中'
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword
    }
    if (query.range) {
      params.dateFrom = query.range[0].format('YYYY-MM-DD')
      params.dateTo = query.range[1].format('YYYY-MM-DD')
    }
    const res: PageResult<ActivityEvent> = await getActivityPage(params)
    rows.value = res.list
    pagination.total = res.total || res.list.length
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
  query.range = undefined
  query.keyword = ''
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.title = ''
  form.eventDate = dayjs()
  form.startTime = undefined
  form.endTime = undefined
  form.location = ''
  form.organizer = ''
  form.content = ''
  form.status = 'PLANNED'
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: ActivityEvent) {
  form.id = record.id
  form.title = record.title
  form.eventDate = dayjs(record.eventDate)
  form.startTime = record.startTime ? dayjs(record.startTime) : undefined
  form.endTime = record.endTime ? dayjs(record.endTime) : undefined
  form.location = record.location || ''
  form.organizer = record.organizer || ''
  form.content = record.content || ''
  form.status = record.status || 'PLANNED'
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  const payload = {
    title: form.title,
    eventDate: dayjs(form.eventDate).format('YYYY-MM-DD'),
    startTime: form.startTime ? dayjs(form.startTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
    endTime: form.endTime ? dayjs(form.endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
    location: form.location,
    organizer: form.organizer,
    content: form.content,
    status: form.status,
    remark: form.remark
  }
  saving.value = true
  try {
    if (form.id) {
      await updateActivity(form.id, payload)
    } else {
      await createActivity(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: ActivityEvent) {
  await deleteActivity(record.id)
  fetchData()
}

fetchData()
</script>
