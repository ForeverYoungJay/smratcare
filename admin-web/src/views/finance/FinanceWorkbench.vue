<template>
  <PageContainer title="财务工作台" subTitle="财务收费与经营分析中心 · 今日经营驾驶舱">
    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 16px;">
      <a-space wrap>
        <a-tag color="blue">业务日期 {{ overview?.bizDate || '-' }}</a-tag>
        <a-tag color="purple">{{ roleTone }}</a-tag>
        <a-button type="primary" @click="loadData">刷新数据</a-button>
      </a-space>
    </a-card>

    <a-card class="hero-board card-elevated" :bordered="false" style="margin-bottom: 16px;">
      <div class="hero-board__main">
        <div class="hero-board__eyebrow">Finance Operations Desk</div>
        <h2>{{ roleHeadline }}</h2>
        <p>{{ roleHint }}</p>
        <a-space class="hero-search" wrap>
          <a-input
            v-model:value="searchKeyword"
            allow-clear
            placeholder="搜索长者 / 账单号 / 外部流水号 / 合同号"
            style="width: 320px"
            @pressEnter="openFinanceSearch"
          />
          <a-button @click="openFinanceSearch">财务搜索</a-button>
        </a-space>
        <a-space wrap>
          <a-button type="primary" @click="go('/finance/payments/cashier-desk')">进入收银台</a-button>
          <a-button @click="go('/finance/reconcile/issue-center')">异常修复中心</a-button>
          <a-button @click="go('/finance/bills/follow-up')">欠费催缴跟进</a-button>
          <a-button v-if="isManagerOrAbove" @click="go('/finance/reconcile/month-close')">月结进度</a-button>
        </a-space>
      </div>
      <div class="hero-board__panel">
        <div class="hero-board__panel-title">当前角色关注项</div>
        <div class="hero-focus-list">
          <div v-for="item in roleFocusItems" :key="item.title" class="hero-focus-item">
            <div class="hero-focus-item__title">{{ item.title }}</div>
            <div class="hero-focus-item__desc">{{ item.desc }}</div>
          </div>
        </div>
      </div>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 16px;">
      <a-row :gutter="[12, 12]">
        <a-col :xs="24" :lg="16">
          <a-alert
            :type="(ledgerHealth?.totalIssueCount || 0) > 0 ? 'warning' : 'success'"
            show-icon
            :message="(ledgerHealth?.totalIssueCount || 0) > 0 ? `财务一致性巡检发现 ${ledgerHealth?.totalIssueCount || 0} 个问题` : '财务一致性巡检正常'"
            :description="ledgerHealthHint"
          />
        </a-col>
        <a-col :xs="24" :lg="8">
          <a-space wrap>
            <a-tag color="blue">账单 {{ ledgerHealth?.billCount || 0 }}</a-tag>
            <a-tag color="purple">收款 {{ ledgerHealth?.paymentCount || 0 }}</a-tag>
            <a-tag color="cyan">流水 {{ ledgerHealth?.consumptionCount || 0 }}</a-tag>
            <a-button @click="go('/finance/reconcile/ledger-health')">巡检明细</a-button>
            <a-button type="primary" ghost @click="go('/finance/reconcile/issue-center')">去处理异常</a-button>
          </a-space>
        </a-col>
        <a-col :span="24" v-if="(ledgerHealth?.issues || []).length">
          <a-list
            size="small"
            :data-source="(ledgerHealth?.issues || []).slice(0, 6)"
            bordered
            style="background: #fff"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-space wrap>
                  <a-tag color="red">{{ item.issueTypeLabel }}</a-tag>
                  <span>{{ item.detail }}</span>
                  <span v-if="item.billId">账单#{{ item.billId }}</span>
                  <span v-if="item.paymentId">收款#{{ item.paymentId }}</span>
                  <a-button type="link" size="small" @click="openLedgerIssue(item)">处理</a-button>
                </a-space>
              </a-list-item>
            </template>
          </a-list>
        </a-col>
      </a-row>
    </a-card>
    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 16px;">
      <a-row :gutter="[12, 12]">
        <a-col :xs="24" :lg="8">
          <a-statistic title="运营洞察总数" :value="opsInsight?.totalInsights || 0" />
        </a-col>
        <a-col :xs="24" :lg="8">
          <a-statistic title="高优先级" :value="opsInsight?.highPriorityCount || 0" />
        </a-col>
        <a-col :xs="24" :lg="8">
          <a-tag color="blue">生成时间 {{ opsInsight?.generatedAt || '-' }}</a-tag>
        </a-col>
        <a-col :span="24" v-if="(opsInsight?.items || []).length">
          <a-list size="small" :data-source="(opsInsight?.items || []).slice(0, 5)" bordered style="background: #fff">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-space wrap>
                  <a-tag :color="item.level === 'HIGH' ? 'red' : item.level === 'MEDIUM' ? 'orange' : 'blue'">
                    {{ item.level }}
                  </a-tag>
                  <span>{{ item.title }}</span>
                  <span>{{ item.detail }}</span>
                  <span style="color: #64748b;">{{ item.suggestion }}</span>
                  <a-button type="link" size="small" @click="go(item.actionPath)">{{ item.actionLabel }}</a-button>
                </a-space>
              </a-list-item>
            </template>
          </a-list>
        </a-col>
      </a-row>
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
              <a-button type="link" @click="go('/finance/bills/follow-up?riskLevel=HIGH')">高风险催缴</a-button>
              <a-button type="link" @click="go('/finance/accounts/list?filter=low_balance')">余额预警</a-button>
              <a-button type="link" @click="go('/finance/bills/follow-up?stage=FOLLOW_UP')">自动催缴</a-button>
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
              <a-button type="link" @click="go('/finance/reconcile/issue-center?sourceModule=RECONCILE')">异常修复中心</a-button>
              <a-button type="link" @click="go('/finance/reconcile/invoice?filter=unlinked')">发票对账</a-button>
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
                v-for="item in visibleQuickEntries"
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
import { getFinanceConfigChangeLogPage, getFinanceLedgerHealth, getFinanceMasterDataOverview, getFinanceOpsInsights, getFinanceWorkbenchOverview } from '../../api/finance'
import { useUserStore } from '../../stores/user'
import type {
  FinanceConfigChangeLogItem,
  FinanceLedgerHealth,
  FinanceLedgerHealthIssueItem,
  FinanceQuickEntry,
  FinanceMasterDataOverview,
  FinanceOpsInsight,
  FinanceRoomRanking,
  FinanceWorkbenchOverview
} from '../../types'
import { useECharts } from '../../plugins/echarts'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const errorMessage = ref('')
const searchKeyword = ref('')
const overview = ref<FinanceWorkbenchOverview | null>(null)
const masterOverview = ref<FinanceMasterDataOverview | null>(null)
const latestConfigChange = ref<FinanceConfigChangeLogItem | null>(null)
const ledgerHealth = ref<FinanceLedgerHealth | null>(null)
const opsInsight = ref<FinanceOpsInsight | null>(null)

