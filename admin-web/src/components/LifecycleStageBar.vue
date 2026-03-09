<template>
  <div class="lifecycle-stage-bar">
    <div class="lifecycle-head">
      <div>
        <div class="lifecycle-title">{{ title }}</div>
        <div v-if="subject" class="lifecycle-subject">{{ subject }}</div>
      </div>
      <a-space :size="8">
        <a-tag :color="stageTagColor">{{ stageTagText }}</a-tag>
        <span v-if="generatedAtText" class="lifecycle-time">更新 {{ generatedAtText }}</span>
      </a-space>
    </div>

    <div class="lifecycle-track">
      <div
        v-for="(item, index) in normalizedSteps"
        :key="item.key"
        class="lifecycle-step"
        :class="stepClass(index)"
      >
        <span class="lifecycle-dot">{{ index + 1 }}</span>
        <span class="lifecycle-label">{{ item.label }}</span>
        <span v-if="item.description" class="lifecycle-desc">{{ item.description }}</span>
      </div>
    </div>

    <a-progress
      class="lifecycle-progress"
      :percent="progressPercent"
      :show-info="false"
      :stroke-color="progressStrokeColor"
    />

    <div v-if="hint" class="lifecycle-hint">{{ hint }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  LIFECYCLE_STAGE_OPTIONS,
  LIFECYCLE_STAGE_ORDER,
  lifecycleStageColor,
  lifecycleStageLabel,
  normalizeLifecycleStage,
  type LifecycleStageKey
} from '../utils/lifecycleStage'

interface StageStep {
  key: LifecycleStageKey
  label: string
  description?: string
}

const props = withDefaults(defineProps<{
  title?: string
  subject?: string
  stage?: string
  stageText?: string
  generatedAt?: string
  hint?: string
  steps?: StageStep[]
}>(), {
  title: '生命周期阶段',
  subject: '',
  stage: 'PENDING_ASSESSMENT',
  stageText: '',
  generatedAt: '',
  hint: ''
})

const normalizedStage = computed(() => normalizeLifecycleStage(props.stage))
const normalizedSteps = computed<StageStep[]>(() => {
  const source = props.steps && props.steps.length ? props.steps : LIFECYCLE_STAGE_OPTIONS
  return source
    .filter((item) => LIFECYCLE_STAGE_ORDER.includes(item.key))
    .sort((a, b) => LIFECYCLE_STAGE_ORDER.indexOf(a.key) - LIFECYCLE_STAGE_ORDER.indexOf(b.key))
})

const currentIndex = computed(() => {
  const index = normalizedSteps.value.findIndex((item) => item.key === normalizedStage.value)
  return index >= 0 ? index : 0
})

const progressPercent = computed(() => {
  const denominator = Math.max(1, normalizedSteps.value.length - 1)
  return Math.round((currentIndex.value / denominator) * 100)
})

const stageTagText = computed(() => props.stageText || lifecycleStageLabel(normalizedStage.value))
const stageTagColor = computed(() => lifecycleStageColor(normalizedStage.value))
const progressStrokeColor = computed(() => {
  if (normalizedStage.value === 'SIGNED') return '#16a34a'
  if (normalizedStage.value === 'PENDING_SIGN') return '#7c3aed'
  if (normalizedStage.value === 'PENDING_BED_SELECT') return '#2563eb'
  return '#ca8a04'
})

const generatedAtText = computed(() => {
  const raw = String(props.generatedAt || '').trim()
  if (!raw) return ''
  const date = new Date(raw)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleString()
})

function stepClass(index: number) {
  if (index < currentIndex.value) return 'is-done'
  if (index === currentIndex.value) return 'is-current'
  return 'is-pending'
}
</script>

<style scoped>
.lifecycle-stage-bar {
  padding: 12px;
  border: 1px solid rgba(37, 99, 235, 0.16);
  border-radius: 12px;
  background:
    radial-gradient(circle at 92% -10%, rgba(59, 130, 246, 0.12) 0%, rgba(59, 130, 246, 0) 55%),
    linear-gradient(135deg, rgba(248, 250, 252, 0.94) 0%, rgba(241, 245, 249, 0.9) 100%);
}

.lifecycle-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.lifecycle-title {
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.2px;
}

.lifecycle-subject {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.lifecycle-time {
  color: #64748b;
  font-size: 12px;
}

.lifecycle-track {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.lifecycle-step {
  min-height: 72px;
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.lifecycle-step:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 16px rgba(15, 23, 42, 0.08);
}

.lifecycle-dot {
  width: 18px;
  height: 18px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  border: 1px solid transparent;
}

.lifecycle-label {
  color: #0f172a;
  font-size: 12px;
  font-weight: 600;
}

.lifecycle-desc {
  color: #64748b;
  font-size: 11px;
  line-height: 1.5;
}

.lifecycle-step.is-done {
  background: rgba(236, 253, 245, 0.86);
  border-color: rgba(22, 163, 74, 0.28);
}

.lifecycle-step.is-done .lifecycle-dot {
  background: rgba(34, 197, 94, 0.18);
  color: #15803d;
  border-color: rgba(22, 163, 74, 0.32);
}

.lifecycle-step.is-current {
  background: rgba(239, 246, 255, 0.95);
  border-color: rgba(37, 99, 235, 0.34);
}

.lifecycle-step.is-current .lifecycle-dot {
  background: rgba(59, 130, 246, 0.18);
  color: #1d4ed8;
  border-color: rgba(37, 99, 235, 0.34);
}

.lifecycle-step.is-pending {
  background: rgba(248, 250, 252, 0.85);
}

.lifecycle-step.is-pending .lifecycle-dot {
  background: rgba(148, 163, 184, 0.16);
  color: #475569;
  border-color: rgba(148, 163, 184, 0.3);
}

.lifecycle-progress {
  margin-top: 10px;
}

.lifecycle-hint {
  margin-top: 8px;
  color: #334155;
  font-size: 12px;
  line-height: 1.6;
}

@media (max-width: 992px) {
  .lifecycle-track {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
