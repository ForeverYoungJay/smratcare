<template>
  <PageContainer title="排班方案" subTitle="支持导入排班记录、自动生成、下载和打印">
    <a-row :gutter="16">
      <a-col :xs="24" :xl="9">
        <a-card title="排班设置" size="small">
          <a-form layout="vertical">
            <a-form-item label="方案名称" required>
              <a-input v-model:value="plan.name" placeholder="例如：4月行政值班表" />
            </a-form-item>
            <a-form-item label="排班日期范围" required>
              <a-range-picker v-model:value="dateRangeValue" style="width: 100%" />
            </a-form-item>
            <a-form-item label="值班时段" required>
              <a-space style="width: 100%">
                <a-time-picker v-model:value="startTimeValue" format="HH:mm" style="flex: 1" />
                <span>至</span>
                <a-time-picker v-model:value="endTimeValue" format="HH:mm" style="flex: 1" />
              </a-space>
            </a-form-item>
            <a-form-item label="每日需要人数" required>
              <a-input-number v-model:value="plan.requiredCount" :min="1" :max="20" style="width: 100%" />
            </a-form-item>
            <a-form-item label="参与值班人员" required>
              <a-select
                v-model:value="plan.staffIds"
                mode="multiple"
                show-search
                :filter-option="false"
                :options="staffOptions"
                :loading="staffLoading"
                placeholder="选择参与本轮值班的员工"
                @search="searchStaff"
                @focus="() => !staffOptions.length && searchStaff('')"
              />
            </a-form-item>
            <a-form-item label="节日日期">
              <a-space direction="vertical" style="width: 100%">
                <a-date-picker v-model:value="holidayPickerValue" style="width: 100%" />
                <a-button @click="addHoliday">添加节日日期</a-button>
                <a-space wrap>
                  <a-tag v-for="item in plan.holidayDates" :key="item" closable @close="removeHoliday(item)">
                    {{ item }}
                  </a-tag>
                </a-space>
              </a-space>
            </a-form-item>
            <a-form-item label="节日主要值班人">
              <a-select
                v-model:value="plan.holidayPrimaryStaffId"
                allow-clear
                show-search
                :filter-option="false"
                :options="staffOptions"
                placeholder="节日当天优先排班的负责人"
                @search="searchStaff"
              />
            </a-form-item>
            <a-form-item label="补充说明">
              <a-textarea v-model:value="plan.remark" :rows="3" placeholder="例如：法定节假日必须安排行政主任到岗" />
            </a-form-item>
          </a-form>
          <a-space wrap>
            <a-button type="primary" @click="generateSchedule">自动生成排班表</a-button>
            <a-button @click="saveDraft">保存草稿</a-button>
            <a-button @click="resetPlan">重置</a-button>
          </a-space>
        </a-card>

        <a-card title="导入排班记录" size="small" style="margin-top: 16px">
          <a-upload :show-upload-list="false" :before-upload="beforeImportSchedule">
            <a-button :loading="importing">上传 CSV 排班记录</a-button>
          </a-upload>
          <div class="hint">
            模板字段：日期,值班时段,值班人员,人数,是否节日,主要值班人,备注
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :xl="15">
        <a-card size="small">
          <template #title>
            <a-space>
              <span>排班预览</span>
              <a-tag color="blue">{{ rows.length }} 天</a-tag>
            </a-space>
          </template>
          <template #extra>
            <a-space>
              <a-button :disabled="rows.length === 0" @click="downloadCsv">下载 CSV</a-button>
              <a-button :disabled="rows.length === 0" @click="printSchedule">打印</a-button>
            </a-space>
          </template>

          <a-alert
            type="info"
            show-icon
            style="margin-bottom: 12px"
            message="自动排班规则"
            :description="scheduleSummary"
          />

          <a-alert
            v-if="scheduleConflicts.length"
            type="warning"
            show-icon
            style="margin-bottom: 12px"
            :message="`发现 ${scheduleConflicts.length} 项排班冲突`"
            :description="scheduleConflictText"
          />

          <DataTable
            rowKey="date"
            :columns="columns"
            :data-source="rows"
            :pagination="false"
            :scroll="{ x: 900 }"
            :row-class-name="rowClassName"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'isHoliday'">
                <a-tag :color="record.isHoliday ? 'red' : 'default'">
                  {{ record.isHoliday ? '节日' : '常规' }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'staffNames'">
                <a-space wrap>
                  <a-tag
                    v-for="name in record.staffNamesList"
                    :key="`${record.date}-${name}`"
                    :color="name === record.primaryStaffName ? 'gold' : 'blue'"
                  >
                    {{ name }}
                  </a-tag>
                </a-space>
              </template>
            </template>
          </DataTable>
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import DataTable from '../../components/DataTable.vue'
import { useStaffOptions } from '../../composables/useStaffOptions'
import { exportCsv } from '../../utils/export'
import { openPrintTableReport } from '../../utils/print'

type ScheduleRow = {
  date: string
  shiftTime: string
  requiredCount: number
  staffNames: string
  staffNamesList: string[]
  isHoliday: boolean
  primaryStaffName?: string
  remark?: string
}

type ScheduleConflict = {
  type: 'HOLIDAY_PRIMARY_MISSING' | 'DUPLICATE_ASSIGNMENT' | 'HEADCOUNT_MISMATCH'
  date: string
  message: string
}

const STORAGE_KEY = 'hr-attendance-scheme-draft-v1'
const { staffOptions, staffLoading, searchStaff } = useStaffOptions({ pageSize: 200 })
const columns = [
  { title: '日期', dataIndex: 'date', key: 'date', width: 120 },
  { title: '值班时段', dataIndex: 'shiftTime', key: 'shiftTime', width: 160 },
  { title: '人数', dataIndex: 'requiredCount', key: 'requiredCount', width: 100 },
  { title: '班次人员', dataIndex: 'staffNames', key: 'staffNames', width: 320 },
  { title: '是否节日', dataIndex: 'isHoliday', key: 'isHoliday', width: 100 },
  { title: '主要值班人', dataIndex: 'primaryStaffName', key: 'primaryStaffName', width: 140 },
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 180 }
]

