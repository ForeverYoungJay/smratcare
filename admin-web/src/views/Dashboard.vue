<template>
  <PageContainer title="运营看板" subTitle="实时掌握机构运行情况">
    <StatefulBlock :loading="loading" :error="errorMessage" @retry="loadSummary">
      <a-row :gutter="16">
        <a-col :xs="24" :sm="12" :lg="6" v-for="card in coreCards" :key="card.title">
          <PermissionGuardCard
            :can-access="card.canAccess"
            :required-roles="card.requiredRoles"
            card-class="card-elevated clickable-card"
            @click="go(card.route)"
          >
            <a-statistic :title="card.title" :value="card.value" />
            <div class="card-meta">
              <a-tag :color="card.color">{{ card.tag }}</a-tag>
              <span>{{ card.trend }}</span>
            </div>
          </PermissionGuardCard>
        </a-col>
      </a-row>

      <a-card class="card-elevated" :bordered="false" style="margin-top: 16px" :title="`统计分析统一口径（${activeWindowText}）`">
        <template #extra>
          <a-space size="small">
            <span class="hint-text">更新时间：{{ refreshedAt || '--' }}</span>
            <a-tag color="blue">口径版本 {{ summary.metricVersion || metricCatalog.metricVersion || '--' }}</a-tag>
            <a-tag color="purple">阈值配置 {{ thresholdConfig.configVersion || '--' }}</a-tag>
            <a-tag v-if="thresholdDirty" color="orange">未保存修改</a-tag>
            <a-button size="small" @click="metricDrawerOpen = true">口径详情</a-button>
            <a-button size="small" @click="thresholdDrawerOpen = true">阈值设置</a-button>
          </a-space>
        </template>
        <div class="hint-text" style="margin-bottom: 12px;">
          口径说明：{{ metricCatalog.defaultWindow || '最近6个月' }}；总消费=账单消费+商城消费；总收入=账单总额。
        </div>
        <div class="window-toolbar">
          <a-space size="small" wrap>
            <span class="hint-text">统计窗口</span>
            <a-button size="small" :type="activeWindowPreset === 3 ? 'primary' : 'default'" @click="applyWindowPreset(3)">
              近3个月
            </a-button>
            <a-button size="small" :type="activeWindowPreset === 6 ? 'primary' : 'default'" @click="applyWindowPreset(6)">
              近6个月
            </a-button>
            <a-button size="small" :type="activeWindowPreset === 12 ? 'primary' : 'default'" @click="applyWindowPreset(12)">
              近12个月
            </a-button>
            <a-button size="small" :type="activeWindowPreset === 0 ? 'primary' : 'default'" @click="clearWindowPreset">
              全部口径
            </a-button>
            <a-tag color="cyan">{{ activeWindowText }}</a-tag>
          </a-space>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" :lg="8" v-for="item in visibleUnifiedCards" :key="item.title">
            <PermissionGuardCard
              size="small"
              :can-access="item.canAccess"
              :required-roles="item.requiredRoles"
              card-class="clickable-card"
              @click="goUnifiedCard(item)"
            >
              <a-statistic :title="item.title" :value="item.value" :precision="item.precision" :suffix="item.suffix" />
              <div class="card-meta">
                <a-tag :color="item.color">{{ item.tag }}</a-tag>
                <span>{{ item.desc }}</span>
              </div>
            </PermissionGuardCard>
          </a-col>
        </a-row>
      </a-card>

      <StatsWorkspacePanel
        page-key="dashboard"
        title="经营指挥区"
        :summary-text="dashboardWorkspaceSummary"
        :current-payload="dashboardWorkspacePayload"
        :target-fields="dashboardTargetFields"
        :data-health="dashboardDataHealth"
        @apply-preset="onDashboardPresetApply"
        @targets-change="onDashboardTargetsChange"
      />

      <StatsInsightDeck :items="dashboardInsightItems" />

      <StatsCommandCenter
        page-key="dashboard-command"
        title="经营协同台"
        share-title="运营看板协同包"
        :summary-text="dashboardCommandSummary"
        :current-payload="dashboardWorkspacePayload"
        :metric-version="summary.metricVersion || metricCatalog.metricVersion || ''"
        :action-items="dashboardActionItems"
        :anomalies="dashboardAnomalies"
        :metric-notes="dashboardMetricNotes"
        :panel-options="dashboardPanelOptions"
        :selected-panel-keys="dashboardMetricKeys"
        @trigger-action="onDashboardCommandAction"
        @panel-change="onDashboardPanelChange"
        @apply-template="onDashboardTemplateApply"
      />

      <a-row :gutter="16" style="margin-top: 16px">
        <a-col :xs="24" :lg="16">
          <a-card class="card-elevated" title="关键指标趋势" :bordered="false">
            <v-chart class="chart" :option="trendOption" autoresize />
          </a-card>
        </a-col>
        <a-col :xs="24" :lg="8">
          <a-card class="card-elevated" title="今日异常" :bordered="false">
            <a-list :data-source="alerts" item-layout="horizontal">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta
                    :title="item.title"
                    :description="item.desc"
                  />
                  <a-tag :color="item.level === '高' ? 'red' : 'orange'">{{ item.level }}</a-tag>
                </a-list-item>
              </template>
            </a-list>
            <a-space v-if="alertActions.length" wrap style="margin-top: 8px;">
              <a-button size="small" v-for="item in alertActions" :key="item.title" @click="go(item.route)">
                处置：{{ item.title }}
              </a-button>
            </a-space>
            <div v-if="alerts.length === 0" class="empty-wrap">
              <a-empty description="暂无异常" />
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px">
        <a-col :xs="24" :lg="12">
          <a-card class="card-elevated" title="机构运营口径" :bordered="false">
            <a-table
              :columns="metricColumns"
              :data-source="metricRows"
              :pagination="false"
              size="small"
            />
          </a-card>
        </a-col>
        <a-col :xs="24" :lg="12">
          <a-card class="card-elevated" title="消费结构占比" :bordered="false">
            <v-chart class="chart" :option="billOption" autoresize />
          </a-card>
        </a-col>
      </a-row>

      <a-drawer
        v-model:open="metricDrawerOpen"
        title="指标口径中心"
        width="620"
        :destroy-on-close="false"
      >
        <a-alert
          type="info"
          show-icon
          :message="`口径版本：${metricCatalog.metricVersion || '--'}；生效日期：${metricCatalog.effectiveDate || '--'}；默认窗口：${metricCatalog.defaultWindow || '--'}`"
          style="margin-bottom: 12px;"
        />
        <a-list :data-source="metricCatalog.definitions || []" item-layout="vertical">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta :title="`${item.metricName}（${item.metricGroup}）`">
                <template #description>
                  <div class="metric-def-line">公式：{{ item.formula }}</div>
                  <div class="metric-def-line">数据源：{{ item.source }}；刷新：{{ item.refreshPolicy }}</div>
                  <div class="metric-def-line">
                    角色：{{ (item.requiredRoles || []).join(' / ') || '--' }}
                  </div>
                </template>
              </a-list-item-meta>
              <a-space>
                <a-button size="small" @click="go(item.suggestedRoute)">查看明细</a-button>
              </a-space>
            </a-list-item>
          </template>
        </a-list>
      </a-drawer>

      <a-drawer
        v-model:open="thresholdDrawerOpen"
        title="告警阈值配置"
        width="520"
        :destroy-on-close="false"
      >
        <a-alert type="info" show-icon :message="thresholdHintText" style="margin-bottom: 12px;" />
        <a-alert
          v-if="thresholdDirty"
          type="warning"
          show-icon
          message="当前阈值有未保存修改，关闭抽屉不会自动保存。"
          style="margin-bottom: 12px;"
        />
        <a-form layout="vertical">
          <a-form-item label="护理异常任务阈值（条）">
            <a-input-number v-model:value="thresholdConfig.abnormalTaskThreshold" :min="1" :max="999" style="width: 100%;" />
          </a-form-item>
          <a-form-item label="库存预警阈值（项）">
            <a-input-number v-model:value="thresholdConfig.inventoryAlertThreshold" :min="1" :max="9999" style="width: 100%;" />
          </a-form-item>
          <a-form-item label="床位高占用阈值（%）">
            <a-input-number v-model:value="thresholdConfig.bedOccupancyThreshold" :min="1" :max="100" style="width: 100%;" />
          </a-form-item>
          <a-form-item label="收入环比下滑阈值（%）">
            <a-input-number v-model:value="thresholdConfig.revenueDropThreshold" :min="1" :max="100" style="width: 100%;" />
          </a-form-item>
        </a-form>
        <div class="hint-text" style="margin-bottom: 6px;">当前阈值命中预览</div>
        <ThresholdPreviewList :rows="thresholdPreviewRows" />
        <a-divider style="margin: 10px 0;" />
        <div class="threshold-preset-row">
          <span class="hint-text">快捷预设</span>
          <a-space wrap>
            <a-button
              v-for="preset in thresholdPresets"
              :key="preset.label"
              size="small"
              @click="applyThresholdPreset(preset.snapshot)"
            >
              {{ preset.label }}
            </a-button>
          </a-space>
        </div>
        <a-space>
          <a-button type="primary" :disabled="!thresholdDirty" @click="saveThresholdConfig">保存配置</a-button>
          <a-button :disabled="!thresholdDirty" @click="rollbackThresholdDraft">撤销修改</a-button>
          <a-button @click="resetThresholdConfig">恢复默认</a-button>
        </a-space>
        <a-divider />
        <div class="hint-text" style="margin-bottom: 8px;">最近阈值变更</div>
        <a-list size="small" :data-source="thresholdLogs.slice(0, 6)" :locale="{ emptyText: '暂无变更记录' }">
          <template #renderItem="{ item }">
            <a-list-item>
              <span class="hint-text">
                {{ dayjs(item.changedAt).format('YYYY-MM-DD HH:mm:ss') }}
                · {{ item.source === 'dashboard' ? '仪表盘' : '首页' }}
                · 异常≥{{ item.abnormalTaskThreshold }}
                · 库存≥{{ item.inventoryAlertThreshold }}
                · 床位≥{{ item.bedOccupancyThreshold }}%
                · 收入≤-{{ item.revenueDropThreshold }}%
              </span>
            </a-list-item>
          </template>
        </a-list>
      </a-drawer>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import VChart from 'vue-echarts'
