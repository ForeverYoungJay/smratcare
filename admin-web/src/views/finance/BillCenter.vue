<template>
  <PageContainer :title="pageTitle" :subTitle="pageSubTitle">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
        <a-form-item label="月份">
          <a-date-picker v-model:value="query.month" picker="month" allow-clear style="width: 160px" />
        </a-form-item>
        <a-form-item label="老人姓名">
          <a-input v-model:value="query.keyword" style="width: 160px" allow-clear />
        </a-form-item>
        <a-form-item label="收款方式">
          <a-select v-model:value="query.payMethod" allow-clear style="width: 140px">
            <a-select-option value="CASH">现金</a-select-option>
            <a-select-option value="CARD">刷卡</a-select-option>
            <a-select-option value="BANK">转账</a-select-option>
            <a-select-option value="ALIPAY">支付宝</a-select-option>
            <a-select-option value="WECHAT">微信</a-select-option>
            <a-select-option value="WECHAT_OFFLINE">微信线下</a-select-option>
            <a-select-option value="QR_CODE">扫码</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-space>
          <a-button @click="exportCsvData">导出</a-button>
          <a-button type="primary" @click="openGenerate">生成账单</a-button>
        </a-space>
      </div>

      <vxe-toolbar custom export></vxe-toolbar>
      <vxe-table
        border
        stripe
        show-overflow
        height="520"
        :loading="loading"
        :data="displayRows"
        :column-config="{ resizable: true }"
      >
        <vxe-column field="billMonth" title="账单月份" width="120" />
        <vxe-column field="elderName" title="老人" min-width="140">
          <template #default="{ row }">
            <span>{{ row.elderName || '未知老人' }}</span>
          </template>
        </vxe-column>
        <vxe-column field="careLevel" title="护理级别" width="120" />
        <vxe-column field="totalAmount" title="总额" width="120" />
        <vxe-column field="nursingFee" title="护理费" width="120" />
        <vxe-column field="bedFee" title="床位费" width="120" />
        <vxe-column field="insuranceFee" title="保险费" width="120" />
        <vxe-column field="paidAmount" title="已付" width="120" />
        <vxe-column field="outstandingAmount" title="欠费" width="120">
          <template #default="{ row }">
            <a-tag :color="row.outstandingAmount > 0 ? 'red' : 'green'">{{ row.outstandingAmount ?? 0 }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column field="lastPayMethod" title="收款方式" width="130">
          <template #default="{ row }">
            <span>{{ payMethodText(row.lastPayMethod) }}</span>
          </template>
        </vxe-column>
        <vxe-column field="status" title="状态" width="140">
          <template #default="{ row }">
            <a-tag :color="statusColor(row.status)">{{ statusText(row.status) }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column title="操作" width="320" fixed="right">
          <template #default="{ row }">
            <a-space>
              <a-button type="link" @click="openDetail(row)">查看</a-button>
              <a-button v-if="row.status !== 9" type="link" @click="openPay(row)">登记收款</a-button>
              <a-button v-if="row.status !== 9 && row.lastPaymentId" type="link" @click="openEditLatestPayment(row)">改最近收款</a-button>
              <a-button type="link" @click="openHistory(row)">收款历史</a-button>
              <a-button v-if="row.status !== 9" type="link" danger @click="markInvalid(row)">无效账单</a-button>
              <a-tag v-else color="default">已无效</a-tag>
              <a-button type="link" @click="goConsumption(row)">消费明细</a-button>
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

    <a-drawer v-model:open="historyOpen" width="760" title="收款历史" :footer-style="{ textAlign: 'right' }">
      <div style="margin-bottom: 8px;">
        <a-space>
          <a-button @click="exportHistory">导出历史</a-button>
          <a-button @click="printHistory">打印历史</a-button>
        </a-space>
      </div>
      <vxe-table border stripe show-overflow :loading="historyLoading" :data="historyRows" height="460">
        <vxe-column field="amount" title="金额" width="110" />
        <vxe-column field="payMethod" title="方式" width="120">
          <template #default="{ row }">{{ payMethodText(row.payMethod) }}</template>
        </vxe-column>
        <vxe-column field="paidAt" title="收款时间" width="180" />
        <vxe-column field="remark" title="备注" min-width="180" />
        <vxe-column title="操作" width="120" fixed="right">
          <template #default="{ row }">
            <a-button v-if="!historyReadonly" type="link" @click="openEditHistoryPayment(row)">修改</a-button>
          </template>
        </vxe-column>
      </vxe-table>
      <template #footer>
        <a-button @click="historyOpen = false">关闭</a-button>
      </template>
    </a-drawer>

    <a-modal v-model:open="generateOpen" title="生成账单" @ok="submitGenerate" :confirm-loading="generating">
      <a-form layout="vertical" :model="generateForm">
        <a-form-item label="账单月份">
          <a-date-picker v-model:value="generateForm.month" picker="month" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>

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
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { getBillPage, generateBill, invalidateBill, payBill } from '../../api/bill'
import { getPaymentRecordPage, updatePaymentRecord } from '../../api/finance'
import type { BillItem, PageResult, PaymentRecordItem } from '../../types'
import router from '../../router'
import { printTableReport } from '../../utils/print'
import { confirmAction } from '../../utils/actionConfirm'

const props = withDefaults(defineProps<{
  title?: string
  subTitle?: string
}>(), {
  title: '月账单中心',
  subTitle: '按月查询、生成与收款登记'
})

const pageTitle = props.title
const pageSubTitle = props.subTitle

const loading = ref(false)
const rows = ref<BillItem[]>([])
const total = ref(0)

const query = reactive({
  month: undefined as any,
  keyword: '',
  payMethod: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const generateOpen = ref(false)
const generating = ref(false)
const generateForm = reactive({
  month: dayjs().startOf('month')
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

const payForm = reactive<PayForm>({
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

const activeBillId = ref<number | null>(null)
const historyOpen = ref(false)
const historyLoading = ref(false)
const historyRows = ref<PaymentRecordItem[]>([])
const historyReadonly = ref(false)

const displayRows = computed(() => {
  if (!query.payMethod) return rows.value
  return rows.value.filter(item => String(item.lastPayMethod || '').toUpperCase() === String(query.payMethod || '').toUpperCase())
})

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

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<BillItem> = await getBillPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      month: query.month ? dayjs(query.month).format('YYYY-MM') : undefined,
      keyword: query.keyword
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reset() {
  query.month = undefined
  query.keyword = ''
  query.payMethod = undefined
  query.pageNo = 1
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

function exportCsvData() {
  const data = displayRows.value.map((row) => ({
    老人: row.elderName || '未知老人',
    护理级别: row.careLevel || '-',
    总费用: row.totalAmount ?? 0,
    护理费: row.nursingFee ?? 0,
    床位费: row.bedFee ?? 0,
    保险费: row.insuranceFee ?? 0,
    已付: row.paidAmount ?? 0,
    欠费: row.outstandingAmount ?? 0,
    状态: statusText(row.status),
    收款方式: payMethodText(row.lastPayMethod)
  }))
  exportCsv(data, `在住账单缴费-${dayjs().format('YYYYMMDD-HHmmss')}.csv`)
}

function openGenerate() {
  generateOpen.value = true
}

async function submitGenerate() {
  if (!generateForm.month) {
    message.warning('请选择月份')
    return
  }
  generating.value = true
  try {
    await generateBill(dayjs(generateForm.month).format('YYYY-MM'))
    message.success('生成成功')
    generateOpen.value = false
    fetchData()
  } finally {
    generating.value = false
  }
}

function openPay(row: BillItem) {
  activeBillId.value = row.id
  activePaymentId.value = null
  payForm.amount = Number(row.outstandingAmount || 0)
  payForm.method = 'CASH'
  originalPayMethod.value = 'CASH'
  payForm.paidAt = dayjs()
  payForm.remark = ''
  payOpen.value = true
}

function openEditLatestPayment(row: BillItem) {
  if (!row.lastPaymentId) {
    message.warning('未找到最近收款记录')
    return
  }
  activeBillId.value = row.id
  activePaymentId.value = Number(row.lastPaymentId)
  payForm.amount = Number(row.lastPaymentAmount || 0)
  payForm.method = String(row.lastPayMethod || 'CASH').toUpperCase()
  originalPayMethod.value = payForm.method
  payForm.paidAt = row.lastPaidAt ? dayjs(row.lastPaidAt) : dayjs()
  payForm.remark = row.lastPaymentRemark || ''
  payOpen.value = true
}

async function submitPay() {
  await payFormRef.value?.validate?.()
  if (!activeBillId.value) return
  paying.value = true
  try {
    const payload = {
      amount: payForm.amount,
      method: payForm.method,
      paidAt: dayjs(payForm.paidAt).format('YYYY-MM-DD HH:mm:ss'),
      remark: payForm.remark
    }
    if (activePaymentId.value) {
      await updatePaymentRecord(activePaymentId.value, payload)
      if (originalPayMethod.value !== payForm.method) {
        message.success(`收款已修改，支付方式 ${payMethodText(originalPayMethod.value)} → ${payMethodText(payForm.method)}`)
      } else {
        message.success('收款已修改')
      }
    } else {
      await payBill(activeBillId.value, payload)
      message.success('收款登记成功')
    }
    payOpen.value = false
    activePaymentId.value = null
    await fetchData()
    if (historyOpen.value && activeBillId.value) {
      const res: PageResult<PaymentRecordItem> = await getPaymentRecordPage({
        pageNo: 1,
        pageSize: 100,
        billId: activeBillId.value
      })
      historyRows.value = res.list || []
    }
  } finally {
    paying.value = false
  }
}

function openDetail(row: BillItem) {
  router.push(`/finance/bill/${row.id}`)
}

function goConsumption(row: BillItem) {
  if (!row?.elderId) {
    message.warning('当前账单未关联老人')
    return
  }
  router.push(`/finance/flows/consumption?elderId=${row.elderId}`)
}

async function markInvalid(row: BillItem) {
  if (!row?.id) return
  const confirmed = await confirmAction({
    title: '确认标记为无效账单？',
    content: `账单：${row.billMonth || '-'} / ${row.elderName || '未知老人'}`,
    impactItems: ['账单状态将变更为“无效”', '后续不可直接继续收款', '需通过修复流程恢复有效状态'],
    okText: '确认作废',
    danger: true
  })
  if (!confirmed) return
  await invalidateBill(row.id)
  message.success('账单已标记为无效')
  fetchData()
}

async function openHistory(row: BillItem) {
  if (!row?.id) return
  historyOpen.value = true
  historyReadonly.value = Number(row.status) === 9
  historyLoading.value = true
  try {
    const res: PageResult<PaymentRecordItem> = await getPaymentRecordPage({
      pageNo: 1,
      pageSize: 100,
      billId: row.id
    })
    historyRows.value = res.list || []
  } finally {
    historyLoading.value = false
  }
}

function openEditHistoryPayment(row: PaymentRecordItem) {
  activeBillId.value = Number(row.billMonthlyId)
  activePaymentId.value = Number(row.id)
  payForm.amount = Number(row.amount || 0)
  payForm.method = String(row.payMethod || 'CASH').toUpperCase()
  originalPayMethod.value = payForm.method
  payForm.paidAt = row.paidAt ? dayjs(row.paidAt) : dayjs()
  payForm.remark = row.remark || ''
  payOpen.value = true
}

function exportHistory() {
  exportCsv(
    (historyRows.value || []).map(item => ({
      金额: item.amount,
      方式: payMethodText(item.payMethod),
      收款时间: item.paidAt,
      备注: item.remark || ''
    })),
    `收款历史-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
  )
}

function printHistory() {
  try {
    printTableReport({
      title: '收款历史',
      columns: [
        { key: 'amount', title: '金额' },
        { key: 'payMethodLabel', title: '方式' },
        { key: 'paidAt', title: '收款时间' },
        { key: 'remark', title: '备注' }
      ],
      rows: (historyRows.value || []).map(item => ({
        amount: item.amount,
        payMethodLabel: payMethodText(item.payMethod),
        paidAt: item.paidAt || '',
        remark: item.remark || ''
      }))
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

onMounted(fetchData)
</script>
