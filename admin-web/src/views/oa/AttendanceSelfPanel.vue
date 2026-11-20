<template>
  <div class="of-page">
    <OfficeHero :shift="overview.expectedWorkStart ? `${overview.expectedWorkStart}–${overview.expectedWorkEnd}` : ''">
      按时出勤、规范请假、合理安排工作，共同守护长者的幸福晚年！
    </OfficeHero>

    <div class="of-kpis">
      <div class="of-kpi"><span class="of-kpi__icon tone-green"><CalendarOutlined /></span><div class="of-kpi__body">
        <span>本月出勤</span><b>{{ overview.onDutyDays || 0 }}<small>天 / {{ workdays }} 天</small></b><em>出勤率 {{ attendanceRate }}%（工作日至今）</em></div></div>
      <div class="of-kpi"><span class="of-kpi__icon tone-orange"><ClockCircleOutlined /></span><div class="of-kpi__body">
        <span>迟到 / 早退</span><b>{{ overview.lateCount || 0 }}<small>次</small> / {{ overview.earlyLeaveCount || 0 }}<small>次</small></b><em>异常 {{ overview.abnormalCount || 0 }} 次</em></div></div>
      <div class="of-kpi"><span class="of-kpi__icon tone-blue"><SolutionOutlined /></span><div class="of-kpi__body">
        <span>请假余额</span>
        <b v-if="annual">年假 {{ annual.remainingDays }}<small>天（共 {{ annual.quotaDays }} 天）</small></b>
        <b v-else>--</b>
        <em>{{ annual ? `本月已请假 ${overview.leaveDays || 0} 天` : '人事尚未维护本年假期额度' }}</em></div></div>
      <div class="of-kpi"><span class="of-kpi__icon tone-purple"><FieldTimeOutlined /></span><div class="of-kpi__body">
        <span>加班时长</span><b>{{ overtime }}<small>小时</small></b><em>本月已批准的加班申请</em></div></div>
    </div>

    <div class="asp-row">
      <article class="of-card">
        <div class="of-card__head"><h3><EnvironmentOutlined />打卡状态</h3></div>
        <div class="asp-status" :class="{ 'asp-status--warn': !today?.checkInTime }">
          <CheckCircleFilled v-if="today?.checkInTime" class="asp-status__icon" />
          <ExclamationCircleFilled v-else class="asp-status__icon" />
          <div><b>{{ overview.todayStatusLabel || '今日未打卡' }}</b><small>班次 {{ overview.expectedWorkStart || '--:--' }} – {{ overview.expectedWorkEnd || '--:--' }}</small></div>
        </div>
        <div class="of-row"><LoginOutlined /><div class="of-row__main"><b>上班打卡</b></div>
          <span>{{ today?.checkInTime ? dayjs(today.checkInTime).format('HH:mm') : '--:--' }}</span>
          <span class="of-pill" :class="today?.checkInTime ? (today?.late ? 'tone-orange' : 'tone-green') : 'tone-red'">{{ today?.checkInTime ? (today?.late ? '迟到' : '正常') : '未打卡' }}</span></div>
        <div class="of-row"><LogoutOutlined /><div class="of-row__main"><b>下班打卡</b></div>
          <span>{{ today?.checkOutTime ? dayjs(today.checkOutTime).format('HH:mm') : '--:--' }}</span>
          <span class="of-pill" :class="today?.checkOutTime ? (today?.earlyLeave ? 'tone-orange' : 'tone-green') : 'tone-blue'">{{ today?.checkOutTime ? (today?.earlyLeave ? '早退' : '正常') : '待打卡' }}</span></div>
        <a-space wrap style="margin-top: 10px">
          <a-button v-for="a in actions" :key="a.key" size="small" :type="a.primary ? 'primary' : 'default'" :loading="punching" @click="emit('punch', a.key)">{{ a.label }}</a-button>
        </a-space>
      </article>

      <article class="of-card">
        <div class="of-card__head"><h3><CalendarOutlined />考勤日历 <small>（{{ dayjs(`${month}-01`).format('YYYY年M月') }}）</small></h3></div>
        <div class="asp-cal">
          <span v-for="w in ['一', '二', '三', '四', '五', '六', '日']" :key="w" class="asp-cal__week">{{ w }}</span>
          <template v-for="(cell, i) in grid" :key="i">
            <span v-if="!cell" />
            <span v-else class="asp-cal__day" :class="[`asp-cal__day--${dayTone(cell.record, cell.date)}`, { 'asp-cal__day--today': cell.date === todayKey }]" :title="dayTitle(cell.record)">
              {{ cell.day }}<i v-if="dayTone(cell.record, cell.date) !== 'none'" />
            </span>
          </template>
        </div>
        <div class="asp-cal__legend"><span><i class="ok" />正常</span><span><i class="late" />迟到/早退</span><span><i class="leave" />请假</span><span><i class="bad" />异常</span></div>
      </article>

      <article class="of-card">
        <div class="of-card__head"><h3><BarChartOutlined />本月考勤统计</h3></div>
        <div class="asp-stats">
          <div class="of-donut" :style="{ background: rateDonut }"><div><small>出勤率</small><b>{{ attendanceRate }}%</b></div></div>
          <div class="of-legend">
            <div><i style="background: #1f9d74" />正常出勤<b>{{ overview.onDutyDays || 0 }} 天</b></div>
            <div><i style="background: #f0b429" />迟到<b>{{ overview.lateCount || 0 }} 次</b></div>
            <div><i style="background: #2f6fd1" />请假<b>{{ overview.leaveDays || 0 }} 天</b></div>
            <div><i style="background: #7a55c7" />加班<b>{{ overtime }} 小时</b></div>
            <div><i style="background: #e5675a" />异常<b>{{ overview.abnormalCount || 0 }} 次</b></div>
          </div>
        </div>
      </article>
    </div>

    <div class="asp-row">
      <article class="of-card">
        <div class="of-card__head"><h3><SolutionOutlined />请假申请</h3><button type="button" class="of-link" @click="go('/oa/approval?type=LEAVE&quick=1')">新建请假申请 ›</button></div>
        <div v-if="!balance.length" class="of-empty">人事尚未维护本年假期额度，仍可直接提交请假申请</div>
        <div class="asp-leave">
          <button v-for="b in balance" :key="b.leaveType" type="button" @click="go('/oa/approval?type=LEAVE&quick=1')">
            <b>{{ LEAVE_TYPES[b.leaveType] || b.leaveType }}</b>
            <span class="of-donut asp-ring" :style="{ background: ring(b) }"><span><b>{{ b.remainingDays }}</b><small>/{{ b.quotaDays }}天</small></span></span>
            <small>可用 {{ b.remainingDays }} 天</small>
          </button>
        </div>
      </article>

      <article class="of-card">
        <div class="of-card__head"><h3><FileTextOutlined />申请记录 <small>（最近）</small></h3><button type="button" class="of-link" @click="go('/oa/approval?scope=MY')">全部记录 ›</button></div>
        <div v-if="!records.length" class="of-empty">暂无请假、加班、调班申请</div>
        <div v-for="r in records" :key="String(r.id)" class="of-row">
          <div class="of-row__main"><b>{{ TYPE_LABEL[r.approvalType] || r.approvalType }} · {{ r.title }}</b>
            <small>{{ r.startTime ? dayjs(r.startTime).format('MM-DD HH:mm') : '' }}{{ r.endTime ? ` 至 ${dayjs(r.endTime).format('MM-DD HH:mm')}` : '' }}</small></div>
          <span class="of-pill" :class="STATUS[r.status || '']?.tone || 'tone-blue'">{{ STATUS[r.status || '']?.label || r.status }}</span>
        </div>
      </article>

      <article class="of-card">
        <div class="of-card__head"><h3><WarningOutlined />异常处理</h3></div>
        <div class="asp-fix">
          <div class="tone-green"><ExclamationCircleOutlined /><b>漏打卡</b><small>由人事开考勤调整单补录</small>
            <a-tooltip title="请联系人事或班组长，人事在「考勤核定与调整」开调整单，审批后写回考勤"><a-button size="small" disabled>联系人事办理</a-button></a-tooltip></div>
          <div class="tone-blue"><SwapOutlined /><b>调班申请</b><small>申请班次调整</small>
            <a-button size="small" @click="go('/oa/approval?type=SHIFT_CHANGE&quick=1')">立即申请 ›</a-button></div>
          <div class="tone-orange"><FieldTimeOutlined /><b>加班申请</b><small>审批后计入加班时长</small>
            <a-button size="small" @click="go('/oa/approval?type=OVERTIME&quick=1')">立即申请 ›</a-button></div>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import {
  BarChartOutlined, CalendarOutlined, CheckCircleFilled, ClockCircleOutlined, EnvironmentOutlined, ExclamationCircleFilled,
  ExclamationCircleOutlined, FieldTimeOutlined, FileTextOutlined, LoginOutlined, LogoutOutlined, SolutionOutlined, SwapOutlined, WarningOutlined
} from '@ant-design/icons-vue'
import { getApprovalPage } from '../../api/oa'
import { getLeaveBalance } from '../../api/me'
import type { AttendanceDashboardDayItem, AttendanceDashboardOverview, OaApproval } from '../../types'
import OfficeHero from './office/OfficeHero.vue'
import './office/office.css'
import { LEAVE_TYPES, monthGrid, overtimeHours, workdaysSoFar } from './myOffice'

