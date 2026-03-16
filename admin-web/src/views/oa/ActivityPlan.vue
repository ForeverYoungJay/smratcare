<template>
  <PageContainer title="活动计划" subTitle="活动排期、组织安排与执行跟踪">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="快捷筛选">
        <a-space wrap>
          <a-checkable-tag :checked="quickFilter === 'ALL'" @change="() => applyQuickFilter('ALL')">全部日程</a-checkable-tag>
          <a-checkable-tag :checked="quickFilter === 'TODAY'" @change="() => applyQuickFilter('TODAY')">今日</a-checkable-tag>
          <a-checkable-tag :checked="quickFilter === 'UPCOMING_7D'" @change="() => applyQuickFilter('UPCOMING_7D')">近7天</a-checkable-tag>
          <a-checkable-tag :checked="quickFilter === 'DONE'" @change="() => applyQuickFilter('DONE')">已完成</a-checkable-tag>
          <a-checkable-tag :checked="quickFilter === 'CANCELLED'" @change="() => applyQuickFilter('CANCELLED')">已取消</a-checkable-tag>
        </a-space>
      </a-form-item>
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="计划标题/组织人/地点" allow-clear />
      </a-form-item>
      <a-form-item label="组织人">
        <a-input v-model:value="query.organizer" placeholder="请输入组织人" allow-clear />
      </a-form-item>
      <a-form-item label="地点">
        <a-input v-model:value="query.location" placeholder="请输入地点" allow-clear />
      </a-form-item>
      <a-form-item label="参与对象">
        <a-input v-model:value="query.participantTarget" placeholder="如：全院长者/护理一区" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 160px" :options="statusOptions" />
      </a-form-item>
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <template #extra>
        <a-segmented
          v-model:value="viewMode"
          :options="viewModeOptions"
          style="margin-right: 8px;"
        />
        <a-button type="primary" @click="openEdit()">新增计划</a-button>
        <a-button :disabled="!selectedSingleRecord || !canEditSelected" @click="editSelected">编辑</a-button>
        <a-button :disabled="!selectedSingleRecord || !canStartSelected" @click="startSelected">开始</a-button>
        <a-button :disabled="!selectedSingleRecord || !canDoneSelected" @click="doneSelected">完成</a-button>
        <a-button :disabled="!selectedSingleRecord || !canCancelSelected" @click="cancelSelected">取消</a-button>
        <a-button :disabled="!selectedSingleRecord" danger @click="removeSelected">删除</a-button>
        <a-button :disabled="selectedStartIds.length === 0" @click="batchStart">批量开始</a-button>
        <a-button :disabled="selectedDoneIds.length === 0" @click="batchDone">批量完成</a-button>
        <a-button :disabled="selectedCancelIds.length === 0" @click="batchCancel">批量取消</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchRemove">批量删除</a-button>
        <a-button @click="downloadExport">导出CSV</a-button>
        <span class="selection-tip">已勾选 {{ selectedRowKeys.length }} 条，批量状态流转按当前状态自动过滤</span>
      </template>
    </SearchForm>

    <DataTable
      v-if="viewMode === 'LIST'"
      rowKey="id"
      :row-selection="rowSelection"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
      </template>
    </DataTable>

    <a-card v-else class="calendar-card" :bordered="false">
      <div>
        <div class="calendar-toolbar">
          <a-segmented v-model:value="calendarGranularity" :options="calendarGranularityOptions" />
          <a-space>
            <a-button size="small" @click="moveCalendar(-1)">上一{{ calendarUnitLabel }}</a-button>
            <a-button size="small" @click="jumpCalendarToday">今天</a-button>
            <a-button size="small" @click="moveCalendar(1)">下一{{ calendarUnitLabel }}</a-button>
          </a-space>
        </div>

        <a-calendar
          v-if="calendarGranularity === 'MONTH'"
          v-model:value="calendarCursor"
          :fullscreen="false"
          @select="onCalendarSelect"
        >
          <template #dateCellRender="{ current }">
            <ul class="calendar-day-list">
              <li
                v-for="item in activityPlansForDate(current)"
                :key="String(item.id)"
                class="calendar-day-item"
                :class="`status-${String(item.status || 'PLANNED').toLowerCase()}`"
              >
                <span class="calendar-day-title">{{ item.title }}</span>
                <span class="calendar-day-meta">{{ item.organizer || item.location || statusText(item.status) }}</span>
              </li>
            </ul>
          </template>
        </a-calendar>

        <div v-else-if="calendarGranularity === 'WEEK'" class="week-board">
          <div v-for="day in weekDays" :key="day.dateKey" class="week-column">
            <div class="week-column-head" @click="onCalendarSelect(day.date)">
              <strong>{{ day.label }}</strong>
              <span>{{ day.date.format('MM-DD') }}</span>
            </div>
            <a-empty v-if="day.items.length === 0" :image="false" description="暂无" />
            <div v-else class="week-column-list">
              <div
                v-for="item in day.items"
                :key="String(item.id)"
                class="calendar-plan-card compact"
              >
                <div class="calendar-plan-top">
                  <strong>{{ item.title }}</strong>
                  <a-tag :color="statusColor(item.status)">{{ statusText(item.status) }}</a-tag>
                </div>
                <div class="calendar-plan-meta">{{ calendarTimeText(item) }}</div>
                <div class="calendar-plan-meta">{{ item.location || item.organizer || '-' }}</div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="day-board">
          <div class="day-board-head">
            <strong>{{ calendarSelectedDate.format('YYYY-MM-DD dddd') }}</strong>
            <span class="selection-tip">共 {{ selectedCalendarPlans.length }} 条</span>
          </div>
          <a-empty v-if="selectedCalendarPlans.length === 0" description="当日暂无匹配日程" />
          <div v-else class="calendar-plan-list">
            <div
              v-for="item in selectedCalendarPlans"
              :key="String(item.id)"
              class="calendar-plan-card"
            >
              <div class="calendar-plan-top">
                <strong>{{ item.title }}</strong>
                <a-tag :color="statusColor(item.status)">{{ statusText(item.status) }}</a-tag>
              </div>
              <div class="calendar-plan-meta">组织人：{{ item.organizer || '-' }}</div>
              <div class="calendar-plan-meta">地点：{{ item.location || '-' }}</div>
              <div class="calendar-plan-meta">参与对象：{{ item.participantTarget || '-' }}</div>
              <div class="calendar-plan-meta">时间：{{ calendarTimeText(item) }}</div>
              <div v-if="item.content" class="calendar-plan-content">{{ item.content }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="calendar-side">
        <div class="calendar-side-head">
          <strong>{{ calendarSideTitle }}</strong>
          <span class="selection-tip">共 {{ selectedCalendarPlans.length }} 条</span>
        </div>
        <a-empty v-if="selectedCalendarPlans.length === 0" description="当日暂无匹配日程" />
        <div v-else class="calendar-plan-list">
          <div
            v-for="item in selectedCalendarPlans"
            :key="String(item.id)"
            class="calendar-plan-card"
          >
            <div class="calendar-plan-top">
              <strong>{{ item.title }}</strong>
              <a-tag :color="statusColor(item.status)">{{ statusText(item.status) }}</a-tag>
            </div>
            <div class="calendar-plan-meta">组织人：{{ item.organizer || '-' }}</div>
            <div class="calendar-plan-meta">地点：{{ item.location || '-' }}</div>
            <div class="calendar-plan-meta">参与对象：{{ item.participantTarget || '-' }}</div>
            <div class="calendar-plan-meta">时间：{{ calendarTimeText(item) }}</div>
            <div v-if="item.content" class="calendar-plan-content">{{ item.content }}</div>
            <a-space size="small" style="margin-top: 8px;">
              <a-button size="small" @click="openEdit(item)">编辑</a-button>
              <a-button size="small" :disabled="item.status !== 'PLANNED'" @click="start(item)">开始</a-button>
              <a-button size="small" :disabled="item.status !== 'IN_PROGRESS'" @click="done(item)">完成</a-button>
            </a-space>
          </div>
        </div>
      </div>
    </a-card>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑活动计划' : '新增活动计划'" @ok="submit" :confirm-loading="saving" width="760px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="计划标题" required>
              <a-input v-model:value="form.title" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="计划日期" required>
              <a-date-picker v-model:value="planDate" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始时间">
              <a-date-picker v-model:value="startTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束时间">
              <a-date-picker v-model:value="endTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="地点">
              <a-input v-model:value="form.location" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="组织人">
              <a-input v-model:value="form.organizer" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="参与对象">
          <a-input v-model:value="form.participantTarget" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="计划内容">
          <a-textarea v-model:value="form.content" :rows="4" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  batchCancelActivityPlan,
  batchCompleteActivityPlan,
  batchDeleteActivityPlan,
  batchStartActivityPlan,
  cancelActivityPlan,
  completeActivityPlan,
  createActivityPlan,
  deleteActivityPlan,
  exportActivityPlan,
  getActivityPlanPage,
  startActivityPlan,
  updateActivityPlan
} from '../../api/oa'
import type { OaActivityPlan, PageResult } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<OaActivityPlan[]>([])
const viewMode = ref<'LIST' | 'CALENDAR'>('LIST')
const quickFilter = ref<'ALL' | 'TODAY' | 'UPCOMING_7D' | 'DONE' | 'CANCELLED'>('ALL')
const calendarGranularity = ref<'MONTH' | 'WEEK' | 'DAY'>('MONTH')
const calendarCursor = ref(dayjs())
const calendarSelectedDate = ref(dayjs())
const viewModeOptions = [
  { label: '列表视图', value: 'LIST' },
  { label: '日历视图', value: 'CALENDAR' }
]
const calendarGranularityOptions = [
  { label: '月', value: 'MONTH' },
  { label: '周', value: 'WEEK' },
  { label: '日', value: 'DAY' }
]
const query = reactive({
  keyword: '',
  organizer: '',
  location: '',
  participantTarget: '',
  status: undefined as string | undefined,
  range: undefined as [Dayjs, Dayjs] | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<string[]>([])

const columns = [
  { title: '计划标题', dataIndex: 'title', key: 'title', width: 200 },
  { title: '计划日期', dataIndex: 'planDate', key: 'planDate', width: 120 },
  { title: '组织人', dataIndex: 'organizer', key: 'organizer', width: 120 },
  { title: '地点', dataIndex: 'location', key: 'location', width: 140 },
  { title: '参与对象', dataIndex: 'participantTarget', key: 'participantTarget', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 }
]

const statusOptions = [
  { label: '计划中', value: 'PLANNED' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'DONE' },
  { label: '已取消', value: 'CANCELLED' }
]

const editOpen = ref(false)
const planDate = ref<Dayjs | undefined>()
const startTime = ref<Dayjs | undefined>()
const endTime = ref<Dayjs | undefined>()
const form = reactive<Partial<OaActivityPlan>>({ status: 'PLANNED' })
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
  }
}))
const selectedRecords = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(String(item.id))))
const selectedSingleRecord = computed(() => (selectedRecords.value.length === 1 ? selectedRecords.value[0] : null))
const canEditSelected = computed(() => !!selectedSingleRecord.value && selectedSingleRecord.value.status !== 'DONE' && selectedSingleRecord.value.status !== 'CANCELLED')
const canStartSelected = computed(() => selectedSingleRecord.value?.status === 'PLANNED')
const canDoneSelected = computed(() => selectedSingleRecord.value?.status === 'IN_PROGRESS')
const canCancelSelected = computed(() => !!selectedSingleRecord.value && selectedSingleRecord.value.status !== 'DONE' && selectedSingleRecord.value.status !== 'CANCELLED')
const selectedStartIds = computed(() =>
  selectedRecords.value.filter((item) => item.status === 'PLANNED').map((item) => String(item.id))
)
const selectedDoneIds = computed(() =>
  selectedRecords.value.filter((item) => item.status === 'IN_PROGRESS').map((item) => String(item.id))
)
const selectedCancelIds = computed(() =>
  selectedRecords.value.filter((item) => item.status !== 'DONE' && item.status !== 'CANCELLED').map((item) => String(item.id))
)
const selectedCalendarPlans = computed(() => activityPlansForDate(calendarSelectedDate.value))
const weekDays = computed(() => {
  const start = calendarSelectedDate.value.startOf('week')
  return Array.from({ length: 7 }).map((_, index) => {
    const date = start.add(index, 'day')
    return {
      date,
      dateKey: date.format('YYYY-MM-DD'),
      label: ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.day()],
      items: activityPlansForDate(date)
    }
  })
})
const calendarUnitLabel = computed(() => {
  if (calendarGranularity.value === 'WEEK') return '周'
  if (calendarGranularity.value === 'DAY') return '日'
  return '月'
})
const calendarSideTitle = computed(() => {
  if (calendarGranularity.value === 'WEEK') {
    const start = weekDays.value[0]?.date
    const end = weekDays.value[6]?.date
    return start && end
      ? `${start.format('YYYY-MM-DD')} 至 ${end.format('YYYY-MM-DD')} 周视图`
      : '周视图日程'
  }
  return `${calendarSelectedDate.value.format('YYYY-MM-DD')} 日程`
})

