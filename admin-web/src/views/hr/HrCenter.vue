<template>
  <ModuleHub>
    <template #metrics>
      <div class="hub-metric-row">
        <div class="hub-metric">
          <span class="hub-metric__label">在职人数</span>
          <strong class="hub-metric__value">{{ summary.onJobCount || 0 }}</strong>
          <small class="hub-metric__hint">当前在岗员工</small>
        </div>
        <div class="hub-metric" :class="{ 'is-warning': (summary.contractExpiringCount || 0) > 0 }">
          <span class="hub-metric__label">合同到期预警</span>
          <strong class="hub-metric__value">{{ summary.contractExpiringCount || 0 }}</strong>
          <small class="hub-metric__hint">临近到期员工合同</small>
        </div>
        <div class="hub-metric" :class="{ 'is-warning': (summary.attendanceAbnormalCount || 0) > 0 }">
          <span class="hub-metric__label">考勤异常</span>
          <strong class="hub-metric__value">{{ summary.attendanceAbnormalCount || 0 }}</strong>
          <small class="hub-metric__hint">待处理考勤异常</small>
        </div>
        <div class="hub-metric" :class="{ 'is-accent': (summary.pendingLeaveApprovalCount || 0) > 0 }">
          <span class="hub-metric__label">请假审批待办</span>
          <strong class="hub-metric__value">{{ summary.pendingLeaveApprovalCount || 0 }}</strong>
          <small class="hub-metric__hint">待我审批的请假</small>
        </div>
        <div class="hub-metric">
          <span class="hub-metric__label">今日生日</span>
          <strong class="hub-metric__value">{{ summary.birthdayTodayCount || 0 }}</strong>
          <small class="hub-metric__hint">安排祝福与关怀</small>
        </div>
      </div>
    </template>
  </ModuleHub>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import ModuleHub from '../ModuleHub.vue'
import { getHrWorkbenchSummary } from '../../api/hr'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'

const summary = reactive({
  onJobCount: 0,
  todayTrainingCount: 0,
  pendingLeaveApprovalCount: 0,
  attendanceAbnormalCount: 0,
  contractExpiringCount: 0,
  birthdayTodayCount: 0,
  birthdayUpcomingCount: 0,
  birthdayTodoCount: 0
})

async function load() {
  const res = await getHrWorkbenchSummary(undefined, { silent403: true })
  summary.onJobCount = Number(res?.onJobCount || 0)
  summary.todayTrainingCount = Number(res?.todayTrainingCount || 0)
  summary.pendingLeaveApprovalCount = Number(res?.pendingLeaveApprovalCount || 0)
  summary.attendanceAbnormalCount = Number(res?.attendanceAbnormalCount || 0)
  summary.contractExpiringCount = Number(res?.contractExpiringCount || 0)
  summary.birthdayTodayCount = Number(res?.birthdayTodayCount || 0)
  summary.birthdayUpcomingCount = Number(res?.birthdayUpcomingCount || 0)
  summary.birthdayTodoCount = Number(res?.birthdayTodoCount || 0)
}

onMounted(() => {
  load().catch(() => {})
})

useLiveSyncRefresh({
  topics: ['hr', 'oa', 'system'],
  refresh: () => {
    load().catch(() => {})
  },
  debounceMs: 900
})
</script>

<style scoped>
.hub-metric-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.hub-metric {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 16px 18px;
  border: 1px solid var(--border-strong);
  border-radius: var(--radius-md);
  background: var(--surface);
  box-shadow: var(--shadow-sm);
}

.hub-metric.is-warning {
  border-color: rgba(var(--warning-rgb), 0.5);
  background: linear-gradient(135deg, rgba(var(--warning-rgb), 0.1), var(--surface));
}

.hub-metric.is-accent {
  border-color: rgba(var(--primary-rgb), 0.4);
  background: linear-gradient(135deg, rgba(var(--primary-rgb), 0.08), var(--surface));
}

.hub-metric__label {
  font-size: 12px;
  color: var(--muted);
}

.hub-metric__value {
  font-size: 26px;
  line-height: 1.1;
  color: var(--ink);
}

.hub-metric__hint {
  font-size: 12px;
  color: var(--muted-2);
}
</style>
