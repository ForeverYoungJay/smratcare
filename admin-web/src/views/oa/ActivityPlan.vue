<template>
  <PageContainer title="活动计划" subTitle="活动排期、组织安排与执行跟踪">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="计划标题/组织人/地点" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 160px" :options="statusOptions" />
      </a-form-item>
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openEdit()">新增计划</a-button>
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
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a @click="openEdit(record)">编辑</a>
            <a @click="remove(record)" style="color: #ff4d4f">删除</a>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑活动计划' : '新增活动计划'" @ok="submit" :confirm-loading="saving" width="760px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="计划标题" required>
              <a-input v-model:value="form.title" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="计划日期" required>
              <a-date-picker v-model:value="planDate" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始时间">
              <a-date-picker v-model:value="startTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束时间">
              <a-date-picker v-model:value="endTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="地点">
              <a-input v-model:value="form.location" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="组织人">
              <a-input v-model:value="form.organizer" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="参与对象">
          <a-input v-model:value="form.participantTarget" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="计划内容">
          <a-textarea v-model:value="form.content" :rows="4" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { createActivityPlan, deleteActivityPlan, getActivityPlanPage, updateActivityPlan } from '../../api/oa'
import type { OaActivityPlan, PageResult } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<OaActivityPlan[]>([])
const query = reactive({
  keyword: '',
  status: undefined as string | undefined,
  range: undefined as [Dayjs, Dayjs] | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '计划标题', dataIndex: 'title', key: 'title', width: 200 },
  { title: '计划日期', dataIndex: 'planDate', key: 'planDate', width: 120 },
  { title: '组织人', dataIndex: 'organizer', key: 'organizer', width: 120 },
  { title: '地点', dataIndex: 'location', key: 'location', width: 140 },
  { title: '参与对象', dataIndex: 'participantTarget', key: 'participantTarget', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '操作', key: 'action', width: 140 }
]

const statusOptions = [
  { label: '计划中', value: 'PLANNED' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'DONE' },
  { label: '已取消', value: 'CANCELLED' }
]

const editOpen = ref(false)
const planDate = ref<Dayjs | undefined>()
const startTime = ref<Dayjs | undefined>()
const endTime = ref<Dayjs | undefined>()
const form = reactive<Partial<OaActivityPlan>>({ status: 'PLANNED' })

function statusText(status?: string) {
  if (status === 'IN_PROGRESS') return '进行中'
  if (status === 'DONE') return '已完成'
  if (status === 'CANCELLED') return '已取消'
  return '计划中'
}

function statusColor(status?: string) {
  if (status === 'DONE') return 'green'
  if (status === 'IN_PROGRESS') return 'blue'
  if (status === 'CANCELLED') return 'default'
  return 'orange'
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { ...query }
    if (query.range) {
      params.dateFrom = query.range[0].format('YYYY-MM-DD')
      params.dateTo = query.range[1].format('YYYY-MM-DD')
    }
    delete params.range
    const res: PageResult<OaActivityPlan> = await getActivityPlanPage(params)
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
  query.keyword = ''
  query.status = undefined
  query.range = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openEdit(record?: OaActivityPlan) {
  Object.assign(form, record || { status: 'PLANNED', title: '', location: '', organizer: '', participantTarget: '', content: '', remark: '' })
  planDate.value = form.planDate ? dayjs(form.planDate) : undefined
  startTime.value = form.startTime ? dayjs(form.startTime) : undefined
  endTime.value = form.endTime ? dayjs(form.endTime) : undefined
  editOpen.value = true
}

async function submit() {
  if (!form.title || !planDate.value) return
  saving.value = true
  try {
    const payload = {
      ...form,
      planDate: planDate.value.format('YYYY-MM-DD'),
      startTime: startTime.value ? startTime.value.format('YYYY-MM-DDTHH:mm:ss') : undefined,
      endTime: endTime.value ? endTime.value.format('YYYY-MM-DDTHH:mm:ss') : undefined
    }
    if (form.id) {
      await updateActivityPlan(form.id, payload)
    } else {
      await createActivityPlan(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: OaActivityPlan) {
  await deleteActivityPlan(record.id)
  fetchData()
}

fetchData()
</script>
