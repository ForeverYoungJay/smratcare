<template>
  <PageContainer title="行政日历" subTitle="支持个人/工作/日常/协同日历，含紧急颜色和周期计划">
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
          <a-button type="primary" @click="openCreate('TASK')">新增日程</a-button>
          <a-button danger @click="openCreate('HOLIDAY')">新增节假日/重大节日</a-button>
          <a-button @click="fetchAll">刷新</a-button>
        </a-space>
      </template>
    </SearchForm>

    <a-card class="card-elevated" :bordered="false">
      <div class="legend-row">
        <span class="legend-item"><i class="legend-dot legend-dot-blue"></i>工作/个人</span>
        <span class="legend-item"><i class="legend-dot legend-dot-green"></i>已完成</span>
        <span class="legend-item"><i class="legend-dot legend-dot-red"></i>紧急/节假日</span>
        <span class="legend-item"><i class="legend-dot legend-dot-orange"></i>生日提醒</span>
      </div>
      <FullCalendar :options="calendarOptions" />
    </a-card>

    <a-drawer
      v-model:open="dayDrawerOpen"
      :title="selectedDate ? `${selectedDate.format('YYYY-MM-DD')} 日计划` : '日计划'"
      width="560"
    >
      <div class="drawer-actions">
        <a-space wrap>
          <a-button type="primary" @click="openCreate('TASK', selectedDate || dayjs())">新增该日计划</a-button>
          <a-button @click="quickCreateTemplate('基础办公')">基础办公</a-button>
          <a-button @click="quickCreateTemplate('行政日常')">行政日常</a-button>
          <a-button @click="quickCreateTemplate('协同会议')">协同会议</a-button>
        </a-space>
      </div>
      <a-list :data-source="selectedDayEvents" :locale="{ emptyText: '当天暂无计划，可点击上方快速创建' }" item-layout="vertical">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta>
              <template #title>
                <a-space wrap>
                  <span>{{ item.title }}</span>
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
          </a-list-item>
        </template>
      </a-list>
    </a-drawer>

    <a-modal
      v-model:open="editOpen"
      :title="form.type === 'HOLIDAY' ? '新增节假日/重大节日' : '新增行政日程'"
      @ok="submit"
      :confirm-loading="saving"
      width="760px"
    >
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="标题" required>
              <a-input v-model:value="form.title" :placeholder="form.type === 'HOLIDAY' ? '例如：中秋活动日/消防演练日' : '例如：月度盘点/家属沟通会'" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="计划分类">
              <a-select v-model:value="form.planCategory" allow-clear placeholder="选择分类">
                <a-select-option v-for="item in planCategoryOptions" :key="item" :value="item">{{ item }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="日历种类">
              <a-select v-model:value="form.calendarType" :options="calendarTypeOptions" />
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
              <a-input v-model:value="form.assigneeName" placeholder="例如：行政部/护理部" />
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
            <a-form-item label="显示颜色">
              <a-select v-model:value="form.eventColor" :options="colorOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="协同部门" v-if="form.calendarType === 'COLLAB'">
              <a-select v-model:value="form.collaboratorDeptId" allow-clear :options="departmentOptions" placeholder="先选部门再选人" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12" v-if="form.calendarType === 'COLLAB'">
          <a-col :span="24">
            <a-form-item label="协同成员">
              <a-select
                v-model:value="form.collaboratorIds"
                mode="multiple"
                allow-clear
                :options="filteredStaffOptions"
                placeholder="邀请其他人后会自动在协同日历展示"
                option-filter-prop="label"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-card size="small" title="周期计划（可一键设立）" class="repeat-card">
          <a-row :gutter="12">
            <a-col :span="8">
              <a-form-item label="周期类型">
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
              <div class="repeat-tip">开启后将按频率一次性生成多条行政日历计划，便于日常办公安排。</div>
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
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import { getOaTaskCalendar, createOaTask } from '../../api/oa'
import { getElderPage } from '../../api/elder'
import { getDepartmentPage, getStaffPage } from '../../api/rbac'
import { useUserStore } from '../../stores/user'
import type { DepartmentItem, OaTask, ElderItem, PageResult, StaffItem } from '../../types'

type EventKind = 'TASK' | 'HOLIDAY' | 'BIRTHDAY'
type CalendarType = 'PERSONAL' | 'WORK' | 'DAILY' | 'COLLAB'
type UrgencyType = 'NORMAL' | 'EMERGENCY'
type RecurrenceType = 'DAILY' | 'WEEKLY' | 'MONTHLY'

const userStore = useUserStore()
const rows = ref<OaTask[]>([])
const elders = ref<ElderItem[]>([])
const staffOptions = ref<{ label: string; value: string }[]>([])
const departmentOptions = ref<{ label: string; value: string }[]>([])
const staffDeptMap = ref<Record<string, string | undefined>>({})
const saving = ref(false)
const editOpen = ref(false)
const dayDrawerOpen = ref(false)
const selectedDate = ref<Dayjs>()
let syncTimer: number | undefined

const query = reactive({
  status: undefined as string | undefined,
  calendarType: undefined as CalendarType | undefined,
  urgency: undefined as UrgencyType | undefined,
  assigneeName: '',
  range: undefined as [Dayjs, Dayjs] | undefined
})

const form = reactive({
  type: 'TASK' as EventKind,
  title: '',
  planCategory: '基础办公',
  calendarType: 'WORK' as CalendarType,
  urgency: 'NORMAL' as UrgencyType,
  eventColor: '#1677ff',
  startTime: undefined as Dayjs | undefined,
  endTime: undefined as Dayjs | undefined,
  assigneeName: '',
  priority: 'NORMAL',
  description: '',
  collaboratorDeptId: undefined as string | undefined,
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
  { label: '工作', value: 'WORK' },
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

const colorOptions = [
  { label: '蓝色（默认）', value: '#1677ff' },
  { label: '绿色（完成）', value: '#52c41a' },
  { label: '橙色（提醒）', value: '#faad14' },
  { label: '红色（紧急）', value: '#ff4d4f' },
  { label: '紫色（协同）', value: '#722ed1' }
]

const planCategoryOptions = ['基础办公', '行政日常', '协同会议', '后勤排班', '专项检查']

const majorFestivalEvents = computed(() => {
  const year = (query.range?.[0] || dayjs()).year()
  const items = [
    { date: `${year}-01-01`, title: '元旦' },
    { date: `${year}-05-01`, title: '劳动节' },
    { date: `${year}-10-01`, title: '国庆节' },
    { date: `${year}-12-31`, title: '年终总结日' }
  ]
  return items.map((item) => ({
    id: `festival-${item.date}`,
    title: `【重大节日】${item.title}`,
    start: `${item.date}T00:00:00`,
    end: `${item.date}T23:59:59`,
    allDay: true,
    color: '#f5222d',
    extendedProps: { type: 'HOLIDAY' }
  }))
})

const birthdayEvents = computed(() => {
  const currentYear = (query.range?.[0] || dayjs()).year()
  return elders.value
    .filter((elder) => !!elder.birthDate)
    .map((elder) => {
      const birth = dayjs(elder.birthDate)
      const date = dayjs(`${currentYear}-${birth.format('MM-DD')}`).format('YYYY-MM-DD')
      return {
        id: `birthday-${elder.id}-${date}`,
        title: `🎂 生日提醒：${elder.fullName}`,
        start: `${date}T08:00:00`,
        end: `${date}T20:00:00`,
        color: '#fa8c16',
        extendedProps: { type: 'BIRTHDAY', elderId: elder.id }
      }
    })
})

const selectedDayEvents = computed(() => {
  if (!selectedDate.value) return []
  const selected = selectedDate.value.format('YYYY-MM-DD')
  return rows.value
    .filter((item) => {
      if (!item.startTime) return false
      return dayjs(item.startTime).format('YYYY-MM-DD') === selected
    })
    .map((item) => ({
      ...item,
      collaboratorNames: normalizeCollaboratorNames(item.collaboratorNames)
    }))
})

const filteredStaffOptions = computed(() => {
  if (!form.collaboratorDeptId) return staffOptions.value
  return staffOptions.value.filter((item) => staffDeptMap.value[item.value] === form.collaboratorDeptId)
})

watch(
  () => form.collaboratorDeptId,
  (deptId) => {
    if (!deptId) return
    form.collaboratorIds = form.collaboratorIds.filter((staffId) => staffDeptMap.value[staffId] === deptId)
  }
)

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  locale: 'zh-cn',
  height: 'auto',
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,dayGridWeek'
  },
  buttonText: {
    today: '今天',
    month: '月视图',
    week: '周视图'
  },
  dateClick: (arg: { dateStr: string }) => {
    openDayDrawer(dayjs(arg.dateStr))
  },
  eventClick: (arg: { event: { start: Date | null } }) => {
    if (!arg.event.start) return
    openDayDrawer(dayjs(arg.event.start))
  },
  events: [
    ...rows.value.map((task) => {
      const isHoliday = (task.title || '').startsWith('【节假日】') || (task.title || '').startsWith('【重大节日】')
      const collaboratorNames = normalizeCollaboratorNames(task.collaboratorNames)
      const suffix = [task.assigneeName, collaboratorNames.length ? `协同:${collaboratorNames.join('、')}` : undefined].filter(Boolean).join(' | ')
      return {
        id: String(task.id),
        title: `${task.title}${suffix ? `（${suffix}）` : ''}`,
        start: task.startTime || task.endTime,
        end: task.endTime || task.startTime,
        color: eventColor(task, isHoliday),
        extendedProps: {
          type: isHoliday ? 'HOLIDAY' : 'TASK',
          calendarType: task.calendarType,
          urgency: task.urgency,
          planCategory: task.planCategory
        }
      }
    }),
    ...majorFestivalEvents.value,
    ...birthdayEvents.value
  ]
}))

