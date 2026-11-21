<template>
  <PageContainer title="照护任务看板" subTitle="按时段安排照护，按长者核对服务与异常">
    <template #extra>
      <a-space>
        <a-date-picker v-model:value="dateValue" :allow-clear="false" value-format="YYYY-MM-DD" @change="changeDate" />
        <a-button v-if="canOpen('/care/exception')" @click="go('/care/exception')">异常任务</a-button>
        <a-button v-if="canOpen('/care/today')" type="primary" @click="go('/care/today')">进入今日任务</a-button>
      </a-space>
    </template>

    <a-row class="business-stat-strip" :gutter="[12, 12]">
      <a-col v-for="item in summaryCards" :key="item.title" :xs="12" :lg="6">
        <a-card class="card-elevated" :bordered="false" size="small">
          <a-statistic :title="item.title" :value="item.value" />
          <div class="stat-desc">{{ item.desc }}</div>
        </a-card>
      </a-col>
    </a-row>

    <a-alert
      v-if="signedLinkageContext.active"
      style="margin: 12px 0;"
      type="success"
      show-icon
      :message="signedLinkageContext.message"
      :description="signedLinkageContext.description"
    >
      <template #action>
        <a-space wrap>
          <a-button v-if="canOpen('/medical-care/inspection')" size="small" @click="go('/medical-care/inspection')">首次巡检</a-button>
          <a-button v-if="canOpen('/elder/resident-360')" size="small" @click="go('/elder/resident-360')">长者360</a-button>
        </a-space>
      </template>
    </a-alert>

    <a-card class="card-elevated" :bordered="false">
      <div class="board-toolbar">
        <a-radio-group :value="view" aria-label="照护任务分组方式" @change="changeView($event.target.value)">
          <a-radio-button value="time">按时段</a-radio-button>
          <a-radio-button value="resident">按长者</a-radio-button>
        </a-radio-group>
        <span aria-live="polite">{{ loading ? `正在加载，已读取 ${loadedCount} 条` : `当前条件共 ${rows.length} 条任务` }}</span>
      </div>
      <StatefulBlock :loading="loading" :error="errorMessage" :empty="!rows.length" empty-text="暂无照护任务" @retry="reload">
        <section v-for="group in groups" :key="group.key" class="task-group" :aria-label="group.title">
          <div class="task-group-heading">
            <h2>{{ group.title }}</h2>
            <a-space wrap><span>{{ group.tasks.length }} 项</span><span>待执行 {{ group.pending }}</span><a-tag v-if="group.risk" color="red">异常/超时 {{ group.risk }}</a-tag></a-space>
          </div>
        <a-table :columns="columns" :data-source="group.tasks" :pagination="{ pageSize: 10, hideOnSinglePage: true, showSizeChanger: false }" :scroll="{ x: 820 }" row-key="taskDailyId" :row-class-name="resolveRowClassName">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
            </template>
            <template v-else-if="column.key === 'planTime'">
              {{ formatPlanTime(record.planTime) }}
            </template>
            <template v-else-if="column.key === 'flags'">
              <a-space>
                <a-tag v-if="record.overdueFlag" color="red">超时</a-tag>
                <a-tag v-if="record.suspiciousFlag" color="orange">疑似异常</a-tag>
                <span v-if="!record.overdueFlag && !record.suspiciousFlag">-</span>
              </a-space>
            </template>
            <template v-else-if="column.key === 'actions'">
              <a-button v-if="canOpen('/care/today')" type="link" size="small" @click="goTask(record)">查看任务</a-button>
              <span v-else>请联系护理责任人</span>
            </template>
          </template>
        </a-table>
        </section>
      </StatefulBlock>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../../components/PageContainer.vue'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import { getTaskPage } from '../../../api/care'
import { normalizeId, normalizeResidentId } from '../../../utils/id'
import type { CareTaskItem, Id } from '../../../types'
import { resolveCareError } from '../careError'
import { groupCareTasks, type CareTaskView } from '../../../utils/careTaskBoard'
import { useUserStore } from '../../../stores/user'
import { resolveRouteAccess } from '../../../utils/routeAccess'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
function canOpen(path: string) {
  return resolveRouteAccess(router, user.roles, path, user.pagePermissions, user.permissions).canAccess
}
const residentId = computed<Id | undefined>(() => normalizeResidentId(route.query as Record<string, unknown>))
const activeTaskId = computed<Id | undefined>(() => normalizeId(route.query.taskId))
const filter = computed(() => String(route.query.filter || 'all'))
function routeDate() {
  const value = String(route.query.date || '')
  return value && value !== 'today' && dayjs(value).isValid() ? dayjs(value).format('YYYY-MM-DD') : dayjs().format('YYYY-MM-DD')
}
const dateValue = ref(routeDate())
const view = computed<CareTaskView>(() => route.query.view === 'resident' ? 'resident' : 'time')
const groups = computed(() => groupCareTasks(rows.value, view.value))

const loading = ref(false)
const errorMessage = ref('')
const rows = ref<CareTaskItem[]>([])
const loadedCount = ref(0)
let loadSequence = 0

const columns = [
  { title: '长者', dataIndex: 'elderName', key: 'elderName', width: 100 },
  { title: '房间', dataIndex: 'roomNo', key: 'roomNo', width: 90 },
  { title: '任务', dataIndex: 'taskName', key: 'taskName' },
  { title: '计划时间', key: 'planTime', width: 170 },
  { title: '执行护工', dataIndex: 'staffName', key: 'staffName', width: 100 },
  { title: '状态', key: 'status', width: 90 },
  { title: '标记', key: 'flags', width: 140 },
  { title: '操作', key: 'actions', width: 100 }
]

