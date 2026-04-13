<template>
  <PageContainer title="考勤与行政日程一览" subTitle="我的考勤、请假联动日程、院长查看员工打卡明细">
    <a-card class="attendance-hero" :bordered="false">
      <div class="hero-head">
        <div>
          <div class="hero-title">考勤与请假联动中心</div>
          <div class="hero-sub">支持院长按员工查看打卡、外出、午休、请假联动日程并打印月报</div>
        </div>
        <a-tag color="processing">{{ query.month }} 月</a-tag>
      </div>
      <a-form layout="vertical" class="hero-form">
        <a-row :gutter="[12, 6]">
          <a-col v-if="canManageAttendance" :xs="24" :sm="12" :lg="8">
            <a-form-item label="查看员工">
              <a-select
                v-model:value="query.staffId"
                style="width: 100%"
                show-search
                allow-clear
                :filter-option="false"
                :options="staffOptions"
                :loading="staffLoading"
                placeholder="请选择员工"
                @search="searchStaff"
                @focus="() => !staffOptions.length && searchStaff('')"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="12" :sm="8" :lg="4">
            <a-form-item label="月份">
              <a-date-picker v-model:value="query.month" picker="month" value-format="YYYY-MM" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :xs="12" :sm="4" :lg="3">
            <a-form-item label="自动刷新">
              <a-switch v-model:checked="autoRefresh" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :lg="9">
            <a-form-item label="快捷操作">
              <a-space wrap>
                <a-button type="primary" @click="fetchData">查询</a-button>
                <a-button @click="reset">重置</a-button>
                <a-button v-if="showPunchInButton" type="primary" :loading="punching" @click="handlePunch('IN')">上班打卡</a-button>
                <a-button v-if="showPunchOutButton" :loading="punching" @click="handlePunch('OUT')">下班打卡</a-button>
                <a-button @click="printCurrentEmployee">打印当前员工</a-button>
                <a-button @click="exportCurrentEmployee">导出月报</a-button>
                <a-button @click="goLeaveApproval">填写请假</a-button>
              </a-space>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
      <div class="hero-tip">
        请假审批通过后会自动写入本页日程；午休中显示蓝色状态；院长可切换员工查看上下班/外出时长并打印。
      </div>
    </a-card>

    <a-row :gutter="16" class="metrics-row">
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="metric-card metric-card-teal" :bordered="false">
          <a-statistic title="当月在岗天数" :value="overview.onDutyDays || 0" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="metric-card metric-card-blue" :bordered="false">
          <a-statistic title="当月请假天数" :value="overview.leaveDays || 0" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="metric-card metric-card-amber" :bordered="false">
          <a-statistic title="迟到/早退" :value="`${overview.lateCount || 0}/${overview.earlyLeaveCount || 0}`" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="metric-card metric-card-violet" :bordered="false">
          <a-statistic title="外出总时长(分)" :value="overview.totalOutingMinutes || 0" />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16">
      <a-col :xs="24" :xl="16">
        <a-card class="grid-card" :bordered="false" title="考勤与行政日程合并视图">
          <a-table
            :columns="dayColumns"
            :data-source="overview.days || []"
            row-key="date"
            :loading="loading"
            :pagination="{ pageSize: 31, hideOnSinglePage: true }"
            size="small"
            class="attendance-table"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'attendanceTime'">
                <div>{{ formatDateTime(record.checkInTime) || '-' }} → {{ formatDateTime(record.checkOutTime) || '-' }}</div>
                <small class="muted">{{ record.workMinutes || 0 }} 分钟</small>
              </template>
              <template v-else-if="column.key === 'status'">
                <a-tag :color="statusColor(record)">
                  {{ record.status || '未打卡' }}
                </a-tag>
                <a-tag v-if="record.hasLeave" color="blue">请假中</a-tag>
              </template>
              <template v-else-if="column.key === 'leaveInfo'">
                <span v-if="record.hasLeave">{{ record.leaveType || '请假' }} {{ record.leaveTitle ? `· ${record.leaveTitle}` : '' }}</span>
                <span v-else class="muted">-</span>
              </template>
              <template v-else-if="column.key === 'restAndOuting'">
                <div>午休 {{ record.lunchBreakMinutes || 0 }} 分钟</div>
                <div>外出 {{ record.outingMinutes || 0 }} 分钟</div>
              </template>
              <template v-else-if="column.key === 'anomalyText'">
                <a-tag v-if="record.anomalyText" color="orange">{{ record.anomalyText }}</a-tag>
                <span v-else class="muted">正常</span>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <a-col :xs="24" :xl="8">
        <a-card class="grid-card today-panel" :bordered="false" title="今日状态">
          <a-space direction="vertical" style="width: 100%">
            <div class="today-line">
              <span>当前员工：</span>
              <strong>{{ currentStaffName || '-' }}</strong>
            </div>
            <div class="today-line">
              <span>当前状态：</span>
              <a-tag :color="todayStatusColor">{{ overview.todayStatusLabel || '未打卡' }}</a-tag>
            </div>
            <div class="today-line">
              <span>当季考勤时间：</span>
              <span>{{ seasonLabel }} {{ overview.expectedWorkStart || '--:--' }} - {{ overview.expectedWorkEnd || '--:--' }}</span>
            </div>
            <a-space v-if="isViewingSelf" wrap>
              <a-button v-if="showPunchInButton" type="primary" :loading="punching" @click="handlePunch('IN')">上班打卡</a-button>
              <a-button v-if="showStartLunchButton" :loading="punching" @click="handlePunch('START_LUNCH')">开始午休</a-button>
              <a-button v-if="showEndLunchButton" type="primary" ghost :loading="punching" @click="handlePunch('END_LUNCH')">结束午休</a-button>
              <a-button v-if="showStartOutingButton" :loading="punching" @click="handlePunch('START_OUTING')">开始外出</a-button>
              <a-button v-if="showEndOutingButton" type="primary" ghost :loading="punching" @click="handlePunch('END_OUTING')">结束外出</a-button>
              <a-button v-if="showPunchOutButton" :loading="punching" @click="handlePunch('OUT')">下班打卡</a-button>
              <span v-if="!showPunchInButton && !showStartLunchButton && !showEndLunchButton && !showStartOutingButton && !showEndOutingButton && !showPunchOutButton" class="muted">当前状态无需再次打卡</span>
            </a-space>
          </a-space>
        </a-card>

        <a-card v-if="canManageAttendance" class="grid-card" :bordered="false" title="季节考勤规则" style="margin-top: 16px">
          <a-form layout="vertical">
            <a-row :gutter="10">
              <a-col :span="12">
                <a-form-item label="冬季上班">
                  <a-time-picker v-model:value="ruleForm.winterWorkStart" format="HH:mm" value-format="HH:mm" style="width: 100%" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="冬季下班">
                  <a-time-picker v-model:value="ruleForm.winterWorkEnd" format="HH:mm" value-format="HH:mm" style="width: 100%" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="夏季上班">
                  <a-time-picker v-model:value="ruleForm.summerWorkStart" format="HH:mm" value-format="HH:mm" style="width: 100%" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="夏季下班">
                  <a-time-picker v-model:value="ruleForm.summerWorkEnd" format="HH:mm" value-format="HH:mm" style="width: 100%" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="10">
              <a-col :span="8">
                <a-form-item label="迟到宽限">
                  <a-input-number v-model:value="ruleForm.lateGraceMinutes" :min="0" :max="120" style="width: 100%" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="早退宽限">
                  <a-input-number v-model:value="ruleForm.earlyLeaveGraceMinutes" :min="0" :max="120" style="width: 100%" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="外出超时阈值">
                  <a-input-number v-model:value="ruleForm.outingMaxMinutes" :min="30" :max="600" style="width: 100%" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="10">
              <a-col :span="12"><a-form-item label="迟到判定"><a-switch v-model:checked="lateEnabledChecked" /></a-form-item></a-col>
              <a-col :span="12"><a-form-item label="早退判定"><a-switch v-model:checked="earlyLeaveEnabledChecked" /></a-form-item></a-col>
              <a-col :span="12"><a-form-item label="外出超时判定"><a-switch v-model:checked="outingOvertimeEnabledChecked" /></a-form-item></a-col>
              <a-col :span="12"><a-form-item label="缺签退判定"><a-switch v-model:checked="missingCheckoutEnabledChecked" /></a-form-item></a-col>
            </a-row>
            <a-button type="primary" block @click="saveRule">保存规则</a-button>
          </a-form>
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { openPrintTableReport } from '../../utils/print'
import { useUserStore } from '../../stores/user'
import { hasMinisterOrHigher } from '../../utils/roleAccess'
import { useStaffOptions } from '../../composables/useStaffOptions'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import {
  getAttendanceOverview,
  getAttendanceSeasonRule,
  punchAttendance,
  saveAttendanceSeasonRule
} from '../../api/schedule'
import type { AttendanceDashboardOverview, AttendanceSeasonRule } from '../../types'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const punching = ref(false)
const autoRefresh = ref(true)
let autoRefreshTimer: number | undefined
const overview = ref<AttendanceDashboardOverview>({ days: [] })
const { staffOptions, staffLoading, searchStaff } = useStaffOptions({ pageSize: 260, preloadSize: 800 })
const currentStaffId = computed(() => userStore.staffInfo?.id ? String(userStore.staffInfo.id) : '')

