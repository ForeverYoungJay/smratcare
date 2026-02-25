<template>
  <PageContainer title="护理日志" subTitle="记录护理执行过程与质量">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词"><a-input v-model:value="query.keyword" placeholder="老人/内容/记录人" allow-clear /></a-form-item>
      <template #extra><a-button type="primary" @click="openCreate">新增日志</a-button></template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" title="护理日志" @ok="submit" :confirm-loading="saving" width="760px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="老人姓名" required><a-input v-model:value="form.elderName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="日志时间" required><a-date-picker v-model:value="form.logTime" show-time style="width: 100%" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="日志类型" required><a-select v-model:value="form.logType" :options="logTypeOptions" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="记录人"><a-input v-model:value="form.staffName" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="日志内容" required><a-textarea v-model:value="form.content" :rows="4" /></a-form-item>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="状态"><a-select v-model:value="form.status" :options="statusOptions" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="备注"><a-input v-model:value="form.remark" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="来源巡检ID"><a-input-number v-model:value="form.sourceInspectionId" :min="1" style="width: 100%" /></a-form-item>
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
import { getHealthNursingLogPage, createHealthNursingLog, updateHealthNursingLog, deleteHealthNursingLog } from '../../api/health'
import type { HealthNursingLog, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<HealthNursingLog[]>([])
const query = reactive({ keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '时间', dataIndex: 'logTime', key: 'logTime', width: 180 },
  { title: '类型', dataIndex: 'logType', key: 'logType', width: 120 },
  { title: '内容', dataIndex: 'content', key: 'content', width: 260 },
  { title: '记录人', dataIndex: 'staffName', key: 'staffName', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 120 }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  elderName: '',
  sourceInspectionId: undefined as number | undefined,
  logTime: dayjs(),
  logType: 'ROUTINE',
  content: '',
  staffName: '',
  status: 'PENDING',
  remark: ''
})
const logTypeOptions = [
  { label: '日常护理', value: 'ROUTINE' },
  { label: '巡检跟进', value: 'INSPECTION_FOLLOW_UP' },
  { label: '异常处理', value: 'INCIDENT' }
]
const statusOptions = [
  { label: '待处理', value: 'PENDING' },
  { label: '已完成', value: 'DONE' },
  { label: '已关闭', value: 'CLOSED' }
]

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HealthNursingLog> = await getHealthNursingLogPage(query)
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
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.elderName = ''
  form.sourceInspectionId = undefined
  form.logTime = dayjs()
  form.logType = 'ROUTINE'
  form.content = ''
  form.staffName = ''
  form.status = 'PENDING'
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: HealthNursingLog) {
  form.id = record.id
  form.elderName = record.elderName || ''
  form.sourceInspectionId = record.sourceInspectionId
  form.logTime = dayjs(record.logTime)
  form.logType = record.logType
  form.content = record.content
  form.staffName = record.staffName || ''
  form.status = record.status || 'PENDING'
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  if (!form.elderName || !form.logType || !form.content) {
    message.error('请补全必填项')
    return
  }
  saving.value = true
  try {
    const payload = {
      elderName: form.elderName,
      sourceInspectionId: form.sourceInspectionId,
      logTime: dayjs(form.logTime).format('YYYY-MM-DD HH:mm:ss'),
      logType: form.logType,
      content: form.content,
      staffName: form.staffName,
      status: form.status,
      remark: form.remark
    }
    if (form.id) {
      await updateHealthNursingLog(form.id, payload)
    } else {
      await createHealthNursingLog(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: HealthNursingLog) {
  await deleteHealthNursingLog(record.id)
  fetchData()
}

fetchData()
</script>
