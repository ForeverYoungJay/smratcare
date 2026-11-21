<template>
  <PageContainer title="考勤核定与调整" sub-title="原始打卡 → 异常核实 → 部门确认 → 人事核定锁定；锁定后只能通过审批的调整单修改">
    <div class="ac-grid">
      <section class="ac-card">
        <div class="ac-head">
          <b>月度核定锁定</b>
          <a-space v-if="isManager">
            <a-date-picker v-model:value="period" picker="month" value-format="YYYY-MM" :disabled-date="notEnded" />
            <a-popconfirm :title="`锁定 ${period} 考勤？锁定后作为薪酬输入，只能走调整单修改`" @confirm="lock">
              <a-button type="primary" :disabled="!period">核定锁定</a-button>
            </a-popconfirm>
          </a-space>
        </div>
        <a-empty v-if="!periods.length" description="还没有锁定的月份" />
        <div v-for="p in periods" :key="String(p.id)" class="ac-row">
          <b>{{ p.period }}</b><span>已锁定 · {{ p.lockedByName }} {{ fmt(p.lockedAt) }}</span>
        </div>
      </section>

      <section class="ac-card">
        <div class="ac-head">
          <b>考勤调整单</b>
          <a-radio-group v-model:value="status" size="small" @change="loadAdjustments">
            <a-radio-button value="PENDING">待审批</a-radio-button>
            <a-radio-button value="">全部</a-radio-button>
          </a-radio-group>
        </div>
        <a-empty v-if="!adjustments.length" description="暂无调整单（在考勤记录中对某条记录“申请调整”）" />
        <div v-for="a in adjustments" :key="String(a.id)" class="ac-row ac-row--wide">
          <div>
            <b>{{ a.workDate || '-' }}</b> · {{ a.reason }}
            <div class="ac-muted">
              {{ a.newCheckIn ? `签到改为 ${fmt(a.newCheckIn)} ` : '' }}{{ a.newCheckOut ? `签退改为 ${fmt(a.newCheckOut)} ` : '' }}{{ a.newStatus ? `状态改为 ${a.newStatus}` : '' }}
              · 申请人 {{ a.requestedByName }}
            </div>
            <div v-if="a.decidedByName" class="ac-muted">{{ a.decidedByName }} {{ a.status === 'APPROVED' ? '通过' : '驳回' }}{{ a.decisionNote ? `：${a.decisionNote}` : '' }}</div>
          </div>
          <a-space v-if="a.status === 'PENDING' && isManager">
            <a-button size="small" type="primary" @click="decide(a, true)">通过</a-button>
            <a-button size="small" @click="decide(a, false)">驳回</a-button>
          </a-space>
          <a-tag v-else :color="a.status === 'APPROVED' ? 'green' : a.status === 'REJECTED' ? 'red' : 'orange'">{{ STATUS[a.status] }}</a-tag>
        </div>
      </section>
    </div>

    <section v-if="isManager" class="ac-card" style="margin-top: 16px">
      <div class="ac-head">
        <b>年度假期额度</b>
        <span class="ac-muted">员工在「考勤与请假」看到的余额 = 额度 − 本年已批准的同类请假天数</span>
      </div>
      <a-space wrap>
        <a-select v-model:value="quota.staffId" show-search :filter-option="false" :options="staffOptions" :loading="staffLoading"
          placeholder="选择员工" style="width: 200px" @search="searchStaff" @focus="() => !staffOptions.length && searchStaff('')" />
        <a-input-number v-model:value="quota.year" :min="2020" :max="2100" style="width: 100px" />
        <a-select v-model:value="quota.leaveType" :options="leaveTypeOptions" style="width: 110px" />
        <a-input-number v-model:value="quota.quotaDays" :min="0" :max="365" :step="0.5" addon-after="天" style="width: 140px" />
        <a-button type="primary" @click="saveQuota">保存额度</a-button>
      </a-space>
      <div class="ac-quota-list">
        <span v-for="q in quotas" :key="String(q.id)" class="ac-quota">{{ staffName(q.staffId) }} · {{ LEAVE_TYPES[q.leaveType] || q.leaveType }} {{ q.quotaDays }} 天</span>
      </div>
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { decideAttendanceAdjustment, getAttendanceAdjustments, getAttendancePeriods, getLeaveQuotas, lockAttendancePeriod, saveLeaveQuota, type AttendanceAdjustment, type AttendancePeriod, type LeaveQuota } from '../../api/hrOps'
import { useStaffOptions } from '../../composables/useStaffOptions'
import { LEAVE_TYPES } from '../oa/myOffice'
import { useUserStore } from '../../stores/user'

const STATUS: Record<string, string> = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回' }
const user = useUserStore()
const isManager = computed(() => (user.roles || []).some((r) => ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'].includes(String(r).toUpperCase())))
const period = ref(dayjs().subtract(1, 'month').format('YYYY-MM'))
const periods = ref<AttendancePeriod[]>([])
const adjustments = ref<AttendanceAdjustment[]>([])
const status = ref('PENDING')

const fmt = (value?: string) => (value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '')
const notEnded = (d: Dayjs) => !d.isBefore(dayjs().startOf('month'))

async function loadAdjustments() {
  adjustments.value = (await getAttendanceAdjustments({ pageNo: 1, pageSize: 50, status: status.value || undefined })).list || []
}
const { staffOptions, staffLoading, searchStaff, findStaffName } = useStaffOptions({ pageSize: 50 })
const leaveTypeOptions = Object.entries(LEAVE_TYPES).map(([value, label]) => ({ value, label }))
const quota = reactive({ staffId: undefined as string | undefined, year: dayjs().year(), leaveType: 'ANNUAL', quotaDays: 5 })
const quotas = ref<LeaveQuota[]>([])
const staffName = (id: LeaveQuota['staffId']) => findStaffName(String(id)) || `员工 ${id}`

async function loadQuotas() {
  quotas.value = await getLeaveQuotas(quota.year).catch(() => [])
}

async function saveQuota() {
  if (!quota.staffId) return message.warning('请选择员工')
  await saveLeaveQuota({ staffId: quota.staffId, year: quota.year, leaveType: quota.leaveType, quotaDays: quota.quotaDays })
  message.success('额度已保存')
  await loadQuotas()
}

async function load() {
  if (isManager.value) loadQuotas()
  periods.value = await getAttendancePeriods()
  await loadAdjustments()
}
async function lock() {
  await lockAttendancePeriod(period.value)
  message.success(`${period.value} 考勤已锁定`)
  await load()
}
async function decide(a: AttendanceAdjustment, approve: boolean) {
  await decideAttendanceAdjustment(a.id, approve, approve ? '同意' : '依据不足')
  message.success(approve ? '已通过，考勤记录已更正' : '已驳回')
  await loadAdjustments()
}

onMounted(load)
</script>

<style scoped>
.ac-grid { display: grid; grid-template-columns: minmax(280px, 1fr) minmax(0, 2fr); gap: 16px; }
.ac-card { padding: 16px; border-radius: 12px; background: #fff; }
.ac-head { display: flex; justify-content: space-between; align-items: center; gap: 12px; margin-bottom: 12px; }
.ac-row { display: flex; justify-content: space-between; gap: 12px; padding: 10px 0; border-bottom: 1px solid #eef2ef; font-size: 13px; }
.ac-quota-list { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 10px; }
.ac-quota { padding: 2px 10px; border-radius: 999px; background: #f4f1fb; font-size: 12px; }
.ac-row span, .ac-muted { color: #7b877e; font-size: 12px; }
@media (max-width: 1100px) { .ac-grid { grid-template-columns: 1fr; } }
</style>
