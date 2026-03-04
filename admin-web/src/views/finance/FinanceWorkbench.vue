<template>
  <PageContainer title="财务工作台" subTitle="财务收费与经营分析中心 · 今日经营驾驶舱">
    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 16px;">
      <a-space wrap>
        <a-tag color="blue">业务日期 {{ overview?.bizDate || '-' }}</a-tag>
        <a-tag color="purple">工作台默认入口</a-tag>
        <a-button type="primary" @click="loadData">刷新数据</a-button>
      </a-space>
    </a-card>

    <StatefulBlock :loading="loading" :error="errorMessage" @retry="loadData">
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :xl="12">
          <a-card title="卡片A：今日收款总览（收银视角）" class="card-elevated" :bordered="false">
            <a-row :gutter="[12, 12]">
              <a-col :span="8">
                <a-statistic title="今日收款总额" :value="overview?.cashier?.todayCollectedTotal || 0" :precision="2" suffix="元" />
              </a-col>
              <a-col :span="8">
                <a-statistic title="今日开票金额" :value="overview?.cashier?.todayInvoiceAmount || 0" :precision="2" suffix="元" />
              </a-col>
              <a-col :span="8">
                <a-statistic title="今日退款金额" :value="overview?.cashier?.todayRefundAmount || 0" :precision="2" suffix="元" />
              </a-col>
            </a-row>
            <a-divider />
            <a-space wrap>
              <a-button type="primary" @click="go('/finance/payments/register?date=today')">收款登记</a-button>
              <a-button @click="go('/finance/payments/shift-close?date=today')">日结/交班</a-button>
              <a-button
                v-for="method in (overview?.cashier?.paymentMethods || [])"
                :key="method.method"
                @click="go(`/finance/payments/records?date=today&method=${method.method}`)"
              >
                {{ method.methodLabel }} {{ money(method.amount) }}
              </a-button>
            </a-space>
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="12">
          <a-card title="卡片B：欠费与余额预警（核心风控）" class="card-elevated" :bordered="false">
            <a-row :gutter="[12, 12]">
              <a-col :span="8">
                <a-statistic title="欠费长者数" :value="overview?.risk?.overdueElderCount || 0" />
              </a-col>
              <a-col :span="8">
                <a-statistic title="欠费金额" :value="overview?.risk?.overdueAmount || 0" :precision="2" suffix="元" />
              </a-col>
              <a-col :span="8">
                <a-statistic title="低余额人数" :value="overview?.risk?.lowBalanceCount || 0" />
              </a-col>
            </a-row>
            <a-row :gutter="[12, 12]" style="margin-top: 8px;">
              <a-col :span="12">
                <a-statistic title="即将到期合同未续费" :value="overview?.risk?.expiringContractCount || 0" />
              </a-col>
            </a-row>
            <a-divider />
            <a-space wrap>
              <a-button type="link" @click="go('/finance/bills/in-resident?filter=overdue')">欠费列表</a-button>
              <a-button type="link" @click="go('/finance/accounts/list?filter=low_balance')">余额预警</a-button>
              <a-button type="link" @click="go('/finance/bills/auto-deduct?filter=active')">自动催缴</a-button>
            </a-space>
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="8">
          <a-card title="卡片C：待审核事项" class="card-elevated" :bordered="false">
            <a-list size="small" :data-source="pendingList">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-space>
                    <span>{{ item.label }}</span>
                    <a-tag color="orange">{{ item.value }}</a-tag>
                  </a-space>
                </a-list-item>
              </template>
            </a-list>
            <a-space wrap>
              <a-button type="link" @click="go('/oa/approval?module=finance&status=pending')">审批中心</a-button>
              <a-button type="link" @click="go('/finance/discharge/review?status=pending')">退住审核与结算</a-button>
            </a-space>
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="8">
          <a-card title="卡片D：本月营收结构" class="card-elevated" :bordered="false">
            <a-statistic title="本月营收总额" :value="overview?.revenueStructure?.monthRevenueTotal || 0" :precision="2" suffix="元" />
            <div ref="revenueChartRef" style="height: 220px; margin-top: 8px;"></div>
            <a-space wrap>
              <a-tag
                v-for="category in (overview?.revenueStructure?.categories || [])"
                :key="category.code"
                color="blue"
                style="cursor: pointer"
                @click="openRevenueCategory(category.code)"
              >
                {{ category.name }} {{ category.ratio }}%
              </a-tag>
            </a-space>
            <a-space wrap>
              <a-button type="link" @click="go('/finance/reports/revenue-structure?period=this_month')">营收占比</a-button>
              <a-button type="link" @click="go('/finance/allocation/public-cost?category=utility&period=this_month')">分摊与公共费用</a-button>
            </a-space>
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="8">
          <a-card title="卡片E：楼层/房间收支快照" class="card-elevated" :bordered="false">
            <a-statistic title="空床损失估算" :value="overview?.roomOps?.emptyBedLossEstimate || 0" :precision="2" suffix="元" />
            <a-divider />
            <a-descriptions :column="1" size="small" bordered>
              <a-descriptions-item label="楼层收入Top">{{ topFloorText }}</a-descriptions-item>
              <a-descriptions-item label="楼层收入Bottom">{{ bottomFloorText }}</a-descriptions-item>
            </a-descriptions>
            <a-space wrap style="margin-top: 8px;">
              <a-button type="link" @click="go('/finance/reports/floor-room-profit?period=this_month')">楼层房间收支</a-button>
              <a-button type="link" @click="openTopRoom">房间经营详情</a-button>
            </a-space>
            <a-divider />
            <vxe-table
              size="mini"
              border
              stripe
              height="220"
              :data="overview?.roomOps?.roomTop10 || []"
              :row-config="{ isHover: true }"
              @cell-click="onRoomCellClick"
            >
              <vxe-column field="roomNo" title="房间" min-width="90" />
              <vxe-column field="income" title="收入" width="110">
                <template #default="{ row }">{{ money(row.income) }}</template>
              </vxe-column>
              <vxe-column field="netAmount" title="净额" width="110">
                <template #default="{ row }">{{ money(row.netAmount) }}</template>
              </vxe-column>
              <vxe-column field="emptyBeds" title="空床" width="70" />
            </vxe-table>
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="12">
          <a-card title="卡片F：自动扣费运行状态" class="card-elevated" :bordered="false">
            <a-row :gutter="[12, 12]">
              <a-col :span="6"><a-statistic title="应扣费" :value="overview?.autoDebit?.shouldDeductCount || 0" /></a-col>
              <a-col :span="6"><a-statistic title="成功" :value="overview?.autoDebit?.successCount || 0" /></a-col>
              <a-col :span="6"><a-statistic title="失败" :value="overview?.autoDebit?.failedCount || 0" /></a-col>
              <a-col :span="6"><a-statistic title="待处理" :value="overview?.autoDebit?.pendingHandleCount || 0" /></a-col>
            </a-row>
            <a-divider />
            <a-space wrap>
              <a-tag v-for="reason in (overview?.autoDebit?.failureReasons || [])" :key="reason.reason" color="red">
                {{ reason.reason }} {{ reason.count }}
              </a-tag>
            </a-space>
            <a-space wrap style="margin-top: 8px;">
              <a-button type="link" @click="go('/finance/bills/auto-deduct?date=today')">自动扣费</a-button>
              <a-button type="link" @click="go('/finance/bills/auto-deduct-errors?date=today')">自动扣费异常</a-button>
            </a-space>
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="12">
          <a-card title="卡片G：医护费用流水" class="card-elevated" :bordered="false">
            <a-row :gutter="[12, 12]">
              <a-col :span="8"><a-statistic title="今日流水笔数" :value="overview?.medicalFlow?.todayFlowCount || 0" /></a-col>
              <a-col :span="8"><a-statistic title="今日流水金额" :value="overview?.medicalFlow?.todayFlowAmount || 0" :precision="2" suffix="元" /></a-col>
              <a-col :span="8"><a-statistic title="待审核" :value="overview?.medicalFlow?.pendingReviewCount || 0" /></a-col>
            </a-row>
            <a-space wrap style="margin-top: 8px;">
              <a-tag color="orange">重复计费 {{ overview?.medicalFlow?.duplicateBillingCount || 0 }}</a-tag>
              <a-tag color="volcano">缺少医嘱关联 {{ overview?.medicalFlow?.missingOrderLinkCount || 0 }}</a-tag>
            </a-space>
            <a-space wrap style="margin-top: 8px;">
              <a-button type="link" @click="go('/finance/flows/medical?date=today')">医护费用流水</a-button>
              <a-button type="link" @click="go('/finance/flows/medical-errors?date=today')">医护费用异常</a-button>
            </a-space>
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="12">
          <a-card title="卡片H：分摊费用运行" class="card-elevated" :bordered="false">
            <a-row :gutter="[12, 12]">
              <a-col :span="8"><a-statistic title="本月已生成分摊" :value="overview?.allocation?.monthGeneratedCount || 0" /></a-col>
              <a-col :span="8"><a-statistic title="未生成房间数" :value="overview?.allocation?.ungeneratedRoomCount || 0" /></a-col>
              <a-col :span="8"><a-statistic title="分摊异常" :value="overview?.allocation?.errorCount || 0" /></a-col>
            </a-row>
            <a-space wrap style="margin-top: 8px;">
              <a-button type="link" @click="go('/finance/allocation/public-cost?period=this_month')">分摊与公共费用</a-button>
              <a-button type="link" @click="go('/finance/allocation/tasks?status=error')">分摊任务</a-button>
            </a-space>
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="12">
          <a-card title="卡片I：对账异常" class="card-elevated" :bordered="false">
            <a-row :gutter="[12, 12]">
              <a-col :span="8"><a-statistic title="账单已收未核销" :value="overview?.reconcile?.billPaidUnmatchedCount || 0" /></a-col>
              <a-col :span="8"><a-statistic title="重复/冲正未完成" :value="overview?.reconcile?.duplicatedOrReversalPendingCount || 0" /></a-col>
              <a-col :span="8"><a-statistic title="发票未关联账单" :value="overview?.reconcile?.invoiceUnlinkedCount || 0" /></a-col>
            </a-row>
            <a-space wrap style="margin-top: 8px;">
              <a-button type="link" @click="go('/finance/reconcile-center?filter=unmatched')">对账中心</a-button>
              <a-button type="link" @click="go('/finance/reconcile-invoice?filter=unlinked')">发票对账</a-button>
            </a-space>
          </a-card>
        </a-col>

        <a-col :span="24">
          <a-card title="卡片J：快捷入口（from=finance_dashboard）" class="card-elevated" :bordered="false">
            <a-space wrap style="margin-bottom: 12px;">
              <a-tag color="blue">费用科目 {{ masterOverview?.feeSubjectCount || 0 }}</a-tag>
              <a-tag color="cyan">计费规则 {{ masterOverview?.billingRuleCount || 0 }}</a-tag>
              <a-tag color="purple">支付渠道 {{ masterOverview?.paymentChannelCount || 0 }}</a-tag>
              <a-tag color="orange">待审批配置 {{ masterOverview?.pendingApprovalCount || 0 }}</a-tag>
              <a-button type="link" @click="go('/finance/config/master-data')">进入配置中心</a-button>
              <a-button type="link" @click="go('/finance/config/change-log')">配置变更记录</a-button>
            </a-space>
            <div class="config-change-tip">
              最近配置变更：{{ latestConfigChangeText }}
            </div>
            <a-space wrap>
              <a-button
                v-for="item in (overview?.quickEntries || [])"
                :key="item.key"
                type="primary"
                ghost
                @click="go(item.path)"
              >
                {{ item.label }}
              </a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { getFinanceConfigChangeLogPage, getFinanceMasterDataOverview, getFinanceWorkbenchOverview } from '../../api/finance'
