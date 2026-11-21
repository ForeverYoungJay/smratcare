<template>
  <PageContainer class="rm-page mk-workbench" title="市场工作台" sub-title="以待办为中心，一眼了解线索、接待与签约进展。">
    <template #extra>
      <a-space wrap>
        <a-button :loading="loading" @click="load">刷新</a-button>
        <a-button @click="go('/marketing/visits', { action: 'create' })"><CalendarOutlined />预约参观</a-button>
        <a-button type="primary" @click="go('/marketing/customers', { action: 'create' })"><PlusOutlined />新增客户</a-button>
      </a-space>
    </template>

    <a-alert v-if="errorMessage" type="error" show-icon :message="errorMessage" style="margin-bottom: 16px" />

    <section class="rm-kpis mk-kpis" aria-label="本月关键指标">
      <button v-for="card in kpis" :key="card.key" type="button" class="rm-kpi" @click="go(card.path, card.query)">
        <span class="rm-kpi__label">{{ card.label }}</span>
        <span class="rm-kpi__icon" :class="card.tone ? `rm-kpi__icon--${card.tone}` : ''"><component :is="card.icon" /></span>
        <strong class="rm-kpi__value">{{ card.value }}</strong>
        <span class="rm-kpi__hint">{{ card.hint }}</span>
      </button>
    </section>

    <div class="rm-split">
      <div class="mk-col">
        <section class="rm-card">
          <div class="rm-card__head">
            <h2 class="rm-card__title">今日重点待办</h2>
            <span class="rm-card__sub">{{ todos.length ? `共 ${todos.length} 项` : '' }}</span>
            <button type="button" class="rm-link rm-card__extra" @click="go('/marketing/customers', { tab: 'followup', view: 'today' })">查看全部待办 ›</button>
          </div>
          <a-spin :spinning="loading">
            <a-empty v-if="!todos.length" description="今天没有需要处理的待办" />
            <div v-for="item in todos.slice(0, 8)" :key="item.key" class="rm-row">
              <span class="rm-avatar" :class="{ 'mk-avatar--danger': item.overdue }"><component :is="todoIcon[item.kind]" /></span>
              <div class="rm-row__main">
                <span class="rm-row__title">{{ item.title }} <span v-if="item.overdue" class="rm-pill rm-pill--danger">已超时</span></span>
                <span class="rm-row__meta">{{ item.desc }}</span>
              </div>
              <span class="rm-row__meta mk-time">{{ todoTime(item.time) }}</span>
              <a-button size="small" @click="go(item.path, item.query)">{{ item.actionLabel }}</a-button>
            </div>
          </a-spin>
        </section>

        <section class="rm-card">
          <div class="rm-card__head">
            <h2 class="rm-card__title">近 6 个月线索与签约趋势</h2>
            <span class="rm-card__sub">按线索登记日期归属月份</span>
          </div>
          <TrendChart :labels="trend.map((m) => m.label)" :series="trendSeries" />
        </section>
      </div>

      <div class="mk-col">
        <section class="rm-card">
          <div class="rm-card__head"><h2 class="rm-card__title">快捷办理</h2></div>
          <div class="rm-actions">
            <button v-for="action in quickActions" :key="action.label" type="button" class="rm-action" @click="go(action.path, action.query)">
              <component :is="action.icon" />{{ action.label }}
            </button>
          </div>
        </section>

        <section class="rm-card">
          <div class="rm-card__head">
            <h2 class="rm-card__title">本月客户转化概览</h2>
            <button v-if="canOperate" type="button" class="rm-link rm-card__extra" @click="go('/marketing/operations')">详细分析 ›</button>
          </div>
          <div class="mk-conv-head">
            <div>
              <strong class="mk-conv-rate">{{ formatRate(current?.contractCount, current?.totalLeads) }}</strong>
              <div class="rm-card__sub">线索 → 签约转化率</div>
            </div>
            <span class="rm-pill rm-pill--success">本月已签 {{ show(current?.contractCount) }} 人</span>
          </div>
          <button v-for="stage in stages" :key="stage.key" type="button" class="mk-bar" @click="go(stage.path, stage.query)">
            <span class="mk-bar__label">{{ stage.label }}</span>
            <span class="mk-bar__track"><span class="mk-bar__fill" :style="{ width: `${stage.width}%` }" /></span>
            <strong>{{ stage.count }}</strong>
          </button>
          <p class="rm-card__sub mk-foot">各阶段按本月登记的线索去重统计；到访按参观预约的到访记录统计。</p>
        </section>
      </div>
    </div>

    <div class="rm-note">
      <SafetyCertificateOutlined />
      <span>营销、护理、财务多部门协同：市场部管理线索与预约；护理/医务确认专业评估；财务确认款项；正式入住由长者管理办理，避免重复建档。</span>
      <button type="button" class="rm-link mk-note-link" @click="go('/marketing/contracts')">查看签约交接 ›</button>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import {
  AimOutlined, AlertOutlined, BarChartOutlined, CalendarOutlined, FileTextOutlined, HomeOutlined,
  PhoneOutlined, PlusOutlined, SafetyCertificateOutlined, TeamOutlined
} from '@ant-design/icons-vue'
import PageContainer from '../../components/PageContainer.vue'
import TrendChart from './center/TrendChart.vue'
import { getContractPage, getLeadPage, getMarketingConversionReport, getMarketingStaffActivity, getMarketingWorkbenchSummary, getVisitAppointmentPage, type MarketingStaffActivity } from '../../api/marketing'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { useUserStore } from '../../stores/user'
import { resolveRouteAccess } from '../../utils/routeAccess'
import { formatRate, funnelStages, growth, monthRange, recentMonths, sortTodos, type TodoItem } from './center/marketingMetrics'
import type { CrmContractItem, CrmLeadItem, CrmVisitAppointmentItem, MarketingConversionReport, MarketingWorkbenchSummary, PageResult } from '../../types'
import '../elder/residentUi.css'

