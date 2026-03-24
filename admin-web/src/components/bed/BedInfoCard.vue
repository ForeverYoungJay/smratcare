<template>
  <div class="bed-info-card" :class="[isBedAlert ? 'is-alert' : '']" @click="handleClick">
    <div class="bed-card-header">
      <div class="bed-card-title">
        <span class="bed-icon">💡</span>
        <span class="elder-name">{{ bed.elderName || '空床' }}</span>
        <span class="room-bed-no">— {{ bed.roomNo || '-' }} — {{ bed.bedNo }}</span>
      </div>
      <div class="bed-card-more">•••</div>
    </div>
    <div class="bed-card-body">
      <div class="status-graphic">
        <div class="graphic-bed" :class="{'occupied': !!bed.elderId, 'empty': !bed.elderId, 'alert': isBedAlert}"></div>
        <div class="graphic-wave"></div>
      </div>
      <div class="status-text" :class="isBedAlert ? 'text-alert' : ''">{{ bedStatusText }}</div>
    </div>
    <div class="bed-card-footer">
      <div class="vitals">
        <span class="vital-item">❤️ {{ heartRate }}</span>
        <span class="vital-item">🫁 {{ breathRate }}</span>
        <span class="vital-item wave-anim" v-if="bed.elderId">||||||||||</span>
      </div>
      <div class="status-badge" :class="bedBadgeClass">{{ bedBadgeText }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  bed: any
}>()

const emit = defineEmits(['click'])

const isBedAlert = computed(() => {
  return props.bed.riskLevel === 'HIGH' || props.bed.status === 0 || (props.bed.abnormalVital24hCount && props.bed.abnormalVital24hCount > 0)
})

const bedStatusText = computed(() => {
  if (!props.bed.elderId) return '空闲'
  if (props.bed.status === 2) return '维修'
  if (props.bed.status === 3) return '清洁中'
  if (isBedAlert.value) return props.bed.riskLabel || '正在告警'
  return '静卧'
})

const heartRate = computed(() => {
  if (!props.bed.elderId) return '--'
  return Math.floor(Math.random() * 20) + 60
})

const breathRate = computed(() => {
  if (!props.bed.elderId) return '--'
  return Math.floor(Math.random() * 5) + 16
})

const bedBadgeClass = computed(() => {
  if (isBedAlert.value) return 'badge-alert'
  if (props.bed.elderId) return 'badge-normal'
  return 'badge-idle'
})

const bedBadgeText = computed(() => {
  if (!props.bed.elderId) return '空闲'
  if (isBedAlert.value) return '异常'
  return '静卧'
})

function handleClick() {
  emit('click', props.bed)
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');

.bed-info-card {
  width: 100%;
  background: linear-gradient(145deg, #1e293b 0%, #0f172a 100%);
  border: 1px solid #334155;
  border-radius: 12px;
  padding: 16px;
  color: #f8fafc;
  font-family: 'Inter', sans-serif;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
  margin-bottom: 16px;
}

.bed-info-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.4);
  border-color: #64748b;
}

.bed-info-card.is-alert {
  border-color: #ef4444;
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.3);
}

.bed-info-card .bed-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #334155;
  padding-bottom: 10px;
  margin-bottom: 14px;
}

.bed-info-card .bed-card-title {
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.bed-info-card .elder-name {
  font-weight: 600;
  color: #e2e8f0;
}

.bed-info-card .room-bed-no {
  color: #94a3b8;
  font-size: 12px;
}

.bed-info-card .bed-card-more {
  color: #64748b;
  letter-spacing: 2px;
  font-weight: bold;
}

.bed-info-card .bed-card-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 90px;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 19px,
    rgba(51, 65, 85, 0.3) 19px,
    rgba(51, 65, 85, 0.3) 20px
  ), repeating-linear-gradient(
    90deg,
    transparent,
    transparent 19px,
    rgba(51, 65, 85, 0.3) 19px,
    rgba(51, 65, 85, 0.3) 20px
  );
  border-radius: 8px;
  margin-bottom: 14px;
  position: relative;
  overflow: hidden;
}

.bed-info-card .status-graphic {
  position: relative;
  width: 100%;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bed-info-card .graphic-bed {
  width: 66px;
  height: 33px;
  border-bottom: 4px solid #475569;
  border-left: 4px solid #475569;
  border-right: 4px solid #475569;
  border-radius: 2px 2px 0 0;
  position: relative;
}
.bed-info-card .graphic-bed::after {
  content: '';
  position: absolute;
  bottom: 0px;
  left: 4px;
  width: 50px;
  height: 14px;
  background: #3b82f6; /* occupied */
  border-radius: 2px;
}
.bed-info-card .graphic-bed.empty::after {
  background: #64748b;
}
.bed-info-card .graphic-bed.alert::after {
  background: #ef4444;
}

.bed-info-card .status-text {
  font-size: 13px;
  font-weight: bold;
  margin-top: 8px;
  color: #cbd5e1;
}
.bed-info-card .status-text.text-alert {
  color: #f87171;
}

.bed-info-card .bed-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bed-info-card .vitals {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #cbd5e1;
}

.bed-info-card .vital-item.wave-anim {
  color: #3b82f6;
  font-weight: bold;
  letter-spacing: 1px;
  opacity: 0.8;
  animation: pulseOpacity 1.5s infinite alternate;
}

@keyframes pulseOpacity {
  0% { opacity: 0.4; }
  100% { opacity: 1; }
}

.bed-info-card .status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}
.bed-info-card .badge-idle {
  background: #334155;
  color: #94a3b8;
}
.bed-info-card .badge-normal {
  background: #1e3a8a;
  color: #93c5fd;
}
.bed-info-card .badge-alert {
  background: rgba(239, 68, 68, 0.2);
  color: #fca5a5;
  border: 1px solid rgba(239, 68, 68, 0.5);
}
</style>