function calendarTypeText(value?: string) {
  if (value === 'PERSONAL') return '个人'
  if (value === 'DAILY') return '日常计划'
  if (value === 'COLLAB') return '协同日历'
  return '工作'
}

function eventColor(task: OaTask, isHoliday: boolean) {
  if (isHoliday) return '#f5222d'
  if (task.eventColor) return task.eventColor
  if (task.urgency === 'EMERGENCY') return '#ff4d4f'
  if (task.status === 'DONE') return '#52c41a'
  if (task.calendarType === 'COLLAB') return '#722ed1'
  return '#1677ff'
}

function normalizeCollaboratorNames(input: string[] | string | undefined) {
  if (!input) return [] as string[]
  if (Array.isArray(input)) {
    return input.filter((item) => !!item)
  }
  return String(input)
    .split(',')
    .map((item) => item.trim())
    .filter((item) => !!item)
}

function openDayDrawer(date: Dayjs) {
  selectedDate.value = date
  dayDrawerOpen.value = true
}

function formatRange(startTime?: string, endTime?: string) {
  if (!startTime) return '-'
  const start = dayjs(startTime)
  const end = endTime ? dayjs(endTime) : undefined
  if (!end) return start.format('HH:mm')
  return `${start.format('HH:mm')} - ${end.format('HH:mm')}`
}

