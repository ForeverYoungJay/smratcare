<template>
  <PageContainer title="日程与任务" subTitle="任务列表与日历视图联动">
    <a-tabs v-model:activeKey="activeKey">
      <a-tab-pane key="list" tab="任务列表">
        <SearchForm :model="query" @search="fetchData" @reset="onReset">
          <a-form-item label="关键字">
            <a-input v-model:value="query.keyword" placeholder="标题/描述/负责人" allow-clear style="width: 240px" />
          </a-form-item>
          <a-form-item label="状态">
            <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
          </a-form-item>
          <a-form-item label="自动刷新">
            <a-switch v-model:checked="autoRefresh" />
          </a-form-item>
          <template #extra>
            <a-space wrap>
              <a-button type="primary" @click="openCreate">新增任务</a-button>
              <a-button @click="openCalendarView">查看日历</a-button>
              <a-button @click="downloadExport">导出CSV</a-button>
              <a-divider type="vertical" />
              <span class="action-group-title">单条操作</span>
              <a-button :disabled="!selectedSingleRecord || !isSelectedSingleOpen" @click="editSelected">编辑</a-button>
              <a-button :disabled="!selectedSingleRecord || !isSelectedSingleOpen" @click="doneSelected">完成</a-button>
              <a-button :disabled="!selectedSingleRecord" danger @click="removeSelected">删除</a-button>
              <a-divider type="vertical" />
              <span class="action-group-title">批量操作</span>
              <a-button :disabled="selectedOpenCount === 0" @click="batchDone">批量完成</a-button>
              <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchRemove">批量删除</a-button>
              <a-tag color="blue">已勾选 {{ selectedRowKeys.length }} 条</a-tag>
              <span class="selection-tip">批量完成仅对进行中生效</span>
            </a-space>
          </template>
        </SearchForm>

        <a-alert
          v-if="lifecycleContext.active"
          type="info"
          show-icon
          style="margin-bottom: 12px"
          :message="lifecycleContext.message"
        >
          <template #description>
            <a-space wrap>
              <a-tag color="blue">场景：入住状态变更联动</a-tag>
              <a-button type="link" size="small" @click="applySummaryFilter('overdue')">查看逾期任务</a-button>
              <a-button type="link" size="small" @click="applySummaryFilter('highPriority')">查看高优任务</a-button>
            </a-space>
          </template>
        </a-alert>

        <StatefulBlock :loading="summaryLoading" :error="summaryError" :empty="false" @retry="fetchData">
          <a-row :gutter="[12, 12]" style="margin-bottom: 12px">
            <a-col :xs="12" :lg="4">
              <a-card class="card-elevated summary-filter-card" :class="{ active: activeSummaryFilter === 'total' }" :bordered="false" size="small" @click="applySummaryFilter('total')">
                <a-statistic title="总任务" :value="summary.totalCount || 0" />
              </a-card>
            </a-col>
            <a-col :xs="12" :lg="4">
              <a-card class="card-elevated summary-filter-card" :class="{ active: activeSummaryFilter === 'open' }" :bordered="false" size="small" @click="applySummaryFilter('open')">
                <a-statistic title="进行中" :value="summary.openCount || 0" />
              </a-card>
            </a-col>
            <a-col :xs="12" :lg="4">
              <a-card class="card-elevated summary-filter-card" :class="{ active: activeSummaryFilter === 'done' }" :bordered="false" size="small" @click="applySummaryFilter('done')">
                <a-statistic title="已完成" :value="summary.doneCount || 0" />
              </a-card>
            </a-col>
            <a-col :xs="12" :lg="4">
              <a-card class="card-elevated summary-filter-card" :class="{ active: activeSummaryFilter === 'highPriority' }" :bordered="false" size="small" @click="applySummaryFilter('highPriority')">
                <a-statistic title="高优先级" :value="summary.highPriorityCount || 0" />
              </a-card>
            </a-col>
            <a-col :xs="12" :lg="4">
              <a-card class="card-elevated summary-filter-card" :class="{ active: activeSummaryFilter === 'dueToday' }" :bordered="false" size="small" @click="applySummaryFilter('dueToday')">
                <a-statistic title="今日到期" :value="summary.dueTodayCount || 0" />
              </a-card>
            </a-col>
            <a-col :xs="12" :lg="4">
              <a-card class="card-elevated summary-filter-card" :class="{ active: activeSummaryFilter === 'overdue' }" :bordered="false" size="small" @click="applySummaryFilter('overdue')">
                <a-statistic title="已逾期" :value="summary.overdueCount || 0" />
              </a-card>
            </a-col>
            <a-col :xs="12" :lg="4">
              <a-card class="card-elevated summary-filter-card" :class="{ active: activeSummaryFilter === 'unassigned' }" :bordered="false" size="small" @click="applySummaryFilter('unassigned')">
                <a-statistic title="未分配" :value="summary.unassignedCount || 0" />
              </a-card>
            </a-col>
          </a-row>
          <a-alert
            v-if="(summary.overdueCount || 0) > 0 || (summary.highPriorityCount || 0) > 0"
            type="warning"
            show-icon
            style="margin-bottom: 12px"
            :message="`任务提醒：逾期 ${summary.overdueCount || 0} 条，高优先级 ${summary.highPriorityCount || 0} 条。`"
          />
        </StatefulBlock>

        <StatefulBlock :loading="loading" :error="tableError" :empty="!loading && !tableError && rows.length === 0" empty-text="暂无任务记录" @retry="fetchData">
          <DataTable
            rowKey="id"
            :row-selection="rowSelection"
            :columns="columns"
            :data-source="rows"
            :loading="false"
            :pagination="pagination"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 'DONE' ? 'green' : 'orange'">
                  {{ record.status === 'DONE' ? '已完成' : '进行中' }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'priority'">
                <a-tag :color="record.priority === 'HIGH' ? 'red' : record.priority === 'LOW' ? 'blue' : 'gold'">
                  {{ priorityLabel(record.priority) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'startTime'">
                {{ record.startTime ? dayjs(record.startTime).format('YYYY-MM-DD HH:mm') : '-' }}
              </template>
              <template v-else-if="column.key === 'endTime'">
                <a-space size="small">
                  <span :style="{ color: isOverdueTask(record) ? '#cf1322' : undefined }">
                    {{ record.endTime ? dayjs(record.endTime).format('YYYY-MM-DD HH:mm') : '-' }}
                  </span>
                  <a-tag v-if="isOverdueTask(record)" color="red">逾期</a-tag>
                </a-space>
              </template>
            </template>
          </DataTable>
        </StatefulBlock>
      </a-tab-pane>
      <a-tab-pane key="calendar" tab="日历视图">
        <a-card class="card-elevated" :bordered="false">
          <div class="calendar-toolbar">
            <a-space wrap>
              <a-tag color="blue">与任务列表实时联动</a-tag>
              <a-button @click="activeKey = 'list'">返回列表</a-button>
              <a-button :loading="calendarLoading" @click="reloadCalendar">刷新日历</a-button>
            </a-space>
          </div>
          <FullCalendar ref="calendarRef" :options="calendarOptions" />
        </a-card>
      </a-tab-pane>
    </a-tabs>

    <a-modal v-model:open="editOpen" title="任务" @ok="submit" :confirm-loading="saving" width="760px">
      <a-form layout="vertical">
        <a-form-item label="任务标题" required>
          <a-input v-model:value="form.title" placeholder="请输入任务标题" />
        </a-form-item>
        <a-form-item label="任务描述">
          <a-textarea v-model:value="form.description" :rows="3" placeholder="可填写目标、交付标准、备注" />
        </a-form-item>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="负责人（可搜索）">
              <a-select
                v-model:value="form.assigneeName"
                show-search
                allow-clear
                :filter-option="false"
                :loading="staffLoading"
                :options="assigneeOptions"
                placeholder="输入姓名搜索"
                @focus="warmTaskStaffOptions"
                @search="searchStaff"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="优先级">
              <a-select v-model:value="form.priority" :options="priorityOptions" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="日历类型">
              <a-select v-model:value="form.calendarType" :options="calendarTypeOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="紧急程度">
              <a-select v-model:value="form.urgency" :options="urgencyOptions" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="任务分类（可搜索）">
              <a-select
                v-model:value="form.planCategory"
                show-search
                allow-clear
                :options="planCategoryOptions"
                :filter-option="filterOptionByLabel"
                placeholder="如：基础办公、行政巡检"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="协同成员（可搜索/可多选）">
              <a-select
                v-model:value="form.collaboratorNames"
                mode="multiple"
                show-search
                allow-clear
                :filter-option="false"
                :loading="staffLoading"
                :options="collaboratorOptions"
                placeholder="输入姓名搜索"
                @focus="warmTaskStaffOptions"
                @search="searchStaff"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="开始时间">
              <a-date-picker v-model:value="form.startTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束时间">
              <a-date-picker v-model:value="form.endTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="周期任务">
              <a-switch v-model:checked="form.recurring" checked-children="开启" un-checked-children="关闭" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="周期规则">
              <a-select v-model:value="form.recurrenceRule" :disabled="!form.recurring" :options="recurrenceRuleOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="周期间隔">
              <a-input-number v-model:value="form.recurrenceInterval" :disabled="!form.recurring" :min="1" :max="90" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="editableStatusOptions" disabled />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref, computed, watch, onMounted, onUnmounted } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import type { SelectProps } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { useStaffOptions } from '../../composables/useStaffOptions'
import {
  getOaTaskSummary,
  getOaTaskPage,
  getOaTaskCalendar,
  createOaTask,
  updateOaTask,
  completeOaTask,
  deleteOaTask,
  batchCompleteOaTask,
  batchDeleteOaTask,
  exportOaTask
} from '../../api/oa'
import type { OaTask, OaTaskSummary } from '../../types'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'

const activeKey = ref('list')
const route = useRoute()
const loading = ref(false)
const tableError = ref('')
const summaryLoading = ref(false)
const summaryError = ref('')
const calendarLoading = ref(false)
const rows = ref<OaTask[]>([])
const calendarRows = ref<OaTask[]>([])
const query = reactive({ keyword: '', status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const autoRefresh = ref(true)
const AUTO_REFRESH_INTERVAL_MS = 60 * 1000
let autoRefreshTimer: number | undefined
const activeSummaryFilter = ref<'total' | 'open' | 'done' | 'highPriority' | 'dueToday' | 'overdue' | 'unassigned'>('total')
const quickHighPriorityOnly = ref(false)
const quickDueTodayOnly = ref(false)
const quickOverdueOnly = ref(false)
const quickUnassignedOnly = ref(false)
const routePresetSignature = ref('')
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<string[]>([])
const summary = reactive<OaTaskSummary>({
  totalCount: 0,
  openCount: 0,
  doneCount: 0,
  highPriorityCount: 0,
  dueTodayCount: 0,
  overdueCount: 0,
  unassignedCount: 0
})
const { staffOptions, staffLoading, searchStaff } = useStaffOptions({ pageSize: 160, preloadSize: 500 })

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 220 },
  { title: '负责人', dataIndex: 'assigneeName', key: 'assigneeName', width: 120 },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 170 },
  { title: '结束时间', dataIndex: 'endTime', key: 'endTime', width: 170 },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 }
]

