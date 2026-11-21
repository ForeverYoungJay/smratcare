<template>
  <PageContainer class="rm-page" title="医务工作台" :sub-title="`${today} · 医生、护士和医务主管按岗位查看的统一入口`">
    <template #extra>
      <a-space wrap>
        <a-button @click="go('/medical-care/clinical?tab=rounds')">今日巡诊</a-button>
        <a-button type="primary" @click="go('/medical-care/clinical?tab=rounds')">发起巡诊</a-button>
      </a-space>
    </template>

    <div class="rm-kpis">
      <button v-for="card in kpis" :key="card.key" type="button" class="rm-kpi" @click="go(card.path)">
        <span class="rm-kpi__icon" :class="card.tone && `rm-kpi__icon--${card.tone}`"><component :is="card.icon" /></span>
        <span class="rm-kpi__label">{{ card.label }}</span>
        <span class="rm-kpi__value">{{ summary ? card.value : '--' }}<small>{{ card.unit }}</small></span>
        <span class="rm-kpi__hint">{{ card.hint }}</span>
      </button>
    </div>

    <div class="rm-split mw-grid">
      <section class="rm-card">
        <div class="rm-card__head">
          <span class="rm-card__title">今日医护任务 <small class="rm-card__sub">统一任务中心 · 按计划时间</small></span>
          <a class="rm-link" @click="go('/medical-care/coordination?tab=tasks')">查看全部 →</a>
        </div>
        <a-radio-group v-model:value="module" size="small" class="mw-filter" @change="loadTasks">
          <a-radio-button v-for="item in MODULES" :key="item.value" :value="item.value">{{ item.label }}</a-radio-button>
        </a-radio-group>
        <a-spin :spinning="taskLoading">
          <div v-if="!tasks.length" class="mw-empty">暂无待办医护任务</div>
          <div v-for="task in tasks" :key="`${task.module}-${task.id}`" class="rm-row">
            <span class="mw-time">{{ task.plannedTime ? dayjs(task.plannedTime).format('HH:mm') : '--' }}</span>
            <span class="rm-avatar">{{ (task.residentName || '长').slice(0, 1) }}</span>
            <div class="rm-row__main">
              <span class="rm-row__title">{{ task.residentName || '未关联长者' }}</span>
              <span class="rm-row__meta">{{ task.taskTitle }}{{ task.assignee ? ` · ${task.assignee}` : '' }}</span>
            </div>
            <span class="rm-pill" :class="task.overdue ? 'rm-pill--danger' : task.priority === 'HIGH' ? 'rm-pill--warning' : 'rm-pill--info'">
              {{ task.overdue ? '已超时' : PRIORITY[task.priority] || '待执行' }}
            </span>
            <a class="rm-link" @click="go(task.suggestedRoute || '/medical-care/coordination?tab=tasks')">办理 →</a>
          </div>
          <p class="mw-muted">医嘱执行、巡诊、随访在各自业务页完成，工作台不重复创建记录。</p>
        </a-spin>
      </section>

      <section class="rm-card">
        <div class="rm-card__head">
          <span class="rm-card__title">重点风险关注</span>
          <a class="rm-link" @click="go('/medical-care/alerts')">进入告警中心 →</a>
        </div>
        <div v-if="!summary?.keyResidents?.length" class="mw-empty">当前没有重点风险长者</div>
        <button v-for="item in summary?.keyResidents || []" :key="String(item.elderId)" type="button" class="rm-row mw-risk"
          @click="go(`/medical-care/residents?residentId=${item.elderId}`)">
          <span class="mw-dot" :class="item.riskLevel === 'HIGH' || item.riskLevel === 'CRITICAL' ? 'mw-dot--danger' : ''" />
          <div class="rm-row__main">
            <span class="rm-row__title">{{ item.elderName || '未命名长者' }}</span>
            <span class="rm-row__meta">{{ item.keyRiskFactors || '风险因素待补充' }}{{ item.assessmentDate ? ` · ${item.assessmentDate}` : '' }}</span>
          </div>
          <span class="rm-link">查看</span>
        </button>
      </section>
    </div>

    <div class="rm-split mw-grid">
      <section class="rm-card">
        <div class="rm-card__head"><span class="rm-card__title">快捷业务入口</span></div>
        <div class="mw-quick">
          <button v-for="item in QUICK" :key="item.label" type="button" @click="go(item.path)"><component :is="item.icon" /><span>{{ item.label }}</span></button>
        </div>
      </section>
      <section class="rm-card">
        <div class="rm-card__head"><span class="rm-card__title">今日业务进展</span><a class="rm-link" @click="go('/medical-care/coordination?tab=quality')">查看协同质控 →</a></div>
        <div class="mw-progress">
          <div v-for="row in progress" :key="row.label" class="mw-bar">
            <span>{{ row.label }}</span>
            <a-progress :percent="row.total ? Math.round((row.done / row.total) * 100) : 0" :show-info="false" stroke-color="#1f9d74" size="small" />
            <em>{{ row.done }}/{{ row.total }}</em>
          </div>
        </div>
      </section>
    </div>

    <section v-if="trend.length" class="rm-card mw-trend">
      <div class="rm-card__head"><span class="rm-card__title">最近 7 日风险指数</span><span class="rm-card__sub">按待办、异常、超时综合计算</span></div>
      <TrendChart :labels="trend.map((p) => p.label)" :series="[{ name: '风险指数', color: '#1f9d74', values: trend.map((p) => p.value) }]" />
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { AlertOutlined, CalendarOutlined, ExperimentOutlined, FileTextOutlined, HeartOutlined, MedicineBoxOutlined, SolutionOutlined } from '@ant-design/icons-vue'
import PageContainer from '../../../components/PageContainer.vue'
import TrendChart from '../../marketing/center/TrendChart.vue'
import { getMedicalCareWorkbenchSummary, getMedicalRiskTimeline, getMedicalUnifiedTaskPage } from '../../../api/medicalCare'
import type { MedicalCareWorkbenchSummary, MedicalRiskTimelinePoint, MedicalUnifiedTaskItem } from '../../../types'
import '../../elder/residentUi.css'

