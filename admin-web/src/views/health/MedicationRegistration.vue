<template>
  <PageContainer title="用药登记" subTitle="记录每次执行用药">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="老人/药品/护士" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增登记</a-button>
      </template>
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

    <a-modal v-model:open="editOpen" title="用药登记" @ok="submit" :confirm-loading="saving" width="680px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="老人姓名" required><a-input v-model:value="form.elderName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="药品名称" required><a-input v-model:value="form.drugName" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="登记时间" required><a-date-picker v-model:value="form.registerTime" show-time style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="执行护士"><a-input v-model:value="form.nurseName" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="用药量" required><a-input-number v-model:value="form.dosageTaken" :min="0" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="单位"><a-input v-model:value="form.unit" /></a-form-item></a-col>
        </a-row>
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
import {
  getHealthMedicationRegistrationPage,
  createHealthMedicationRegistration,
  updateHealthMedicationRegistration,
  deleteHealthMedicationRegistration
} from '../../api/health'
import type { HealthMedicationRegistration, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<HealthMedicationRegistration[]>([])
const query = reactive({ keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '药品', dataIndex: 'drugName', key: 'drugName', width: 150 },
  { title: '登记时间', dataIndex: 'registerTime', key: 'registerTime', width: 180 },
  { title: '用药量', dataIndex: 'dosageTaken', key: 'dosageTaken', width: 100 },
  { title: '单位', dataIndex: 'unit', key: 'unit', width: 80 },
  { title: '护士', dataIndex: 'nurseName', key: 'nurseName', width: 120 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 120 }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  elderName: '',
  drugName: '',
  registerTime: dayjs(),
  dosageTaken: 0 as number | undefined,
  unit: '',
  nurseName: '',
  remark: ''
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HealthMedicationRegistration> = await getHealthMedicationRegistrationPage(query)
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
  form.drugName = ''
  form.registerTime = dayjs()
  form.dosageTaken = 0
  form.unit = ''
  form.nurseName = ''
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: HealthMedicationRegistration) {
  form.id = record.id
  form.elderName = record.elderName || ''
  form.drugName = record.drugName
  form.registerTime = dayjs(record.registerTime)
  form.dosageTaken = record.dosageTaken
  form.unit = record.unit || ''
  form.nurseName = record.nurseName || ''
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  if (!form.elderName || !form.drugName) {
    message.error('请填写老人和药品')
    return
  }
  if (!form.dosageTaken || form.dosageTaken <= 0) {
    message.error('用药量必须大于0')
    return
  }
  saving.value = true
  try {
    const payload = {
      elderName: form.elderName,
      drugName: form.drugName,
      registerTime: dayjs(form.registerTime).format('YYYY-MM-DD HH:mm:ss'),
      dosageTaken: form.dosageTaken,
      unit: form.unit,
      nurseName: form.nurseName,
      remark: form.remark
    }
    if (form.id) {
      await updateHealthMedicationRegistration(form.id, payload)
    } else {
      await createHealthMedicationRegistration(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: HealthMedicationRegistration) {
  await deleteHealthMedicationRegistration(record.id)
  fetchData()
}

fetchData()
</script>