const editOpen = ref(false)
const saving = ref(false)
const calendarRef = ref<any>()
const calendarRange = reactive({ startDate: '', endDate: '' })

const form = reactive({
  id: undefined as string | undefined,
  title: '',
  description: '',
  startTime: undefined as any,
  endTime: undefined as any,
  assigneeName: '',
  priority: 'NORMAL',
  status: 'OPEN',
  calendarType: 'WORK',
  planCategory: undefined as string | undefined,
  urgency: 'NORMAL',
  collaboratorNames: [] as string[],
  recurring: false,
  recurrenceRule: 'WEEKLY',
  recurrenceInterval: 1
})

const statusOptions = [
  { label: '进行中', value: 'OPEN' },
  { label: '已完成', value: 'DONE' }
]
const editableStatusOptions = [{ label: '进行中', value: 'OPEN' }]
const priorityOptions = [
  { label: '低', value: 'LOW' },
  { label: '普通', value: 'NORMAL' },
  { label: '高', value: 'HIGH' }
]
const calendarTypeOptions = [
  { label: '个人日历', value: 'PERSONAL' },
  { label: '工作日历', value: 'WORK' },
  { label: '日常计划', value: 'DAILY' },
  { label: '协同日历', value: 'COLLAB' }
]
const urgencyOptions = [
  { label: '常规', value: 'NORMAL' },
  { label: '紧急', value: 'EMERGENCY' }
]
const recurrenceRuleOptions = [
  { label: '每天', value: 'DAILY' },
  { label: '每周', value: 'WEEKLY' },
  { label: '每月', value: 'MONTHLY' }
]
const planCategoryOptions = [
  { label: '基础办公', value: '基础办公' },
  { label: '行政日常', value: '行政日常' },
  { label: '会议安排', value: '会议安排' },
  { label: '跨部门协同', value: '跨部门协同' },
  { label: '院长督办', value: '院长督办' }
]
const assigneeOptions = computed(() => {
  const optionMap = new Map<string, { label: string; value: string }>()
  ;(staffOptions.value || []).forEach((item) => {
    const name = String(item.name || item.label || item.username || '').trim()
    if (!name) return
    optionMap.set(name, { label: name, value: name })
  })
  const current = String(form.assigneeName || '').trim()
  if (current && !optionMap.has(current)) optionMap.set(current, { label: current, value: current })
  return Array.from(optionMap.values())
})
const collaboratorOptions = computed(() => {
  const optionMap = new Map<string, { label: string; value: string }>()
  ;(staffOptions.value || []).forEach((item) => {
    const name = String(item.name || item.label || item.username || '').trim()
    if (!name) return
    optionMap.set(name, { label: name, value: name })
  })
  ;(form.collaboratorNames || []).forEach((item) => {
    const name = String(item || '').trim()
    if (name && !optionMap.has(name)) optionMap.set(name, { label: name, value: name })
  })
  return Array.from(optionMap.values())
})
const lifecycleContext = computed(() => {
  const source = String(route.query.source || '').trim().toLowerCase()
  const scene = String(route.query.scene || '').trim().toLowerCase()
  const active = source === 'lifecycle' || scene === 'status-change'
  return {
    active,
    message: active ? '已自动应用“入住状态变更”联动筛选，优先展示高风险/超时任务。' : ''
  }
})

