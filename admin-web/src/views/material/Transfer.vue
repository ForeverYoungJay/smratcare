<template>
  <PageContainer title="物资调拨" subTitle="跨仓调拨申请与执行跟踪">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
        <a-form-item label="状态">
          <a-select v-model:value="query.status" style="width: 160px" allow-clear :options="statusOptions" />
        </a-form-item>
        <a-form-item label="单号">
          <a-input v-model:value="query.keyword" placeholder="调拨单号" allow-clear />
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
        <a-button type="primary" @click="openCreate">新建调拨单</a-button>
      </div>
      <a-table row-key="id" :loading="loading" :data-source="rows" :pagination="false" :columns="columns" size="middle">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'COMPLETED' ? 'green' : 'blue'">{{ record.status }}</a-tag>
          </template>
          <template v-if="column.key === 'actions'">
            <a-space>
              <a @click="viewDetail(record.id)">明细</a>
              <a v-if="record.status === 'DRAFT'" @click="complete(record.id)">执行完成</a>
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

    <a-modal v-model:open="editorOpen" title="新建调拨单" width="860px" :confirm-loading="saving" @ok="submit">
      <a-form layout="vertical" :model="form">
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="调出仓库">
              <a-select v-model:value="form.fromWarehouseId" :options="warehouseOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="调入仓库">
              <a-select v-model:value="form.toWarehouseId" :options="warehouseOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>

      <div class="items-header">
        <span>调拨明细</span>
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
          <template v-else-if="column.key === 'actions'">
            <a @click="removeItem(index)">删除</a>
          </template>
        </template>
      </a-table>
    </a-modal>

    <a-drawer v-model:open="detailOpen" title="调拨单明细" width="640">
      <a-descriptions bordered :column="2" size="small" v-if="detail">
        <a-descriptions-item label="调拨单号">{{ detail.transferNo }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ detail.status }}</a-descriptions-item>
        <a-descriptions-item label="调出仓库">{{ detail.fromWarehouseName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="调入仓库">{{ detail.toWarehouseName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</a-descriptions-item>
      </a-descriptions>
      <a-table style="margin-top: 12px" :pagination="false" :data-source="detail?.items || []" :columns="detailColumns" size="small" />
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getProductPage } from '../../api/store'
import {
  completeTransfer,
  createTransfer,
  getTransferDetail,
  getTransferPage,
  getWarehousePage
} from '../../api/material'
import type { MaterialTransferOrder, MaterialWarehouseItem, PageResult, ProductItem } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<MaterialTransferOrder[]>([])
const total = ref(0)
const detailOpen = ref(false)
const detail = ref<MaterialTransferOrder>()

const query = reactive({
  status: undefined as string | undefined,
  keyword: '',
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '调拨单号', dataIndex: 'transferNo', key: 'transferNo', width: 180 },
  { title: '调出仓库', dataIndex: 'fromWarehouseName', key: 'fromWarehouseName', width: 160 },
  { title: '调入仓库', dataIndex: 'toWarehouseName', key: 'toWarehouseName', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'actions', width: 160 }
]

const detailColumns = [
  { title: '物资ID', dataIndex: 'productId', key: 'productId', width: 120 },
  { title: '物资名称', dataIndex: 'productName', key: 'productName' },
  { title: '数量', dataIndex: 'quantity', key: 'quantity', width: 120 }
]

const itemColumns = [
  { title: '物资', key: 'productId' },
  { title: '数量', key: 'quantity', width: 140 },
  { title: '操作', key: 'actions', width: 80 }
]

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已作废', value: 'CANCELLED' }
]

const warehouses = ref<MaterialWarehouseItem[]>([])
const products = ref<ProductItem[]>([])
const warehouseOptions = computed(() => warehouses.value.map((it) => ({ label: it.warehouseName, value: it.id })))
const productOptions = computed(() =>
  products.value.map((it) => ({ label: `${it.productName} (ID:${it.idStr || it.id})`, value: Number(it.idStr || it.id) }))
)

const editorOpen = ref(false)
const form = reactive({
  fromWarehouseId: undefined as number | undefined,
  toWarehouseId: undefined as number | undefined,
  remark: '',
  items: [] as Array<{ _idx: number; productId: number; quantity: number }>
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<MaterialTransferOrder> = await getTransferPage(query)
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  const [wRes, pRes] = await Promise.all([
    getWarehousePage({ pageNo: 1, pageSize: 200 }),
    getProductPage({ pageNo: 1, pageSize: 500 })
  ])
  warehouses.value = wRes.list
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
    fromWarehouseId: undefined,
    toWarehouseId: undefined,
    remark: '',
    items: [{ _idx: Date.now(), productId: 0, quantity: 1 }]
  })
  editorOpen.value = true
}

function addItem() {
  form.items.push({ _idx: Date.now() + Math.random(), productId: 0, quantity: 1 })
}

function removeItem(index: number) {
  form.items.splice(index, 1)
}

async function submit() {
  if (!form.fromWarehouseId || !form.toWarehouseId) {
    message.warning('请选择调出和调入仓库')
    return
  }
  if (form.fromWarehouseId === form.toWarehouseId) {
    message.warning('调出和调入仓库不能相同')
    return
  }
  if (!form.items.length) {
    message.warning('请至少添加一条明细')
    return
  }
  const invalid = form.items.some((it) => !it.productId || !it.quantity || it.quantity <= 0)
  if (invalid) {
    message.warning('请完善明细中的物资和数量')
    return
  }

  saving.value = true
  try {
    await createTransfer({
      fromWarehouseId: form.fromWarehouseId,
      toWarehouseId: form.toWarehouseId,
      remark: form.remark,
      items: form.items.map((it) => ({ productId: it.productId, quantity: it.quantity }))
    })
    message.success('调拨单创建成功')
    editorOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function viewDetail(id: number) {
  detail.value = await getTransferDetail(id)
  detailOpen.value = true
}

async function complete(id: number) {
  await completeTransfer(id)
  message.success('调拨已完成')
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