type PunchAction = 'IN' | 'OUT' | 'START_LUNCH' | 'END_LUNCH' | 'START_OUTING' | 'END_OUTING'
const props = defineProps<{ overview: AttendanceDashboardOverview; month: string; punching?: boolean }>()
const emit = defineEmits<{ punch: [action: PunchAction] }>()
const TYPE_LABEL: Record<string, string> = { LEAVE: '请假', OVERTIME: '加班', SHIFT_CHANGE: '调班' }
const STATUS: Record<string, { label: string; tone: string }> = {
  PENDING: { label: '待审批', tone: 'tone-orange' }, APPROVED: { label: '已批准', tone: 'tone-green' },
  REJECTED: { label: '已驳回', tone: 'tone-red' }, CANCELLED: { label: '已撤回', tone: 'tone-blue' }
}

const router = useRouter()
const balance = ref<Awaited<ReturnType<typeof getLeaveBalance>>>([])
const records = ref<OaApproval[]>([])
const overtimeItems = ref<OaApproval[]>([])
const todayKey = dayjs().format('YYYY-MM-DD')

const today = computed(() => (props.overview.days || []).find((d) => d.date === todayKey))
const grid = computed(() => monthGrid(props.month, props.overview.days || []))
const workdays = computed(() => workdaysSoFar(props.month))
const attendanceRate = computed(() => (workdays.value ? Math.min(100, Math.round(((props.overview.onDutyDays || 0) / workdays.value) * 1000) / 10) : 0))
const rateDonut = computed(() => `conic-gradient(#1f9d74 0 ${attendanceRate.value * 3.6}deg, #f0b429 ${attendanceRate.value * 3.6}deg ${Math.min(360, attendanceRate.value * 3.6 + 12)}deg, #e9eeeb 0)`)
const annual = computed(() => balance.value.find((b) => b.leaveType === 'ANNUAL'))
const overtime = computed(() => overtimeHours(overtimeItems.value.filter((a) => dayjs(a.startTime).format('YYYY-MM') === props.month)))
const actions = computed(() => {
  const status = String(props.overview.todayStatus || '').toUpperCase()
  if (!status || status === 'NOT_CHECKED_IN') return [{ key: 'IN' as PunchAction, label: '上班打卡', primary: true }]
  if (status === 'ON_DUTY') return [{ key: 'OUT' as PunchAction, label: '下班打卡', primary: true }, { key: 'START_LUNCH' as PunchAction, label: '开始午休' }, { key: 'START_OUTING' as PunchAction, label: '外出报备' }]
  if (status === 'LUNCH_BREAK') return [{ key: 'END_LUNCH' as PunchAction, label: '结束午休', primary: true }]
  if (status === 'OUTING') return [{ key: 'END_OUTING' as PunchAction, label: '外出返回', primary: true }]
  return []
})