import type { FinanceConfigChangeLogItem, FinanceMasterDataOverview, FinanceRoomRanking, FinanceWorkbenchOverview } from '../../types'
import { useECharts } from '../../plugins/echarts'

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const overview = ref<FinanceWorkbenchOverview | null>(null)
const masterOverview = ref<FinanceMasterDataOverview | null>(null)
const latestConfigChange = ref<FinanceConfigChangeLogItem | null>(null)

const { chartRef: revenueChartRef, setOption: setRevenueOption } = useECharts()

const pendingList = computed(() => [
  { label: '待审批减免单', value: overview.value?.pending?.pendingDiscountCount || 0 },
  { label: '待审批退款', value: overview.value?.pending?.pendingRefundCount || 0 },
  { label: '待审核退住结算', value: overview.value?.pending?.pendingDischargeSettlementCount || 0 }
])

const topFloorText = computed(() => (overview.value?.roomOps?.floorTop || [])
  .map(item => `${item.floorNo}:${money(item.income)}`)
  .join('，') || '-')

const bottomFloorText = computed(() => (overview.value?.roomOps?.floorBottom || [])
  .map(item => `${item.floorNo}:${money(item.income)}`)
  .join('，') || '-')

const latestConfigChangeText = computed(() => {
  if (!latestConfigChange.value) return '暂无记录'
  const who = latestConfigChange.value.actorName || '系统'
  const when = latestConfigChange.value.createTime || '-'
  const detail = latestConfigChange.value.detail || latestConfigChange.value.actionType || ''
  return `${when} ${who} - ${detail}`
})

