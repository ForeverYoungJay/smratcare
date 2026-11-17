<template>
  <PageContainer class="rm-page" title="护理工作台" :sub-title="`业务日期 ${today} · 显示当前账号授权范围内的长者与任务`">
    <template #extra>
      <a-space wrap>
        <a-button type="primary" @click="go('/care/tasks?tab=today')">查看今日任务</a-button>
        <a-button @click="go('/care/safety?tab=incident&action=create')">上报异常</a-button>
      </a-space>
    </template>

    <div class="rm-kpis">
      <button v-for="card in kpis" :key="card.key" type="button" class="rm-kpi" @click="go(card.path)">
        <span class="rm-kpi__icon" :class="card.tone && `rm-kpi__icon--${card.tone}`"><component :is="card.icon" /></span>
        <span class="rm-kpi__label">{{ card.label }}</span>
        <span class="rm-kpi__value">{{ card.value ?? '--' }}<small>{{ card.unit }}</small></span>
        <span class="rm-kpi__hint">{{ card.hint }}</span>
      </button>
    </div>

    <div class="rm-split nw-grid">
      <section class="rm-card">
        <div class="rm-card__head">
          <span class="rm-card__title">今日重点待办 <small class="rm-card__sub">按超时、异常、计划时间排序</small></span>
          <a class="rm-link" @click="go('/care/tasks?tab=today')">全部任务 →</a>
        </div>
        <a-spin :spinning="loading">
          <div v-if="error" class="nw-empty">{{ error }} <a-button type="link" @click="load">重试</a-button></div>
          <div v-else-if="!todo.length" class="nw-empty">今天没有待办护理任务</div>
          <div v-for="task in todo" v-else :key="task.taskDailyId" class="rm-row">
            <span class="nw-time">{{ task.planTime ? dayjs(task.planTime).format('HH:mm') : '--' }}</span>
            <span class="rm-avatar">{{ (task.elderName || '长').slice(0, 1) }}</span>
            <div class="rm-row__main">
              <span class="rm-row__title">{{ task.elderName || '未命名长者' }}</span>
              <span class="rm-row__meta">{{ task.roomNo || '未分配房间' }} · {{ task.taskName }} · {{ task.staffName || '待分配' }}</span>
            </div>
            <span class="rm-pill" :class="statusTone(task)">{{ statusText(task) }}</span>
            <a-button size="small" @click="openTask(task)">处理</a-button>
          </div>
        </a-spin>
      </section>

      <div class="nw-side">
        <section class="rm-card">
          <div class="rm-card__head"><span class="rm-card__title">快捷办理</span></div>
          <div class="nw-quick">
            <button v-for="item in quick" :key="item.label" type="button" @click="go(item.path)"><component :is="item.icon" /><span>{{ item.label }}</span></button>
          </div>
        </section>
        <section class="rm-card">
          <div class="rm-card__head"><span class="rm-card__title">当班任务执行进度 <small class="rm-card__sub">按责任人</small></span></div>
          <div v-if="!staffProgress.length" class="nw-empty">暂无任务</div>
          <div v-for="row in staffProgress" :key="row.name" class="nw-bar">
            <span>{{ row.name }}</span>
            <a-progress :percent="row.rate" :show-info="false" stroke-color="#1f9d74" size="small" />
            <em>{{ row.done }}/{{ row.total }}</em>
          </div>
        </section>
      </div>
    </div>

    <div class="rm-split nw-grid">
      <section class="rm-card">
        <div class="rm-card__head"><span class="rm-card__title">分时护理任务完成趋势</span><span class="rm-card__sub">到各整点为止：已完成 / 应完成</span></div>
        <TrendChart :labels="trend.map((p) => p.label)" :series="[{ name: '完成率 %', color: '#1f9d74', values: trend.map((p) => p.rate) }]" />
      </section>
      <section class="rm-card">
        <div class="rm-card__head">
          <span class="rm-card__title">重点长者与风险提示</span>
          <a class="rm-link" @click="go('/care/residents')">查看护理档案 →</a>
        </div>
        <div v-if="!risks.length" class="nw-empty">当前没有超时、异常任务或高风险未闭环事件</div>
        <button v-for="item in risks" :key="item.key" type="button" class="rm-row nw-risk" @click="item.elderId ? go(`/care/residents/${item.elderId}`) : go('/care/safety?tab=incident')">
          <WarningOutlined class="nw-risk__icon" />
          <div class="rm-row__main"><span class="rm-row__title">{{ item.name }}</span><span class="rm-row__meta">{{ item.meta }}</span></div>
          <span class="rm-pill" :class="item.tone">{{ item.tag }}</span>
        </button>
      </section>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { AlertOutlined, CalendarOutlined, CheckCircleOutlined, ContainerOutlined, HeartOutlined, SwapOutlined, TeamOutlined, WarningOutlined } from '@ant-design/icons-vue'