const summaryCards = computed(() => [
  { title: '任务总数', value: rows.value.length, desc: '当前日期与查询条件' },
  { title: '待执行', value: rows.value.filter(task => ['PENDING', 'PROCESSING'].includes(task.status)).length, desc: '含待分配与执行中' },
  { title: '已完成', value: rows.value.filter(task => task.status === 'DONE').length, desc: '当前条件下的完成任务' },
  { title: '异常/超时', value: rows.value.filter(task => task.status === 'EXCEPTION' || task.overdueFlag || task.suspiciousFlag).length, desc: '同一任务只计一次' }
])
const signedLinkageContext = computed(() => {
  const source = routeQueryText(route.query.source).toLowerCase()
  const entryScene = routeQueryText(route.query.entryScene).toLowerCase()
  const residentName = routeQueryText(route.query.residentName)
  const active = !!residentId.value && (source === 'contract_signed' || entryScene === 'signed_onboarding')
  return {
    active,
    message: active ? `当前为新签约长者 ${residentName || '当前长者'} 的护理交接任务` : '',
    description: active ? '已默认聚焦今日照护任务，请先确认交接、执行安排和责任人。' : ''
  }
})

function statusColor(status?: string) {
  if (status === 'DONE') return 'green'
  if (status === 'EXCEPTION') return 'red'
  if (status === 'PENDING') return 'orange'
  return 'default'
}

const STATUS_LABELS: Record<string, string> = {
  DONE: '已完成',
  EXCEPTION: '异常',
  PENDING: '待执行',
  PROCESSING: '执行中',
  CANCELLED: '已取消'
}

function statusLabel(status?: string) {
  if (!status) return '-'
  return STATUS_LABELS[status] || status
}

function formatPlanTime(value?: string) {
  if (!value) return '-'
  const parsed = dayjs(value)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm') : value
}

function go(path: string) {
  if (!canOpen(path)) return
  const [pathname, search] = path.split('?')
  const parsed = new URLSearchParams(search || '')
  const query: Record<string, any> = {}
  parsed.forEach((value, key) => {
    query[key] = value
  })
  const resident = route.query.residentId ?? route.query.elderId
  if (resident != null && query.residentId == null && query.elderId == null) {
    query.residentId = resident
    query.elderId = resident
  }
  if (resident != null) {
    for (const key of ['contractNo', 'leadId', 'bedId']) {
      const value = routeQueryText(route.query[key])
      if (value && query[key] == null) query[key] = value
    }
  }
  if (pathname === '/care/today') query.date = dateValue.value
  router.push({ path: pathname, query })
}

function changeView(value: CareTaskView) {
  void router.replace({ query: { ...route.query, view: value } })
}

function changeDate() {
  void router.replace({ query: { ...route.query, date: dateValue.value } })
}

function goTask(task: CareTaskItem) {
  if (!canOpen('/care/today')) return
  const query: Record<string, string> = {
    residentId: String(task.elderId), elderId: String(task.elderId),
    taskId: String(task.taskDailyId), date: dateValue.value
  }
  if (String(task.elderId) === String(residentId.value)) {
    for (const key of ['contractNo', 'leadId', 'bedId']) {
      const value = routeQueryText(route.query[key])
      if (value) query[key] = value
    }
  }
  void router.push({ path: '/care/today', query })
}

function routeQueryText(value: unknown) {
  return typeof value === 'string' ? value.trim() : ''
}

function resolveRowClassName(record: CareTaskItem) {
  if (activeTaskId.value && String(record.taskDailyId || '') === String(activeTaskId.value)) {
    return 'task-row-focus'
  }
  return ''
}

async function reload() {
  const sequence = ++loadSequence
  loading.value = true
  errorMessage.value = ''
  rows.value = []
  loadedCount.value = 0
  try {
    const status = filter.value === 'overdue_or_missed' ? 'PENDING' : undefined
    const overdueOnly = filter.value === 'overdue_or_missed' ? true : undefined
    const params = { pageSize: 200, date: dateValue.value, elderId: residentId.value, status, overdueOnly }
    const tasks = new Map<number, CareTaskItem>()
    let pageNo = 1
    let total = 0
    do {
      const page = await getTaskPage({ ...params, pageNo })
      if (sequence !== loadSequence) return
      total = page.total
      const previousCount = tasks.size
      for (const task of page.list || []) tasks.set(task.taskDailyId, task)
      if (tasks.size === previousCount && tasks.size < total) throw new Error('任务数据不完整，请刷新后重试')
      loadedCount.value = tasks.size
      pageNo++
    } while (tasks.size < total)
    rows.value = [...tasks.values()]
  } catch (error) {
    if (sequence !== loadSequence) return
    rows.value = []
    errorMessage.value = resolveCareError(error, '加载照护任务失败')
    message.error(errorMessage.value)
  } finally {
    if (sequence === loadSequence) loading.value = false
  }
}

onMounted(reload)

watch(
  () => [route.query.residentId, route.query.elderId, route.query.taskId, route.query.date, route.query.filter],
  () => {
    dateValue.value = routeDate()
    void reload()
  }
)
</script>

<style scoped>
.stat-desc {
  margin-top: 4px;
  color: var(--muted);
  font-size: 12px;
}
.board-toolbar, .task-group-heading { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; }
.board-toolbar { margin-bottom: 16px; color: var(--muted); }
.task-group + .task-group { margin-top: 24px; }
.task-group-heading { margin-bottom: 10px; }
.task-group-heading h2 { margin: 0; font-size: 16px; }
:deep(.task-row-focus > td) {
  background: var(--primary-soft) !important;
}
</style>
