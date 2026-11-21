<template>
  <PageContainer
    class="rm-page"
    kicker="RESIDENT MANAGEMENT / 01"
    title="长者工作台"
    subTitle="全院在住动态、待办事项和一键办理入口"
  >
    <template #extra>
      <a-space wrap>
        <span v-if="lastLoadedAt" class="rm-card__sub">更新于 {{ lastLoadedAt }}</span>
        <a-button :loading="loading" @click="loadAll">刷新</a-button>
        <a-button type="primary" @click="go('/elder/create')"><PlusOutlined />新增长者</a-button>
      </a-space>
    </template>

    <div class="rm-kpis">
      <button type="button" class="rm-kpi" @click="go('/elder/list?lifecycleStatus=IN_HOSPITAL')">
        <span class="rm-kpi__icon"><TeamOutlined /></span>
        <span class="rm-kpi__label">当前在住</span>
        <span class="rm-kpi__value">{{ show(summary?.inHospitalCount) }}<small>人</small></span>
        <span class="rm-kpi__hint">今日入住 {{ show(admissionSummary?.todayAdmissionCount) }} 人</span>
      </button>
      <button type="button" class="rm-kpi" @click="go('/elder/admission-processing')">
        <span class="rm-kpi__icon rm-kpi__icon--warning"><SolutionOutlined /></span>
        <span class="rm-kpi__label">待办理入住</span>
        <span class="rm-kpi__value">{{ show(admissionContractTotal) }}<small>人</small></span>
        <span class="rm-kpi__hint">合同已到选床入住环节</span>
      </button>
      <button type="button" class="rm-kpi" @click="go('/elder/bed-panorama')">
        <span class="rm-kpi__icon rm-kpi__icon--info"><HomeOutlined /></span>
        <span class="rm-kpi__label">可用空床</span>
        <span class="rm-kpi__value">{{ beds ? idleBeds : '--' }}<small>张</small></span>
        <span class="rm-kpi__hint">共 {{ beds ? beds.length : '--' }} 张床位</span>
      </button>
      <div class="rm-kpi">
        <span class="rm-kpi__icon rm-kpi__icon--danger"><AlertOutlined /></span>
        <span class="rm-kpi__label">待处理提醒</span>
        <span class="rm-kpi__value">{{ todosFailed ? '--' : todos.length }}<small>项</small></span>
        <span class="rm-kpi__hint">其中紧急 {{ urgentCount }} 项</span>
      </div>
    </div>

    <div class="rm-split">
      <section class="rm-card">
        <div class="rm-card__head">
          <h3 class="rm-card__title">今日重点待办</h3>
          <a-radio-group v-model:value="todoScope" size="small" button-style="solid" class="rm-card__extra">
            <a-radio-button value="mine">我负责的 {{ mineCount }}</a-radio-button>
            <a-radio-button value="all">全部 {{ allTodos.length }}</a-radio-button>
          </a-radio-group>
        </div>
        <a-spin :spinning="loading">
          <div v-for="item in todos.slice(0, TODO_LIMIT)" :key="item.id" class="rm-row">
            <span :class="['todo-icon', `todo-icon--${TODO_LEVEL_META[item.level].tone}`]"><AlertOutlined /></span>
            <div class="rm-row__main">
              <span class="rm-row__title">{{ item.elderName || '--' }} · {{ item.title }}</span>
              <span class="rm-row__meta">{{ todoMeta(item) }}</span>
            </div>
            <span :class="['rm-pill', `rm-pill--${TODO_LEVEL_META[item.level].tone}`]">{{ TODO_LEVEL_META[item.level].text }}</span>
            <button type="button" class="rm-link" @click="go(item.path)">去办理 →</button>
          </div>
          <a-empty v-if="!todos.length" :description="todosFailed ? '待办加载失败，请刷新重试' : '暂无待办'" />
          <div v-if="todos.length > TODO_LIMIT" class="rm-card__sub todo-more">另有 {{ todos.length - TODO_LIMIT }} 项，请进入对应页面查看</div>
        </a-spin>
      </section>

      <section class="rm-card">
        <div class="rm-card__head">
          <h3 class="rm-card__title">常用快捷操作</h3>
        </div>
        <div class="rm-actions">
          <button v-for="action in quickActions" :key="action.label" type="button" class="rm-action" @click="go(action.path)">
            <component :is="action.icon" />{{ action.label }}
          </button>
        </div>
        <h4 class="rm-section-title status-title">长者在院状态</h4>
        <div v-for="row in statusRows" :key="row.label" class="status-row" @click="go(row.path)">
          <span :class="['status-dot', `status-dot--${row.tone}`]" />
          <span class="status-label">{{ row.label }}</span>
          <strong>{{ show(row.value) }} 人</strong>
        </div>
      </section>
    </div>

    <div class="rm-split rm-split--even">
      <section class="rm-card">
        <div class="rm-card__head">
          <h3 class="rm-card__title">楼栋入住情况</h3>
          <button type="button" class="rm-link rm-card__extra" @click="go('/elder/bed-panorama')">进入床位管理 →</button>
        </div>
        <div v-for="row in buildings" :key="row.building" class="building-row">
          <div class="building-row__head">
            <span><strong>{{ row.building }}</strong> <span class="rm-card__sub">{{ row.occupied }} / {{ row.total }} 床</span></span>
            <strong class="building-row__rate">{{ row.rate }}%</strong>
          </div>
          <a-progress :percent="row.rate" :show-info="false" stroke-color="#21705f" />
        </div>
        <a-empty v-if="!buildings.length" :description="beds ? '暂无床位数据' : '床位数据不可用'" />
      </section>

      <section class="rm-card">
        <div class="rm-card__head">
          <h3 class="rm-card__title">最近办理记录</h3>
          <button type="button" class="rm-link rm-card__extra" @click="go('/elder/change-log')">查看全部 →</button>
        </div>
        <a-timeline v-if="changeLogs.length">
          <a-timeline-item v-for="log in changeLogs" :key="log.id" color="#21705f">
            <div class="log-row">
              <span class="rm-row__title">{{ log.elderName || '--' }} · {{ changeTypeText(log.changeType) }}</span>
              <span class="rm-card__sub">{{ formatChineseDateTime(log.createTime, '--') }}</span>
            </div>
            <div class="rm-row__meta">{{ changeDetail(log) }}</div>
          </a-timeline-item>
        </a-timeline>
        <a-empty v-else description="暂无办理记录" />
      </section>
    </div>

    <div class="rm-note">数据与护理、医务、财务部门共享，按角色显示可操作范围；无权限的模块不显示数据。</div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  AlertOutlined,
  AuditOutlined,
  HomeOutlined,
  LogoutOutlined,
  PlusOutlined,
  SolutionOutlined,
  SwapOutlined,
  TeamOutlined
} from '@ant-design/icons-vue'
import PageContainer from '../../../components/PageContainer.vue'
import { getElderWorkbenchTodos, getResidenceStatusSummary } from '../../../api/elderResidence'
import { getAdmissionRecordSummary, getChangeLogs } from '../../../api/elderLifecycle'
import { getBedMap } from '../../../api/bed'
import { getContractPage } from '../../../api/marketing'
import { useLiveSyncRefresh } from '../../../composables/useLiveSyncRefresh'
import { formatChineseDateTime } from '../../../utils/dateLocale'
import type {
  AdmissionRecordSummary,
  BedItem,
  ChangeLogItem,
  ElderWorkbenchTodoItem,
  ResidenceStatusSummary
} from '../../../types'
import {
  TODO_LEVEL_META,
  buildingOccupancy,
  defaultTodoScope,
  idleBedCount,
  todosInScope,
  type TodoScope
} from './residentWorkbenchModel'
import '../residentUi.css'