function statusText(status?: string) {
  if (status === 'IN_PROGRESS') return '进行中'
  if (status === 'DONE') return '已完成'
  if (status === 'CANCELLED') return '已取消'
  return '计划中'
}

function statusColor(status?: string) {
  if (status === 'DONE') return 'green'
  if (status === 'IN_PROGRESS') return 'blue'
  if (status === 'CANCELLED') return 'default'
  return 'orange'
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      pageNo: query.pageNo,
      pageSize: viewMode.value === 'CALENDAR' ? 200 : query.pageSize,
      keyword: query.keyword || undefined,
      organizer: query.organizer || undefined,
      location: query.location || undefined,
      participantTarget: query.participantTarget || undefined,
      status: query.status
    }
    if (query.range) {
      params.dateFrom = query.range[0].format('YYYY-MM-DD')
      params.dateTo = query.range[1].format('YYYY-MM-DD')
    }
    const res: PageResult<OaActivityPlan> = await getActivityPlanPage(params)
    rows.value = (res.list || []).map((item) => ({
      ...item,
      id: String(item.id)
    }))
    pagination.total = res.total || res.list.length
    selectedRowKeys.value = []
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function activityPlansForDate(date: Dayjs) {
  const target = date.format('YYYY-MM-DD')
  return rows.value.filter((item) => String(item.planDate || '') === target)
}

