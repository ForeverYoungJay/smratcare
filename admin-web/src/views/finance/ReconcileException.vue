<template>
  <PageContainer title="对账异常处理" subTitle="短款/多款/重复/跨期等异常明细与处理建议">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query">
        <a-form-item label="日期">
          <a-date-picker v-model:value="query.date" style="width: 150px" />
        </a-form-item>
        <a-form-item label="异常类型">
          <a-select v-model:value="query.type" allow-clear style="width: 180px">
            <a-select-option value="BILL_PAID_UNMATCHED">账单已收未核销</a-select-option>
            <a-select-option value="DUPLICATED_PAYMENT">收款重复/冲正未完成</a-select-option>
            <a-select-option value="INVOICE_UNLINKED">发票未关联账单</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="风险等级">
          <a-select v-model:value="query.severity" allow-clear style="width: 140px">
            <a-select-option value="HIGH">高风险</a-select-option>
            <a-select-option value="MEDIUM">中风险</a-select-option>
            <a-select-option value="LOW">低风险</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">查询</a-button>
            <a-button @click="quickType('DUPLICATED_PAYMENT')">仅看重复收款</a-button>
            <a-button @click="quickType('INVOICE_UNLINKED')">仅看发票未关联</a-button>
            <a-button @click="exportCsvReport">导出CSV</a-button>
            <a-button @click="printCurrent">打印当前</a-button>
            <a-input v-model:value="query.printRemark" allow-clear placeholder="打印备注" style="width: 180px" />
            <a-button @click="go('/finance/reconcile/ledger-health')">财务一致性巡检</a-button>
            <a-button @click="go('/finance/reconcile/center')">返回对账中心</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <StatefulBlock style="margin-top: 16px;" :loading="loading" :error="errorMessage" @retry="loadData">
      <a-row :gutter="[12, 12]" style="margin-bottom: 12px;">
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="异常总数" :value="filteredRows.length" /></a-card></a-col>
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="高风险数" :value="highRiskCount" /></a-card></a-col>
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="异常金额" :value="totalAmount" suffix="元" :precision="2" /></a-card></a-col>
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="高风险金额" :value="highRiskAmount" suffix="元" :precision="2" /></a-card></a-col>
      </a-row>
      <a-card class="card-elevated" :bordered="false">
        <vxe-table border stripe show-overflow :loading="loading" :data="filteredRows" height="560">
          <vxe-column field="occurredAt" title="发生时间" width="180" />
          <vxe-column field="exceptionTypeLabel" title="异常类型" width="180">
            <template #default="{ row }">
              <a-tag color="volcano">{{ row.exceptionTypeLabel }}</a-tag>
            </template>
          </vxe-column>
          <vxe-column title="风险等级" width="100">
            <template #default="{ row }">
              <a-tag :color="severityColor(row)">{{ severityText(row) }}</a-tag>
            </template>
          </vxe-column>
          <vxe-column field="elderName" title="长者" width="140">
            <template #default="{ row }">{{ row.elderName || '-' }}</template>
          </vxe-column>
          <vxe-column field="amount" title="涉及金额" width="120" />
          <vxe-column field="detail" title="异常描述" min-width="240" />
          <vxe-column field="suggestion" title="处理建议" min-width="220" />
          <vxe-column title="操作" width="180" fixed="right">
            <template #default="{ row }">
              <a-space>
                <a-button v-if="row.billId" type="link" @click="go(`/finance/bill/${row.billId}`)">查看账单</a-button>
                <a-button type="link" @click="go('/finance/payments/register?from=reconcile_exception')">去处理</a-button>
              </a-space>
            </template>
          </vxe-column>
        </vxe-table>
      </a-card>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { exportFinanceReconcileExceptionsCsv, getFinanceReconcileExceptions } from '../../api/finance'
import type { FinanceReconcileExceptionItem } from '../../types'
import { printTableReport } from '../../utils/print'

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const rows = ref<FinanceReconcileExceptionItem[]>([])
const query = ref({
  date: dayjs(),
  type: undefined as string | undefined,
  severity: undefined as 'HIGH' | 'MEDIUM' | 'LOW' | undefined,
  printRemark: ''
})
const filteredRows = computed(() => (rows.value || [])
  .filter(item => !query.value.severity || severityText(item) === (query.value.severity === 'HIGH' ? '高' : query.value.severity === 'MEDIUM' ? '中' : '低')))
const totalAmount = computed(() => filteredRows.value.reduce((sum, item) => sum + Number(item.amount || 0), 0))
const highRiskRows = computed(() => filteredRows.value.filter(item => severityText(item) === '高'))
const highRiskCount = computed(() => highRiskRows.value.length)
const highRiskAmount = computed(() => highRiskRows.value.reduce((sum, item) => sum + Number(item.amount || 0), 0))

function go(path: string) {
  router.push(path)
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    rows.value = await getFinanceReconcileExceptions({
      date: dayjs(query.value.date).format('YYYY-MM-DD'),
      type: query.value.type
    })
  } catch (error: any) {
    errorMessage.value = error?.message || '加载对账异常失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

function severityText(item: FinanceReconcileExceptionItem) {
  const type = String(item.exceptionType || '').toUpperCase()
  const amount = Number(item.amount || 0)
  if (type === 'DUPLICATED_PAYMENT') return '高'
  if (type === 'BILL_PAID_UNMATCHED' && amount >= 1000) return '高'
  if (type === 'INVOICE_UNLINKED' && amount >= 2000) return '高'
  if (amount >= 500) return '中'
  return '低'
}

function severityColor(item: FinanceReconcileExceptionItem) {
  const level = severityText(item)
  if (level === '高') return 'red'
  if (level === '中') return 'orange'
  return 'green'
}

function quickType(type: string) {
  query.value.type = type
  query.value.severity = undefined
  loadData()
}

async function exportCsvReport() {
  try {
    await exportFinanceReconcileExceptionsCsv({
      date: dayjs(query.value.date).format('YYYY-MM-DD'),
      type: query.value.type
    })
    message.success('导出成功')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  }
}

function printCurrent() {
  try {
    printTableReport({
      title: '对账异常处理',
      subtitle: `日期：${dayjs(query.value.date).format('YYYY-MM-DD')}；类型：${query.value.type || '全部'}；风险：${query.value.severity || '全部'}；备注：${query.value.printRemark || '-'}`,
      columns: [
        { key: 'occurredAt', title: '发生时间' },
        { key: 'exceptionTypeLabel', title: '异常类型' },
        { key: 'severity', title: '风险等级' },
        { key: 'elderName', title: '长者' },
        { key: 'amount', title: '涉及金额' },
        { key: 'detail', title: '异常描述' },
        { key: 'suggestion', title: '处理建议' }
      ],
      rows: filteredRows.value.map(item => ({
        occurredAt: item.occurredAt || '-',
        exceptionTypeLabel: item.exceptionTypeLabel || '-',
        severity: severityText(item),
        elderName: item.elderName || '-',
        amount: item.amount || 0,
        detail: item.detail || '-',
        suggestion: item.suggestion || '-'
      }))
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

onMounted(loadData)
</script>