function filterOptionByLabel(input: string, option: SelectProps['options'][number]) {
  const label = String((option as any)?.label || '')
  return label.toLowerCase().includes(input.toLowerCase())
}

function warmTaskStaffOptions() {
  if (!staffOptions.value.length) {
    searchStaff('').catch(() => {})
  }
}

function priorityLabel(priority?: string) {
  if (priority === 'HIGH') return '高'
  if (priority === 'LOW') return '低'
  return '普通'
}

function isDueTodayTask(task: OaTask) {
  if (!task || task.status !== 'OPEN' || !task.endTime) return false
  const end = dayjs(task.endTime)
  return end.isValid() && end.isSame(dayjs(), 'day')
}

function isOverdueTask(task: OaTask) {
  if (!task || task.status !== 'OPEN' || !task.endTime) return false
  const end = dayjs(task.endTime)
  return end.isValid() && end.isBefore(dayjs())
}

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  height: 'auto',
  events: calendarRows.value.map((task) => ({
    id: String(task.id),
    title: `${task.title}${task.assigneeName ? ` · ${task.assigneeName}` : ''}`,
    start: task.startTime,
    end: task.endTime,
    color: task.urgency === 'EMERGENCY' ? '#ff4d4f' : task.priority === 'HIGH' ? '#fa8c16' : '#1677ff',
    extendedProps: { task }
  })),
  datesSet: (arg: any) => {
    calendarRange.startDate = dayjs(arg.start).format('YYYY-MM-DD')
    calendarRange.endDate = dayjs(arg.end).subtract(1, 'day').format('YYYY-MM-DD')
    loadCalendarData()
  },
  eventClick: (arg: any) => {
    const task = arg.event?.extendedProps?.task as OaTask | undefined
    if (!task) return
    activeKey.value = 'list'
    openEdit(task)
  }
}))

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
  }
}))
const selectedRecords = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(String(item.id))))
const selectedSingleRecord = computed(() => (selectedRecords.value.length === 1 ? selectedRecords.value[0] : null))
const isSelectedSingleOpen = computed(() => selectedSingleRecord.value?.status === 'OPEN')
const selectedOpenIds = computed(() => selectedRecords.value.filter((item) => item.status === 'OPEN').map((item) => String(item.id)))
const selectedOpenCount = computed(() => selectedOpenIds.value.length)

