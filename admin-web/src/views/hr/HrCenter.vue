<template>
  <PageContainer title="行政人事部工作台" subTitle="用专业的人力资源管理，支撑更温暖、更高效的颐养服务。" mode="scenic">
    <DepartmentHomeOverview
      department="HR"
      :show-header="false"
      :metrics="metrics"
      :data-state="dataState"
      :data-message="errorMessage"
      :scope-label="managementView ? '部门人员与行政业务' : '当前岗位可用范围'"
      period-label="当前组织"
      :updated-at="updatedAt"
      :refreshing="dataState === 'loading'"
      show-refresh
      @refresh="load"
    />
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import DepartmentHomeOverview from '../../components/workbench/DepartmentHomeOverview.vue'
import { getHrWorkbenchSummary } from '../../api/hr'
import type { HrWorkbenchSummary } from '../../types'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { useUserStore } from '../../stores/user'
import type { DepartmentHomeDataState, DepartmentHomeMetric } from '../../workbench/departmentHome'

const userStore = useUserStore()
const summary = ref<HrWorkbenchSummary | null>(null)
const dataState = ref<DepartmentHomeDataState>('loading')
const errorMessage = ref('')
const updatedAt = ref('')
const managementView = computed(() => (userStore.roles || []).some((role) => ['HR_MINISTER', 'DIRECTOR', 'ADMIN'].includes(String(role).toUpperCase())))

const metrics = computed<DepartmentHomeMetric[]>(() => {
  if (!managementView.value) return [
    { key: 'notice', label: '通知公告', value: '查看', helper: '了解最新工作安排', path: '/oa/notice', tone: 'normal' },
    { key: 'document', label: '制度与文档', value: '查看', helper: '查询业务规范', path: '/oa/document', tone: 'normal' },
    { key: 'attendance', label: '我的考勤', value: '查看', helper: '核对个人考勤与请假', path: '/workbench/attendance', tone: 'normal' }
  ]
  if (!summary.value) return []
  return [
    { key: 'leave', label: '请假待审批', value: summary.value.pendingLeaveApprovalCount, helper: '需要及时处理', path: '/hr/attendance/leave-approval', tone: 'warning' },
    { key: 'attendance', label: '考勤异常', value: summary.value.attendanceAbnormalCount, helper: '待核对异常记录', path: '/hr/attendance/abnormal', tone: 'danger' },
    { key: 'contracts', label: '合同临期', value: summary.value.contractExpiringCount, helper: '预警周期内到期', path: '/hr/profile/contract-reminders', tone: 'warning' },
    { key: 'on-job', label: '在职人数', value: summary.value.onJobCount, helper: '当前在岗员工', path: '/hr/profile/basic', tone: 'normal' }
  ]
})

async function load() {
  dataState.value = 'loading'
  errorMessage.value = ''
  try {
    const data = await getHrWorkbenchSummary(undefined, { silent403: true })
    summary.value = data || null
    dataState.value = data ? 'ready' : 'empty'
    updatedAt.value = dayjs().format('HH:mm')
  } catch (error: any) {
    summary.value = null
    dataState.value = Number(error?.response?.status) === 403 ? 'forbidden' : 'error'
    errorMessage.value = error?.message || '行政人事首页数据加载失败'
  }
}

onMounted(load)

useLiveSyncRefresh({
  topics: ['hr', 'oa', 'system'],
  refresh: load,
  debounceMs: 900
})
</script>
