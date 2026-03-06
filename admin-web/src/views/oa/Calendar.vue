<template>
  <PageContainer title="行政日历 / 协同日历" subTitle="与首页一致：个人、部门工作、日常计划、协同日历统一视图">
    <SearchForm :model="query" @search="fetchAll" @reset="onReset">
      <a-form-item label="日历种类">
        <a-select v-model:value="query.calendarType" :options="calendarTypeOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="紧急程度">
        <a-select v-model:value="query.urgency" :options="urgencyOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="负责人">
        <a-input v-model:value="query.assigneeName" placeholder="负责人" allow-clear />
      </a-form-item>
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <template #extra>
        <a-space wrap>
          <a-button type="primary" @click="openCreate()">新增行政日程</a-button>
          <a-button @click="fetchAll">刷新</a-button>
        </a-space>
      </template>
    </SearchForm>

    <a-card class="card-elevated" :bordered="false">
      <div class="calendar-toolbar">
        <div class="calendar-title-tip">日历分层：个人、部门工作、日常计划、协同日历（与首页一致）</div>
        <a-space wrap>
          <a-checkable-tag
            v-for="item in calendarBuckets"
            :key="item.type"
            :checked="visibleCalendarTypes.includes(item.type)"
            @change="toggleCalendarType(item.type)"
          >
            <span class="legend-dot" :style="{ background: item.color }"></span>
            {{ item.label }} {{ item.count }}
          </a-checkable-tag>
        </a-space>
      </div>
      <FullCalendar :options="calendarOptions" />
    </a-card>

    <a-drawer
      v-model:open="dayDrawerOpen"
      :title="selectedDate ? `${selectedDate.format('YYYY-MM-DD')} 日程详情` : '日程详情'"
      width="560"
    >
      <div class="drawer-actions">
        <a-space wrap>
          <a-button type="primary" @click="openCreate(selectedDate || dayjs())">新增该日计划</a-button>
          <a-button @click="quickCreateTemplate('基础办公')">基础办公</a-button>
          <a-button @click="quickCreateTemplate('行政日常')">行政日常</a-button>
          <a-button @click="quickCreateTemplate('协同会议')">协同会议</a-button>
        </a-space>
      </div>
      <a-list :data-source="selectedDayEvents" :locale="{ emptyText: '当天暂无日程，可点击上方快速创建' }" item-layout="vertical">
        <template #renderItem="{ item }">
          <a-list-item :class="{ 'agenda-item-done': item.status === 'DONE' }">
            <a-list-item-meta>
              <template #title>
                <a-space wrap>
                  <span :class="{ 'agenda-title-done': item.status === 'DONE' }">{{ item.title }}</span>
                  <a-tag v-if="item.status === 'DONE'">已完成</a-tag>
                  <a-tag>{{ calendarTypeText(item.calendarType) }}</a-tag>
                  <a-tag :color="item.urgency === 'EMERGENCY' ? 'red' : 'blue'">{{ item.urgency === 'EMERGENCY' ? '紧急' : '常规' }}</a-tag>
                  <a-tag v-if="item.planCategory" color="purple">{{ item.planCategory }}</a-tag>
                </a-space>
              </template>
              <template #description>
                <div>
                  <div>{{ formatRange(item.startTime, item.endTime) }}</div>
                  <div>负责人：{{ item.assigneeName || '-' }}，协同：{{ (item.collaboratorNames || []).join('、') || '-' }}</div>
                </div>
              </template>
            </a-list-item-meta>
            <template #actions>
              <a-button type="link" size="small" @click="openEdit(item)">编辑</a-button>
              <a-button type="link" size="small" @click="markDone(item)">完成</a-button>
              <a-button type="link" size="small" danger @click="removeTask(item)">删除</a-button>
            </template>
          </a-list-item>
        </template>
      </a-list>
    </a-drawer>

    <a-modal
      v-model:open="editOpen"
      :title="modalTitle"
      @ok="submit"
      :confirm-loading="saving"
      width="760px"
    >
      <a-form layout="vertical">
        <a-form-item label="标题" required>
          <a-input v-model:value="form.title" placeholder="例如：月度行政例会" />
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="开始时间" required>
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
          <a-col :span="12">
            <a-form-item label="负责人">
              <a-input v-model:value="form.assigneeName" placeholder="例如：行政部" />
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
              <a-select v-model:value="form.calendarType" :options="calendarTypeOptions" @change="onCalendarTypeChange" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="紧急程度">
              <a-select v-model:value="form.urgency" :options="urgencyOptions" @change="onUrgencyChange" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="计划分类">
              <a-select v-model:value="form.planCategory" :options="planCategoryOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="颜色策略">
              <a-input :value="calendarTypeText(form.calendarType)" disabled />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12" v-if="form.calendarType === 'COLLAB'">
          <a-col :span="24">
            <a-form-item label="协同部门（可多选）">
              <a-select
                v-model:value="form.collaboratorDeptIds"
                mode="multiple"
                allow-clear
                show-search
                :filter-option="false"
                :options="departmentOptions"
                placeholder="可选择多个部门，系统会自动同步到这些部门成员的协同日历"
                @search="searchDepartments"
                @focus="() => !departmentOptions.length && searchDepartments('')"
              />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="协同成员">
              <a-select
                v-model:value="form.collaboratorIds"
                mode="multiple"
                allow-clear
                show-search
                :filter-option="false"
                :options="staffOptions"
                placeholder="可直接选择某些员工；与协同部门可同时选择"
                @search="searchStaff"
                @focus="() => !staffOptions.length && searchStaff('')"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-card size="small" class="repeat-card" title="周期计划">
          <a-row :gutter="12">
            <a-col :span="8">
              <a-form-item label="开启周期">
                <a-switch v-model:checked="form.recurring" checked-children="开启" un-checked-children="关闭" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="频率">
                <a-select v-model:value="form.recurrenceRule" :options="recurrenceRuleOptions" :disabled="!form.recurring" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="间隔">
                <a-input-number v-model:value="form.recurrenceInterval" :min="1" :max="90" :disabled="!form.recurring" style="width: 100%" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="12">
            <a-col :span="8">
              <a-form-item label="生成次数">
                <a-input-number v-model:value="form.recurrenceCount" :min="1" :max="30" :disabled="!form.recurring" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="16">
              <div class="repeat-tip">支持个人、部门、协同等多日历周期排程。</div>
            </a-col>
          </a-row>
        </a-card>

        <a-form-item label="备注">
          <a-textarea v-model:value="form.description" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import { getOaTaskCalendar, createOaTask, updateOaTask, completeOaTask, deleteOaTask } from '../../api/oa'
