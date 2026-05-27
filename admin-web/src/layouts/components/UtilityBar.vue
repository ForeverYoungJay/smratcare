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
  gap: 14px;
  padding: 9px 24px 10px;
  border-bottom: 1px solid rgba(212, 225, 234, 0.72);
  background:
    linear-gradient(180deg, rgba(243, 248, 252, 0.78), rgba(240, 247, 251, 0.56));
}

.utility-bar__summary {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.utility-bar__label {
  color: var(--muted-2);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.1em;
  white-space: nowrap;
}

.utility-bar__summary strong {
  color: var(--ink-soft);
  font-size: 12px;
  font-weight: 600;
  min-width: 0;
}

.utility-bar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

@media (max-width: 992px) {
  .utility-bar {
    padding: 10px 14px;
    flex-direction: column;
    align-items: stretch;
  }

  .utility-bar__summary {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>
