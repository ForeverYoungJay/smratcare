<template>
  <div class="stateful-wrap">
    <template v-if="loading">
      <div class="stateful-panel">
        <a-skeleton active :paragraph="{ rows: rows }" />
      </div>
    </template>
    <template v-else-if="error">
      <div class="stateful-panel">
        <a-result status="error" :title="errorTitle" :sub-title="error">
          <template #extra>
            <a-button type="primary" size="small" @click="$emit('retry')">重试</a-button>
          </template>
        </a-result>
      </div>
    </template>
    <template v-else-if="empty">
      <div class="stateful-panel empty-wrap">
        <a-empty :description="emptyText" />
      </div>
    </template>
    <template v-else>
      <slot />
    </template>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    loading?: boolean
    error?: string
    empty?: boolean
    emptyText?: string
    rows?: number
    errorTitle?: string
  }>(),
  {
    loading: false,
    error: '',
    empty: false,
    emptyText: '暂无数据',
    rows: 4,
    errorTitle: '加载失败'
  }
)

defineEmits<{ (e: 'retry'): void }>()
</script>

<style scoped>
.stateful-wrap {
  min-height: 64px;
}

.stateful-panel {
  padding: 12px;
}
</style>