const { chartRef: revenueChartRef, setOption: setRevenueOption } = useECharts()

const userRoles = computed(() => userStore.roles || [])
const isManagerOrAbove = computed(() => userRoles.value.some(role => ['FINANCE_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'].includes(role)))
const roleTone = computed(() => isManagerOrAbove.value ? '主管/管理视角' : '一线收银视角')
const roleHeadline = computed(() => isManagerOrAbove.value ? '先看风险、审批、催缴和月结推进。' : '先把收费、票据、退款和交班动作做顺。')
const roleHint = computed(() => isManagerOrAbove.value
  ? '工作台优先暴露异常修复、催缴跟进和月结进度，便于主管快速判断今天哪些链路会拖慢结账。'
  : '工作台优先暴露收银台、票据处理和退款修正，减少一线财务在收费链路里的跳转次数。')
const roleFocusItems = computed(() => isManagerOrAbove.value
  ? [
      { title: '异常修复', desc: '一致性巡检、对账和催缴异常统一进入一个修复池。' },
      { title: '欠费推进', desc: '按风险和续约到期状态排序，先盯住高风险欠费。' },
      { title: '月结收口', desc: '关账前直接看哪一步还在阻塞。' }
    ]
  : [
      { title: '收银台', desc: '同页看当日收款、票据待办和退款修正。' },
      { title: '票据处理', desc: '交班前先补齐收据和发票关联。' },
      { title: '异常回捞', desc: '重复收款、作废账单和修正记录当天闭环。' }
    ])
