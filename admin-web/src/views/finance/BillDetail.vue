<template>
  <PageContainer title="账单详情" :subTitle="`账单ID: ${currentBillId}`">
    <a-card v-if="loadError" class="card-elevated" :bordered="false">
      <a-result
        status="warning"
        title="未找到可访问的账单"
        :sub-title="loadError"
      >
        <template #extra>
          <a-space wrap>
            <a-button type="primary" @click="router.push('/finance/bills/detail-query')">返回账单详情查询</a-button>
            <a-button @click="router.push('/finance/bills/in-resident')">前往在住账单</a-button>
            <a-button v-if="lastValidBillId" @click="router.push(`/finance/bill/${lastValidBillId}`)">返回最近账单</a-button>
          </a-space>
        </template>
      </a-result>
      <a-divider />
      <a-space wrap>
        <a-select
          v-model:value="fixQuery.elderId"
          show-search
          allow-clear
          :filter-option="false"
          :options="elderOptions"
          :loading="elderLoading"
          placeholder="选择老人后定位账单"
          style="width: 280px"
          @search="searchElders"
        />
        <a-date-picker v-model:value="fixQuery.month" picker="month" style="width: 160px" />
        <a-button type="primary" @click="locateBillByElder">按老人+月份定位账单</a-button>
      </a-space>
    </a-card>

    <template v-else>
    <a-card class="card-elevated" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-statistic title="老人" :value="resolvedElderName" />
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
      <a-tag style="margin-top: 8px;" :color="statusColor(detail?.status)">{{ statusText(detail?.status) }}</a-tag>

      <a-alert v-if="!detail?.elderName" type="warning" show-icon style="margin-top: 12px;" message="该账单老人信息缺失，请按老人+月份重新定位" />
      <a-space v-if="!detail?.elderName" style="margin-top: 12px;" wrap>
        <a-select
          v-model:value="fixQuery.elderId"
          show-search
          allow-clear
          :filter-option="false"
          :options="elderOptions"
          :loading="elderLoading"
          placeholder="选择老人"
          style="width: 260px"
          @search="searchElders"
        />
        <a-date-picker v-model:value="fixQuery.month" picker="month" style="width: 160px" />
        <a-button type="primary" @click="locateBillByElder">按老人查询账单</a-button>
        <a-button :loading="bindingElder" @click="bindCurrentBillElder">修复本账单老人</a-button>
      </a-space>

      <div style="margin-top: 16px;">
        <a-space>
          <a-button v-if="detail?.status !== 9" type="primary" @click="openPay">登记收款</a-button>
          <a-button v-if="detail?.status !== 9" danger @click="markInvalid">无效账单</a-button>
          <a-button @click="goConsumption">消费明细</a-button>
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
        <vxe-column field="payMethod" title="方式" width="140">
          <template #default="{ row }">
            {{ payMethodText(row.payMethod) }}
          </template>
        </vxe-column>
        <vxe-column field="paidAt" title="支付时间" width="180" />
        <vxe-column field="operatorStaffName" title="操作人" width="120" />
        <vxe-column field="remark" title="备注" min-width="200" />
        <vxe-column title="操作" width="120" fixed="right">
          <template #default="{ row }">
            <a-button v-if="detail?.status !== 9" type="link" @click="openEditPayment(row)">修改</a-button>
          </template>
        </vxe-column>
      </vxe-table>
    </a-card>

    <a-modal v-model:open="payOpen" :title="activePaymentId ? '修改收款' : '登记收款'" @ok="submitPay" :confirm-loading="paying">
      <a-form layout="vertical" :model="payForm" :rules="payRules" ref="payFormRef">
        <a-form-item label="金额" name="amount">
          <a-input-number v-model:value="payForm.amount" style="width: 100%" />
        </a-form-item>
        <a-form-item label="方式" name="method">
          <a-select v-model:value="payForm.method">
            <a-select-option value="CASH">现金</a-select-option>
            <a-select-option value="CARD">刷卡</a-select-option>
            <a-select-option value="BANK">转账</a-select-option>
            <a-select-option value="ALIPAY">支付宝</a-select-option>
            <a-select-option value="WECHAT">微信</a-select-option>
            <a-select-option value="WECHAT_OFFLINE">微信线下</a-select-option>
            <a-select-option value="QR_CODE">扫码</a-select-option>
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
    </template>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { bindFinanceBillElder, getFinanceBillDetail, updatePaymentRecord } from '../../api/finance'
