<template>
  <PageContainer title="对账中心" subTitle="按日核对收款与账单变动">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
        <a-form-item label="日期">
          <a-date-picker v-model:value="query.date" />
        </a-form-item>
        <a-form-item label="打印备注">
          <a-input v-model:value="query.printRemark" allow-clear placeholder="例如：日结复核版" style="width: 200px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="reconcile">生成对账</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button @click="severityFilter = 'HIGH'">仅看高风险</a-button>
            <a-button @click="severityFilter = 'ALL'">全部风险</a-button>
            <a-button @click="go('/finance/reconcile/exception')">异常处理</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <StatefulBlock style="margin-top: 16px;" :loading="loading" :error="errorMessage" @retry="loadSummary">
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="账单已收未核销" :value="summary.billPaidUnmatchedCount" /></a-card></a-col>
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="重复/冲正异常" :value="summary.duplicatedOrReversalPendingCount" /></a-card></a-col>
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="发票未关联" :value="summary.invoiceUnlinkedCount" /></a-card></a-col>
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="当日异常数" :value="todayExceptionCount" /></a-card></a-col>
      </a-row>
      <a-alert v-if="todayExceptionCount > 0 || result?.mismatchFlag" style="margin-top: 12px;" type="warning" show-icon message="存在对账异常，请尽快处理并核销" />
      <a-alert
        v-if="moduleSummary?.warningMessage"
        style="margin-top: 12px;"
        type="warning"
        show-icon
        :message="moduleSummary.warningMessage"
        :description="`当日收款 ${Number(moduleSummary.todayAmount || 0).toFixed(2)} 元；异常总数 ${moduleSummary.totalCount || 0}`"
      />

      <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="对账结果">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-statistic title="日期" :value="result?.date || '-'" />
          </a-col>
          <a-col :span="8">
            <a-statistic title="当日收款" :value="result?.totalReceived ?? 0" />
          </a-col>
          <a-col :span="8">
            <a-statistic title="是否异常">
              <template #value>
                <a-tag :color="result?.mismatchFlag ? 'red' : 'green'">{{ result?.mismatchFlag ? '存在差异' : '正常' }}</a-tag>
              </template>
            </a-statistic>
          </a-col>
        </a-row>
      </a-card>
      <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="当日异常明细（分级）">
        <vxe-table border stripe show-overflow :data="filteredExceptions" height="260">
          <vxe-column field="occurredAt" title="发生时间" width="180" />
          <vxe-column field="exceptionTypeLabel" title="异常类型" width="180" />
          <vxe-column field="elderName" title="长者" width="130" />
          <vxe-column field="amount" title="金额" width="120" />
          <vxe-column title="等级" width="90">
            <template #default="{ row }">
              <a-tag :color="exceptionLevelColor(row)">{{ exceptionLevel(row) }}</a-tag>
            </template>
          </vxe-column>
          <vxe-column field="detail" title="异常描述" min-width="220" />
          <vxe-column title="操作" width="180" fixed="right">
            <template #default="{ row }">
              <a-space>
                <a-button type="link" @click="go(`/finance/bill/${row.billId}`)" v-if="row.billId">查看账单</a-button>
                <a-button type="link" @click="go('/finance/reconcile/exception')">去处理</a-button>
              </a-space>
            </template>
          </vxe-column>
        </vxe-table>
      </a-card>
    </StatefulBlock>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="对账历史">
      <a-form layout="inline" :model="historyQuery" class="search-form">
        <a-form-item label="开始日期">
          <a-date-picker v-model:value="historyQuery.from" />
        </a-form-item>
        <a-form-item label="结束日期">
          <a-date-picker v-model:value="historyQuery.to" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchHistory">查询</a-button>
            <a-button @click="resetHistory">重置</a-button>
            <a-button @click="exportHistory">导出</a-button>
            <a-button @click="printHistory">打印当前</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <vxe-table border stripe show-overflow :data="history" height="320" :loading="historyLoading">
        <vxe-column field="reconcileDate" title="对账日期" width="140">
          <template #default="{ row }">
            <span>{{ row.reconcileDate || row.date }}</span>
          </template>
        </vxe-column>
        <vxe-column field="totalReceived" title="收款总额" width="140" />
        <vxe-column field="mismatchFlag" title="是否异常" width="120">
          <template #default="{ row }">
            <a-tag :color="row.mismatchFlag ? 'red' : 'green'">{{ row.mismatchFlag ? '异常' : '正常' }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column field="remark" title="备注" min-width="200" />
        <vxe-column field="createTime" title="生成时间" width="180" />
      </vxe-table>

      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="historyQuery.pageNo"
        :page-size="historyQuery.pageSize"
        :total="historyTotal"
        show-size-changer
        @change="onHistoryPageChange"
        @showSizeChange="onHistoryPageSizeChange"
      />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { exportFinanceReconcileHistoryCsv, reconcileDaily, getReconcilePage, getFinanceModuleEntrySummary, getFinanceReconcileExceptions, getFinanceWorkbenchOverview } from '../../api/finance'
import type { FinanceModuleEntrySummary, FinanceReconcileCard, FinanceReconcileExceptionItem, PageResult, ReconcileDailyItem } from '../../types'
import { printTableReport } from '../../utils/print'

const router = useRouter()
const query = ref({
  date: dayjs(),
  printRemark: ''
})

const loading = ref(false)
const errorMessage = ref('')
const result = ref<ReconcileDailyItem | null>(null)
const summary = ref<FinanceReconcileCard>({
  billPaidUnmatchedCount: 0,
  duplicatedOrReversalPendingCount: 0,
  invoiceUnlinkedCount: 0
})
const todayExceptions = ref<FinanceReconcileExceptionItem[]>([])
const todayExceptionCount = computed(() => todayExceptions.value.length)
const moduleSummary = ref<FinanceModuleEntrySummary | null>(null)
const severityFilter = ref<'ALL' | 'HIGH'>('ALL')
const filteredExceptions = computed(() => (todayExceptions.value || [])
  .filter(item => severityFilter.value === 'ALL' || exceptionLevel(item) === '高'))

const historyQuery = ref({
  from: dayjs().subtract(14, 'day'),
  to: dayjs(),
  pageNo: 1,
  pageSize: 10
})
const historyLoading = ref(false)
const history = ref<ReconcileDailyItem[]>([])
const historyTotal = ref(0)

async function reconcile() {
  loading.value = true
  errorMessage.value = ''
  try {
    result.value = await reconcileDaily({
      date: dayjs(query.value.date).format('YYYY-MM-DD')
    })
    await loadSummary()
    await fetchHistory()
    message.success('对账已生成')
  } catch (error: any) {
    errorMessage.value = error?.message || '生成对账失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

function reset() {
  query.value.date = dayjs()
  query.value.printRemark = ''
  result.value = null
  loadSummary()
}

async function fetchHistory() {
  historyLoading.value = true
  try {
    const res: PageResult<ReconcileDailyItem> = await getReconcilePage({
      pageNo: historyQuery.value.pageNo,
      pageSize: historyQuery.value.pageSize,
      from: dayjs(historyQuery.value.from).format('YYYY-MM-DD'),
      to: dayjs(historyQuery.value.to).format('YYYY-MM-DD')
    })
    history.value = res.list
    historyTotal.value = res.total
  } finally {
    historyLoading.value = false
  }
}

async function loadSummary() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [overview, exceptions, summaryEntry] = await Promise.all([
      getFinanceWorkbenchOverview(),
      getFinanceReconcileExceptions({
        date: dayjs(query.value.date).format('YYYY-MM-DD')
      }),
      getFinanceModuleEntrySummary({ moduleKey: 'RECONCILE_CENTER' })
    ])
    summary.value = overview.reconcile || {
      billPaidUnmatchedCount: 0,
      duplicatedOrReversalPendingCount: 0,
      invoiceUnlinkedCount: 0
    }
    todayExceptions.value = exceptions || []
    moduleSummary.value = summaryEntry
  } catch (error: any) {
    errorMessage.value = error?.message || '加载对账摘要失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

function exceptionLevel(item: FinanceReconcileExceptionItem) {
  const type = String(item.exceptionType || '').toUpperCase()
  const amount = Number(item.amount || 0)
  if (type === 'BILL_PAID_UNMATCHED' && amount >= 1000) return '高'
  if (type === 'DUPLICATED_PAYMENT') return '高'
  if (type === 'INVOICE_UNLINKED' && amount >= 2000) return '高'
  return '中'
}

function exceptionLevelColor(item: FinanceReconcileExceptionItem) {
  return exceptionLevel(item) === '高' ? 'red' : 'orange'
}

function resetHistory() {
  historyQuery.value.from = dayjs().subtract(14, 'day')
  historyQuery.value.to = dayjs()
  historyQuery.value.pageNo = 1
  fetchHistory()
}

async function exportHistory() {
  try {
    await exportFinanceReconcileHistoryCsv({
      from: dayjs(historyQuery.value.from).format('YYYY-MM-DD'),
      to: dayjs(historyQuery.value.to).format('YYYY-MM-DD')
    })
    message.success('导出成功')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  }
}

function printHistory() {
  try {
    printTableReport({
      title: '对账历史',
      subtitle: `${dayjs(historyQuery.value.from).format('YYYY-MM-DD')} ~ ${dayjs(historyQuery.value.to).format('YYYY-MM-DD')}；备注：${query.value.printRemark || '-'}；当日异常数：${todayExceptionCount.value}`,
      columns: [
        { key: 'reconcileDate', title: '对账日期' },
        { key: 'totalReceived', title: '收款总额' },
        { key: 'mismatchText', title: '是否异常' },
        { key: 'remark', title: '备注' },
        { key: 'createTime', title: '生成时间' }
      ],
      rows: history.value.map(item => ({
        reconcileDate: item.reconcileDate || item.date || '-',
        totalReceived: item.totalReceived ?? 0,
        mismatchText: item.mismatchFlag ? '异常' : '正常',
        remark: item.remark || '-',
        createTime: item.createTime || '-'
      }))
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

function onHistoryPageChange(page: number) {
  historyQuery.value.pageNo = page
  fetchHistory()
}

function onHistoryPageSizeChange(current: number, size: number) {
  historyQuery.value.pageNo = 1
  historyQuery.value.pageSize = size
  fetchHistory()
}

function go(path: string) {
  router.push(path)
}

onMounted(async () => {
  await Promise.all([loadSummary(), fetchHistory()])
})
</script>
