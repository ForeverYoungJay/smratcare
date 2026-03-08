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

    <a-card class="card-elevated calendar-surface" :bordered="false">
      <div class="calendar-toolbar">
        <div>
          <div class="calendar-title-tip">日历分层：个人、部门工作、日常计划、协同日历（与首页一致）</div>
          <a-space size="small" wrap class="calendar-insights">
            <a-tag v-for="item in calendarInsights" :key="item.label" :color="item.color">{{ item.label }} {{ item.value }}</a-tag>
          </a-space>
          <a-space size="small" wrap class="calendar-loads" v-if="calendarLoadLeaders.length">
            <a-tag color="geekblue">负责人负载</a-tag>
            <a-tag v-for="item in calendarLoadLeaders" :key="item.name">{{ item.name }} {{ item.count }}条</a-tag>
          </a-space>
          <div class="calendar-week-trend" v-if="calendarWeeklyLoad.length">
            <span class="repeat-tip">本周负载：</span>
            <div
              v-for="item in calendarWeeklyLoad"
              :key="item.day"
              class="calendar-week-bar"
              :title="`${item.label} ${item.count}条`"
              :style="{ height: `${Math.max(18, item.height)}px` }"
            >
              <span>{{ item.label }}</span>
            </div>
          </div>
        </div>
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
          <a-button size="small" type="link" @click="showAllCalendarTypes">显示全部</a-button>
        </a-space>
      </div>
      <FullCalendar ref="calendarRef" :options="calendarOptions" />
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
                  <a-tag v-if="item.readonly" color="default">系统提醒</a-tag>
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
              <a-button v-if="!item.readonly" type="link" size="small" @click="openEdit(item)">编辑</a-button>
              <a-button v-if="!item.readonly" type="link" size="small" @click="markDone(item)">完成</a-button>
              <a-button v-if="!item.readonly" type="link" size="small" danger @click="removeTask(item)">删除</a-button>
              <span v-if="item.readonly" class="readonly-tip">{{ item.readonlyReason || '系统提醒' }}</span>
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
        <a-row :gutter="12">
          <a-col :span="24">
            <a-form-item label="冲突策略">
              <a-segmented v-model:value="conflictPolicy" :options="conflictPolicyOptions" block />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="24">
            <a-space wrap>
              <a-button :loading="conflictPreviewLoading" @click="previewScheduleConflicts">立即预检冲突</a-button>
              <span class="repeat-tip">保存前可先预检，策略生效：{{ conflictPolicyOptions.find((item) => item.value === conflictPolicy)?.label }}</span>
            </a-space>
          </a-col>
        </a-row>
        <a-row :gutter="12" v-if="conflictPreviewItems.length">
          <a-col :span="24">
            <a-alert :message="`预检发现 ${conflictPreviewItems.length} 条冲突`" type="warning" show-icon>
              <template #description>
                <div v-for="(item, index) in conflictPreviewItems.slice(0, 5)" :key="`${item.title}_${index}`">
                  {{ index + 1 }}. {{ item.title || '未命名日程' }}（{{ conflictItemTimeText(item) }}）{{ item.reason ? ` - ${item.reason}` : '' }}
                  <a-button type="link" size="small" @click="focusConflictDate(item.startTime)">定位</a-button>
                </div>
              </template>
            </a-alert>
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
          <a-col :span="24" v-if="collaboratorTip">
            <div class="repeat-tip">
              {{ collaboratorTip }}
              <a-button v-if="collaboratorPreviewAllNames.length > collaboratorPreviewNames.length" type="link" size="small" @click="showAllCollaboratorPreview">
                查看全部
              </a-button>
            </div>
          </a-col>
          <a-col :span="24" v-if="collaboratorDeltaTip">
            <div class="repeat-tip">{{ collaboratorDeltaTip }}</div>
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
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import { getOaTaskCalendar, createOaTask, updateOaTask, completeOaTask, deleteOaTask, checkOaTaskConflicts } from '../../api/oa'
import { getBirthdayPage } from '../../api/life'
import { getStaffPage } from '../../api/rbac'
import { useDepartmentOptions } from '../../composables/useDepartmentOptions'
import { useStaffOptions } from '../../composables/useStaffOptions'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { useUserStore } from '../../stores/user'
import type { BirthdayReminder, OaTask } from '../../types'

