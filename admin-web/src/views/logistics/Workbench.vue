<template>
  <PageContainer title="后勤工作台" subTitle="资产、物资、餐饮、维修的一体化运营看板">
    <template #extra>
      <a-space wrap>
        <a-tag :color="dutyMode ? 'gold' : 'default'">{{ dutyMode ? '值班高密度模式' : '标准模式' }}</a-tag>
        <span class="refresh-meta">最近刷新：{{ lastLoadedAt || '-' }}</span>
        <a-button type="primary" ghost @click="loadSummary">刷新数据</a-button>
        <a-switch v-model:checked="autoRefresh" checked-children="自动刷新" un-checked-children="手动刷新" />
        <a-switch v-model:checked="dutyMode" checked-children="值班" un-checked-children="标准" @change="handleModeChange" />
      </a-space>
    </template>

    <a-card class="card-elevated filter-panel" :bordered="false" style="margin-bottom: 12px">
      <div class="filter-top-row">
        <a-space wrap>
          <a-tag color="blue">统计窗口 {{ configuredQuery.windowDays }} 天</a-tag>
          <a-tag color="orange">临期预警 {{ configuredQuery.expiryDays }} 天</a-tag>
          <a-tag color="volcano">维修超时 {{ configuredQuery.overdueDays }} 天</a-tag>
          <a-tag color="cyan">维保临近 {{ configuredQuery.maintenanceDueDays }} 天</a-tag>
        </a-space>
        <a-space wrap>
          <a-button size="small" @click="applyPreset('stable')">稳健预设</a-button>
          <a-button size="small" @click="applyPreset('balanced')">平衡预设</a-button>
          <a-button size="small" @click="applyPreset('sensitive')">敏感预设</a-button>
          <a-button size="small" type="primary" @click="applyFilters">应用参数</a-button>
          <a-button size="small" @click="resetFilters">恢复默认</a-button>
        </a-space>
      </div>
      <a-form layout="inline" class="filter-form" @submit.prevent>
        <a-form-item label="统计窗口">
          <a-select v-model:value="draftQuery.windowDays" :options="windowOptions" style="width: 128px" />
        </a-form-item>
        <a-form-item label="临期天数">
          <a-select v-model:value="draftQuery.expiryDays" :options="expiryOptions" style="width: 128px" />
        </a-form-item>
        <a-form-item label="超时天数">
          <a-select v-model:value="draftQuery.overdueDays" :options="overdueOptions" style="width: 128px" />
        </a-form-item>
        <a-form-item label="维保临近天数">
          <a-select v-model:value="draftQuery.maintenanceDueDays" :options="maintenanceDueOptions" style="width: 140px" />
        </a-form-item>
      </a-form>
    </a-card>

    <a-alert
      v-if="signedLinkageContext.active"
      style="margin-bottom: 12px"
      type="success"
      show-icon
      :message="signedLinkageContext.message"
      :description="signedLinkageContext.description"
    >
      <template #action>
        <a-space wrap>
          <a-button size="small" @click="go('/elder/resident-360?residentId=' + signedLinkageContext.residentId + '&from=contractSigned')">长者总览</a-button>
          <a-button size="small" @click="go('/logistics/assets/room-state-map?from=contractSigned')">房态图</a-button>
          <a-button size="small" @click="go('/logistics/task-center?source=contract_signed&entryScene=signed_onboarding&residentId=' + signedLinkageContext.residentId + '&residentName=' + encodeURIComponent(signedLinkageContext.residentName || '') + '&tab=delivery')">后勤任务中心</a-button>
        </a-space>
      </template>
    </a-alert>

    <StatefulBlock :loading="loading" :error="errorMessage" :empty="!summary" empty-text="暂无后勤数据" @retry="loadSummary">
      <a-card class="card-elevated workbench-hero" :bordered="false" style="margin-bottom: 12px">
        <a-row :gutter="[12, 12]" align="middle">
          <a-col :xs="24" :xl="9">
            <div class="hero-title">后勤风险指数</div>
            <div class="hero-score-row">
              <span class="hero-score">{{ riskIndex }}</span>
              <a-tag :color="riskTagColor">{{ riskLevelLabel }}</a-tag>
            </div>
            <a-progress
              :percent="riskIndex"
              :stroke-color="riskProgressGradient"
              :status="riskIndex >= 75 ? 'exception' : riskIndex >= 55 ? 'active' : 'normal'"
            />
            <div class="hero-subline">
              触发信号 {{ Number(summary?.riskTriggeredCount || riskSignals.length) }} 项 · 更新时间 {{ generatedAtText }}
            </div>
          </a-col>
          <a-col :xs="24" :xl="15">
            <a-row :gutter="[12, 12]">
              <a-col :xs="12" :md="6" v-for="tile in heroTiles" :key="tile.key">
                <div class="hero-metric-tile">
                  <div class="tile-label">{{ tile.label }}</div>
                  <div class="tile-value">{{ tile.value }}</div>
                  <div class="tile-hint">{{ tile.hint }}</div>
                </div>
              </a-col>
            </a-row>
          </a-col>
        </a-row>
      </a-card>

      <a-card class="card-elevated" :bordered="false" style="margin-bottom: 12px" title="风险触发详情">
        <a-list size="small" :data-source="riskSignals" :pagination="false" :locale="{ emptyText: '当前参数下无触发信号' }">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-space>
                <a-tag :color="riskTagColor">{{ riskLevelLabel }}</a-tag>
                <span>{{ item }}</span>
              </a-space>
            </a-list-item>
          </template>
        </a-list>
      </a-card>

      <a-row :gutter="dutyMode ? 8 : 12">
        <a-col
          :xs="24"
          :md="dutyMode ? 8 : 12"
          :xl="dutyMode ? 6 : 8"
          v-for="card in cards"
          :key="card.key"
          :style="{ marginBottom: dutyMode ? '8px' : '12px' }"
        >
          <a-card class="card-elevated card-panel" :class="{ 'card-duty': dutyMode }" :bordered="false" :title="card.title">
            <template #extra>
              <a-tag :color="card.color">{{ card.tag }}</a-tag>
            </template>
            <div class="line-item" v-for="line in visibleLines(card.lines)" :key="line">{{ line }}</div>
            <a-space wrap :style="{ marginTop: dutyMode ? '8px' : '12px' }">
              <a-button
                v-for="action in visibleActions(card.actions)"
                :key="action.label"
                :type="action.primary ? 'primary' : 'default'"
                :disabled="action.disabled"
                :size="dutyMode ? 'small' : 'middle'"
                @click="go(action.path)"
              >
                {{ action.label }}
              </a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import {
  generateEquipmentMaintenanceTodos,
  getLogisticsWorkbenchSummary,
  rerunLatestFailedMaintenanceTodoJobLog
} from '../../api/logistics'
import type { LogisticsWorkbenchSummary, LogisticsWorkbenchSummaryQuery } from '../../types'
import {
  LOGISTICS_WORKBENCH_QUERY_DEFAULTS,
  buildLogisticsWorkbenchRouteQuery,
  normalizeLogisticsWorkbenchQuery,
  parseLogisticsWorkbenchQueryPatch
} from '../../utils/logisticsWorkbenchQuery'