const TODO_LIMIT = 8

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const lastLoadedAt = ref('')
const summary = ref<ResidenceStatusSummary>()
const admissionSummary = ref<AdmissionRecordSummary>()
const beds = ref<BedItem[]>()
const changeLogs = ref<ChangeLogItem[]>([])
const allTodos = ref<ElderWorkbenchTodoItem[]>([])
const todosFailed = ref(false)
const todoScope = ref<TodoScope>()
const admissionContractTotal = ref<number>()

const todos = computed(() => todosInScope(allTodos.value, todoScope.value || 'all'))
const mineCount = computed(() => allTodos.value.filter((item) => item.mine).length)
const urgentCount = computed(() => todos.value.filter((item) => item.level === 'urgent').length)
const idleBeds = computed(() => idleBedCount(beds.value || []))
const buildings = computed(() => buildingOccupancy(beds.value || []))

const quickActions = [
  { label: '入住办理', icon: SolutionOutlined, path: '/elder/admission-processing' },
  { label: '调床', icon: SwapOutlined, path: '/elder/bed-panorama' },
  { label: '请假/住院', icon: LogoutOutlined, path: '/elder/bed-panorama' },
  { label: '费用/退住', icon: AuditOutlined, path: '/elder/status-change/discharge-settlement' }
]

const statusRows = computed(() => [
  { label: '在院', value: summary.value?.inHospitalCount, tone: 'success', path: '/elder/list?lifecycleStatus=IN_HOSPITAL' },
  { label: '外出请假', value: summary.value?.outingCount, tone: 'warning', path: '/elder/list?lifecycleStatus=OUTING' },
  { label: '外出就医', value: summary.value?.medicalOutingCount, tone: 'info', path: '/elder/list?lifecycleStatus=MEDICAL_OUTING' },
  { label: '待退住', value: summary.value?.dischargePendingCount, tone: 'danger', path: '/elder/list?lifecycleStatus=DISCHARGE_PENDING' },
  { label: '试住', value: summary.value?.trialCount, tone: 'muted', path: '/elder/list?lifecycleStatus=TRIAL' }
])