type CalendarType = 'PERSONAL' | 'WORK' | 'DAILY' | 'COLLAB'
type UrgencyType = 'NORMAL' | 'EMERGENCY'
type RecurrenceType = 'DAILY' | 'WEEKLY' | 'MONTHLY'

const userStore = useUserStore()
const rows = ref<OaTask[]>([])
const birthdayRows = ref<BirthdayReminder[]>([])
const saving = ref(false)
const collaboratorPreviewTotal = ref(0)
const collaboratorPreviewLoading = ref(false)
const collaboratorPreviewNames = ref<string[]>([])
const collaboratorPreviewAllNames = ref<string[]>([])
const originalCollaboratorIds = ref<string[]>([])
const conflictPreviewLoading = ref(false)
const conflictPreviewItems = ref<Array<{ title?: string; assigneeName?: string; startTime?: string; endTime?: string; reason?: string }>>([])
const editOpen = ref(false)
const dayDrawerOpen = ref(false)
const selectedDate = ref<Dayjs>()
const editingTaskId = ref<string | number | null>(null)
const calendarRef = ref<any>(null)
let syncTimer: number | undefined

const { departmentOptions, searchDepartments } = useDepartmentOptions({ pageSize: 240, preloadSize: 500 })
const { staffOptions, searchStaff } = useStaffOptions({ pageSize: 300, preloadSize: 500 })