const router = useRouter()
const user = useUserStore()
const silent = { silent403: true }
const loading = ref(false)
const errorMessage = ref('')
const summary = ref<MarketingWorkbenchSummary | null>(null)
const current = ref<MarketingConversionReport | null>(null)
const previous = ref<MarketingConversionReport | null>(null)
const monthVisits = ref<number | null>(null)
const trend = ref(recentMonths(6).map((m) => ({ ...m, leads: 0, contracts: 0 })))
const todos = ref<TodoItem[]>([])

const canOperate = computed(() => resolveRouteAccess(router, user.roles || [], '/marketing/operations', user.pagePermissions || []).canAccess)
const show = (value?: number | null) => (value == null ? '--' : Number(value))
const todoIcon = { followup: PhoneOutlined, visit: CalendarOutlined, contract: FileTextOutlined, reservation: HomeOutlined }

const kpis = computed(() => {
  const c = current.value, s = summary.value
  const change = growth(c?.totalLeads, previous.value?.totalLeads)
  const pending = s ? Number(s.followup?.todayDue || 0) + Number(s.followup?.overdue || 0) : null
  return [
    { key: 'lead', label: '本月新增线索', icon: TeamOutlined, value: show(c?.totalLeads), hint: change == null ? '较上月 --' : `较上月 ${change >= 0 ? '↑' : '↓'} ${Math.abs(change)}%`, path: '/marketing/customers' },
    { key: 'intent', label: '有效意向客户', icon: AimOutlined, value: show(c?.intentCount), hint: `有效率 ${formatRate(c?.intentCount, c?.totalLeads)}`, path: '/marketing/customers', query: { tab: 'intent' } },
    { key: 'visit', label: '本月实际到访', icon: CalendarOutlined, value: show(monthVisits.value), hint: `意向到访率 ${formatRate(monthVisits.value, c?.intentCount)}`, path: '/marketing/visits' },
    { key: 'reservation', label: '床位预订', icon: HomeOutlined, value: show(c?.reservationCount), hint: '本月完成预订', path: '/marketing/visits', query: { tab: 'reservation' } },
    { key: 'contract', label: '已签约', icon: FileTextOutlined, value: show(c?.contractCount), hint: `线索签约率 ${formatRate(c?.contractCount, c?.totalLeads)}`, path: '/marketing/contracts' },
    { key: 'todo', label: '待处理任务', icon: AlertOutlined, tone: 'warning', value: show(pending), hint: `其中超时 ${show(s?.followup?.overdue)} 项`, path: '/marketing/customers', query: { tab: 'followup', view: 'overdue' } }
  ]
})