import { getStaffPage } from '../../api/rbac'
import { useDepartmentOptions } from '../../composables/useDepartmentOptions'
import { useStaffOptions } from '../../composables/useStaffOptions'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { useUserStore } from '../../stores/user'
import type { OaTask } from '../../types'

type CalendarType = 'PERSONAL' | 'WORK' | 'DAILY' | 'COLLAB'
type UrgencyType = 'NORMAL' | 'EMERGENCY'
type RecurrenceType = 'DAILY' | 'WEEKLY' | 'MONTHLY'

const userStore = useUserStore()
const rows = ref<OaTask[]>([])
const saving = ref(false)
const editOpen = ref(false)
const dayDrawerOpen = ref(false)
const selectedDate = ref<Dayjs>()
const editingTaskId = ref<string | number | null>(null)
let syncTimer: number | undefined

const { departmentOptions, searchDepartments } = useDepartmentOptions({ pageSize: 240, preloadSize: 500 })
const { staffOptions, searchStaff } = useStaffOptions({ pageSize: 300, preloadSize: 500 })

const visibleCalendarTypes = ref<Array<CalendarType>>(['PERSONAL', 'WORK', 'DAILY', 'COLLAB'])

const query = reactive({
  status: undefined as string | undefined,
  calendarType: undefined as CalendarType | undefined,
  urgency: undefined as UrgencyType | undefined,
  assigneeName: '',
  range: undefined as [Dayjs, Dayjs] | undefined
})

const form = reactive({
  title: '',
  startTime: undefined as Dayjs | undefined,
  endTime: undefined as Dayjs | undefined,
  assigneeName: '',
  priority: 'NORMAL',
  description: '',
  calendarType: 'WORK' as CalendarType,
  urgency: 'NORMAL' as UrgencyType,
  planCategory: '基础办公',
  eventColor: '#1677ff',
  collaboratorDeptIds: [] as string[],
  collaboratorIds: [] as string[],
  recurring: false,
  recurrenceRule: 'WEEKLY' as RecurrenceType,
  recurrenceInterval: 1,
  recurrenceCount: 4
})