function quickCreateTemplate(category: string) {
  openCreate('TASK', selectedDate.value || dayjs())
  form.planCategory = category
  form.calendarType = category === '协同会议' ? 'COLLAB' : category === '行政日常' ? 'DAILY' : 'WORK'
  form.title = category
}

function onUrgencyChange(value: UrgencyType) {
  if (value === 'EMERGENCY') {
    form.eventColor = '#ff4d4f'
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
  const elderPage: PageResult<ElderItem> = await getElderPage({ pageNo: 1, pageSize: 500 })
  elders.value = elderPage.list || []
}

async function fetchStaffOptions() {
  const page: PageResult<StaffItem> = await getStaffPage({ pageNo: 1, pageSize: 300 })
  staffDeptMap.value = {}
  ;(page.list || []).forEach((item) => {
    staffDeptMap.value[String(item.id)] = item.departmentId == null ? undefined : String(item.departmentId)
  })
  staffOptions.value = (page.list || []).map((item) => ({
    label: item.realName || item.username || `员工#${item.id}`,
    value: String(item.id)
  }))
}

async function fetchDepartmentOptions() {
  const page: PageResult<DepartmentItem> = await getDepartmentPage({ pageNo: 1, pageSize: 200 })
  departmentOptions.value = (page.list || []).map((item) => ({ label: item.deptName, value: String(item.id) }))
}

function onReset() {
  query.status = undefined
  query.calendarType = undefined
  query.urgency = undefined
  query.assigneeName = ''
  query.range = undefined
  fetchAll()
}

function openCreate(type: EventKind, date?: Dayjs) {
  form.type = type
  form.title = ''
  form.planCategory = '基础办公'
  form.calendarType = type === 'HOLIDAY' ? 'DAILY' : 'WORK'
  form.urgency = type === 'HOLIDAY' ? 'EMERGENCY' : 'NORMAL'
  form.eventColor = type === 'HOLIDAY' ? '#ff4d4f' : '#1677ff'
  form.startTime = date ? date.hour(9).minute(0).second(0) : undefined
  form.endTime = date ? date.hour(10).minute(0).second(0) : undefined
  form.assigneeName = (userStore.staffInfo?.realName || userStore.staffInfo?.username || '').trim()
  form.priority = type === 'HOLIDAY' ? 'HIGH' : 'NORMAL'
  form.description = ''
  form.collaboratorDeptId = undefined
  form.collaboratorIds = []
  form.recurring = false
  form.recurrenceRule = 'WEEKLY'
  form.recurrenceInterval = 1
  form.recurrenceCount = 4
  editOpen.value = true
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
  const collaboratorNames = staffOptions.value
    .filter((item) => form.collaboratorIds.includes(item.value))
    .map((item) => item.label)
  const repeatCount = form.recurring ? Math.max(1, Number(form.recurrenceCount || 1)) : 1
  const repeatRule = form.recurrenceRule
  const repeatInterval = Math.max(1, Number(form.recurrenceInterval || 1))

  saving.value = true
  try {
    const tasks: Promise<any>[] = []
    for (let i = 0; i < repeatCount; i++) {
      const startTime = i === 0 ? form.startTime : addByRule(form.startTime, repeatRule, repeatInterval * i)
      const endTime = form.endTime ? (i === 0 ? form.endTime : addByRule(form.endTime, repeatRule, repeatInterval * i)) : undefined
      const title = form.type === 'HOLIDAY' ? `【节假日】${form.title.trim()}` : form.title.trim()
      tasks.push(
        createOaTask({
          title,
          description: form.description || undefined,
          startTime: dayjs(startTime).format('YYYY-MM-DDTHH:mm:ss'),
          endTime: endTime ? dayjs(endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
          priority: form.priority,
          status: 'OPEN',
          assigneeName: form.assigneeName || undefined,
          calendarType: form.calendarType,
          planCategory: form.planCategory || undefined,
          urgency: form.urgency,
          eventColor: form.eventColor,
          collaboratorIds: form.calendarType === 'COLLAB' ? form.collaboratorIds : [],
          collaboratorNames: form.calendarType === 'COLLAB' ? collaboratorNames : [],
          recurring: form.recurring,
          recurrenceRule: form.recurring ? repeatRule : undefined,
          recurrenceInterval: form.recurring ? repeatInterval : undefined,
          recurrenceCount: form.recurring ? repeatCount : undefined
        })
      )
    }

    await Promise.all(tasks)
    message.success(form.recurring ? `已按周期生成 ${tasks.length} 条行政计划` : form.type === 'HOLIDAY' ? '节假日/重大节日已记录' : '行政日程已记录')
    editOpen.value = false
    await fetchAll()
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchAll(), fetchStaffOptions(), fetchDepartmentOptions()])
  syncTimer = window.setInterval(() => {
    fetchAll()
  }, 15000)
})

onBeforeUnmount(() => {
  if (syncTimer) {
    window.clearInterval(syncTimer)
  }
})
</script>

<style scoped>
.legend-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 10px;
  color: #475569;
  font-size: 12px;
}
.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}
.legend-dot-blue {
  background: #1677ff;
}
.legend-dot-green {
  background: #52c41a;
}
.legend-dot-red {
  background: #ff4d4f;
}
.legend-dot-orange {
  background: #fa8c16;
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
</style>