import PageContainer from '../components/PageContainer.vue'
import {
  getDashboardMetricCatalog,
  getDashboardSummary,
  getDashboardThresholdDefaults,
  type DashboardMetricCatalog,
  type DashboardThresholdDefaults,
  type DashboardSummary
} from '../api/dashboard'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { useUserStore } from '../stores/user'
import PermissionGuardCard from '../components/PermissionGuardCard.vue'
import { resolveRouteAccess } from '../utils/routeAccess'
import StatefulBlock from '../components/StatefulBlock.vue'
import ThresholdPreviewList from '../components/ThresholdPreviewList.vue'
import StatsWorkspacePanel from '../components/stats/StatsWorkspacePanel.vue'
import StatsInsightDeck from '../components/stats/StatsInsightDeck.vue'
import StatsCommandCenter from '../components/stats/StatsCommandCenter.vue'
import {
  clearThresholdSnapshot,
  loadThresholdLogs,
  loadThresholdSnapshot,
  saveThresholdSnapshot,
  thresholdPulseKey,
  type ThresholdChangeLog,
  type ThresholdSnapshot
} from '../utils/dashboardThreshold'
import {
  DASHBOARD_THRESHOLD_PRESETS,
  mergeThresholdConfig,
  parseDashboardRouteFilters,
  parseThresholdQuery,
  thresholdSnapshotsEqual,
  toThresholdSnapshot
} from '../utils/dashboardQuery'
import { buildThresholdPreviewRows } from '../utils/dashboardThresholdPreview'