const MODULES = [
  { value: '', label: '全部' }, { value: 'ORDER', label: '医嘱' }, { value: 'INSPECTION', label: '巡检' },
  { value: 'NURSING_LOG', label: '护理日志' }, { value: 'HANDOVER', label: '交接' }
]
const PRIORITY: Record<string, string> = { HIGH: '高优先', MEDIUM: '待执行', LOW: '待执行' }
const QUICK = [
  { label: '巡诊登记', icon: SolutionOutlined, path: '/medical-care/clinical?tab=rounds' },
  { label: '病历记录', icon: FileTextOutlined, path: '/medical-care/clinical?tab=emr' },
  { label: '医嘱执行', icon: MedicineBoxOutlined, path: '/medical-care/medication?tab=executions' },
  { label: '健康随访', icon: HeartOutlined, path: '/medical-care/monitoring?tab=followup' }
]

const router = useRouter()
const today = dayjs().format('YYYY年M月D日')
const summary = ref<MedicalCareWorkbenchSummary | null>(null)
const tasks = ref<MedicalUnifiedTaskItem[]>([])
const taskLoading = ref(false)
const module = ref('')
const timeline = ref<MedicalRiskTimelinePoint[]>([])

const n = (value: unknown) => Number(value ?? 0)
const kpis = computed(() => {
  const s = summary.value
  return [
    { key: 'inspection', label: '今日待巡检', value: n(s?.todayInspectionPendingCount), unit: '人次', hint: `今日计划 ${n(s?.todayInspectionPlanCount)}`, icon: ExperimentOutlined, path: '/medical-care/monitoring?tab=inspection' },
    { key: 'order', label: '待执行医嘱', value: n(s?.medicalOrderPendingCount), unit: '条', hint: `待审核 ${n(s?.pendingReviewCount)} 条`, icon: MedicineBoxOutlined, tone: 'info', path: '/medical-care/medication?tab=executions' },
    { key: 'followup', label: '待随访复查', value: n(s?.cvdNeedFollowupCount), unit: '人', hint: `心血管高风险 ${n(s?.cvdHighRiskCount)} 人`, icon: CalendarOutlined, path: '/medical-care/monitoring?tab=followup' },
    { key: 'risk', label: '需优先处置', value: n(s?.unclosedAbnormalCount), unit: '项', hint: `24 小时体征异常 ${n(s?.abnormalVital24hCount)} 次`, icon: AlertOutlined, tone: 'danger', path: '/medical-care/alerts' }
  ]
})
const progress = computed(() => {
  const s = summary.value
  return [
    { label: '医嘱执行', done: n(s?.medicalOrderDoneCount), total: n(s?.medicalOrderShouldCount) },
    { label: '用药登记', done: n(s?.medicationDoneCount), total: n(s?.medicationShouldCount) },
    { label: '健康巡检', done: n(s?.todayInspectionDoneCount), total: n(s?.todayInspectionPlanCount) },
    { label: '交接完成', done: n(s?.handoverDoneCount), total: n(s?.handoverDoneCount) + n(s?.handoverPendingCount) }
  ]
})
const trend = computed(() => timeline.value.map((p) => ({ label: dayjs(p.date).format('MM/DD'), value: n(p.riskScore) })))

