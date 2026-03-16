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
      <div style="display: flex; justify-content: flex-end; margin-bottom: 12px">
        <a-button type="primary" :loading="optionsLoading && createOpen" @click="openCreate">新增送餐计划</a-button>
      </div>
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

    <a-modal v-model:open="createOpen" title="新增送餐计划" :confirm-loading="creating" @ok="submitCreate">
      <a-form layout="vertical">
        <a-alert
          v-if="!optionsLoading && mealOrders.length === 0"
          type="warning"
          show-icon
          message="暂无可用餐单"
          description="请先在点餐管理创建餐单，再回来新增送餐计划。"
          style="margin-bottom: 12px"
        >
          <template #action>
            <a-button type="link" size="small" @click="router.push('/dining/order')">去点餐管理</a-button>
          </template>
        </a-alert>
        <a-form-item label="餐单" required>
          <a-select
            v-model:value="createForm.mealOrderId"
            :options="mealOrderOptions"
            :loading="optionsLoading"
            show-search
            option-filter-prop="label"
            placeholder="请选择餐单"
          />
        </a-form-item>
        <a-form-item label="送餐区域">
          <a-select
            v-model:value="createForm.deliveryAreaId"
            :options="deliveryAreaOptions"
            :loading="optionsLoading"
            allow-clear
            show-search
            option-filter-prop="label"
            placeholder="可留空，默认取餐单区域"
          />
        </a-form-item>
        <a-form-item label="配送员">
          <a-input v-model:value="createForm.deliveredByName" maxlength="32" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="createForm.remark" maxlength="255" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  createDiningDeliveryRecord,
  getDiningDeliveryAreaList,
  getDiningDeliveryRecordPage,
  getDiningMealOrderList,
  redispatchDiningDeliveryRecord,
  updateDiningDeliveryRecord
} from '../../api/dining'
import type { DiningDeliveryArea, DiningDeliveryRecord, DiningMealOrder, PageResult } from '../../types'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const creating = ref(false)
const optionsLoading = ref(false)
const createOpen = ref(false)
const failOpen = ref(false)
const redispatchOpen = ref(false)
const rows = ref<DiningDeliveryRecord[]>([])
const mealOrders = ref<DiningMealOrder[]>([])
const deliveryAreas = ref<DiningDeliveryArea[]>([])
const selectedRecord = ref<DiningDeliveryRecord>()
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const summary = reactive({ total: 0, delivered: 0, undelivered: 0 })
const query = reactive({
  status: undefined as string | undefined,
  date: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const statusOptions = [
  { label: '待送达', value: 'PENDING' },
  { label: '已送达', value: 'DELIVERED' },
  { label: '送达失败', value: 'FAILED' }
]

const mealOrderOptions = computed(() =>
  mealOrders.value.map((item) => ({
    label: `${item.orderNo}｜${item.elderName || '未知长者'}｜${item.orderDate || ''} ${item.mealType || ''}｜${item.deliveryAreaName || '未分区'}`,
    value: item.id
  }))
)

const deliveryAreaOptions = computed(() =>
  deliveryAreas.value.map((item) => ({
    label: item.areaName || `区域${item.id}`,
    value: item.id
  }))
)

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

const createForm = reactive({
  mealOrderId: undefined as number | undefined,
  deliveryAreaId: undefined as number | undefined,
  deliveredByName: '',
  remark: ''
})

function statusLabel(status?: string) {
  if (status === 'DELIVERED') return '已送达'
  if (status === 'FAILED') return '送达失败'
  return '待送达'
}

function applyRouteFilters() {
  const statusRaw = Array.isArray(route.query.status) ? route.query.status[0] : route.query.status
  const filterRaw = Array.isArray(route.query.filter) ? route.query.filter[0] : route.query.filter
  const dateRaw = Array.isArray(route.query.date) ? route.query.date[0] : route.query.date
  if (typeof statusRaw === 'string' && statusRaw) {
    query.status = statusRaw
  } else if (String(filterRaw || '') === 'undelivered') {
    query.status = 'PENDING'
  } else {
    query.status = undefined
  }
  query.date = typeof dateRaw === 'string' && dateRaw ? dateRaw : undefined
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
    summary.total = pagination.total
    summary.delivered = rows.value.filter((item) => item.status === 'DELIVERED').length
    summary.undelivered = rows.value.filter((item) => item.status !== 'DELIVERED').length
  } catch (error: any) {
    message.error(error?.message || '加载送餐计划失败')
  } finally {
    loading.value = false
  }
}

async function loadCreateOptions() {
  optionsLoading.value = true
  try {
    const [orderList, areaList] = await Promise.all([
      getDiningMealOrderList({ pendingOnly: true, date: query.date || undefined, limit: 300 }),
      getDiningDeliveryAreaList({})
    ])
    let orders = orderList || []
    if (orders.length === 0 && query.date) {
      const fallback = await getDiningMealOrderList({ pendingOnly: true, limit: 300 })
      orders = fallback || []
      if (orders.length > 0) {
        message.info('当前日期暂无餐单，已自动展示全部可用餐单')
      }
    }
    mealOrders.value = orders
    deliveryAreas.value = areaList || []
  } catch (error: any) {
    mealOrders.value = []
    deliveryAreas.value = []
    message.error(error?.message || '加载餐单/送餐区域失败')
  } finally {
    optionsLoading.value = false
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
  try {
    await updateDiningDeliveryRecord(record.id, {
      mealOrderId: record.mealOrderId,
      orderNo: record.orderNo,
      deliveryAreaId: record.deliveryAreaId,
      deliveryAreaName: record.deliveryAreaName,
      deliveredByName: record.deliveredByName,
      status: 'DELIVERED',
      failureReason: undefined,
      remark: record.remark
    })
    message.success('已标记送达')
    await fetchData()
  } catch (error: any) {
    message.error(error?.message || '标记送达失败')
  }
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
    message.success('已标记送达失败')
    await fetchData()
  } catch (error: any) {
    message.error(error?.message || '标记失败失败')
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
    message.success('已重派送餐任务')
    await fetchData()
  } catch (error: any) {
    message.error(error?.message || '重派失败')
  } finally {
    saving.value = false
  }
}

async function openCreate() {
  createForm.mealOrderId = undefined
  createForm.deliveryAreaId = undefined
  createForm.deliveredByName = ''
  createForm.remark = ''
  createOpen.value = true
  await loadCreateOptions()
}

async function submitCreate() {
  if (!createForm.mealOrderId) {
    message.warning('请先选择餐单')
    return
  }
  const selectedOrder = mealOrders.value.find((item) => item.id === createForm.mealOrderId)
  if (!selectedOrder) {
    message.warning('餐单不存在或已失效，请刷新后重试')
    return
  }
  const selectedArea = createForm.deliveryAreaId
    ? deliveryAreas.value.find((item) => item.id === createForm.deliveryAreaId)
    : undefined
  creating.value = true
  try {
    await createDiningDeliveryRecord({
      mealOrderId: selectedOrder.id,
      orderNo: selectedOrder.orderNo,
      deliveryAreaId: createForm.deliveryAreaId ?? selectedOrder.deliveryAreaId,
      deliveryAreaName: selectedArea?.areaName || selectedOrder.deliveryAreaName,
      deliveredByName: createForm.deliveredByName.trim() || undefined,
      status: 'PENDING',
      remark: createForm.remark.trim() || undefined
    })
    createOpen.value = false
    message.success('送餐计划已创建')
    await fetchData()
  } catch (error: any) {
    message.error(error?.message || '新增送餐计划失败')
  } finally {
    creating.value = false
  }
}

onMounted(async () => {
  applyRouteFilters()
  await fetchData()
  await loadCreateOptions()
})

watch(
  () => route.query,
  () => {
    applyRouteFilters()
    fetchData()
  }
)
</script>
