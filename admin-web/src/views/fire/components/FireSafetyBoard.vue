<template>
  <PageContainer :title="title" :subTitle="subTitle">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="标题/区域/问题描述/处置措施" allow-clear style="width: 300px" />
      </a-form-item>
      <a-form-item label="负责人">
        <a-input v-model:value="query.inspectorName" placeholder="请输入检查/值班人" allow-clear style="width: 220px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 140px" />
      </a-form-item>
      <a-form-item label="检查时间">
        <a-range-picker v-model:value="query.checkTimeRange" value-format="YYYY-MM-DDTHH:mm:ss" show-time />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增记录</a-button>
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
          <a-tag :color="record.status === 'CLOSED' ? 'green' : 'orange'">
            {{ record.status === 'CLOSED' ? '已关闭' : '处理中' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" :disabled="record.status === 'CLOSED'" @click="closeRecord(record)">关闭</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal
      v-model:open="editOpen"
      :title="form.id ? `编辑${title}` : `新增${title}`"
      @ok="submit"
      :confirm-loading="saving"
      width="720px"
    >
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="标题" required>
              <a-input v-model:value="form.title" placeholder="请输入标题" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="区域/位置">
              <a-input v-model:value="form.location" placeholder="如：A栋1层配电间" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="负责人" required>
              <a-input v-model:value="form.inspectorName" placeholder="请输入检查/值班人" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="检查时间" required>
              <a-date-picker v-model:value="form.checkTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="问题描述">
          <a-textarea v-model:value="form.issueDescription" :rows="3" />
        </a-form-item>
        <a-form-item label="处置措施">
          <a-textarea v-model:value="form.actionTaken" :rows="3" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="下次检查日期">
              <a-date-picker v-model:value="form.nextCheckDate" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="form.status" :options="statusOptions" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import dayjs, { Dayjs } from 'dayjs'
import PageContainer from '../../../components/PageContainer.vue'
import SearchForm from '../../../components/SearchForm.vue'
import DataTable from '../../../components/DataTable.vue'
import {
  closeFireSafetyRecord,
  createFireSafetyRecord,
  deleteFireSafetyRecord,
  getFireSafetyRecordPage,
  updateFireSafetyRecord
} from '../../../api/fire'
import type { FireSafetyRecord, FireSafetyRecordType, FireSafetyStatus, PageResult } from '../../../types'

const props = defineProps<{
  title: string
  subTitle: string
  recordType: FireSafetyRecordType
}>()

const loading = ref(false)
const rows = ref<FireSafetyRecord[]>([])
const query = reactive({
  keyword: undefined as string | undefined,
  inspectorName: undefined as string | undefined,
  status: undefined as FireSafetyStatus | undefined,
  checkTimeRange: undefined as [string, string] | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 220 },
  { title: '区域/位置', dataIndex: 'location', key: 'location', width: 180 },
  { title: '负责人', dataIndex: 'inspectorName', key: 'inspectorName', width: 120 },
  { title: '检查时间', dataIndex: 'checkTime', key: 'checkTime', width: 180 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '问题描述', dataIndex: 'issueDescription', key: 'issueDescription' },
  { title: '操作', key: 'action', width: 180 }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  title: '',
  location: '',
  inspectorName: '',
  checkTime: dayjs(),
  status: 'OPEN' as FireSafetyStatus,
  issueDescription: '',
  actionTaken: '',
  nextCheckDate: undefined as Dayjs | undefined
})

const statusOptions = [
  { label: '处理中', value: 'OPEN' },
  { label: '已关闭', value: 'CLOSED' }
]

async function fetchData() {
  loading.value = true
  try {
    const [checkTimeStart, checkTimeEnd] = query.checkTimeRange || []
    const res: PageResult<FireSafetyRecord> = await getFireSafetyRecordPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      recordType: props.recordType,
      inspectorName: query.inspectorName,
      status: query.status,
      checkTimeStart,
      checkTimeEnd
    })
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
  query.keyword = undefined
  query.inspectorName = undefined
  query.status = undefined
  query.checkTimeRange = undefined
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.title = ''
  form.location = ''
  form.inspectorName = ''
  form.checkTime = dayjs()
  form.status = 'OPEN'
  form.issueDescription = ''
  form.actionTaken = ''
  form.nextCheckDate = undefined
  editOpen.value = true
}

function openEdit(record: FireSafetyRecord) {
  form.id = record.id
  form.title = record.title
  form.location = record.location || ''
  form.inspectorName = record.inspectorName
  form.checkTime = dayjs(record.checkTime)
  form.status = record.status
  form.issueDescription = record.issueDescription || ''
  form.actionTaken = record.actionTaken || ''
  form.nextCheckDate = record.nextCheckDate ? dayjs(record.nextCheckDate) : undefined
  editOpen.value = true
}

async function submit() {
  const payload = {
    recordType: props.recordType,
    title: form.title,
    location: form.location || undefined,
    inspectorName: form.inspectorName,
    checkTime: form.checkTime ? dayjs(form.checkTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
    status: form.status,
    issueDescription: form.issueDescription || undefined,
    actionTaken: form.actionTaken || undefined,
    nextCheckDate: form.nextCheckDate ? dayjs(form.nextCheckDate).format('YYYY-MM-DD') : undefined
  }
  saving.value = true
  try {
    if (form.id) {
      await updateFireSafetyRecord(form.id, payload)
    } else {
      await createFireSafetyRecord(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function closeRecord(record: FireSafetyRecord) {
  await closeFireSafetyRecord(record.id)
  fetchData()
}

async function remove(record: FireSafetyRecord) {
  await deleteFireSafetyRecord(record.id)
  fetchData()
}

fetchData()
</script>
