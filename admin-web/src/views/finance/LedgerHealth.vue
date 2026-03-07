<template>
  <PageContainer title="财务一致性巡检" subTitle="账单/收款/消费流水三方一致性检查">
    <a-card class="card-elevated" :bordered="false">
      <a-space wrap>
        <a-input-number v-model:value="query.limit" :min="10" :max="200" :step="10" style="width: 140px" />
        <a-input v-model:value="query.printRemark" allow-clear placeholder="打印备注" style="width: 200px" />
        <a-button type="primary" :loading="loading" @click="loadData">刷新巡检</a-button>
        <a-button :loading="exporting" @click="exportCsvReport">导出CSV</a-button>
        <a-button @click="printCurrent">打印当前</a-button>
        <a-button @click="go('/finance/reconcile/center')">返回对账中心</a-button>
      </a-space>
    </a-card>

    <StatefulBlock style="margin-top: 16px;" :loading="loading" :error="errorMessage" @retry="loadData">
      <a-row :gutter="[12, 12]" style="margin-bottom: 12px;">
        <a-col :xs="12" :lg="4"><a-card size="small"><a-statistic title="账单" :value="data?.billCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card size="small"><a-statistic title="收款" :value="data?.paymentCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card size="small"><a-statistic title="流水" :value="data?.consumptionCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card size="small"><a-statistic title="总问题" :value="data?.totalIssueCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card size="small"><a-statistic title="明细不一致" :value="data?.mismatchBillItemCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card size="small"><a-statistic title="已付不一致" :value="data?.mismatchPaidCount || 0" /></a-card></a-col>
      </a-row>

      <a-alert
        :type="(data?.totalIssueCount || 0) > 0 ? 'warning' : 'success'"
        show-icon
        :message="(data?.totalIssueCount || 0) > 0 ? `发现 ${data?.totalIssueCount || 0} 个一致性问题` : '巡检通过，无异常'"
        :description="`巡检时间：${data?.checkedAt || '-'}；缺少收款流水：${data?.missingPaymentFlowCount || 0}`"
        style="margin-bottom: 12px;"
      />

      <a-card class="card-elevated" :bordered="false">
        <vxe-table border stripe show-overflow :loading="loading" :data="data?.issues || []" height="560">
          <vxe-column field="issueTypeLabel" title="异常类型" width="180">
            <template #default="{ row }"><a-tag color="red">{{ row.issueTypeLabel }}</a-tag></template>
          </vxe-column>
          <vxe-column field="billId" title="账单ID" width="120" />
          <vxe-column field="paymentId" title="收款ID" width="120" />
          <vxe-column field="elderName" title="老人" width="140">
            <template #default="{ row }">{{ row.elderName || '-' }}</template>
          </vxe-column>
          <vxe-column field="expectedAmount" title="期望金额" width="120" />
          <vxe-column field="actualAmount" title="实际金额" width="120" />
          <vxe-column field="detail" title="异常描述" min-width="260" />
          <vxe-column title="操作" width="180" fixed="right">
            <template #default="{ row }">
              <a-space>
                <a-button v-if="row.billId" type="link" @click="go(`/finance/bill/${row.billId}`)">查看账单</a-button>
                <a-button type="link" @click="go('/finance/reconcile/center?filter=unmatched')">去处理</a-button>
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
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { exportFinanceLedgerHealthCsv, getFinanceLedgerHealth } from '../../api/finance'
import type { FinanceLedgerHealth } from '../../types'
import { printTableReport } from '../../utils/print'

const router = useRouter()
const loading = ref(false)
const exporting = ref(false)
const errorMessage = ref('')
const data = ref<FinanceLedgerHealth | null>(null)
const query = ref({
  limit: 80,
  printRemark: ''
})

function go(path: string) {
  router.push(path)
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    data.value = await getFinanceLedgerHealth({ limit: query.value.limit })
  } catch (error: any) {
    errorMessage.value = error?.message || '加载巡检数据失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

async function exportCsvReport() {
  exporting.value = true
  try {
    await exportFinanceLedgerHealthCsv({ limit: query.value.limit })
    message.success('导出成功')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  } finally {
    exporting.value = false
  }
}

function printCurrent() {
  try {
    printTableReport({
      title: '财务一致性巡检',
      subtitle: `巡检数量上限：${query.value.limit}；备注：${query.value.printRemark || '-'}`,
      columns: [
        { key: 'issueTypeLabel', title: '异常类型' },
        { key: 'billId', title: '账单ID' },
        { key: 'paymentId', title: '收款ID' },
        { key: 'elderName', title: '老人' },
        { key: 'expectedAmount', title: '期望金额' },
        { key: 'actualAmount', title: '实际金额' },
        { key: 'detail', title: '异常描述' }
      ],
      rows: (data.value?.issues || []).map(item => ({
        issueTypeLabel: item.issueTypeLabel || '-',
        billId: item.billId || '-',
        paymentId: item.paymentId || '-',
        elderName: item.elderName || '-',
        expectedAmount: item.expectedAmount || 0,
        actualAmount: item.actualAmount || 0,
        detail: item.detail || '-'
      }))
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

onMounted(loadData)
</script>
