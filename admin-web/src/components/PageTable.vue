<template>
  <a-card class="card-elevated table-card" :bordered="false">
    <div v-if="$slots.search" class="search-slot">
      <slot name="search" />
    </div>
    <a-table
      :columns="columns"
      :data-source="dataSource"
      :row-key="rowKey"
      :loading="loading"
      :pagination="pagination"
      :scroll="{ x: 'max-content' }"
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
  </a-card>
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

const pagination = reactive<TablePaginationConfig>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
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
.search-slot {
  margin-bottom: 16px;
}
</style>
