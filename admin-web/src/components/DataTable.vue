<template>
  <section class="table-shell card-elevated">
    <div class="table-shell-head" v-if="$slots.toolbar || canCollapseColumns">
      <slot name="toolbar" />
      <a-button v-if="canCollapseColumns" type="link" :aria-expanded="showAllColumns" @click="showAllColumns = !showAllColumns">{{ showAllColumns ? '只看常用信息' : '显示完整信息' }}</a-button>
    </div>
    <div v-if="selectionCount" class="business-selection" role="status">
      <span>已选 {{ selectionCount }} 条</span>
      <a-button v-if="rowSelection?.onChange" type="link" @click="rowSelection.onChange([], [])">取消选择</a-button>
    </div>
    <a-table
      class="data-table"
      :columns="displayColumns"
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
        <TableEmpty
          :filtered="isFilteredEmpty"
          :title="emptyTitle"
          :description="emptyDescription"
          @clear-filters="onClearFilters"
        >
          <slot name="emptyExtra" />
        </TableEmpty>
      </template>
    </a-table>
  </section>
</template>

<script setup lang="ts">
import type { TablePaginationConfig } from 'ant-design-vue'
import { computed, inject, ref } from 'vue'
import { useRoute } from 'vue-router'
import { emphasizeColumns, compactWorkspaceColumns } from '../workspaces/columns'
import { clearFilters, countActiveFilters } from '../utils/filterState'
import TableEmpty from './TableEmpty.vue'

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
  emptyTitle?: string
  emptyDescription?: string
  showStriped?: boolean
  /** 传入页面的查询表单对象后，空状态会区分「被筛掉了」和「本来就没有」 */
  filterModel?: Record<string, any> | null
}>()

const embedded = inject('function-workspace-embedded', false)
const route = useRoute()
const showAllColumns = ref(false)
const selectionCount = computed(() => props.rowSelection?.selectedRowKeys?.length || 0)
const compactColumns = computed(() => embedded ? compactWorkspaceColumns(props.columns, route?.path || '') : props.columns)
const canCollapseColumns = computed(() => compactColumns.value.length < props.columns.length)
const displayColumns = computed(() => emphasizeColumns(showAllColumns.value ? props.columns : compactColumns.value))

/** 有筛选条件、且当前结果为空 —— 和「一条数据都还没建」是两件事 */
const isFilteredEmpty = computed(
  () => (props.dataSource || []).length === 0 && countActiveFilters(props.filterModel) > 0
)

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

const emit = defineEmits<{
  (e: 'change', pagination: TablePaginationConfig, filters: any, sorter: any): void
  (e: 'clear-filters'): void
}>()

function onChange(pagination: TablePaginationConfig, filters: any, sorter: any) {
  emit('change', pagination, filters, sorter)
}

/**
 * 先把筛选条件真正清掉，再让页面重新查询。
 * 不能直接复用页面的「清空」按钮：那个是恢复初始值，而初始值本身常常就是筛选条件。
 */
function onClearFilters() {
  clearFilters(props.filterModel)
  emit('clear-filters')
}
</script>

<style scoped>
.business-selection { display:flex;align-items:center;gap:12px;padding:6px 12px;border-radius:6px;background:var(--primary-soft);color:var(--primary); }
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
