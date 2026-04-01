<template>
  <PageContainer :title="pageTitle" :subTitle="pageSubTitle">
    <template #stats>
      <div class="bill-overview-grid">
        <div class="overview-card">
          <span>当前账单数</span>
          <strong>{{ total }}</strong>
          <small>按当前筛选条件实时统计</small>
        </div>
        <div class="overview-card">
          <span>欠费笔数</span>
          <strong>{{ overdueCount }}</strong>
          <small>需优先安排催缴与跟进</small>
        </div>
        <div class="overview-card">
          <span>欠费金额</span>
          <strong>{{ formatAmount(overdueAmount) }}</strong>
          <small>帮助财务快速识别风险账单</small>
        </div>
        <div class="overview-card">
          <span>已收金额</span>
          <strong>{{ formatAmount(paidAmount) }}</strong>
          <small>当前页收款完成率 {{ collectionRate }}%</small>
        </div>
      </div>
    </template>

    <SearchForm :model="query" @search="runQuery" @reset="reset">
      <a-form-item label="月份">
        <a-date-picker v-model:value="query.month" picker="month" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="老人姓名">
        <ElderNameAutocomplete
          v-model:value="query.keyword"
          width="200px"
          placeholder="老人姓名(编号)"
          @select="() => runQuery(true)"
        />
      </a-form-item>
      <a-form-item label="收款方式">
        <a-select v-model:value="query.payMethod" allow-clear style="width: 148px">
          <a-select-option value="CASH">现金</a-select-option>
          <a-select-option value="CARD">刷卡</a-select-option>
          <a-select-option value="BANK">转账</a-select-option>
          <a-select-option value="ALIPAY">支付宝</a-select-option>
          <a-select-option value="WECHAT">微信</a-select-option>
          <a-select-option value="WECHAT_OFFLINE">微信线下</a-select-option>
          <a-select-option value="QR_CODE">扫码</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item v-if="!props.lockScene" label="账单场景">
        <a-select v-model:value="query.scene" allow-clear style="width: 148px">
          <a-select-option value="ADMISSION">入住首期</a-select-option>
          <a-select-option value="RESIDENT">在住周期</a-select-option>
        </a-select>
      </a-form-item>
    </SearchForm>

    <section class="surface-toolbar">
      <div class="surface-toolbar-title">
        <strong>账单生成与收款工作台</strong>
        <span>先锁定账期和长者，再完成生成、收款登记、异常识别与历史追溯。</span>
      </div>
      <a-space wrap>
        <a-tag color="processing">账期 {{ currentMonthLabel }}</a-tag>
        <a-tag :color="query.scene ? 'blue' : 'default'">{{ currentSceneLabel }}</a-tag>
        <a-tag color="gold">欠费 {{ overdueCount }} 笔</a-tag>
        <a-tag color="green">收款完成率 {{ collectionRate }}%</a-tag>
      </a-space>
    </section>

    <a-alert
      v-if="signedLinkageContext.active"
      class="linkage-alert"
      type="success"
      show-icon
      :message="signedLinkageContext.message"
      :description="signedLinkageContext.description"
    >
      <template #action>
        <a-space wrap>
          <a-button size="small" @click="goSignedResident360">长者总览</a-button>
          <a-button size="small" @click="goSignedAccountList">长者账户</a-button>
        </a-space>
      </template>
    </a-alert>

    <section class="card-elevated bill-workspace">
      <div class="bill-insight-grid">
        <div class="bill-insight-card">
          <span>全院风险摘要</span>
          <strong>{{ riskSummaryText }}</strong>
          <small>{{ riskSummaryDescription }}</small>
        </div>
        <div class="bill-insight-card is-soft">
          <span>当前页工作提示</span>
          <strong>{{ total }} 条账单，{{ invalidBillCount }} 条已作废</strong>
          <small>支持直接查看详情、登记收款、修正最近收款与查看消费明细。</small>
        </div>
      </div>

      <div class="action-toolbar">
        <div class="table-head">
          <div>
            <strong>账单列表</strong>
            <span>保留原有账单业务逻辑，优化筛选、风险识别和收款操作的工作顺序。</span>
          </div>
          <a-space wrap>
            <a-tag color="default">共 {{ total }} 条</a-tag>
            <a-tag color="purple">已作废 {{ invalidBillCount }} 条</a-tag>
          </a-space>
        </div>
        <div class="action-toolbar-main">
          <a-button @click="exportCsvData">导出CSV</a-button>
          <a-button type="primary" @click="openGenerate">生成账单</a-button>
        </div>
      </div>

      <div class="bill-table-frame">
        <vxe-toolbar class="finance-vxe-toolbar" custom export></vxe-toolbar>
        <vxe-table
          class="finance-vxe-table"
          border
          stripe
          show-overflow
          height="520"
          :loading="loading"
          :data="displayRows"
          :column-config="{ resizable: true }"
        >
          <vxe-column field="billMonth" title="账单月份" width="120" />
          <vxe-column field="elderName" title="老人" min-width="180">
            <template #default="{ row }">
              <div class="bill-person-cell">
                <strong>{{ row.elderName || '未知老人' }}</strong>
                <span>{{ row.lastPaidAt ? `最近收款 ${dayjs(row.lastPaidAt).format('MM-DD HH:mm')}` : '暂无收款记录' }}</span>
              </div>
            </template>
          </vxe-column>
          <vxe-column field="careLevel" title="护理级别" width="120" />
          <vxe-column field="totalAmount" title="总额" width="120">
            <template #default="{ row }">
              <span class="amount-text">{{ formatAmount(row.totalAmount) }}</span>
            </template>
          </vxe-column>
          <vxe-column field="nursingFee" title="护理费" width="120">
            <template #default="{ row }">
              <span class="amount-subtext">{{ formatAmount(row.nursingFee) }}</span>
            </template>
          </vxe-column>
          <vxe-column field="bedFee" title="床位费" width="120">
            <template #default="{ row }">
              <span class="amount-subtext">{{ formatAmount(row.bedFee) }}</span>
            </template>
          </vxe-column>
          <vxe-column field="insuranceFee" title="保险费" width="120">
            <template #default="{ row }">
              <span class="amount-subtext">{{ formatAmount(row.insuranceFee) }}</span>
            </template>
          </vxe-column>
          <vxe-column field="paidAmount" title="已付" width="120">
            <template #default="{ row }">
              <span class="amount-text is-positive">{{ formatAmount(row.paidAmount) }}</span>
            </template>
          </vxe-column>
          <vxe-column field="outstandingAmount" title="欠费" width="120">
            <template #default="{ row }">
              <span class="amount-chip" :class="Number(row.outstandingAmount || 0) > 0 ? 'is-danger' : 'is-safe'">
                {{ formatAmount(row.outstandingAmount) }}
              </span>
            </template>
          </vxe-column>
          <vxe-column field="lastPayMethod" title="收款方式" width="130">
            <template #default="{ row }">
              <span class="pay-method-pill">{{ payMethodText(row.lastPayMethod) }}</span>
            </template>
          </vxe-column>
          <vxe-column field="status" title="状态" width="140">
            <template #default="{ row }">
              <a-tag :color="statusColor(row.status)">{{ statusText(row.status) }}</a-tag>
            </template>
          </vxe-column>
          <vxe-column title="操作" width="320" fixed="right">
            <template #default="{ row }">
              <div class="row-action-links">
                <a-button type="link" @click="openDetail(row)">查看</a-button>
                <a-button v-if="row.status !== 9" type="link" @click="openPay(row)">登记收款</a-button>
                <a-button v-if="row.status !== 9 && row.lastPaymentId" type="link" @click="openEditLatestPayment(row)">改最近收款</a-button>
                <a-button type="link" @click="openHistory(row)">收款历史</a-button>
                <a-button v-if="row.status !== 9" type="link" danger @click="markInvalid(row)">无效账单</a-button>
                <a-tag v-else color="default">已无效</a-tag>
                <a-button type="link" @click="goConsumption(row)">消费明细</a-button>
              </div>
            </template>
          </vxe-column>
        </vxe-table>
      </div>

      <div class="pager-row">
        <a-pagination
          :current="query.pageNo"
          :page-size="query.pageSize"
          :total="total"
          show-size-changer
          @change="onPageChange"
          @showSizeChange="onPageSizeChange"
        />
      </div>
    </section>

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
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import { exportCsv } from '../../utils/export'
import { getBillPage, generateBill, invalidateBill, payBill } from '../../api/bill'
import { getFinanceModuleEntrySummary, getPaymentRecordPage, updatePaymentRecord } from '../../api/finance'
import type { BillItem, FinanceModuleEntrySummary, Id, PageResult, PaymentRecordItem } from '../../types'
import { printTableReport } from '../../utils/print'
import { confirmAction } from '../../utils/actionConfirm'
import { normalizeResidentId } from '../../utils/id'
import {
  buildBillCenterRouteQuery,
  buildBillCenterRouteSignature,
  flattenBillCenterRouteQuery,
  parseBillCenterRouteState,
  type BillCenterScene
} from '../../utils/billCenterQuery'

