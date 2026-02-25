<template>
  <PageContainer title="订单管理" subTitle="订单查询与风控追踪">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" placeholder="订单号/老人姓名" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.orderStatus" allow-clear style="width: 140px">
            <a-select-option :value="1">已创建</a-select-option>
            <a-select-option :value="2">已支付</a-select-option>
            <a-select-option :value="3">已出库</a-select-option>
            <a-select-option :value="4">已取消</a-select-option>
            <a-select-option :value="5">已退款</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="命中禁忌">
          <a-select v-model:value="query.riskHit" allow-clear style="width: 120px">
            <a-select-option :value="1">是</a-select-option>
            <a-select-option :value="0">否</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">搜索</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-space>
          <a-button @click="exportCsvData">导出CSV</a-button>
        </a-space>
      </div>

      <vxe-toolbar custom export></vxe-toolbar>
      <vxe-table
        border
        stripe
        show-overflow
        height="520"
        :loading="loading"
        :data="rows"
        :column-config="{ resizable: true }"
      >
        <vxe-column field="orderNo" title="订单号" width="200" />
        <vxe-column field="elderName" title="老人" width="140">
          <template #default="{ row }">
            <span>{{ row.elderName || '未知老人' }}</span>
          </template>
        </vxe-column>
        <vxe-column field="totalAmount" title="总金额" width="120" />
        <vxe-column field="pointsUsed" title="积分" width="120" />
        <vxe-column field="orderStatus" title="状态" width="120">
          <template #default="{ row }">
            <a-tag :color="statusColor(row.orderStatus)">{{ statusText(row.orderStatus) }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column field="createTime" title="下单时间" width="180" />
        <vxe-column title="操作" width="260" fixed="right">
          <template #default="{ row }">
            <a-space>
              <a @click="openDetail(row)">详情</a>
              <a
                @click="handleFulfill(row)"
                :style="{ color: isFulfillDisabled(row) ? '#999' : '' }"
              >
                标记出库
              </a>
              <a @click="handleCancel(row)">取消</a>
              <a @click="handleRefund(row)">退款</a>
            </a-space>
          </template>
        </vxe-column>
      </vxe-table>

      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>

    <a-drawer v-model:open="detailOpen" title="订单详情" width="640">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="订单号">{{ detail?.orderNo }}</a-descriptions-item>
        <a-descriptions-item label="老人">{{ detail?.elderName || '未知老人' }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ statusText(detail?.orderStatus) }}</a-descriptions-item>
        <a-descriptions-item label="积分">{{ detail?.pointsUsed }}</a-descriptions-item>
      </a-descriptions>
      <a-divider />
      <a-tabs>
        <a-tab-pane key="items" tab="商品明细">
          <a-empty v-if="!detail?.items?.length" description="暂无明细" />
          <a-list v-else :data-source="detail.items">
            <template #renderItem="{ item }">
              <a-list-item>
                {{ item.productName || item.productId }} × {{ item.quantity }}
              </a-list-item>
            </template>
          </a-list>
        </a-tab-pane>
        <a-tab-pane key="risk" tab="风控原因">
          <a-empty v-if="!detail?.riskReasons?.length" description="暂无风控原因" />
          <a-list v-else :data-source="detail.riskReasons">
            <template #renderItem="{ item }">
              <a-list-item>
                {{ item.diseaseName }} - {{ item.tagName || item.tagCode }}
              </a-list-item>
            </template>
          </a-list>
        </a-tab-pane>
        <a-tab-pane key="fifo" tab="FIFO扣减明细">
          <a-empty v-if="!detail?.fifoLogs?.length" description="暂无扣减记录" />
          <a-list v-else :data-source="detail.fifoLogs">
            <template #renderItem="{ item }">
              <a-list-item>
                批次 {{ item.batchNo }} / 数量 {{ item.quantity }}
              </a-list-item>
            </template>
          </a-list>
        </a-tab-pane>
      </a-tabs>
    </a-drawer>

    <a-modal
      v-model:open="refundOpen"
      title="订单退款"
      :confirm-loading="refundSubmitting"
      @ok="submitRefund"
      @cancel="closeRefund"
    >
      <a-form layout="vertical">
        <a-form-item label="退款原因" required>
          <a-select v-model:value="refundReason" placeholder="请选择退款原因">
            <a-select-option v-for="item in refundReasonOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getBaseConfigItemList } from '../../api/baseConfig'
