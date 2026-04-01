<template>
  <button class="bed-info-card" :class="[`is-${statusMeta.key}`, isAlert ? 'is-alert' : '']" @click="handleClick">
    <div class="bed-card-top">
      <div>
        <div class="bed-code">{{ bed.roomNo || '-' }} / {{ bed.bedNo || '-' }}</div>
        <div class="bed-name">{{ bed.elderName || '空床待命' }}</div>
      </div>
      <div class="bed-status-chip">{{ statusMeta.label }}</div>
    </div>

    <div class="bed-card-center">
      <div class="bed-silhouette">
        <span class="bed-frame"></span>
        <span class="bed-mattress"></span>
        <span class="bed-signal"></span>
      </div>
      <div class="bed-state-copy">
        <div class="state-title">{{ stateText }}</div>
        <div class="state-subtitle">{{ subText }}</div>
      </div>
    </div>

    <div class="bed-card-metrics">
      <div class="metric-item">
        <span class="metric-label">心率</span>
        <strong>{{ heartRate }}</strong>
      </div>
      <div class="metric-item">
        <span class="metric-label">呼吸</span>
        <strong>{{ breathRate }}</strong>
      </div>
      <div class="metric-item">
        <span class="metric-label">异常</span>
        <strong>{{ bed.abnormalVital24hCount || 0 }}</strong>
      </div>
    </div>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  bed: any
}>()

const emit = defineEmits(['click'])

const isAlert = computed(() => {
  return props.bed.riskLevel === 'HIGH' || props.bed.status === 0 || Number(props.bed.abnormalVital24hCount || 0) > 0
})

const statusMeta = computed(() => {
  if (props.bed.status === 2) return { key: 'maintenance', label: '维修' }
  if (props.bed.status === 3) return { key: 'cleaning', label: '清洁中' }
  if (!props.bed.elderId && String(props.bed.bedNo || '').endsWith('R')) return { key: 'reserved', label: '预定' }
  if (!props.bed.elderId) return { key: 'idle', label: '空闲' }
  if (props.bed.riskLevel === 'HIGH') return { key: 'alert', label: '告警' }
  if (props.bed.riskLevel === 'MEDIUM') return { key: 'warning', label: '关注' }
  if (props.bed.riskLevel === 'LOW') return { key: 'sleep', label: '睡眠' }
  return { key: 'occupied', label: '在住' }
})

const stateText = computed(() => {
  if (!props.bed.elderId) return '床位空闲，可执行分配'
  if (props.bed.status === 2) return '设备维护中'
  if (props.bed.status === 3) return '清洁与消杀执行中'
  if (isAlert.value) return props.bed.riskLabel || '异常监测触发'
  if (props.bed.riskLevel === 'LOW') return '睡眠状态平稳'
  return '生命体征监测正常'
})

const subText = computed(() => {
  return props.bed.careLevel || props.bed.riskSource || '实时床态采集中'
})

function hashSeed() {
  const source = `${props.bed.id || ''}${props.bed.bedNo || ''}${props.bed.roomNo || ''}`
  return source.split('').reduce((sum: number, char: string) => sum + char.charCodeAt(0), 0)
}

const heartRate = computed(() => {
  if (!props.bed.elderId) return '--'
  return 62 + (hashSeed() % 22)
})

const breathRate = computed(() => {
  if (!props.bed.elderId) return '--'
  return 15 + (hashSeed() % 6)
})

function handleClick() {
  emit('click', props.bed)
}
</script>

<style scoped>
.bed-info-card {
  width: 100%;
  border: 1px solid rgba(87, 215, 255, 0.2);
  border-radius: 18px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(242, 249, 255, 0.98) 100%);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.8),
    0 12px 28px rgba(73, 130, 178, 0.12);
  color: #173854;
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.28s ease, border-color 0.28s ease, box-shadow 0.28s ease;
}

.bed-info-card:hover {
  transform: translateY(-3px);
  border-color: rgba(87, 215, 255, 0.42);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.86),
    0 16px 32px rgba(73, 130, 178, 0.16),
    0 0 24px rgba(44, 179, 255, 0.1);
}

.bed-info-card.is-alert {
  border-color: rgba(255, 93, 124, 0.56);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.86),
    0 16px 32px rgba(73, 130, 178, 0.16),
    0 0 24px rgba(255, 93, 124, 0.14);
}