const stages = computed(() => funnelStages({ ...current.value, visitCount: monthVisits.value ?? 0 }))
const trendSeries = computed(() => [
  { name: '新增线索', color: '#21705f', values: trend.value.map((m) => m.leads) },
  { name: '合同签约', color: '#6b9bd2', values: trend.value.map((m) => m.contracts) }
])

const quickActions = computed(() => [
  { label: '新增线索', icon: PlusOutlined, path: '/marketing/customers', query: { action: 'create' } },
  { label: '记录回访', icon: PhoneOutlined, path: '/marketing/customers', query: { tab: 'followup', view: 'today' } },
  { label: '预约参观', icon: CalendarOutlined, path: '/marketing/visits', query: { action: 'create' } },
  { label: '申请预订', icon: HomeOutlined, path: '/marketing/visits', query: { tab: 'reservation' } },
  { label: '合同办理', icon: FileTextOutlined, path: '/marketing/contracts' },
  ...(canOperate.value ? [{ label: '经营分析', icon: BarChartOutlined, path: '/marketing/operations' }] : [])
])

function go(path: string, query?: Record<string, string>) {
  router.push({ path, query })
}

function todoTime(time?: string) {
  if (!time) return ''
  const t = dayjs(time)
  if (!t.isValid()) return time
  return t.isSame(dayjs(), 'day') ? (time.length > 10 ? t.format('HH:mm') : '今天') : t.format('MM-DD')
}

const customerName = (lead: Partial<CrmLeadItem>) => lead.elderName || lead.name || lead.consultantName || '未命名客户'
const list = <T,>(result: PromiseSettledResult<unknown>): T[] => (result.status === 'fulfilled' ? ((result.value as PageResult<T>)?.list || []) : [])

function buildTodos(due: CrmLeadItem[], visits: CrmVisitAppointmentItem[], contracts: CrmContractItem[], expiring: CrmLeadItem[]) {
  const today = dayjs().format('YYYY-MM-DD')
  return sortTodos([
    ...due.map((lead): TodoItem => ({
      key: `f-${lead.id}`, kind: 'followup', title: `${customerName(lead)} · 回访跟进`,
      desc: [lead.followupStatus, lead.ownerStaffName || lead.marketerName ? `负责人：${lead.ownerStaffName || lead.marketerName}` : ''].filter(Boolean).join(' · ') || '按计划联系客户',
      time: lead.nextFollowDate, overdue: Boolean(lead.nextFollowDate && lead.nextFollowDate < today),
      actionLabel: '去跟进', path: '/marketing/customers', query: { leadId: String(lead.id), drawer: 'followup' }
    })),
    ...visits.filter((v) => v.status === 'SCHEDULED' || v.status === 'ARRIVED').map((v): TodoItem => ({
      key: `v-${v.id}`, kind: 'visit', title: `${v.elderName || v.leadName || v.visitorName} · ${v.status === 'ARRIVED' ? '填写参观结果' : '参观接待'}`,
      desc: `${v.visitorName}${v.partySize ? ` 等 ${v.partySize} 人` : ''}${v.ownerStaffName ? ` · 接待：${v.ownerStaffName}` : ''}`,
      time: dayjs(v.appointmentTime).format('YYYY-MM-DD HH:mm'), overdue: v.status === 'SCHEDULED' && dayjs(v.appointmentTime).isBefore(dayjs()),
      actionLabel: '查看预约', path: '/marketing/visits', query: { leadId: String(v.leadId) }
    })),
    ...contracts.map((c): TodoItem => ({
      key: `c-${c.id}`, kind: 'contract', title: `${c.elderName || c.name || '客户'} · 合同待签署`,
      desc: [c.contractNo ? `合同号 ${c.contractNo}` : '', c.marketerName ? `负责人：${c.marketerName}` : ''].filter(Boolean).join(' · '),
      overdue: false, actionLabel: '去办理', path: '/marketing/contracts', query: { contractId: String(c.id) }
    })),
    ...expiring.map((lead): TodoItem => ({
      key: `r-${lead.id}`, kind: 'reservation', title: `${customerName(lead)} · 预订即将到期`,
      desc: lead.reservationRoomNo ? `预订床位 ${lead.reservationRoomNo}` : '核对预订有效期',
      overdue: false, actionLabel: '去处理', path: '/marketing/customers', query: { leadId: String(lead.id) }
    }))
  ])
}

