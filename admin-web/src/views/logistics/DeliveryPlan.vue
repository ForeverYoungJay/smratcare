<template>
  <PageContainer title="送餐计划" subTitle="查看送餐执行状态与未送达清单">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="送餐状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="日期">
        <a-date-picker v-model:value="query.date" value-format="YYYY-MM-DD" />
      </a-form-item>
    </SearchForm>

    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 12px">
      <a-row :gutter="12">
        <a-col :span="8"><a-statistic title="总任务" :value="summary.total" /></a-col>
        <a-col :span="8"><a-statistic title="已送达" :value="summary.delivered" /></a-col>
        <a-col :span="8"><a-statistic title="未送达" :value="summary.undelivered" /></a-col>
      </a-row>
    </a-card>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 'DELIVERED' ? 'green' : record.status === 'FAILED' ? 'red' : 'gold'">
            {{ statusLabel(record.status) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'failureReason'">
          {{ record.failureReason || '-' }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="markDelivered(record)" v-if="record.status !== 'DELIVERED'">标记送达</a-button>
            <a-button type="link" danger @click="openFail(record)" v-if="record.status !== 'FAILED'">标记失败</a-button>
            <a-button type="link" @click="openRedispatch(record)" v-if="record.status !== 'DELIVERED'">重派</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="failOpen" title="标记送达失败" :confirm-loading="saving" @ok="submitFail">
      <a-form layout="vertical">
        <a-form-item label="未送达原因" required>
          <a-textarea v-model:value="failForm.failureReason" :rows="3" maxlength="255" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="failForm.remark" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="redispatchOpen" title="重派送餐任务" :confirm-loading="saving" @ok="submitRedispatch">
      <a-form layout="vertical">
        <a-form-item label="重派时间">
          <a-date-picker v-model:value="redispatchForm.redispatchAt" show-time value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </a-form-item>
        <a-form-item label="重派负责人">
          <a-input v-model:value="redispatchForm.redispatchByName" />
        </a-form-item>
        <a-form-item label="重派说明">
          <a-input v-model:value="redispatchForm.redispatchRemark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getDiningDeliveryRecordPage, redispatchDiningDeliveryRecord, updateDiningDeliveryRecord } from '../../api/dining'
import type { DiningDeliveryRecord, PageResult } from '../../types'

const route = useRoute()
const loading = ref(false)
const saving = ref(false)
const failOpen = ref(false)
const redispatchOpen = ref(false)
const rows = ref<DiningDeliveryRecord[]>([])
const selectedRecord = ref<DiningDeliveryRecord>()
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const summary = reactive({ total: 0, delivered: 0, undelivered: 0 })
const query = reactive({
  status: (String(route.query.filter || '') === 'undelivered' ? 'PENDING' : undefined) as string | undefined,
  date: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const statusOptions = [
  { label: '待送达', value: 'PENDING' },
  { label: '已送达', value: 'DELIVERED' },
  { label: '送达失败', value: 'FAILED' }
]

const columns = [
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', width: 160 },
  { title: '送餐区域', dataIndex: 'deliveryAreaName', key: 'deliveryAreaName', width: 140 },
  { title: '送餐人', dataIndex: 'deliveredByName', key: 'deliveredByName', width: 120 },
  { title: '送达时间', dataIndex: 'deliveredAt', key: 'deliveredAt', width: 170 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '未送达原因', dataIndex: 'failureReason', key: 'failureReason', width: 170 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 220, fixed: 'right' as const }
]

const failForm = reactive({
  failureReason: '',
  remark: ''
})

const redispatchForm = reactive({
  redispatchAt: undefined as string | undefined,
  redispatchByName: '',
  redispatchRemark: ''
})

function statusLabel(status?: string) {
  if (status === 'DELIVERED') return '已送达'
  if (status === 'FAILED') return '送达失败'
  return '待送达'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<DiningDeliveryRecord> = await getDiningDeliveryRecordPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status || undefined,
      dateFrom: query.date || undefined,
      dateTo: query.date || undefined
    })
    rows.value = res.list || []
    pagination.total = res.total || 0
    summary.total = rows.value.length
    summary.delivered = rows.value.filter((item) => item.status === 'DELIVERED').length
    summary.undelivered = rows.value.filter((item) => item.status !== 'DELIVERED').length
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.status = undefined
  query.date = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function handleTableChange(pag: any) {
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

function openFail(record: DiningDeliveryRecord) {
  selectedRecord.value = record
  failForm.failureReason = record.failureReason || ''
  failForm.remark = record.remark || ''
  failOpen.value = true
}

function openRedispatch(record: DiningDeliveryRecord) {
  selectedRecord.value = record
  redispatchForm.redispatchAt = undefined
  redispatchForm.redispatchByName = record.redispatchByName || ''
  redispatchForm.redispatchRemark = ''
  redispatchOpen.value = true
}

async function markDelivered(record: DiningDeliveryRecord) {
  await updateDiningDeliveryRecord(record.id, {
    mealOrderId: record.mealOrderId,
    orderNo: record.orderNo,
    deliveryAreaId: record.deliveryAreaId,
    deliveryAreaName: record.deliveryAreaName,
    deliveredByName: record.deliveredByName,
    status: 'DELIVERED',
    remark: record.remark
  })
  fetchData()
}

async function submitFail() {
  const record = selectedRecord.value
  if (!record) return
  if (!failForm.failureReason.trim()) {
    message.warning('请填写未送达原因')
    return
  }
  saving.value = true
  try {
    await updateDiningDeliveryRecord(record.id, {
      mealOrderId: record.mealOrderId,
      orderNo: record.orderNo,
      deliveryAreaId: record.deliveryAreaId,
      deliveryAreaName: record.deliveryAreaName,
      deliveredByName: record.deliveredByName,
      status: 'FAILED',
      failureReason: failForm.failureReason.trim(),
      remark: failForm.remark.trim() || undefined
    })
    failOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function submitRedispatch() {
  const record = selectedRecord.value
  if (!record) return
  saving.value = true
  try {
    await redispatchDiningDeliveryRecord(record.id, {
      redispatchAt: redispatchForm.redispatchAt,
      redispatchByName: redispatchForm.redispatchByName.trim() || undefined,
      redispatchRemark: redispatchForm.redispatchRemark.trim() || undefined
    })
    redispatchOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

onMounted(fetchData)
</script>
