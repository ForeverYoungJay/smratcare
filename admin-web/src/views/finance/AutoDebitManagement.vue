<template>
  <PageContainer title="自动扣费/催缴管理" subTitle="自动扣费运行态势与异常处理">
    <a-card class="card-elevated" :bordered="false">
      <a-space wrap>
        <a-date-picker v-model:value="query.date" />
        <a-button type="primary" @click="loadData">刷新</a-button>
        <a-button @click="exportCsvReport">导出CSV</a-button>
        <a-button @click="printCurrent">打印当前结果</a-button>
        <a-input v-model:value="query.printRemark" allow-clear placeholder="打印备注" style="width: 180px" />
        <a-button @click="go('/finance/accounts/list?filter=low_balance')">低余额账户</a-button>
        <a-button @click="go('/finance/reconcile/center?filter=unmatched')">对账异常</a-button>
      </a-space>
    </a-card>

    <StatefulBlock style="margin-top: 16px;" :loading="loading" :error="errorMessage" @retry="loadData">
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="今日应扣费" :value="summary?.shouldDeductCount || 0" /></a-card></a-col>
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="成功" :value="summary?.successCount || 0" /></a-card></a-col>
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="失败" :value="summary?.failedCount || 0" /></a-card></a-col>
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="待处理" :value="summary?.pendingHandleCount || 0" /></a-card></a-col>
      </a-row>

      <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="自动扣费异常列表">
        <a-space wrap style="margin-bottom: 12px;">
          <a-tag v-for="reason in (summary?.failureReasons || [])" :key="reason.reason" color="red">{{ reason.reason }} {{ reason.count }}</a-tag>
        </a-space>
        <vxe-table border stripe show-overflow :loading="loading" :data="rows" height="520">
          <vxe-column field="billMonth" title="账单月" width="120" />
          <vxe-column field="elderName" title="长者" min-width="140" />
          <vxe-column field="outstandingAmount" title="应扣金额" width="120" />
          <vxe-column field="balance" title="账户余额" width="120" />
          <vxe-column field="reasonLabel" title="失败原因" width="140">
            <template #default="{ row }">
              <a-tag color="volcano">{{ row.reasonLabel }}</a-tag>
            </template>
          </vxe-column>
          <vxe-column field="suggestion" title="处理建议" min-width="200" />
          <vxe-column title="操作" width="200" fixed="right">
            <template #default="{ row }">
              <a-space>
                <a-button type="link" @click="go(`/finance/accounts/list?elderId=${row.elderId || ''}`)">查看账户</a-button>
                <a-button type="link" @click="go('/finance/payments/register?from=auto_debit_exception')">转人工收款</a-button>
              </a-space>
            </template>
          </vxe-column>
        </vxe-table>
      </a-card>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { exportFinanceAutoDebitExceptionsCsv, getFinanceAutoDebitExceptions, getFinanceWorkbenchOverview } from '../../api/finance'
import type { FinanceAutoDebitCard, FinanceAutoDebitExceptionItem } from '../../types'
import { printTableReport } from '../../utils/print'

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const query = ref({
  date: dayjs(),
  printRemark: ''
})
const summary = ref<FinanceAutoDebitCard | null>(null)
const rows = ref<FinanceAutoDebitExceptionItem[]>([])

function go(path: string) {
  router.push(path)
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [overview, exceptions] = await Promise.all([
      getFinanceWorkbenchOverview(),
      getFinanceAutoDebitExceptions({
        date: dayjs(query.value.date).format('YYYY-MM-DD')
      })
    ])
    summary.value = overview?.autoDebit || null
    rows.value = exceptions || []
  } catch (error: any) {
    errorMessage.value = error?.message || '加载自动扣费数据失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

async function exportCsvReport() {
  try {
    await exportFinanceAutoDebitExceptionsCsv({
      date: dayjs(query.value.date).format('YYYY-MM-DD')
    })
    message.success('导出成功')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  }
}

function printCurrent() {
  try {
    printTableReport({
      title: '自动扣费异常列表',
      subtitle: `日期：${dayjs(query.value.date).format('YYYY-MM-DD')}；备注：${query.value.printRemark || '-'}`,
      columns: [
        { key: 'billMonth', title: '账单月' },
        { key: 'elderName', title: '长者' },
        { key: 'outstandingAmount', title: '应扣金额' },
        { key: 'balance', title: '账户余额' },
        { key: 'reasonLabel', title: '失败原因' },
        { key: 'suggestion', title: '处理建议' }
      ],
      rows: rows.value.map(item => ({
        billMonth: item.billMonth || '-',
        elderName: item.elderName || '-',
        outstandingAmount: item.outstandingAmount || 0,
        balance: item.balance || 0,
        reasonLabel: item.reasonLabel || '-',
        suggestion: item.suggestion || '-'
      }))
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

onMounted(loadData)
</script>