const loading = ref(true)
const errorMessage = ref('')
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const refreshedAt = ref('')
const metricDrawerOpen = ref(false)
const thresholdDrawerOpen = ref(false)
const thresholdLogs = ref<ThresholdChangeLog[]>([])
let thresholdStorageHandler: ((event: StorageEvent) => void) | null = null
const summary = ref<DashboardSummary>({
  careTasksToday: 0,
  abnormalTasksToday: 0,
  inventoryAlerts: 0,
  unpaidBills: 0,
  totalAdmissions: 0,
  totalDischarges: 0,
  checkInNetIncrease: 0,
  dischargeToAdmissionRate: 0,
  totalBillConsumption: 0,
  totalStoreConsumption: 0,
  totalConsumption: 0,
  averageMonthlyConsumption: 0,
  billConsumptionRatio: 0,
  storeConsumptionRatio: 0,
  inHospitalCount: 0,
  dischargedCount: 0,
  totalBeds: 0,
  occupiedBeds: 0,
  availableBeds: 0,
  bedOccupancyRate: 0,
  bedAvailableRate: 0,
  totalRevenue: 0,
  averageMonthlyRevenue: 0,
  revenueGrowthRate: 0,
  statsFromMonth: '',
  statsToMonth: '',
  metricVersion: '',
  metricEffectiveDate: '',
  dataRefreshedAt: ''
})
const metricCatalog = ref<DashboardMetricCatalog>({
  metricVersion: '',
  effectiveDate: '',
  defaultWindow: '最近6个月',
  definitions: []
})
const defaultThresholdConfig: DashboardThresholdDefaults = {
  abnormalTaskThreshold: 3,
  inventoryAlertThreshold: 10,
  bedOccupancyThreshold: 95,
  revenueDropThreshold: 5,
  configVersion: '--'
}
const thresholdConfig = ref<DashboardThresholdDefaults>({ ...defaultThresholdConfig })
const thresholdSavedSnapshot = ref<ThresholdSnapshot>(toThresholdSnapshot(defaultThresholdConfig))
const thresholdPresets = DASHBOARD_THRESHOLD_PRESETS
const thresholdDirty = computed(() =>
  !thresholdSnapshotsEqual(toThresholdSnapshot(thresholdConfig.value), thresholdSavedSnapshot.value)
)
const routeFilters = computed(() => parseDashboardRouteFilters(route.query))
const activeWindowText = computed(() => {
  const from = routeFilters.value.from || summary.value.statsFromMonth
  const to = routeFilters.value.to || summary.value.statsToMonth
  if (from && to) {
    return `${from} ~ ${to}`
  }
  return metricCatalog.value.defaultWindow || '最近6个月'
})
const activeWindowPreset = computed(() => {
  const from = routeFilters.value.from
  const to = routeFilters.value.to
  if (!from || !to) return 0
  const start = dayjs(`${from}-01`)
  const end = dayjs(`${to}-01`)
  if (!start.isValid() || !end.isValid() || end.isBefore(start)) return -1
  const months = end.diff(start, 'month') + 1
  return [3, 6, 12].includes(months) ? months : -1
})
const thresholdPreviewRows = computed(() =>
  buildThresholdPreviewRows(
    {
      abnormalCount: Number(summary.value.abnormalTasksToday || 0),
      inventoryCount: Number(summary.value.inventoryAlerts || 0),
      bedOccupancyRate: Number(summary.value.bedOccupancyRate || 0),
      revenueGrowthRate: Number(summary.value.revenueGrowthRate || 0)
    },
    toThresholdSnapshot(thresholdConfig.value)
  )
)
const dashboardTargets = ref<Record<string, number>>({
  totalRevenue: 0,
  totalConsumption: 0,
  bedOccupancyRate: 95,
  checkInNetIncrease: 0
})
const dashboardMetricKeys = ref<string[]>([])