async function load() {
  if (loading.value) return
  loading.value = true
  errorMessage.value = ''
  const now = dayjs()
  const thisMonth = monthRange(now)
  const lastMonth = monthRange(now.subtract(1, 'month'))
  const today = now.format('YYYY-MM-DD')
  const months = recentMonths(6, now)
  try {
    const [sum, cur, prev, visitsMonth, due, visitsToday, contracts, expiring, ...trendResults] = await Promise.allSettled([
      getMarketingWorkbenchSummary({ dateFrom: thisMonth.dateFrom, dateTo: today }),
      getMarketingConversionReport({ dateFrom: thisMonth.dateFrom, dateTo: thisMonth.dateTo }, silent),
      getMarketingConversionReport({ dateFrom: lastMonth.dateFrom, dateTo: lastMonth.dateTo }, silent),
      getMarketingStaffActivity({ dateFrom: thisMonth.dateFrom, dateTo: thisMonth.dateTo }, silent),
      getLeadPage({ pageNo: 1, pageSize: 20, followupDueOnly: true, followupDateTo: today }, silent),
      getVisitAppointmentPage({ pageNo: 1, pageSize: 20, dateFrom: today, dateTo: today }, silent),
      getContractPage({ pageNo: 1, pageSize: 5, flowStage: 'PENDING_SIGN' }, silent),
      getLeadPage({ pageNo: 1, pageSize: 5, mode: 'reservation', scenario: 'expiring_lock' }, silent),
      ...months.map((m) => getMarketingConversionReport({ dateFrom: m.dateFrom, dateTo: m.dateTo }, silent))
    ])
    if (sum.status === 'rejected') throw sum.reason
    summary.value = sum.value as unknown as MarketingWorkbenchSummary
    current.value = cur.status === 'fulfilled' ? cur.value as unknown as MarketingConversionReport : null
    previous.value = prev.status === 'fulfilled' ? prev.value as unknown as MarketingConversionReport : null
    // 到访按去重客户计，同一客户多次到访只算一次
    monthVisits.value = visitsMonth.status === 'fulfilled' ? Number((visitsMonth.value as unknown as MarketingStaffActivity).arrivedLeadCount || 0) : null
    trend.value = months.map((m, i) => {
      const r = trendResults[i]
      const report = r.status === 'fulfilled' ? r.value as unknown as MarketingConversionReport : null
      return { ...m, leads: Number(report?.totalLeads || 0), contracts: Number(report?.contractCount || 0) }
    })
    todos.value = buildTodos(list(due), list(visitsToday), list(contracts), list(expiring))
  } catch (error: any) {
    summary.value = null
    errorMessage.value = Number(error?.response?.status) === 403 ? '当前账号无权查看市场工作台数据' : (error?.message || '市场工作台加载失败，请重试')
  } finally {
    loading.value = false
  }
}

onMounted(load)
useLiveSyncRefresh({ topics: ['marketing'], refresh: load, debounceMs: 900 })
</script>

<style scoped>
.mk-kpis { grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); }
.mk-col { display: flex; flex-direction: column; gap: 16px; min-width: 0; }
.mk-time { flex: none; }
.mk-avatar--danger { background: #fff1f0; color: var(--rm-danger); }
.mk-conv-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 8px; margin-bottom: 12px; }
.mk-conv-rate { font-size: 28px; color: var(--rm-green-dark); }
.mk-bar { display: grid; grid-template-columns: 64px 1fr 36px; align-items: center; gap: 10px; width: 100%; padding: 6px 0; border: 0; background: none; font: inherit; font-size: 13px; color: var(--rm-muted); cursor: pointer; text-align: left; }
.mk-bar strong { color: var(--rm-text); text-align: right; }
.mk-bar__track { height: 8px; border-radius: 4px; background: #eef2f0; overflow: hidden; }
.mk-bar__fill { display: block; height: 100%; border-radius: 4px; background: #3a9d7f; }
.mk-bar:hover .mk-bar__fill { background: var(--rm-green); }
.mk-foot { margin: 8px 0 0; }
.mk-note-link { margin-left: auto; flex: none; }
.rm-split { grid-template-columns: minmax(0, 1fr) 420px; }
@media (max-width: 1200px) { .rm-split { grid-template-columns: minmax(0, 1fr); } }
</style>
