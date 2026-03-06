<template>
  <PageContainer title="考勤与请假" subTitle="查看员工每日上下班签到状态、本月病假次数与额外假期天数">
    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 16px">
      <a-form layout="inline">
        <a-form-item label="员工">
          <a-select
            v-model:value="query.staffId"
            style="width: 260px"
            show-search
            allow-clear
            :filter-option="false"
            :options="staffOptions"
            :loading="staffLoading"
            :disabled="!canManageAttendance"
            placeholder="请选择员工"
            @search="searchStaff"
            @focus="() => !staffOptions.length && searchStaff('')"
          />
        </a-form-item>
        <a-form-item label="月份">
          <a-date-picker v-model:value="query.month" picker="month" value-format="YYYY-MM" />
        </a-form-item>
        <a-form-item label="自动刷新">
          <a-switch v-model:checked="autoRefresh" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button @click="exportAttendance">导出考勤明细</a-button>
            <a-button @click="exportLeaveApprovals">导出请假回写</a-button>
            <a-button @click="goLeaveApproval">在线填写请假申请</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :xs="24" :sm="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="本月正常签到" :value="summary.normalCount" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="本月病假次数" :value="summary.sickLeaveCount" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="额外假期/节日天数" :value="summary.extraHolidayDays" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="年度请假次数" :value="summary.annualLeaveCount" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false">
      <a-table :columns="columns" :data-source="rows" row-key="id" :loading="loading" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ record.status || '-' }}</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <template #title>当月请假审批回写</template>
      <a-table :columns="leaveColumns" :data-source="leaveApprovalRows" row-key="id" :loading="loading" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'APPROVED' ? 'green' : 'orange'">{{ record.status === 'APPROVED' ? '已通过' : '待审批' }}</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { useUserStore } from '../../stores/user'
import { getAttendancePage } from '../../api/schedule'
import { getApprovalPage } from '../../api/oa'
import { hasMinisterOrHigher } from '../../utils/roleAccess'
import { useStaffOptions } from '../../composables/useStaffOptions'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import type { AttendanceItem, OaApproval } from '../../types'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const autoRefresh = ref(true)
let autoRefreshTimer: number | undefined
const rows = ref<AttendanceItem[]>([])
const approvals = ref<OaApproval[]>([])
const { staffOptions, staffLoading, searchStaff } = useStaffOptions({ pageSize: 260, preloadSize: 800 })

const query = reactive({
  staffId: undefined as string | number | undefined,
  month: dayjs().format('YYYY-MM')
})