const query = reactive({
  staffId: (currentStaffId.value || undefined) as string | number | undefined,
  month: dayjs().format('YYYY-MM')
})

const ruleForm = reactive<AttendanceSeasonRule>({
  winterWorkStart: '13:30',
  winterWorkEnd: '17:00',
  summerWorkStart: '14:30',
  summerWorkEnd: '18:00',
  lateGraceMinutes: 10,
  earlyLeaveGraceMinutes: 10,
  outingMaxMinutes: 180,
  lateEnabled: 1,
  earlyLeaveEnabled: 1,
  outingOvertimeEnabled: 1,
  missingCheckoutEnabled: 1
})

const lateEnabledChecked = computed({
  get: () => Number(ruleForm.lateEnabled) === 1,
  set: (value: boolean) => {
    ruleForm.lateEnabled = value ? 1 : 0
  }
})
const earlyLeaveEnabledChecked = computed({
  get: () => Number(ruleForm.earlyLeaveEnabled) === 1,
  set: (value: boolean) => {
    ruleForm.earlyLeaveEnabled = value ? 1 : 0
  }
})
const outingOvertimeEnabledChecked = computed({
  get: () => Number(ruleForm.outingOvertimeEnabled) === 1,
  set: (value: boolean) => {
    ruleForm.outingOvertimeEnabled = value ? 1 : 0
  }
})
const missingCheckoutEnabledChecked = computed({
  get: () => Number(ruleForm.missingCheckoutEnabled) === 1,
  set: (value: boolean) => {
    ruleForm.missingCheckoutEnabled = value ? 1 : 0
  }
})