const plan = reactive({
  name: '行政值班排班表',
  requiredCount: 2,
  staffIds: [] as Array<string | number>,
  holidayDates: [] as string[],
  holidayPrimaryStaffId: undefined as string | number | undefined,
  remark: ''
})
const rows = ref<ScheduleRow[]>([])
const importing = ref(false)
const scheduleConflicts = ref<ScheduleConflict[]>([])
const dateRangeValue = ref<[Dayjs, Dayjs] | undefined>([dayjs().startOf('month'), dayjs().endOf('month')])
const startTimeValue = ref<Dayjs | undefined>(dayjs().hour(8).minute(0).second(0))
const endTimeValue = ref<Dayjs | undefined>(dayjs().hour(18).minute(0).second(0))
const holidayPickerValue = ref<Dayjs | undefined>()

const scheduleSummary = computed(() => {
  const staffCount = plan.staffIds.length
  const holidayCount = plan.holidayDates.length
  return `按日期顺序轮转排班；每日 ${plan.requiredCount || 0} 人，当前参与 ${staffCount} 人，节日 ${holidayCount} 天会优先安排主要值班人。`
})
const scheduleConflictText = computed(() => scheduleConflicts.value.map((item) => `${item.date}：${item.message}`).join('；'))

function getStaffLabel(staffId: string | number | undefined) {
  const hit = staffOptions.value.find((item) => String(item.value) === String(staffId))
  return hit?.label || String(staffId || '')
}

function addHoliday() {
  if (!holidayPickerValue.value) {
    message.info('请先选择节日日期')
    return
  }
  const next = holidayPickerValue.value.format('YYYY-MM-DD')
  if (!plan.holidayDates.includes(next)) {
    plan.holidayDates = [...plan.holidayDates, next].sort()
  }
  holidayPickerValue.value = undefined
}

function removeHoliday(date: string) {
  plan.holidayDates = plan.holidayDates.filter((item) => item !== date)
}

function validatePlan() {
  if (!plan.name.trim()) {
    message.warning('请填写方案名称')
    return false
  }
  if (!dateRangeValue.value?.[0] || !dateRangeValue.value?.[1]) {
    message.warning('请选择排班日期范围')
    return false
  }
  if (!startTimeValue.value || !endTimeValue.value) {
    message.warning('请选择值班时段')
    return false
  }
  if (endTimeValue.value.isBefore(startTimeValue.value) || endTimeValue.value.isSame(startTimeValue.value)) {
    message.warning('值班结束时间必须晚于开始时间')
    return false
  }
  if (!plan.requiredCount || plan.requiredCount < 1) {
    message.warning('每日需要人数至少为 1')
    return false
  }
  if (plan.staffIds.length < plan.requiredCount) {
    message.warning('参与值班人员数量不能少于每日需要人数')
    return false
  }
  if (plan.holidayDates.length > 0 && !plan.holidayPrimaryStaffId) {
    message.warning('存在节日日期时，必须指定节日主要值班人')
    return false
  }
  return true
}

