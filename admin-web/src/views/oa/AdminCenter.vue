<template>
  <ModuleHub>
    <template #metrics>
      <div class="hub-metric-row">
        <div class="hub-metric" :class="{ 'is-accent': (summary.openTodoCount || 0) > 0 }">
          <span class="hub-metric__label">未完成待办</span>
          <strong class="hub-metric__value">{{ summary.openTodoCount || 0 }}</strong>
          <small class="hub-metric__hint">需跟进的协同事项</small>
        </div>
        <div class="hub-metric" :class="{ 'is-warning': (summary.pendingApprovalCount || 0) > 0 }">
          <span class="hub-metric__label">审批处理中</span>
          <strong class="hub-metric__value">{{ summary.pendingApprovalCount || 0 }}</strong>
          <small class="hub-metric__hint">待推进的审批单</small>
        </div>
        <div class="hub-metric">
          <span class="hub-metric__label">进行中任务</span>
          <strong class="hub-metric__value">{{ summary.ongoingTaskCount || 0 }}</strong>
          <small class="hub-metric__hint">执行中的行政任务</small>
        </div>
      </div>
    </template>
  </ModuleHub>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import ModuleHub from '../ModuleHub.vue'
import { getPortalSummary } from '../../api/oa'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { useUserStore } from '../../stores/user'
import { hasMinisterOrHigher } from '../../utils/roleAccess'

const userStore = useUserStore()
const summary = reactive({
  openTodoCount: 0,
  pendingApprovalCount: 0,
  ongoingTaskCount: 0
})

async function load() {
  const scope = hasMinisterOrHigher(userStore.roles || []) ? 'ORG' : 'PERSONAL'
  const res = await getPortalSummary({ params: { scope }, silent403: true })
  summary.openTodoCount = Number(res?.openTodoCount || 0)
  summary.pendingApprovalCount = Number(res?.pendingApprovalCount || 0)
  summary.ongoingTaskCount = Number(res?.ongoingTaskCount || 0)
}

onMounted(() => {
  load().catch(() => {})
})

useLiveSyncRefresh({
  topics: ['oa', 'hr', 'system'],
  refresh: () => {
    load().catch(() => {})
  },
  debounceMs: 900
})
</script>

<style scoped>
.hub-metric-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
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