const dayColumns = [
  { title: '日期', dataIndex: 'date', key: 'date', width: 110 },
  { title: '星期', dataIndex: 'weekLabel', key: 'weekLabel', width: 90 },
  { title: '打卡时间', dataIndex: 'attendanceTime', key: 'attendanceTime', width: 230 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 150 },
  { title: '请假信息', dataIndex: 'leaveInfo', key: 'leaveInfo', width: 220 },
  { title: '午休/外出', dataIndex: 'restAndOuting', key: 'restAndOuting', width: 170 },
  { title: '行政日程', dataIndex: 'scheduleTitles', key: 'scheduleTitles', ellipsis: true },
  { title: '异常判定', dataIndex: 'anomalyText', key: 'anomalyText', width: 180 }
]

const canManageAttendance = computed(() => {
  const roles = (userStore.roles || []).map((item) => String(item || '').toUpperCase())
  return hasMinisterOrHigher(roles)
})

const currentStaffName = computed(() => {
  if (overview.value?.staffName) return overview.value.staffName
  if (query.staffId == null) return userStore.staffInfo?.realName || userStore.staffInfo?.username || ''
  const target = (staffOptions.value || []).find((item: any) => String(item.value) === String(query.staffId))
  return target?.label || ''
})
const isViewingSelf = computed(() => {
  const targetStaffId = query.staffId != null ? String(query.staffId) : currentStaffId.value
  return !!targetStaffId && targetStaffId === currentStaffId.value
})