import PageContainer from '../../../components/PageContainer.vue'
import TrendChart from '../../marketing/center/TrendChart.vue'
import { getTaskSummary, getTodayTasks } from '../../../api/care'
import { getElderPage } from '../../../api/elder'
import { getAssessmentRecordSummary } from '../../../api/assessment'
import { getIncidentPage, getIncidentSummary } from '../../../api/nursing'
import type { CareIncidentItem, CareIncidentSummary, CareTaskItem, CareTaskSummary } from '../../../types'
import { INCIDENT_TYPES, hourlyCompletion, progressByStaff, ratioPercent, sortTodo } from './nursingModel'
import '../../elder/residentUi.css'

const router = useRouter()
const today = dayjs().format('YYYY-MM-DD')
const loading = ref(false)
const error = ref('')
const tasks = ref<CareTaskItem[]>([])
const summary = ref<CareTaskSummary | null>(null)
const incidents = ref<CareIncidentSummary | null>(null)
const highIncidents = ref<CareIncidentItem[]>([])
const residentCount = ref<number>()
const reassessDue = ref<number>()

const todo = computed(() => sortTodo(tasks.value).slice(0, 6))
const staffProgress = computed(() => progressByStaff(tasks.value).slice(0, 6))
const trend = computed(() => hourlyCompletion(tasks.value))

const kpis = computed(() => [
  { key: 'resident', label: '在住长者', value: residentCount.value, unit: '人', hint: '可在护理档案中查看', icon: TeamOutlined, path: '/care/residents' },
  { key: 'tasks', label: '今日护理任务', value: summary.value?.totalCount, unit: '项', hint: `已完成 ${summary.value?.doneCount ?? '--'} 项`, icon: ContainerOutlined, path: '/care/tasks?tab=today' },
  { key: 'rate', label: '任务完成率', value: ratioPercent(summary.value?.completionRate), unit: '%', hint: `超时 ${summary.value?.overdueCount ?? '--'} 项`, icon: CheckCircleOutlined, path: '/care/tasks?tab=today&metric=overdue' },
  { key: 'incident', label: '待处理异常', value: incidents.value ? Number(incidents.value.processingCount) + Number(incidents.value.pendingReviewCount) : undefined, unit: '项', hint: `高风险未闭环 ${incidents.value?.openHighCount ?? '--'} 项`, icon: AlertOutlined, tone: 'danger', path: '/care/safety?tab=incident' },
  { key: 'reassess', label: '待复评长者', value: reassessDue.value, unit: '人', hint: '复评日期已到', icon: CalendarOutlined, tone: 'warning', path: '/care/residents?filter=reassess' }
])

const quick = [
  { label: '护理记录', icon: CheckCircleOutlined, path: '/care/tasks?tab=records' },
  { label: '交接班', icon: SwapOutlined, path: '/care/safety?tab=handover' },
  { label: '能力复评', icon: HeartOutlined, path: '/care/residents?filter=reassess' },
  { label: '异常上报', icon: WarningOutlined, path: '/care/safety?tab=incident&action=create' }
]