const props = withDefaults(defineProps<{
  title?: string
  subTitle?: string
  defaultScene?: 'ADMISSION' | 'RESIDENT' | undefined
  lockScene?: boolean
  defaultCurrentMonth?: boolean
}>(), {
  title: '月账单中心',
  subTitle: '按月查询、生成与收款登记',
  defaultScene: undefined,
  lockScene: false,
  defaultCurrentMonth: false
})

const pageTitle = props.title
const pageSubTitle = props.subTitle
const route = useRoute()
const router = useRouter()
const billRouteSignature = ref('')
const skipNextBillRouteWatch = ref(false)

const loading = ref(false)
const rows = ref<BillItem[]>([])
const total = ref(0)
const summary = ref<FinanceModuleEntrySummary>({
  moduleKey: 'BALANCE_WARN',
  bizDate: '',
  todayAmount: 0,
  monthAmount: 0,
  totalCount: 0,
  pendingCount: 0,
  exceptionCount: 0,
  warningMessage: '',
  topItems: []
})

const query = reactive({
  month: undefined as any,
  keyword: '',
  elderId: undefined as Id | undefined,
  payMethod: undefined as string | undefined,
  scene: undefined as ('ADMISSION' | 'RESIDENT' | undefined),
  pageNo: 1,
  pageSize: 10
})
const signedLinkageContext = computed(() => {
  const source = routeQueryText(route.query.source).toLowerCase()
  const entryScene = routeQueryText(route.query.entryScene).toLowerCase()
  const elderName = routeQueryText(route.query.elderName || route.query.residentName)
  const active = !!query.elderId && (source === 'contract_signed' || entryScene === 'signed_onboarding')
  return {
    active,
    elderName,
    message: active ? `当前账单中心已定位到新签约长者 ${elderName || '该长者'}` : '',
    description: active ? `当前账期、首期账单和收款都可以直接在这里继续办理。` : ''
  }
})

