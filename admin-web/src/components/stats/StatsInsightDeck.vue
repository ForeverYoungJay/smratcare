<template>
  <a-row :gutter="[14, 14]" class="stats-insight-deck">
    <a-col v-for="item in items" :key="item.key" :xs="24" :md="12" :xl="6">
      <div class="insight-card" :class="`tone-${item.tone || 'neutral'}`">
        <div class="insight-eyebrow">{{ item.label }}</div>
        <div class="insight-value">{{ item.valueText }}</div>
        <div class="insight-trend">{{ item.trendText }}</div>
        <div class="insight-detail" v-if="item.detail">{{ item.detail }}</div>
      </div>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
defineProps<{
  items: Array<{
    key: string
    label: string
    valueText: string
    trendText: string
    detail?: string
    tone?: 'neutral' | 'good' | 'warning' | 'danger'
  }>
}>()
</script>

<style scoped>
.stats-insight-deck {
  margin-top: 16px;
}

.insight-card {
  min-height: 136px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.12);
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.98), rgba(244, 247, 251, 0.96)),
    radial-gradient(circle at top right, rgba(22, 119, 255, 0.08), transparent 48%);
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.08);
}

.insight-card.tone-good {
  background:
    linear-gradient(145deg, rgba(245, 255, 250, 0.98), rgba(234, 247, 240, 0.96)),
    radial-gradient(circle at top right, rgba(82, 196, 26, 0.12), transparent 48%);
}

.insight-card.tone-warning {
  background:
    linear-gradient(145deg, rgba(255, 251, 235, 0.98), rgba(248, 240, 214, 0.96)),
    radial-gradient(circle at top right, rgba(250, 173, 20, 0.14), transparent 48%);
}

.insight-card.tone-danger {
  background:
    linear-gradient(145deg, rgba(255, 244, 244, 0.98), rgba(250, 230, 230, 0.96)),
    radial-gradient(circle at top right, rgba(255, 77, 79, 0.14), transparent 48%);
}

.insight-eyebrow {
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(15, 23, 42, 0.52);
}

.insight-value {
  margin-top: 8px;
  font-size: 26px;
  line-height: 1.1;
  font-weight: 700;
  color: #111827;
}

.insight-trend {
  margin-top: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.insight-detail {
  margin-top: 8px;
  font-size: 12px;
  line-height: 1.6;
  color: rgba(31, 41, 55, 0.72);
}
</style>