const risks = computed(() => [
  ...highIncidents.value.map((item) => ({ key: `i${item.id}`, elderId: item.elderId, name: item.elderName || item.location || item.incidentNo, meta: `${item.location || ''} · ${INCIDENT_TYPES[item.incidentType] || item.incidentType}`, tag: '高风险', tone: 'rm-pill--danger' })),
  ...tasks.value.filter((t) => t.overdueFlag || t.status === 'EXCEPTION').map((t) => ({ key: `t${t.taskDailyId}`, elderId: t.elderId, name: t.elderName || '未命名长者', meta: `${t.roomNo || ''} · ${t.taskName}`, tag: t.overdueFlag ? '任务超时' : '任务异常', tone: 'rm-pill--warning' }))
].slice(0, 5))

function statusText(task: CareTaskItem) { return task.overdueFlag ? '超时' : task.status === 'EXCEPTION' ? '异常' : '待执行' }
function statusTone(task: CareTaskItem) { return task.overdueFlag || task.status === 'EXCEPTION' ? 'rm-pill--danger' : 'rm-pill--warning' }
function go(path: string) { router.push(path) }
function openTask(task: CareTaskItem) { go(`/care/tasks?tab=today&elderId=${task.elderId}`) }

async function load() {
  loading.value = true
  error.value = ''
  // 各块数据互不依赖，一块失败不影响其它块
  const [page, sum, inc, high, elders, assess] = await Promise.allSettled([
    getTodayTasks({ pageNo: 1, pageSize: 200, date: today }),
    getTaskSummary({ dateFrom: today, dateTo: today }),
    getIncidentSummary(dayjs().format('YYYY-MM')),
    getIncidentPage({ pageNo: 1, pageSize: 20, severity: 'HIGH' }),
    getElderPage({ pageNo: 1, pageSize: 1, lifecycleStatus: 'IN_HOSPITAL' }),
    getAssessmentRecordSummary({})
  ])
  if (page.status === 'fulfilled') tasks.value = page.value.list || []
  else error.value = (page.reason as Error)?.message || '任务加载失败'
  summary.value = sum.status === 'fulfilled' ? sum.value : null
  incidents.value = inc.status === 'fulfilled' ? inc.value : null
  highIncidents.value = high.status === 'fulfilled' ? (high.value.list || []).filter((i) => i.status !== 'CLOSED') : []
  residentCount.value = elders.status === 'fulfilled' ? elders.value.total : undefined
  reassessDue.value = assess.status === 'fulfilled' ? assess.value.reassessOverdueCount : undefined
  loading.value = false
}

onMounted(load)
</script>

<style scoped>
.nw-grid { grid-template-columns: minmax(0, 1.6fr) minmax(300px, 1fr); margin-top: 16px; }
.nw-side { display: grid; gap: 16px; align-content: start; }
.nw-time { width: 44px; color: #6b7a74; font-variant-numeric: tabular-nums; }
.nw-empty { padding: 24px 8px; color: #7b877e; text-align: center; }
.nw-quick { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; }
.nw-quick button { display: grid; justify-items: center; gap: 6px; padding: 14px 4px; border: 1px solid #e3ebe6; border-radius: 10px; background: #fff; color: #1f6f55; font-size: 22px; cursor: pointer; }
.nw-quick button span { color: #33433a; font-size: 12px; }
.nw-quick button:hover { border-color: #1f9d74; background: #f3faf6; }
.nw-bar { display: grid; grid-template-columns: 72px 1fr 44px; align-items: center; gap: 8px; padding: 4px 0; font-size: 12px; }
.nw-bar em { color: #6b7a74; font-style: normal; text-align: right; }
.nw-risk { width: 100%; border: 0; background: none; text-align: left; cursor: pointer; }
.nw-risk__icon { color: #d9822b; font-size: 20px; }
@media (max-width: 1100px) { .nw-grid { grid-template-columns: 1fr; } }
</style>