const seasonLabel = computed(() => (overview.value?.seasonType === 'WINTER' ? '冬季' : '夏季'))

const todayStatusColor = computed(() => {
  const status = String(overview.value?.todayStatus || '').toUpperCase()
  if (status === 'LUNCH_BREAK') return 'blue'
  if (status === 'ON_LEAVE') return 'cyan'
  if (status === 'OUTING') return 'orange'
  if (status === 'ON_DUTY') return 'green'
  if (status === 'OFF_DUTY') return 'purple'
  return 'default'
})
const showPunchInButton = computed(() => {
  if (!isViewingSelf.value) return false
  const status = String(overview.value?.todayStatus || '').toUpperCase()
  return !status || status === 'NOT_CHECKED_IN'
})
const showPunchOutButton = computed(() => {
  if (!isViewingSelf.value) return false
  const status = String(overview.value?.todayStatus || '').toUpperCase()
  return status === 'ON_DUTY'
})
const showStartLunchButton = computed(() => {
  if (!isViewingSelf.value) return false
  const status = String(overview.value?.todayStatus || '').toUpperCase()
  const hasLunchStarted = !!currentDayRecord.value?.lunchBreakStartTime
  return status === 'ON_DUTY' && !hasLunchStarted
})
const showEndLunchButton = computed(() => {
  if (!isViewingSelf.value) return false
  const status = String(overview.value?.todayStatus || '').toUpperCase()
  return status === 'LUNCH_BREAK'
})
const showStartOutingButton = computed(() => {
  if (!isViewingSelf.value) return false
  const status = String(overview.value?.todayStatus || '').toUpperCase()
  const hasOutingStarted = !!currentDayRecord.value?.outingStartTime
  return status === 'ON_DUTY' && !hasOutingStarted
})
const showEndOutingButton = computed(() => {
  if (!isViewingSelf.value) return false
  const status = String(overview.value?.todayStatus || '').toUpperCase()
  return status === 'OUTING'
})
const currentDayRecord = computed(() => {
  const today = dayjs().format('YYYY-MM-DD')
  return (overview.value?.days || []).find((item: any) => String(item?.date || '') === today)
})

function statusColor(record: any) {
  if (record?.hasLeave) return 'blue'
  if (record?.anomalyText) return 'orange'
  if (String(record?.status || '').includes('未打卡')) return 'default'
  return 'green'
}

function formatDateTime(value?: string) {
  if (!value) return ''
  const parsed = dayjs(value)
  if (!parsed.isValid()) return value
  return parsed.format('HH:mm')
}

async function fetchData() {
  loading.value = true
  try {
    const targetStaffId = query.staffId ?? currentStaffId.value ?? undefined
    const [overviewResp, ruleResp] = await Promise.all([
      getAttendanceOverview({ staffId: targetStaffId, month: query.month }),
      getAttendanceSeasonRule()
    ])
    overview.value = overviewResp || { days: [] }
    Object.assign(ruleForm, ruleResp || {})
  } catch (error: any) {
    message.error(error?.message || '加载考勤一览失败')
  } finally {
    loading.value = false
  }
}

function reset() {
  query.month = dayjs().format('YYYY-MM')
  query.staffId = currentStaffId.value || undefined
  fetchData()
}