interface WorkbenchAction {
  label: string
  path: string
  primary?: boolean
  disabled?: boolean
}

interface WorkbenchCard {
  key: string
  title: string
  tag: string
  color: string
  lines: string[]
  actions: WorkbenchAction[]
}

type PresetKey = 'stable' | 'balanced' | 'sensitive'

const QUERY_PRESETS: Record<PresetKey, Required<LogisticsWorkbenchSummaryQuery>> = {
  stable: { windowDays: 60, expiryDays: 45, overdueDays: 4, maintenanceDueDays: 45 },
  balanced: { windowDays: 30, expiryDays: 30, overdueDays: 2, maintenanceDueDays: 30 },
  sensitive: { windowDays: 14, expiryDays: 15, overdueDays: 1, maintenanceDueDays: 20 }
}

const windowOptions = [
  { label: '近7天', value: 7 },
  { label: '近14天', value: 14 },
  { label: '近30天', value: 30 },
  { label: '近60天', value: 60 },
  { label: '近90天', value: 90 }
]
const expiryOptions = [
  { label: '7天', value: 7 },
  { label: '15天', value: 15 },
  { label: '30天', value: 30 },
  { label: '45天', value: 45 },
  { label: '60天', value: 60 }
]
const overdueOptions = [
  { label: '1天', value: 1 },
  { label: '2天', value: 2 },
  { label: '3天', value: 3 },
  { label: '5天', value: 5 },
  { label: '7天', value: 7 }
]
const maintenanceDueOptions = [
  { label: '15天', value: 15 },
  { label: '20天', value: 20 },
  { label: '30天', value: 30 },
  { label: '45天', value: 45 },
  { label: '60天', value: 60 }
]