const visibleCalendarTypes = ref<Array<CalendarType>>(['PERSONAL', 'WORK', 'DAILY', 'COLLAB'])
const VISIBLE_TYPES_STORAGE_KEY = 'oa_calendar_visible_types_v1'
const CALENDAR_VIEW_STORAGE_KEY = 'oa_calendar_view_mode_v1'
const CALENDAR_SYNC_PULSE_KEY = 'oa_calendar_sync_pulse_v1'
const CONFLICT_POLICY_STORAGE_KEY = 'oa_calendar_conflict_policy_v1'
const currentCalendarView = ref<'dayGridMonth' | 'dayGridWeek' | 'dayGridDay'>('dayGridMonth')
const conflictPolicy = ref<'WARN' | 'BLOCK' | 'ALLOW'>('WARN')
let calendarStorageHandler: ((event: StorageEvent) => void) | null = null

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
  assigneeId: undefined as string | number | undefined,
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
const conflictPolicyOptions = [
  { label: '提示后可继续', value: 'WARN' as const },
  { label: '发现冲突即阻止', value: 'BLOCK' as const },
  { label: '忽略冲突直接保存', value: 'ALLOW' as const }
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

const birthdayReminderEvents = computed(() => birthdayRows.value
  .map((item) => {
    const date = item.nextBirthday ? dayjs(item.nextBirthday) : null
    if (!date || !date.isValid()) return null
    const urgency = Number(item.daysUntil || 9999) <= 0 ? 'EMERGENCY' : 'NORMAL'
    return {
      id: `calendar-birthday-${item.elderId || item.elderName || item.nextBirthday}`,
      title: `🎂 ${item.elderName || '老人生日'}`,
      start: `${date.format('YYYY-MM-DD')}T09:00:00`,
      end: `${date.format('YYYY-MM-DD')}T10:00:00`,
      color: urgency === 'EMERGENCY' ? '#eb2f96' : '#f759ab',
      extendedProps: {
        calendarType: 'DAILY',
        urgency,
        assigneeName: '系统提醒',
        collaboratorNames: '',
        planCategory: '生日提醒',
        readonly: true,
        readonlyReason: '生日提醒'
      }
    }
  })
  .filter((item): item is NonNullable<typeof item> => !!item))

type DayAgendaItem = OaTask & {
  collaboratorNames: string[]
  readonly?: boolean
  readonlyReason?: string
}

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

const calendarInsights = computed(() => {
  const todayText = dayjs().format('YYYY-MM-DD')
  return [
    { label: '今日', value: rows.value.filter((task) => normalizeDateTimeValue(task.startTime || task.endTime)?.startsWith(todayText)).length, color: 'blue' },
    { label: '紧急', value: rows.value.filter((task) => task.urgency === 'EMERGENCY' && task.status !== 'DONE').length, color: 'red' },
    { label: '已完成', value: rows.value.filter((task) => task.status === 'DONE').length, color: 'default' },
    { label: '节假日', value: majorFestivalEvents.value.length, color: 'orange' },
    { label: '生日提醒', value: birthdayReminderEvents.value.length, color: 'magenta' }
  ]
})

const calendarLoadLeaders = computed(() => {
  const bucket = new Map<string, number>()
  rows.value
    .filter((task) => task.status !== 'DONE')
    .forEach((task) => {
      const name = (task.assigneeName || '未分配').trim()
      bucket.set(name, Number(bucket.get(name) || 0) + 1)
    })
  return Array.from(bucket.entries())
    .sort((a, b) => b[1] - a[1])
    .slice(0, 4)
    .map(([name, count]) => ({ name, count }))
})

const calendarWeeklyLoad = computed(() => {
  const start = dayjs().startOf('week')
  const list = Array.from({ length: 7 }).map((_, index) => {
    const day = start.add(index, 'day')
    const dayText = day.format('YYYY-MM-DD')
    const count = rows.value.filter((task) => {
      if (task.status === 'DONE') return false
      const startTime = normalizeDateTimeValue(task.startTime || task.endTime)
      return startTime ? dayjs(startTime).format('YYYY-MM-DD') === dayText : false
    }).length
    return {
      day: dayText,
      label: ['日', '一', '二', '三', '四', '五', '六'][day.day()],
      count
    }
  })
  const max = list.reduce((acc, item) => Math.max(acc, item.count), 1)
  return list.map((item) => ({
    ...item,
    height: Math.round((item.count / max) * 48)
  }))
})

const selectedDayEvents = computed<DayAgendaItem[]>(() => {
  if (!selectedDate.value) return []
  const selected = selectedDate.value.format('YYYY-MM-DD')
  const taskItems: DayAgendaItem[] = rows.value
    .filter((task) => visibleCalendarTypes.value.includes((task.calendarType || 'WORK') as CalendarType))
    .filter((task) => {
      const start = normalizeDateTimeValue(task.startTime || task.endTime)
      return start ? dayjs(start).format('YYYY-MM-DD') === selected : false
    })
    .map((task) => ({
      ...task,
      collaboratorNames: normalizeCollaboratorNames(task.collaboratorNames),
      readonly: false
    }))

  const birthdayItems: DayAgendaItem[] = birthdayReminderEvents.value
    .filter((event) => dayjs(event.start).format('YYYY-MM-DD') === selected)
    .map((event) => ({
      id: event.id,
      title: event.title,
      startTime: event.start,
      endTime: event.end,
      status: 'OPEN',
      calendarType: 'DAILY',
      urgency: (event.extendedProps?.urgency || 'NORMAL') as UrgencyType,
      assigneeName: '系统提醒',
      planCategory: '生日提醒',
      collaboratorNames: [],
      readonly: true,
      readonlyReason: '生日提醒'
    } as DayAgendaItem))

  const festivalItems: DayAgendaItem[] = majorFestivalEvents.value
    .filter((event) => dayjs(event.start).format('YYYY-MM-DD') === selected)
    .map((event) => ({
      id: event.id,
      title: event.title,
      startTime: event.start,
      endTime: event.end,
      status: 'OPEN',
      calendarType: 'DAILY',
      urgency: 'EMERGENCY',
      assigneeName: '系统提醒',
      planCategory: '节假日',
      collaboratorNames: [],
      readonly: true,
      readonlyReason: '节假日'
    } as DayAgendaItem))

  return [...taskItems, ...birthdayItems, ...festivalItems]
    .sort((a, b) => {
      const left = normalizeDateTimeValue(a.startTime || a.endTime)
      const right = normalizeDateTimeValue(b.startTime || b.endTime)
      return (left ? dayjs(left).valueOf() : 0) - (right ? dayjs(right).valueOf() : 0)
    })
})

const modalTitle = computed(() => (editingTaskId.value != null ? '编辑行政日程' : '新增行政日程'))
const collaboratorTip = computed(() => {
  if (form.calendarType !== 'COLLAB') return ''
  const deptCount = form.collaboratorDeptIds.length
  const staffCount = form.collaboratorIds.length
  if (!deptCount && !staffCount) return '未选择协同对象'
  const previewText = collaboratorPreviewLoading.value
    ? '正在计算同步人数'
    : `预计同步 ${collaboratorPreviewTotal.value} 人`
  const namesText = collaboratorPreviewNames.value.length ? `（${collaboratorPreviewNames.value.join('、')}）` : ''
  return `已选 ${deptCount} 个部门、${staffCount} 位员工；${previewText}${namesText}，保存后自动展开并同步到协同日历`
})

const collaboratorDeltaTip = computed(() => {
  if (form.calendarType !== 'COLLAB' || editingTaskId.value == null) return ''
  const currentSet = new Set(form.collaboratorIds)
  const originalSet = new Set(originalCollaboratorIds.value)
  const added = form.collaboratorIds.filter((id) => !originalSet.has(id))
  const removed = originalCollaboratorIds.value.filter((id) => !currentSet.has(id))
  if (!added.length && !removed.length) return '协同成员未发生变化'
  const addedText = added.length ? `新增 ${added.length} 人` : '新增 0 人'
  const removedText = removed.length ? `移除 ${removed.length} 人` : '移除 0 人'
  return `协同成员变更：${addedText}，${removedText}`
})

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: currentCalendarView.value,
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
  datesSet: (arg: any) => {
    const viewType = String(arg?.view?.type || '')
    if (viewType === 'dayGridMonth' || viewType === 'dayGridWeek' || viewType === 'dayGridDay') {
      currentCalendarView.value = viewType
    }
  },
  eventDidMount: (arg: any) => {
    const event = arg?.event
    const taskType = calendarTypeText(event?.extendedProps?.calendarType)
    const urgencyText = event?.extendedProps?.urgency === 'EMERGENCY' ? '紧急' : '常规'
    const assigneeText = event?.extendedProps?.assigneeName || '-'
    const collaboratorText = event?.extendedProps?.collaboratorNames || '-'
    const planCategory = event?.extendedProps?.planCategory || '-'
    arg?.el?.setAttribute?.(
      'title',
      `${event?.title || ''}\n类型：${taskType}\n紧急：${urgencyText}\n负责人：${assigneeText}\n协同：${collaboratorText}\n分类：${planCategory}`
    )
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
        classNames: task.status === 'DONE' ? ['calendar-event-done'] : [],
        extendedProps: {
          calendarType: task.calendarType || 'WORK',
          urgency: task.urgency || 'NORMAL',
          assigneeName: task.assigneeName || '',
          collaboratorNames: normalizeCollaboratorNames(task.collaboratorNames).join('、'),
          planCategory: task.planCategory || ''
        }
      })),
    ...birthdayReminderEvents.value,
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