function go(path: string) { router.push(path) }

async function loadTasks() {
  taskLoading.value = true
  try {
    const page = await getMedicalUnifiedTaskPage({ pageNo: 1, pageSize: 6, module: module.value || undefined, sortBy: 'PLANNED_TIME', sortDirection: 'ASC' })
    tasks.value = page.list || []
  } catch {
    tasks.value = []
  } finally {
    taskLoading.value = false
  }
}

onMounted(async () => {
  loadTasks()
  const quiet = { silent403: true }
  const [s, t] = await Promise.allSettled([getMedicalCareWorkbenchSummary({}, quiet), getMedicalRiskTimeline({ windowDays: 7 }, quiet)])
  summary.value = s.status === 'fulfilled' ? s.value : null
  timeline.value = t.status === 'fulfilled' ? t.value || [] : []
})
</script>

<style scoped>
.mw-grid { grid-template-columns: minmax(0, 1.6fr) minmax(300px, 1fr); margin-top: 16px; }
.mw-filter { margin-bottom: 10px; }
.mw-time { width: 44px; color: #6b7a74; font-variant-numeric: tabular-nums; }
.mw-empty { padding: 24px 8px; color: #7b877e; text-align: center; }
.mw-muted { margin: 10px 0 0; color: #8a968f; font-size: 12px; }
.mw-risk { width: 100%; border: 0; background: none; text-align: left; cursor: pointer; }
.mw-dot { flex: 0 0 8px; height: 8px; border-radius: 50%; background: #d9a441; }
.mw-dot--danger { background: #d9534f; }
.mw-quick { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
.mw-quick button { display: grid; justify-items: center; gap: 8px; padding: 18px 4px; border: 1px solid #e3ebe6; border-radius: 10px; background: #fff; color: #1f6f55; font-size: 22px; cursor: pointer; }
.mw-quick button span { color: #33433a; font-size: 13px; }
.mw-quick button:hover { border-color: #1f9d74; background: #f3faf6; }
.mw-progress { display: grid; gap: 10px; }
.mw-bar { display: grid; grid-template-columns: 72px 1fr 52px; align-items: center; gap: 8px; font-size: 13px; }
.mw-bar em { color: #6b7a74; font-style: normal; text-align: right; }
.mw-trend { margin-top: 16px; }
@media (max-width: 1100px) { .mw-grid { grid-template-columns: 1fr; } }
</style>