const columns = [
  { title: '签到时间', dataIndex: 'checkInTime', key: 'checkInTime', width: 200 },
  { title: '签退时间', dataIndex: 'checkOutTime', key: 'checkOutTime', width: 200 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 140 }
]
const leaveColumns = [
  { title: '标题', dataIndex: 'title', key: 'title', width: 220 },
  { title: '请假开始', dataIndex: 'startTime', key: 'startTime', width: 180 },
  { title: '请假结束', dataIndex: 'endTime', key: 'endTime', width: 180 },
  { title: '审批状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '审批意见', dataIndex: 'approvalOpinion', key: 'approvalOpinion', width: 220 },
  { title: '年度累计', dataIndex: 'annualLeaveCount', key: 'annualLeaveCount', width: 120 }
]

const canManageAttendance = computed(() => {
  const roles = (userStore.roles || []).map((item) => String(item || '').toUpperCase())
  return hasMinisterOrHigher(roles)
})

const summary = computed(() => {
  const normalCount = rows.value.filter((item) => ['NORMAL', 'ON_TIME'].includes(String(item.status || '').toUpperCase())).length
  const monthStart = dayjs(`${query.month}-01`)
  const monthEnd = monthStart.endOf('month')
  const currentYear = monthStart.year()

  const leaveApprovals = approvals.value.filter((item) => {
    const inMonth = item.startTime ? dayjs(item.startTime).isBetween(monthStart, monthEnd, null, '[]') : false
    return item.approvalType === 'LEAVE' && item.status === 'APPROVED' && inMonth
  })

  const sickLeaveCount = leaveApprovals.filter((item) => {
    const text = `${item.title || ''} ${item.remark || ''} ${item.formData || ''}`
    return text.includes('病假')
  }).length

  const extraHolidayDays = leaveApprovals.filter((item) => {
    const text = `${item.title || ''} ${item.remark || ''} ${item.formData || ''}`
    return text.includes('调休') || text.includes('节日') || text.includes('额外假')
  }).length

  const annualLeaveCount = approvals.value
    .filter((item) => item.approvalType === 'LEAVE' && item.status === 'APPROVED')
    .filter((item) => {
      const startYear = item.startTime ? dayjs(item.startTime).year() : currentYear
      return startYear === currentYear
    })
    .reduce((maxCount, item) => {
      const parsed = parseApprovalFormData(item.formData)
      const count = Number(parsed?.annualLeaveApprovedCount || 0)
      return count > maxCount ? count : maxCount
    }, 0)

  return { normalCount, sickLeaveCount, extraHolidayDays, annualLeaveCount }
})

const leaveApprovalRows = computed(() => {
  const monthStart = dayjs(`${query.month}-01`)
  const monthEnd = monthStart.endOf('month')
  return approvals.value
    .filter((item) => item.approvalType === 'LEAVE')
    .filter((item) => {
      const inMonth = item.startTime ? dayjs(item.startTime).isBetween(monthStart, monthEnd, null, '[]') : false
      return inMonth
    })
    .map((item) => {
      const parsed = parseApprovalFormData(item.formData)
      return {
        ...item,
        approvalOpinion: parsed?.lastApprovalRemark || item.remark || '-',
        annualLeaveCount: Number(parsed?.annualLeaveApprovedCount || 0) || '-'
      }
    })
})

function parseApprovalFormData(formData?: string) {
  if (!formData) return {}
  try {
    const parsed = JSON.parse(formData)
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch {
    return {}
  }
}

function statusColor(status?: string) {
  const val = String(status || '').toUpperCase()
  if (val === 'NORMAL' || val === 'ON_TIME') return 'green'
  if (val.includes('LATE') || val.includes('ABNORMAL')) return 'orange'
  return 'default'
}

async function fetchData() {
  loading.value = true
  try {
    const monthStart = dayjs(`${query.month}-01`).format('YYYY-MM-DD')
    const monthEnd = dayjs(`${query.month}-01`).endOf('month').format('YYYY-MM-DD')
    const currentStaffId = userStore.staffInfo?.id ? Number(userStore.staffInfo.id) : undefined
    const selectedStaffId = query.staffId != null && query.staffId !== '' ? Number(query.staffId) : undefined
    const effectiveStaffId = canManageAttendance.value ? selectedStaffId : currentStaffId

    const [attendancePage, approvalPage] = await Promise.all([
      getAttendancePage({
        pageNo: 1,
        pageSize: 500,
        staffId: effectiveStaffId,
        dateFrom: monthStart,
        dateTo: monthEnd
      }),
      getApprovalPage({
        pageNo: 1,
        pageSize: 500,
        type: 'LEAVE',
        applicantId: effectiveStaffId
      })
    ])

    rows.value = attendancePage.list || []
    approvals.value = approvalPage.list || []
  } catch (error: any) {
    message.error(error?.message || '加载考勤与请假数据失败')
  } finally {
    loading.value = false
  }
}

function reset() {
  query.staffId = canManageAttendance.value ? undefined : userStore.staffInfo?.id ? String(userStore.staffInfo.id) : undefined
  query.month = dayjs().format('YYYY-MM')
  fetchData()
}

function goLeaveApproval() {
  router.push('/oa/approval?type=LEAVE&quick=1')
}

function exportAttendance() {
  exportCsv(
    rows.value.map((item) => ({
      签到时间: item.checkInTime || '',
      签退时间: item.checkOutTime || '',
      状态: item.status || ''
    })),
    `考勤明细-${query.month}`
  )
}

function exportLeaveApprovals() {
  exportCsv(
    leaveApprovalRows.value.map((item: any) => ({
      标题: item.title || '',
      请假开始: item.startTime || '',
      请假结束: item.endTime || '',
      审批状态: item.status || '',
      审批意见: item.approvalOpinion || '',
      年度累计: item.annualLeaveCount || ''
    })),
    `请假审批回写-${query.month}`
  )
}

function startAutoRefresh() {
  if (!autoRefresh.value || autoRefreshTimer !== undefined) {
    return
  }
  autoRefreshTimer = window.setInterval(() => {
    if (loading.value) return
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
  if (!canManageAttendance.value) {
    query.staffId = userStore.staffInfo?.id ? String(userStore.staffInfo.id) : undefined
  }
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
