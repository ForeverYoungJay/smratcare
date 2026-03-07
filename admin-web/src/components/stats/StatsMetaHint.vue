<template>
  <a-alert type="info" show-icon :message="message" class="stats-meta-hint" />
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  scopeText: string
  refreshedAt?: string
  noteText?: string
}>()

const message = computed(() => {
  const refreshed = props.refreshedAt || '--'
  const note = props.noteText ? `；${props.noteText}` : ''
  return `口径：${props.scopeText}；更新时间：${refreshed}${note}`
})
</script>

<style scoped>
.stats-meta-hint {
  margin-top: 12px;
}
</style>