import { exportCsv } from '../../utils/export'
import { getOrderPage, getOrderDetail, cancelOrder, refundOrder, fulfillOrder } from '../../api/store'
import type { BaseConfigItem, OrderItem, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<OrderItem[]>([])
const total = ref(0)
const detailOpen = ref(false)
const detail = ref<OrderItem | null>(null)
const fulfillingIds = ref<Set<number>>(new Set())
const refundOpen = ref(false)
const refundSubmitting = ref(false)
const refundOrderId = ref<number | null>(null)
const refundReason = ref('')
const refundReasonOptions = ref<{ label: string; value: string }[]>([])

const query = reactive({
  keyword: '',
  orderStatus: undefined as number | undefined,
  riskHit: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10
})

function statusText(status?: number) {
  if (status === 1) return '已创建'
  if (status === 2) return '已支付'
  if (status === 3) return '已出库'
  if (status === 4) return '已取消'
  if (status === 5) return '已退款'
  return '未知'
}

function statusColor(status?: number) {
  if (status === 3) return 'green'
  if (status === 4) return 'default'
  if (status === 5) return 'volcano'
  return 'blue'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<OrderItem> = await getOrderPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      orderStatus: query.orderStatus === undefined ? undefined : Number(query.orderStatus),
      riskHit: query.riskHit === undefined ? undefined : Number(query.riskHit)
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function loadRefundReasons() {
  const options = await getBaseConfigItemList({ configGroup: 'REFUND_REASON', status: 1 })
  refundReasonOptions.value = (options || []).map((item: BaseConfigItem) => ({
    label: item.itemName,
    value: item.itemName
  }))
}

function reset() {
  query.keyword = ''
  query.orderStatus = undefined
  query.riskHit = undefined
  query.pageNo = 1
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(_page: number, size: number) {
  query.pageSize = size
  query.pageNo = 1
  fetchData()
}

function exportCsvData() {
  exportCsv(
    rows.value.map((o) => ({
      订单号: o.orderNo,
      老人: o.elderName || '未知老人',
      总金额: o.totalAmount,
      积分: o.pointsUsed,
      状态: statusText(o.orderStatus)
    })),
    '订单列表'
  )
}

async function openDetail(row: OrderItem) {
  detailOpen.value = true
  detail.value = await getOrderDetail(row.id)
}

function handleCancel(row: OrderItem) {
  if (isCancelDisabled(row)) {
    message.info('当前状态不可取消')
    return
  }
  Modal.confirm({
    title: '确认取消订单？',
    onOk: async () => {
      await cancelOrder({ orderId: row.id })
      message.success('已取消')
      fetchData()
    }
  })
}

function handleRefund(row: OrderItem) {
  if (isRefundDisabled(row)) {
    message.info('当前状态不可退款')
    return
  }
  refundOrderId.value = row.id
  refundReason.value = ''
  refundOpen.value = true
}

function closeRefund() {
  refundOpen.value = false
  refundOrderId.value = null
  refundReason.value = ''
}

async function submitRefund() {
  if (!refundOrderId.value) {
    return
  }
  if (!refundReason.value) {
    message.error('请选择退款原因')
    return
  }
  refundSubmitting.value = true
  try {
    await refundOrder({ orderId: refundOrderId.value, reason: refundReason.value })
    message.success('已退款')
    closeRefund()
    fetchData()
  } finally {
    refundSubmitting.value = false
  }
}

function handleFulfill(row: OrderItem) {
  if (isFulfillDisabled(row)) {
    message.info('当前状态无需出库')
    return
  }
  Modal.confirm({
    title: '确认标记出库？',
    onOk: async () => {
      if (fulfillingIds.value.has(row.id)) {
        return
      }
      fulfillingIds.value.add(row.id)
      try {
        await fulfillOrder(row.id)
        message.success('已出库')
        fetchData()
      } finally {
        fulfillingIds.value.delete(row.id)
      }
    }
  })
}

function isFulfillDisabled(row: OrderItem) {
  return (row.orderStatus !== undefined && row.orderStatus >= 3) || fulfillingIds.value.has(row.id)
}

function isCancelDisabled(row: OrderItem) {
  return row.orderStatus !== undefined && row.orderStatus >= 3
}

function isRefundDisabled(row: OrderItem) {
  return row.orderStatus === undefined || row.orderStatus < 3
}

onMounted(async () => {
  await loadRefundReasons()
  await fetchData()
})
</script>

<style scoped>
.search-form {
  row-gap: 8px;
}
.table-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}
</style>
