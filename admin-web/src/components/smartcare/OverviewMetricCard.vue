<template>
  <button type="button" class="overview-metric-card" :class="[`overview-metric-card--${tone}`, { 'is-clickable': !!clickable }]" @click="handleClick">
    <div class="overview-metric-card__head">
      <span>{{ label }}</span>
      <StatusTag v-if="statusText" :text="statusText" :tone="statusTone" />
    </div>
    <strong>{{ displayValue }}</strong>
    <div class="overview-metric-card__meta">
      <span>{{ helper }}</span>
      <small v-if="suffix">{{ suffix }}</small>
    </div>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import StatusTag from './StatusTag.vue'

const props = withDefaults(defineProps<{
  clickable?: boolean
  helper?: string
  label: string
  statusText?: string
  statusTone?: 'normal' | 'pending' | 'warning' | 'danger' | 'offline'
  suffix?: string
  tone?: 'default' | 'brand' | 'warning' | 'danger' | 'success'
  value?: number | string | null
}>(), {
  clickable: false,
  helper: '',
  statusText: '',
  statusTone: 'pending',
  suffix: '',
  tone: 'default',
  value: '--'
})

const emit = defineEmits<{
  (e: 'click'): void
}>()

const displayValue = computed(() => {
  if (props.value === null || props.value === undefined || props.value === '') return '--'
  return props.value
})

function handleClick() {
  if (!props.clickable) return
  emit('click')
}
</script>

<style scoped>
.overview-metric-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
  width: 100%;
  padding: 18px;
  border: 1px solid rgba(214, 225, 233, 0.92);
  border-radius: 20px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(246, 251, 253, 0.96));
  box-shadow: var(--shadow-xs);
  text-align: left;
}

.overview-metric-card.is-clickable {
  cursor: pointer;
}

.overview-metric-card__head,
.overview-metric-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.overview-metric-card__head span,
.overview-metric-card__meta span,
.overview-metric-card__meta small {
  color: var(--muted);
  font-size: 12px;
}

.overview-metric-card strong {
  color: var(--ink);
  font-size: 32px;
  line-height: 1;
}

.overview-metric-card--brand {
  background: linear-gradient(135deg, rgba(19, 108, 181, 0.12), rgba(30, 138, 143, 0.08), rgba(255, 255, 255, 0.96));
}

.overview-metric-card--warning {
  background: linear-gradient(135deg, rgba(var(--warning-rgb), 0.16), rgba(255, 255, 255, 0.96));
}

.overview-metric-card--danger {
  background: linear-gradient(135deg, rgba(var(--danger-rgb), 0.16), rgba(255, 255, 255, 0.96));
}

.overview-metric-card--success {
  background: linear-gradient(135deg, rgba(var(--success-rgb), 0.16), rgba(255, 255, 255, 0.96));
}
</style>

