<template>
  <PageContainer title="库存总览" subTitle="批次库存、临期与低库存监控">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" placeholder="商品名/编码/批次号" allow-clear />
        </a-form-item>
        <a-form-item label="商品ID">
          <a-input-number v-model:value="query.productId" style="width: 140px" />
        </a-form-item>
        <a-form-item label="仓库">
          <a-select v-model:value="query.warehouseId" :options="warehouseOptions" allow-clear style="width: 180px" />
        </a-form-item>
        <a-form-item label="分类">
          <a-select v-model:value="query.category" :options="categoryOptions" allow-clear style="width: 180px" />
        </a-form-item>
        <a-form-item label="临期天数<=">
          <a-input-number v-model:value="query.expiryDays" :min="0" style="width: 120px" />
        </a-form-item>
        <a-form-item label="仅低库存">
          <a-switch v-model:checked="query.lowStockOnly" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">搜索</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-space>
          <a-button @click="exportCsvData">导出CSV</a-button>
          <a-button type="primary" @click="openAdjust">盘点调整</a-button>
        </a-space>
      </div>

      <vxe-toolbar custom export></vxe-toolbar>
      <vxe-table
        border
        stripe
        show-overflow
        height="520"
        :loading="loading"
        :data="rows"
        :column-config="{ resizable: true }"
      >
        <vxe-column field="productName" title="商品名称" min-width="160" />
        <vxe-column field="productId" title="商品ID" width="120" />
        <vxe-column field="batchNo" title="批次号" min-width="160" />
        <vxe-column field="quantity" title="库存数量" width="120">
          <template #default="{ row }">
            <a-tag :color="lowStock(row) ? 'red' : 'blue'">{{ row.quantity }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column field="safetyStock" title="安全库存" width="120" />
        <vxe-column field="expireDate" title="有效期" width="140">
          <template #default="{ row }">
            <a-tag :color="expiring(row) ? 'orange' : 'default'">{{ row.expireDate || '-' }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column field="warehouseLocation" title="库位" width="120" />
        <vxe-column field="createTime" title="入库时间" width="180" />
      </vxe-table>

      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>

    <a-modal v-model:open="adjustOpen" title="盘点调整" @ok="submitAdjust" :confirm-loading="adjusting">
      <a-form layout="vertical" :model="adjustForm" :rules="adjustRules" ref="adjustFormRef">
        <a-form-item label="商品ID" name="productId">
          <a-input-number v-model:value="adjustForm.productId" style="width: 100%" />
        </a-form-item>
        <a-form-item label="批次ID">
          <a-input-number v-model:value="adjustForm.batchId" style="width: 100%" />
        </a-form-item>
        <a-form-item label="调整类型" name="adjustType">
          <a-select v-model:value="adjustForm.adjustType">
            <a-select-option value="GAIN">盘盈</a-select-option>
            <a-select-option value="LOSS">盘亏</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="调整数量" name="adjustQty">
          <a-input-number v-model:value="adjustForm.adjustQty" style="width: 100%" />
        </a-form-item>
        <a-form-item label="原因">
          <a-input v-model:value="adjustForm.reason" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { adjustInventory, getInventoryBatchPage } from '../../api/inventory'
import { getWarehousePage } from '../../api/material'
import { getProductPage } from '../../api/store'
import type { InventoryBatchItem, InventoryAdjustRequest, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<InventoryBatchItem[]>([])
const total = ref(0)

const query = reactive({
  keyword: '',
  productId: undefined as number | undefined,
  warehouseId: undefined as number | undefined,
  category: undefined as string | undefined,
  expiryDays: undefined as number | undefined,
  lowStockOnly: false,
  pageNo: 1,
  pageSize: 10
})
const warehouseOptions = ref<Array<{ label: string; value: number }>>([])
const categoryOptions = ref<Array<{ label: string; value: string }>>([])

const adjustOpen = ref(false)
const adjusting = ref(false)
const adjustFormRef = ref()
const adjustForm = reactive<InventoryAdjustRequest>({
  productId: 0,
  batchId: undefined,
  adjustType: 'GAIN',
  adjustQty: 0,
  reason: ''
})
const adjustRules = {
  productId: [{ required: true, message: '请输入商品ID' }],
  adjustType: [{ required: true, message: '请选择类型' }],
  adjustQty: [{ required: true, message: '请输入数量' }]
}

function expiring(row: InventoryBatchItem) {
  if (!row.expireDate) return false
  const expire = new Date(row.expireDate).getTime()
  const now = Date.now()
  const diff = (expire - now) / (24 * 3600 * 1000)
  return diff <= 30
}

function lowStock(row: InventoryBatchItem) {
  if (row.safetyStock == null) return false
  return row.quantity < row.safetyStock
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<InventoryBatchItem> = await getInventoryBatchPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      productId: query.productId,
      warehouseId: query.warehouseId,
      category: query.category,
      expiryDays: query.expiryDays,
      lowStockOnly: query.lowStockOnly,
      keyword: query.keyword || undefined
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reset() {
  query.keyword = ''
  query.productId = undefined
  query.warehouseId = undefined
  query.category = undefined
  query.expiryDays = undefined
  query.lowStockOnly = false
  query.pageNo = 1
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(_page: number, size: number) {
  query.pageSize = size
  query.pageNo = 1
  fetchData()
}

function exportCsvData() {
  exportCsv(
    rows.value.map((r) => ({
      商品名称: r.productName,
      商品ID: r.productId,
      批次号: r.batchNo,
      库存数量: r.quantity,
      安全库存: r.safetyStock,
      有效期: r.expireDate,
      库位: r.warehouseLocation
    })),
    '库存总览'
  )
}

function openAdjust() {
  Object.assign(adjustForm, { productId: 0, batchId: undefined, adjustType: 'GAIN', adjustQty: 0, reason: '' })
  adjustOpen.value = true
}

async function submitAdjust() {
  await adjustFormRef.value?.validate()
  adjusting.value = true
  try {
    await adjustInventory(adjustForm)
    message.success('调整成功')
    adjustOpen.value = false
    fetchData()
  } finally {
    adjusting.value = false
  }
}

onMounted(fetchData)

onMounted(async () => {
  const [warehouseRes, productRes] = await Promise.all([
    getWarehousePage({ pageNo: 1, pageSize: 500 }),
    getProductPage({ pageNo: 1, pageSize: 500 })
  ])
  warehouseOptions.value = warehouseRes.list.map((it: { id: number; warehouseName?: string }) => ({ label: it.warehouseName || `仓库${it.id}`, value: it.id }))
  const categorySet = new Set<string>()
  for (const row of productRes.list || []) {
    if (row.category) categorySet.add(row.category)
  }
  categoryOptions.value = Array.from(categorySet).map((it) => ({ label: it, value: it }))
})
</script>

<style scoped>
.search-form {
  row-gap: 8px;
}
.table-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}
</style>