.bed-card-top,
.bed-card-center,
.bed-card-metrics {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.bed-card-top {
  margin-bottom: 14px;
}

.bed-code {
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #6d8aa3;
}

.bed-name {
  margin-top: 4px;
  font-size: 17px;
  font-weight: 700;
}

.bed-status-chip {
  flex-shrink: 0;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(87, 215, 255, 0.14);
  border: 1px solid rgba(87, 215, 255, 0.2);
  font-size: 12px;
  font-weight: 700;
  color: #18506f;
}

.bed-card-center {
  align-items: stretch;
  margin-bottom: 14px;
}

.bed-silhouette {
  position: relative;
  width: 90px;
  min-width: 90px;
  border-radius: 16px;
  background:
    linear-gradient(180deg, rgba(87, 215, 255, 0.08) 0%, rgba(87, 215, 255, 0.02) 100%);
  border: 1px solid rgba(87, 215, 255, 0.12);
  overflow: hidden;
}

.bed-silhouette::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    repeating-linear-gradient(0deg, transparent, transparent 14px, rgba(87, 215, 255, 0.06) 14px, rgba(87, 215, 255, 0.06) 15px);
}

.bed-frame,
.bed-mattress,
.bed-signal {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.bed-frame {
  top: 18px;
  width: 54px;
  height: 34px;
  border: 2px solid rgba(130, 169, 204, 0.7);
  border-bottom-width: 4px;
  border-radius: 8px 8px 4px 4px;
}

.bed-mattress {
  top: 24px;
  width: 40px;
  height: 18px;
  border-radius: 6px;
  background: linear-gradient(90deg, var(--cockpit-cyan), var(--cockpit-blue));
  box-shadow: 0 0 16px rgba(87, 215, 255, 0.38);
}

.bed-signal {
  bottom: 14px;
  width: 46px;
  height: 8px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(87, 215, 255, 0.12), rgba(87, 215, 255, 0.72), rgba(87, 215, 255, 0.12));
  animation: bedSignal 2.4s linear infinite;
}

.bed-state-copy {
  display: grid;
  gap: 6px;
  align-content: center;
}

.state-title {
  font-size: 14px;
  font-weight: 700;
  color: #173854;
}

.state-subtitle {
  font-size: 12px;
  color: #6d8aa3;
  line-height: 1.6;
}

.bed-card-metrics {
  padding-top: 12px;
  border-top: 1px solid rgba(87, 215, 255, 0.12);
}

.metric-item {
  display: grid;
  gap: 4px;
}

.metric-label {
  font-size: 11px;
  color: #6d8aa3;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.metric-item strong {
  font-size: 16px;
  font-weight: 700;
  color: #173854;
}

.is-idle .bed-status-chip,
.is-maintenance .bed-status-chip {
  color: #c8d4df;
  background: rgba(125, 145, 165, 0.14);
  border-color: rgba(125, 145, 165, 0.2);
}

.is-occupied .bed-status-chip {
  color: #c6ffef;
  background: rgba(62, 232, 181, 0.12);
  border-color: rgba(62, 232, 181, 0.22);
}

.is-sleep .bed-status-chip {
  color: #eadbff;
  background: rgba(155, 123, 255, 0.14);
  border-color: rgba(155, 123, 255, 0.24);
}

.is-warning .bed-status-chip,
.is-cleaning .bed-status-chip,
.is-reserved .bed-status-chip {
  color: #ffe1bc;
  background: rgba(255, 174, 87, 0.14);
  border-color: rgba(255, 174, 87, 0.24);
}

.is-alert .bed-status-chip {
  color: #ffd3dc;
  background: rgba(255, 93, 124, 0.16);
  border-color: rgba(255, 93, 124, 0.28);
}

.is-idle .bed-mattress,
.is-maintenance .bed-mattress {
  background: linear-gradient(90deg, #70839a, #94a6ba);
  box-shadow: none;
}

.is-occupied .bed-mattress {
  background: linear-gradient(90deg, #25c78d, #53f0c0);
}

.is-sleep .bed-mattress {
  background: linear-gradient(90deg, #7c61ff, #9e88ff);
}

.is-warning .bed-mattress,
.is-cleaning .bed-mattress,
.is-reserved .bed-mattress {
  background: linear-gradient(90deg, #ff9a53, #ffbf74);
}

.is-alert .bed-mattress {
  background: linear-gradient(90deg, #ff5875, #ff7f92);
}

@keyframes bedSignal {
  0% {
    transform: translateX(-50%) scaleX(0.88);
    opacity: 0.35;
  }

  50% {
    transform: translateX(-50%) scaleX(1.02);
    opacity: 1;
  }

  100% {
    transform: translateX(-50%) scaleX(0.88);
    opacity: 0.35;
  }
}
</style>
