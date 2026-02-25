<template>
  <PageContainer title="健康巡检" subTitle="日常巡检项目与整改闭环">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词"><a-input v-model:value="query.keyword" placeholder="老人/项目/巡检人" allow-clear /></a-form-item>
      <a-form-item label="状态"><a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 180px" /></a-form-item>
      <template #extra><a-button type="primary" @click="openCreate">新增巡检</a-button></template>
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

    <a-modal v-model:open="editOpen" title="健康巡检" @ok="submit" :confirm-loading="saving" width="760px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="老人姓名" required><a-input v-model:value="form.elderName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="巡检日期" required><a-date-picker v-model:value="form.inspectionDate" style="width: 100%" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="巡检项目" required><a-input v-model:value="form.inspectionItem" /></a-form-item>
        <a-form-item label="结果"><a-textarea v-model:value="form.result" :rows="2" /></a-form-item>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="状态"><a-select v-model:value="form.status" :options="statusOptions" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="巡检人"><a-input v-model:value="form.inspectorName" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="跟进措施"><a-input v-model:value="form.followUpAction" /></a-form-item>
        <a-form-item label="备注"><a-input v-model:value="form.remark" /></a-form-item>
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
import { getHealthInspectionPage, createHealthInspection, updateHealthInspection, deleteHealthInspection } from '../../api/health'
import type { HealthInspection, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<HealthInspection[]>([])
const query = reactive({ keyword: '', status: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '日期', dataIndex: 'inspectionDate', key: 'inspectionDate', width: 120 },
  { title: '项目', dataIndex: 'inspectionItem', key: 'inspectionItem', width: 150 },
  { title: '结果', dataIndex: 'result', key: 'result', width: 200 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '巡检人', dataIndex: 'inspectorName', key: 'inspectorName', width: 120 },
  { title: '跟进措施', dataIndex: 'followUpAction', key: 'followUpAction' },
  { title: '操作', key: 'action', width: 120 }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  elderName: '',
  inspectionDate: dayjs(),
  inspectionItem: '',
  result: '',
  status: '',
  inspectorName: '',
  followUpAction: '',
  remark: ''
})
const statusOptions = [
  { label: '正常', value: 'NORMAL' },
  { label: '异常', value: 'ABNORMAL' },
  { label: '跟进中', value: 'FOLLOWING' },
  { label: '已关闭', value: 'CLOSED' }
]

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HealthInspection> = await getHealthInspectionPage(query)
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
  query.status = ''
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.elderName = ''
  form.inspectionDate = dayjs()
  form.inspectionItem = ''
  form.result = ''
  form.status = 'NORMAL'
  form.inspectorName = ''
  form.followUpAction = ''
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: HealthInspection) {
  form.id = record.id
  form.elderName = record.elderName || ''
  form.inspectionDate = dayjs(record.inspectionDate)
  form.inspectionItem = record.inspectionItem
  form.result = record.result || ''
  form.status = record.status || 'NORMAL'
  form.inspectorName = record.inspectorName || ''
  form.followUpAction = record.followUpAction || ''
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  if (!form.elderName || !form.inspectionItem) {
    message.error('请补全必填项')
    return
  }
  saving.value = true
  try {
    const payload = {
      elderName: form.elderName,
      inspectionDate: dayjs(form.inspectionDate).format('YYYY-MM-DD'),
      inspectionItem: form.inspectionItem,
      result: form.result,
      status: form.status,
      inspectorName: form.inspectorName,
      followUpAction: form.followUpAction,
      remark: form.remark
    }
    if (form.id) {
      await updateHealthInspection(form.id, payload)
    } else {
      await createHealthInspection(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: HealthInspection) {
  await deleteHealthInspection(record.id)
  fetchData()
}

fetchData()
</script>