function goLeaveApproval() {
  router.push('/oa/approval?type=LEAVE&quick=1')
}

function printCurrentEmployee() {
  const rows = (overview.value.days || []).map((item: any) => ({
    date: item.date || '',
    weekLabel: item.weekLabel || '',
    attendance: `${formatDateTime(item.checkInTime) || '-'} → ${formatDateTime(item.checkOutTime) || '-'}`,
    leaveInfo: item.hasLeave ? `${item.leaveType || '请假'} ${item.leaveTitle || ''}` : '-',
    restAndOuting: `午休${item.lunchBreakMinutes || 0}分 / 外出${item.outingMinutes || 0}分`,
    status: item.status || '未打卡',
    anomalyText: item.anomalyText || '正常'
  }))
  if (!rows.length) {
    message.info('暂无可打印数据')
    return
  }
  openPrintTableReport({
    title: `${currentStaffName.value || '员工'}考勤月报`,
    subtitle: `月份：${query.month}，打印时间：${dayjs().format('YYYY-MM-DD HH:mm:ss')}`,
    columns: [
      { key: 'date', title: '日期' },
      { key: 'weekLabel', title: '星期' },
      { key: 'attendance', title: '打卡时间' },
      { key: 'leaveInfo', title: '请假信息' },
      { key: 'restAndOuting', title: '午休/外出' },
      { key: 'status', title: '状态' },
      { key: 'anomalyText', title: '异常判定' }
    ],
    rows
  })
}

function exportCurrentEmployee() {
  const rows = (overview.value.days || []).map((item: any) => ({
    日期: item.date || '',
    星期: item.weekLabel || '',
    打卡时间: `${formatDateTime(item.checkInTime) || '-'} -> ${formatDateTime(item.checkOutTime) || '-'}`,
    请假信息: item.hasLeave ? `${item.leaveType || '请假'} ${item.leaveTitle || ''}` : '-',
    午休分钟: item.lunchBreakMinutes || 0,
    外出分钟: item.outingMinutes || 0,
    状态: item.status || '未打卡',
    异常判定: item.anomalyText || '正常'
  }))
  exportCsv(rows, `${currentStaffName.value || '员工'}-考勤月报-${query.month}`)
}

async function handlePunch(action: 'IN' | 'OUT' | 'START_LUNCH' | 'END_LUNCH' | 'START_OUTING' | 'END_OUTING') {
  if (!isViewingSelf.value) {
    message.warning('只能为当前登录员工本人打卡')
    return
  }
  punching.value = true
  try {
    await punchAttendance(action)
    message.success(resolvePunchSuccessText(action))
    await fetchData()
  } catch (error: any) {
    message.error(error?.message || resolvePunchErrorText(action))
  } finally {
    punching.value = false
  }
}

function resolvePunchSuccessText(action: 'IN' | 'OUT' | 'START_LUNCH' | 'END_LUNCH' | 'START_OUTING' | 'END_OUTING') {
  if (action === 'IN') return '上班打卡成功'
  if (action === 'OUT') return '下班打卡成功'
  if (action === 'START_LUNCH') return '已开始午休'
  if (action === 'END_LUNCH') return '已结束午休'
  if (action === 'START_OUTING') return '已开始外出'
  return '已结束外出'
}

function resolvePunchErrorText(action: 'IN' | 'OUT' | 'START_LUNCH' | 'END_LUNCH' | 'START_OUTING' | 'END_OUTING') {
  if (action === 'IN') return '上班打卡失败'
  if (action === 'OUT') return '下班打卡失败'
  if (action === 'START_LUNCH') return '开始午休失败'
  if (action === 'END_LUNCH') return '结束午休失败'
  if (action === 'START_OUTING') return '开始外出失败'
  return '结束外出失败'
}