const RISK_LEVEL_LABEL_MAP: Record<string, string> = {
  LOW: '低风险',
  MEDIUM: '中风险',
  HIGH: '高风险',
  CRITICAL: '紧急风险'
}

const RISK_LEVEL_COLOR_MAP: Record<string, string> = {
  LOW: 'green',
  MEDIUM: 'blue',
  HIGH: 'orange',
  CRITICAL: 'red'
}

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const errorMessage = ref('')
const summary = ref<LogisticsWorkbenchSummary>()
const dutyMode = ref(false)
const autoRefresh = ref(true)
const lastLoadedAt = ref('')
const activeQuery = ref<LogisticsWorkbenchSummaryQuery>({})
const draftQuery = ref<Required<LogisticsWorkbenchSummaryQuery>>({ ...LOGISTICS_WORKBENCH_QUERY_DEFAULTS })
const loadedQuerySignature = ref('')
const hasLoadedSummary = ref(false)
const signedLinkageContext = computed(() => {
  const source = routeQueryText(route.query.source).toLowerCase()
  const entryScene = routeQueryText(route.query.entryScene).toLowerCase()
  const residentId = routeQueryText(route.query.residentId || route.query.elderId)
  const residentName = routeQueryText(route.query.residentName || route.query.elderName)
  const active = !!residentId && (source === 'contract_signed' || entryScene === 'signed_onboarding')
  return {
    active,
    residentId,
    residentName,
    message: active ? `新签约长者 ${residentName || '该长者'} 已进入后勤协同范围` : '',
    description: active ? '可继续核对床位、送餐、维修和值班任务。' : ''
  }
})
let refreshTimer: number | undefined

const configuredQuery = computed(() =>
  normalizeLogisticsWorkbenchQuery({
    windowDays: summary.value?.configuredWindowDays ?? activeQuery.value.windowDays,
    expiryDays: summary.value?.configuredExpiryDays ?? activeQuery.value.expiryDays,
    overdueDays: summary.value?.configuredOverdueDays ?? activeQuery.value.overdueDays,
    maintenanceDueDays: summary.value?.configuredMaintenanceDueDays ?? activeQuery.value.maintenanceDueDays
  })
)

const riskLevel = computed(() => String(summary.value?.riskLevel || 'LOW'))
const riskLevelLabel = computed(() => RISK_LEVEL_LABEL_MAP[riskLevel.value] || '低风险')
const riskTagColor = computed(() => RISK_LEVEL_COLOR_MAP[riskLevel.value] || 'default')
const riskIndex = computed(() => Math.max(0, Math.min(100, Number(summary.value?.riskIndex || 0))))
const generatedAtText = computed(() => (summary.value?.generatedAt ? dayjs(summary.value.generatedAt).format('MM-DD HH:mm:ss') : '-'))
const riskProgressGradient = computed(() => {
  if (riskIndex.value >= 75) {
    return { '0%': '#ff4d4f', '100%': '#cf1322' }
  }
  if (riskIndex.value >= 55) {
    return { '0%': '#fa8c16', '100%': '#d46b08' }
  }
  if (riskIndex.value >= 30) {
    return { '0%': '#1677ff', '100%': '#0958d9' }
  }
  return { '0%': '#52c41a', '100%': '#389e0d' }
})