function generateSchedule() {
  if (!validatePlan()) return
  const [start, end] = dateRangeValue.value as [Dayjs, Dayjs]
  const shiftTime = `${startTimeValue.value?.format('HH:mm') || ''}-${endTimeValue.value?.format('HH:mm') || ''}`
  const selectedStaff = plan.staffIds.map((id) => ({ id, name: getStaffLabel(id) })).filter((item) => item.name)
  const result: ScheduleRow[] = []
  let rotateIndex = 0
  const primaryName = getStaffLabel(plan.holidayPrimaryStaffId)

  for (let cursor = start.startOf('day'); !cursor.isAfter(end, 'day'); cursor = cursor.add(1, 'day')) {
    const date = cursor.format('YYYY-MM-DD')
    const isHoliday = plan.holidayDates.includes(date)
    const assigned: string[] = []

    if (isHoliday && primaryName) {
      assigned.push(primaryName)
    }

    while (assigned.length < plan.requiredCount) {
      const next = selectedStaff[rotateIndex % selectedStaff.length]
      rotateIndex += 1
      if (!next) break
      if (!assigned.includes(next.name)) {
        assigned.push(next.name)
      }
    }

    result.push({
      date,
      shiftTime,
      requiredCount: plan.requiredCount,
      staffNames: assigned.join('、'),
      staffNamesList: assigned,
      isHoliday,
      primaryStaffName: isHoliday ? primaryName : '',
      remark: isHoliday ? firstNonEmpty(`节日值班，负责人：${primaryName || '待定'}`, plan.remark) : plan.remark
    })
  }

  rows.value = result
  scheduleConflicts.value = collectScheduleConflicts(result)
  if (scheduleConflicts.value.length) {
    message.warning(`排班表已生成，但发现 ${scheduleConflicts.value.length} 项冲突，请先处理`)
    return
  }
  message.success('排班表已自动生成')
}

function saveDraft() {
  const payload = {
    plan: { ...plan },
    dateRange: dateRangeValue.value?.map((item) => item.format('YYYY-MM-DD')) || [],
    timeRange: [startTimeValue.value?.format('HH:mm') || '', endTimeValue.value?.format('HH:mm') || ''],
    rows: rows.value
  }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(payload))
  message.success('排班草稿已保存')
}

function restoreDraft() {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return
  try {
    const parsed = JSON.parse(raw)
    Object.assign(plan, parsed.plan || {})
    if (Array.isArray(parsed.dateRange) && parsed.dateRange.length === 2) {
      dateRangeValue.value = [dayjs(parsed.dateRange[0]), dayjs(parsed.dateRange[1])]
    }
    if (Array.isArray(parsed.timeRange) && parsed.timeRange.length === 2) {
      startTimeValue.value = parsed.timeRange[0] ? dayjs(`2024-01-01 ${parsed.timeRange[0]}`) : undefined
      endTimeValue.value = parsed.timeRange[1] ? dayjs(`2024-01-01 ${parsed.timeRange[1]}`) : undefined
    }
    rows.value = Array.isArray(parsed.rows) ? parsed.rows : []
    scheduleConflicts.value = collectScheduleConflicts(rows.value)
  } catch {
    localStorage.removeItem(STORAGE_KEY)
  }
}

function resetPlan() {
  plan.name = '行政值班排班表'
  plan.requiredCount = 2
  plan.staffIds = []
  plan.holidayDates = []
  plan.holidayPrimaryStaffId = undefined
  plan.remark = ''
  dateRangeValue.value = [dayjs().startOf('month'), dayjs().endOf('month')]
  startTimeValue.value = dayjs().hour(8).minute(0).second(0)
  endTimeValue.value = dayjs().hour(18).minute(0).second(0)
  holidayPickerValue.value = undefined
  rows.value = []
  scheduleConflicts.value = []
}