function dayTone(record: AttendanceDashboardDayItem | undefined, date: string) {
  if (!record || dayjs(date).isAfter(dayjs(), 'day')) return 'none'
  if (record.hasLeave) return 'leave'
  if (record.anomalyText && !record.late && !record.earlyLeave) return 'bad'
  if (record.late || record.earlyLeave) return 'late'
  return record.checkInTime ? 'ok' : 'none'
}
function dayTitle(record?: AttendanceDashboardDayItem) {
  if (!record) return ''
  return [record.checkInTime && `上班 ${dayjs(record.checkInTime).format('HH:mm')}`, record.checkOutTime && `下班 ${dayjs(record.checkOutTime).format('HH:mm')}`,
    record.hasLeave && (record.leaveType || '请假'), record.anomalyText].filter(Boolean).join(' · ')
}
function ring(b: { quotaDays: number; remainingDays: number }) {
  const deg = b.quotaDays ? (Number(b.remainingDays) / Number(b.quotaDays)) * 360 : 0
  return `conic-gradient(#1f9d74 0 ${deg}deg, #e9eeeb ${deg}deg 360deg)`
}
function go(path: string) { router.push(path) }

async function loadRequests() {
  const [b, ...pages] = await Promise.allSettled([
    getLeaveBalance(),
    ...['LEAVE', 'OVERTIME', 'SHIFT_CHANGE'].map((type) => getApprovalPage({ pageNo: 1, pageSize: 20, mine: true, type }))
  ])
  balance.value = b.status === 'fulfilled' ? b.value : []
  const all = pages.flatMap((r) => (r.status === 'fulfilled' ? (r.value as { list?: OaApproval[] }).list || [] : []))
  records.value = [...all].sort((x, y) => String(y.startTime || '').localeCompare(String(x.startTime || ''))).slice(0, 4)
  overtimeItems.value = all.filter((a) => a.approvalType === 'OVERTIME' && a.status === 'APPROVED')
}
onMounted(loadRequests)
watch(() => props.month, loadRequests)
</script>