async function fetchData() {
  loading.value = true
  summaryLoading.value = true
  tableError.value = ''
  summaryError.value = ''
  try {
    const params = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status,
      keyword: query.keyword || undefined
    }
    const [res, sum] = await Promise.all([getOaTaskPage(params), getOaTaskSummary(params)])
    const rawRows = (res.list || []).map((item) => ({
      ...item,
      id: String(item.id)
    }))
    rows.value = rawRows.filter((item) => {
      if (quickHighPriorityOnly.value && item.priority !== 'HIGH') return false
      if (quickDueTodayOnly.value && !isDueTodayTask(item)) return false
      if (quickOverdueOnly.value && !isOverdueTask(item)) return false
      if (quickUnassignedOnly.value && String(item.assigneeName || '').trim()) return false
      return true
    })
    pagination.total = quickHighPriorityOnly.value || quickDueTodayOnly.value || quickOverdueOnly.value || quickUnassignedOnly.value ? rows.value.length : (res.total || res.list.length)
    selectedRowKeys.value = []
    Object.assign(summary, sum || {})
  } catch (error: any) {
    const text = error?.message || '加载失败，请稍后重试'
    tableError.value = text
    summaryError.value = text
  } finally {
    loading.value = false
    summaryLoading.value = false
  }
}