const heroTiles = computed(() => {
  const data = summary.value
  if (!data) {
    return [
      { key: 'inventory', label: '库存预警', value: '0', hint: '低库存 + 临期' },
      { key: 'maintenance', label: '超时维修', value: '0', hint: '待处理超时单' },
      { key: 'delivery', label: '未送达', value: '0', hint: '餐饮配送风险' },
      { key: 'equipment', label: '维保临近', value: '0', hint: '设备维保窗口' }
    ]
  }
  return [
    {
      key: 'inventory',
      label: '库存预警',
      value: `${Number(data.lowStockCount || 0) + Number(data.expiringCount || 0)}`,
      hint: `低库存 ${data.lowStockCount || 0} / 临期 ${data.expiringCount || 0}`
    },
    {
      key: 'maintenance',
      label: '超时维修',
      value: `${data.maintenanceOverdueCount || 0}`,
      hint: `超时率 ${Number(data.maintenanceOverdueRate || 0).toFixed(2)}%`
    },
    {
      key: 'delivery',
      label: '未送达',
      value: `${data.undeliveredCount || 0}`,
      hint: `未送达率 ${Number(data.deliveryUndeliveredRate || 0).toFixed(2)}%`
    },
    {
      key: 'equipment',
      label: '维保临近',
      value: `${data.equipmentDueSoonCount || 0}`,
      hint: `临近率 ${Number(data.equipmentDueSoonRate || 0).toFixed(2)}%`
    }
  ]
})

const riskSignals = computed(() => {
  const serverSignals = summary.value?.riskSignals || []
  if (serverSignals.length) {
    return serverSignals
  }
  const data = summary.value
  if (!data) {
    return []
  }
  const fallbackSignals: string[] = []
  if (data.lowStockCount || data.expiringCount) {
    fallbackSignals.push(`库存风险 ${Number(data.lowStockCount || 0) + Number(data.expiringCount || 0)} 项`)
  }
  if (data.maintenanceOverdueCount) {
    fallbackSignals.push(`维修超时 ${data.maintenanceOverdueCount} 项`)
  }
  if (data.undeliveredCount) {
    fallbackSignals.push(`送餐未送达 ${data.undeliveredCount} 项`)
  }
  return fallbackSignals
})

