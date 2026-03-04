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
      <a-row :gutter="12" style="margin-bottom: 12px">
        <a-col :span="6"><a-statistic title="固定资产金额" :value="summaryStats.assetAmount" :precision="2" /></a-col>
        <a-col :span="6"><a-statistic title="耗材金额" :value="summaryStats.consumableAmount" :precision="2" /></a-col>
        <a-col :span="6"><a-statistic title="食材金额" :value="summaryStats.foodAmount" :precision="2" /></a-col>
        <a-col :span="6"><a-statistic title="服务金额" :value="summaryStats.serviceAmount" :precision="2" /></a-col>
      </a-row>
      <a-table
        row-key="id"
        :loading="loading"
        :data-source="displayRows"
        :pagination="false"
        :columns="columns"
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
          </template>
          <template v-if="column.key === 'source'">
            <a-tag>{{ sourceLabel(record.source) }}</a-tag>
          </template>
          <template v-if="column.key === 'typeAmountSummary'">
            <span>{{ typeAmountSummary(record) }}</span>
          </template>
          <template v-if="column.key === 'actions'">
            <a-space>
              <a @click="viewDetail(record.id)">明细</a>
              <a v-if="record.status === ORDER_STATUS.DRAFT" @click="openEdit(record.id)">编辑</a>
              <a v-if="record.status === ORDER_STATUS.DRAFT" @click="approve(record.id)">审核</a>
              <a v-if="record.status === ORDER_STATUS.APPROVED" @click="complete(record.id)">完成</a>
              <a v-if="record.status === ORDER_STATUS.DRAFT || record.status === ORDER_STATUS.APPROVED" @click="cancel(record.id)">作废</a>
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
      :title="editingId ? '编辑采购单' : '新建采购单'"
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
        <a-space>
          <a-select v-model:value="productFilter.businessDomain" style="width: 140px" :options="purchaseDomainOptions" />
          <a-select v-model:value="productFilter.itemType" style="width: 140px" :options="purchaseItemTypeOptions" />
          <a-button size="small" @click="addItem">新增行</a-button>
        </a-space>
      </div>
      <a-table row-key="_idx" :data-source="form.items" :pagination="false" size="small" :columns="itemColumns">
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'productId'">
            <a-select v-model:value="record.productId" :options="filteredProductOptions" show-search allow-clear />
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
        <a-descriptions-item label="状态">{{ statusLabel(detail.status) }}</a-descriptions-item>
        <a-descriptions-item label="来源">{{ sourceLabel(detail.source) }}</a-descriptions-item>
        <a-descriptions-item label="来源关联">{{ detail.sourceRef || '-' }}</a-descriptions-item>
        <a-descriptions-item label="仓库">{{ detail.warehouseName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="供应商">{{ detail.supplierName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="下单日期">{{ detail.orderDate || '-' }}</a-descriptions-item>
        <a-descriptions-item label="总金额">{{ Number(detail.totalAmount || 0).toFixed(2) }}</a-descriptions-item>
        <a-descriptions-item label="固定资产金额">{{ Number(detail.assetAmount || 0).toFixed(2) }}</a-descriptions-item>
        <a-descriptions-item label="耗材金额">{{ Number(detail.consumableAmount || 0).toFixed(2) }}</a-descriptions-item>
        <a-descriptions-item label="食材金额">{{ Number(detail.foodAmount || 0).toFixed(2) }}</a-descriptions-item>
        <a-descriptions-item label="服务金额">{{ Number(detail.serviceAmount || 0).toFixed(2) }}</a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</a-descriptions-item>
      </a-descriptions>

      <a-table style="margin-top: 12px" :pagination="false" :data-source="detailItemRows" size="small" :columns="detailColumns" />
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { getProductPage } from '../../api/store'
import {
  MATERIAL_ORDER_STATUS,
  MATERIAL_PURCHASE_STATUS_OPTIONS,
  type MaterialOrderStatus,
  materialOrderStatusColor,
  materialOrderStatusLabel
} from '../../utils/materialStatus'
import {
  approvePurchase,
  cancelPurchase,
  completePurchase,
  createPurchase,
  getPurchaseDetail,
  getPurchasePage,
  getSupplierPage,
  getWarehousePage,
  updatePurchase
} from '../../api/materialCenter'
import type {
  MaterialPurchaseOrder,
  MaterialPurchaseOrderItem,
  MaterialSupplierItem,
  MaterialWarehouseItem,
  PageResult,
  ProductItem
} from '../../types'

const loading = ref(false)
const route = useRoute()
const router = useRouter()
const saving = ref(false)
const rows = ref<MaterialPurchaseOrder[]>([])
const total = ref(0)
const detailOpen = ref(false)
const detail = ref<MaterialPurchaseOrder>()
const ORDER_STATUS = MATERIAL_ORDER_STATUS

const query = reactive({
  status: undefined as MaterialOrderStatus | undefined,
  source: undefined as string | undefined,
  keyword: '',
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '采购单号', dataIndex: 'orderNo', key: 'orderNo', width: 180 },
  { title: '来源', dataIndex: 'source', key: 'source', width: 140 },
  { title: '仓库', dataIndex: 'warehouseName', key: 'warehouseName', width: 140 },
  { title: '供应商', dataIndex: 'supplierName', key: 'supplierName', width: 160 },
  { title: '类型金额(资/耗/食)', key: 'typeAmountSummary', width: 220 },
  { title: '下单日期', dataIndex: 'orderDate', key: 'orderDate', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '总金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120 },
  { title: '操作', key: 'actions', width: 180, fixed: 'right' as const }
]

const detailColumns = [
  { title: '物资ID', dataIndex: 'productId', key: 'productId', width: 100 },
  { title: '物资名称', dataIndex: 'productName', key: 'productName' },
  { title: '物资类型', dataIndex: 'itemTypeText', key: 'itemTypeText', width: 100 },
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

const statusOptions = MATERIAL_PURCHASE_STATUS_OPTIONS

const warehouses = ref<MaterialWarehouseItem[]>([])
const suppliers = ref<MaterialSupplierItem[]>([])
const products = ref<ProductItem[]>([])

const warehouseOptions = computed(() => warehouses.value.map((it) => ({ label: it.warehouseName, value: it.id })))
const supplierOptions = computed(() => suppliers.value.map((it) => ({ label: it.supplierName, value: it.id })))
const purchaseDomainOptions = [
  { label: '企业+双用途', value: 'INTERNAL_OR_BOTH' },
  { label: '企业内部', value: 'INTERNAL' },
  { label: '商城', value: 'MALL' },
  { label: '双用途', value: 'BOTH' },
  { label: '全部', value: 'ALL' }
]
const purchaseItemTypeOptions = [
  { label: '全部类型', value: 'ALL' },
  { label: '固定资产', value: 'ASSET' },
  { label: '耗材', value: 'CONSUMABLE' },
  { label: '食材', value: 'FOOD' },
  { label: '服务', value: 'SERVICE' }
]
const productFilter = reactive({
  businessDomain: 'INTERNAL_OR_BOTH',
  itemType: 'ALL'
})
const detailItemRows = computed(() =>
  (detail.value?.items || []).map((it) => ({
    ...it,
    itemTypeText: itemTypeLabel(it.itemType)
  }))
)
const filteredProductOptions = computed(() =>
  products.value
    .filter((it) => matchDomainFilter(it.businessDomain))
    .filter((it) => productFilter.itemType === 'ALL' || (it.itemType || '') === productFilter.itemType)
    .map((it) => ({ label: `${it.productName} (ID:${it.idStr || it.id})`, value: Number(it.idStr || it.id) }))
)
const summaryStats = computed(() => ({
  assetAmount: displayRows.value.reduce((sum, row) => sum + Number(row.assetAmount || 0), 0),
  consumableAmount: displayRows.value.reduce((sum, row) => sum + Number(row.consumableAmount || 0), 0),
  foodAmount: displayRows.value.reduce((sum, row) => sum + Number(row.foodAmount || 0), 0),
  serviceAmount: displayRows.value.reduce((sum, row) => sum + Number(row.serviceAmount || 0), 0)
}))
const displayRows = computed(() => {
  if (!query.source) return rows.value
  return rows.value.filter((row) => String(row.source || '') === query.source)
})

const editorOpen = ref(false)
const editingId = ref<number>()
const form = reactive({
  warehouseId: undefined as number | undefined,
  supplierId: undefined as number | undefined,
  orderDate: undefined as any,
  remark: '',
  items: [] as (MaterialPurchaseOrderItem & { _idx: number })[]
})

function statusColor(status?: string) {
  return materialOrderStatusColor(status)
}

function statusLabel(status?: string) {
  return materialOrderStatusLabel(status)
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
    getWarehousePage({ pageNo: 1, pageSize: 200, enabledOnly: true }),
    getSupplierPage({ pageNo: 1, pageSize: 200, enabledOnly: true }),
    getProductPage({ pageNo: 1, pageSize: 500 })
  ])
  warehouses.value = wRes.list
  suppliers.value = sRes.list
  products.value = pRes.list
}

function reset() {
  query.status = undefined
  query.source = undefined
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

async function openCreate() {
  await loadOptions()
  editingId.value = undefined
  productFilter.businessDomain = 'INTERNAL_OR_BOTH'
  productFilter.itemType = 'ALL'
  const routePrefill = getRoutePrefillItems()
  const routeRemark = getRouteSourceRemark()
  Object.assign(form, {
    warehouseId: undefined,
    supplierId: undefined,
    orderDate: dayjs(),
    remark: routeRemark,
    items: routePrefill.length ? routePrefill : [{ _idx: Date.now(), productId: 0, quantity: 1, unitPrice: 0 }]
  })
  editorOpen.value = true
}

async function openEdit(id: number) {
  await loadOptions()
  productFilter.businessDomain = 'INTERNAL_OR_BOTH'
  productFilter.itemType = 'ALL'
  const order = await getPurchaseDetail(id)
  if (order.status !== ORDER_STATUS.DRAFT) {
    message.warning('仅草稿采购单可编辑')
    return
  }
  editingId.value = id
  Object.assign(form, {
    warehouseId: order.warehouseId,
    supplierId: order.supplierId,
    orderDate: order.orderDate ? dayjs(order.orderDate) : dayjs(),
    remark: order.remark || '',
    items: (order.items || []).map((it, idx) => ({
      _idx: Date.now() + idx,
      productId: it.productId,
      quantity: it.quantity,
      unitPrice: it.unitPrice || 0
    }))
  })
  sanitizeDisabledSelections()
  editorOpen.value = true
}

function sanitizeDisabledSelections() {
  const selectedWarehouse = warehouses.value.find((it) => it.id === form.warehouseId)
  if (form.warehouseId && !selectedWarehouse) {
    form.warehouseId = undefined
    message.warning('原采购单仓库不可用，已自动清空，请重新选择（原因：仓库已停用或不存在）')
  }
  const selectedSupplier = suppliers.value.find((it) => it.id === form.supplierId)
  if (form.supplierId && !selectedSupplier) {
    form.supplierId = undefined
    message.warning('原采购单供应商不可用，已自动清空，请重新选择（原因：供应商已停用或不存在）')
  }
}

function addItem() {
  form.items.push({ _idx: Date.now() + Math.random(), productId: 0, quantity: 1, unitPrice: 0 })
}

function removeItem(index: number) {
  form.items.splice(index, 1)
}

function matchDomainFilter(domain?: string) {
  if (productFilter.businessDomain === 'ALL') return true
  if (productFilter.businessDomain === 'INTERNAL_OR_BOTH') {
    return (domain || 'BOTH') === 'INTERNAL' || (domain || 'BOTH') === 'BOTH'
  }
  return (domain || 'BOTH') === productFilter.businessDomain
}

function sourceLabel(source?: string) {
  if (!source) return '手工'
  if (source === 'inventory_alert_batch') return '库存预警(批量)'
  if (source === 'inventory_alert') return '库存预警'
  if (source === 'store_order_item') return '商城订单(单品)'
  if (source === 'store_order_detail') return '商城订单(整单)'
  return source
}

function itemTypeLabel(value?: string) {
  if (value === 'ASSET') return '固定资产'
  if (value === 'FOOD') return '食材'
  if (value === 'SERVICE') return '服务'
  if (value === 'CONSUMABLE') return '耗材'
  return '未设置'
}

function typeAmountSummary(record: MaterialPurchaseOrder) {
  const asset = Number(record.assetAmount || 0).toFixed(0)
  const consumable = Number(record.consumableAmount || 0).toFixed(0)
  const food = Number(record.foodAmount || 0).toFixed(0)
  return `${asset}/${consumable}/${food}`
}

async function submit() {
  if (!form.warehouseId) {
    message.warning('请选择仓库')
    return
  }
  if (!form.supplierId) {
    message.warning('请选择供应商')
    return
  }
  const selectedWarehouse = warehouses.value.find((it) => it.id === form.warehouseId)
  if (!selectedWarehouse) {
    message.warning('当前仓库不可用，请选择启用中的仓库（原因：仓库已停用或不存在）')
    form.warehouseId = undefined
    return
  }
  const selectedSupplier = suppliers.value.find((it) => it.id === form.supplierId)
  if (!selectedSupplier) {
    message.warning('当前供应商不可用，请选择启用中的供应商（原因：供应商已停用或不存在）')
    form.supplierId = undefined
    return
  }
  if (!form.items.length) {
    message.warning('请至少添加一条明细')
    return
  }
  const invalid = form.items.some((it) => !it.productId || !it.quantity || it.quantity <= 0 || it.unitPrice < 0)
  if (invalid) {
    message.warning('请完善明细中的物资、数量与单价')
    return
  }
  const productIds = form.items.map((it) => Number(it.productId))
  if (new Set(productIds).size !== productIds.length) {
    message.warning('同一物资不能重复添加')
    return
  }
  saving.value = true
  try {
    const payload = {
      warehouseId: form.warehouseId,
      supplierId: form.supplierId,
      orderDate: form.orderDate?.format?.('YYYY-MM-DD'),
      remark: form.remark,
      items: form.items.map((it) => ({ productId: it.productId, quantity: it.quantity, unitPrice: it.unitPrice }))
    }
    if (editingId.value) {
      await updatePurchase(editingId.value, payload)
      message.success('采购单已更新')
    } else {
      await createPurchase(payload)
      message.success('采购单创建成功')
    }
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
  const confirmed = await confirmAction('确认审核采购单？', '审核后可执行入库流程')
  if (!confirmed) {
    return
  }
  await approvePurchase(id)
  message.success('已审核')
  fetchData()
}

async function complete(id: number) {
  const confirmed = await confirmAction('确认完成采购单？', '完成后将执行采购入库并更新库存')
  if (!confirmed) {
    return
  }
  await completePurchase(id)
  message.success('已完成')
  fetchData()
}

async function cancel(id: number) {
  const confirmed = await confirmAction('确认作废采购单？', '作废后该单据不可继续审核或完成')
  if (!confirmed) {
    return
  }
  await cancelPurchase(id)
  message.success('已作废')
  fetchData()
}

async function confirmAction(title: string, content: string): Promise<boolean> {
  return new Promise((resolve) => {
    Modal.confirm({
      title,
      content,
      onOk: () => resolve(true),
      onCancel: () => resolve(false)
    })
  })
}

function parseQueryNumber(value: unknown): number | undefined {
  const raw = Array.isArray(value) ? value[0] : value
  if (raw === undefined || raw === null || raw === '') return undefined
  const n = Number(raw)
  return Number.isNaN(n) ? undefined : n
}

function getRoutePrefillItems() {
  const productIdsRaw = Array.isArray(route.query.productIds) ? route.query.productIds[0] : route.query.productIds
  const quantitiesRaw = Array.isArray(route.query.quantities) ? route.query.quantities[0] : route.query.quantities
  if (typeof productIdsRaw === 'string' && productIdsRaw.trim()) {
    const ids = productIdsRaw
      .split(',')
      .map((item) => Number(item.trim()))
      .filter((item) => Number.isFinite(item) && item > 0)
    const quantities = String(quantitiesRaw || '')
      .split(',')
      .map((item) => Math.max(Number(item.trim()) || 1, 1))
    return ids.map((id, index) => ({
      _idx: Date.now() + index,
      productId: id,
      quantity: quantities[index] || 1,
      unitPrice: 0
    }))
  }

  const productId = parseQueryNumber(route.query.productId)
  const quantity = Math.max(parseQueryNumber(route.query.quantity) || 1, 1)
  if (!productId) return []
  return [{ _idx: Date.now(), productId, quantity, unitPrice: 0 }]
}

function getRouteSourceRemark() {
  const sourceRaw = Array.isArray(route.query.source) ? route.query.source[0] : route.query.source
  const sourceRefRaw = Array.isArray(route.query.sourceRef) ? route.query.sourceRef[0] : route.query.sourceRef
  if (!sourceRaw && !sourceRefRaw) return ''
  const sourceText = sourceRaw ? `来源=${String(sourceRaw)}` : ''
  const sourceRefText = sourceRefRaw ? `关联=${String(sourceRefRaw)}` : ''
  return [sourceText, sourceRefText].filter(Boolean).join('；')
}

function applyRouteFilters() {
  const statusRaw = Array.isArray(route.query.status) ? route.query.status[0] : route.query.status
  const sourceRaw = Array.isArray(route.query.source) ? route.query.source[0] : route.query.source
  const keywordRaw = Array.isArray(route.query.keyword) ? route.query.keyword[0] : route.query.keyword
  if (statusRaw) {
    query.status = String(statusRaw) as MaterialOrderStatus
  }
  if (typeof sourceRaw === 'string' && sourceRaw) {
    query.source = sourceRaw
  }
  if (typeof keywordRaw === 'string') {
    query.keyword = keywordRaw
  }
}

async function autoOpenFromRouteIfNeeded() {
  const autoCreate = String(Array.isArray(route.query.autoCreate) ? route.query.autoCreate[0] : route.query.autoCreate || '')
  if (autoCreate !== '1') return
  await openCreate()
  const nextQuery = { ...route.query }
  delete (nextQuery as any).autoCreate
  await router.replace({ path: route.path, query: nextQuery })
}

onMounted(async () => {
  applyRouteFilters()
  await loadOptions()
  await fetchData()
  await autoOpenFromRouteIfNeeded()
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
