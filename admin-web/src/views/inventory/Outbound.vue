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
        <a-form-item label="商品">
          <a-select
            v-model:value="query.productId"
            :options="productOptions"
            allow-clear
            show-search
            option-filter-prop="label"
            style="width: 220px"
          />
        </a-form-item>
        <a-form-item label="业务域">
          <a-select v-model:value="query.businessDomain" allow-clear style="width: 140px" :options="businessDomainOptions" />
        </a-form-item>
        <a-form-item label="物资类型">
          <a-select v-model:value="query.itemType" allow-clear style="width: 140px" :options="itemTypeOptions" />
        </a-form-item>
        <a-form-item label="日期">
          <a-range-picker v-model:value="query.range" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">搜索</a-button>
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
      <a-row :gutter="12" style="margin-bottom: 12px">
        <a-col :span="6"><a-statistic title="固定资产出库" :value="summaryStats.assetQty" /></a-col>
        <a-col :span="6"><a-statistic title="耗材出库" :value="summaryStats.consumableQty" /></a-col>
        <a-col :span="6"><a-statistic title="食材出库" :value="summaryStats.foodQty" /></a-col>
        <a-col :span="6"><a-statistic title="服务出库" :value="summaryStats.serviceQty" /></a-col>
      </a-row>

      <vxe-toolbar custom export></vxe-toolbar>
      <vxe-table
        border
        stripe
        show-overflow
        height="520"
        :loading="loading"
        :data="filteredRows"
        :column-config="{ resizable: true }"
      >
        <vxe-column field="createTime" title="出库时间" width="180" />
        <vxe-column field="productName" title="商品名称" min-width="160" />
        <vxe-column field="productId" title="商品ID" width="120" />
        <vxe-column field="itemType" title="物资类型" width="110">
          <template #default="{ row }">{{ itemTypeLabel(productById.get(Number(row.productId))?.itemType) }}</template>
        </vxe-column>
        <vxe-column field="businessDomain" title="业务域" width="110">
          <template #default="{ row }">{{ domainLabel(productById.get(Number(row.productId))?.businessDomain) }}</template>
        </vxe-column>
        <vxe-column field="batchNo" title="批次号" min-width="160" />
        <vxe-column field="changeQty" title="出库数量" width="120" />
        <vxe-column field="receiverName" title="领取人" width="120" />
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
        <a-descriptions-item label="领取人">{{ detail?.receiverName || '-' }}</a-descriptions-item>
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
        <a-form-item label="商品" name="productId">
          <a-select
            v-model:value="outboundForm.productId"
            :options="productOptions"
            allow-clear
            show-search
            option-filter-prop="label"
          />
        </a-form-item>
        <a-form-item label="批次ID">
          <a-input-number v-model:value="outboundForm.batchId" style="width: 100%" />
        </a-form-item>
        <a-form-item label="出库数量" name="quantity">
          <a-input-number v-model:value="outboundForm.quantity" :min="1" style="width: 100%" />
        </a-form-item>
        <a-form-item label="领取人" name="receiverName">
          <a-input v-model:value="outboundForm.receiverName" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="outboundForm.reason" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { getInventoryOutboundPage, createOutbound } from '../../api/materialCenter'
import { getOrderDetail, getProductPage } from '../../api/store'
import { message } from 'ant-design-vue'
import type { InventoryLogItem, InventoryOutboundRequest, PageResult, ProductItem } from '../../types'

const loading = ref(false)
const route = useRoute()
const rows = ref<InventoryLogItem[]>([])
const total = ref(0)
const products = ref<ProductItem[]>([])
const detailOpen = ref(false)
const detail = ref<InventoryLogItem | null>(null)
const outboundOpen = ref(false)
const outboundSubmitting = ref(false)
const outboundFormRef = ref()
const outboundForm = reactive<InventoryOutboundRequest>({
  productId: '',
  batchId: undefined,
  quantity: 1,
  receiverName: '',
  reason: ''
})
const outboundRules = {
  productId: [{ required: true, message: '请输入商品ID' }],
  quantity: [{ required: true, message: '请输入数量' }],
  receiverName: [{ required: true, message: '请输入领取人' }]
}