async function loadCalendarData() {
  if (!calendarRange.startDate || !calendarRange.endDate) return
  calendarLoading.value = true
  try {
    const res = await getOaTaskCalendar({
      status: query.status,
      keyword: query.keyword || undefined,
      startDate: calendarRange.startDate,
      endDate: calendarRange.endDate
    })
    calendarRows.value = (res || []).map((item) => ({
      ...item,
      id: String(item.id)
    }))
  } finally {
    calendarLoading.value = false
  }
}

function reloadCalendar() {
  loadCalendarData()
}

function openCalendarView() {
  activeKey.value = 'calendar'
  const selectedStart = selectedSingleRecord.value?.startTime
  if (selectedStart && calendarRef.value?.getApi) {
    calendarRef.value.getApi().gotoDate(selectedStart)
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function resetQuickFilters() {
  quickHighPriorityOnly.value = false
  quickDueTodayOnly.value = false
  quickOverdueOnly.value = false
  quickUnassignedOnly.value = false
}

function firstRouteQueryText(value: unknown) {
  if (Array.isArray(value)) return firstRouteQueryText(value[0])
  if (value == null) return ''
  return String(value).trim()
}

function buildRoutePresetSignature(source: Record<string, unknown>) {
  const keys = ['source', 'scene', 'status', 'keyword', 'quick', 'category', 'auto'] as const
  return keys.map((key) => `${key}:${firstRouteQueryText(source[key])}`).join('|')
}

function resolveRouteQuickFilter(routeQuick: string, routeCategory: string) {
  const quick = routeQuick.toLowerCase()
  if (quick === 'high-priority' || quick === 'high_priority' || quick === 'highpriority') return 'highPriority'
  if (quick === 'due-today' || quick === 'duetoday') return 'dueToday'
  if (quick === 'overdue' || quick === 'risk' || quick === 'alert') return 'overdue'
  if (quick === 'unassigned') return 'unassigned'
  if (routeCategory.toLowerCase() === 'alert') return 'overdue'
  return ''
}

function applyRoutePreset() {
  const status = firstRouteQueryText(route.query.status).toUpperCase()
  query.status = status === 'OPEN' || status === 'DONE' ? status : undefined
  query.keyword = firstRouteQueryText(route.query.keyword)
  const auto = firstRouteQueryText(route.query.auto).toLowerCase()
  if (auto === '0' || auto === 'false') autoRefresh.value = false
  if (auto === '1' || auto === 'true') autoRefresh.value = true

  activeSummaryFilter.value = 'total'
  resetQuickFilters()
  const quickFilter = resolveRouteQuickFilter(
    firstRouteQueryText(route.query.quick),
    firstRouteQueryText(route.query.category)
  )
  if (quickFilter === 'highPriority') {
    query.status = 'OPEN'
    activeSummaryFilter.value = 'highPriority'
    quickHighPriorityOnly.value = true
    return
  }
  if (quickFilter === 'dueToday') {
    query.status = 'OPEN'
    activeSummaryFilter.value = 'dueToday'
    quickDueTodayOnly.value = true
    return
  }
  if (quickFilter === 'overdue') {
    query.status = 'OPEN'
    activeSummaryFilter.value = 'overdue'
    quickOverdueOnly.value = true
    return
  }
  if (quickFilter === 'unassigned') {
    query.status = 'OPEN'
    activeSummaryFilter.value = 'unassigned'
    quickUnassignedOnly.value = true
    return
  }
  if (query.status === 'OPEN') {
    activeSummaryFilter.value = 'open'
    return
  }
  if (query.status === 'DONE') {
    activeSummaryFilter.value = 'done'
  }
}

function applySummaryFilter(filterKey: typeof activeSummaryFilter.value) {
  activeSummaryFilter.value = filterKey
  query.pageNo = 1
  pagination.current = 1
  resetQuickFilters()
  if (filterKey === 'total') {
    query.status = undefined
    fetchData()
    return
  }
  if (filterKey === 'open') {
    query.status = 'OPEN'
    fetchData()
    return
  }
  if (filterKey === 'done') {
    query.status = 'DONE'
    fetchData()
    return
  }
  if (filterKey === 'highPriority') {
    query.status = 'OPEN'
    quickHighPriorityOnly.value = true
    fetchData()
    return
  }
  if (filterKey === 'dueToday') {
    query.status = 'OPEN'
    quickDueTodayOnly.value = true
    fetchData()
    return
  }
  if (filterKey === 'overdue') {
    query.status = 'OPEN'
    quickOverdueOnly.value = true
    fetchData()
    return
  }
  if (filterKey === 'unassigned') {
    query.status = 'OPEN'
    quickUnassignedOnly.value = true
    fetchData()
  }
}

function onReset() {
  query.keyword = ''
  query.status = undefined
  activeSummaryFilter.value = 'total'
  resetQuickFilters()
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  warmTaskStaffOptions()
  form.id = undefined
  form.title = ''
  form.description = ''
  form.startTime = undefined
  form.endTime = undefined
  form.assigneeName = ''
  form.priority = 'NORMAL'
  form.status = 'OPEN'
  form.calendarType = 'WORK'
  form.planCategory = undefined
  form.urgency = 'NORMAL'
  form.collaboratorNames = []
  form.recurring = false
  form.recurrenceRule = 'WEEKLY'
  form.recurrenceInterval = 1
  editOpen.value = true
}

function parseCollaboratorNames(value: unknown): string[] {
  if (Array.isArray(value)) {
    return value.map((item) => String(item || '').trim()).filter(Boolean)
  }
  const text = String(value || '').trim()
  if (!text) return []
  return text.split(/[，,、]/).map((item) => item.trim()).filter(Boolean)
}

function openEdit(record: OaTask) {
  warmTaskStaffOptions()
  form.id = String(record.id)
  form.title = record.title
  form.description = record.description || ''
  form.startTime = record.startTime ? dayjs(record.startTime) : undefined
  form.endTime = record.endTime ? dayjs(record.endTime) : undefined
  form.assigneeName = record.assigneeName || ''
  form.priority = record.priority || 'NORMAL'
  form.status = record.status || 'OPEN'
  form.calendarType = record.calendarType || 'WORK'
  form.planCategory = record.planCategory || undefined
  form.urgency = record.urgency || 'NORMAL'
  form.collaboratorNames = parseCollaboratorNames(record.collaboratorNames)
  form.recurring = Boolean(record.recurring)
  form.recurrenceRule = record.recurrenceRule || 'WEEKLY'
  form.recurrenceInterval = Number(record.recurrenceInterval || 1)
  editOpen.value = true
}

async function submit() {
  if (!form.title.trim()) {
    message.warning('请填写任务标题')
    return
  }
  const payload = {
    title: form.title,
    description: form.description,
    startTime: form.startTime ? dayjs(form.startTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
    endTime: form.endTime ? dayjs(form.endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
    assigneeName: form.assigneeName,
    priority: form.priority,
    status: 'OPEN',
    calendarType: form.calendarType,
    planCategory: form.planCategory,
    urgency: form.urgency,
    eventColor: form.urgency === 'EMERGENCY' ? '#ff4d4f' : undefined,
    collaboratorNames: form.collaboratorNames,
    recurring: form.recurring,
    recurrenceRule: form.recurring ? form.recurrenceRule : undefined,
    recurrenceInterval: form.recurring ? form.recurrenceInterval : undefined
  }
  saving.value = true
  try {
    if (form.id) {
      await updateOaTask(form.id, payload)
    } else {
      await createOaTask(payload)
    }
    editOpen.value = false
    await fetchData()
    if (activeKey.value === 'calendar') {
      await loadCalendarData()
    }
  } finally {
    saving.value = false
  }
}

async function done(record: OaTask) {
  if (record.status !== 'OPEN') return
  await completeOaTask(String(record.id))
  await fetchData()
  if (activeKey.value === 'calendar') {
    await loadCalendarData()
  }
}

async function remove(record: OaTask) {
  await deleteOaTask(String(record.id))
  await fetchData()
  if (activeKey.value === 'calendar') {
    await loadCalendarData()
  }
}

function requireSingleSelection(action: string) {
  if (!selectedSingleRecord.value) {
    message.info(`请先勾选 1 条任务后再${action}`)
    return null
  }
  return selectedSingleRecord.value
}

function editSelected() {
  const record = requireSingleSelection('编辑')
  if (!record) return
  if (record.status !== 'OPEN') {
    message.warning('仅进行中任务可编辑')
    return
  }
  openEdit(record)
}

async function doneSelected() {
  const record = requireSingleSelection('完成')
  if (!record) return
  await done(record)
}

async function removeSelected() {
  const record = requireSingleSelection('删除')
  if (!record) return
  await remove(record)
}

async function batchDone() {
  if (selectedOpenIds.value.length === 0) {
    message.info('勾选项中没有进行中任务')
    return
  }
  const affected = await batchCompleteOaTask(selectedOpenIds.value)
  message.success(`批量完成，共处理 ${affected || 0} 条`)
  await fetchData()
  if (activeKey.value === 'calendar') {
    await loadCalendarData()
  }
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchDeleteOaTask(selectedRowKeys.value)
  message.success(`批量删除，共处理 ${affected || 0} 条`)
  await fetchData()
  if (activeKey.value === 'calendar') {
    await loadCalendarData()
  }
}

async function downloadExport() {
  const blob = await exportOaTask({
    keyword: query.keyword || undefined,
    status: query.status
  })
  const href = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = href
  link.download = `oa-task-${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(href)
}

function stopAutoRefreshTimer() {
  if (autoRefreshTimer !== undefined) {
    window.clearInterval(autoRefreshTimer)
    autoRefreshTimer = undefined
  }
}

function startAutoRefreshTimer() {
  if (!autoRefresh.value || autoRefreshTimer !== undefined) {
    return
  }
  autoRefreshTimer = window.setInterval(() => {
    if (loading.value || summaryLoading.value || calendarLoading.value || document.visibilityState !== 'visible') return
    fetchData()
    if (activeKey.value === 'calendar') {
      loadCalendarData()
    }
  }, AUTO_REFRESH_INTERVAL_MS)
}

watch(autoRefresh, (enabled) => {
  if (enabled) {
    startAutoRefreshTimer()
    return
  }
  stopAutoRefreshTimer()
})

watch(activeKey, (value) => {
  if (value === 'calendar') {
    loadCalendarData()
  }
})

onMounted(async () => {
  applyRoutePreset()
  routePresetSignature.value = buildRoutePresetSignature(route.query as Record<string, unknown>)
  query.pageNo = 1
  pagination.current = 1
  await fetchData()
  startAutoRefreshTimer()
})

watch(
  () => route.query,
  async (queryMap) => {
    const nextSignature = buildRoutePresetSignature(queryMap as Record<string, unknown>)
    if (nextSignature === routePresetSignature.value) return
    routePresetSignature.value = nextSignature
    applyRoutePreset()
    query.pageNo = 1
    pagination.current = 1
    await fetchData()
    if (activeKey.value === 'calendar') {
      await loadCalendarData()
    }
  },
  { deep: true }
)

onUnmounted(() => {
  stopAutoRefreshTimer()
})
</script>

<style scoped>
.selection-tip {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

.action-group-title {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.55);
}

.summary-filter-card {
  cursor: pointer;
  transition: all 0.2s ease;
}

.summary-filter-card:hover {
  transform: translateY(-1px);
}

.summary-filter-card.active {
  box-shadow: 0 0 0 1px #1677ff inset;
  background: #e6f4ff;
}

.calendar-toolbar {
  margin-bottom: 12px;
}
</style>
