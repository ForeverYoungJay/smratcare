<template>
  <PageContainer class="rm-page" title="客户与跟进" sub-title="客户建档、分配、跟进与回访集中在同一个页面；点客户「详情」查看完整沟通历史。">
    <template #extra>
      <a-button type="primary" @click="createCustomer">新增客户</a-button>
    </template>

    <div class="rm-kpis">
      <button v-for="card in kpis" :key="card.key" type="button" class="rm-kpi" @click="go(card.tab, card.extra)">
        <span class="rm-kpi__label">{{ card.label }}</span>
        <span class="rm-kpi__value">{{ summary ? card.value : '--' }}</span>
        <span class="rm-kpi__hint">{{ card.hint }}</span>
        <span class="rm-kpi__icon" :class="card.tone && `rm-kpi__icon--${card.tone}`"><component :is="card.icon" /></span>
      </button>
    </div>

    <section class="rm-card">
      <a-tabs :active-key="view.tab" @change="(key: string | number) => go(key as CustomerTab)">
        <a-tab-pane v-for="tab in CUSTOMER_TABS" :key="tab.key">
          <template #tab>{{ tab.label }} <span v-if="summary" class="cc-count">{{ tabCount(tab.key) }}</span></template>
        </a-tab-pane>
      </a-tabs>

      <div v-if="view.tab === 'followup'" class="cc-followup-bar">
        <a-radio-group :value="view.followupView" button-style="solid" size="small" @change="(e: any) => go('followup', { view: e.target.value, ...(view.callbackType ? { callbackType: view.callbackType } : {}) })">
          <a-radio-button value="today">今日</a-radio-button>
          <a-radio-button value="due">待回访</a-radio-button>
          <a-radio-button value="overdue">已逾期</a-radio-button>
        </a-radio-group>
        <a-select :value="view.callbackType || ''" size="small" style="width: 140px" :options="CALLBACK_TYPES"
          @change="(value: any) => go('followup', { view: view.followupView || 'due', ...(value ? { callbackType: value } : {}) })" />
      </div>

      <EmbeddedPage>
        <LeadPipelineTable :key="tableKey" :mode="view.mode" :title="currentTab.label" sub-title="" :scenario="view.scenario" :callback-type="view.callbackType || ''" />
      </EmbeddedPage>
    </section>

    <a-alert class="cc-note" type="warning" show-icon message="客户资料与长者正式在住档案分开管理：正式入住后按授权移交必要信息，市场人员不录入完整病历或护理记录。" />
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ClockCircleOutlined, PhoneOutlined, StarOutlined, TeamOutlined } from '@ant-design/icons-vue'
import PageContainer from '../../../components/PageContainer.vue'
import LeadPipelineTable from '../components/LeadPipelineTable.vue'
import EmbeddedPage from './EmbeddedPage.vue'
import { getMarketingLeadEntrySummary } from '../../../api/marketing'
import type { MarketingLeadEntrySummary, MarketingLeadMode } from '../../../types'
import { CUSTOMER_TABS, customerTabQuery, resolveCustomerView, type CustomerTab } from './customerModel'
import '../../elder/residentUi.css'

const route = useRoute()
const router = useRouter()
const summary = ref<MarketingLeadEntrySummary | null>(null)

const CALLBACK_TYPES = [
  { value: '', label: '全部回访类型' }, { value: 'checkin', label: '入住后回访' },
  { value: 'trial', label: '试住回访' }, { value: 'discharge', label: '退住回访' }
]

const view = computed(() => resolveCustomerView(route.query))
const currentTab = computed(() => CUSTOMER_TABS.find((tab) => tab.key === view.value.tab)!)
const tableKey = computed(() => `${view.value.tab}-${view.value.scenario}-${view.value.callbackType || ''}`)

const kpis = computed(() => {
  const s = summary.value
  return [
    { key: 'total', label: '客户总量', value: s?.totalCount ?? 0, hint: '当前账号可见客户池', icon: TeamOutlined, tab: 'all' as CustomerTab },
    { key: 'today', label: '今日待跟进', value: s?.callbackDueTodayCount ?? 0, hint: `待回访共 ${s?.callbackPendingCount ?? 0} 人`, icon: PhoneOutlined, tab: 'followup' as CustomerTab, extra: { view: 'today' } },
    { key: 'intent', label: '高意向客户', value: s?.intentCount ?? 0, hint: '重点推进参观与预订', icon: StarOutlined, tab: 'intent' as CustomerTab },
    { key: 'overdue', label: '逾期未跟进', value: s?.callbackOverdueCount ?? 0, hint: '建议优先回访', icon: ClockCircleOutlined, tone: 'warning', tab: 'followup' as CustomerTab, extra: { view: 'overdue' } }
  ]
})

function tabCount(tab: CustomerTab) {
  const s = summary.value
  if (!s) return ''
  return { all: s.totalCount, intent: s.intentCount, followup: s.callbackPendingCount, reservation: s.reservationCount, invalid: s.invalidCount }[tab] ?? ''
}

function go(tab: CustomerTab, extra: Record<string, string> = {}) {
  router.replace({ path: route.path, query: customerTabQuery(route.query, tab, extra) })
}

function createCustomer() {
  // LeadPipelineTable 识别 quick=1 打开新增弹窗（含重复客户校验）
  router.replace({ path: route.path, query: { ...customerTabQuery(route.query, 'all'), quick: '1' } })
}

async function loadSummary() {
  // 后端未识别的 mode 按全部客户统计
  summary.value = await getMarketingLeadEntrySummary({ mode: 'all' as MarketingLeadMode }).catch(() => null)
}

onMounted(() => {
  loadSummary()
  // 工作台等处的「新增客户」带 action=create 过来
  if (route.query.action === 'create') {
    const { action: _a, ...rest } = route.query
    router.replace({ path: route.path, query: { ...rest, quick: '1' } })
  }
})
watch(() => view.value.tab, loadSummary)
</script>

<style scoped>
.cc-count { margin-left: 2px; color: var(--rm-muted); font-size: 12px; }
.cc-followup-bar { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.cc-note { margin-top: 16px; }
</style>
