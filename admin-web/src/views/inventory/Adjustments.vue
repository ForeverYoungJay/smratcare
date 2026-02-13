<template>
  <PageContainer title="盘点调整记录" subTitle="盘盈盘亏历史记录">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
        <a-form-item label="商品ID">
          <a-input-number v-model:value="query.productId" style="width: 140px" />
        </a-form-item>
        <a-form-item label="类型">
          <a-select v-model:value="query.adjustType" allow-clear style="width: 120px">
            <a-select-option value="GAIN">盘盈</a-select-option>
            <a-select-option value="LOSS">盘亏</a-select-option>
          </a-select>
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
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { getInventoryAdjustmentPage } from '../../api/inventory'
import type { InventoryAdjustmentItem, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<InventoryAdjustmentItem[]>([])
const total = ref(0)

const query = reactive({
  productId: undefined as number | undefined,
  adjustType: undefined as 'GAIN' | 'LOSS' | undefined,
  pageNo: 1,
  pageSize: 10
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<InventoryAdjustmentItem> = await getInventoryAdjustmentPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      productId: query.productId,
      adjustType: query.adjustType
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reset() {
  query.productId = undefined
  query.adjustType = undefined
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

onMounted(fetchData)
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
