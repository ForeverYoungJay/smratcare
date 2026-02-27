<template>
  <PageContainer title="退院结算" subTitle="自动对账、费用清单、押金处理与床位释放">
    <a-row :gutter="16">
      <a-col :xs="24" :lg="14">
        <a-card title="退院结算清单" class="card-elevated" :bordered="false">
          <StatefulBlock :loading="loading" :error="errorMessage" @retry="loadSettlement">
            <a-descriptions :column="1" size="small" bordered>
              <a-descriptions-item label="账单汇总">{{ feeSummary }}</a-descriptions-item>
              <a-descriptions-item label="押金处理">{{ depositSummary }}</a-descriptions-item>
              <a-descriptions-item label="自动对账">{{ reconcileSummary }}</a-descriptions-item>
              <a-descriptions-item label="床位释放">{{ bedReleaseSummary }}</a-descriptions-item>
            </a-descriptions>
            <a-space style="margin-top: 12px">
              <a-button type="primary" @click="go(`/finance/bill?period=this_month&residentId=${residentId}`)">查看账单</a-button>
              <a-button @click="go(`/finance/account-log?residentId=${residentId}`)">费用明细</a-button>
              <a-button danger @click="go(`/elder/discharge?residentId=${residentId}`)">确认退院登记</a-button>
            </a-space>
          </StatefulBlock>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="10">
        <a-card title="流程联动" class="card-elevated" :bordered="false">
          <StatefulBlock :loading="loading" :error="errorMessage" @retry="loadSettlement">
            <a-timeline>
              <a-timeline-item color="green">退住申请审批完成</a-timeline-item>
              <a-timeline-item :color="settlement ? 'green' : 'gray'">自动生成退院结算单</a-timeline-item>
              <a-timeline-item :color="settlement?.financeRefunded === 1 ? 'green' : 'blue'">押金退还/欠费补缴</a-timeline-item>
              <a-timeline-item :color="settlement?.financeRefunded === 1 ? 'green' : 'blue'">回写长者状态为已退住</a-timeline-item>
              <a-timeline-item :color="isBedReleased ? 'green' : 'gray'">释放床位并停止餐饮/护理任务</a-timeline-item>
            </a-timeline>
          </StatefulBlock>
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../../components/PageContainer.vue'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import { getDischargeSettlementPage } from '../../../api/financeFee'
import type { DischargeSettlementItem, PageResult } from '../../../types'

const router = useRouter()
const route = useRoute()
const residentId = computed(() => Number(route.query.residentId || 0))
const settlement = ref<DischargeSettlementItem | null>(null)
const loading = ref(false)
const errorMessage = ref('')

const feeSummary = computed(() => {
  if (!settlement.value) {
    return '暂无结算数据'
  }
  return `应收 ${toAmount(settlement.value.payableAmount)}，押金抵扣 ${toAmount(settlement.value.fromDepositAmount)}，待补缴 ${toAmount(settlement.value.supplementAmount)}`
})

const depositSummary = computed(() => {
  if (!settlement.value) {
    return '暂无结算数据'
  }
  const linkage = extractLinkage(settlement.value.remark, '押金处理:')
  return linkage || `应退款 ${toAmount(settlement.value.refundAmount)}`
})

const reconcileSummary = computed(() => {
  if (!settlement.value) {
    return '暂无结算数据'
  }
  const linkage = extractLinkage(settlement.value.remark, '自动对账:')
  if (linkage) {
    return linkage
  }
  return settlement.value.financeRefunded === 1 ? '已触发自动对账' : '待结算触发'
})

const bedReleaseSummary = computed(() => {
  if (!settlement.value) {
    return '暂无结算数据'
  }
  const linkage = extractLinkage(settlement.value.remark, '床位释放:')
  if (linkage) {
    return linkage
  }
  return settlement.value.financeRefunded === 1 ? '已回写床位/状态' : '结算完成后自动回写'
})

const isBedReleased = computed(() => extractLinkage(settlement.value?.remark, '床位释放:').includes('已释放'))

function go(path: string) {
  router.push(path)
}

function toAmount(value?: number) {
  return Number(value || 0).toFixed(2)
}

function extractLinkage(remark: string | undefined, prefix: string) {
  if (!remark) {
    return ''
  }
  const parts = remark.split('；')
  const matched = parts.find((item) => item.trim().startsWith(prefix))
  return matched ? matched.trim().slice(prefix.length).trim() : ''
}

async function loadSettlement() {
  loading.value = true
  errorMessage.value = ''
  if (!residentId.value) {
    settlement.value = null
    loading.value = false
    return
  }
  try {
    const res: PageResult<DischargeSettlementItem> = await getDischargeSettlementPage({
      pageNo: 1,
      pageSize: 1,
      elderId: residentId.value
    })
    settlement.value = res.list?.[0] || null
  } catch (error: any) {
    errorMessage.value = error?.message || '加载退院结算失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

watch(
  () => residentId.value,
  () => {
    loadSettlement()
  }
)

onMounted(() => {
  loadSettlement()
})
</script>
