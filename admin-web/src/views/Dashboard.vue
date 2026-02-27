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
          <span class="hint-text">更新时间：{{ refreshedAt || '--' }}</span>
        </template>
        <div class="hint-text" style="margin-bottom: 12px;">
          口径说明：近6个月；总消费=账单消费+商城消费；总收入=账单总额。
        </div>
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" :lg="8" v-for="item in unifiedCards" :key="item.title">
            <PermissionGuardCard
              size="small"
              :can-access="item.canAccess"
              :required-roles="item.requiredRoles"
              card-class="clickable-card"
              @click="go(item.route)"
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
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import VChart from 'vue-echarts'
import PageContainer from '../components/PageContainer.vue'
import { getDashboardSummary, type DashboardSummary } from '../api/dashboard'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { useUserStore } from '../stores/user'
import PermissionGuardCard from '../components/PermissionGuardCard.vue'
import { resolveRouteAccess } from '../utils/routeAccess'
import StatefulBlock from '../components/StatefulBlock.vue'

const loading = ref(true)
const errorMessage = ref('')
const router = useRouter()
const userStore = useUserStore()
const refreshedAt = ref('')
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
  revenueGrowthRate: 0
})

async function loadSummary() {
  loading.value = true
  errorMessage.value = ''
  try {
    summary.value = await getDashboardSummary()
    refreshedAt.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
  } catch (error: any) {
    errorMessage.value = error?.message || '加载运营看板失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

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
      route: '/material/stock-query'
    },
    {
      title: '未支付账单',
      value: summary.value.unpaidBills || 0,
      tag: '待回款',
      color: 'gold',
      trend: '财务关注',
      route: '/finance/report'
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
  if ((summary.value.abnormalTasksToday || 0) > 0) {
    rows.push({ title: '护理异常任务', desc: `今日 ${summary.value.abnormalTasksToday} 项待关注`, level: '高' })
  }
  if ((summary.value.inventoryAlerts || 0) > 0) {
    rows.push({ title: '库存预警', desc: `${summary.value.inventoryAlerts} 个商品低于安全库存`, level: '中' })
  }
  if ((summary.value.bedOccupancyRate || 0) > 95) {
    rows.push({ title: '床位高占用', desc: `当前使用率 ${summary.value.bedOccupancyRate}%`, level: '中' })
  }
  if ((summary.value.revenueGrowthRate || 0) < 0) {
    rows.push({ title: '收入环比下滑', desc: `最近环比 ${summary.value.revenueGrowthRate}%`, level: '中' })
  }
  return rows
})

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

function fmtAmount(value?: number) {
  return Number(value || 0).toFixed(2)
}

function fmtPercent(value?: number) {
  return Number(value || 0).toFixed(2)
}

onMounted(loadSummary)
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
</style>
