<template>
  <PageContainer title="事故登记" subTitle="事故/意外记录与追踪">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="老人姓名">
        <a-input v-model:value="query.elderName" placeholder="请输入老人姓名" allow-clear />
      </a-form-item>
      <a-form-item label="事故类型">
        <a-input v-model:value="query.incidentType" placeholder="请输入事故类型" allow-clear />
      </a-form-item>
      <a-form-item label="值班护工">
        <a-input v-model:value="query.reporterName" placeholder="请输入值班护工" allow-clear />
      </a-form-item>
      <a-form-item label="事故等级">
        <a-select v-model:value="query.level" allow-clear style="width: 140px" placeholder="请选择事故等级">
          <a-select-option value="NORMAL">一般</a-select-option>
          <a-select-option value="MAJOR">重大</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="处理状态">
        <a-select v-model:value="query.status" allow-clear style="width: 140px" placeholder="请选择处理状态">
          <a-select-option value="OPEN">处理中</a-select-option>
          <a-select-option value="CLOSED">已关闭</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="发生时间">
        <a-range-picker
          v-model:value="query.incidentTimeRange"
          value-format="YYYY-MM-DDTHH:mm:ss"
          show-time
        />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="exportRows">导出</a-button>
          <a-button type="primary" @click="openCreate">新增事故</a-button>
        </a-space>
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
        <template v-if="column.key === 'level'">
          <a-tag :color="record.level === 'MAJOR' ? 'red' : 'blue'">
            {{ record.level === 'MAJOR' ? '重大' : '一般' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="record.status === 'CLOSED' ? 'green' : 'orange'">
            {{ record.status === 'CLOSED' ? '已关闭' : '处理中' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" @click="closeIncidentRow(record)">关闭</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" title="事故登记" @ok="submit" :confirm-loading="saving" width="640px">
      <a-form layout="vertical">
        <a-form-item label="老人" required>
          <a-select
            v-model:value="form.elderId"
            show-search
            :filter-option="false"
            :options="elderOptions"
            placeholder="请输入姓名搜索"
            @search="searchElders"
            @focus="() => !elderOptions.length && searchElders('')"
            @change="onElderChange"
          />
        </a-form-item>
        <a-form-item label="报告人" required>
          <a-input v-model:value="form.reporterName" />
        </a-form-item>
        <a-form-item label="发生时间" required>
          <a-date-picker v-model:value="form.incidentTime" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="事故类型" required>
          <a-input v-model:value="form.incidentType" />
        </a-form-item>
        <a-form-item label="等级">
          <a-select v-model:value="form.level" :options="levelOptions" />
        </a-form-item>
        <a-form-item label="事故描述" required>
          <a-textarea v-model:value="form.description" :rows="3" />
        </a-form-item>
        <a-form-item label="处理措施">
          <a-textarea v-model:value="form.actionTaken" :rows="2" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { getIncidentPage, createIncident, updateIncident, deleteIncident, closeIncident, exportIncidentPage } from '../../api/life'
import type { IncidentLevel, IncidentReport, IncidentStatus, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<IncidentReport[]>([])
const query = reactive({
  elderName: undefined as string | undefined,
  incidentType: undefined as string | undefined,
  reporterName: undefined as string | undefined,
  level: undefined as IncidentLevel | undefined,
  status: undefined as IncidentStatus | undefined,
  incidentTimeRange: undefined as [string, string] | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '事故类型', dataIndex: 'incidentType', key: 'incidentType', width: 140 },
  { title: '事故等级', dataIndex: 'level', key: 'level', width: 80 },
  { title: '值班护工', dataIndex: 'reporterName', key: 'reporterName', width: 120 },
  { title: '事故描述', dataIndex: 'description', key: 'description' },
  { title: '发生时间', dataIndex: 'incidentTime', key: 'incidentTime', width: 180 },
  { title: '处理状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 180 }
]

const editOpen = ref(false)
const saving = ref(false)
const { elderOptions, searchElders, findElderName, ensureSelectedElder } = useElderOptions({ pageSize: 50 })
const form = reactive({
  id: undefined as number | undefined,
  elderId: undefined as number | undefined,
  elderName: '',
  reporterName: '',
  incidentTime: dayjs(),
  incidentType: '',
  level: 'NORMAL',
  description: '',
  actionTaken: '',
  status: 'OPEN'
})

const levelOptions = [
  { label: '一般', value: 'NORMAL' },
  { label: '重大', value: 'MAJOR' }
]
const statusOptions = [
  { label: '处理中', value: 'OPEN' },
  { label: '已关闭', value: 'CLOSED' }
]

async function fetchData() {
  loading.value = true
  try {
    const [incidentTimeStart, incidentTimeEnd] = query.incidentTimeRange || []
    const res: PageResult<IncidentReport> = await getIncidentPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      elderName: query.elderName,
      incidentType: query.incidentType,
      reporterName: query.reporterName,
      level: query.level,
      status: query.status,
      incidentTimeStart,
      incidentTimeEnd
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
  query.elderName = undefined
  query.incidentType = undefined
  query.reporterName = undefined
  query.level = undefined
  query.status = undefined
  query.incidentTimeRange = undefined
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.elderId = undefined
  form.elderName = ''
  form.reporterName = ''
  form.incidentTime = dayjs()
  form.incidentType = ''
  form.level = 'NORMAL'
  form.description = ''
  form.actionTaken = ''
  form.status = 'OPEN'
  editOpen.value = true
}

function openEdit(record: IncidentReport) {
  form.id = record.id
  form.elderId = record.elderId
  form.elderName = record.elderName || ''
  ensureSelectedElder(record.elderId, record.elderName)
  form.reporterName = record.reporterName
  form.incidentTime = dayjs(record.incidentTime)
  form.incidentType = record.incidentType
  form.level = record.level || 'NORMAL'
  form.description = record.description
  form.actionTaken = record.actionTaken || ''
  form.status = record.status || 'OPEN'
  editOpen.value = true
}

function onElderChange(elderId?: number) {
  form.elderName = findElderName(elderId)
}

async function submit() {
  if (!form.elderId) {
    message.error('请选择老人')
    return
  }
  saving.value = true
  try {
    const payload = {
      elderId: form.elderId,
      elderName: findElderName(form.elderId) || form.elderName,
      reporterName: form.reporterName,
      incidentTime: dayjs(form.incidentTime).format('YYYY-MM-DDTHH:mm:ss'),
      incidentType: form.incidentType,
      level: form.level,
      description: form.description,
      actionTaken: form.actionTaken,
      status: form.status
    }
    if (form.id) {
      await updateIncident(form.id, payload)
    } else {
      await createIncident(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function closeIncidentRow(record: IncidentReport) {
  await closeIncident(record.id)
  fetchData()
}

async function remove(record: IncidentReport) {
  await deleteIncident(record.id)
  fetchData()
}

async function exportRows() {
  const [incidentTimeStart, incidentTimeEnd] = query.incidentTimeRange || []
  await exportIncidentPage({
    elderName: query.elderName,
    incidentType: query.incidentType,
    reporterName: query.reporterName,
    level: query.level,
    status: query.status,
    incidentTimeStart,
    incidentTimeEnd
  })
}

fetchData()
searchElders('')
</script>
