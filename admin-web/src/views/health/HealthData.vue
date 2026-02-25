<template>
  <PageContainer title="健康数据" subTitle="采集血压、血糖、体温等数据">
    <a-row :gutter="16" class="summary-row">
      <a-col :xs="24" :md="6">
        <a-card :bordered="false">
          <a-statistic title="记录总数" :value="summary.totalCount" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="6">
        <a-card :bordered="false">
          <a-statistic title="异常记录" :value="summary.abnormalCount" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="6">
        <a-card :bordered="false">
          <a-statistic title="正常记录" :value="summary.normalCount" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="6">
        <a-card :bordered="false">
          <a-statistic title="异常率" :value="summary.abnormalRate" suffix="%" :precision="1" />
        </a-card>
      </a-col>
    </a-row>

    <a-card :bordered="false" class="type-card">
      <a-space wrap>
        <a-tag v-for="item in summary.typeStats" :key="item.dataType" color="blue">
          {{ formatType(item.dataType) }}：{{ item.abnormalCount }}/{{ item.totalCount }}
        </a-tag>
        <a-tag v-if="!summary.typeStats.length" color="default">暂无分类统计</a-tag>
      </a-space>
    </a-card>

    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词"><a-input v-model:value="query.keyword" placeholder="老人/数值" allow-clear /></a-form-item>
      <a-form-item label="类型">
        <a-select v-model:value="query.dataType" :options="dataTypeOptions" allow-clear placeholder="选择类型" style="width: 180px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.abnormalFlag" :options="abnormalOptions" allow-clear placeholder="全部状态" style="width: 140px" />
      </a-form-item>
      <a-form-item label="时间">
        <a-range-picker v-model:value="query.measuredRange" show-time style="width: 320px" />
      </a-form-item>
      <template #extra><a-button type="primary" @click="openCreate">新增数据</a-button></template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'dataType'">
          {{ formatType(record.dataType) }}
        </template>
        <template v-else-if="column.key === 'measuredAt'">
          {{ formatDateTime(record.measuredAt) }}
        </template>
        <template v-else-if="column.key === 'abnormalFlag'">
          <a-tag :color="record.abnormalFlag ? 'red' : 'green'">{{ record.abnormalFlag ? '异常' : '正常' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确认删除该记录吗？" ok-text="确认" cancel-text="取消" @confirm="remove(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
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
import {
  getHealthDataRecordPage,
  getHealthDataSummary,
  createHealthDataRecord,
  updateHealthDataRecord,
  deleteHealthDataRecord
} from '../../api/health'
import type { HealthDataRecord, HealthDataSummary, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<HealthDataRecord[]>([])
const query = reactive({
  keyword: '',
  dataType: '',
  abnormalFlag: undefined as number | undefined,
  measuredRange: [] as any[],
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const summary = reactive<HealthDataSummary>({
  totalCount: 0,
  abnormalCount: 0,
  normalCount: 0,
  abnormalRate: 0,
  typeStats: []
})

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
const abnormalOptions = [
  { label: '正常', value: 0 },
  { label: '异常', value: 1 }
]

async function fetchData() {
  loading.value = true
  try {
    const params = buildQueryParams()
    const [res, summaryRes] = await Promise.all([
      getHealthDataRecordPage(params) as Promise<PageResult<HealthDataRecord>>,
      getHealthDataSummary(params) as Promise<HealthDataSummary>
    ])
    rows.value = res.list
    pagination.total = res.total || 0
    summary.totalCount = summaryRes.totalCount || 0
    summary.abnormalCount = summaryRes.abnormalCount || 0
    summary.normalCount = summaryRes.normalCount || 0
    summary.abnormalRate = summaryRes.abnormalRate || 0
    summary.latestMeasuredAt = summaryRes.latestMeasuredAt
    summary.typeStats = summaryRes.typeStats || []
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
  query.abnormalFlag = undefined
  query.measuredRange = []
  query.pageNo = 1
  query.pageSize = pagination.pageSize
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
  form.measuredAt = record.measuredAt ? dayjs(record.measuredAt) : dayjs()
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
  const validateMessage = validateDataValue(form.dataType, form.dataValue)
  if (validateMessage) {
    message.error(validateMessage)
    return
  }
  saving.value = true
  try {
    const measuredAt = dayjs(form.measuredAt).format('YYYY-MM-DD HH:mm:ss')
    const recommendedAbnormal = recommendAbnormalFlag(form.dataType, form.dataValue)
    const payload = {
      elderName: form.elderName,
      dataType: form.dataType,
      dataValue: form.dataValue,
      measuredAt,
      source: form.source,
      abnormalFlag: typeof recommendedAbnormal === 'number' ? recommendedAbnormal : (form.abnormalFlag ? 1 : 0),
      remark: form.remark
    }
    if (form.id) {
      await updateHealthDataRecord(form.id, payload)
    } else {
      await createHealthDataRecord(payload)
    }
    message.success('保存成功')
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: HealthDataRecord) {
  await deleteHealthDataRecord(record.id)
  message.success('删除成功')
  fetchData()
}

function buildQueryParams() {
  const params: Record<string, any> = {
    keyword: query.keyword || undefined,
    dataType: query.dataType || undefined,
    abnormalFlag: typeof query.abnormalFlag === 'number' ? query.abnormalFlag : undefined,
    pageNo: query.pageNo,
    pageSize: query.pageSize
  }
  if (Array.isArray(query.measuredRange) && query.measuredRange.length === 2) {
    params.measuredFrom = dayjs(query.measuredRange[0]).format('YYYY-MM-DD HH:mm:ss')
    params.measuredTo = dayjs(query.measuredRange[1]).format('YYYY-MM-DD HH:mm:ss')
  }
  return params
}

function formatType(type?: string) {
  if (!type) return '-'
  const item = dataTypeOptions.find((entry) => entry.value === type)
  return item?.label || type
}

function formatDateTime(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'
}

function validateDataValue(dataType: string, value: string) {
  if (dataType === 'BP') {
    if (!/^\d{2,3}\/\d{2,3}$/.test(value)) {
      return '血压请使用“收缩压/舒张压”格式，例如 120/80'
    }
    return ''
  }
  if (dataType === 'OTHER') {
    return ''
  }
  const numeric = Number(value)
  if (Number.isNaN(numeric)) {
    return '该类型数据值需为数字'
  }
  return ''
}

function recommendAbnormalFlag(dataType: string, value: string) {
  if (dataType === 'BP') {
    const parts = value.split('/')
    if (parts.length !== 2) return undefined
    const high = Number(parts[0])
    const low = Number(parts[1])
    if (Number.isNaN(high) || Number.isNaN(low)) return undefined
    return high >= 140 || low >= 90 || high < 90 || low < 60 ? 1 : 0
  }
  const numeric = Number(value)
  if (Number.isNaN(numeric)) return undefined
  if (dataType === 'HR') return numeric < 60 || numeric > 100 ? 1 : 0
  if (dataType === 'TEMP') return numeric < 36 || numeric > 37.3 ? 1 : 0
  if (dataType === 'SPO2') return numeric < 95 ? 1 : 0
  if (dataType === 'GLUCOSE') return numeric < 3.9 || numeric > 10 ? 1 : 0
  return undefined
}

fetchData()
</script>

<style scoped>
.summary-row {
  margin-bottom: 12px;
}
.type-card {
  margin-bottom: 12px;
}
</style>
