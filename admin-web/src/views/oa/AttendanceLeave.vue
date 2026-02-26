<template>
  <PageContainer title="考勤与请假" subTitle="查看员工每日上下班签到状态、本月病假次数与额外假期天数">
    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 16px">
      <a-form layout="inline">
        <a-form-item label="员工">
          <a-select v-model:value="query.staffId" style="width: 260px" show-search allow-clear placeholder="请选择员工">
            <a-select-option v-for="staff in staffOptions" :key="staff.value" :value="staff.value">{{ staff.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="月份">
          <a-date-picker v-model:value="query.month" picker="month" value-format="YYYY-MM" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button @click="goLeaveApproval">在线填写请假申请</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="本月正常签到" :value="summary.normalCount" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="本月病假次数" :value="summary.sickLeaveCount" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="额外假期/节日天数" :value="summary.extraHolidayDays" />
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
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getAttendancePage } from '../../api/schedule'
import { getHrStaffPage } from '../../api/hr'
import { getApprovalPage } from '../../api/oa'
import type { AttendanceItem, HrStaffProfile, OaApproval, PageResult } from '../../types'

const router = useRouter()
const loading = ref(false)
const rows = ref<AttendanceItem[]>([])
const approvals = ref<OaApproval[]>([])
const staffList = ref<HrStaffProfile[]>([])

const query = reactive({
  staffId: undefined as number | undefined,
  month: dayjs().format('YYYY-MM')
})

const columns = [
  { title: '签到时间', dataIndex: 'checkInTime', key: 'checkInTime', width: 200 },
  { title: '签退时间', dataIndex: 'checkOutTime', key: 'checkOutTime', width: 200 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 140 }
]

const staffOptions = computed(() =>
  staffList.value.map((item) => ({ label: `${item.realName || '-'}（${item.phone || '无手机号'}）`, value: item.staffId }))
)

const summary = computed(() => {
  const normalCount = rows.value.filter((item) => ['NORMAL', 'ON_TIME'].includes(String(item.status || '').toUpperCase())).length
  const monthStart = dayjs(`${query.month}-01`)
  const monthEnd = monthStart.endOf('month')

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

  return { normalCount, sickLeaveCount, extraHolidayDays }
})

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

    const [attendancePage, approvalPage] = await Promise.all([
      getAttendancePage({
        pageNo: 1,
        pageSize: 500,
        staffId: query.staffId,
        dateFrom: monthStart,
        dateTo: monthEnd
      }),
      getApprovalPage({
        pageNo: 1,
        pageSize: 500,
        type: 'LEAVE',
        status: 'APPROVED',
        keyword: query.staffId ? String(query.staffId) : undefined
      })
    ])

    rows.value = attendancePage.list || []
    approvals.value = approvalPage.list || []
  } finally {
    loading.value = false
  }
}

async function loadStaff() {
  const page: PageResult<HrStaffProfile> = await getHrStaffPage({ pageNo: 1, pageSize: 300 })
  staffList.value = page.list || []
}

function reset() {
  query.staffId = undefined
  query.month = dayjs().format('YYYY-MM')
  fetchData()
}

function goLeaveApproval() {
  router.push('/oa/approval?type=LEAVE&quick=1')
}

onMounted(async () => {
  await loadStaff()
  await fetchData()
})
</script>