const statusOptions = [
  { label: '进行中', value: 'OPEN' },
  { label: '已完成', value: 'DONE' }
]

const priorityOptions = [
  { label: '低', value: 'LOW' },
  { label: '中', value: 'NORMAL' },
  { label: '高', value: 'HIGH' }
]

const calendarTypeOptions = [
  { label: '个人', value: 'PERSONAL' },
  { label: '部门工作', value: 'WORK' },
  { label: '日常计划', value: 'DAILY' },
  { label: '协同日历', value: 'COLLAB' }
]

const urgencyOptions = [
  { label: '常规', value: 'NORMAL' },
  { label: '紧急（红色）', value: 'EMERGENCY' }
]

const recurrenceRuleOptions = [
  { label: '每天', value: 'DAILY' },
  { label: '每周', value: 'WEEKLY' },
  { label: '每月', value: 'MONTHLY' }
]

const planCategoryOptions = [
  { label: '基础办公', value: '基础办公' },
  { label: '行政日常', value: '行政日常' },
  { label: '协同会议', value: '协同会议' },
  { label: '后勤排班', value: '后勤排班' },
  { label: '专项检查', value: '专项检查' }
]

const majorFestivalEvents = computed(() => {
  const year = dayjs().year()
  const items = [
    { date: `${year}-01-01`, title: '元旦' },
    { date: `${year}-05-01`, title: '劳动节' },
    { date: `${year}-10-01`, title: '国庆节' },
    { date: `${year}-12-31`, title: '年终总结日' }
  ]
  return items.map((item) => ({
    id: `calendar-festival-${item.date}`,
    title: `【重大节日】${item.title}`,
    start: `${item.date}T00:00:00`,
    end: `${item.date}T23:59:59`,
    allDay: true,
    color: '#f5222d',
    extendedProps: {
      calendarType: 'DAILY',
      urgency: 'EMERGENCY'
    }
  }))
})

const calendarBuckets = computed(() => {
  const defs = [
    { type: 'PERSONAL' as const, label: '个人', color: '#52c41a' },
    { type: 'WORK' as const, label: '部门工作', color: '#1677ff' },
    { type: 'DAILY' as const, label: '日常计划', color: '#fa8c16' },
    { type: 'COLLAB' as const, label: '协同日历', color: '#722ed1' }
  ]
  return defs.map((item) => ({
    ...item,
    count: rows.value.filter((task) => (task.calendarType || 'WORK') === item.type).length
  }))
})

const selectedDayEvents = computed(() => {
  if (!selectedDate.value) return []
  const selected = selectedDate.value.format('YYYY-MM-DD')
  return rows.value
    .filter((task) => visibleCalendarTypes.value.includes((task.calendarType || 'WORK') as CalendarType))
    .filter((task) => {
      const start = normalizeDateTimeValue(task.startTime || task.endTime)
      return start ? dayjs(start).format('YYYY-MM-DD') === selected : false
    })
    .sort((a, b) => {
      const left = normalizeDateTimeValue(a.startTime || a.endTime)
      const right = normalizeDateTimeValue(b.startTime || b.endTime)
      return (left ? dayjs(left).valueOf() : 0) - (right ? dayjs(right).valueOf() : 0)
    })
    .map((task) => ({
      ...task,
      collaboratorNames: normalizeCollaboratorNames(task.collaboratorNames)
    }))
})

const modalTitle = computed(() => (editingTaskId.value != null ? '编辑行政日程' : '新增行政日程'))

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  locale: 'zh-cn',
  height: 'auto',
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,dayGridWeek,dayGridDay'
  },
  buttonText: {
    today: '今天',
    dayGridMonth: '月视图',
    dayGridWeek: '周视图',
    dayGridDay: '日视图'
  },
  dateClick: (arg: { dateStr: string }) => {
    openDayDrawer(dayjs(arg.dateStr))
  },
  eventClick: (arg: { event: { start: Date | null } }) => {
    if (!arg.event.start) return
    openDayDrawer(dayjs(arg.event.start))
  },
  events: [
    ...rows.value
      .filter((task) => visibleCalendarTypes.value.includes((task.calendarType || 'WORK') as CalendarType))
      .map((task) => ({
        id: String(task.id),
        title: `${task.title}${task.assigneeName ? `（${task.assigneeName}）` : ''}`,
        start: normalizeDateTimeValue(task.startTime || task.endTime),
        end: normalizeDateTimeValue(task.endTime || task.startTime),
        color: resolveTaskColor(task),
        extendedProps: {
          calendarType: task.calendarType || 'WORK',
          urgency: task.urgency || 'NORMAL'
        }
      })),
    ...majorFestivalEvents.value
  ]
}))

