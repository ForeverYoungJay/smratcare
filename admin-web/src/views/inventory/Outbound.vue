<template>
  <PageContainer title="出库记录" subTitle="销售/领用出库明细">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
        <a-form-item label="出库类型">
          <a-select v-model:value="query.outType" allow-clear style="width: 140px">
            <a-select-option value="SALE">销售</a-select-option>
            <a-select-option value="CONSUME">领用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="商品ID">
          <a-input-number v-model:value="query.productId" style="width: 140px" />
        </a-form-item>
        <a-form-item label="日期">
          <a-range-picker v-model:value="query.range" />
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
          <a-button type="primary" @click="openOutbound">新增领用出库</a-button>
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
        <vxe-column field="createTime" title="出库时间" width="180" />
        <vxe-column field="productName" title="商品名称" min-width="160" />
        <vxe-column field="productId" title="商品ID" width="120" />
        <vxe-column field="batchNo" title="批次号" min-width="160" />
        <vxe-column field="changeQty" title="出库数量" width="120" />
        <vxe-column field="outType" title="类型" width="120">
          <template #default="{ row }">
            <a-tag :color="row.outType === 'SALE' ? 'blue' : 'purple'">
              {{ row.outType === 'SALE' ? '销售' : '领用' }}
            </a-tag>
          </template>
        </vxe-column>
        <vxe-column field="refOrderId" title="订单ID" width="140" />
        <vxe-column field="remark" title="备注" min-width="160" />
        <vxe-column title="操作" width="120" fixed="right">
          <template #default="{ row }">
            <a @click="openDetail(row)">详情</a>
          </template>
        </vxe-column>
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

    <a-drawer v-model:open="detailOpen" title="出库详情" width="520">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="商品名称">{{ detail?.productName }}</a-descriptions-item>
        <a-descriptions-item label="商品ID">{{ detail?.productId }}</a-descriptions-item>
        <a-descriptions-item label="批次号">{{ detail?.batchNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="数量">{{ detail?.changeQty }}</a-descriptions-item>
        <a-descriptions-item label="类型">
          {{ detail?.outType === 'SALE' ? '销售' : '领用' }}
        </a-descriptions-item>
        <a-descriptions-item label="出库时间">{{ detail?.createTime }}</a-descriptions-item>
        <a-descriptions-item label="订单ID">{{ detail?.refOrderId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="备注">{{ detail?.remark || '-' }}</a-descriptions-item>
      </a-descriptions>
      <a-divider />
      <div v-if="detail?.outType === 'SALE'">
        <a-button type="link" @click="openOrderDetail">查看订单详情</a-button>
      </div>
    </a-drawer>

    <a-modal v-model:open="outboundOpen" title="新增领用出库" @ok="submitOutbound" :confirm-loading="outboundSubmitting">
      <a-form layout="vertical" :model="outboundForm" :rules="outboundRules" ref="outboundFormRef">
        <a-form-item label="商品ID" name="productId">
          <a-input-number v-model:value="outboundForm.productId" style="width: 100%" />
        </a-form-item>
        <a-form-item label="批次ID">
          <a-input-number v-model:value="outboundForm.batchId" style="width: 100%" />
        </a-form-item>
        <a-form-item label="出库数量" name="quantity">
          <a-input-number v-model:value="outboundForm.quantity" style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="outboundForm.reason" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { getInventoryOutboundPage, createOutbound } from '../../api/inventory'
import { getOrderDetail } from '../../api/store'
import { message } from 'ant-design-vue'
import type { InventoryLogItem, InventoryOutboundRequest, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<InventoryLogItem[]>([])
const total = ref(0)
const detailOpen = ref(false)
const detail = ref<InventoryLogItem | null>(null)
const outboundOpen = ref(false)
const outboundSubmitting = ref(false)
const outboundFormRef = ref()
const outboundForm = reactive<InventoryOutboundRequest>({
  productId: 0,
  batchId: undefined,
  quantity: 0,
  reason: ''
})
const outboundRules = {
  productId: [{ required: true, message: '请输入商品ID' }],
  quantity: [{ required: true, message: '请输入数量' }]
}

const query = reactive({
  outType: undefined as 'SALE' | 'CONSUME' | undefined,
  productId: undefined as number | undefined,
  range: undefined as any,
  pageNo: 1,
  pageSize: 10
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<InventoryLogItem> = await getInventoryOutboundPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      productId: query.productId,
      outType: query.outType,
      dateFrom: query.range?.[0]?.format?.('YYYY-MM-DD'),
      dateTo: query.range?.[1]?.format?.('YYYY-MM-DD')
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reset() {
  query.outType = undefined
  query.productId = undefined
  query.range = undefined
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
      出库时间: r.createTime,
      商品名称: r.productName,
      商品ID: r.productId,
      批次号: r.batchNo,
      出库数量: r.changeQty,
      类型: r.outType === 'SALE' ? '销售' : '领用',
      订单ID: r.refOrderId,
      备注: r.remark
    })),
    '出库记录'
  )
}

function openDetail(row: InventoryLogItem) {
  detail.value = row
  detailOpen.value = true
}

function openOutbound() {
  Object.assign(outboundForm, { productId: 0, batchId: undefined, quantity: 0, reason: '' })
  outboundOpen.value = true
}

async function submitOutbound() {
  await outboundFormRef.value?.validate()
  outboundSubmitting.value = true
  try {
    await createOutbound(outboundForm)
    message.success('出库成功')
    outboundOpen.value = false
    fetchData()
  } finally {
    outboundSubmitting.value = false
  }
}

async function openOrderDetail() {
  if (!detail.value?.refOrderId) {
    message.info('无关联订单')
    return
  }
  await getOrderDetail(detail.value.refOrderId)
  message.success('订单详情已拉取，可在订单管理查看')
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
