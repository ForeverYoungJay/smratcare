<template>
  <PageContainer title="采购单" subTitle="维护采购计划与采购明细">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
        <a-form-item label="状态">
          <a-select v-model:value="query.status" style="width: 160px" allow-clear :options="statusOptions" />
        </a-form-item>
        <a-form-item label="单号">
          <a-input v-model:value="query.keyword" placeholder="采购单号" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <div class="table-actions">
        <a-button type="primary" @click="openCreate">新建采购单</a-button>
      </div>
      <a-table
        row-key="id"
        :loading="loading"
        :data-source="rows"
        :pagination="false"
        :columns="columns"
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ record.status }}</a-tag>
          </template>
          <template v-if="column.key === 'actions'">
            <a-space>
              <a @click="viewDetail(record.id)">明细</a>
              <a v-if="record.status === 'DRAFT'" @click="approve(record.id)">审核</a>
              <a v-if="record.status === 'APPROVED'" @click="complete(record.id)">完成</a>
            </a-space>
          </template>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 16px; text-align: right"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>

    <a-modal
      v-model:open="editorOpen"
      title="新建采购单"
      width="900px"
      :confirm-loading="saving"
      @ok="submit"
    >
      <a-form layout="vertical" :model="form">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="仓库">
              <a-select v-model:value="form.warehouseId" :options="warehouseOptions" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="供应商">
              <a-select v-model:value="form.supplierId" :options="supplierOptions" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="下单日期">
              <a-date-picker v-model:value="form.orderDate" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>

      <div class="items-header">
        <span>采购明细</span>
        <a-button size="small" @click="addItem">新增行</a-button>
      </div>
      <a-table row-key="_idx" :data-source="form.items" :pagination="false" size="small" :columns="itemColumns">
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'productId'">
            <a-select v-model:value="record.productId" :options="productOptions" show-search allow-clear />
          </template>
          <template v-else-if="column.key === 'quantity'">
            <a-input-number v-model:value="record.quantity" :min="1" style="width: 100%" />
          </template>
          <template v-else-if="column.key === 'unitPrice'">
            <a-input-number v-model:value="record.unitPrice" :min="0" :precision="2" style="width: 100%" />
          </template>
          <template v-else-if="column.key === 'amount'">
            {{ ((record.quantity || 0) * (record.unitPrice || 0)).toFixed(2) }}
          </template>
          <template v-else-if="column.key === 'actions'">
            <a @click="removeItem(index)">删除</a>
          </template>
        </template>
      </a-table>
    </a-modal>

    <a-drawer v-model:open="detailOpen" title="采购单明细" width="680">
      <a-descriptions bordered :column="2" size="small" v-if="detail">
        <a-descriptions-item label="采购单号">{{ detail.orderNo }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ detail.status }}</a-descriptions-item>
        <a-descriptions-item label="仓库">{{ detail.warehouseName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="供应商">{{ detail.supplierName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="下单日期">{{ detail.orderDate || '-' }}</a-descriptions-item>
        <a-descriptions-item label="总金额">{{ Number(detail.totalAmount || 0).toFixed(2) }}</a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</a-descriptions-item>
      </a-descriptions>

      <a-table style="margin-top: 12px" :pagination="false" :data-source="detail?.items || []" size="small" :columns="detailColumns" />
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { getProductPage } from '../../api/store'
import {
  approvePurchase,
  completePurchase,
  createPurchase,
  getPurchaseDetail,
  getPurchasePage,
  getSupplierPage,
  getWarehousePage
} from '../../api/material'
import type {
  MaterialPurchaseOrder,
  MaterialPurchaseOrderItem,
  MaterialSupplierItem,
  MaterialWarehouseItem,
  PageResult,
  ProductItem
} from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<MaterialPurchaseOrder[]>([])
const total = ref(0)
const detailOpen = ref(false)
const detail = ref<MaterialPurchaseOrder>()

const query = reactive({
  status: undefined as string | undefined,
  keyword: '',
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '采购单号', dataIndex: 'orderNo', key: 'orderNo', width: 180 },
  { title: '仓库', dataIndex: 'warehouseName', key: 'warehouseName', width: 140 },
  { title: '供应商', dataIndex: 'supplierName', key: 'supplierName', width: 160 },
  { title: '下单日期', dataIndex: 'orderDate', key: 'orderDate', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '总金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120 },
  { title: '操作', key: 'actions', width: 180 }
]

const detailColumns = [
  { title: '物资ID', dataIndex: 'productId', key: 'productId', width: 100 },
  { title: '物资名称', dataIndex: 'productName', key: 'productName' },
  { title: '数量', dataIndex: 'quantity', key: 'quantity', width: 80 },
  { title: '单价', dataIndex: 'unitPrice', key: 'unitPrice', width: 100 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 120 }
]

const itemColumns = [
  { title: '物资', key: 'productId' },
  { title: '数量', key: 'quantity', width: 120 },
  { title: '单价', key: 'unitPrice', width: 140 },
  { title: '金额', key: 'amount', width: 120 },
  { title: '操作', key: 'actions', width: 80 }
]

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已审核', value: 'APPROVED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已作废', value: 'CANCELLED' }
]

const warehouses = ref<MaterialWarehouseItem[]>([])
const suppliers = ref<MaterialSupplierItem[]>([])
const products = ref<ProductItem[]>([])

const warehouseOptions = computed(() => warehouses.value.map((it) => ({ label: it.warehouseName, value: it.id })))
const supplierOptions = computed(() => suppliers.value.map((it) => ({ label: it.supplierName, value: it.id })))
const productOptions = computed(() =>
  products.value.map((it) => ({ label: `${it.productName} (ID:${it.idStr || it.id})`, value: Number(it.idStr || it.id) }))
)

const editorOpen = ref(false)
const form = reactive({
  warehouseId: undefined as number | undefined,
  supplierId: undefined as number | undefined,
  orderDate: undefined as any,
  remark: '',
  items: [] as (MaterialPurchaseOrderItem & { _idx: number })[]
})

function statusColor(status?: string) {
  if (status === 'DRAFT') return 'default'
  if (status === 'APPROVED') return 'blue'
  if (status === 'COMPLETED') return 'green'
  return 'red'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<MaterialPurchaseOrder> = await getPurchasePage(query)
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  const [wRes, sRes, pRes] = await Promise.all([
    getWarehousePage({ pageNo: 1, pageSize: 200 }),
    getSupplierPage({ pageNo: 1, pageSize: 200 }),
    getProductPage({ pageNo: 1, pageSize: 500 })
  ])
  warehouses.value = wRes.list
  suppliers.value = sRes.list
  products.value = pRes.list
}

function reset() {
  query.status = undefined
  query.keyword = ''
  query.pageNo = 1
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(_page: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

function openCreate() {
  Object.assign(form, {
    warehouseId: undefined,
    supplierId: undefined,
    orderDate: dayjs(),
    remark: '',
    items: [{ _idx: Date.now(), productId: 0, quantity: 1, unitPrice: 0 }]
  })
  editorOpen.value = true
}

function addItem() {
  form.items.push({ _idx: Date.now() + Math.random(), productId: 0, quantity: 1, unitPrice: 0 })
}

function removeItem(index: number) {
  form.items.splice(index, 1)
}

async function submit() {
  if (!form.items.length) {
    message.warning('请至少添加一条明细')
    return
  }
  const invalid = form.items.some((it) => !it.productId || !it.quantity || it.quantity <= 0)
  if (invalid) {
    message.warning('请完善明细中的物资与数量')
    return
  }
  saving.value = true
  try {
    await createPurchase({
      warehouseId: form.warehouseId,
      supplierId: form.supplierId,
      orderDate: form.orderDate?.format?.('YYYY-MM-DD'),
      remark: form.remark,
      items: form.items.map((it) => ({ productId: it.productId, quantity: it.quantity, unitPrice: it.unitPrice }))
    })
    message.success('采购单创建成功')
    editorOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function viewDetail(id: number) {
  detail.value = await getPurchaseDetail(id)
  detailOpen.value = true
}

async function approve(id: number) {
  await approvePurchase(id)
  message.success('已审核')
  fetchData()
}

async function complete(id: number) {
  await completePurchase(id)
  message.success('已完成')
  fetchData()
}

onMounted(async () => {
  await loadOptions()
  fetchData()
})
</script>

<style scoped>
.search-form {
  row-gap: 8px;
}
.table-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}
.items-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 8px 0;
  font-weight: 500;
}
</style>
