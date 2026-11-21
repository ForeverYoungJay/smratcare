<template>
  <PageContainer class="rm-page" title="消防安全工作台" :sub-title="`${today} · 全院消防态势、今日待办与风险预警`">
    <template #extra>
      <a-space wrap>
        <a-button @click="go('/fire/inspection?tab=day')">新增巡查记录</a-button>
        <a-button danger @click="guideOpen = true">火警应急指引</a-button>
      </a-space>
    </template>

    <a-alert class="fw-alert" type="success" show-icon message="预防为主 · 防消结合 · 全员履责"
      description="火灾报警以现场核实和消防主机信息为准；本系统未接入消防主机，不显示实时火警，也不会自动拨打 119。" />

    <div class="rm-kpis">
      <button v-for="card in kpis" :key="card.key" type="button" class="rm-kpi" @click="go(card.path)">
        <span class="rm-kpi__icon" :class="card.tone && `rm-kpi__icon--${card.tone}`"><component :is="card.icon" /></span>
        <span class="rm-kpi__label">{{ card.label }}</span>
        <span class="rm-kpi__value">{{ loaded ? card.value : '--' }}<small>{{ card.unit }}</small></span>
        <span class="rm-kpi__hint">{{ card.hint }}</span>
      </button>
    </div>

    <div class="rm-split fw-grid">
      <section class="rm-card">
        <div class="rm-card__head">
          <span class="rm-card__title">待整改隐患 <small class="rm-card__sub">按检查时间，最新在前</small></span>
          <a class="rm-link" @click="go('/fire/safety?tab=hazards')">全部隐患 →</a>
        </div>
        <div v-if="!hazards.length" class="fw-empty">{{ loaded ? '当前没有未关闭的隐患' : '暂无数据' }}</div>
        <button v-for="item in hazards" :key="item.id" type="button" class="rm-row fw-row" @click="go(RECORD_TYPE_PATH[item.recordType])">
          <span class="fw-dot" :class="overdue(item) ? 'fw-dot--danger' : 'fw-dot--warning'" />
          <div class="rm-row__main">
            <span class="rm-row__title">{{ item.location || item.title }} · {{ item.issueDescription }}</span>
            <span class="rm-row__meta">{{ RECORD_TYPE_LABEL[item.recordType] }} · {{ item.inspectorName }} · {{ item.checkTime ? dayjs(item.checkTime).format('MM-DD HH:mm') : '' }}</span>
          </div>
          <span class="rm-pill" :class="overdue(item) ? 'rm-pill--danger' : 'rm-pill--warning'">{{ overdue(item) ? '已逾期' : item.status === 'RUNNING' ? '整改中' : '待整改' }}</span>
        </button>
      </section>

      <section class="rm-card">
        <div class="rm-card__head"><span class="rm-card__title">楼栋风险概览</span><a v-if="manager" class="rm-link" @click="go('/fire/settings')">维护点位 →</a></div>
        <div v-if="!buildings.length" class="fw-empty">尚未配置楼栋点位{{ manager ? '，请在消防系统配置中维护' : '' }}</div>
        <div class="fw-buildings">
          <div v-for="b in buildings" :key="b.building" class="fw-building" :class="`fw-building--${b.level}`">
            <b>{{ b.building }}</b>
            <span>{{ b.points }} 个点位 · 重点部位 {{ b.keyParts }}</span>
            <span>{{ b.openHazards ? `${b.openHazards} 项隐患处理中${b.overdue ? `，${b.overdue} 项逾期` : ''}` : '无未关闭隐患' }}</span>
          </div>
        </div>
      </section>
    </div>

    <div class="rm-split fw-grid">
      <section class="rm-card">
        <div class="rm-card__head"><span class="rm-card__title">近 7 日巡查与隐患趋势</span></div>
        <TrendChart v-if="trend.length" :labels="trend.map((p) => p.label)" :series="[
          { name: '巡查/检查记录', color: '#1f9d74', values: trend.map((p) => p.patrols) },
          { name: '新登记问题', color: '#e09a3e', values: trend.map((p) => p.issues) }]" />
      </section>
      <section class="rm-card fw-dist">
        <div class="rm-card__head"><span class="rm-card__title">记录分布 <small class="rm-card__sub">本月</small></span><a class="rm-link" @click="go('/fire/safety?tab=analytics')">统计报表 →</a></div>
        <div class="fw-bars">
          <div v-for="row in typeRows" :key="row.type" class="fw-bar">
            <span>{{ RECORD_TYPE_LABEL[row.type] }}</span>
            <a-progress :percent="row.percent" :show-info="false" stroke-color="#1f9d74" size="small" />
            <em>{{ row.count }}</em>
          </div>
        </div>
      </section>
      <section class="rm-card">
        <div class="rm-card__head"><span class="rm-card__title">常用快捷入口</span></div>
        <div class="fw-quick">
          <button v-for="item in QUICK" :key="item.label" type="button" @click="go(item.path)"><component :is="item.icon" /><span>{{ item.label }}</span></button>
        </div>
      </section>
    </div>

    <a-modal v-model:open="guideOpen" title="火警应急指引" :footer="null" width="560px">
      <a-alert type="error" show-icon message="确认发生火灾：立即拨打 119，并按预案组织灭火和疏散。" description="本页面只提供步骤参考，不会自动拨打电话，也不能代替现场应急行动。" />
      <ol class="fw-steps">
        <li v-for="step in EMERGENCY_STEPS" :key="step.title"><b>{{ step.title }}</b><span>{{ step.text }}</span></li>
      </ol>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { AlertOutlined, AuditOutlined, CalendarOutlined, ClockCircleOutlined, FileSearchOutlined, SafetyOutlined, ScheduleOutlined, TeamOutlined, ToolOutlined } from '@ant-design/icons-vue'
