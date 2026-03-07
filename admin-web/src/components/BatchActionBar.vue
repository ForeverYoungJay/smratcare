<template>
  <div class="batch-action-bar">
    <a-space wrap>
      <template v-for="item in actions" :key="item.key">
        <a-button
          :type="item.type || 'default'"
          :danger="Boolean(item.danger)"
          :disabled="Boolean(item.disabled)"
          :loading="Boolean(item.loading)"
          @click="item.onClick?.()"
        >
          {{ item.label }}
        </a-button>
      </template>
      <slot />
    </a-space>
  </div>
</template>

<script setup lang="ts">
export interface BatchActionItem {
  key: string
  label: string
  type?: 'default' | 'primary' | 'dashed' | 'link' | 'text'
  danger?: boolean
  disabled?: boolean
  loading?: boolean
  onClick?: () => void | Promise<void>
}

defineProps<{
  actions: BatchActionItem[]
}>()
</script>

<style scoped>
.batch-action-bar {
  margin-bottom: 8px;
}
</style>
