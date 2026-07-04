<template>
  <div class="utility-bar">
    <div class="utility-bar__summary">
      <span class="utility-bar__label">运行与协同</span>
      <strong>{{ summaryText }}</strong>
    </div>
    <div class="utility-bar__actions">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  summaryText: string
}>()
</script>

<style scoped>
.utility-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-height: 36px;
  padding: 4px 16px;
  border-bottom: 1px solid var(--border-soft);
  background: var(--surface-2);
}

.utility-bar__summary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.utility-bar__label {
  color: var(--muted-2);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  white-space: nowrap;
}

.utility-bar__summary strong {
  color: var(--ink-soft);
  font-size: 12px;
  font-weight: 600;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.utility-bar__actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.utility-bar__actions :deep(.ant-btn) {
  height: 30px;
  border-radius: 8px;
  font-size: 12px;
}

.utility-bar__actions :deep(.ant-tag),
.utility-bar__actions :deep(.ant-badge-status-text) {
  font-size: 12px;
}

@media (max-width: 992px) {
  .utility-bar {
    padding: 5px 12px;
    flex-direction: column;
    align-items: stretch;
  }

  .utility-bar__summary {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}

@media (max-width: 1440px) {
  .utility-bar__summary {
    display: none;
  }
}
</style>
