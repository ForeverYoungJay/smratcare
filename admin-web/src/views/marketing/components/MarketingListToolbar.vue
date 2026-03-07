<template>
  <div class="marketing-list-toolbar">
    <div class="toolbar-left">
      <slot />
    </div>
    <div class="toolbar-right">
      <slot name="right">
        <span v-if="selectedCount > 0" class="toolbar-selected">已勾选 {{ selectedCount }} 条</span>
        <span v-if="tip" class="toolbar-tip">{{ tip }}</span>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  tip?: string
  selectedCount?: number
}>(), {
  tip: '',
  selectedCount: 0
})
</script>

<style scoped>
.marketing-list-toolbar {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  min-width: 160px;
}

.toolbar-tip {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

.toolbar-selected {
  color: #1677ff;
  font-size: 12px;
}
</style>