async function loadSummary() {
  loading.value = true
  errorMessage.value = ''
  try {
    const params = routeFilters.value
    summary.value = await getDashboardSummary({ params })
    refreshedAt.value = summary.value.dataRefreshedAt
      ? dayjs(summary.value.dataRefreshedAt).format('YYYY-MM-DD HH:mm:ss')
      : dayjs().format('YYYY-MM-DD HH:mm:ss')
    try {
      metricCatalog.value = await getDashboardMetricCatalog()
    } catch (error: any) {
      message.warning(error?.message || '口径中心加载失败，已使用默认口径展示')
    }
  } catch (error: any) {
    errorMessage.value = error?.message || '加载运营看板失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

function patchRouteQuery(patch: Record<string, string | undefined>) {
  const nextQuery: Record<string, any> = { ...route.query }
  Object.entries(patch).forEach(([key, value]) => {
    if (!value) {
      delete nextQuery[key]
      return
    }
    nextQuery[key] = value
  })
  const nextFullPath = router.resolve({ path: route.path, query: nextQuery }).fullPath
  if (nextFullPath === route.fullPath) {
    return
  }
  router.replace({ path: route.path, query: nextQuery }).catch(() => {})
}

function applyWindowPreset(months: number) {
  const safeMonths = [3, 6, 12].includes(months) ? months : 6
  const to = dayjs().format('YYYY-MM')
  const from = dayjs().subtract(safeMonths - 1, 'month').format('YYYY-MM')
  patchRouteQuery({
    window: `${from}_${to}`,
    from: undefined,
    to: undefined
  })
}

function clearWindowPreset() {
  patchRouteQuery({
    window: undefined,
    from: undefined,
    to: undefined
  })
}

async function loadThresholdDefaults() {
  try {
    const defaults = await getDashboardThresholdDefaults()
    thresholdConfig.value = mergeThresholdConfig(defaults)
    const cache = loadThresholdSnapshot(userStore.staffInfo?.id)
    if (cache) {
      thresholdConfig.value = mergeThresholdConfig(thresholdConfig.value, cache)
    }
    const routeThresholdPatch = parseThresholdQuery(route.query)
    if (Object.keys(routeThresholdPatch).length > 0) {
      thresholdConfig.value = mergeThresholdConfig(thresholdConfig.value, routeThresholdPatch)
    }
    thresholdLogs.value = loadThresholdLogs(userStore.staffInfo?.id)
  } catch (error: any) {
    message.warning(error?.message || '阈值默认配置加载失败，已使用内置默认值')
    const routeThresholdPatch = parseThresholdQuery(route.query)
    if (Object.keys(routeThresholdPatch).length > 0) {
      thresholdConfig.value = mergeThresholdConfig(thresholdConfig.value, routeThresholdPatch)
    }
  }
  thresholdSavedSnapshot.value = toThresholdSnapshot(thresholdConfig.value)
}

function saveThresholdConfig() {
  const snapshot = toThresholdSnapshot(thresholdConfig.value)
  if (!thresholdDirty.value) {
    message.info('阈值未变化，无需保存')
    return
  }
  thresholdLogs.value = saveThresholdSnapshot(userStore.staffInfo?.id, snapshot, 'dashboard')
  thresholdSavedSnapshot.value = snapshot
  message.success('阈值配置已保存')
}

function applyThresholdPreset(snapshot: ThresholdSnapshot) {
  thresholdConfig.value = mergeThresholdConfig(thresholdConfig.value, snapshot)
}

function rollbackThresholdDraft() {
  thresholdConfig.value = mergeThresholdConfig(thresholdConfig.value, thresholdSavedSnapshot.value)
}

async function resetThresholdConfig() {
  clearThresholdSnapshot(userStore.staffInfo?.id)
  await loadThresholdDefaults()
  message.success('阈值配置已恢复默认')
}

const thresholdHintText = computed(() => {
  return `当前配置：异常≥${thresholdConfig.value.abnormalTaskThreshold}；库存≥${thresholdConfig.value.inventoryAlertThreshold}；床位≥${thresholdConfig.value.bedOccupancyThreshold}%；收入环比≤-${thresholdConfig.value.revenueDropThreshold}%`
})

const coreCards = computed(() => {
  const cardDefs = [
    {
      title: '护理任务',
      value: summary.value.careTasksToday || 0,
      tag: '今日任务',
      color: 'blue',
      trend: `异常 ${summary.value.abnormalTasksToday || 0}`,
      route: '/care/today'
    },
    {
      title: '库存预警',
      value: summary.value.inventoryAlerts || 0,
      tag: '低库存商品',
      color: 'orange',
      trend: '需及时补货',
      route: '/logistics/storage/stock-query'
    },
    {
      title: '未支付账单',
      value: summary.value.unpaidBills || 0,
      tag: '待回款',
      color: 'gold',
      trend: '财务关注',
      route: '/finance/reports/overall'
    },
    {
      title: '在院老人',
      value: summary.value.inHospitalCount || 0,
      tag: '实时在院',
      color: 'green',
      trend: `离院 ${summary.value.dischargedCount || 0}`,
      route: '/stats/elder-info'
    }
  ]
  const roles = userStore.roles || []
  return cardDefs.map((item) => {
    const access = resolveRouteAccess(router, roles, item.route)
    return { ...item, ...access }
  })
})

const unifiedCards = computed(() => {
  const cardDefs = [
    {
      title: '入住净增长',
      metricKey: 'check_in_net_increase',
      value: summary.value.checkInNetIncrease || 0,
      precision: 0,
      suffix: '',
      tag: '入住口径',
      color: 'blue',
      desc: `入住 ${summary.value.totalAdmissions || 0} / 离院 ${summary.value.totalDischarges || 0}`,
      route: '/stats/check-in'
    },
    {
      title: '离院/入住比',
      metricKey: 'discharge_to_admission_rate',
      value: summary.value.dischargeToAdmissionRate || 0,
      precision: 2,
      suffix: '%',
      tag: '入住口径',
      color: 'cyan',
      desc: '与统计分析模块一致',
      route: '/stats/check-in'
    },
    {
      title: '总消费',
      metricKey: 'total_consumption',
      value: summary.value.totalConsumption || 0,
      precision: 2,
      suffix: '元',
      tag: '消费口径',
      color: 'purple',
      desc: `月均 ${fmtAmount(summary.value.averageMonthlyConsumption)} 元`,
      route: '/stats/consumption'
    },
    {
      title: '账单消费占比',
      metricKey: 'bill_consumption_ratio',
      value: summary.value.billConsumptionRatio || 0,
      precision: 2,
      suffix: '%',
      tag: '消费口径',
      color: 'geekblue',
      desc: `商城占比 ${fmtPercent(summary.value.storeConsumptionRatio)}%`,
      route: '/stats/consumption'
    },
    {
      title: '床位使用率',
      metricKey: 'bed_occupancy_rate',
      value: summary.value.bedOccupancyRate || 0,
      precision: 2,
      suffix: '%',
      tag: '床位口径',
      color: 'green',
      desc: `总床位 ${summary.value.totalBeds || 0}`,
      route: '/stats/org/bed-usage'
    },
    {
      title: '床位空闲率',
      metricKey: 'bed_available_rate',
      value: summary.value.bedAvailableRate || 0,
      precision: 2,
      suffix: '%',
      tag: '床位口径',
      color: 'lime',
      desc: `空闲床位 ${summary.value.availableBeds || 0}`,
      route: '/stats/org/bed-usage'
    },
    {
      title: '总收入',
      metricKey: 'total_revenue',
      value: summary.value.totalRevenue || 0,
      precision: 2,
      suffix: '元',
      tag: '收入口径',
      color: 'gold',
      desc: `月均 ${fmtAmount(summary.value.averageMonthlyRevenue)} 元`,
      route: '/stats/monthly-revenue'
    },
    {
      title: '最近收入环比',
      metricKey: 'revenue_growth_rate',
      value: summary.value.revenueGrowthRate || 0,
      precision: 2,
      suffix: '%',
      tag: '收入口径',
      color: summary.value.revenueGrowthRate >= 0 ? 'success' : 'error',
      desc: '最近两个月对比',
      route: '/stats/monthly-revenue'
    }
  ]
  const roles = userStore.roles || []
  return cardDefs.map((item) => {
    const access = resolveRouteAccess(router, roles, item.route)
    return { ...item, ...access }
  })
})
const visibleUnifiedCards = computed(() => {
  const selected = dashboardMetricKeys.value.length
    ? new Set(dashboardMetricKeys.value)
    : null
  return unifiedCards.value.filter((item) => !selected || selected.has(item.metricKey))
})

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 20, right: 16, top: 20, bottom: 20, containLabel: true },
  xAxis: {
    type: 'category',
    data: ['离院/入住比', '床位使用率', '床位空闲率', '账单消费占比', '最近收入环比']
  },
  yAxis: { type: 'value', axisLabel: { formatter: '{value}%' } },
  series: [
    {
      data: [
        summary.value.dischargeToAdmissionRate || 0,
        summary.value.bedOccupancyRate || 0,
        summary.value.bedAvailableRate || 0,
        summary.value.billConsumptionRatio || 0,
        summary.value.revenueGrowthRate || 0
      ],
      type: 'bar',
      color: '#1677ff'
    }
  ]
}))