function money(value?: number) {
  return `¥${Number(value || 0).toFixed(2)}`
}

function go(path: string) {
  router.push(path)
}

function openTopRoom() {
  const room = overview.value?.roomOps?.roomTop10?.[0]
  if (!room) {
    message.info('暂无房间经营数据')
    return
  }
  const query = `building=${encodeURIComponent(room.building || '')}&room=${encodeURIComponent(room.roomNo || '')}&period=this_month`
  go(`/finance/reports/room-ops-detail?${query}`)
}

function onRoomCellClick({ row }: { row: FinanceRoomRanking }) {
  const query = `building=${encodeURIComponent(row.building || '')}&room=${encodeURIComponent(row.roomNo || '')}&period=this_month`
  go(`/finance/reports/room-ops-detail?${query}`)
}

function openRevenueCategory(categoryCode: string) {
  if (categoryCode === 'UTILITY') {
    go('/finance/allocation/public-cost?category=utility&period=this_month')
    return
  }
  go(`/finance/reports/revenue-structure?period=this_month&category=${encodeURIComponent(categoryCode)}`)
}

function renderRevenueChart() {
  const categories = overview.value?.revenueStructure?.categories || []
  setRevenueOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['45%', '72%'],
        avoidLabelOverlap: false,
        data: categories.map(item => ({ name: item.name, value: item.amount }))
      }
    ]
  })
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [workbenchData, configData] = await Promise.all([
      getFinanceWorkbenchOverview(),
      getFinanceMasterDataOverview()
    ])
    overview.value = workbenchData
    masterOverview.value = configData
    const changeLogPage = await getFinanceConfigChangeLogPage({ pageNo: 1, pageSize: 1 })
    latestConfigChange.value = changeLogPage?.list?.[0] || null
    renderRevenueChart()
  } catch (error: any) {
    errorMessage.value = error?.message || '加载财务工作台失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.config-change-tip {
  color: #64748b;
  margin-bottom: 10px;
}
</style>
