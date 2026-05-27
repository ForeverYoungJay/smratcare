<template>
  <div class="risk-list">
    <button
      v-for="item in items"
      :key="item.key"
      type="button"
      class="risk-list__item"
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
        <small>{{ item.actionLabel || '查看' }}</small>
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
  padding: 14px 16px;
  border: 1px solid rgba(214, 224, 232, 0.9);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.92);
  text-align: left;
  cursor: pointer;
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