const alerts = computed(() => {
  const rows: Array<{ title: string; desc: string; level: '高' | '中' }> = []
  const abnormalThreshold = Number(thresholdConfig.value.abnormalTaskThreshold || 0)
  const inventoryThreshold = Number(thresholdConfig.value.inventoryAlertThreshold || 0)
  const bedThreshold = Number(thresholdConfig.value.bedOccupancyThreshold || 0)
  const revenueDropThreshold = Number(thresholdConfig.value.revenueDropThreshold || 0)

  if ((summary.value.abnormalTasksToday || 0) >= abnormalThreshold) {
    rows.push({ title: '护理异常任务', desc: `今日 ${summary.value.abnormalTasksToday} 项，阈值 ${abnormalThreshold}`, level: '高' })
  }
  if ((summary.value.inventoryAlerts || 0) >= inventoryThreshold) {
    rows.push({ title: '库存预警', desc: `${summary.value.inventoryAlerts} 个商品低于安全库存，阈值 ${inventoryThreshold}`, level: '中' })
  }
  if ((summary.value.bedOccupancyRate || 0) >= bedThreshold) {
    rows.push({ title: '床位高占用', desc: `当前使用率 ${summary.value.bedOccupancyRate}% ，阈值 ${bedThreshold}%`, level: '中' })
  }
  if ((summary.value.revenueGrowthRate || 0) <= 0 - revenueDropThreshold) {
    rows.push({ title: '收入环比下滑', desc: `最近环比 ${summary.value.revenueGrowthRate}% ，阈值 -${revenueDropThreshold}%`, level: '中' })
  }
  return rows
})