import { getBillDetail, invalidateBill, payBill } from '../../api/bill'
import { getOrderDetail } from '../../api/store'
import type { FinanceBillDetail } from '../../types'
import { exportCsv } from '../../utils/export'
import { confirmAction } from '../../utils/actionConfirm'
import { useElderOptions } from '../../composables/useElderOptions'

const route = useRoute()
const router = useRouter()
const currentBillId = computed(() => Number(route.params.billId))
const detail = ref<FinanceBillDetail | null>(null)
const loadError = ref('')
const lastValidBillId = ref<number | null>(null)
const orderDetailMap = ref<Record<number, any>>({})
const orderLoading = ref<Record<number, boolean>>({})

const { elderOptions, elderLoading, searchElders, ensureSelectedElder, findElderName } = useElderOptions({ pageSize: 80, inHospitalOnly: false })

const fixQuery = reactive({
  elderId: undefined as number | undefined,
  month: dayjs().startOf('month') as any
})
const bindingElder = ref(false)

const resolvedElderName = computed(() => {
  const current = detail.value
  if (!current) return '-'
  if (current.elderName) return current.elderName
  if (current.elderId) return findElderName(current.elderId) || '姓名待完善'
  if (fixQuery.elderId) return findElderName(fixQuery.elderId) || '姓名待完善'
  return '姓名待完善'
})

const payOpen = ref(false)
const paying = ref(false)
const payFormRef = ref()
const activePaymentId = ref<number | null>(null)
const originalPayMethod = ref<string>('CASH')
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

function statusText(status?: number) {
  if (status === 9) return '无效'
  if (status === 2) return '已付'
  if (status === 1) return '部分已付'
  return '未付'
}

function statusColor(status?: number) {
  if (status === 9) return 'default'
  if (status === 2) return 'green'
  if (status === 1) return 'orange'
  return 'red'
}

async function refresh() {
  const billId = Number(currentBillId.value)
  if (!Number.isFinite(billId) || billId <= 0) {
    detail.value = null
    loadError.value = '账单ID无效，请返回账单中心重新选择。'
    return
  }
  try {
    const data = await getFinanceBillDetail(billId, { silentError: true })
    detail.value = data
    lastValidBillId.value = billId
    loadError.value = ''
    const month = detail.value?.billMonth
    if (month) {
      fixQuery.month = dayjs(month, 'YYYY-MM')
    }
    if (detail.value?.elderId) {
      ensureSelectedElder(detail.value.elderId, detail.value.elderName)
      fixQuery.elderId = detail.value.elderId
    }
  } catch (error: any) {
    detail.value = null
    const raw = String(error?.message || error?.msg || '').trim()
    if (raw) {
      loadError.value = /bill not found/i.test(raw)
        ? `账单不存在、已删除或无权限访问（ID: ${billId}）`
        : raw
    } else {
      loadError.value = `账单不存在、已删除或无权限访问（ID: ${billId}）`
    }
  }
}

async function locateBillByElder() {
  if (!fixQuery.elderId) {
    message.warning('请先选择老人')
    return
  }
  const monthText = fixQuery.month ? dayjs(fixQuery.month).format('YYYY-MM') : ''
  if (!monthText) {
    message.warning('请选择账单月份')
    return
  }
  try {
    const bill = await getBillDetail(fixQuery.elderId, monthText)
    if (!bill?.id) {
      message.warning('未查询到该老人该月账单')
      return
    }
    if (Number(bill.id) === currentBillId.value) {
      refresh()
      return
    }
    router.replace(`/finance/bill/${bill.id}`)
  } catch (error: any) {
    message.error(error?.message || '查询失败')
  }
}