function onCalendarSelect(date: Dayjs) {
  calendarSelectedDate.value = date
  calendarCursor.value = date
}

function calendarTimeText(item: OaActivityPlan) {
  const start = item.startTime ? dayjs(item.startTime) : null
  const end = item.endTime ? dayjs(item.endTime) : null
  if (start?.isValid() && end?.isValid()) {
    return `${start.format('HH:mm')} - ${end.format('HH:mm')}`
  }
  if (start?.isValid()) return `${start.format('HH:mm')} 开始`
  return '全天安排'
}

function moveCalendar(offset: number) {
  if (calendarGranularity.value === 'WEEK') {
    const next = calendarSelectedDate.value.add(offset, 'week')
    calendarSelectedDate.value = next
    calendarCursor.value = next
    return
  }
  if (calendarGranularity.value === 'DAY') {
    const next = calendarSelectedDate.value.add(offset, 'day')
    calendarSelectedDate.value = next
    calendarCursor.value = next
    return
  }
  const next = calendarCursor.value.add(offset, 'month')
  calendarCursor.value = next
  calendarSelectedDate.value = next
}

function jumpCalendarToday() {
  const today = dayjs()
  calendarCursor.value = today
  calendarSelectedDate.value = today
}

function onReset() {
  query.keyword = ''
  query.organizer = ''
  query.location = ''
  query.participantTarget = ''
  query.status = undefined
  query.range = undefined
  quickFilter.value = 'ALL'
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function applyQuickFilter(mode: 'ALL' | 'TODAY' | 'UPCOMING_7D' | 'DONE' | 'CANCELLED') {
  quickFilter.value = mode
  query.pageNo = 1
  pagination.current = 1
  if (mode === 'ALL') {
    query.status = undefined
    query.range = undefined
  } else if (mode === 'TODAY') {
    query.status = undefined
    query.range = [dayjs().startOf('day'), dayjs().endOf('day')]
  } else if (mode === 'UPCOMING_7D') {
    query.status = undefined
    query.range = [dayjs().startOf('day'), dayjs().add(6, 'day').endOf('day')]
  } else if (mode === 'DONE') {
    query.status = 'DONE'
    query.range = undefined
  } else if (mode === 'CANCELLED') {
    query.status = 'CANCELLED'
    query.range = undefined
  }
  fetchData()
}

function openEdit(record?: OaActivityPlan) {
  if (record && (record.status === 'DONE' || record.status === 'CANCELLED')) return
  Object.assign(form, record || { status: 'PLANNED', title: '', location: '', organizer: '', participantTarget: '', content: '', remark: '' })
  planDate.value = form.planDate ? dayjs(form.planDate) : undefined
  startTime.value = form.startTime ? dayjs(form.startTime) : undefined
  endTime.value = form.endTime ? dayjs(form.endTime) : undefined
  editOpen.value = true
}

async function submit() {
  if (!form.title || !planDate.value) return
  saving.value = true
  try {
    const payload = {
      ...form,
      planDate: planDate.value.format('YYYY-MM-DD'),
      startTime: startTime.value ? startTime.value.format('YYYY-MM-DDTHH:mm:ss') : undefined,
      endTime: endTime.value ? endTime.value.format('YYYY-MM-DDTHH:mm:ss') : undefined
    }
    if (form.id) {
      await updateActivityPlan(form.id, payload)
    } else {
      await createActivityPlan(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: OaActivityPlan) {
  await deleteActivityPlan(String(record.id))
  fetchData()
}

async function start(record: OaActivityPlan) {
  if (record.status !== 'PLANNED') return
  await startActivityPlan(String(record.id))
  fetchData()
}

async function done(record: OaActivityPlan) {
  if (record.status !== 'IN_PROGRESS') return
  await completeActivityPlan(String(record.id))
  fetchData()
}

async function cancel(record: OaActivityPlan) {
  if (record.status === 'DONE' || record.status === 'CANCELLED') return
  await cancelActivityPlan(String(record.id))
  fetchData()
}

function requireSingleSelection(action: string) {
  if (!selectedSingleRecord.value) {
    message.info(`请先勾选 1 条计划后再${action}`)
    return null
  }
  return selectedSingleRecord.value
}

function editSelected() {
  const record = requireSingleSelection('编辑')
  if (!record) return
  openEdit(record)
}

async function startSelected() {
  const record = requireSingleSelection('开始')
  if (!record) return
  await start(record)
}

async function doneSelected() {
  const record = requireSingleSelection('完成')
  if (!record) return
  await done(record)
}

async function cancelSelected() {
  const record = requireSingleSelection('取消')
  if (!record) return
  await cancel(record)
}

async function removeSelected() {
  const record = requireSingleSelection('删除')
  if (!record) return
  await remove(record)
}

async function batchStart() {
  if (selectedStartIds.value.length === 0) {
    message.info('勾选项中没有可开始计划')
    return
  }
  const affected = await batchStartActivityPlan(selectedStartIds.value)
  message.success(`批量开始，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchDone() {
  if (selectedDoneIds.value.length === 0) {
    message.info('勾选项中没有可完成计划')
    return
  }
  const affected = await batchCompleteActivityPlan(selectedDoneIds.value)
  message.success(`批量完成，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchCancel() {
  if (selectedCancelIds.value.length === 0) {
    message.info('勾选项中没有可取消计划')
    return
  }
  const affected = await batchCancelActivityPlan(selectedCancelIds.value)
  message.success(`批量取消，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchDeleteActivityPlan(selectedRowKeys.value)
  message.success(`批量删除，共处理 ${affected || 0} 条`)
  fetchData()
}

async function downloadExport() {
  const params: any = {
    keyword: query.keyword || undefined,
    organizer: query.organizer || undefined,
    location: query.location || undefined,
    participantTarget: query.participantTarget || undefined,
    status: query.status
  }
  if (query.range) {
    params.dateFrom = query.range[0].format('YYYY-MM-DD')
    params.dateTo = query.range[1].format('YYYY-MM-DD')
  }
  const blob = await exportActivityPlan(params)
  const href = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = href
  link.download = `oa-activity-plan-${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(href)
}

fetchData()
</script>

<style scoped>
.calendar-card {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(320px, 0.9fr);
  gap: 16px;
}

.calendar-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.calendar-day-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.calendar-day-item {
  margin-bottom: 4px;
  padding: 4px 6px;
  border-radius: 8px;
  background: #f8fafc;
  border-left: 3px solid #91caff;
}

.calendar-day-item.status-done {
  border-left-color: #52c41a;
}

.calendar-day-item.status-in_progress {
  border-left-color: #1677ff;
}

.calendar-day-item.status-cancelled {
  border-left-color: #d9d9d9;
}

.calendar-day-title {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #1f2937;
}

.calendar-day-meta {
  display: block;
  font-size: 11px;
  color: rgba(0, 0, 0, 0.45);
}

.calendar-side {
  border-left: 1px solid #f0f0f0;
  padding-left: 16px;
}

.calendar-side-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.calendar-plan-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.calendar-plan-card {
  padding: 12px 14px;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  background: #fafafa;
}

.calendar-plan-card.compact {
  padding: 10px 10px 8px;
}

.calendar-plan-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}

.calendar-plan-meta {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.65);
  line-height: 1.8;
}

.calendar-plan-content {
  margin-top: 8px;
  color: #262626;
  line-height: 1.7;
}

.week-board {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 10px;
}

.week-column {
  min-height: 220px;
  padding: 10px;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  background: #fff;
}

.week-column-head {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #e5e7eb;
  cursor: pointer;
}

.week-column-head span {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.week-column-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.day-board {
  min-height: 420px;
}

.day-board-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.selection-tip {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

@media (max-width: 1100px) {
  .calendar-card {
    grid-template-columns: 1fr;
  }

  .calendar-side {
    border-left: 0;
    border-top: 1px solid #f0f0f0;
    padding-left: 0;
    padding-top: 16px;
  }

  .week-board {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .week-board {
    grid-template-columns: 1fr;
  }
}
</style>
