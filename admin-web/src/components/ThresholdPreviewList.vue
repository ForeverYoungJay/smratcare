<template>
  <a-list size="small" :data-source="rows" :pagination="false" :locale="{ emptyText }">
    <template #renderItem="{ item }">
      <a-list-item>
        <a-space>
          <a-tag :color="resolveTagColor(item)">
            {{ item.triggered ? '已触发' : '未触发' }}
          </a-tag>
          <span>{{ item.title }}</span>
          <span class="hint-text">当前 {{ item.currentText }}；阈值 {{ item.thresholdText }}</span>
        </a-space>
      </a-list-item>
    </template>
  </a-list>
</template>

<script setup lang="ts">
import type { ThresholdPreviewRow } from '../utils/dashboardThresholdPreview'

withDefaults(
  defineProps<{
    rows: ThresholdPreviewRow[]
    emptyText?: string
  }>(),
  {
    emptyText: '暂无预览数据'
  }
)

function resolveTagColor(row: ThresholdPreviewRow) {
  if (!row.triggered) return 'default'
  return row.key === 'abnormal' ? 'red' : 'orange'
}
</script>

<style scoped>
.hint-text {
  color: var(--muted);
  font-size: 12px;
}
</style>
