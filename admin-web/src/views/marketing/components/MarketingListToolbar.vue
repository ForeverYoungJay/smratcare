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
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid rgba(214, 225, 234, 0.86);
  background: linear-gradient(180deg, rgba(247, 251, 253, 0.96), rgba(255, 255, 255, 0.96));
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
  color: #68839a;
  font-size: 12px;
}

.toolbar-selected {
  color: #0f7a9d;
  font-size: 12px;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(15, 122, 157, 0.1);
}
</style>