const CHANGE_TYPE_TEXT: Record<string, string> = {
  ADMISSION: '入住办理',
  DISCHARGE: '退住办理',
  BED_CHANGE: '床位变更',
  STATUS_CHANGE: '状态变更',
  ADMISSION_DATE_CHANGE: '入院日期变更',
  CARE_LEVEL_CHANGE: '护理等级变更',
  SOURCE_TYPE_CHANGE: '档案来源变更',
  HISTORICAL_CONTRACT_FILE_CHANGE: '历史合同附件变更'
}

function changeTypeText(value?: string) {
  return CHANGE_TYPE_TEXT[value || ''] || value || '--'
}

function changeDetail(log: ChangeLogItem) {
  const diff = log.beforeValue || log.afterValue ? `${log.beforeValue || '--'} → ${log.afterValue || '--'}` : ''
  return [diff, log.reason].filter(Boolean).join(' · ') || '--'
}

function todoMeta(item: ElderWorkbenchTodoItem) {
  return [
    item.departmentName && `责任部门 ${item.departmentName}`,
    item.dueTime && `截止 ${formatChineseDateTime(item.dueTime, '--')}`,
    item.ownerName && `负责人 ${item.ownerName}`,
    item.detail
  ]
    .filter(Boolean)
    .join(' · ')
}

function show(value?: number) {
  return value ?? '--'
}

function go(path: string) {
  router.push(path)
}

async function loadAll() {
  loading.value = true
  // allSettled：某个接口无权限或失败时，其余卡片照常显示，失败的显示 --
  // 一次取全部待办（含 mine 标记），「我负责的 / 全部」在页面切换，免得来回请求
  const [summaryRes, admissionRes, bedRes, logRes, todoRes, contractRes] = await Promise.allSettled([
    getResidenceStatusSummary(),
    getAdmissionRecordSummary({}),
    getBedMap(),
    getChangeLogs({ pageNo: 1, pageSize: 6 }),
    getElderWorkbenchTodos('all'),
    getContractPage({ pageNo: 1, pageSize: 1, flowStage: 'PENDING_BED_SELECT' })
  ])
  summary.value = summaryRes.status === 'fulfilled' ? summaryRes.value : undefined
  admissionSummary.value = admissionRes.status === 'fulfilled' ? admissionRes.value : undefined
  beds.value = bedRes.status === 'fulfilled' ? bedRes.value || [] : undefined
  changeLogs.value = logRes.status === 'fulfilled' ? logRes.value.list || [] : []
  todosFailed.value = todoRes.status === 'rejected'
  allTodos.value = todoRes.status === 'fulfilled' ? todoRes.value.items || [] : []
  if (!todoScope.value && !todosFailed.value) todoScope.value = defaultTodoScope(allTodos.value)
  admissionContractTotal.value = contractRes.status === 'fulfilled' ? contractRes.value.total : undefined
  lastLoadedAt.value = formatChineseDateTime(new Date())
  loading.value = false
}

onMounted(() => {
  // 旧链接 ?residentId= 指向单个长者，转到一体化详情
  const residentId = String(route.query.residentId || route.query.elderId || '').trim()
  if (residentId) {
    router.replace(`/elder/detail/${encodeURIComponent(residentId)}`)
    return
  }
  loadAll()
})

useLiveSyncRefresh({
  topics: ['elder', 'lifecycle', 'bed'],
  refresh: () => {
    if (!loading.value) loadAll()
  },
  debounceMs: 1000
})
</script>

<style scoped>
.rm-split + .rm-split { margin-top: 16px; }
.rm-split--even { grid-template-columns: repeat(auto-fit, minmax(360px, 1fr)); }

.todo-icon {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  flex: none;
  border-radius: 8px;
  background: var(--rm-green-soft);
  color: var(--rm-green);
}
.todo-icon--danger { background: #fff1f0; color: var(--rm-danger); }
.todo-icon--warning { background: #fff5e1; color: var(--rm-warning); }
.todo-icon--info { background: #e8f0fc; color: var(--rm-info); }
.todo-more { padding-top: 10px; }

.status-title { margin-top: 20px; }
.status-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 0;
  cursor: pointer;
}
.status-row:hover .status-label { color: var(--rm-green); }
.status-label { flex: 1; color: var(--rm-muted); }
.status-dot { width: 8px; height: 8px; border-radius: 50%; background: #c4ccc9; }
.status-dot--success { background: var(--rm-green); }
.status-dot--warning { background: var(--rm-warning); }
.status-dot--info { background: var(--rm-info); }
.status-dot--danger { background: var(--rm-danger); }

.building-row + .building-row { margin-top: 10px; }
.building-row__head { display: flex; justify-content: space-between; align-items: baseline; }
.building-row__rate { color: var(--rm-green-dark); }

.log-row { display: flex; justify-content: space-between; gap: 12px; }
</style>