const cards = computed<WorkbenchCard[]>(() => {
  const data = summary.value
  if (!data) return []
  const weekTop = data.weekTopConsumption?.slice(0, 5).map((item) => `${item.name}(${item.quantity})`).join('、') || '-'
  const monthTop = data.monthTopConsumption?.slice(0, 10).map((item) => `${item.name}(${item.quantity})`).join('、') || '-'
  const expiryDays = configuredQuery.value.expiryDays
  const windowDays = configuredQuery.value.windowDays
  const maintenanceDueDays = configuredQuery.value.maintenanceDueDays
  return [
    {
      key: 'inventory-alert',
      title: '库存预警中心',
      tag: '核心',
      color: 'red',
      lines: [
        `低库存物品：${data.lowStockCount}，临期物品：${data.expiringCount}`,
        `今日出库数量：${data.todayOutboundQty}，库存总金额：${amount(data.inventoryTotalAmount)}`,
        `近7天消耗Top5：${weekTop}`
      ],
      actions: [
        { label: '查看低库存', path: '/logistics/storage/alerts?lowStockOnly=true', primary: true },
        { label: `查看${expiryDays}天临期`, path: `/logistics/storage/alerts?expiryDays=${expiryDays}` },
        { label: '发起采购', path: '/logistics/storage/purchase?from=low_stock' },
        { label: '库存报表', path: '/logistics/reports/stock-amount' }
      ]
    },
    {
      key: 'purchase-progress',
      title: '采购执行进度',
      tag: '采购',
      color: 'blue',
      lines: [
        `待审批：${data.purchasePendingApprovalCount}，待到货：${data.purchaseApprovedNotArrivedCount}`,
        `已入库未对账：${data.purchaseReceivedNotSettledCount}`,
        `近${windowDays}天采购金额：${amount(data.monthPurchaseAmount)}`
      ],
      actions: [
        { label: '待审批采购单', path: '/logistics/storage/purchase?status=DRAFT', primary: true },
        { label: '待到货采购单', path: '/logistics/storage/purchase?status=APPROVED' },
        { label: '已入库未对账', path: '/logistics/storage/purchase?status=COMPLETED' }
      ]
    },
    {
      key: 'bed-asset',
      title: '床态与资产运行',
      tag: '资产',
      color: 'geekblue',
      lines: [
        `在住床位：${data.occupiedBeds}，空闲床位：${data.freeBeds}`,
        `清洁中床位：${data.cleaningBeds}，维修中床位：${data.maintenanceBeds}`,
        `床位占用率：${Number(data.bedOccupancyRate || 0).toFixed(2)}%`
      ],
      actions: [
        { label: '房态图', path: '/logistics/assets/room-state-map?filter=occupied', primary: true },
        { label: '清洁中床位', path: '/life/room-cleaning?status=PENDING' },
        { label: '维修中床位', path: '/logistics/assets/maintenance-record?status=PROCESSING' }
      ]
    },
    {
      key: 'maintenance',
      title: '维修与报障中心',
      tag: '维修',
      color: 'orange',
      lines: [
        `待维修：${data.maintenancePendingCount}，处理中：${data.maintenanceProcessingCount}`,
        `超时维修：${data.maintenanceOverdueCount}（超时率 ${Number(data.maintenanceOverdueRate || 0).toFixed(2)}%）`,
        `近${windowDays}天维修成本：${amount(data.monthMaintenanceCost)}`
      ],
      actions: [
        { label: '待维修', path: '/logistics/assets/maintenance-record?status=OPEN', primary: true },
        { label: '超时维修', path: `/logistics/assets/maintenance-record?filter=overdue&overdueDays=${configuredQuery.value.overdueDays}` },
        { label: '维修成本', path: '/logistics/maintenance/cost' },
        { label: '设备档案', path: '/logistics/maintenance/assets' },
        { label: `生成${maintenanceDueDays}天维保待办`, path: 'action:generate-maintenance-todos' },
        { label: '失败重跑', path: 'action:rerun-latest-failed-maintenance-todos', disabled: data.maintenanceTodoFailedCount7d <= 0 },
        { label: '执行日志', path: `/logistics/reports/maintenance-todo-log?windowDays=${windowDays}` }
      ]
    },
    {
      key: 'dining-overview',
      title: '餐饮执行总览',
      tag: '餐饮',
      color: 'green',
      lines: [
        `今日订餐人数：${data.todayMealOrderCount}，个性化餐：${data.personalizedMealCount}`,
        `送餐完成率：${percent(data.deliveryCompletionRate)}，未送达：${data.undeliveredCount}`,
        `今日餐饮成本估算：${amount(data.todayDiningCost)}`
      ],
      actions: [
        { label: '订餐详情', path: '/logistics/dining/stats?date=today', primary: true },
        { label: '未送达', path: '/logistics/dining/delivery-plan?filter=undelivered' },
        { label: '餐饮成本', path: '/logistics/reports/dining-cost' }
      ]
    },
    {
      key: 'dining-risk',
      title: '个性化点餐与禁忌预警',
      tag: '膳食风险',
      color: 'volcano',
      lines: [
        `糖尿病餐：${data.diabetesMealCount}，低盐餐：${data.lowSaltMealCount}`,
        `过敏禁忌提醒：${data.allergyAlertCount}`,
        `特殊营养餐：${data.specialNutritionMealCount}`
      ],
      actions: [
        { label: '食谱管理', path: '/logistics/dining/recipe', primary: true },
        { label: '禁忌设置', path: '/logistics/commerce/risk' }
      ]
    },
    {
      key: 'consume-trend',
      title: '物资消耗趋势',
      tag: '消耗',
      color: 'purple',
      lines: [
        `近${windowDays}天消耗金额：${amount(data.monthConsumeAmount)}`,
        `Top10消耗物资：${monthTop}`,
        `护理消耗 vs 餐饮消耗：${amount(data.nursingConsumeAmount)} / ${amount(data.diningConsumeAmount)}`
      ],
      actions: [{ label: '物资消耗报表', path: '/logistics/reports/consume', primary: true }]
    },
    {
      key: 'supply-chain',
      title: '仓储四链路聚合',
      tag: '聚合',
      color: 'magenta',
      lines: [
        `库存(资/耗/食/服)：${data.inventoryAssetQty}/${data.inventoryConsumableQty}/${data.inventoryFoodQty}/${data.inventoryServiceQty}`,
        `低库存预警(资/耗/食/服)：${data.lowStockAssetCount}/${data.lowStockConsumableCount}/${data.lowStockFoodCount}/${data.lowStockServiceCount}`,
        `近${windowDays}天采购金额(资/耗/食/服)：${amount(data.monthPurchaseAssetAmount)} / ${amount(data.monthPurchaseConsumableAmount)} / ${amount(data.monthPurchaseFoodAmount)} / ${amount(data.monthPurchaseServiceAmount)}`,
        `今日出库(资/耗/食/服)：${data.todayOutboundAssetQty}/${data.todayOutboundConsumableQty}/${data.todayOutboundFoodQty}/${data.todayOutboundServiceQty}`
      ],
      actions: [
        { label: '库存总览', path: '/logistics/storage/stock-query?businessDomain=INTERNAL&itemType=CONSUMABLE', primary: true },
        { label: '库存预警', path: '/logistics/storage/alerts?focus=low&businessDomain=INTERNAL&itemType=CONSUMABLE' },
        { label: '采购单', path: '/logistics/storage/purchase?status=DRAFT&source=inventory_alert_batch' },
        { label: '出库记录', path: '/logistics/storage/outbound?outType=CONSUME&businessDomain=INTERNAL&itemType=CONSUMABLE' }
      ]
    },
    {
      key: 'task-load',
      title: '后勤任务负载',
      tag: '任务',
      color: 'cyan',
      lines: [
        `今日清洁任务：${data.todayCleaningTaskCount}，今日维修任务：${data.todayMaintenanceTaskCount}`,
        `今日送餐任务：${data.todayDeliveryTaskCount}，今日库存盘点任务：${data.todayInventoryCheckTaskCount}`,
        `设备总数：${data.equipmentTotalCount}，${maintenanceDueDays}天内待维保：${data.equipmentDueSoonCount}`,
        `最近维保任务：${lastMaintenanceTodoSummary(data)}`
      ],
      actions: [
        { label: '任务中心', path: buildTaskCenterPath('cleaning'), primary: true },
        { label: '维修任务', path: buildTaskCenterPath('maintenance') },
        { label: '送餐任务', path: buildTaskCenterPath('delivery') },
        { label: '盘点任务', path: buildTaskCenterPath('inventory') },
        { label: '维保设备', path: '/logistics/maintenance/assets?status=MAINTENANCE' },
        { label: '执行日志', path: `/logistics/reports/maintenance-todo-log?windowDays=${windowDays}` }
      ]
    }
  ]
})

