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
          <a-button type="primary" @click="openOutboundSheet">新增领用出库</a-button>
          <a-button @click="exportCsvData">导出CSV</a-button>
        </a-space>
      </div>
      <a-row :gutter="12" style="margin-bottom: 12px">
        <a-col :span="6"><a-statistic title="固定资产出库" :value="summaryStats.assetQty" /></a-col>
        <a-col :span="6"><a-statistic title="耗材出库" :value="summaryStats.consumableQty" /></a-col>
        <a-col :span="6"><a-statistic title="食材出库" :value="summaryStats.foodQty" /></a-col>
        <a-col :span="6"><a-statistic title="服务出库" :value="summaryStats.serviceQty" /></a-col>
      </a-row>

      <vxe-table
        border
        stripe
        show-overflow
        height="460"
        :loading="loading"
        :data="filteredRows"
        :column-config="{ resizable: true }"
      >
        <vxe-column field="createTime" title="出库时间" width="180" />
        <vxe-column field="outboundNo" title="领用单号" min-width="160" />
        <vxe-column field="productName" title="商品名称" min-width="160" />
        <vxe-column field="productId" title="商品ID" width="120" />
        <vxe-column field="itemType" title="物资类型" width="110">
          <template #default="{ row }">{{ itemTypeLabel(productById.get(Number(row.productId))?.itemType) }}</template>
        </vxe-column>
        <vxe-column field="businessDomain" title="业务域" width="110">
          <template #default="{ row }">{{ domainLabel(productById.get(Number(row.productId))?.businessDomain) }}</template>
        </vxe-column>
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

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="领用单待确认">
      <a-form layout="inline" :model="sheetQuery" class="search-form" style="margin-bottom: 12px;">
        <a-form-item label="状态">
          <a-select v-model:value="sheetQuery.status" allow-clear style="width: 140px">
            <a-select-option value="DRAFT">待确认</a-select-option>
            <a-select-option value="CONFIRMED">已确认</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关键字">
          <a-input v-model:value="sheetQuery.keyword" placeholder="领用单号/领取人" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchSheetData">查询</a-button>
            <a-button @click="resetSheetQuery">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      <a-table
        row-key="id"
        :loading="sheetLoading"
        :data-source="sheets"
        :pagination="false"
        size="small"
      >
        <a-table-column title="领用单号" data-index="outboundNo" key="outboundNo" width="180" />
        <a-table-column title="领取人" data-index="receiverName" key="receiverName" width="120" />
        <a-table-column title="老人院内ID" data-index="elderId" key="elderId" width="120" />
        <a-table-column title="合同号" data-index="contractNo" key="contractNo" width="140" />
        <a-table-column title="申请部门" data-index="applyDept" key="applyDept" width="120" />
        <a-table-column title="经办人" data-index="operatorName" key="operatorName" width="120" />
        <a-table-column title="状态" key="status" width="100">
          <template #default="{ record }">
            <a-tag :color="record.status === 'CONFIRMED' ? 'green' : 'orange'">{{ record.status === 'CONFIRMED' ? '已确认' : '待确认' }}</a-tag>
          </template>
        </a-table-column>
        <a-table-column title="明细行数" key="itemCount" width="100">
          <template #default="{ record }">{{ record.items?.length || 0 }}</template>
        </a-table-column>
        <a-table-column title="创建时间" data-index="createTime" key="createTime" width="180" />
        <a-table-column title="确认时间" data-index="confirmTime" key="confirmTime" width="180" />
        <a-table-column title="操作" key="action" width="280" fixed="right">
          <template #default="{ record }">
            <a-space>
              <a-button type="link" @click="openSheetDetail(record.id)">详情</a-button>
              <a-button type="link" @click="printSheet(record.id)">打印领取单</a-button>
              <a-button type="link" @click="downloadSheetPdf(record.id)">导出PDF</a-button>
              <a-button type="link" @click="downloadSheetHtml(record.id)">下载领取单</a-button>
              <a-button v-if="record.status !== 'CONFIRMED'" type="link" @click="confirmSheet(record.id)">确认出库</a-button>
            </a-space>
          </template>
        </a-table-column>
      </a-table>
      <a-pagination
        style="margin-top: 12px; text-align: right;"
        :current="sheetQuery.pageNo"
        :page-size="sheetQuery.pageSize"
        :total="sheetTotal"
        show-size-changer
        @change="onSheetPageChange"
        @showSizeChange="onSheetPageSizeChange"
      />
    </a-card>

    <a-drawer v-model:open="detailOpen" title="出库详情" width="520">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="领用单号">{{ detail?.outboundNo || '-' }}</a-descriptions-item>
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
    </a-drawer>

    <a-drawer v-model:open="sheetDetailOpen" title="领用单详情" width="680">
      <a-descriptions v-if="sheetDetail" :column="2" bordered size="small" style="margin-bottom: 12px;">
        <a-descriptions-item label="领用单号">{{ sheetDetail.outboundNo }}</a-descriptions-item>
        <a-descriptions-item label="领取人">{{ sheetDetail.receiverName }}</a-descriptions-item>
        <a-descriptions-item label="老人院内ID">{{ sheetDetail.elderId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="合同号">{{ sheetDetail.contractNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="申请部门">{{ sheetDetail.applyDept || '-' }}</a-descriptions-item>
        <a-descriptions-item label="经办人">{{ sheetDetail.operatorName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ sheetDetail.status === 'CONFIRMED' ? '已确认' : '待确认' }}</a-descriptions-item>
        <a-descriptions-item label="确认时间">{{ sheetDetail.confirmTime || '-' }}</a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">{{ sheetDetail.remark || '-' }}</a-descriptions-item>
      </a-descriptions>
      <a-table
        v-if="sheetDetail"
        row-key="id"
        :data-source="sheetDetail.items || []"
        :pagination="false"
        size="small"
      >
        <a-table-column title="商品" data-index="productName" key="productName" />
        <a-table-column title="规格/型号" key="unit" width="140">
          <template #default="{ record }">{{ itemSpec(record) }}</template>
        </a-table-column>
        <a-table-column title="数量" data-index="quantity" key="quantity" width="100" />
        <a-table-column title="批次" data-index="batchNo" key="batchNo" width="160" />
        <a-table-column title="原因" data-index="reason" key="reason" />
      </a-table>
      <a-space style="margin-top: 12px;">
        <a-button @click="printSheet(sheetDetail.id)">打印领取单</a-button>
        <a-button @click="downloadSheetPdf(sheetDetail.id)">导出PDF</a-button>
        <a-button @click="downloadSheetHtml(sheetDetail.id)">下载领取单</a-button>
        <a-button v-if="sheetDetail.status !== 'CONFIRMED'" type="primary" @click="confirmSheet(sheetDetail.id)">确认出库</a-button>
      </a-space>
    </a-drawer>

    <a-modal
      v-model:open="outboundOpen"
      title="新增领用出库"
      width="860"
      @ok="submitOutboundSheet"
      :confirm-loading="outboundSubmitting"
    >
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="领取人" required>
              <a-input v-model:value="sheetForm.receiverName" placeholder="请输入领取人" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="老人姓名">
              <a-select
                v-model:value="sheetForm.elderId"
                :options="elderSelectOptions"
                allow-clear
                show-search
                :filter-option="false"
                :loading="elderLoading"
                option-filter-prop="label"
                placeholder="输入老人姓名/拼音模糊搜索"
                @search="searchElders"
                @focus="() => searchElders('')"
                @change="onSheetElderChange"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="合同号">
              <a-input v-model:value="sheetForm.contractNo" placeholder="自动补齐（可手改）" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="领用单号">
              <a-input v-model:value="sheetForm.outboundNo" disabled placeholder="系统自动生成，不可填写" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="申请部门">
              <a-select
                v-model:value="sheetForm.applyDept"
                :options="departmentSelectOptions"
                allow-clear
                show-search
                :filter-option="false"
                :loading="departmentLoading"
                option-filter-prop="label"
                placeholder="请选择申请部门"
                @search="searchDepartments"
                @focus="() => searchDepartments('')"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="备注">
              <a-input v-model:value="sheetForm.remark" placeholder="可选" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="经办人（自动回填）">
              <a-input :value="currentOperatorName" disabled />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
      <a-table :data-source="sheetForm.items" :pagination="false" row-key="rowKey" size="small">
        <a-table-column title="商品" key="productId">
          <template #default="{ record }">
            <a-select
              v-model:value="record.productId"
              :options="productOptions"
              allow-clear
              show-search
              option-filter-prop="label"
              placeholder="选择商品"
            />
          </template>
        </a-table-column>
        <a-table-column title="规格/型号" key="spec" width="140">
          <template #default="{ record }">
            {{ productSpecById(record.productId) }}
          </template>
        </a-table-column>
        <a-table-column title="数量" key="quantity" width="120">
          <template #default="{ record }">
            <a-input-number v-model:value="record.quantity" :min="1" style="width: 100%;" />
          </template>
        </a-table-column>
        <a-table-column title="领用原因" key="reason">
          <template #default="{ record }">
            <a-input v-model:value="record.reason" placeholder="可选" />
          </template>
        </a-table-column>
        <a-table-column title="操作" key="action" width="120">
          <template #default="{ index }">
            <a-button type="link" danger @click="removeSheetItem(index)">删除</a-button>
          </template>
        </a-table-column>
      </a-table>
      <a-button style="margin-top: 10px;" @click="appendSheetItem">+ 增加物品</a-button>
      <a-alert style="margin-top: 10px;" type="info" show-icon message="保存后生成领用单；领取完成后请点击“确认出库”，并打印领取单给家属签字。" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Modal, message } from 'ant-design-vue'
import jsPDF from 'jspdf'
import autoTable from 'jspdf-autotable'
import { useUserStore } from '../../stores/user'
import { useElderOptions } from '../../composables/useElderOptions'
import { useDepartmentOptions } from '../../composables/useDepartmentOptions'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import {
  confirmOutboundSheet,
  createOutboundSheet,
  getInventoryOutboundPage,
  getOutboundSheetDetail,
  getOutboundSheetPage
} from '../../api/materialCenter'
import { getContractPage } from '../../api/marketing'
import { getProductPage } from '../../api/store'
import type {
  CrmContractItem,
  InventoryLogItem,
  InventoryOutboundSheet,
  InventoryOutboundSheetCreateRequest,
  InventoryOutboundSheetItemRequest,
  PageResult,
  ProductItem
} from '../../types'

const loading = ref(false)
const route = useRoute()
const userStore = useUserStore()
const { elderOptions, elderLoading, searchElders } = useElderOptions({ pageSize: 80, inHospitalOnly: true })
const { departmentOptions, departmentLoading, searchDepartments } = useDepartmentOptions({ pageSize: 120 })
const rows = ref<InventoryLogItem[]>([])
const total = ref(0)
const products = ref<ProductItem[]>([])
const detailOpen = ref(false)
const detail = ref<InventoryLogItem | null>(null)

const sheetLoading = ref(false)
const sheets = ref<InventoryOutboundSheet[]>([])
const sheetTotal = ref(0)
const sheetDetail = ref<InventoryOutboundSheet | null>(null)
const sheetDetailOpen = ref(false)

const outboundOpen = ref(false)
const outboundSubmitting = ref(false)
let sheetRowSeq = 1
const sheetForm = reactive<{
  receiverName: string
  outboundNo: string
  elderId?: number
  elderName: string
  contractNo: string
  applyDept?: string
  remark: string
  items: Array<{ rowKey: string; productId?: number | string; quantity: number; reason?: string }>
}>({
  receiverName: '',
  outboundNo: '',
  elderId: undefined,
  elderName: '',
  contractNo: '',
  applyDept: undefined,
  remark: '',
  items: []
})

const query = reactive({
  outType: undefined as 'SALE' | 'CONSUME' | undefined,
  productId: undefined as number | undefined,
  businessDomain: undefined as string | undefined,
  itemType: undefined as string | undefined,
  range: undefined as any,
  pageNo: 1,
  pageSize: 10
})

const sheetQuery = reactive({
  status: 'DRAFT' as 'DRAFT' | 'CONFIRMED' | undefined,
  keyword: '',
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

const currentOperatorName = computed(() => {
  const realName = String(userStore.staffInfo?.realName || '').trim()
  const username = String(userStore.staffInfo?.username || '').trim()
  return realName || username || '当前登录用户'
})

const elderSelectOptions = computed(() => elderOptions.value.map((item) => ({
  label: item.label,
  value: item.value
})))

const departmentSelectOptions = computed(() =>
  departmentOptions.value.map((item) => ({
    label: item.label,
    value: item.value
  }))
)

async function fetchProducts() {
  const res: PageResult<ProductItem> = await getProductPage({ pageNo: 1, pageSize: 300 })
  products.value = res.list || []
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
    rows.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function fetchSheetData() {
  sheetLoading.value = true
  try {
    const res: PageResult<InventoryOutboundSheet> = await getOutboundSheetPage({
      pageNo: sheetQuery.pageNo,
      pageSize: sheetQuery.pageSize,
      status: sheetQuery.status,
      keyword: sheetQuery.keyword || undefined
    })
    sheets.value = res.list || []
    sheetTotal.value = res.total || 0
  } finally {
    sheetLoading.value = false
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

function onSheetPageChange(page: number) {
  sheetQuery.pageNo = page
  fetchSheetData()
}

function onSheetPageSizeChange(_page: number, size: number) {
  sheetQuery.pageSize = size
  sheetQuery.pageNo = 1
  fetchSheetData()
}

function resetSheetQuery() {
  sheetQuery.status = 'DRAFT'
  sheetQuery.keyword = ''
  sheetQuery.pageNo = 1
  fetchSheetData()
}

function exportCsvData() {
  exportCsv(
    filteredRows.value.map((r) => ({
      出库时间: r.createTime,
      领用单号: r.outboundNo || '',
      商品名称: r.productName,
      商品ID: r.productId,
      业务域: domainLabel(productById.value.get(Number(r.productId))?.businessDomain),
      物资类型: itemTypeLabel(productById.value.get(Number(r.productId))?.itemType),
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

function openOutboundSheet() {
  sheetForm.receiverName = ''
  sheetForm.outboundNo = ''
  sheetForm.elderId = undefined
  sheetForm.elderName = ''
  sheetForm.contractNo = ''
  sheetForm.applyDept = undefined
  sheetForm.remark = ''
  sheetForm.items = []
  appendSheetItem()
  outboundOpen.value = true
}

async function resolveContractNoByElder(elderId?: number | string) {
  if (!elderId) return ''
  const normalizedElderId = Number(elderId)
  if (!normalizedElderId) return ''
  try {
    const page: PageResult<CrmContractItem> = await getContractPage({
      pageNo: 1,
      pageSize: 20,
      elderId: normalizedElderId
    })
    const list = page.list || []
    const active = list.find((item) => item.contractNo && item.status !== 'VOID')
    return (active || list.find((item) => item.contractNo))?.contractNo || ''
  } catch {
    return ''
  }
}

async function onSheetElderChange(elderId?: number) {
  const selected = elderOptions.value.find((item) => Number(item.value) === Number(elderId || 0))
  sheetForm.elderName = selected?.name || ''
  if (!elderId) {
    sheetForm.contractNo = ''
    return
  }
  const contractNo = await resolveContractNoByElder(elderId)
  sheetForm.contractNo = contractNo || ''
}

function appendSheetItem() {
  sheetForm.items.push({
    rowKey: `row-${Date.now()}-${sheetRowSeq++}`,
    productId: undefined,
    quantity: 1,
    reason: ''
  })
}

function removeSheetItem(index: number) {
  sheetForm.items.splice(index, 1)
}

async function submitOutboundSheet() {
  if (!sheetForm.receiverName.trim()) {
    message.warning('请输入领取人')
    return
  }
  if (!sheetForm.items.length) {
    message.warning('请至少添加一条领用物品')
    return
  }
  const invalid = sheetForm.items.find((item) => !item.productId || !item.quantity || item.quantity <= 0)
  if (invalid) {
    message.warning('请完整填写每条物品的商品与数量')
    return
  }
  outboundSubmitting.value = true
  const applyDeptName = sheetForm.applyDept
    ? (departmentOptions.value.find((item) => String(item.value) === String(sheetForm.applyDept))?.name || String(sheetForm.applyDept))
    : ''
  try {
    const payload: InventoryOutboundSheetCreateRequest = {
      receiverName: sheetForm.receiverName.trim(),
      outboundNo: sheetForm.outboundNo.trim() || undefined,
      elderId: sheetForm.elderId || undefined,
      contractNo: sheetForm.contractNo.trim() || undefined,
      applyDept: applyDeptName || undefined,
      remark: sheetForm.remark.trim() || undefined,
      items: sheetForm.items.map((item): InventoryOutboundSheetItemRequest => ({
        productId: item.productId as number | string,
        quantity: Number(item.quantity),
        reason: item.reason?.trim() || undefined
      }))
    }
    const created = await createOutboundSheet(payload)
    message.success(`领用单已创建：${created.outboundNo}，请领取完成后确认出库`)
    outboundOpen.value = false
    await fetchSheetData()
  } finally {
    outboundSubmitting.value = false
  }
}

async function openSheetDetail(id: number) {
  sheetDetail.value = await getOutboundSheetDetail(id)
  sheetDetailOpen.value = true
}

async function confirmSheet(id: number) {
  Modal.confirm({
    title: '确认领取完成并执行出库？',
    content: '确认后会扣减库存并生成出库日志，操作不可撤销。',
    onOk: async () => {
      await confirmOutboundSheet(id)
      message.success('确认出库成功')
      if (sheetDetail.value?.id === id) {
        sheetDetail.value = await getOutboundSheetDetail(id)
      }
      await Promise.all([fetchSheetData(), fetchData()])
    }
  })
}

async function printSheet(id: number) {
  const sheet = await getOutboundSheetDetail(id)
  const html = buildSheetHtml(sheet)
  const popup = window.open('', '_blank', 'width=980,height=760')
  if (!popup) {
    message.warning('浏览器拦截了弹窗，请允许后重试')
    return
  }
  popup.document.write(html)
  popup.document.close()
  popup.focus()
  popup.print()
}

async function downloadSheetHtml(id: number) {
  const sheet = await getOutboundSheetDetail(id)
  const html = buildSheetHtml(sheet)
  const blob = new Blob([html], { type: 'text/html;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `领用出库单_${sheet.outboundNo || id}.html`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  message.success('领取单已下载')
}

async function downloadSheetPdf(id: number) {
  const sheet = await getOutboundSheetDetail(id)
  const doc = new jsPDF({
    orientation: 'p',
    unit: 'pt',
    format: 'a4'
  })
  const marginLeft = 40
  let cursorY = 40
  doc.setFont('Helvetica', 'bold')
  doc.setFontSize(16)
  doc.text('领用出库单', 297, cursorY, { align: 'center' })
  cursorY += 28
  doc.setFont('Helvetica', 'normal')
  doc.setFontSize(10)
  const metaLines = [
    `领用人：${sheet.receiverName || '-'}`,
    `老人院内ID：${sheet.elderId || '-'}`,
    `合同号：${sheet.contractNo || '-'}`,
    `领用单号：${sheet.outboundNo || '-'}`,
    `申请部门：${sheet.applyDept || '-'}`,
    `经办人：${sheet.operatorName || '-'}`,
    `日期：${(sheet.createTime || '').slice(0, 10) || '-'}`,
    `备注：${sheet.remark || '-'}`
  ]
  for (const line of metaLines) {
    doc.text(line, marginLeft, cursorY)
    cursorY += 16
  }
  cursorY += 6
  const rows = (sheet.items || []).map((item, index) => [
    String(index + 1),
    item.productName || '-',
    itemSpec(item),
    String(item.quantity || ''),
    item.reason || '-'
  ])
  while (rows.length < 12) {
    rows.push([String(rows.length + 1), '', '', '', ''])
  }
  autoTable(doc, {
    startY: cursorY,
    head: [['序号', '物品名称', '规格/型号', '数量', '备注']],
    body: rows,
    margin: { left: marginLeft, right: marginLeft },
    styles: { fontSize: 9, cellPadding: 5, lineColor: [180, 180, 180], lineWidth: 0.5 },
    headStyles: { fillColor: [245, 245, 245], textColor: [30, 30, 30], fontStyle: 'bold' }
  })
  const tableFinalY = (doc as any).lastAutoTable?.finalY || cursorY + 260
  const signY = tableFinalY + 32
  doc.text('领用人签字：__________________', marginLeft, signY)
  doc.text(`经办人：${sheet.operatorName || '__________________'}`, marginLeft + 190, signY)
  doc.text('仓库管理员：__________________', marginLeft + 370, signY)
  doc.save(`领用出库单_${sheet.outboundNo || id}.pdf`)
  message.success('PDF 已导出')
}

function buildSheetHtml(sheet: InventoryOutboundSheet) {
  const sourceRows = sheet.items || []
  const minRows = 12
  const rows: Array<{ index: number; name: string; spec: string; quantity: string; remark: string }> = sourceRows.map((item, idx) => ({
    index: idx + 1,
    name: item.productName || '-',
    spec: itemSpec(item),
    quantity: String(item.quantity ?? ''),
    remark: item.reason || '-'
  }))
  while (rows.length < minRows) {
    rows.push({ index: rows.length + 1, name: '', spec: '', quantity: '', remark: '' })
  }
  const itemRows = rows
    .map((item) => `
      <tr>
        <td>${item.index}</td>
        <td>${safeHtml(item.name)}</td>
        <td>${safeHtml(item.spec)}</td>
        <td>${safeHtml(item.quantity)}</td>
        <td>${safeHtml(item.remark)}</td>
      </tr>
    `)
    .join('')
  return `
    <html>
      <head>
        <title>领用单-${safeHtml(sheet.outboundNo)}</title>
        <style>
          body { font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif; padding: 18px; color: #222; }
          h2 { margin: 0 0 12px 0; text-align: center; }
          .meta { margin-bottom: 10px; line-height: 1.8; }
          table { width: 100%; border-collapse: collapse; margin-top: 10px; }
          th, td { border: 1px solid #ccc; padding: 8px; text-align: left; font-size: 13px; }
          th { background: #f5f5f5; }
          .sign { margin-top: 28px; display: flex; justify-content: space-between; }
        </style>
      </head>
      <body>
        <h2>领用出库单</h2>
        <div class="meta">
          领用人：${safeHtml(sheet.receiverName)}<br/>
          老人院内ID：${safeHtml(sheet.elderId || '-')}<br/>
          合同号：${safeHtml(sheet.contractNo || '-')}<br/>
          出库单号：${safeHtml(sheet.outboundNo)}<br/>
          申请部门：${safeHtml(sheet.applyDept || '-')}<br/>
          经办人：${safeHtml(sheet.operatorName || '-')}<br/>
          日期：${safeHtml((sheet.createTime || '').slice(0, 10) || '-')}<br/>
          备注：${safeHtml(sheet.remark || '-')}
        </div>
        <div style="margin: 10px 0 0 0; font-weight: 600;">领用物品明细：</div>
        <table>
          <thead>
            <tr><th>序号</th><th>物品名称</th><th>规格/型号</th><th>数量</th><th>备注</th></tr>
          </thead>
          <tbody>${itemRows}</tbody>
        </table>
        <div class="sign">
          <div>领用人签字：______________</div>
          <div>经办人：${safeHtml(sheet.operatorName || '______________')}</div>
          <div>仓库管理员：______________</div>
        </div>
      </body>
    </html>
  `
}

function productSpecById(productId?: number | string) {
  if (!productId) return '-'
  const product = productById.value.get(Number(productId))
  if (!product) return '-'
  return product.unit || product.productCode || '-'
}

function itemSpec(item: { unit?: string; productCode?: string }) {
  return item.unit || item.productCode || '-'
}

function safeHtml(value: unknown) {
  const text = value === null || value === undefined ? '' : String(value)
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
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
  if (outTypeRaw === 'SALE' || outTypeRaw === 'CONSUME') query.outType = outTypeRaw
  if (productIdRaw !== undefined) {
    const parsed = Number(productIdRaw)
    query.productId = Number.isNaN(parsed) ? undefined : parsed
  }
  if (typeof domainRaw === 'string' && domainRaw) query.businessDomain = domainRaw
  if (typeof itemTypeRaw === 'string' && itemTypeRaw) query.itemType = itemTypeRaw
}

onMounted(async () => {
  applyRouteFilters()
  await Promise.all([fetchProducts(), searchElders(''), searchDepartments('')])
  await Promise.all([fetchData(), fetchSheetData()])
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