const generateOpen = ref(false)
const generating = ref(false)
const generateForm = reactive({
  month: dayjs().startOf('month')
})

const payOpen = ref(false)
const paying = ref(false)
const payFormRef = ref()
const activePaymentId = ref<Id | null>(null)

function routeQueryText(value: unknown) {
  if (Array.isArray(value)) {
    return routeQueryText(value[0])
  }
  if (value == null) {
    return ''
  }
  return String(value).trim()
}
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

const activeBillId = ref<Id | null>(null)
const historyOpen = ref(false)
const historyLoading = ref(false)
const historyRows = ref<PaymentRecordItem[]>([])
const historyReadonly = ref(false)

const displayRows = computed(() => rows.value)
const overdueCount = computed(() => displayRows.value.filter(item => Number(item.outstandingAmount || 0) > 0).length)
const overdueAmount = computed(() => displayRows.value.reduce((sum, item) => sum + Number(item.outstandingAmount || 0), 0))
const paidAmount = computed(() => displayRows.value.reduce((sum, item) => sum + Number(item.paidAmount || 0), 0))
const totalAmount = computed(() => displayRows.value.reduce((sum, item) => sum + Number(item.totalAmount || 0), 0))
const invalidBillCount = computed(() => displayRows.value.filter(item => Number(item.status) === 9).length)
const collectionRate = computed(() => {
  if (totalAmount.value <= 0) {
    return 0
  }
  return Math.round((paidAmount.value / totalAmount.value) * 100)
})
const currentMonthLabel = computed(() => (query.month ? dayjs(query.month).format('YYYY年MM月') : '全部账期'))
const currentSceneLabel = computed(() => {
  if (query.scene === 'ADMISSION') return '入住首期'
  if (query.scene === 'RESIDENT') return '在住周期'
  return '全部场景'
})
const riskSummaryText = computed(() => (
  summary.value.warningMessage
    || `欠费 ${summary.value.totalCount} 人，低余额 ${summary.value.pendingCount} 人，合同到期风险 ${summary.value.exceptionCount} 人`
))
const riskSummaryDescription = computed(() => (
  `今日收款 ${formatAmount(summary.value.todayAmount)} 元，累计欠费 ${formatAmount(summary.value.monthAmount)} 元`
))