function amount(value?: number) {
  const num = Number(value || 0)
  return `${num.toFixed(2)} 元`
}

function percent(value?: number) {
  return `${Number(value || 0).toFixed(2)}%`
}

function routeQueryText(value: unknown) {
  if (Array.isArray(value)) {
    return routeQueryText(value[0])
  }
  if (value == null) {
    return ''
  }
  return String(value).trim()
}

function resolveDutyMode(raw: unknown) {
  return String(Array.isArray(raw) ? raw[0] : raw || '').toLowerCase() === 'duty'
}

function querySignature(query: LogisticsWorkbenchSummaryQuery) {
  if (Object.keys(query || {}).length === 0) {
    return ''
  }
  const normalized = normalizeLogisticsWorkbenchQuery(query)
  return [normalized.windowDays, normalized.expiryDays, normalized.overdueDays, normalized.maintenanceDueDays].join('-')
}

function visibleLines(lines: string[]) {
  if (!dutyMode.value) return lines
  return lines.slice(0, 2)
}

function visibleActions(actions: WorkbenchAction[]) {
  if (!dutyMode.value) return actions
  return actions.slice(0, 3)
}

function buildTaskCenterPath(tab: string) {
  const query = buildLogisticsWorkbenchRouteQuery(configuredQuery.value, {
    tab,
    source: signedLinkageContext.value.active ? 'contract_signed' : undefined,
    entryScene: signedLinkageContext.value.active ? 'signed_onboarding' : undefined,
    residentId: signedLinkageContext.value.active ? signedLinkageContext.value.residentId : undefined,
    residentName: signedLinkageContext.value.active ? signedLinkageContext.value.residentName : undefined
  })
  return `/logistics/task-center?${new URLSearchParams(query).toString()}`
}

function go(path: string) {
  if (path === 'action:generate-maintenance-todos') {
    triggerMaintenanceTodos()
    return
  }
  if (path === 'action:rerun-latest-failed-maintenance-todos') {
    rerunLatestFailed()
    return
  }
  router.push(path)
}