async function bindCurrentBillElder() {
  if (!fixQuery.elderId) {
    message.warning('请先选择老人')
    return
  }
  bindingElder.value = true
  try {
    await bindFinanceBillElder(currentBillId.value, {
      elderId: fixQuery.elderId,
      remark: '账单详情页手工修复'
    })
    message.success('账单老人已修复')
    await refresh()
  } finally {
    bindingElder.value = false
  }
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
  activePaymentId.value = null
  originalPayMethod.value = 'CASH'
  payForm.value = {
    amount: Number(detail.value?.outstandingAmount || 0),
    method: 'CASH',
    paidAt: dayjs(),
    remark: ''
  }
  payOpen.value = true
}

function openEditPayment(row: any) {
  activePaymentId.value = Number(row.id)
  originalPayMethod.value = String(row.payMethod || 'CASH').toUpperCase()
  payForm.value = {
    amount: Number(row.amount || 0),
    method: String(row.payMethod || 'CASH').toUpperCase(),
    paidAt: row.paidAt ? dayjs(row.paidAt) : dayjs(),
    remark: row.remark || ''
  }
  payOpen.value = true
}

function payMethodText(method?: string) {
  const text = String(method || '').toUpperCase()
  if (!text) return '-'
  if (text === 'ALIPAY') return '支付宝'
  if (text === 'WECHAT') return '微信'
  if (text === 'WECHAT_OFFLINE') return '微信线下'
  if (text === 'QR_CODE') return '扫码'
  if (text === 'BANK') return '转账'
  if (text === 'CARD') return '刷卡'
  if (text === 'CASH') return '现金'
  return text
}

async function submitPay() {
  if (!detail.value) return
  await payFormRef.value?.validate?.()
  paying.value = true
  try {
    const payload = {
      amount: payForm.value.amount,
      method: payForm.value.method,
      paidAt: dayjs(payForm.value.paidAt).format('YYYY-MM-DD HH:mm:ss'),
      remark: payForm.value.remark
    }
    if (activePaymentId.value) {
      await updatePaymentRecord(activePaymentId.value, payload)
      if (originalPayMethod.value !== payForm.value.method) {
        message.success(`收款已修改，支付方式 ${payMethodText(originalPayMethod.value)} → ${payMethodText(payForm.value.method)}`)
      } else {
        message.success('收款已修改')
      }
    } else {
      await payBill(currentBillId.value, payload)
      message.success('收款登记成功')
    }
    payOpen.value = false
    activePaymentId.value = null
    refresh()
  } finally {
    paying.value = false
  }
}

async function markInvalid() {
  if (!detail.value) return
  const confirmed = await confirmAction({
    title: '确认标记为无效账单？',
    content: `账单ID：${currentBillId.value}`,
    impactItems: ['账单状态将变更为“无效”', '该账单后续不可直接收款', '如需恢复需走修复流程'],
    okText: '确认作废',
    danger: true
  })
  if (!confirmed) return
  await invalidateBill(currentBillId.value)
  message.success('账单已标记为无效')
  refresh()
}

function exportPayments() {
  if (!detail.value) return
  exportCsv(detail.value?.payments || [], `payments-${currentBillId.value}.csv`)
}

function goConsumption() {
  const elderId = detail.value?.elderId || fixQuery.elderId
  if (!elderId) {
    message.warning('当前账单未关联老人')
    return
  }
  router.push(`/finance/flows/consumption?elderId=${elderId}`)
}

watch(
  () => route.params.billId,
  () => {
    orderDetailMap.value = {}
    orderLoading.value = {}
    refresh()
  }
)

onMounted(() => {
  searchElders('').catch(() => {})
  refresh()
})
</script>
