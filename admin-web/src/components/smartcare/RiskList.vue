<template>
  <div class="risk-list">
    <button
      v-for="item in items"
      :key="item.key"
      type="button"
      class="risk-list__item"
      :class="`risk-list__item--${item.tone}`"
      @click="$emit('select', item)"
    >
      <div class="risk-list__main">
        <StatusTag :text="item.levelText" :tone="item.tone" />
        <div class="risk-list__copy">
          <strong>{{ item.title }}</strong>
          <span>{{ item.description }}</span>
        </div>
      </div>
      <div class="risk-list__meta">
        <b>{{ item.value }}</b>
        <span class="risk-list__action" :class="`risk-list__action--${item.tone}`">
          {{ item.actionLabel || '查看' }}
          <span aria-hidden="true">→</span>
        </span>
      </div>
    </button>
    <a-empty v-if="!items.length" description="暂无预警" />
  </div>
</template>

<script setup lang="ts">
import StatusTag from './StatusTag.vue'

export type RiskListItem = {
  actionLabel?: string
  description: string
  key: string
  levelText: string
  title: string
  tone: 'normal' | 'pending' | 'warning' | 'danger' | 'offline'
  value: number | string
}

defineProps<{
  items: RiskListItem[]
}>()

defineEmits<{
  (e: 'select', item: RiskListItem): void
}>()
</script>

<style scoped>
.risk-list {
  display: grid;
  gap: 10px;
}

.risk-list__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  padding: 14px 16px 14px 18px;
  border: 1px solid rgba(214, 224, 232, 0.9);
  border-left: 4px solid rgba(148, 163, 184, 0.5);
  border-radius: 14px;
  background: #ffffff;
  box-shadow: var(--shadow-xs);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.risk-list__item:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm, 0 6px 16px rgba(15, 23, 42, 0.08));
}

.risk-list__item--danger {
  border-left-color: var(--danger);
  background: linear-gradient(90deg, rgba(var(--danger-rgb), 0.06), #ffffff 40%);
}

.risk-list__item--warning {
  border-left-color: var(--warning);
  background: linear-gradient(90deg, rgba(var(--warning-rgb), 0.06), #ffffff 40%);
}

.risk-list__item--pending {
  border-left-color: var(--primary-strong);
}

.risk-list__action {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  border-radius: 999px;
  background: rgba(100, 116, 139, 0.12);
  color: var(--ink);
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.risk-list__action--danger {
  background: rgba(var(--danger-rgb), 0.14);
  color: var(--danger);
}

.risk-list__action--warning {
  background: rgba(var(--warning-rgb), 0.16);
  color: var(--warning);
}

.risk-list__action--pending {
  background: rgba(var(--primary-rgb), 0.12);
  color: var(--primary-strong);
}

.risk-list__main {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.risk-list__copy {
  display: grid;
  gap: 4px;
}

.risk-list__copy strong {
  color: var(--ink);
}

.risk-list__copy span,
.risk-list__meta small {
  color: var(--muted);
  font-size: 12px;
}

.risk-list__meta {
  display: grid;
  justify-items: end;
  gap: 4px;
}

.risk-list__meta b {
  color: var(--ink);
  font-size: 18px;
}
</style>

