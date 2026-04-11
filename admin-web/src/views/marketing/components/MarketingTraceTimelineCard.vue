<template>
  <a-card class="card-elevated" :bordered="false" :style="cardStyle">
    <template #title>{{ title }}</template>
    <a-empty v-if="!items.length" :description="emptyText" />
    <a-timeline v-else>
      <a-timeline-item v-for="(item, index) in items" :key="resolveKey(item, index)">
        <slot :item="item" />
      </a-timeline-item>
    </a-timeline>
  </a-card>
</template>

<script setup lang="ts" generic="T extends Record<string, any>">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  title: string
  items: T[]
  emptyText?: string
  keyField?: string
  marginTop?: number
}>(), {
  emptyText: '暂无记录',
  keyField: 'id',
  marginTop: 12
})

const cardStyle = computed(() => ({ marginTop: `${props.marginTop}px` }))

function resolveKey(item: T, index: number) {
  const key = item?.[props.keyField]
  if (key != null && key !== '') {
    return key
  }
  return `${props.title}-${index}`
}
</script>