const visibleQuickEntries = computed(() => {
  const entries = overview.value?.quickEntries || []
  const preferredKeys = isManagerOrAbove.value
    ? ['issue_center', 'collection_follow_up', 'month_close', 'invoice_receipt', 'fee_adjustment', 'payment_register']
    : ['cashier_desk', 'payment_register', 'invoice_receipt', 'fee_adjustment', 'prepaid_recharge', 'issue_center']
  const score = new Map(preferredKeys.map((key, index) => [key, index]))
  return [...entries]
    .filter((item: FinanceQuickEntry) => isManagerOrAbove.value || item.key !== 'month_close')
    .sort((a: FinanceQuickEntry, b: FinanceQuickEntry) => (score.get(a.key) ?? 99) - (score.get(b.key) ?? 99))
})

const pendingList = computed(() => [
  { label: '待审批减免单', value: overview.value?.pending?.pendingDiscountCount || 0 },
  { label: '待审批退款', value: overview.value?.pending?.pendingRefundCount || 0 },
  { label: '待审核退住结算', value: overview.value?.pending?.pendingDischargeSettlementCount || 0 },
  { label: '异常工单待处理', value: overview.value?.pending?.issueTodoCount || 0 },
  { label: '催缴提醒到期', value: overview.value?.pending?.collectionReminderCount || 0 },
  { label: '当前已锁账月', value: overview.value?.pending?.lockedMonthCount || 0 }
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

const ledgerHealthHint = computed(() => {
  if (!ledgerHealth.value) return '巡检未完成'
  const mismatchItem = ledgerHealth.value.mismatchBillItemCount || 0
  const mismatchPaid = ledgerHealth.value.mismatchPaidCount || 0
  const missingFlow = ledgerHealth.value.missingPaymentFlowCount || 0
  const checkedAt = ledgerHealth.value.checkedAt || '-'
  return `明细不一致 ${mismatchItem}，已付不一致 ${mismatchPaid}，缺少收款流水 ${missingFlow}（巡检时间：${checkedAt}）`
})

function money(value?: number) {
  return `¥${Number(value || 0).toFixed(2)}`
}

function go(path: string) {
  router.push(path)
}

function openFinanceSearch() {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    message.warning('请输入搜索关键词')
    return
  }
  go(`/finance/search?keyword=${encodeURIComponent(keyword)}`)
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

function openLedgerIssue(item: FinanceLedgerHealthIssueItem) {
  if (item.billId) {
    go(`/finance/bill/${item.billId}`)
    return
  }
  go('/finance/reconcile/issue-center')
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
    const [workbenchData, configData, ledgerData, opsInsightData] = await Promise.all([
      getFinanceWorkbenchOverview(),
      getFinanceMasterDataOverview(),
      getFinanceLedgerHealth({ limit: 12 }),
      getFinanceOpsInsights()
    ])
    overview.value = workbenchData
    masterOverview.value = configData
    ledgerHealth.value = ledgerData
    opsInsight.value = opsInsightData
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
.hero-board {
  display: grid;
  gap: 18px;
  grid-template-columns: 1.15fr 0.85fr;
  background:
    radial-gradient(circle at top left, rgba(34, 197, 94, 0.22), transparent 30%),
    linear-gradient(135deg, #111827 0%, #1f2937 48%, #0f766e 100%);
  color: #fff;
}

.hero-board__eyebrow {
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.72);
}

.hero-board h2 {
  margin: 10px 0;
  font-size: 28px;
  line-height: 1.18;
}

.hero-board p {
  margin: 0 0 16px;
  max-width: 580px;
  color: rgba(255, 255, 255, 0.76);
}

.hero-search {
  margin-bottom: 14px;
}

.hero-board__panel {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
}

.hero-board__panel-title {
  font-size: 13px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.72);
}

.hero-focus-list {
  display: grid;
  gap: 12px;
  margin-top: 14px;
}

.hero-focus-item {
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.08);
}

.hero-focus-item__title {
  font-weight: 700;
  color: #fff;
}

.hero-focus-item__desc {
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 13px;
}

.config-change-tip {
  color: #64748b;
  margin-bottom: 10px;
}

@media (max-width: 1100px) {
  .hero-board {
    grid-template-columns: 1fr;
  }
}
</style>
