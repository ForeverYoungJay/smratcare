<template>
  <PageContainer title="账单详情" :subTitle="`账单ID: ${billId}`">
    <a-card class="card-elevated" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-statistic title="老人" :value="detail?.elderName || '未知老人'" />
        </a-col>
        <a-col :span="6">
          <a-statistic title="账单月份" :value="detail?.billMonth" />
        </a-col>
        <a-col :span="4">
          <a-statistic title="总额" :value="detail?.totalAmount ?? 0" />
        </a-col>
        <a-col :span="4">
          <a-statistic title="已付" :value="detail?.paidAmount ?? 0" />
        </a-col>
        <a-col :span="4">
          <a-statistic title="欠费" :value="detail?.outstandingAmount ?? 0" />
        </a-col>
      </a-row>
      <div style="margin-top: 16px;">
        <a-space>
          <a-button type="primary" @click="openPay">登记收款</a-button>
          <a-button @click="refresh">刷新</a-button>
        </a-space>
      </div>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="费用构成">
      <vxe-table border stripe show-overflow :data="detail?.items || []" height="260">
        <vxe-column field="itemType" title="类型" width="140" />
        <vxe-column field="itemName" title="项目" min-width="160" />
        <vxe-column field="amount" title="金额" width="120" />
        <vxe-column field="basis" title="依据" min-width="200" />
      </vxe-table>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="商城消费明细">
      <vxe-table
        border
        stripe
        show-overflow
        :data="detail?.storeOrders || []"
        height="260"
        @toggle-row-expand="onToggleOrderExpand"
      >
        <vxe-column type="expand" width="60">
          <template #content="{ row }">
            <a-spin :spinning="orderLoading[row.id]">
              <div v-if="orderDetailMap[row.id]">
                <div class="expand-title">订单明细</div>
                <vxe-table border stripe show-overflow :data="orderDetailMap[row.id].items || []" height="180">
                  <vxe-column field="productName" title="商品" min-width="160" />
                  <vxe-column field="quantity" title="数量" width="80" />
                  <vxe-column field="unitPrice" title="单价" width="100" />
                  <vxe-column field="amount" title="金额" width="100" />
                </vxe-table>

                <div class="expand-title" style="margin-top: 12px;">风控原因</div>
                <div v-if="orderDetailMap[row.id].riskReasons?.length">
                  <a-tag v-for="(r, idx) in orderDetailMap[row.id].riskReasons" :key="idx" color="red">
                    {{ r.diseaseName }} / {{ r.tagName || r.tagCode }}
                  </a-tag>
                </div>
                <a-empty v-else description="暂无风控命中" />

                <div class="expand-title" style="margin-top: 12px;">FIFO 批次扣减</div>
                <vxe-table border stripe show-overflow :data="orderDetailMap[row.id].fifoLogs || []" height="160">
                  <vxe-column field="batchNo" title="批次号" min-width="140" />
                  <vxe-column field="quantity" title="数量" width="80" />
                  <vxe-column field="expireDate" title="有效期" width="140" />
                </vxe-table>
              </div>
              <a-empty v-else description="暂无明细" />
            </a-spin>
          </template>
        </vxe-column>
        <vxe-column field="orderNo" title="订单号" min-width="160" />
        <vxe-column field="totalAmount" title="总额" width="120" />
        <vxe-column field="payableAmount" title="应付" width="120" />
        <vxe-column field="orderStatus" title="订单状态" width="140">
          <template #default="{ row }">
            <a-tag>{{ orderStatusText(row.orderStatus) }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column field="payStatus" title="支付状态" width="140">
          <template #default="{ row }">
            <a-tag :color="row.payStatus === 1 ? 'green' : 'orange'">{{ row.payStatus === 1 ? '已支付' : '未支付' }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column field="payTime" title="支付时间" width="180" />
        <vxe-column field="createTime" title="创建时间" width="180" />
      </vxe-table>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="支付记录">
      <div class="table-actions">
        <a-space>
          <a-button @click="exportPayments">导出支付记录</a-button>
        </a-space>
      </div>
      <vxe-table border stripe show-overflow :data="detail?.payments || []" height="220">
        <vxe-column field="amount" title="金额" width="120" />
        <vxe-column field="payMethod" title="方式" width="140" />
        <vxe-column field="paidAt" title="支付时间" width="180" />
        <vxe-column field="operatorStaffName" title="操作人" width="120" />
        <vxe-column field="remark" title="备注" min-width="200" />
      </vxe-table>
    </a-card>

    <a-modal v-model:open="payOpen" title="登记收款" @ok="submitPay" :confirm-loading="paying">
      <a-form layout="vertical" :model="payForm" :rules="payRules" ref="payFormRef">
        <a-form-item label="金额" name="amount">
          <a-input-number v-model:value="payForm.amount" style="width: 100%" />
        </a-form-item>
        <a-form-item label="方式" name="method">
          <a-select v-model:value="payForm.method">
            <a-select-option value="CASH">现金</a-select-option>
            <a-select-option value="BANK">转账</a-select-option>
            <a-select-option value="WECHAT_OFFLINE">微信线下</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="收款时间" name="paidAt">
          <a-date-picker v-model:value="payForm.paidAt" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="payForm.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getFinanceBillDetail } from '../../api/finance'
import { payBill } from '../../api/bill'
import { getOrderDetail } from '../../api/store'
import type { FinanceBillDetail } from '../../types'
import { exportCsv } from '../../utils/export'

const route = useRoute()
const billId = Number(route.params.billId)
const detail = ref<FinanceBillDetail | null>(null)
const orderDetailMap = ref<Record<number, any>>({})
const orderLoading = ref<Record<number, boolean>>({})

const payOpen = ref(false)
const paying = ref(false)
const payFormRef = ref()
type PayForm = {
  amount: number
  method: string
  paidAt: any
  remark: string
}

const payForm = ref<PayForm>({
  amount: 0,
  method: 'CASH',
  paidAt: dayjs(),
  remark: ''
})
const payRules = {
  amount: [{ required: true, message: '请输入金额' }],
  method: [{ required: true, message: '请选择方式' }],
  paidAt: [{ required: true, message: '请选择时间' }]
}

function orderStatusText(status?: number) {
  if (status === 4) return '已退款'
  if (status === 3) return '已取消'
  if (status === 2) return '已出库'
  if (status === 1) return '已支付'
  return '待处理'
}

async function refresh() {
  detail.value = await getFinanceBillDetail(billId)
}

async function onToggleOrderExpand({ row, expanded }: any) {
  if (!expanded || !row?.id) return
  if (orderDetailMap.value[row.id]) return
  orderLoading.value[row.id] = true
  try {
    const data = await getOrderDetail(row.id)
    orderDetailMap.value[row.id] = data
  } finally {
    orderLoading.value[row.id] = false
  }
}

function openPay() {
  payForm.value = {
    amount: Number(detail.value?.outstandingAmount || 0),
    method: 'CASH',
    paidAt: dayjs(),
    remark: ''
  }
  payOpen.value = true
}

async function submitPay() {
  await payFormRef.value?.validate?.()
  paying.value = true
  try {
    await payBill(billId, {
      amount: payForm.value.amount,
      method: payForm.value.method,
      paidAt: dayjs(payForm.value.paidAt).format('YYYY-MM-DD HH:mm:ss'),
      remark: payForm.value.remark
    })
    message.success('收款登记成功')
    payOpen.value = false
    refresh()
  } finally {
    paying.value = false
  }
}

function exportPayments() {
  exportCsv(detail.value?.payments || [], `payments-${billId}.csv`)
}

onMounted(refresh)
</script>