function handleModeChange(checked: boolean) {
  const query: Record<string, string> = {}
  if (Object.keys(activeQuery.value).length > 0) {
    Object.assign(query, buildLogisticsWorkbenchRouteQuery(configuredQuery.value))
  }
  if (checked) {
    query.mode = 'duty'
  }
  router.replace({ query })
}

function applyPreset(presetKey: PresetKey) {
  draftQuery.value = { ...QUERY_PRESETS[presetKey] }
  message.success('已应用参数预设，点击“应用参数”即可生效')
}

function applyFilters() {
  const normalized = normalizeLogisticsWorkbenchQuery(draftQuery.value)
  activeQuery.value = { ...normalized }
  const query = buildLogisticsWorkbenchRouteQuery(normalized, { mode: dutyMode.value ? 'duty' : undefined })
  router.replace({ query })
}

function resetFilters() {
  activeQuery.value = {}
  draftQuery.value = { ...LOGISTICS_WORKBENCH_QUERY_DEFAULTS }
  const query: Record<string, string> = dutyMode.value ? { mode: 'duty' } : {}
  router.replace({ query })
  message.success('已恢复默认统计参数')
}

function refreshPreferenceStorageKey() {
  return 'logistics-workbench-auto-refresh'
}

function loadRefreshPreference() {
  try {
    const raw = window.localStorage.getItem(refreshPreferenceStorageKey())
    if (raw === '0') {
      autoRefresh.value = false
      return
    }
  } catch {}
  autoRefresh.value = true
}

function saveRefreshPreference() {
  try {
    window.localStorage.setItem(refreshPreferenceStorageKey(), autoRefresh.value ? '1' : '0')
  } catch {}
}

function startAutoRefreshTimer() {
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
  }
  refreshTimer = window.setInterval(() => {
    if (!autoRefresh.value || loading.value || document.visibilityState !== 'visible') {
      return
    }
    loadSummary({ silent: true })
  }, 30000)
}

function stopAutoRefreshTimer() {
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
    refreshTimer = undefined
  }
}

async function triggerMaintenanceTodos() {
  const days = configuredQuery.value.maintenanceDueDays
  try {
    const res: any = await generateEquipmentMaintenanceTodos(days)
    const created = Number(res?.createdCount || 0)
    const skipped = Number(res?.skippedCount || 0)
    message.success(`已生成${created}条维保待办，跳过${skipped}条重复提醒`)
    loadSummary()
  } catch (error: any) {
    message.error(error?.message || '生成维保待办失败')
  }
}

function lastMaintenanceTodoSummary(data: LogisticsWorkbenchSummary) {
  if (!data.maintenanceTodoLastExecutedAt) {
    return '暂无执行记录'
  }
  const status = data.maintenanceTodoLastStatus === 'SUCCESS' ? '成功' : '失败'
  const triggerTypeLabelMap: Record<string, string> = {
    MANUAL: '手动',
    SCHEDULED: '自动',
    RETRY: '重跑'
  }
  const trigger = triggerTypeLabelMap[data.maintenanceTodoLastTriggerType || ''] || '未知'
  const counts = `新建${Number(data.maintenanceTodoLastCreatedCount || 0)} / 跳过${Number(data.maintenanceTodoLastSkippedCount || 0)}`
  const failedTip = data.maintenanceTodoFailedCount7d > 0 ? `，近7天失败${data.maintenanceTodoFailedCount7d}次` : ''
  const error = data.maintenanceTodoLastStatus === 'FAILED' && data.maintenanceTodoLastErrorMessage
    ? `，错误：${data.maintenanceTodoLastErrorMessage}`
    : ''
  return `${status}（${trigger}）${counts}${failedTip}${error}`
}

async function rerunLatestFailed() {
  try {
    const res: any = await rerunLatestFailedMaintenanceTodoJobLog()
    if (Number(res?.blockedByWindow || 0)) {
      message.warning('仅支持重跑30天内的失败记录')
      return
    }
    if (Number(res?.blockedByCooldown || 0)) {
      message.warning('重跑过于频繁，请2分钟后再试')
      return
    }
    if (!Number(res?.rerunTriggered || 0)) {
      message.info('暂无失败执行记录，无需重跑')
      return
    }
    message.success(`失败重跑完成：新建${Number(res?.createdCount || 0)}，跳过${Number(res?.skippedCount || 0)}`)
    loadSummary()
  } catch (error: any) {
    message.error(error?.message || '失败重跑触发失败')
  }
}

