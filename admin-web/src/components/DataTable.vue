<template>
  <section class="table-shell card-elevated">
    <div class="table-shell-head" v-if="$slots.toolbar">
      <slot name="toolbar" />
    </div>
    <a-table
      class="data-table"
      :columns="columns"
      :data-source="dataSource"
      :row-key="rowKey"
      :row-class-name="mergedRowClassName"
      :row-selection="rowSelection"
      :loading="loading"
      :size="size || 'middle'"
      :pagination="mergedPagination"
      :sticky="sticky === false ? false : { offsetHeader: 0 }"
      :scroll="mergedScroll"
      @change="onChange"
    >
      <template #bodyCell="slotProps">
        <slot name="bodyCell" v-bind="slotProps" />
      </template>
      <template #emptyText>
        <div class="empty-wrap">
          <a-empty :description="emptyDescription || '暂无数据'" />
        </div>
      </template>
    </a-table>
  </section>
</template>

<script setup lang="ts">
import type { TablePaginationConfig } from 'ant-design-vue'
import { computed } from 'vue'

const props = defineProps<{
  columns: any[]
  dataSource: any[]
  rowKey?: string
  rowClassName?: (record: any, index: number) => string
  rowSelection?: any
  loading?: boolean
  pagination?: TablePaginationConfig | false
  scroll?: any
  size?: 'small' | 'middle' | 'large'
  sticky?: boolean
  emptyDescription?: string
  showStriped?: boolean
}>()

const mergedPagination = computed<TablePaginationConfig | false>(() => {
  if (props.pagination === false) return false
  return {
    showSizeChanger: true,
    showQuickJumper: true,
    pageSizeOptions: ['10', '20', '50', '100'],
    showTotal: (total) => `共 ${total} 条记录`,
    position: ['bottomRight'],
    ...(props.pagination || {})
  }
})

const mergedScroll = computed(() => ({
  x: 'max-content',
  ...(props.scroll || {})
}))

const mergedRowClassName = (record: any, index: number) => {
  const external = props.rowClassName ? props.rowClassName(record, index) : ''
  const striped = props.showStriped === false ? '' : index % 2 === 1 ? 'is-striped-row' : ''
  return `${striped} ${external}`.trim()
}

const emit = defineEmits<{ (e: 'change', pagination: TablePaginationConfig, filters: any, sorter: any): void }>()

function onChange(pagination: TablePaginationConfig, filters: any, sorter: any) {
  emit('change', pagination, filters, sorter)
}
</script>

<style scoped>
.table-shell {
  overflow: hidden;
}

.table-shell-head {
  padding: 16px 18px 0;
}

.table-shell :deep(.ant-table-wrapper) {
  padding: 0 4px 4px;
}

.data-table :deep(.is-striped-row > td) {
  background: rgba(247, 251, 254, 0.88);
}
</style>
