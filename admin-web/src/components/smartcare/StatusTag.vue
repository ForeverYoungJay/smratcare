<template>
  <span class="status-tag" :class="`status-tag--${tone}`">
    <span class="status-tag__dot"></span>
    {{ text }}
  </span>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  text: string
  tone?: 'normal' | 'pending' | 'warning' | 'danger' | 'offline'
}>(), {
  tone: 'normal'
})
</script>

<style scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  border: 1px solid transparent;
}

.status-tag__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

.status-tag--normal {
  color: var(--success);
  background: rgba(var(--success-rgb), 0.12);
  border-color: rgba(var(--success-rgb), 0.2);
}

.status-tag--pending {
  color: var(--primary-strong);
  background: rgba(var(--primary-rgb), 0.12);
  border-color: rgba(var(--primary-rgb), 0.18);
}

.status-tag--warning {
  color: var(--warning);
  background: rgba(var(--warning-rgb), 0.12);
  border-color: rgba(var(--warning-rgb), 0.2);
}

.status-tag--danger {
  color: var(--danger);
  background: rgba(var(--danger-rgb), 0.12);
  border-color: rgba(var(--danger-rgb), 0.2);
}

.status-tag--offline {
  color: var(--offline);
  background: rgba(100, 116, 139, 0.12);
  border-color: rgba(100, 116, 139, 0.2);
}
</style>

