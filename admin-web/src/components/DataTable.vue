<template>
  <a-card class="card-elevated" :bordered="false">
    <a-table
      :columns="columns"
      :data-source="dataSource"
      :row-key="rowKey"
      :row-class-name="rowClassName"
      :row-selection="rowSelection"
      :loading="loading"
      :pagination="pagination"
      :scroll="scroll"
      @change="onChange"
    >
      <template #bodyCell="slotProps">
        <slot name="bodyCell" v-bind="slotProps" />
      </template>
      <template #emptyText>
        <div class="empty-wrap">
          <a-empty description="暂无数据" />
        </div>
      </template>
    </a-table>
  </a-card>
</template>

<script setup lang="ts">
import type { TablePaginationConfig } from 'ant-design-vue'

defineProps<{
  columns: any[]
  dataSource: any[]
  rowKey: string
  rowClassName?: (record: any, index: number) => string
  rowSelection?: any
  loading?: boolean
  pagination?: TablePaginationConfig
  scroll?: any
}>()

const emit = defineEmits<{ (e: 'change', pagination: TablePaginationConfig, filters: any, sorter: any): void }>()

function onChange(pagination: TablePaginationConfig, filters: any, sorter: any) {
  emit('change', pagination, filters, sorter)
}
</script>
