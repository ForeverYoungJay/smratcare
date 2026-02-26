<template>
  <PageContainer title="基础健康记录" subTitle="身高体重、血压等基础指标">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="老人姓名">
        <a-input v-model:value="query.keyword" placeholder="输入姓名" allow-clear />
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
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" title="健康记录" @ok="submit" :confirm-loading="saving" width="640px">
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
        <a-form-item label="记录日期" required>
          <a-date-picker v-model:value="form.recordDate" style="width: 100%" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="身高(cm)">
              <a-input-number v-model:value="form.heightCm" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="体重(kg)">
              <a-input-number v-model:value="form.weightKg" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="BMI">
              <a-input-number v-model:value="form.bmi" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="心率">
              <a-input-number v-model:value="form.heartRate" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="血压">
          <a-input v-model:value="form.bloodPressure" placeholder="120/80" />
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
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { getHealthBasicPage, createHealthBasic, updateHealthBasic, deleteHealthBasic } from '../../api/life'
import type { HealthBasicRecord, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<HealthBasicRecord[]>([])
const query = reactive({ keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '记录日期', dataIndex: 'recordDate', key: 'recordDate', width: 120 },
  { title: '身高(cm)', dataIndex: 'heightCm', key: 'heightCm', width: 100 },
  { title: '体重(kg)', dataIndex: 'weightKg', key: 'weightKg', width: 100 },
  { title: 'BMI', dataIndex: 'bmi', key: 'bmi', width: 80 },
  { title: '血压', dataIndex: 'bloodPressure', key: 'bloodPressure', width: 100 },
  { title: '心率', dataIndex: 'heartRate', key: 'heartRate', width: 80 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 120 }
]

const editOpen = ref(false)
const saving = ref(false)
const { elderOptions, searchElders, findElderName, ensureSelectedElder } = useElderOptions({ pageSize: 50 })
const form = reactive({
  id: undefined as number | undefined,
  elderId: undefined as number | undefined,
  elderName: '',
  recordDate: dayjs(),
  heightCm: undefined as number | undefined,
  weightKg: undefined as number | undefined,
  bmi: undefined as number | undefined,
  bloodPressure: '',
  heartRate: undefined as number | undefined,
  remark: ''
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HealthBasicRecord> = await getHealthBasicPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword
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
  query.keyword = ''
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.elderId = undefined
  form.elderName = ''
  form.recordDate = dayjs()
  form.heightCm = undefined
  form.weightKg = undefined
  form.bmi = undefined
  form.bloodPressure = ''
  form.heartRate = undefined
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: HealthBasicRecord) {
  form.id = record.id
  form.elderId = record.elderId
  form.elderName = record.elderName || ''
  ensureSelectedElder(record.elderId, record.elderName)
  form.recordDate = dayjs(record.recordDate)
  form.heightCm = record.heightCm
  form.weightKg = record.weightKg
  form.bmi = record.bmi
  form.bloodPressure = record.bloodPressure || ''
  form.heartRate = record.heartRate
  form.remark = record.remark || ''
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
  const payload = {
    elderId: form.elderId,
    elderName: findElderName(form.elderId) || form.elderName,
    recordDate: dayjs(form.recordDate).format('YYYY-MM-DD'),
    heightCm: form.heightCm,
    weightKg: form.weightKg,
    bmi: form.bmi,
    bloodPressure: form.bloodPressure,
    heartRate: form.heartRate,
    remark: form.remark
  }
  saving.value = true
  try {
    if (form.id) {
      await updateHealthBasic(form.id, payload)
    } else {
      await createHealthBasic(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: HealthBasicRecord) {
  await deleteHealthBasic(record.id)
  fetchData()
}

fetchData()
searchElders('')
</script>