const alertActions = computed(() => alerts.value.map((item) => ({
  title: item.title,
  route: resolveAlertRoute(item.title)
})).filter((item) => !!item.route))

const metricColumns = [
  { title: '指标', dataIndex: 'metric', key: 'metric' },
  { title: '数值', dataIndex: 'value', key: 'value', width: 140 },
  { title: '说明', dataIndex: 'remark', key: 'remark' }
]

const metricRows = computed(() => [
  {
    key: 1,
    metric: '在院 / 离院',
    value: `${summary.value.inHospitalCount || 0} / ${summary.value.dischargedCount || 0}`,
    remark: '老人信息统计口径'
  },
  {
    key: 2,
    metric: '床位占用 / 空闲',
    value: `${summary.value.occupiedBeds || 0} / ${summary.value.availableBeds || 0}`,
    remark: '床位使用统计口径'
  },
  {
    key: 3,
    metric: '账单消费 / 商城消费',
    value: `${fmtAmount(summary.value.totalBillConsumption)} / ${fmtAmount(summary.value.totalStoreConsumption)}`,
    remark: '消费统计口径（元）'
  },
  {
    key: 4,
    metric: '月均消费 / 月均收入',
    value: `${fmtAmount(summary.value.averageMonthlyConsumption)} / ${fmtAmount(summary.value.averageMonthlyRevenue)}`,
    remark: '近6个月统一口径（元）'
  },
  {
    key: 5,
    metric: '口径版本 / 生效日期',
    value: `${summary.value.metricVersion || metricCatalog.value.metricVersion || '--'} / ${summary.value.metricEffectiveDate || metricCatalog.value.effectiveDate || '--'}`,
    remark: `${summary.value.statsFromMonth || '--'} ~ ${summary.value.statsToMonth || '--'}`
  }
])

const billOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { value: summary.value.totalBillConsumption || 0, name: '账单消费' },
        { value: summary.value.totalStoreConsumption || 0, name: '商城消费' }
      ],
      label: { formatter: '{b}: {d}%'}
    }
  ]
}))

const dashboardWorkspaceSummary = computed(() => {
  return `围绕收入、消费、床位与入住净增长统一查看当前口径，并沉淀筛选方案与订阅节奏。当前窗口 ${activeWindowText.value}。`
})

const dashboardWorkspacePayload = computed(() => ({
  window: route.query.window || '',
  from: route.query.from || '',
  to: route.query.to || '',
  metricVersion: summary.value.metricVersion || metricCatalog.value.metricVersion || '',
  abnormalTaskThreshold: thresholdConfig.value.abnormalTaskThreshold,
  inventoryAlertThreshold: thresholdConfig.value.inventoryAlertThreshold,
  bedOccupancyThreshold: thresholdConfig.value.bedOccupancyThreshold,
  revenueDropThreshold: thresholdConfig.value.revenueDropThreshold,
  totalRevenue: dashboardTargets.value.totalRevenue,
  totalConsumption: dashboardTargets.value.totalConsumption,
  bedOccupancyRate: dashboardTargets.value.bedOccupancyRate,
  checkInNetIncrease: dashboardTargets.value.checkInNetIncrease
}))

