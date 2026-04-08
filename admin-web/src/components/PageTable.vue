<template>
  <section class="page-table-shell card-elevated">
    <div v-if="$slots.search" class="search-slot">
      <slot name="search" />
    </div>
    <a-table
      class="page-table"
      :columns="columns"
      :data-source="dataSource"
      :row-key="rowKey"
      :loading="loading"
      size="middle"
      :sticky="{ offsetHeader: 0 }"
      :pagination="pagination"
      :scroll="{ x: 'max-content' }"
      :row-class-name="rowClassName"
      @change="onChange"
    >
      <template #bodyCell="slotProps">
        <slot name="action" v-bind="slotProps" />
      </template>
      <template #emptyText>
        <div class="empty-wrap">
          <a-empty description="暂无数据" />
        </div>
      </template>
    </a-table>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import { message } from 'ant-design-vue'

const props = defineProps<{
  columns: any[]
  rowKey?: string
  fetchData: (params: any) => Promise<any>
  initialParams?: Record<string, any>
}>()

const rowKey = props.rowKey || 'id'
const dataSource = ref<any[]>([])
const loading = ref(false)
const rowClassName = (_record: any, index: number) => (index % 2 === 1 ? 'is-striped-row' : '')

const pagination = reactive<TablePaginationConfig>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  pageSizeOptions: ['10', '20', '50', '100'],
  showTotal: (total) => `共 ${total} 条记录`,
  position: ['bottomRight']
})

async function load(extra?: Record<string, any>) {
  loading.value = true
  try {
    const params = {
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
      ...(props.initialParams || {}),
      ...(extra || {})
    }
    const res = await props.fetchData(params)
    if (res?.data?.list) {
      dataSource.value = res.data.list
      pagination.total = res.data.total || res.data.list.length
    } else if (Array.isArray(res?.list)) {
      dataSource.value = res.list
      pagination.total = res.total || res.list.length
    } else {
      dataSource.value = []
      pagination.total = 0
    }
  } catch (error: any) {
    message.error(error?.message || '表格加载失败')
  } finally {
    loading.value = false
  }
}

function onChange(pag: TablePaginationConfig, filters: any, sorter: any) {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  load({ filters, sorter })
}

onMounted(() => load())

defineExpose({ reload: load })
</script>

<style scoped>
.page-table-shell {
  overflow: hidden;
}

.search-slot {
  padding: 16px 18px 0;
}

.page-table-shell :deep(.ant-table-wrapper) {
  padding: 0 4px 4px;
}

.page-table :deep(.is-striped-row > td) {
  background: rgba(247, 251, 254, 0.88);
}
</style>