function normalizeDateTimeValue(value?: string) {
  if (!value) return undefined
  const normalized = String(value).replace(/\s*T\s*/g, 'T').trim()
  const parsed = dayjs(normalized)
  if (!parsed.isValid()) return undefined
  return parsed.format('YYYY-MM-DDTHH:mm:ss')
}

function normalizeCollaboratorNames(input: string[] | string | undefined) {
  if (!input) return [] as string[]
  if (Array.isArray(input)) return input.filter(Boolean)
  return String(input)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function calendarTypeText(value?: string) {
  if (value === 'PERSONAL') return '个人'
  if (value === 'DAILY') return '日常计划'
  if (value === 'COLLAB') return '协同日历'
  return '部门工作'
}

function resolveCalendarTypeColor(calendarType: CalendarType, urgency: UrgencyType) {
  if (urgency === 'EMERGENCY') return '#ff4d4f'
  if (calendarType === 'PERSONAL') return '#52c41a'
  if (calendarType === 'DAILY') return '#fa8c16'
  if (calendarType === 'COLLAB') return '#722ed1'
  return '#1677ff'
}

function resolveTaskColor(task: OaTask) {
  if (task.status === 'DONE') return '#94a3b8'
  return resolveCalendarTypeColor((task.calendarType || 'WORK') as CalendarType, (task.urgency || 'NORMAL') as UrgencyType)
}

function toggleCalendarType(type: CalendarType) {
  if (visibleCalendarTypes.value.length === 1 && visibleCalendarTypes.value[0] === type) {
    visibleCalendarTypes.value = ['PERSONAL', 'WORK', 'DAILY', 'COLLAB']
    return
  }
  visibleCalendarTypes.value = [type]
}

function openDayDrawer(date: Dayjs) {
  selectedDate.value = date
  dayDrawerOpen.value = true
}

function formatRange(startTime?: string, endTime?: string) {
  const startValue = normalizeDateTimeValue(startTime)
  const endValue = normalizeDateTimeValue(endTime)
  if (!startValue) return '-'
  const start = dayjs(startValue)
  const end = endValue ? dayjs(endValue) : undefined
  if (!end) return start.format('HH:mm')
  return `${start.format('HH:mm')} - ${end.format('HH:mm')}`
}

function onUrgencyChange(value: UrgencyType) {
  form.eventColor = resolveCalendarTypeColor(form.calendarType, value)
}

function onCalendarTypeChange(value: CalendarType) {
  form.eventColor = resolveCalendarTypeColor(value, form.urgency)
}

function quickCreateTemplate(category: string) {
  openCreate(selectedDate.value || dayjs())
  form.planCategory = category
  form.calendarType = category === '协同会议' ? 'COLLAB' : category === '行政日常' ? 'DAILY' : 'WORK'
  form.eventColor = resolveCalendarTypeColor(form.calendarType, form.urgency)
  form.title = category
}

async function resolveCollaborators(departmentIds: string[], staffIds: string[]) {
  const collaboratorMap = new Map<string, string>()
  staffOptions.value
    .filter((item) => staffIds.includes(item.value))
    .forEach((item) => collaboratorMap.set(String(item.value), item.name || item.label))

  for (const staffId of staffIds) {
    if (!collaboratorMap.has(staffId)) collaboratorMap.set(staffId, `员工#${staffId}`)
  }

  if (departmentIds.length > 0) {
    const pages = await Promise.all(
      departmentIds.map((departmentId) => getStaffPage({
        pageNo: 1,
        pageSize: 500,
        departmentId,
        status: 1
      }))
    )
    pages.forEach((page) => {
      ;(page.list || []).forEach((item) => {
        const id = String(item.id)
        const name = (item.realName || item.username || `员工#${id}`).trim()
        collaboratorMap.set(id, name)
      })
    })
  }

  return {
    ids: Array.from(collaboratorMap.keys()),
    names: Array.from(collaboratorMap.values())
  }
}

async function fetchAll() {
  rows.value = await getOaTaskCalendar({
    status: query.status,
    calendarType: query.calendarType,
    urgency: query.urgency,
    assigneeName: query.assigneeName || undefined,
    startDate: query.range?.[0] ? dayjs(query.range[0]).format('YYYY-MM-DD') : undefined,
    endDate: query.range?.[1] ? dayjs(query.range[1]).format('YYYY-MM-DD') : undefined
  })
}

async function fetchStaffOptions() {
  await searchStaff('')
}

async function fetchDepartmentOptions() {
  await searchDepartments('')
}

function onReset() {
  query.status = undefined
  query.calendarType = undefined
  query.urgency = undefined
  query.assigneeName = ''
  query.range = undefined
  fetchAll()
}

function openCreate(date?: Dayjs) {
  editingTaskId.value = null
  form.title = ''
  form.startTime = date ? date.hour(9).minute(0).second(0) : undefined
  form.endTime = date ? date.hour(10).minute(0).second(0) : undefined
  form.assigneeName = (userStore.staffInfo?.realName || userStore.staffInfo?.username || '').trim()
  form.priority = 'NORMAL'
  form.description = ''
  form.calendarType = 'WORK'
  form.urgency = 'NORMAL'
  form.planCategory = '基础办公'
  form.eventColor = resolveCalendarTypeColor(form.calendarType, form.urgency)
  form.collaboratorDeptIds = []
  form.collaboratorIds = []
  form.recurring = false
  form.recurrenceRule = 'WEEKLY'
  form.recurrenceInterval = 1
  form.recurrenceCount = 4
  editOpen.value = true
}

function openEdit(item: OaTask) {
  editingTaskId.value = item.id
  form.title = item.title || ''
  form.startTime = item.startTime ? dayjs(item.startTime) : undefined
  form.endTime = item.endTime ? dayjs(item.endTime) : undefined
  form.assigneeName = item.assigneeName || ''
  form.priority = item.priority || 'NORMAL'
  form.description = item.description || ''
  form.calendarType = (item.calendarType || 'WORK') as CalendarType
  form.urgency = (item.urgency || 'NORMAL') as UrgencyType
  form.planCategory = item.planCategory || '基础办公'
  form.eventColor = resolveCalendarTypeColor(form.calendarType, form.urgency)
  form.collaboratorDeptIds = []
  form.collaboratorIds = Array.isArray(item.collaboratorIds)
    ? item.collaboratorIds.map((x) => String(x))
    : typeof item.collaboratorIds === 'string' && item.collaboratorIds
      ? item.collaboratorIds.split(',').map((x) => x.trim()).filter(Boolean)
      : []
  form.recurring = false
  form.recurrenceRule = 'WEEKLY'
  form.recurrenceInterval = 1
  form.recurrenceCount = 1
  editOpen.value = true
}

function upsertTaskRow(task: OaTask) {
  const index = rows.value.findIndex((item) => String(item.id) === String(task.id))
  if (index >= 0) {
    rows.value[index] = { ...rows.value[index], ...task }
    return
  }
  rows.value = [...rows.value, task]
}

function removeTaskRow(id: string | number) {
  rows.value = rows.value.filter((item) => String(item.id) !== String(id))
}

function markDone(item: OaTask) {
  if (!item.id) return
  Modal.confirm({
    title: '确认将该日程标记为已完成？',
    onOk: async () => {
      const updated = await completeOaTask(item.id)
      if (updated?.id != null) {
        upsertTaskRow(updated)
      } else {
        upsertTaskRow({ ...item, status: 'DONE' })
      }
      message.success('已标记完成')
    }
  })
}

function removeTask(item: OaTask) {
  if (!item.id) return
  Modal.confirm({
    title: '确认删除该日程？',
    okButtonProps: { danger: true },
    onOk: async () => {
      await deleteOaTask(item.id)
      removeTaskRow(item.id)
      message.success('已删除')
    }
  })
}

function addByRule(base: Dayjs, rule: RecurrenceType, interval: number) {
  if (rule === 'DAILY') return base.add(interval, 'day')
  if (rule === 'MONTHLY') return base.add(interval, 'month')
  return base.add(interval, 'week')
}

async function submit() {
  if (!form.title.trim()) {
    message.warning('请填写标题')
    return
  }
  if (!form.startTime) {
    message.warning('请选择开始时间')
    return
  }
  if (form.endTime && form.startTime.isAfter(form.endTime)) {
    message.warning('开始时间不能晚于结束时间')
    return
  }
  if (form.calendarType === 'COLLAB' && !form.collaboratorDeptIds.length && !form.collaboratorIds.length) {
    message.warning('协同日历请至少选择部门或协同成员')
    return
  }

  const collaboratorPayload = form.calendarType === 'COLLAB'
    ? await resolveCollaborators(form.collaboratorDeptIds, form.collaboratorIds)
    : { ids: [] as string[], names: [] as string[] }

  const repeatCount = form.recurring ? Math.max(1, Number(form.recurrenceCount || 1)) : 1
  const repeatRule = form.recurrenceRule
  const repeatInterval = Math.max(1, Number(form.recurrenceInterval || 1))

  saving.value = true
  try {
    if (editingTaskId.value != null) {
      const updated = await updateOaTask(editingTaskId.value, {
        title: form.title.trim(),
        description: form.description || undefined,
        startTime: dayjs(form.startTime).format('YYYY-MM-DDTHH:mm:ss'),
        endTime: form.endTime ? dayjs(form.endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
        priority: form.priority,
        assigneeName: form.assigneeName || undefined,
        calendarType: form.calendarType,
        planCategory: form.planCategory || undefined,
        urgency: form.urgency,
        eventColor: resolveCalendarTypeColor(form.calendarType, form.urgency),
        collaboratorIds: collaboratorPayload.ids,
        collaboratorNames: collaboratorPayload.names
      })
      upsertTaskRow(updated)
      editOpen.value = false
      editingTaskId.value = null
      message.success('日程已更新')
      return
    }

    const tasks: Promise<any>[] = []
    for (let i = 0; i < repeatCount; i += 1) {
      const startTime = i === 0 ? form.startTime : addByRule(form.startTime, repeatRule, repeatInterval * i)
      const endTime = form.endTime ? (i === 0 ? form.endTime : addByRule(form.endTime, repeatRule, repeatInterval * i)) : undefined
      tasks.push(
        createOaTask({
          title: form.title.trim(),
          description: form.description || undefined,
          startTime: dayjs(startTime).format('YYYY-MM-DDTHH:mm:ss'),
          endTime: endTime ? dayjs(endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
          priority: form.priority,
          status: 'OPEN',
          assigneeName: form.assigneeName || undefined,
          calendarType: form.calendarType,
          planCategory: form.planCategory || undefined,
          urgency: form.urgency,
          eventColor: resolveCalendarTypeColor(form.calendarType, form.urgency),
          collaboratorIds: collaboratorPayload.ids,
          collaboratorNames: collaboratorPayload.names,
          recurring: form.recurring,
          recurrenceRule: form.recurring ? repeatRule : undefined,
          recurrenceInterval: form.recurring ? repeatInterval : undefined,
          recurrenceCount: form.recurring ? repeatCount : undefined
        })
      )
    }

    const createdRows = await Promise.all(tasks)
    createdRows.forEach((row) => {
      if (row?.id != null) upsertTaskRow(row)
    })
    editOpen.value = false
    editingTaskId.value = null
    message.success(form.recurring ? `已生成 ${tasks.length} 条周期日程` : '日程已新增')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchAll(), fetchStaffOptions(), fetchDepartmentOptions()])
  syncTimer = window.setInterval(() => {
    fetchAll().catch(() => {})
  }, 15000)
})

onBeforeUnmount(() => {
  if (syncTimer) window.clearInterval(syncTimer)
})

useLiveSyncRefresh({
  topics: ['oa', 'hr', 'elder', 'system'],
  refresh: () => {
    if (saving.value) return
    fetchAll().catch(() => {})
  },
  debounceMs: 900
})
</script>

<style scoped>
.calendar-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.calendar-title-tip {
  color: #64748b;
  font-size: 12px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 6px;
}

.drawer-actions {
  margin-bottom: 12px;
}

.repeat-card {
  margin-bottom: 12px;
}

.repeat-tip {
  color: #64748b;
  font-size: 12px;
  line-height: 32px;
}

.agenda-item-done {
  opacity: 0.72;
}

.agenda-title-done {
  color: #64748b;
  text-decoration: line-through;
}
</style>
