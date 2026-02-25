<template>
  <PageContainer title="用药设置" subTitle="维护老人用药方案与阈值">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="老人/药品" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增设置</a-button>
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

    <a-modal v-model:open="editOpen" title="用药设置" @ok="submit" :confirm-loading="saving" width="720px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="老人姓名" required><a-input v-model:value="form.elderName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="药品名称" required><a-input v-model:value="form.drugName" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8"><a-form-item label="剂量"><a-input v-model:value="form.dosage" /></a-form-item></a-col>
          <a-col :span="8">
            <a-form-item label="频次">
              <a-select v-model:value="form.frequency" :options="frequencyOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="8"><a-form-item label="用药时段"><a-input v-model:value="form.medicationTime" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="开始日期"><a-date-picker v-model:value="form.startDate" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="结束日期"><a-date-picker v-model:value="form.endDate" style="width: 100%" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="最小剩余阈值"><a-input-number v-model:value="form.minRemainQty" :min="0" style="width: 100%" /></a-form-item>
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
  getHealthMedicationSettingPage,
  createHealthMedicationSetting,
  updateHealthMedicationSetting,
  deleteHealthMedicationSetting
} from '../../api/health'
import type { HealthMedicationSetting, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<HealthMedicationSetting[]>([])
const query = reactive({ keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '药品', dataIndex: 'drugName', key: 'drugName', width: 150 },
  { title: '剂量', dataIndex: 'dosage', key: 'dosage', width: 100 },
  { title: '频次', dataIndex: 'frequency', key: 'frequency', width: 100 },
  { title: '用药时段', dataIndex: 'medicationTime', key: 'medicationTime', width: 120 },
  { title: '起止日期', key: 'period', width: 220, customRender: ({ record }: any) => `${record.startDate || '-'} ~ ${record.endDate || '-'}` },
  { title: '最小阈值', dataIndex: 'minRemainQty', key: 'minRemainQty', width: 100 },
  { title: '操作', key: 'action', width: 120 }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  elderName: '',
  drugName: '',
  dosage: '',
  frequency: 'QD',
  medicationTime: '',
  startDate: undefined as any,
  endDate: undefined as any,
  minRemainQty: undefined as number | undefined,
  remark: ''
})
const frequencyOptions = [
  { label: '每日一次(QD)', value: 'QD' },
  { label: '每日两次(BID)', value: 'BID' },
  { label: '每日三次(TID)', value: 'TID' },
  { label: '每日四次(QID)', value: 'QID' },
  { label: '按需(PRN)', value: 'PRN' },
  { label: '自定义(CUSTOM)', value: 'CUSTOM' }
]

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HealthMedicationSetting> = await getHealthMedicationSettingPage(query)
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
  form.dosage = ''
  form.frequency = 'QD'
  form.medicationTime = ''
  form.startDate = undefined
  form.endDate = undefined
  form.minRemainQty = undefined
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: HealthMedicationSetting) {
  form.id = record.id
  form.elderName = record.elderName || ''
  form.drugName = record.drugName
  form.dosage = record.dosage || ''
  form.frequency = record.frequency || 'QD'
  form.medicationTime = record.medicationTime || ''
  form.startDate = record.startDate ? dayjs(record.startDate) : undefined
  form.endDate = record.endDate ? dayjs(record.endDate) : undefined
  form.minRemainQty = record.minRemainQty
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  if (!form.elderName || !form.drugName) {
    message.error('请填写老人和药品')
    return
  }
  if (!form.frequency) {
    message.error('请选择频次')
    return
  }
  if (form.frequency === 'CUSTOM' && !form.medicationTime.trim()) {
    message.error('自定义频次时请填写用药时段，格式如 08:00,20:00')
    return
  }
  if (form.minRemainQty != null && form.minRemainQty < 0) {
    message.error('最小剩余阈值不能小于0')
    return
  }
  saving.value = true
  try {
    const payload = {
      elderName: form.elderName,
      drugName: form.drugName,
      dosage: form.dosage,
      frequency: form.frequency,
      medicationTime: form.medicationTime,
      startDate: form.startDate ? dayjs(form.startDate).format('YYYY-MM-DD') : undefined,
      endDate: form.endDate ? dayjs(form.endDate).format('YYYY-MM-DD') : undefined,
      minRemainQty: form.minRemainQty,
      remark: form.remark
    }
    if (form.id) {
      await updateHealthMedicationSetting(form.id, payload)
    } else {
      await createHealthMedicationSetting(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: HealthMedicationSetting) {
  await deleteHealthMedicationSetting(record.id)
  fetchData()
}

fetchData()
</script>