async function beforeImportSchedule(file: File) {
  importing.value = true
  try {
    const text = await file.text()
    const parsed = parseCsv(text)
    rows.value = parsed
    scheduleConflicts.value = collectScheduleConflicts(parsed)
    if (scheduleConflicts.value.length) {
      message.warning(`已导入 ${parsed.length} 条排班记录，发现 ${scheduleConflicts.value.length} 项冲突`)
    } else {
      message.success(`已导入 ${parsed.length} 条排班记录`)
    }
  } catch {
    message.error('导入失败，请检查 CSV 内容')
  } finally {
    importing.value = false
  }
  return false
}

function parseCsv(text: string) {
  const lines = text.split(/\r?\n/).map((item) => item.trim()).filter(Boolean)
  if (lines.length <= 1) return []
  return lines.slice(1).map((line) => {
    const [date, shiftTime, staffNames, requiredCount, isHoliday, primaryStaffName, remark] = line.split(',').map((item) => item.trim())
    const staffNamesList = staffNames ? staffNames.split(/[、;；|]/).map((item) => item.trim()).filter(Boolean) : []
    return {
      date,
      shiftTime,
      requiredCount: Number(requiredCount || staffNamesList.length || 0),
      staffNames,
      staffNamesList,
      isHoliday: ['1', 'true', 'TRUE', '节日', '是'].includes(isHoliday),
      primaryStaffName,
      remark
    } as ScheduleRow
  })
}

function downloadCsv() {
  if (scheduleConflicts.value.length) {
    message.warning('请先处理排班冲突后再下载')
    return
  }
  exportCsv(rows.value.map((item) => ({
    日期: item.date,
    值班时段: item.shiftTime,
    人数: item.requiredCount,
    班次人员: item.staffNames,
    是否节日: item.isHoliday ? '是' : '否',
    主要值班人: item.primaryStaffName || '',
    备注: item.remark || ''
  })), `${plan.name || '排班方案'}-${dayjs().format('YYYYMMDD')}`)
}

function printSchedule() {
  if (scheduleConflicts.value.length) {
    message.warning('请先处理排班冲突后再打印')
    return
  }
  openPrintTableReport({
    title: plan.name || '排班方案',
    subtitle: `生成时间：${dayjs().format('YYYY-MM-DD HH:mm:ss')}`,
    columns: [
      { key: 'date', title: '日期' },
      { key: 'shiftTime', title: '值班时段' },
      { key: 'requiredCount', title: '人数' },
      { key: 'staffNames', title: '班次人员' },
      { key: 'holidayText', title: '节日' },
      { key: 'primaryStaffName', title: '主要值班人' },
      { key: 'remark', title: '备注' }
    ],
    rows: rows.value.map((item) => ({
      ...item,
      holidayText: item.isHoliday ? '是' : '否'
    })),
    signatures: ['排班人', '部门负责人']
  })
}

function firstNonEmpty(...values: Array<string | undefined>) {
  return values.find((item) => String(item || '').trim()) || ''
}

function collectScheduleConflicts(sourceRows: ScheduleRow[]) {
  const conflicts: ScheduleConflict[] = []
  const assignmentMap = new Map<string, number>()

  sourceRows.forEach((item) => {
    if (item.isHoliday && !String(item.primaryStaffName || '').trim()) {
      conflicts.push({
        type: 'HOLIDAY_PRIMARY_MISSING',
        date: item.date,
        message: '节日当天未设置主要值班人'
      })
    }

    if ((item.staffNamesList || []).length !== Number(item.requiredCount || 0)) {
      conflicts.push({
        type: 'HEADCOUNT_MISMATCH',
        date: item.date,
        message: `排班人数与需求不一致，当前 ${item.staffNamesList.length} 人，需求 ${item.requiredCount} 人`
      })
    }

    ;(item.staffNamesList || []).forEach((name) => {
      const key = `${item.date}::${name}`
      assignmentMap.set(key, (assignmentMap.get(key) || 0) + 1)
    })
  })

  assignmentMap.forEach((count, key) => {
    if (count <= 1) return
    const [date, name] = key.split('::')
    conflicts.push({
      type: 'DUPLICATE_ASSIGNMENT',
      date,
      message: `${name} 在同一天被重复排班 ${count} 次`
    })
  })

  return conflicts
}

function rowClassName(record: ScheduleRow) {
  return scheduleConflicts.value.some((item) => item.date === record.date) ? 'schedule-row-warning' : ''
}

searchStaff('')
restoreDraft()
</script>

<style scoped>
.hint {
  margin-top: 8px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

:deep(.schedule-row-warning) {
  background: #fffbe6 !important;
}
</style>