async function loadSummary(options?: { silent?: boolean }) {
  loading.value = true
  errorMessage.value = ''
  try {
    const params = Object.keys(activeQuery.value).length > 0
      ? normalizeLogisticsWorkbenchQuery(activeQuery.value)
      : undefined
    summary.value = await getLogisticsWorkbenchSummary(params)
    const serverQuery = normalizeLogisticsWorkbenchQuery({
      windowDays: summary.value?.configuredWindowDays,
      expiryDays: summary.value?.configuredExpiryDays,
      overdueDays: summary.value?.configuredOverdueDays,
      maintenanceDueDays: summary.value?.configuredMaintenanceDueDays
    })
    draftQuery.value = { ...serverQuery }
    loadedQuerySignature.value = querySignature(activeQuery.value)
    hasLoadedSummary.value = true
    lastLoadedAt.value = dayjs().format('MM-DD HH:mm:ss')
  } catch (error: any) {
    errorMessage.value = error?.message || '加载后勤工作台失败'
    if (!options?.silent) {
      message.error(errorMessage.value)
    }
  } finally {
    loading.value = false
  }
}

watch(
  () => route.query,
  async (query) => {
    const nextQuery = parseLogisticsWorkbenchQueryPatch(query)
    const nextSignature = querySignature(nextQuery)
    dutyMode.value = resolveDutyMode(query.mode)
    activeQuery.value = nextQuery
    draftQuery.value = normalizeLogisticsWorkbenchQuery(activeQuery.value)
    if (hasLoadedSummary.value && nextSignature === loadedQuerySignature.value) {
      return
    }
    await loadSummary()
  },
  { immediate: true, deep: true }
)

watch(autoRefresh, () => {
  saveRefreshPreference()
})

onMounted(() => {
  loadRefreshPreference()
  startAutoRefreshTimer()
})

onUnmounted(() => {
  stopAutoRefreshTimer()
})
</script>

<style scoped>
.filter-panel {
  border: 1px solid #e6f4ff;
  background: linear-gradient(120deg, #f8fbff 0%, #f2f8ff 42%, #f9fcff 100%);
}

.refresh-meta {
  color: #64748b;
  font-size: 12px;
}

.filter-top-row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.filter-form {
  row-gap: 8px;
}

.workbench-hero {
  overflow: hidden;
  border: 1px solid #e6f4ff;
  background:
    radial-gradient(160% 120% at 0% 0%, rgba(22, 119, 255, 0.08) 0%, rgba(22, 119, 255, 0) 60%),
    linear-gradient(135deg, #f8fbff 0%, #f0f7ff 40%, #f7fcff 100%);
}

.hero-title {
  font-size: 15px;
  color: #1f2937;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.hero-score-row {
  margin-top: 8px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.hero-score {
  font-size: 42px;
  line-height: 1;
  font-weight: 700;
  color: #111827;
}

.hero-subline {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
}

.hero-metric-tile {
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  background: rgba(255, 255, 255, 0.78);
  padding: 10px;
  min-height: 94px;
  animation: tile-rise 0.45s ease both;
}

.tile-label {
  color: #64748b;
  font-size: 12px;
}

.tile-value {
  margin-top: 6px;
  font-size: 24px;
  line-height: 1.1;
  color: #0f172a;
  font-weight: 700;
}

.tile-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #475569;
}

.line-item {
  margin-top: 6px;
  color: #334155;
  font-size: 13px;
  line-height: 1.65;
}

.card-panel {
  border: 1px solid #f1f5f9;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.card-panel:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.08);
}

.card-duty :deep(.ant-card-head) {
  min-height: 44px;
}

.card-duty :deep(.ant-card-head-title) {
  font-size: 14px;
}

.card-duty :deep(.ant-card-body) {
  padding: 12px;
}

.card-duty .line-item {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.45;
}

@keyframes tile-rise {
  from {
    transform: translateY(6px);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@media (max-width: 900px) {
  .hero-score {
    font-size: 34px;
  }

  .hero-metric-tile {
    min-height: 84px;
  }
}
</style>
