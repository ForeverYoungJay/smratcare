<template>
  <PageContainer title="盘点调整记录" subTitle="盘盈盘亏历史记录">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
        <a-form-item label="商品ID">
          <a-input-number v-model:value="query.productId" style="width: 140px" />
        </a-form-item>
        <a-form-item label="仓库">
          <a-select v-model:value="query.warehouseId" :options="warehouseOptions" allow-clear style="width: 180px" />
        </a-form-item>
        <a-form-item label="分类">
          <a-select v-model:value="query.category" :options="categoryOptions" allow-clear style="width: 160px" />
        </a-form-item>
        <a-form-item label="类型">
          <a-select v-model:value="query.adjustType" allow-clear style="width: 120px">
            <a-select-option value="GAIN">盘盈</a-select-option>
            <a-select-option value="LOSS">盘亏</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="日期">
          <a-range-picker v-model:value="query.range" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="searchAll">搜索</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-space>
          <a-button @click="exportCsvData">导出CSV</a-button>
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
        <vxe-column field="createTime" title="调整时间" width="180" />
        <vxe-column field="productName" title="商品名称" min-width="160" />
        <vxe-column field="productId" title="商品ID" width="120" />
        <vxe-column field="batchId" title="批次ID" width="120" />
        <vxe-column field="adjustType" title="类型" width="120">
          <template #default="{ row }">
            <a-tag :color="row.adjustType === 'GAIN' ? 'green' : 'volcano'">
              {{ row.adjustType === 'GAIN' ? '盘盈' : '盘亏' }}
            </a-tag>
          </template>
        </vxe-column>
        <vxe-column field="adjustQty" title="数量" width="120" />
        <vxe-column field="reason" title="原因" min-width="160" />
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

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-space>
          <span style="font-weight: 500;">盘点差异报表</span>
          <a-button @click="fetchDiffReport">刷新差异报表</a-button>
          <a-button @click="exportDiffCsvData">导出差异CSV</a-button>
        </a-space>
      </div>
      <a-table
        row-key="key"
        :loading="loadingReport"
        :data-source="reportRows"
        :pagination="false"
        size="small"
        :columns="reportColumns"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'diffQty'">
            <a-tag :color="record.diffQty >= 0 ? 'green' : 'volcano'">{{ record.diffQty }}</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { getInventoryAdjustmentDiffReport, getInventoryAdjustmentPage } from '../../api/inventory'
import { getWarehousePage } from '../../api/material'
import { getProductPage } from '../../api/store'
import type { InventoryAdjustmentDiffItem, InventoryAdjustmentItem, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<InventoryAdjustmentItem[]>([])
const total = ref(0)
const loadingReport = ref(false)
const reportRows = ref<Array<InventoryAdjustmentDiffItem & { key: string }>>([])
const warehouseOptions = ref<Array<{ label: string; value: number }>>([])
const categoryOptions = ref<Array<{ label: string; value: string }>>([])

const query = reactive({
  productId: undefined as number | undefined,
  warehouseId: undefined as number | undefined,
  category: undefined as string | undefined,
  adjustType: undefined as 'GAIN' | 'LOSS' | undefined,
  range: undefined as any,
  pageNo: 1,
  pageSize: 10
})

const reportColumns = [
  { title: '商品ID', dataIndex: 'productId', key: 'productId', width: 100 },
  { title: '商品名称', dataIndex: 'productName', key: 'productName' },
  { title: '分类', dataIndex: 'category', key: 'category', width: 120 },
  { title: '仓库', dataIndex: 'warehouseName', key: 'warehouseName', width: 160 },
  { title: '盘盈', dataIndex: 'gainQty', key: 'gainQty', width: 100 },
  { title: '盘亏', dataIndex: 'lossQty', key: 'lossQty', width: 100 },
  { title: '净差异', dataIndex: 'diffQty', key: 'diffQty', width: 100 }
]

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<InventoryAdjustmentItem> = await getInventoryAdjustmentPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      productId: query.productId,
      warehouseId: query.warehouseId,
      category: query.category,
      adjustType: query.adjustType,
      dateFrom: query.range?.[0]?.format?.('YYYY-MM-DD'),
      dateTo: query.range?.[1]?.format?.('YYYY-MM-DD')
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function fetchDiffReport() {
  loadingReport.value = true
  try {
    const res = await getInventoryAdjustmentDiffReport({
      warehouseId: query.warehouseId,
      category: query.category,
      dateFrom: query.range?.[0]?.format?.('YYYY-MM-DD'),
      dateTo: query.range?.[1]?.format?.('YYYY-MM-DD')
    })
    reportRows.value = (res || []).map((it) => ({
      ...it,
      key: `${it.productId || 0}-${it.warehouseId || 0}`
    }))
  } finally {
    loadingReport.value = false
  }
}

function reset() {
  query.productId = undefined
  query.warehouseId = undefined
  query.category = undefined
  query.adjustType = undefined
  query.range = undefined
  query.pageNo = 1
  fetchData()
  fetchDiffReport()
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

function searchAll() {
  query.pageNo = 1
  fetchData()
  fetchDiffReport()
}

function exportCsvData() {
  exportCsv(
    rows.value.map((r) => ({
      调整时间: r.createTime,
      商品名称: r.productName,
      商品ID: r.productId,
      批次ID: r.batchId,
      类型: r.adjustType === 'GAIN' ? '盘盈' : '盘亏',
      数量: r.adjustQty,
      原因: r.reason
    })),
    '盘点调整记录'
  )
}

function exportDiffCsvData() {
  exportCsv(
    reportRows.value.map((r) => ({
      商品ID: r.productId,
      商品名称: r.productName,
      分类: r.category,
      仓库: r.warehouseName,
      盘盈: r.gainQty,
      盘亏: r.lossQty,
      净差异: r.diffQty
    })),
    '盘点差异报表'
  )
}

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
  await fetchData()
  await fetchDiffReport()
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
