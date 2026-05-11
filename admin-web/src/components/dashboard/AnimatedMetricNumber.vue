<template>
  <span class="animated-number">{{ displayText }}</span>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'

const props = withDefaults(defineProps<{
  value: number
  duration?: number
  decimals?: number
  prefix?: string
  suffix?: string
}>(), {
  duration: 900,
  decimals: 0,
  prefix: '',
  suffix: ''
})

const animatedValue = ref(props.value)
let frameId = 0

function cancelAnimation() {
  if (frameId) {
    cancelAnimationFrame(frameId)
    frameId = 0
  }
}

function animateTo(target: number) {
  cancelAnimation()
  const start = animatedValue.value
  const diff = target - start
  if (!diff) return
  const startedAt = performance.now()

  const tick = (now: number) => {
    const progress = Math.min((now - startedAt) / props.duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    animatedValue.value = start + diff * eased
    if (progress < 1) {
      frameId = requestAnimationFrame(tick)
      return
    }
    animatedValue.value = target
    frameId = 0
  }

  frameId = requestAnimationFrame(tick)
}

watch(() => props.value, (next) => {
  animateTo(next)
}, { immediate: true })

onBeforeUnmount(() => {
  cancelAnimation()
})

const displayText = computed(() => {
  const text = animatedValue.value.toFixed(props.decimals)
  return `${props.prefix}${text}${props.suffix}`
})
</script>

<style scoped>
.animated-number {
  font-variant-numeric: tabular-nums;
}
</style>
