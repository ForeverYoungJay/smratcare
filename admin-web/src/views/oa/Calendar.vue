<template>
  <PageContainer title="协同日历" subTitle="与任务联动的月/周视图">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="负责人">
        <a-input v-model:value="query.assigneeName" placeholder="负责人" allow-clear />
      </a-form-item>
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
    </SearchForm>
    <a-card class="card-elevated" :bordered="false">
      <FullCalendar :options="calendarOptions" />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import { getOaTaskCalendar } from '../../api/oa'
import type { OaTask } from '../../types'

const rows = ref<OaTask[]>([])
const query = reactive({
  status: undefined as string | undefined,
  assigneeName: '',
  range: undefined as [Dayjs, Dayjs] | undefined
})

const statusOptions = [
  { label: '进行中', value: 'OPEN' },
  { label: '已完成', value: 'DONE' }
]

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
  events: rows.value.map((task) => ({
    id: String(task.id),
    title: `${task.title}${task.assigneeName ? `（${task.assigneeName}）` : ''}`,
    start: task.startTime || task.endTime,
    end: task.endTime || task.startTime,
    color: task.status === 'DONE' ? '#52c41a' : '#1677ff'
  }))
}))

async function fetchData() {
  rows.value = await getOaTaskCalendar({
    status: query.status,
    assigneeName: query.assigneeName || undefined,
    startDate: query.range?.[0] ? dayjs(query.range[0]).format('YYYY-MM-DD') : undefined,
    endDate: query.range?.[1] ? dayjs(query.range[1]).format('YYYY-MM-DD') : undefined
  })
}

function onReset() {
  query.status = undefined
  query.assigneeName = ''
  query.range = undefined
  fetchData()
}

fetchData()
</script>