import PageContainer from '../../../components/PageContainer.vue'
import TrendChart from '../../marketing/center/TrendChart.vue'
import { getFireSafetyRecordPage, getFireSafetySummary } from '../../../api/fire'
import { getFireBuildingRisk, getFireHazardStats, type FireBuildingRisk, type FireHazardStats } from '../../../api/fireOps'
import { useUserStore } from '../../../stores/user'
import type { FireSafetyRecord, FireSafetyRecordType, FireSafetyReportSummary } from '../../../types'
import { EMERGENCY_STEPS, RECORD_TYPE_LABEL, RECORD_TYPE_PATH, canCloseHazard, dailyTrend } from './fireModel'
import '../../elder/residentUi.css'

const QUICK = [
  { label: '日间巡查', icon: ScheduleOutlined, path: '/fire/inspection?tab=day' },
  { label: '夜间巡查', icon: ClockCircleOutlined, path: '/fire/inspection?tab=night' },
  { label: '每月防火检查', icon: CalendarOutlined, path: '/fire/inspection?tab=monthly' },
  { label: '设施报修', icon: ToolOutlined, path: '/fire/facilities?tab=faults' },
  { label: '值班交接', icon: TeamOutlined, path: '/fire/control-center?tab=duty' },
  { label: '隐患整改', icon: AlertOutlined, path: '/fire/safety?tab=hazards' }
]

const router = useRouter()
const manager = canCloseHazard(useUserStore().roles || [])
const buildings = ref<FireBuildingRisk[]>([])
const hazardStats = ref<FireHazardStats | null>(null)
const today = dayjs().format('YYYY年M月D日')
const loaded = ref(false)
const guideOpen = ref(false)
const todaySummary = ref<FireSafetyReportSummary | null>(null)
const monthSummary = ref<FireSafetyReportSummary | null>(null)
const faultOpen = ref(0)
const hazardTotal = ref(0)
const hazards = ref<FireSafetyRecord[]>([])
const recent = ref<FireSafetyRecord[]>([])

const n = (value: unknown) => Number(value ?? 0)
const typeCount = (summary: FireSafetyReportSummary | null, type: FireSafetyRecordType) =>
  n(summary?.typeStats?.find((item) => item.recordType === type)?.count)
const overdue = (item: FireSafetyRecord) => Boolean(item.nextCheckDate && dayjs(item.nextCheckDate).isBefore(dayjs(), 'day'))

const kpis = computed(() => {
  const t = todaySummary.value
  const m = monthSummary.value
  const patrolsToday = typeCount(t, 'DAY_PATROL') + typeCount(t, 'NIGHT_PATROL')
  return [
    { key: 'hazard', label: '待整改隐患', value: hazardTotal.value, unit: '项', hint: `其中逾期 ${n(hazardStats.value?.overdue)} 项`, icon: AlertOutlined, tone: 'danger', path: '/fire/safety?tab=hazards' },
    { key: 'patrol', label: '今日巡查记录', value: patrolsToday, unit: '条', hint: `日间扫码完成 ${n(t?.dailyCompletedCount)} 条`, icon: FileSearchOutlined, tone: 'info', path: '/fire/inspection?tab=day' },
    { key: 'fault', label: '设施故障未关闭', value: faultOpen.value, unit: '项', hint: '关闭前须写明修复与复核', icon: ToolOutlined, tone: 'warning', path: '/fire/facilities?tab=faults' },
    { key: 'duty', label: '今日值班记录', value: n(t?.dutyRecordCount), unit: '条', hint: `交接打卡 ${n(t?.handoverPunchCount)} 次`, icon: TeamOutlined, path: '/fire/control-center?tab=duty' },
    { key: 'monthly', label: '本月防火检查', value: typeCount(m, 'MONTHLY_CHECK'), unit: '次', hint: `扫码完成 ${n(m?.monthlyCompletedCount)} 次`, icon: AuditOutlined, path: '/fire/inspection?tab=monthly' },
    { key: 'expiring', label: '器材 30 天内到期', value: n(m?.expiringSoonCount), unit: '项', hint: `下次检查临近 ${n(m?.nextCheckDueSoonCount)} 项`, icon: SafetyOutlined, tone: 'warning', path: '/fire/facilities?tab=assets' }
  ]
})
const typeRows = computed(() => {
  const stats = monthSummary.value?.typeStats || []
  const max = Math.max(1, ...stats.map((s) => n(s.count)))
  return stats.map((s) => ({ type: s.recordType, count: n(s.count), percent: Math.round((n(s.count) / max) * 100) }))
})
const trend = computed(() => dailyTrend(recent.value, 7))