<style scoped>
.asp-row { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1.3fr) minmax(0, 1fr); gap: 16px; }
.asp-status { display: flex; align-items: center; gap: 12px; padding: 14px; margin-bottom: 6px; border-radius: 12px; background: #eef8f2; }
.asp-status--warn { background: #fff6ea; }
.asp-status__icon { font-size: 30px; color: #1f9d74; }
.asp-status--warn .asp-status__icon { color: #e0962b; }
.asp-status div { display: grid; }
.asp-status b { font-size: 17px; color: #1d3a2f; }
.asp-status small { color: #6b7a74; }
.asp-cal { display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); gap: 4px; text-align: center; }
.asp-cal__week { color: #6b7a74; font-size: 12px; padding: 2px 0; }
.asp-cal__day { position: relative; padding: 6px 0 10px; border-radius: 8px; font-size: 13px; color: #33433a; }
.asp-cal__day i { position: absolute; left: 50%; bottom: 3px; width: 6px; height: 6px; margin-left: -3px; border-radius: 50%; }
.asp-cal__day--ok i { background: #1f9d74; }
.asp-cal__day--late i { background: #f0b429; }
.asp-cal__day--leave i { background: #2f6fd1; }
.asp-cal__day--bad i { background: #e5675a; }
.asp-cal__day--today { background: #1f7a59; color: #fff; font-weight: 700; }
.asp-cal__legend { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 10px; color: #6b7a74; font-size: 12px; }
.asp-cal__legend i { display: inline-block; width: 7px; height: 7px; margin-right: 4px; border-radius: 50%; }
.asp-cal__legend .ok { background: #1f9d74; } .asp-cal__legend .late { background: #f0b429; }
.asp-cal__legend .leave { background: #2f6fd1; } .asp-cal__legend .bad { background: #e5675a; }
.asp-stats { display: flex; align-items: center; gap: 18px; }
.asp-leave { display: grid; grid-template-columns: repeat(auto-fill, minmax(96px, 1fr)); gap: 10px; }
.asp-leave button { display: grid; justify-items: center; gap: 6px; padding: 10px 6px; border: 1px solid #e8eee9; border-radius: 12px; background: #fafcfb; cursor: pointer; }
.asp-ring { width: 74px; height: 74px; }
.asp-ring::before { inset: 8px; }
.asp-ring > span { position: relative; display: grid; text-align: center; line-height: 1.1; }
.asp-ring b { font-size: 18px; }
.asp-ring small { font-size: 11px; color: #6b7a74; }
.asp-fix { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px; }
.asp-fix > div { display: grid; justify-items: center; gap: 6px; padding: 14px 6px; border-radius: 12px; text-align: center; font-size: 22px; }
.asp-fix b { font-size: 14px; color: #22332e; }
.asp-fix small { font-size: 12px; color: #6b7a74; }
@media (max-width: 1200px) { .asp-row { grid-template-columns: 1fr 1fr; } }
@media (max-width: 860px) { .asp-row, .asp-fix { grid-template-columns: 1fr; } }
</style>
