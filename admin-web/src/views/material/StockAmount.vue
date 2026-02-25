<template>
  <PageContainer title="库存金额" subTitle="按物资汇总库存数量与库存成本金额">
    <a-card class="card-elevated" :bordered="false">
      <div class="toolbar">
        <a-space>
          <a-select v-model:value="query.dimension" style="width: 160px" :options="dimensionOptions" />
          <a-select
            v-model:value="query.warehouseId"
            :disabled="query.dimension !== 'PRODUCT'"
            :options="warehouseOptions"
            style="width: 180px"
            allow-clear
            placeholder="筛选仓库"
          />
          <a-input v-model:value="query.category" style="width: 180px" allow-clear placeholder="筛选分类(模糊)" />
          <a-button type="primary" @click="fetchData">刷新</a-button>
          <span>库存总金额：<b>{{ totalAmount.toFixed(2) }}</b></span>
        </a-space>
      </div>

      <a-table row-key="rowKey" :loading="loading" :data-source="rows" :columns="columns" :pagination="false" size="middle">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            {{ record.displayName }}
          </template>
          <template v-if="column.key === 'totalAmount'">
            {{ Number(record.totalAmount || 0).toFixed(2) }}
          </template>
        </template>
      </a-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { getStockAmount, getWarehousePage } from '../../api/material'
import type { MaterialStockAmountItem } from '../../types'

const loading = ref(false)
const rows = ref<Array<MaterialStockAmountItem & { rowKey: string; displayName: string }>>([])
const warehouseOptions = ref<Array<{ label: string; value: number }>>([])
const query = reactive({
  dimension: 'PRODUCT' as 'PRODUCT' | 'WAREHOUSE' | 'CATEGORY',
  warehouseId: undefined as number | undefined,
  category: ''
})

const dimensionOptions = [
  { label: '按物资', value: 'PRODUCT' },
  { label: '按仓库', value: 'WAREHOUSE' },
  { label: '按分类', value: 'CATEGORY' }
]

const columns = computed(() => [
  { title: query.dimension === 'WAREHOUSE' ? '仓库ID' : query.dimension === 'CATEGORY' ? '分类' : '物资ID', dataIndex: query.dimension === 'CATEGORY' ? 'category' : query.dimension === 'WAREHOUSE' ? 'warehouseId' : 'productId', key: 'idCol', width: 120 },
  { title: query.dimension === 'PRODUCT' ? '物资编码' : '维度', dataIndex: query.dimension === 'PRODUCT' ? 'productCode' : 'dimension', key: 'dimensionCol', width: 160 },
  { title: query.dimension === 'WAREHOUSE' ? '仓库名称' : query.dimension === 'CATEGORY' ? '分类名称' : '物资名称', dataIndex: 'displayName', key: 'name' },
  { title: '库存数量', dataIndex: 'totalQuantity', key: 'totalQuantity', width: 120 },
  { title: '库存金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 140 }
])

const totalAmount = computed(() => rows.value.reduce((sum, it) => sum + Number(it.totalAmount || 0), 0))

async function fetchData() {
  loading.value = true
  try {
    const res = await getStockAmount({
      dimension: query.dimension,
      warehouseId: query.dimension === 'PRODUCT' ? query.warehouseId : undefined,
      category: query.category || undefined
    })
    rows.value = (res || []).map((it) => ({
      ...it,
      rowKey: `${it.dimension}-${it.productId || 0}-${it.warehouseId || 0}-${it.category || ''}`,
      displayName:
        query.dimension === 'WAREHOUSE'
          ? it.warehouseName || '未分配仓库'
          : query.dimension === 'CATEGORY'
            ? it.category || '未分类'
            : it.productName || '-'
    }))
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const warehouseRes = await getWarehousePage({ pageNo: 1, pageSize: 500, enabledOnly: true })
  warehouseOptions.value = (warehouseRes.list || [])
    .map((it: { id: number; warehouseName?: string }) => ({ label: it.warehouseName || `仓库${it.id}`, value: it.id }))
  fetchData()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
</style>