function formatAmount(value?: number | string) {
  return Number(value || 0).toFixed(2)
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

function routeOptions() {
  return {
    lockScene: props.lockScene,
    defaultScene: props.defaultScene as BillCenterScene | undefined,
    defaultMonth: props.defaultCurrentMonth ? dayjs().format('YYYY-MM') : undefined,
    defaultPageNo: 1,
    defaultPageSize: 10
  }
}

function applyBillRouteState() {
  const state = parseBillCenterRouteState(route.query, routeOptions())
  query.month = state.month ? dayjs(state.month, 'YYYY-MM') : undefined
  query.keyword = state.keyword || ''
  query.elderId = normalizeResidentId(route.query as Record<string, unknown>)
  query.payMethod = state.payMethod
  query.scene = state.scene
  query.pageNo = state.pageNo
  query.pageSize = state.pageSize
}

function currentRouteState() {
  return {
    month: query.month ? dayjs(query.month).format('YYYY-MM') : undefined,
    keyword: query.keyword,
    payMethod: query.payMethod ? String(query.payMethod).toUpperCase() : undefined,
    scene: query.scene,
    pageNo: query.pageNo,
    pageSize: query.pageSize
  }
}

function buildQueryRouteQuery() {
  return buildBillCenterRouteQuery(currentRouteState(), route.query, routeOptions())
}

function hasSameQueryPayload(nextQuery: Record<string, string>) {
  const currentQuery = flattenBillCenterRouteQuery(route.query)
  const currentKeys = Object.keys(currentQuery)
  const nextKeys = Object.keys(nextQuery)
  if (currentKeys.length !== nextKeys.length) {
    return false
  }
  return nextKeys.every((key) => currentQuery[key] === nextQuery[key])
}

async function syncQueryToRoute() {
  const nextQuery = buildQueryRouteQuery()
  if (hasSameQueryPayload(nextQuery)) {
    return
  }
  skipNextBillRouteWatch.value = true
  billRouteSignature.value = buildBillCenterRouteSignature(nextQuery)
  await router.replace({ path: route.path, query: nextQuery })
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<BillItem> = await getBillPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      month: query.month ? dayjs(query.month).format('YYYY-MM') : undefined,
      elderId: query.elderId,
      keyword: query.keyword,
      scene: query.scene,
      payMethod: query.payMethod ? String(query.payMethod).toUpperCase() : undefined
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function fetchSummary() {
  try {
    summary.value = await getFinanceModuleEntrySummary({ moduleKey: 'BALANCE_WARN' })
  } catch {
    // keep page usable even when summary API is unavailable
  }
}

async function runQuery(resetPage = true) {
  if (resetPage) {
    query.pageNo = 1
  }
  await syncQueryToRoute()
  await Promise.all([fetchData(), fetchSummary()])
}

function reset() {
  const state = parseBillCenterRouteState({}, routeOptions())
  query.month = state.month ? dayjs(state.month, 'YYYY-MM') : undefined
  query.keyword = state.keyword || ''
  query.payMethod = state.payMethod
  query.scene = state.scene
  query.pageNo = state.pageNo
  query.pageSize = state.pageSize
  runQuery(false).catch(() => {})
}

function goSignedResident360() {
  if (!query.elderId) {
    message.warning('当前没有定位到长者')
    return
  }
  router.push({
    path: '/elder/resident-360',
    query: {
      residentId: String(query.elderId),
      residentName: signedLinkageContext.value.elderName || undefined,
      from: 'contractSigned'
    }
  })
}

function goSignedAccountList() {
  router.push({
    path: '/finance/accounts/list',
    query: {
      keyword: signedLinkageContext.value.elderName || query.keyword || undefined,
      elderId: query.elderId ? String(query.elderId) : undefined,
      source: 'contract_signed',
      entryScene: 'signed_onboarding'
    }
  })
}

async function onPageChange(page: number) {
  query.pageNo = page
  await syncQueryToRoute()
  await fetchData()
}

async function onPageSizeChange(current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  await syncQueryToRoute()
  await fetchData()
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
  activePaymentId.value = row.lastPaymentId || null
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
  if (!row?.id) {
    message.warning('该行账单缺少ID，无法查看详情')
    return
  }
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
  activeBillId.value = row.billMonthlyId
  activePaymentId.value = row.id
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

watch(
  () => route.query,
  (queryMap) => {
    const nextSignature = buildBillCenterRouteSignature(queryMap)
    if (skipNextBillRouteWatch.value) {
      skipNextBillRouteWatch.value = false
      billRouteSignature.value = nextSignature
      return
    }
    if (nextSignature === billRouteSignature.value) {
      return
    }
    billRouteSignature.value = nextSignature
    applyBillRouteState()
    runQuery(false).catch(() => {})
  },
  { deep: true }
)

onMounted(async () => {
  applyBillRouteState()
  billRouteSignature.value = buildBillCenterRouteSignature(route.query)
  await runQuery(false)
})
</script>

<style scoped>
.bill-overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.overview-card {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid #dce9f2;
  background: rgba(255, 255, 255, 0.86);
}

.overview-card span,
.table-head span,
.bill-insight-card span {
  color: #6d8aa3;
  font-size: 12px;
}

.overview-card strong {
  color: #173854;
  font-size: 24px;
}

.overview-card small,
.bill-insight-card small,
.bill-person-cell span {
  color: #7a97b0;
  font-size: 12px;
}

.linkage-alert {
  margin-top: 2px;
}

.bill-workspace {
  padding: 16px;
}

.bill-insight-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(0, 1fr);
  gap: 12px;
  margin-bottom: 14px;
}

.bill-insight-card {
  display: grid;
  gap: 8px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid #dce9f2;
  background: linear-gradient(135deg, rgba(232, 247, 253, 0.96) 0%, rgba(255, 255, 255, 0.96) 100%);
}

.bill-insight-card.is-soft {
  background: linear-gradient(135deg, rgba(246, 250, 253, 0.96) 0%, rgba(255, 255, 255, 0.98) 100%);
}

.bill-insight-card strong,
.table-head strong {
  color: #173854;
  font-size: 16px;
  line-height: 1.5;
}

.action-toolbar,
.action-toolbar-main,
.table-head,
.bill-person-cell,
.row-action-links,
.pager-row {
  display: flex;
  align-items: center;
}

.action-toolbar {
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.action-toolbar-main,
.row-action-links {
  gap: 8px;
  flex-wrap: wrap;
}

.table-head {
  justify-content: space-between;
  gap: 12px;
  flex: 1;
  flex-wrap: wrap;
}

.bill-table-frame {
  border: 1px solid #e4edf4;
  border-radius: 18px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.95);
}

.bill-person-cell {
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.bill-person-cell strong {
  color: #173854;
}

.amount-text,
.amount-subtext {
  font-variant-numeric: tabular-nums;
}

.amount-text {
  color: #173854;
  font-weight: 700;
}

.amount-text.is-positive {
  color: #1f8f63;
}

.amount-subtext {
  color: #4f6f86;
}

.amount-chip,
.pay-method-pill {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.amount-chip {
  font-variant-numeric: tabular-nums;
}

.amount-chip.is-danger {
  background: #fff1f0;
  color: #cf3f3f;
}

.amount-chip.is-safe {
  background: #edf8f2;
  color: #1f8f63;
}

.pay-method-pill {
  background: #eef8fc;
  color: #1b6282;
}

.row-action-links {
  gap: 2px;
}

.pager-row {
  justify-content: flex-end;
  margin-top: 16px;
}

.finance-vxe-toolbar :deep(.vxe-toolbar) {
  padding: 14px 16px 10px;
  border-bottom: 1px solid #e8f0f6;
  background: rgba(246, 250, 253, 0.88);
}

.finance-vxe-table :deep(.vxe-table--header-wrapper) {
  background: #f7fbfe;
}

.finance-vxe-table :deep(.vxe-header--column),
.finance-vxe-table :deep(.vxe-body--column) {
  height: 54px;
}

.finance-vxe-table :deep(.vxe-header--column) {
  color: #59758c;
  font-weight: 700;
}

.finance-vxe-table :deep(.vxe-body--row:hover) {
  background: rgba(234, 246, 252, 0.82);
}

@media (max-width: 1200px) {
  .bill-overview-grid,
  .bill-insight-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .bill-overview-grid,
  .bill-insight-grid {
    grid-template-columns: 1fr;
  }

  .table-head,
  .action-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .pager-row {
    justify-content: stretch;
  }
}
</style>
