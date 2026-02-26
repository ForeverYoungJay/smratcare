<template>
  <PageContainer title="行政日历" subTitle="按天记录行政事项，支持特殊节假日/重大节日标注，并提醒老人生日">
    <SearchForm :model="query" @search="fetchAll" @reset="onReset">
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
        <a-button type="primary" @click="openCreate('TASK')">新增日程</a-button>
        <a-button danger @click="openCreate('HOLIDAY')">新增节假日/重大节日</a-button>
      </template>
    </SearchForm>

    <a-card class="card-elevated" :bordered="false">
      <FullCalendar :options="calendarOptions" />
    </a-card>

    <a-modal v-model:open="editOpen" :title="form.type === 'HOLIDAY' ? '新增节假日/重大节日' : '新增行政日程'" @ok="submit" :confirm-loading="saving" width="640px">
      <a-form layout="vertical">
        <a-form-item label="标题" required>
          <a-input v-model:value="form.title" :placeholder="form.type === 'HOLIDAY' ? '例如：中秋活动日/消防演练日' : '例如：月度盘点/家属沟通会'" />
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
              <a-input v-model:value="form.assigneeName" placeholder="例如：行政部/护理部" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="优先级">
              <a-select v-model:value="form.priority" :options="priorityOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.description" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
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
import type { OaTask, ElderItem, PageResult } from '../../types'

type EventKind = 'TASK' | 'HOLIDAY' | 'BIRTHDAY'

const rows = ref<OaTask[]>([])
const elders = ref<ElderItem[]>([])
const saving = ref(false)
const editOpen = ref(false)
const query = reactive({
  status: undefined as string | undefined,
  assigneeName: '',
  range: undefined as [Dayjs, Dayjs] | undefined
})

const form = reactive({
  type: 'TASK' as EventKind,
  title: '',
  startTime: undefined as Dayjs | undefined,
  endTime: undefined as Dayjs | undefined,
  assigneeName: '',
  priority: 'NORMAL',
  description: ''
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
  dateClick: (arg: any) => {
    openCreate('TASK', dayjs(arg.dateStr))
  },
  events: [
    ...rows.value.map((task) => {
      const isHoliday = (task.title || '').startsWith('【节假日】') || (task.title || '').startsWith('【重大节日】')
      return {
        id: String(task.id),
        title: `${task.title}${task.assigneeName ? `（${task.assigneeName}）` : ''}`,
        start: task.startTime || task.endTime,
        end: task.endTime || task.startTime,
        color: isHoliday ? '#f5222d' : task.status === 'DONE' ? '#52c41a' : '#1677ff',
        extendedProps: { type: isHoliday ? 'HOLIDAY' : 'TASK' }
      }
    }),
    ...majorFestivalEvents.value,
    ...birthdayEvents.value
  ]
}))

async function fetchAll() {
  rows.value = await getOaTaskCalendar({
    status: query.status,
    assigneeName: query.assigneeName || undefined,
    startDate: query.range?.[0] ? dayjs(query.range[0]).format('YYYY-MM-DD') : undefined,
    endDate: query.range?.[1] ? dayjs(query.range[1]).format('YYYY-MM-DD') : undefined
  })
  const elderPage: PageResult<ElderItem> = await getElderPage({ pageNo: 1, pageSize: 500 })
  elders.value = elderPage.list || []
}

function onReset() {
  query.status = undefined
  query.assigneeName = ''
  query.range = undefined
  fetchAll()
}

function openCreate(type: EventKind, date?: Dayjs) {
  form.type = type
  form.title = ''
  form.startTime = date ? date.hour(9).minute(0).second(0) : undefined
  form.endTime = date ? date.hour(10).minute(0).second(0) : undefined
  form.assigneeName = type === 'HOLIDAY' ? '行政部' : ''
  form.priority = type === 'HOLIDAY' ? 'HIGH' : 'NORMAL'
  form.description = ''
  editOpen.value = true
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
  saving.value = true
  try {
    const title = form.type === 'HOLIDAY' ? `【节假日】${form.title.trim()}` : form.title.trim()
    await createOaTask({
      title,
      description: form.description || undefined,
      startTime: dayjs(form.startTime).format('YYYY-MM-DDTHH:mm:ss'),
      endTime: form.endTime ? dayjs(form.endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
      priority: form.priority,
      status: 'OPEN',
      assigneeName: form.assigneeName || undefined
    })
    message.success(form.type === 'HOLIDAY' ? '节假日/重大节日已记录' : '行政日程已记录')
    editOpen.value = false
    await fetchAll()
  } finally {
    saving.value = false
  }
}

fetchAll()
</script>