async function saveRule() {
  try {
    await saveAttendanceSeasonRule({
      winterWorkStart: ruleForm.winterWorkStart,
      winterWorkEnd: ruleForm.winterWorkEnd,
      summerWorkStart: ruleForm.summerWorkStart,
      summerWorkEnd: ruleForm.summerWorkEnd,
      lateGraceMinutes: Number(ruleForm.lateGraceMinutes || 0),
      earlyLeaveGraceMinutes: Number(ruleForm.earlyLeaveGraceMinutes || 0),
      outingMaxMinutes: Number(ruleForm.outingMaxMinutes || 0),
      lateEnabled: Number(ruleForm.lateEnabled || 0),
      earlyLeaveEnabled: Number(ruleForm.earlyLeaveEnabled || 0),
      outingOvertimeEnabled: Number(ruleForm.outingOvertimeEnabled || 0),
      missingCheckoutEnabled: Number(ruleForm.missingCheckoutEnabled || 0)
    })
    message.success('季节考勤规则已保存')
    fetchData()
  } catch (error: any) {
    message.error(error?.message || '保存规则失败')
  }
}

function startAutoRefresh() {
  if (!autoRefresh.value || autoRefreshTimer !== undefined) return
  autoRefreshTimer = window.setInterval(() => {
    if (loading.value || document.visibilityState !== 'visible') return
    fetchData()
  }, 60 * 1000)
}

function stopAutoRefresh() {
  if (autoRefreshTimer !== undefined) {
    window.clearInterval(autoRefreshTimer)
    autoRefreshTimer = undefined
  }
}

watch(autoRefresh, (enabled) => {
  if (enabled) {
    startAutoRefresh()
    return
  }
  stopAutoRefresh()
})

onMounted(async () => {
  query.staffId = currentStaffId.value || undefined
  await searchStaff('')
  await fetchData()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})

useLiveSyncRefresh({
  topics: ['oa', 'hr', 'system'],
  refresh: () => {
    if (!autoRefresh.value) return
    fetchData().catch(() => {})
  },
  debounceMs: 900
})
</script>

<style scoped>
.attendance-hero {
  background: linear-gradient(135deg, #0f766e 0%, #0ea5e9 52%, #1d4ed8 100%);
  color: #fff;
  border-radius: 16px;
  box-shadow: 0 14px 30px rgba(2, 12, 27, 0.18);
}

.attendance-hero :deep(.ant-form-item-label > label),
.attendance-hero :deep(.ant-form-item) {
  color: #fff !important;
}

.hero-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.hero-title {
  font-size: 18px;
  font-weight: 600;
  line-height: 1.2;
}

.hero-sub {
  margin-top: 4px;
  font-size: 12px;
  opacity: 0.9;
}

.hero-form {
  margin-top: 6px;
}

.hero-tip {
  margin-top: 2px;
  font-size: 12px;
  opacity: 0.92;
}

.metrics-row {
  margin-top: 16px;
  margin-bottom: 16px;
}

.metric-card {
  border-radius: 14px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
}

.metric-card :deep(.ant-statistic-title) {
  color: rgba(15, 23, 42, 0.72);
}

.metric-card :deep(.ant-statistic-content) {
  font-weight: 600;
}

.metric-card-teal {
  background: linear-gradient(180deg, #f0fdfa 0%, #ccfbf1 100%);
}

.metric-card-blue {
  background: linear-gradient(180deg, #eff6ff 0%, #dbeafe 100%);
}

.metric-card-amber {
  background: linear-gradient(180deg, #fffbeb 0%, #fef3c7 100%);
}

.metric-card-violet {
  background: linear-gradient(180deg, #f5f3ff 0%, #ede9fe 100%);
}

.grid-card {
  border-radius: 14px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
}

.today-panel {
  background: linear-gradient(180deg, #f8fafc 0%, #eef2ff 100%);
}

.muted {
  color: #8c8c8c;
}

.today-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.attendance-table :deep(.ant-table-thead > tr > th) {
  background: #f5f7ff;
}

.attendance-table :deep(.ant-table-tbody > tr:hover > td) {
  background: #f8fafc;
}
</style>
