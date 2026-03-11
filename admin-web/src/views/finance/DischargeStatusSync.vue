<template>
  <PageContainer title="结算完成状态回写" subTitle="退住结算后回写老人档案状态与床位状态">
    <a-card class="card-elevated" :bordered="false">
      <a-space wrap>
        <a-input-number v-model:value="query.limit" :min="20" :max="500" :step="20" style="width: 140px" />
        <a-input v-model:value="query.printRemark" allow-clear placeholder="打印备注" style="width: 180px" />
        <a-button type="primary" :loading="loading" @click="loadData">刷新巡检</a-button>
        <a-button :loading="executing" @click="executeBatch">执行全部回写</a-button>
        <a-button @click="exportCurrent">导出CSV</a-button>
        <a-button @click="printCurrent">打印当前</a-button>
        <a-button @click="go('/finance/discharge/settlement')">查看退住结算</a-button>
        <a-button @click="go('/elder/bed-panorama')">查看床态全景</a-button>
      </a-space>
    </a-card>

    <StatefulBlock style="margin-top: 16px;" :loading="loading" :error="errorMessage" @retry="loadData">
      <a-row :gutter="[12, 12]" style="margin-bottom: 12px;">
        <a-col :xs="12" :lg="8"><a-card size="small"><a-statistic title="已结算单" :value="data?.settledCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="8"><a-card size="small"><a-statistic title="待回写异常" :value="data?.issueCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="8"><a-card size="small"><a-statistic title="巡检时间" :value="data?.checkedAt || '-'" /></a-card></a-col>
      </a-row>

      <a-alert
        :type="(data?.issueCount || 0) > 0 ? 'warning' : 'success'"
        show-icon
        :message="(data?.issueCount || 0) > 0 ? `发现 ${data?.issueCount || 0} 条待回写异常` : '状态回写已全部一致'"
        :description="(data?.issueCount || 0) > 0 ? '建议优先处理床位未释放与老人状态未回写异常。' : '无需额外人工处理。'"
        style="margin-bottom: 12px;"
      />

      <a-card class="card-elevated" :bordered="false">
        <vxe-table border stripe show-overflow :loading="loading" :data="data?.rows || []" height="560">
          <vxe-column field="settlementId" title="结算单ID" width="130" />
          <vxe-column field="elderName" title="老人" min-width="140">
            <template #default="{ row }">{{ row.elderName || '-' }}</template>
          </vxe-column>
          <vxe-column field="settledTime" title="结算时间" width="180" />
          <vxe-column field="issueType" title="异常类型" width="220">
            <template #default="{ row }"><a-tag color="volcano">{{ issueTypeText(row.issueType) }}</a-tag></template>
          </vxe-column>
          <vxe-column field="issueMessage" title="异常描述" min-width="220" />
          <vxe-column field="elderStatus" title="老人状态" width="100" />
          <vxe-column field="elderBedId" title="档案床位ID" width="120" />
          <vxe-column field="occupiedBedId" title="占用床位ID" width="120" />
          <vxe-column title="操作" width="220" fixed="right">
            <template #default="{ row }">
              <a-space>
                <a-button type="link" @click="go(`/finance/discharge/settlement`)">看结算</a-button>
                <a-button type="link" @click="executeOne(row)">执行回写</a-button>
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
import { executeFinanceDischargeStatusSync, getFinanceDischargeStatusSync } from '../../api/finance'
import type { FinanceDischargeStatusSync, FinanceDischargeStatusSyncRow } from '../../types'
import { confirmAction } from '../../utils/actionConfirm'
import { exportCsv } from '../../utils/export'
import { printTableReport } from '../../utils/print'

const router = useRouter()
const loading = ref(false)
const executing = ref(false)
const errorMessage = ref('')
const data = ref<FinanceDischargeStatusSync | null>(null)
const query = ref({
  limit: 200,
  printRemark: ''
})

function go(path: string) {
  router.push(path)
}

function issueTypeText(type?: string) {
  if (type === 'ELDER_STATUS_NOT_DISCHARGED') return '老人状态未退住'
  if (type === 'BED_NOT_RELEASED') return '床位未释放'
  if (type === 'ELDER_MISSING') return '老人档案缺失'
  return type || '-'
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    data.value = await getFinanceDischargeStatusSync({ limit: query.value.limit })
  } catch (error: any) {
    errorMessage.value = error?.message || '加载状态回写巡检失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

async function executeOne(row: FinanceDischargeStatusSyncRow) {
  const confirmed = await confirmAction({
    title: '确认执行该条状态回写？',
    content: `结算单#${row.settlementId || '-'} / 老人：${row.elderName || '姓名待完善'}`,
    impactItems: ['老人状态将回写为已退住', '老人档案床位将清空', '床位状态将释放为空床'],
    okText: '确认回写',
    danger: true
  })
  if (!confirmed) return
  executing.value = true
  try {
    const res = await executeFinanceDischargeStatusSync({
      settlementId: row.settlementId,
      elderId: row.elderId
    })
    message.success(`已处理：成功 ${res.successCount}，失败 ${res.failCount}`)
    await loadData()
  } finally {
    executing.value = false
  }
}

async function executeBatch() {
  if ((data.value?.issueCount || 0) <= 0) {
    message.info('当前无待回写异常')
    return
  }
  const confirmed = await confirmAction({
    title: '确认执行全部状态回写？',
    content: `当前异常 ${data.value?.issueCount || 0} 条`,
    impactItems: ['批量回写老人退住状态', '批量释放占用床位', '写入后将影响床态全景与后续收费逻辑'],
    okText: '确认全部回写',
    danger: true
  })
  if (!confirmed) return
  executing.value = true
  try {
    const res = await executeFinanceDischargeStatusSync({
      processAll: true
    })
    message.success(`批量完成：成功 ${res.successCount}，失败 ${res.failCount}`)
    await loadData()
  } finally {
    executing.value = false
  }
}

function exportCurrent() {
  exportCsv(
    (data.value?.rows || []).map(item => ({
      结算单ID: item.settlementId || '',
      老人姓名: item.elderName || '',
      结算时间: item.settledTime || '',
      异常类型: issueTypeText(item.issueType),
      异常描述: item.issueMessage || '',
      老人状态: item.elderStatus ?? '',
      档案床位ID: item.elderBedId ?? '',
      占用床位ID: item.occupiedBedId ?? ''
    })),
    `结算状态回写-${new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-')}.csv`
  )
}

function printCurrent() {
  try {
    printTableReport({
      title: '结算完成状态回写',
      subtitle: `巡检上限：${query.value.limit}；备注：${query.value.printRemark || '-'}`,
      columns: [
        { key: 'settlementId', title: '结算单ID' },
        { key: 'elderName', title: '老人姓名' },
        { key: 'settledTime', title: '结算时间' },
        { key: 'issueTypeText', title: '异常类型' },
        { key: 'issueMessage', title: '异常描述' },
        { key: 'elderStatus', title: '老人状态' },
        { key: 'elderBedId', title: '档案床位ID' },
        { key: 'occupiedBedId', title: '占用床位ID' }
      ],
      rows: (data.value?.rows || []).map(item => ({
        settlementId: item.settlementId || '-',
        elderName: item.elderName || '-',
        settledTime: item.settledTime || '-',
        issueTypeText: issueTypeText(item.issueType),
        issueMessage: item.issueMessage || '-',
        elderStatus: item.elderStatus ?? '-',
        elderBedId: item.elderBedId ?? '-',
        occupiedBedId: item.occupiedBedId ?? '-'
      }))
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

onMounted(loadData)
</script>
