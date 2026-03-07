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

      <a-card class="card-elevated" :bordered="false" style="margin-top: 16px" title="统计分析统一口径（近6个月）">
        <template #extra>
          <a-space size="small">
            <span class="hint-text">更新时间：{{ refreshedAt || '--' }}</span>
            <a-tag color="blue">口径版本 {{ summary.metricVersion || metricCatalog.metricVersion || '--' }}</a-tag>
            <a-tag color="purple">阈值配置 {{ thresholdConfig.configVersion || '--' }}</a-tag>
            <a-button size="small" @click="metricDrawerOpen = true">口径详情</a-button>
            <a-button size="small" @click="thresholdDrawerOpen = true">阈值设置</a-button>
            <a-button size="small" @click="copyDashboardShareLink">复制筛选链接</a-button>
          </a-space>
        </template>
        <div class="hint-text" style="margin-bottom: 12px;">
          口径说明：{{ metricCatalog.defaultWindow || '最近6个月' }}；总消费=账单消费+商城消费；总收入=账单总额。
        </div>
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" :lg="8" v-for="item in unifiedCards" :key="item.title">
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
        <a-space>
          <a-button type="primary" @click="saveThresholdConfig">保存配置</a-button>
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
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
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
import { copyText } from '../utils/clipboard'
import {
  clearThresholdSnapshot,
  loadThresholdLogs,
  loadThresholdSnapshot,
  saveThresholdSnapshot,
  thresholdPulseKey,
  type ThresholdChangeLog
} from '../utils/dashboardThreshold'

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
const thresholdConfig = ref<DashboardThresholdDefaults>({
  abnormalTaskThreshold: 3,
  inventoryAlertThreshold: 10,
  bedOccupancyThreshold: 95,
  revenueDropThreshold: 5,
  configVersion: '--'
})

async function loadSummary() {
  loading.value = true
  errorMessage.value = ''
  try {
    summary.value = await getDashboardSummary()
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

async function loadThresholdDefaults() {
  try {
    const defaults = await getDashboardThresholdDefaults()
    thresholdConfig.value = { ...defaults }
    const cache = loadThresholdSnapshot(userStore.staffInfo?.id)
    if (cache) {
      thresholdConfig.value = {
        ...thresholdConfig.value,
        ...cache,
        configVersion: thresholdConfig.value.configVersion
      }
    }
    thresholdLogs.value = loadThresholdLogs(userStore.staffInfo?.id)
    applyThresholdsFromRouteQuery()
  } catch (error: any) {
    message.warning(error?.message || '阈值默认配置加载失败，已使用内置默认值')
  }
}

function applyThresholdsFromRouteQuery() {
  const abnormalTaskThreshold = Number(route.query.abnormalTaskThreshold)
  const inventoryAlertThreshold = Number(route.query.inventoryAlertThreshold)
  const bedOccupancyThreshold = Number(route.query.bedOccupancyThreshold)
  const revenueDropThreshold = Number(route.query.revenueDropThreshold)
  if (Number.isFinite(abnormalTaskThreshold) && abnormalTaskThreshold > 0) {
    thresholdConfig.value.abnormalTaskThreshold = abnormalTaskThreshold
  }
  if (Number.isFinite(inventoryAlertThreshold) && inventoryAlertThreshold > 0) {
    thresholdConfig.value.inventoryAlertThreshold = inventoryAlertThreshold
  }
  if (Number.isFinite(bedOccupancyThreshold) && bedOccupancyThreshold > 0) {
    thresholdConfig.value.bedOccupancyThreshold = bedOccupancyThreshold
  }
  if (Number.isFinite(revenueDropThreshold) && revenueDropThreshold > 0) {
    thresholdConfig.value.revenueDropThreshold = revenueDropThreshold
  }
}

function saveThresholdConfig() {
  const snapshot = {
    abnormalTaskThreshold: Number(thresholdConfig.value.abnormalTaskThreshold || 0),
    inventoryAlertThreshold: Number(thresholdConfig.value.inventoryAlertThreshold || 0),
    bedOccupancyThreshold: Number(thresholdConfig.value.bedOccupancyThreshold || 0),
    revenueDropThreshold: Number(thresholdConfig.value.revenueDropThreshold || 0)
  }
  thresholdLogs.value = saveThresholdSnapshot(userStore.staffInfo?.id, snapshot, 'dashboard')
  message.success('阈值配置已保存')
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

async function copyDashboardShareLink() {
  const resolved = router.resolve({
    path: route.path,
    query: {
      metricVersion: summary.value.metricVersion || metricCatalog.value.metricVersion || '',
      abnormalTaskThreshold: String(thresholdConfig.value.abnormalTaskThreshold || ''),
      inventoryAlertThreshold: String(thresholdConfig.value.inventoryAlertThreshold || ''),
      bedOccupancyThreshold: String(thresholdConfig.value.bedOccupancyThreshold || ''),
      revenueDropThreshold: String(thresholdConfig.value.revenueDropThreshold || ''),
      window: summary.value.statsFromMonth && summary.value.statsToMonth
        ? `${summary.value.statsFromMonth}_${summary.value.statsToMonth}`
        : ''
    }
  })
  const ok = await copyText(`${window.location.origin}${resolved.fullPath}`)
  if (ok) {
    message.success('链接已复制')
  } else {
    message.warning('复制失败，请手动复制地址栏链接')
  }
}

function fmtAmount(value?: number) {
  return Number(value || 0).toFixed(2)
}

function fmtPercent(value?: number) {
  return Number(value || 0).toFixed(2)
}

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
</style>