const dashboardTargetFields = computed(() => ([
  { key: 'totalRevenue', label: '总收入目标(元)', value: dashboardTargets.value.totalRevenue, min: 0, max: 999999999, step: 1000 },
  { key: 'totalConsumption', label: '总消费目标(元)', value: dashboardTargets.value.totalConsumption, min: 0, max: 999999999, step: 1000 },
  { key: 'bedOccupancyRate', label: '床位使用率上限(%)', value: dashboardTargets.value.bedOccupancyRate, min: 1, max: 100, step: 1 },
  { key: 'checkInNetIncrease', label: '入住净增长目标', value: dashboardTargets.value.checkInNetIncrease, min: -9999, max: 9999, step: 1 }
]))

const dashboardDataHealth = computed(() => ({
  freshness: refreshedAt.value || '--',
  completeness: summary.value.metricVersion ? '已对齐统一口径' : '基础模式',
  issues: alerts.value.map((item) => `${item.title}：${item.desc}`)
}))

const dashboardInsightItems = computed(() => {
  const revenueTarget = Number(dashboardTargets.value.totalRevenue || 0)
  const consumptionTarget = Number(dashboardTargets.value.totalConsumption || 0)
  const revenueGap = Number(summary.value.totalRevenue || 0) - revenueTarget
  const consumptionGap = Number(summary.value.totalConsumption || 0) - consumptionTarget
  const hasRevenueTarget = revenueTarget > 0
  const hasConsumptionTarget = consumptionTarget > 0
  const occupancyGap = Number(summary.value.bedOccupancyRate || 0) - Number(dashboardTargets.value.bedOccupancyRate || 0)
  const checkInGap = Number(summary.value.checkInNetIncrease || 0) - Number(dashboardTargets.value.checkInNetIncrease || 0)
  return [
    {
      key: 'revenue',
      label: '总收入对比',
      valueText: `${fmtAmount(summary.value.totalRevenue)} 元`,
      trendText: hasRevenueTarget ? `目标差 ${fmtAmount(revenueGap)} 元` : '等待配置目标',
      detail: `当前窗口 ${activeWindowText.value} · 平均月收入 ${fmtAmount(summary.value.averageMonthlyRevenue)} 元`,
      tone: hasRevenueTarget && revenueGap < 0 ? 'warning' : 'good'
    },
    {
      key: 'consumption',
      label: '总消费对比',
      valueText: `${fmtAmount(summary.value.totalConsumption)} 元`,
      trendText: hasConsumptionTarget ? `目标差 ${fmtAmount(consumptionGap)} 元` : '等待配置目标',
      detail: `当前窗口 ${activeWindowText.value} · 账单占比 ${fmtPercent(summary.value.billConsumptionRatio)}%`,
      tone: hasConsumptionTarget && consumptionGap < 0 ? 'warning' : 'good'
    },
    {
      key: 'occupancy',
      label: '床位使用率监测',
      valueText: `${fmtPercent(summary.value.bedOccupancyRate)}%`,
      trendText: occupancyGap > 0 ? `超目标 ${fmtPercent(occupancyGap)}%` : `距上限 ${fmtPercent(Math.abs(occupancyGap))}%`,
      detail: `空闲率 ${fmtPercent(summary.value.bedAvailableRate)}% · 维护异常请联动床位处置`,
      tone: occupancyGap > 0 ? 'danger' : 'good'
    },
    {
      key: 'checkin',
      label: '入住净增长目标',
      valueText: `${summary.value.checkInNetIncrease || 0}`,
      trendText: `目标差 ${checkInGap >= 0 ? '+' : ''}${checkInGap}`,
      detail: `入住 ${summary.value.totalAdmissions || 0} / 离院 ${summary.value.totalDischarges || 0}`,
      tone: checkInGap < 0 ? 'warning' : 'good'
    }
  ]
})
const dashboardCommandSummary = computed(() => {
  return '把异常处置、自定义指标和纠错入口集中在同一个操作台，减少在看板与统计页之间来回切换。'
})
const dashboardActionItems = computed(() => {
  const rows = alertActions.value.map((item) => ({
    key: item.title,
    label: `处置${item.title}`,
    description: `直达 ${item.route}`,
    route: item.route,
    tone: 'danger' as const
  }))
  rows.push(
    {
      key: 'open-revenue',
      label: '查看收入明细',
      description: '联动月收入统计与机构经营页',
      route: '/stats/monthly-revenue',
      tone: 'neutral' as const
    }
  )
  return rows
})
const dashboardAnomalies = computed(() =>
  alerts.value.map((item, index) => ({
    key: `alert-${index}`,
    label: item.title,
    detail: item.desc,
    tone: item.level === '高' ? 'danger' : 'warning'
  }))
)
const dashboardMetricNotes = computed(() => [
  { key: 'consumption', label: '总消费', note: '总消费=账单消费+商城消费，并与当前时间窗口保持一致。' },
  { key: 'revenue', label: '总收入', note: '总收入按账单总额统计，跟月运营收入页保持同口径。' },
  { key: 'bed', label: '床位使用率', note: '当前已使用床位 / 总床位，维护床位不计入已使用。' },
  { key: 'checkin', label: '入住净增长', note: '净增长=入住人数-离院人数，支持继续钻取到入住统计页。' }
])
const dashboardPanelOptions = computed(() =>
  unifiedCards.value.map((item) => ({
    key: item.metricKey,
    label: item.title,
    description: item.desc
  }))
)

