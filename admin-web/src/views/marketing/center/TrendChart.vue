<template>
  <figure class="mk-trend">
    <svg :viewBox="`0 0 ${W} ${H}`" role="img" :aria-label="ariaLabel" preserveAspectRatio="none">
      <line v-for="i in 4" :key="i" :x1="PAD" :x2="W - PAD" :y1="PAD + (i - 1) * gridStep" :y2="PAD + (i - 1) * gridStep" class="mk-trend__grid" />
      <polygon v-if="points[0]?.length" :points="area" :fill="series[0].color" opacity="0.08" />
      <g v-for="(line, si) in points" :key="series[si].name">
        <polyline :points="line.map((p) => `${p.x},${p.y}`).join(' ')" fill="none" :stroke="series[si].color" stroke-width="2.5" />
        <circle v-for="(p, i) in line" :key="i" :cx="p.x" :cy="p.y" r="4" fill="#fff" :stroke="series[si].color" stroke-width="2">
          <title>{{ labels[i] }} {{ series[si].name }}：{{ p.value }}</title>
        </circle>
      </g>
    </svg>
    <div class="mk-trend__axis">
      <span v-for="label in labels" :key="label">{{ label }}</span>
    </div>
    <figcaption class="mk-trend__legend">
      <span v-for="s in series" :key="s.name"><i :style="{ background: s.color }" />{{ s.name }}</span>
    </figcaption>
  </figure>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { trendPoints } from './marketingMetrics'

const props = defineProps<{ labels: string[]; series: Array<{ name: string; color: string; values: number[] }> }>()
const W = 600, H = 180, PAD = 16
const gridStep = (H - PAD * 2) / 3
const points = computed(() => trendPoints(props.series.map((s) => s.values), W, H, PAD))
const area = computed(() => {
  const line = points.value[0]
  return `${line[0].x},${H - PAD} ${line.map((p) => `${p.x},${p.y}`).join(' ')} ${line[line.length - 1].x},${H - PAD}`
})
const ariaLabel = computed(() => props.series.map((s) => `${s.name}：${s.values.map((v, i) => `${props.labels[i]} ${v}`).join('，')}`).join('；'))
</script>

<style scoped>
.mk-trend { margin: 0; }
.mk-trend svg { display: block; width: 100%; height: 180px; }
.mk-trend__grid { stroke: #edf1ef; stroke-width: 1; }
.mk-trend__axis { display: flex; justify-content: space-between; padding: 4px 8px 0; color: var(--rm-muted, #6b7a74); font-size: 12px; }
.mk-trend__legend { display: flex; gap: 16px; margin-top: 8px; color: var(--rm-muted, #6b7a74); font-size: 12px; }
.mk-trend__legend i { display: inline-block; width: 8px; height: 8px; margin-right: 6px; border-radius: 50%; }
</style>
