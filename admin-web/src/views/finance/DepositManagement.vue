<template>
  <PageContainer title="押金管理" subTitle="押金标准、缴纳/扣款/退还登记与在押余额台账">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query">
        <a-form-item label="长者">
          <a-input v-model:value="query.keyword" allow-clear placeholder="按姓名搜索" style="width: 200px" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear placeholder="全部状态" style="width: 150px">
            <a-select-option value="UNPAID">未缴</a-select-option>
            <a-select-option value="PARTIAL">部分缴</a-select-option>
            <a-select-option value="PAID">已缴清</a-select-option>
            <a-select-option value="CLOSED">已结清</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="runSearch">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button type="primary" ghost @click="openTxn('PAY')">押金缴纳</a-button>
            <a-button @click="openStandard">押金标准</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="[16, 16]" style="margin-top: 16px;">
      <a-col :xs="24" :md="12" :xl="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="在押余额合计" :value="Number(summary?.totalBalance || 0)" :precision="2" suffix="元" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12" :xl="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic
            title="未缴清人数"
            :value="Number(summary?.shortfallCount || 0)"
            :value-style="Number(summary?.shortfallCount || 0) > 0 ? { color: '#cf1322' } : undefined"
            suffix="人"
          />
          <div class="stat-hint">合计差额 {{ formatAmount(summary?.shortfallAmount) }} 元</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12" :xl="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="累计已扣" :value="Number(summary?.totalDeducted || 0)" :precision="2" suffix="元" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12" :xl="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="累计已退" :value="Number(summary?.totalRefunded || 0)" :precision="2" suffix="元" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <vxe-table
        border
        stripe
        show-overflow="title"
        :loading="loading"
        :data="rows"
        height="520"
        :row-class-name="rowClassName"
      >
        <vxe-column field="elderName" title="长者" width="130" />
        <vxe-column field="careLevel" title="护理等级" width="110" />
        <vxe-column title="房间 / 床位" width="150">
          <template #default="{ row }">{{ (row.roomNo || '-') + ' / ' + (row.bedNo || '-') }}</template>
        </vxe-column>
        <vxe-column field="standardAmount" title="应缴标准" width="110">
          <template #default="{ row }">{{ formatAmount(row.standardAmount) }}</template>
        </vxe-column>
        <vxe-column field="paidAmount" title="已缴" width="110">
          <template #default="{ row }">{{ formatAmount(row.paidAmount) }}</template>
        </vxe-column>
        <vxe-column field="shortfallAmount" title="应缴差额" width="110">
          <template #default="{ row }">
            <span :class="Number(row.shortfallAmount || 0) > 0 ? 'is-danger-text' : ''">
              {{ formatAmount(row.shortfallAmount) }}
            </span>
          </template>
        </vxe-column>
        <vxe-column field="deductedAmount" title="已扣" width="100">
          <template #default="{ row }">{{ formatAmount(row.deductedAmount) }}</template>
        </vxe-column>
        <vxe-column field="refundedAmount" title="已退" width="100">
          <template #default="{ row }">{{ formatAmount(row.refundedAmount) }}</template>
        </vxe-column>
        <vxe-column field="balanceAmount" title="在押余额" width="120">
          <template #default="{ row }"><strong>{{ formatAmount(row.balanceAmount) }}</strong></template>
        </vxe-column>
        <vxe-column field="statusText" title="状态" width="100">
          <template #default="{ row }">
            <a-tag :color="statusColor(row.status)">{{ row.statusText || row.status }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column title="操作" width="240" fixed="right">
          <template #default="{ row }">
            <a-space>
              <a-button type="link" size="small" @click="openDetail(row)">详情</a-button>
              <a-button type="link" size="small" @click="openTxn('PAY', row)">缴纳</a-button>
              <a-button type="link" size="small" @click="openTxn('DEDUCT', row)">扣款</a-button>
              <a-button type="link" size="small" danger @click="openTxn('REFUND', row)">退还</a-button>
            </a-space>
          </template>
        </vxe-column>
      </vxe-table>
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
    </a-card>

    <a-modal
      v-model:open="txnOpen"
      :title="txnTitle"
      width="600"
      :confirm-loading="txnSaving"
      @ok="submitTxn"
    >
      <a-form layout="vertical" :model="txnForm" :rules="txnRules" ref="txnFormRef">
        <a-form-item label="长者" name="elderId">
          <a-select
            v-model:value="txnForm.elderId"
            show-search
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            placeholder="选择长者"
            style="width: 100%"
            @search="searchElders"
            @change="onElderChange"
          />
        </a-form-item>
        <a-alert
          v-if="activeAccount"
          type="info"
          show-icon
          style="margin-bottom: 12px;"
          :message="`应缴 ${formatAmount(activeAccount.standardAmount)} · 已缴 ${formatAmount(activeAccount.paidAmount)} · 在押余额 ${formatAmount(activeAccount.balanceAmount)} 元`"
          :description="txnType === 'PAY'
            ? `当前应缴差额 ${formatAmount(activeAccount.shortfallAmount)} 元`
            : `本次${txnType === 'DEDUCT' ? '扣款' : '退还'}不能超过在押余额 ${formatAmount(activeAccount.balanceAmount)} 元`"
        />
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="金额（元）" name="amount">
              <a-input-number v-model:value="txnForm.amount" :min="0.01" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="发生时间">
              <a-date-picker v-model:value="txnForm.occurredAt" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item v-if="txnType === 'PAY'" label="收款方式">
          <a-select v-model:value="txnForm.payMethod" allow-clear placeholder="选择方式">
            <a-select-option value="CASH">现金</a-select-option>
            <a-select-option value="CARD">刷卡</a-select-option>
            <a-select-option value="BANK">转账</a-select-option>
            <a-select-option value="ALIPAY">支付宝</a-select-option>
            <a-select-option value="WECHAT">微信</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-else label="原因" name="reason">
          <a-input
            v-model:value="txnForm.reason"
            :placeholder="txnType === 'DEDUCT' ? '必填：如损坏赔偿、欠费抵扣' : '必填：如退住结算退还'"
          />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="txnForm.remark" allow-clear />
        </a-form-item>
      </a-form>
      <div v-if="txnError" class="txn-error">{{ txnError }}</div>
    </a-modal>

    <a-drawer v-model:open="detailOpen" width="760" :title="`押金台账 · ${activeRow?.elderName || ''}`">
      <a-descriptions bordered :column="2" size="small">
        <a-descriptions-item label="应缴标准">{{ formatAmount(activeRow?.standardAmount) }} 元</a-descriptions-item>
        <a-descriptions-item label="已缴">{{ formatAmount(activeRow?.paidAmount) }} 元</a-descriptions-item>
        <a-descriptions-item label="已扣">{{ formatAmount(activeRow?.deductedAmount) }} 元</a-descriptions-item>
        <a-descriptions-item label="已退">{{ formatAmount(activeRow?.refundedAmount) }} 元</a-descriptions-item>
        <a-descriptions-item label="在押余额">{{ formatAmount(activeRow?.balanceAmount) }} 元</a-descriptions-item>
        <a-descriptions-item label="状态">{{ activeRow?.statusText }}</a-descriptions-item>
      </a-descriptions>
      <div class="txn-title">押金流水</div>
      <vxe-table border stripe show-overflow="title" :loading="txnLoading" :data="txnRows" height="380">
        <vxe-column field="occurredAt" title="时间" width="170" />
        <vxe-column field="txnTypeText" title="类型" width="90" />
        <vxe-column field="amount" title="金额" width="110">
          <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
        </vxe-column>
        <vxe-column field="balanceAfter" title="操作后余额" width="120">
          <template #default="{ row }">{{ formatAmount(row.balanceAfter) }}</template>
        </vxe-column>
        <vxe-column field="payMethod" title="方式" width="100" />
        <vxe-column field="reason" title="原因" min-width="140" />
        <vxe-column field="remark" title="备注" min-width="120" />
      </vxe-table>
      <a-empty v-if="!txnLoading && !txnRows.length" description="还没有押金流水" />
      <template #footer>
        <a-space>
          <a-button @click="detailOpen = false">关闭</a-button>
          <a-button @click="doRefreshStandard">按当前护理等级刷新应缴</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-modal v-model:open="standardOpen" title="押金标准" width="720" :footer="null">
      <a-form layout="inline" :model="standardForm" style="margin-bottom: 12px;">
        <a-form-item label="名称">
          <a-input v-model:value="standardForm.standardName" placeholder="如：三级护理押金" style="width: 160px" />
        </a-form-item>
        <a-form-item label="护理等级">
          <a-input v-model:value="standardForm.careLevel" placeholder="留空=通用" style="width: 120px" />
        </a-form-item>
        <a-form-item label="金额">
          <a-input-number v-model:value="standardForm.amount" :min="0" :precision="2" style="width: 120px" />
        </a-form-item>
        <a-form-item label="生效日">
          <a-date-picker v-model:value="standardForm.effectiveFrom" style="width: 140px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" :loading="standardSaving" @click="submitStandard">
              {{ standardForm.id ? '保存修改' : '新增标准' }}
            </a-button>
            <a-button v-if="standardForm.id" @click="resetStandardForm">取消编辑</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      <vxe-table border stripe :data="standards" height="320">
        <vxe-column field="standardName" title="名称" min-width="150" />
        <vxe-column field="careLevel" title="护理等级" width="120">
          <template #default="{ row }">{{ row.careLevel || '通用' }}</template>
        </vxe-column>
        <vxe-column field="amount" title="金额(元)" width="120">
          <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
        </vxe-column>
        <vxe-column field="effectiveFrom" title="生效日期" width="130" />
        <vxe-column title="操作" width="140" fixed="right">
          <template #default="{ row }">
            <a-space>
              <a-button type="link" size="small" @click="editStandard(row)">编辑</a-button>
              <a-button type="link" size="small" danger @click="removeStandard(row)">删除</a-button>
            </a-space>
          </template>
        </vxe-column>
      </vxe-table>
      <div class="standard-hint">按护理等级就近匹配，没有匹配项时回落到「通用」标准。</div>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import {
  deleteDepositStandard,
  getDepositPage,
  getDepositStandards,
  getDepositSummary,
  getDepositTransactions,
  refreshDepositStandard,
  registerDepositDeduct,
  registerDepositPay,
  registerDepositRefund,
  saveDepositStandard,
  type DepositAccountItem,
  type DepositStandardItem,
  type DepositSummary,
  type DepositTransactionItem
} from '../../api/deposit'
import type { Id, PageResult } from '../../types'

const { elderOptions, elderLoading, searchElders } = useElderOptions({ pageSize: 80 })

const loading = ref(false)
const rows = ref<DepositAccountItem[]>([])
const total = ref(0)
const summary = ref<DepositSummary | null>(null)
const query = reactive({
  keyword: '',
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const txnOpen = ref(false)
const txnSaving = ref(false)
const txnType = ref<'PAY' | 'DEDUCT' | 'REFUND'>('PAY')
const txnFormRef = ref<FormInstance>()
const txnForm = reactive({
  elderId: undefined as Id | undefined,
  amount: undefined as number | undefined,
  payMethod: 'CASH' as string | undefined,
  occurredAt: dayjs() as any,
  reason: '',
  remark: ''
})
const txnRules = computed<FormRules>(() => ({
  elderId: [{ required: true, message: '请选择长者' }],
  amount: [{ required: true, message: '请输入金额' }],
  ...(txnType.value === 'PAY' ? {} : { reason: [{ required: true, message: '请填写原因' }] })
}))
const activeAccount = ref<DepositAccountItem | null>(null)
const txnTitle = computed(() =>
  txnType.value === 'PAY' ? '押金缴纳登记' : txnType.value === 'DEDUCT' ? '押金扣款登记' : '押金退还登记'
)
const txnError = computed(() => {
  if (txnType.value === 'PAY') return ''
  const balance = Number(activeAccount.value?.balanceAmount || 0)
  if (Number(txnForm.amount || 0) > balance) {
    return `金额超过在押余额 ${formatAmount(balance)} 元`
  }
  return ''
})

const detailOpen = ref(false)
const activeRow = ref<DepositAccountItem | null>(null)
const txnLoading = ref(false)
const txnRows = ref<DepositTransactionItem[]>([])

const standardOpen = ref(false)
const standardSaving = ref(false)
const standards = ref<DepositStandardItem[]>([])
const standardForm = reactive({
  id: undefined as Id | undefined,
  standardName: '',
  careLevel: '',
  amount: undefined as number | undefined,
  effectiveFrom: dayjs() as any,
  remark: ''
})

function formatAmount(value?: number | string) {
  return Number(value || 0).toFixed(2)
}

function statusColor(status?: string) {
  switch (status) {
    case 'PAID':
      return 'green'
    case 'PARTIAL':
      return 'orange'
    case 'UNPAID':
      return 'red'
    case 'CLOSED':
      return 'default'
    default:
      return 'default'
  }
}

function rowClassName({ row }: { row: DepositAccountItem }) {
  return Number(row?.shortfallAmount || 0) > 0 ? 'is-shortfall-row' : ''
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<DepositAccountItem> = await getDepositPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status || undefined,
      keyword: query.keyword.trim() || undefined
    })
    rows.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function fetchSummary() {
  summary.value = await getDepositSummary()
}

function runSearch() {
  query.pageNo = 1
  fetchData()
  fetchSummary()
}

function reset() {
  query.keyword = ''
  query.status = undefined
  runSearch()
}

function onPageChange(page: number, pageSize: number) {
  query.pageNo = page
  query.pageSize = pageSize
  fetchData()
}

function onPageSizeChange(_current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

function openTxn(type: 'PAY' | 'DEDUCT' | 'REFUND', row?: DepositAccountItem) {
  txnType.value = type
  txnForm.elderId = row?.elderId
  txnForm.amount = undefined
  txnForm.payMethod = 'CASH'
  txnForm.occurredAt = dayjs()
  txnForm.reason = ''
  txnForm.remark = ''
  activeAccount.value = row || null
  if (type === 'PAY' && row && Number(row.shortfallAmount || 0) > 0) {
    txnForm.amount = Number(row.shortfallAmount)
  }
  txnOpen.value = true
}

function onElderChange(elderId: Id) {
  const matched = rows.value.find((item) => String(item.elderId) === String(elderId))
  activeAccount.value = matched || null
  if (txnType.value === 'PAY' && matched && Number(matched.shortfallAmount || 0) > 0) {
    txnForm.amount = Number(matched.shortfallAmount)
  }
}

async function submitTxn() {
  if (txnSaving.value) return
  await txnFormRef.value?.validate?.()
  if (txnError.value) {
    message.warning(txnError.value)
    return
  }
  txnSaving.value = true
  try {
    const payload = {
      elderId: txnForm.elderId as Id,
      amount: Number(txnForm.amount),
      payMethod: txnType.value === 'PAY' ? txnForm.payMethod || undefined : undefined,
      occurredAt: txnForm.occurredAt ? dayjs(txnForm.occurredAt).format('YYYY-MM-DD HH:mm:ss') : undefined,
      reason: txnForm.reason.trim() || undefined,
      remark: txnForm.remark.trim() || undefined
    }
    if (txnType.value === 'PAY') {
      await registerDepositPay(payload)
      message.success('押金缴纳已登记')
    } else if (txnType.value === 'DEDUCT') {
      await registerDepositDeduct(payload)
      message.success('押金扣款已登记')
    } else {
      await registerDepositRefund(payload)
      message.success('押金退还已登记')
    }
    txnOpen.value = false
    await Promise.all([fetchData(), fetchSummary()])
    if (detailOpen.value && activeRow.value) {
      await loadTransactions(activeRow.value.elderId)
    }
  } finally {
    txnSaving.value = false
  }
}

async function openDetail(row: DepositAccountItem) {
  activeRow.value = row
  detailOpen.value = true
  await loadTransactions(row.elderId)
}

async function loadTransactions(elderId: Id) {
  txnLoading.value = true
  try {
    txnRows.value = await getDepositTransactions(elderId)
  } finally {
    txnLoading.value = false
  }
}

async function doRefreshStandard() {
  const row = activeRow.value
  if (!row) return
  const updated = await refreshDepositStandard(row.elderId)
  activeRow.value = updated
  message.success('应缴标准已按当前护理等级刷新')
  await Promise.all([fetchData(), fetchSummary()])
}

async function openStandard() {
  standardOpen.value = true
  resetStandardForm()
  standards.value = await getDepositStandards()
}

function resetStandardForm() {
  standardForm.id = undefined
  standardForm.standardName = ''
  standardForm.careLevel = ''
  standardForm.amount = undefined
  standardForm.effectiveFrom = dayjs()
  standardForm.remark = ''
}

function editStandard(row: DepositStandardItem) {
  standardForm.id = row.id
  standardForm.standardName = row.standardName
  standardForm.careLevel = row.careLevel || ''
  standardForm.amount = Number(row.amount)
  standardForm.effectiveFrom = row.effectiveFrom ? dayjs(row.effectiveFrom) : dayjs()
  standardForm.remark = row.remark || ''
}

async function submitStandard() {
  if (!standardForm.standardName.trim()) {
    message.warning('请填写标准名称')
    return
  }
  if (standardForm.amount == null) {
    message.warning('请填写押金金额')
    return
  }
  standardSaving.value = true
  try {
    await saveDepositStandard({
      id: standardForm.id,
      standardName: standardForm.standardName.trim(),
      careLevel: standardForm.careLevel.trim() || undefined,
      amount: Number(standardForm.amount),
      effectiveFrom: dayjs(standardForm.effectiveFrom).format('YYYY-MM-DD'),
      remark: standardForm.remark.trim() || undefined
    })
    message.success('押金标准已保存')
    resetStandardForm()
    standards.value = await getDepositStandards()
  } finally {
    standardSaving.value = false
  }
}

function removeStandard(row: DepositStandardItem) {
  Modal.confirm({
    title: `删除押金标准「${row.standardName}」？`,
    content: '删除后新建押金账户将按其他标准或通用标准取值，已生成的账户金额不变。',
    okType: 'danger',
    onOk: async () => {
      await deleteDepositStandard(row.id)
      message.success('已删除')
      standards.value = await getDepositStandards()
    }
  })
}

onMounted(async () => {
  await Promise.all([fetchData(), fetchSummary()])
})
</script>

<style scoped>
.pager-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.stat-hint,
.standard-hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-tertiary, #999);
}

.is-danger-text {
  color: #cf1322;
  font-weight: 600;
}

.txn-title {
  margin: 16px 0 8px;
  font-size: 14px;
  font-weight: 600;
}

.txn-error {
  margin-top: 4px;
  font-size: 12px;
  color: #cf1322;
}

:deep(.vxe-body--row.is-shortfall-row) td {
  background-color: rgba(207, 19, 34, 0.06) !important;
}
</style>