function go(path: string) { router.push(path) }

onMounted(async () => {
  const fmt = 'YYYY-MM-DDTHH:mm:ss'
  const [t, m, f, h, r, b, st] = await Promise.allSettled([
    getFireSafetySummary({ checkTimeStart: dayjs().startOf('day').format(fmt), checkTimeEnd: dayjs().endOf('day').format(fmt) }),
    getFireSafetySummary({ checkTimeStart: dayjs().startOf('month').format(fmt), checkTimeEnd: dayjs().endOf('day').format(fmt) }),
    getFireSafetySummary({ recordType: 'FAULT_MAINTENANCE' }),
    getFireSafetyRecordPage({ pageNo: 1, pageSize: 6, status: 'ACTIVE', issueOnly: true }),
    getFireSafetyRecordPage({ pageNo: 1, pageSize: 500, checkTimeStart: dayjs().subtract(6, 'day').startOf('day').format(fmt) }),
    getFireBuildingRisk(),
    getFireHazardStats()
  ])
  buildings.value = b.status === 'fulfilled' ? b.value || [] : []
  hazardStats.value = st.status === 'fulfilled' ? st.value : null
  todaySummary.value = t.status === 'fulfilled' ? t.value : null
  monthSummary.value = m.status === 'fulfilled' ? m.value : null
  faultOpen.value = f.status === 'fulfilled' ? n(f.value?.openCount) : 0
  hazards.value = h.status === 'fulfilled' ? h.value.list || [] : []
  hazardTotal.value = h.status === 'fulfilled' ? n(h.value.total) : 0
  recent.value = r.status === 'fulfilled' ? r.value.list || [] : []
  loaded.value = true
})
</script>

<style scoped>
.fw-alert { margin-bottom: 14px; }
.fw-grid { grid-template-columns: minmax(0, 1.6fr) minmax(300px, 1fr); margin-top: 16px; }
.fw-empty { padding: 24px 8px; color: #7b877e; text-align: center; }
.fw-row { width: 100%; border: 0; background: none; text-align: left; cursor: pointer; }
.fw-dot { flex: 0 0 8px; height: 8px; border-radius: 50%; }
.fw-dot--warning { background: #d9a441; }
.fw-dot--danger { background: #d9534f; }
.fw-bars { display: grid; gap: 10px; }
.fw-bar { display: grid; grid-template-columns: 80px 1fr 40px; align-items: center; gap: 8px; font-size: 13px; }
.fw-bar em { color: #6b7a74; font-style: normal; text-align: right; }
.fw-dist { margin-bottom: 16px; }
.fw-buildings { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
.fw-building { display: grid; gap: 4px; padding: 12px; border: 1px solid #d9eee3; border-radius: 10px; background: #f1faf5; font-size: 12px; color: #5d6b63; }
.fw-building b { color: #22352c; font-size: 14px; }
.fw-building--WARNING { border-color: #f3dfbd; background: #fff8ee; }
.fw-building--DANGER { border-color: #f3c9c4; background: #fff3f2; }
.fw-quick { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.fw-quick button { display: grid; justify-items: center; gap: 8px; padding: 16px 4px; border: 1px solid #e3ebe6; border-radius: 10px; background: #fff; color: #1f6f55; font-size: 22px; cursor: pointer; }
.fw-quick button span { color: #33433a; font-size: 13px; }
.fw-quick button:hover { border-color: #1f9d74; background: #f3faf6; }
.fw-steps { display: grid; gap: 10px; margin: 14px 0 0; padding-left: 20px; }
.fw-steps li span { display: block; color: #6b7a74; font-size: 13px; }
@media (max-width: 1100px) { .fw-grid { grid-template-columns: 1fr; } }
</style>
