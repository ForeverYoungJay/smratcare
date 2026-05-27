<template>
  <a-drawer
    :open="open"
    :title="title"
    :width="width"
    placement="right"
    @update:open="$emit('update:open', $event)"
  >
    <template #extra>
      <slot name="extra" />
    </template>
    <slot />
  </a-drawer>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  open: boolean
  title: string
  width?: number
}>(), {
  width: 460
})

defineEmits<{
  (e: 'update:open', value: boolean): void
}>()
</script>