const query = reactive({
  outType: undefined as 'SALE' | 'CONSUME' | undefined,
  productId: undefined as number | undefined,
  businessDomain: undefined as string | undefined,
  itemType: undefined as string | undefined,
  range: undefined as any,
  pageNo: 1,
  pageSize: 10
})
const businessDomainOptions = [
  { label: '企业内部', value: 'INTERNAL' },
  { label: '商城', value: 'MALL' },
  { label: '双用途', value: 'BOTH' }
]
const itemTypeOptions = [
  { label: '固定资产', value: 'ASSET' },
  { label: '耗材', value: 'CONSUMABLE' },
  { label: '食材', value: 'FOOD' },
  { label: '服务', value: 'SERVICE' }
]
const productById = computed(() => {
  const map = new Map<number, ProductItem>()
  for (const product of products.value) {
    map.set(Number(product.idStr || product.id), product)
  }
  return map
})
const productOptions = computed(() =>
  products.value
    .filter((product) => !query.businessDomain || (product.businessDomain || 'BOTH') === query.businessDomain)
    .filter((product) => !query.itemType || (product.itemType || '') === query.itemType)
    .map((p) => ({
      label: `${p.productName} (ID:${p.idStr || p.id})`,
      value: p.id
    }))
)
const filteredRows = computed(() =>
  rows.value.filter((row) => {
    if (query.productId && Number(row.productId) !== Number(query.productId)) return false
    const product = productById.value.get(Number(row.productId))
    if (query.businessDomain && (product?.businessDomain || 'BOTH') !== query.businessDomain) return false
    if (query.itemType && (product?.itemType || '') !== query.itemType) return false
    return true
  })
)
const summaryStats = computed(() => {
  const result = { assetQty: 0, consumableQty: 0, foodQty: 0, serviceQty: 0 }
  for (const row of filteredRows.value) {
    const qty = Number(row.changeQty || 0)
    const itemType = productById.value.get(Number(row.productId))?.itemType || 'CONSUMABLE'
    if (itemType === 'ASSET') result.assetQty += qty
    else if (itemType === 'FOOD') result.foodQty += qty
    else if (itemType === 'SERVICE') result.serviceQty += qty
    else result.consumableQty += qty
  }
  return result
})

async function fetchProducts() {
  const res: PageResult<ProductItem> = await getProductPage({ pageNo: 1, pageSize: 200 })
  products.value = res.list
}

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
  query.businessDomain = undefined
  query.itemType = undefined
  query.range = undefined
  query.pageNo = 1
  fetchData()
}

function handleSearch() {
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
    filteredRows.value.map((r) => ({
      出库时间: r.createTime,
      商品名称: r.productName,
      商品ID: r.productId,
      业务域: domainLabel(productById.value.get(Number(r.productId))?.businessDomain),
      物资类型: itemTypeLabel(productById.value.get(Number(r.productId))?.itemType),
      批次号: r.batchNo,
      出库数量: r.changeQty,
      领取人: r.receiverName,
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
  Object.assign(outboundForm, { productId: '', batchId: undefined, quantity: 1, receiverName: '', reason: '' })
  outboundOpen.value = true
}

async function submitOutbound() {
  await outboundFormRef.value?.validate()
  outboundSubmitting.value = true
  try {
    await createOutbound({
      ...outboundForm,
      productId: String(outboundForm.productId)
    })
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

function domainLabel(value?: string) {
  if (value === 'INTERNAL') return '企业内部'
  if (value === 'MALL') return '商城'
  if (value === 'BOTH') return '双用途'
  return '未设置'
}

function itemTypeLabel(value?: string) {
  if (value === 'ASSET') return '固定资产'
  if (value === 'FOOD') return '食材'
  if (value === 'SERVICE') return '服务'
  if (value === 'CONSUMABLE') return '耗材'
  return '未设置'
}

function applyRouteFilters() {
  const routeQuery = route.query || {}
  const outTypeRaw = Array.isArray(routeQuery.outType) ? routeQuery.outType[0] : routeQuery.outType
  const productIdRaw = Array.isArray(routeQuery.productId) ? routeQuery.productId[0] : routeQuery.productId
  const domainRaw = Array.isArray(routeQuery.businessDomain) ? routeQuery.businessDomain[0] : routeQuery.businessDomain
  const itemTypeRaw = Array.isArray(routeQuery.itemType) ? routeQuery.itemType[0] : routeQuery.itemType
  if (outTypeRaw === 'SALE' || outTypeRaw === 'CONSUME') {
    query.outType = outTypeRaw
  }
  if (productIdRaw !== undefined) {
    const parsed = Number(productIdRaw)
    query.productId = Number.isNaN(parsed) ? undefined : parsed
  }
  if (typeof domainRaw === 'string' && domainRaw) {
    query.businessDomain = domainRaw
  }
  if (typeof itemTypeRaw === 'string' && itemTypeRaw) {
    query.itemType = itemTypeRaw
  }
}

onMounted(() => {
  applyRouteFilters()
  fetchProducts()
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
  margin-bottom: 8px;
}
</style>
