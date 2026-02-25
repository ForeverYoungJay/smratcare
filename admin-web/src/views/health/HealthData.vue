<template>
  <PageContainer title="健康数据" subTitle="采集血压、血糖、体温等数据">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词"><a-input v-model:value="query.keyword" placeholder="老人/数值" allow-clear /></a-form-item>
      <a-form-item label="类型">
        <a-select v-model:value="query.dataType" :options="dataTypeOptions" allow-clear placeholder="选择类型" style="width: 180px" />
      </a-form-item>
      <template #extra><a-button type="primary" @click="openCreate">新增数据</a-button></template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'abnormalFlag'">
          <a-tag :color="record.abnormalFlag ? 'red' : 'green'">{{ record.abnormalFlag ? '异常' : '正常' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" title="健康数据" @ok="submit" :confirm-loading="saving" width="680px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="老人姓名" required><a-input v-model:value="form.elderName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="数据类型" required><a-select v-model:value="form.dataType" :options="dataTypeOptions" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="数据值" required><a-input v-model:value="form.dataValue" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="采集时间" required><a-date-picker v-model:value="form.measuredAt" show-time style="width: 100%" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="来源"><a-input v-model:value="form.source" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="异常标记"><a-switch v-model:checked="form.abnormalFlag" /></a-form-item></a-col>
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
import { getHealthDataRecordPage, createHealthDataRecord, updateHealthDataRecord, deleteHealthDataRecord } from '../../api/health'
import type { HealthDataRecord, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<HealthDataRecord[]>([])
const query = reactive({ keyword: '', dataType: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '类型', dataIndex: 'dataType', key: 'dataType', width: 120 },
  { title: '数据值', dataIndex: 'dataValue', key: 'dataValue', width: 140 },
  { title: '采集时间', dataIndex: 'measuredAt', key: 'measuredAt', width: 180 },
  { title: '来源', dataIndex: 'source', key: 'source', width: 120 },
  { title: '状态', key: 'abnormalFlag', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 120 }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  elderName: '',
  dataType: '',
  dataValue: '',
  measuredAt: dayjs(),
  source: '',
  abnormalFlag: false,
  remark: ''
})
const dataTypeOptions = [
  { label: '血压(BP)', value: 'BP' },
  { label: '心率(HR)', value: 'HR' },
  { label: '体温(TEMP)', value: 'TEMP' },
  { label: '血氧(SPO2)', value: 'SPO2' },
  { label: '血糖(GLUCOSE)', value: 'GLUCOSE' },
  { label: '体重(WEIGHT)', value: 'WEIGHT' },
  { label: '其他(OTHER)', value: 'OTHER' }
]

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HealthDataRecord> = await getHealthDataRecordPage(query)
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
  query.dataType = ''
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.elderName = ''
  form.dataType = ''
  form.dataValue = ''
  form.measuredAt = dayjs()
  form.source = ''
  form.abnormalFlag = false
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: HealthDataRecord) {
  form.id = record.id
  form.elderName = record.elderName || ''
  form.dataType = record.dataType
  form.dataValue = record.dataValue
  form.measuredAt = dayjs(record.measuredAt)
  form.source = record.source || ''
  form.abnormalFlag = !!record.abnormalFlag
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  if (!form.elderName || !form.dataType || !form.dataValue) {
    message.error('请补全必填项')
    return
  }
  saving.value = true
  try {
    const payload = {
      elderName: form.elderName,
      dataType: form.dataType,
      dataValue: form.dataValue,
      measuredAt: dayjs(form.measuredAt).format('YYYY-MM-DD HH:mm:ss'),
      source: form.source,
      abnormalFlag: form.abnormalFlag ? 1 : 0,
      remark: form.remark
    }
    if (form.id) {
      await updateHealthDataRecord(form.id, payload)
    } else {
      await createHealthDataRecord(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: HealthDataRecord) {
  await deleteHealthDataRecord(record.id)
  fetchData()
}

fetchData()
</script>
