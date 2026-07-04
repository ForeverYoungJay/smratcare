<template>
  <span class="status-tag" :class="`status-tag--${tone}`">
    <span class="status-tag__icon" aria-hidden="true">{{ toneIcon }}</span>
    {{ text }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  text: string
  tone?: 'normal' | 'pending' | 'warning' | 'danger' | 'offline'
}>(), {
  tone: 'normal'
})

// 除颜色外再用一个形状符号区分状态，便于色弱用户与打印场景识别。
const toneIcon = computed(() => {
  switch (props.tone) {
    case 'danger':
      return '✕'
    case 'warning':
      return '!'
    case 'pending':
      return '◔'
    case 'offline':
      return '·'
    default:
      return '✓'
  }
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

.status-tag__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: currentColor;
  color: #ffffff;
  font-size: 9px;
  font-weight: 900;
  line-height: 1;
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