function onDashboardTargetsChange(payload: Record<string, number>) {
  dashboardTargets.value = {
    ...dashboardTargets.value,
    ...(payload || {})
  }
}

function onDashboardPresetApply(payload: Record<string, any>) {
  thresholdConfig.value = mergeThresholdConfig(thresholdConfig.value, {
    abnormalTaskThreshold: payload.abnormalTaskThreshold,
    inventoryAlertThreshold: payload.inventoryAlertThreshold,
    bedOccupancyThreshold: payload.bedOccupancyThreshold,
    revenueDropThreshold: payload.revenueDropThreshold
  })
  dashboardTargets.value = {
    ...dashboardTargets.value,
    totalRevenue: normalizeTargetValue(payload.totalRevenue, dashboardTargets.value.totalRevenue ?? 0),
    totalConsumption: normalizeTargetValue(payload.totalConsumption, dashboardTargets.value.totalConsumption ?? 0),
    bedOccupancyRate: normalizeTargetValue(payload.bedOccupancyRate, dashboardTargets.value.bedOccupancyRate ?? 95),
    checkInNetIncrease: normalizeTargetValue(payload.checkInNetIncrease, dashboardTargets.value.checkInNetIncrease ?? 0)
  }
  patchRouteQuery({
    window: String(payload.window || '').trim() || undefined,
    from: String(payload.from || '').trim() || undefined,
    to: String(payload.to || '').trim() || undefined
  })
}

function onDashboardPanelChange(keys: string[]) {
  dashboardMetricKeys.value = keys.length ? [...keys] : unifiedCards.value.map((item) => item.metricKey)
}

function onDashboardCommandAction(item: { key: string; route?: string }) {
  if (item.route) {
    go(item.route)
  }
}

function onDashboardTemplateApply(payload: { payload: Record<string, any> }) {
  onDashboardPresetApply(payload.payload || {})
}

function go(path: string) {
  router.push(path)
}

function resolveAlertRoute(title: string) {
  if (title.includes('护理异常')) return '/care/today'
  if (title.includes('库存预警')) return '/logistics/storage/alerts'
  if (title.includes('床位高占用')) return '/stats/org/bed-usage'
  if (title.includes('收入环比')) return '/stats/monthly-revenue'
  return '/dashboard'
}

function buildDrillQuery(metricKey: string, routePath: string) {
  const query: Record<string, string> = {
    fromSource: 'dashboard',
    metricKey
  }
  if (summary.value.statsFromMonth && summary.value.statsToMonth && routePath !== '/stats/org/bed-usage') {
    query.from = summary.value.statsFromMonth
    query.to = summary.value.statsToMonth
  }
  if (summary.value.metricVersion) {
    query.metricVersion = summary.value.metricVersion
  }
  return query
}

function goUnifiedCard(card: { route: string; metricKey: string }) {
  router.push({
    path: card.route,
    query: buildDrillQuery(card.metricKey, card.route)
  })
}

function fmtAmount(value?: number) {
  return Number(value || 0).toFixed(2)
}

function fmtPercent(value?: number) {
  return Number(value || 0).toFixed(2)
}

function normalizeTargetValue(value: unknown, fallback: number) {
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric : fallback
}

watch(
  () => [route.query.metricVersion, route.query.window, route.query.from, route.query.to],
  () => {
    loadSummary().catch(() => {})
  }
)

watch(
  () => [
    route.query.abnormalTaskThreshold,
    route.query.inventoryAlertThreshold,
    route.query.bedOccupancyThreshold,
    route.query.revenueDropThreshold
  ],
  () => {
    const patch = parseThresholdQuery(route.query)
    if (!Object.keys(patch).length) return
    thresholdConfig.value = mergeThresholdConfig(thresholdConfig.value, patch)
    thresholdSavedSnapshot.value = toThresholdSnapshot(thresholdConfig.value)
  }
)

onMounted(async () => {
  await Promise.all([loadSummary(), loadThresholdDefaults()])
  thresholdStorageHandler = (event: StorageEvent) => {
    if (event.key !== thresholdPulseKey()) return
    loadThresholdDefaults().catch(() => {})
    message.info('阈值配置已由其他页面更新')
  }
  window.addEventListener('storage', thresholdStorageHandler)
})

onBeforeUnmount(() => {
  if (thresholdStorageHandler) {
    window.removeEventListener('storage', thresholdStorageHandler)
  }
  thresholdStorageHandler = null
})
</script>

<style scoped>
.chart {
  height: 260px;
}

.card-meta {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--muted);
}

.hint-text {
  color: var(--muted);
  font-size: 12px;
}

.metric-def-line {
  margin-top: 2px;
  color: var(--muted);
}

.threshold-preset-row {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

.window-toolbar {
  margin-bottom: 12px;
}
</style>