function showAllCalendarTypes() {
  visibleCalendarTypes.value = ['PERSONAL', 'WORK', 'DAILY', 'COLLAB']
}

function normalizeVisibleTypes(input: unknown): CalendarType[] {
  const valid: CalendarType[] = ['PERSONAL', 'WORK', 'DAILY', 'COLLAB']
  if (!Array.isArray(input)) return [...valid]
  const filtered = input.filter((item): item is CalendarType => valid.includes(item as CalendarType))
  const deduped = Array.from(new Set(filtered))
  return deduped.length ? deduped : [...valid]
}

function syncVisibleTypesFromStorage() {
  try {
    const raw = localStorage.getItem(VISIBLE_TYPES_STORAGE_KEY)
    if (!raw) return
    visibleCalendarTypes.value = normalizeVisibleTypes(JSON.parse(raw))
  } catch {}
}

function syncCalendarViewFromStorage() {
  try {
    const raw = localStorage.getItem(CALENDAR_VIEW_STORAGE_KEY)
    if (!raw) return
    if (raw === 'dayGridMonth' || raw === 'dayGridWeek' || raw === 'dayGridDay') {
      currentCalendarView.value = raw
    }
  } catch {}
}

function syncConflictPolicyFromStorage() {
  try {
    const raw = localStorage.getItem(CONFLICT_POLICY_STORAGE_KEY)
    if (!raw) return
    if (raw === 'WARN' || raw === 'BLOCK' || raw === 'ALLOW') {
      conflictPolicy.value = raw
    }
  } catch {}
}

function emitCalendarSyncPulse(action: 'CREATE' | 'UPDATE' | 'DONE' | 'DELETE') {
  try {
    localStorage.setItem(CALENDAR_SYNC_PULSE_KEY, JSON.stringify({
      action,
      at: dayjs().format('YYYY-MM-DDTHH:mm:ss')
    }))
  } catch {}
}

async function fetchBirthdayReminders() {
  const data = await getBirthdayPage({
    pageNo: 1,
    pageSize: 500,
    daysAhead: 366
  })
  birthdayRows.value = data.list || []
}

function focusCalendarDate(dateText: string) {
  const api = calendarRef.value?.getApi?.()
  if (!api) {
    message.warning('日历未就绪，请稍后再试')
    return
  }
  api.gotoDate(dateText)
  api.changeView('dayGridDay')
  openDayDrawer(dayjs(dateText))
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

function resolveAssigneeIdForSubmit() {
  const currentId = userStore.staffInfo?.id != null ? String(userStore.staffInfo.id) : ''
  if (!currentId) return undefined
  const currentRealName = String(userStore.staffInfo?.realName || '').trim()
  const currentUsername = String(userStore.staffInfo?.username || '').trim()
  const input = String(form.assigneeName || '').trim()
  if (!input) return Number(currentId)
  if ((currentRealName && input === currentRealName) || (currentUsername && input === currentUsername)) {
    return Number(currentId)
  }
  return form.assigneeId != null ? Number(form.assigneeId) : undefined
}

function buildConflictMessage(items: Array<{ title?: string; assigneeName?: string; startTime?: string; endTime?: string; reason?: string }>) {
  return items
    .slice(0, 6)
    .map((item, index) => {
      const start = item.startTime ? dayjs(item.startTime).format('MM-DD HH:mm') : '--'
      const end = item.endTime ? dayjs(item.endTime).format('MM-DD HH:mm') : '--'
      return `${index + 1}. ${item.title || '未命名日程'}（${start} ~ ${end}）${item.assigneeName ? ` [${item.assigneeName}]` : ''} - ${item.reason || '时间冲突'}`
    })
    .join('\n')
}

function conflictItemTimeText(item: { startTime?: string; endTime?: string }) {
  const start = item.startTime ? dayjs(item.startTime).format('MM-DD HH:mm') : '--'
  const end = item.endTime ? dayjs(item.endTime).format('MM-DD HH:mm') : '--'
  return `${start} ~ ${end}`
}

function focusConflictDate(startTime?: string) {
  const normalized = normalizeDateTimeValue(startTime)
  if (!normalized) return
  focusCalendarDate(dayjs(normalized).format('YYYY-MM-DD'))
}

function showAllCollaboratorPreview() {
  if (!collaboratorPreviewAllNames.value.length) {
    message.info('暂无协同成员')
    return
  }
  Modal.info({
    title: `协同成员清单（${collaboratorPreviewAllNames.value.length} 人）`,
    content: collaboratorPreviewAllNames.value.join('、'),
    width: 680
  })
}

async function previewScheduleConflicts() {
  if (!form.startTime) {
    message.warning('请先选择开始时间')
    return
  }
  const collaboratorPayload = form.calendarType === 'COLLAB'
    ? await resolveCollaborators(form.collaboratorDeptIds, form.collaboratorIds)
    : { ids: [] as string[], names: [] as string[] }
  const payload = {
    taskId: editingTaskId.value || undefined,
    startTime: dayjs(form.startTime).format('YYYY-MM-DDTHH:mm:ss'),
    endTime: form.endTime ? dayjs(form.endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
    assigneeName: form.assigneeName || undefined,
    collaboratorIds: collaboratorPayload.ids
  }
  conflictPreviewLoading.value = true
  try {
    const conflicts = await checkOaTaskConflicts(payload)
    conflictPreviewItems.value = conflicts || []
    if (!conflicts?.length) {
      message.success('预检通过，未发现冲突')
    }
  } catch {
    message.warning('冲突预检失败，请稍后重试')
  } finally {
    conflictPreviewLoading.value = false
  }
}

async function confirmByConflicts(payload: { taskId?: string | number; startTime: string; endTime?: string; assigneeName?: string; collaboratorIds: string[] }) {
  if (conflictPolicy.value === 'ALLOW') return true
  try {
    const conflicts = await checkOaTaskConflicts({
      taskId: payload.taskId,
      startTime: payload.startTime,
      endTime: payload.endTime,
      assigneeName: payload.assigneeName,
      collaboratorIds: payload.collaboratorIds
    })
    conflictPreviewItems.value = conflicts || []
    if (!conflicts?.length) return true
    if (conflictPolicy.value === 'BLOCK') {
      message.error(`检测到 ${conflicts.length} 条冲突，当前策略不允许保存`)
      return false
    }
    return await new Promise<boolean>((resolve) => {
      Modal.confirm({
        title: `检测到 ${conflicts.length} 条潜在冲突`,
        content: buildConflictMessage(conflicts),
        okText: '仍然保存',
        cancelText: '取消',
        onOk: () => resolve(true),
        onCancel: () => resolve(false)
      })
    })
  } catch {
    message.warning('冲突检测失败，已跳过冲突提示')
    return true
  }
}

let collaboratorPreviewSeq = 0
async function refreshCollaboratorPreview() {
  if (form.calendarType !== 'COLLAB') {
    collaboratorPreviewTotal.value = 0
    collaboratorPreviewNames.value = []
    collaboratorPreviewAllNames.value = []
    collaboratorPreviewLoading.value = false
    return
  }
  const currentSeq = ++collaboratorPreviewSeq
  collaboratorPreviewLoading.value = true
  try {
    const result = await resolveCollaborators(form.collaboratorDeptIds, form.collaboratorIds)
    if (currentSeq !== collaboratorPreviewSeq) return
    collaboratorPreviewTotal.value = result.ids.length
    collaboratorPreviewAllNames.value = result.names
    collaboratorPreviewNames.value = result.names.slice(0, 6)
  } finally {
    if (currentSeq === collaboratorPreviewSeq) {
      collaboratorPreviewLoading.value = false
    }
  }
}

async function fetchAll() {
  const [taskRows] = await Promise.all([
    getOaTaskCalendar({
      status: query.status,
      calendarType: query.calendarType,
      urgency: query.urgency,
      assigneeName: query.assigneeName || undefined,
      startDate: query.range?.[0] ? dayjs(query.range[0]).format('YYYY-MM-DD') : undefined,
      endDate: query.range?.[1] ? dayjs(query.range[1]).format('YYYY-MM-DD') : undefined
    }),
    fetchBirthdayReminders()
  ])
  rows.value = taskRows
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
  form.assigneeId = userStore.staffInfo?.id != null ? String(userStore.staffInfo.id) : undefined
  form.assigneeName = (userStore.staffInfo?.realName || userStore.staffInfo?.username || '').trim()
  form.priority = 'NORMAL'
  form.description = ''
  form.calendarType = 'WORK'
  form.urgency = 'NORMAL'
  form.planCategory = '基础办公'
  form.eventColor = resolveCalendarTypeColor(form.calendarType, form.urgency)
  form.collaboratorDeptIds = []
  form.collaboratorIds = []
  originalCollaboratorIds.value = []
  collaboratorPreviewNames.value = []
  collaboratorPreviewAllNames.value = []
  conflictPreviewItems.value = []
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
  form.assigneeId = item.assigneeId != null ? String(item.assigneeId) : undefined
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
  originalCollaboratorIds.value = [...form.collaboratorIds]
  collaboratorPreviewAllNames.value = []
  conflictPreviewItems.value = []
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
      emitCalendarSyncPulse('DONE')
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
      emitCalendarSyncPulse('DELETE')
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
  const firstStart = dayjs(form.startTime).format('YYYY-MM-DDTHH:mm:ss')
  const firstEnd = form.endTime ? dayjs(form.endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined
  const canContinue = await confirmByConflicts({
    taskId: editingTaskId.value || undefined,
    startTime: firstStart,
    endTime: firstEnd,
    assigneeName: form.assigneeName || undefined,
    collaboratorIds: collaboratorPayload.ids
  })
  if (!canContinue) return

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
        assigneeId: resolveAssigneeIdForSubmit(),
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
      conflictPreviewItems.value = []
      emitCalendarSyncPulse('UPDATE')
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
          assigneeId: resolveAssigneeIdForSubmit(),
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
    conflictPreviewItems.value = []
    emitCalendarSyncPulse('CREATE')
    message.success(form.recurring ? `已生成 ${tasks.length} 条周期日程` : '日程已新增')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  syncVisibleTypesFromStorage()
  syncCalendarViewFromStorage()
  syncConflictPolicyFromStorage()
  await Promise.all([fetchAll(), fetchStaffOptions(), fetchDepartmentOptions()])
  syncTimer = window.setInterval(() => {
    fetchAll().catch(() => {})
  }, 15000)
  calendarStorageHandler = (event: StorageEvent) => {
    if (event.key === VISIBLE_TYPES_STORAGE_KEY) {
      syncVisibleTypesFromStorage()
      return
    }
    if (event.key === CALENDAR_VIEW_STORAGE_KEY) {
      syncCalendarViewFromStorage()
      return
    }
    if (event.key === CONFLICT_POLICY_STORAGE_KEY) {
      syncConflictPolicyFromStorage()
      return
    }
    if (event.key === CALENDAR_SYNC_PULSE_KEY) {
      fetchAll().catch(() => {})
    }
  }
  window.addEventListener('storage', calendarStorageHandler)
})

onBeforeUnmount(() => {
  if (syncTimer) window.clearInterval(syncTimer)
  if (calendarStorageHandler) window.removeEventListener('storage', calendarStorageHandler)
  calendarStorageHandler = null
})

watch(
  () => visibleCalendarTypes.value,
  (value) => {
    try {
      localStorage.setItem(VISIBLE_TYPES_STORAGE_KEY, JSON.stringify(value))
    } catch {}
  },
  { deep: true }
)

watch(
  () => currentCalendarView.value,
  (value) => {
    try {
      localStorage.setItem(CALENDAR_VIEW_STORAGE_KEY, value)
    } catch {}
  }
)

watch(
  () => conflictPolicy.value,
  (value) => {
    try {
      localStorage.setItem(CONFLICT_POLICY_STORAGE_KEY, value)
    } catch {}
  }
)

watch(
  () => [
    form.calendarType,
    form.collaboratorDeptIds.join(','),
    form.collaboratorIds.join(',')
  ],
  () => {
    refreshCollaboratorPreview().catch(() => {})
  },
  { immediate: true }
)

watch(
  () => [
    form.startTime ? dayjs(form.startTime).format('YYYY-MM-DDTHH:mm:ss') : '',
    form.endTime ? dayjs(form.endTime).format('YYYY-MM-DDTHH:mm:ss') : '',
    form.assigneeName,
    form.collaboratorIds.join(','),
    form.collaboratorDeptIds.join(',')
  ],
  () => {
    conflictPreviewItems.value = []
  }
)

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
  margin-bottom: 12px;
  padding: 10px 12px;
  border: 1px solid #dbeafe;
  border-radius: 12px;
  background: linear-gradient(135deg, #f7fbff 0%, #eef6ff 100%);
}

.calendar-title-tip {
  color: #64748b;
  font-size: 12px;
}

.calendar-insights {
  margin-top: 6px;
}

.calendar-loads {
  margin-top: 2px;
}

.calendar-week-trend {
  display: flex;
  align-items: flex-end;
  gap: 6px;
}

.calendar-week-bar {
  min-width: 16px;
  padding: 2px 4px;
  border-radius: 6px 6px 2px 2px;
  background: linear-gradient(180deg, #91caff 0%, #1677ff 100%);
  color: #fff;
  font-size: 11px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
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

.readonly-tip {
  color: #64748b;
  font-size: 12px;
}

.calendar-surface :deep(.fc) {
  --fc-border-color: #e2e8f0;
  --fc-page-bg-color: transparent;
  --fc-neutral-bg-color: #f8fafc;
  --fc-event-border-color: transparent;
}

.calendar-surface :deep(.fc .fc-toolbar.fc-header-toolbar) {
  margin-bottom: 10px;
  padding: 8px 10px;
  border: 1px solid #dbeafe;
  border-radius: 10px;
  background: #f8fbff;
}

.calendar-surface :deep(.fc .fc-button) {
  border-radius: 999px;
  border: 1px solid #bfdbfe;
  background: #ffffff;
  color: #1e3a8a;
  box-shadow: none;
}

.calendar-surface :deep(.fc .fc-button:hover) {
  background: #eff6ff;
  border-color: #93c5fd;
}

.calendar-surface :deep(.fc .fc-col-header-cell-cushion) {
  color: #334155;
  font-weight: 600;
}

.calendar-surface :deep(.fc .fc-daygrid-day-number) {
  color: #64748b;
  font-weight: 600;
}

.calendar-surface :deep(.fc .fc-daygrid-day.fc-day-today) {
  background: rgba(219, 234, 254, 0.56);
}

.calendar-surface :deep(.fc .fc-daygrid-event) {
  border-radius: 8px;
  padding: 1px 4px;
  box-shadow: 0 2px 8px rgba(30, 64, 175, 0.14);
}

.calendar-surface :deep(.calendar-event-done .fc-event-title) {
  text-decoration: line-through;
}

.calendar-surface :deep(.calendar-event-done) {
  opacity: 0.82;
}
</style>
