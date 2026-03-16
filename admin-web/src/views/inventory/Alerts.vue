<template>
  <PageContainer title="库存预警" subTitle="低库存与临期预警中心">
    <a-card class="card-elevated" :bordered="false">
      <a-space>
        <a-button @click="exportSuggestions">导出采购建议</a-button>
        <a-button @click="exportPhotoIndex" :disabled="photoAttachments.length === 0">导出附件清单</a-button>
        <a-button danger @click="clearPhotos" :disabled="photoAttachments.length === 0">清空附件</a-button>
        <a-button type="primary" @click="toBatchPurchase" :disabled="!lowRowsFiltered.length">低库存批量建采购单</a-button>
        <a-upload :show-upload-list="false" :before-upload="beforeUploadItemPhoto">
          <a-button :loading="uploadingItem">上传单品盘点图</a-button>
        </a-upload>
        <a-upload :show-upload-list="false" :before-upload="beforeUploadTotalPhoto">
          <a-button :loading="uploadingTotal">上传合计数量图</a-button>
        </a-upload>
        <a-button @click="fetchAll">刷新</a-button>
      </a-space>
      <a-row :gutter="12" style="margin-top: 12px">
        <a-col :span="6"><a-statistic title="固定资产预警" :value="summaryStats.assetCount" /></a-col>
        <a-col :span="6"><a-statistic title="耗材预警" :value="summaryStats.consumableCount" /></a-col>
        <a-col :span="6"><a-statistic title="食材预警" :value="summaryStats.foodCount" /></a-col>
        <a-col :span="6"><a-statistic title="服务预警" :value="summaryStats.serviceCount" /></a-col>
      </a-row>
      <a-form layout="inline" class="search-form" style="margin-top: 12px">
        <a-form-item label="业务域">
          <a-select v-model:value="filters.businessDomain" allow-clear style="width: 140px" :options="businessDomainOptions" />
        </a-form-item>
        <a-form-item label="物资类型">
          <a-select v-model:value="filters.itemType" allow-clear style="width: 140px" :options="itemTypeOptions" />
        </a-form-item>
      </a-form>
      <div v-if="itemPhotoAttachments.length || totalPhotoAttachments.length" class="attach-list">
        <div class="attach-title">单品盘点图片（{{ itemPhotoAttachments.length }}）</div>
        <div v-for="(item, idx) in itemPhotoAttachments" :key="`item-${item.url}-${idx}`" class="attach-item">
          <div class="attach-item-main">
            <a :href="item.url" target="_blank" rel="noopener noreferrer">{{ item.name }}</a>
            <span class="attach-time">{{ displayUploadedAt(item.uploadedAt) }}</span>
          </div>
          <a-button type="link" danger size="small" @click="removePhoto(item)">删除</a-button>
        </div>
        <div class="attach-title" style="margin-top: 8px">合计数量图片（{{ totalPhotoAttachments.length }}）</div>
        <div v-for="(item, idx) in totalPhotoAttachments" :key="`total-${item.url}-${idx}`" class="attach-item">
          <div class="attach-item-main">
            <a :href="item.url" target="_blank" rel="noopener noreferrer">{{ item.name }}</a>
            <span class="attach-time">{{ displayUploadedAt(item.uploadedAt) }}</span>
          </div>
          <a-button type="link" danger size="small" @click="removePhoto(item)">删除</a-button>
        </div>
      </div>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-tabs v-model:activeKey="activeKey">
        <a-tab-pane key="low" tab="低库存预警">
          <a-table
            :columns="lowColumns"
            :data-source="lowRowsFiltered"
            row-key="productId"
            :loading="loading"
            :pagination="false"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'actions'">
                <a-space>
                  <a @click="toStock(record)">查看库存</a>
                  <a @click="toPurchase(record)">发起采购</a>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
        <a-tab-pane key="expiry" tab="临期预警">
          <a-table
            :columns="expiryColumns"
            :data-source="expiryRowsFiltered"
            row-key="batchId"
            :loading="loading"
            :pagination="false"
          />
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { getInventoryAlerts, getInventoryExpiryAlerts } from '../../api/materialCenter'
import { getProductPage } from '../../api/store'
import { uploadOaFile } from '../../api/oa'
import { useUserStore } from '../../stores/user'
import type { Id, InventoryAlertItem, InventoryExpiryAlertItem, PageResult, ProductItem } from '../../types'

const activeKey = ref('low')
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const uploadingItem = ref(false)
const uploadingTotal = ref(false)
const lowRows = ref<InventoryAlertItem[]>([])
const expiryRows = ref<InventoryExpiryAlertItem[]>([])
const products = ref<ProductItem[]>([])
const expiryDays = ref<number | undefined>(undefined)
type AlertPhotoAttachment = { name: string; url: string; photoType: 'ITEM' | 'TOTAL'; uploadedAt?: string }
const photoAttachments = ref<AlertPhotoAttachment[]>([])
const filters = reactive({
  businessDomain: undefined as string | undefined,
  itemType: undefined as string | undefined
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
  const map = new Map<Id, ProductItem>()
  for (const product of products.value) {
    map.set(String(product.idStr || product.id), product)
  }
  return map
})
const lowRowsFiltered = computed(() =>
  lowRows.value.filter((row) => matchProductFilters(row.productId))
)
const expiryRowsFiltered = computed(() =>
  expiryRows.value.filter((row) => matchProductFilters(row.productId))
)
const summaryStats = computed(() => {
  const stats = { assetCount: 0, consumableCount: 0, foodCount: 0, serviceCount: 0 }
  for (const row of lowRowsFiltered.value) {
    const itemType = productById.value.get(String(row.productId))?.itemType || 'CONSUMABLE'
    if (itemType === 'ASSET') stats.assetCount += 1
    else if (itemType === 'FOOD') stats.foodCount += 1
    else if (itemType === 'SERVICE') stats.serviceCount += 1
    else stats.consumableCount += 1
  }
  return stats
})
const itemPhotoAttachments = computed(() => photoAttachments.value.filter((item) => item.photoType === 'ITEM'))
const totalPhotoAttachments = computed(() => photoAttachments.value.filter((item) => item.photoType === 'TOTAL'))

const lowColumns = [
  { title: '商品名称', dataIndex: 'productName' },
  { title: '商品ID', dataIndex: 'productId', width: 120 },
  { title: '安全库存', dataIndex: 'safetyStock', width: 120 },
  { title: '当前库存', dataIndex: 'currentStock', width: 120 },
  {
    title: '建议采购数量',
    key: 'suggestQty',
    width: 140,
    customRender: ({ record }: { record: InventoryAlertItem }) => {
      const current = Number(record.currentStock || 0)
      const safety = Number(record.safetyStock || 0)
      return Math.max(safety - current, 0)
    }
  },
  { title: '操作', key: 'actions', width: 180 }
]

const expiryColumns = [
  { title: '商品名称', dataIndex: 'productName' },
  { title: '批次号', dataIndex: 'batchNo', width: 160 },
  { title: '库存数量', dataIndex: 'quantity', width: 120 },
  { title: '有效期', dataIndex: 'expireDate', width: 140 },
  { title: '距离到期(天)', dataIndex: 'daysToExpire', width: 140 }
]

async function fetchAll() {
  loading.value = true
  try {
    const [low, expiry, productRes] = await Promise.all([
      getInventoryAlerts(),
      getInventoryExpiryAlerts(expiryDays.value),
      getProductPage({ pageNo: 1, pageSize: 500 }) as Promise<PageResult<ProductItem>>
    ])
    lowRows.value = low
    expiryRows.value = expiry
    products.value = productRes.list || []
  } finally {
    loading.value = false
  }
}

function exportSuggestions() {
  const latestItemPhoto = itemPhotoAttachments.value[0]
  const latestTotalPhoto = totalPhotoAttachments.value[0]
  const exportTime = dayjs().format('YYYY-MM-DD HH:mm:ss')
  const exportBy = userStore.staffInfo?.realName || userStore.staffInfo?.username || '未知用户'
  const exportDomain = domainLabel(filters.businessDomain)
  const exportItemType = itemTypeLabel(filters.itemType)
  const data = [
    ...lowRowsFiltered.value.map((r) => ({
      类型: '低库存',
      商品名称: r.productName,
      商品ID: r.productId,
      业务域: domainLabel(productById.value.get(String(r.productId))?.businessDomain),
      物资类型: itemTypeLabel(productById.value.get(String(r.productId))?.itemType),
      安全库存: r.safetyStock,
      当前库存: r.currentStock,
      建议采购数量: Math.max(Number(r.safetyStock || 0) - Number(r.currentStock || 0), 0),
      单品盘点图数: itemPhotoAttachments.value.length,
      合计数量图数: totalPhotoAttachments.value.length,
      最新单品图: latestItemPhoto?.url || '',
      最新合计图: latestTotalPhoto?.url || '',
      导出业务域: exportDomain,
      导出物资类型: exportItemType,
      导出人: exportBy,
      导出时间: exportTime
    })),
    ...expiryRowsFiltered.value.map((r) => ({
      类型: '临期',
      商品名称: r.productName,
      批次号: r.batchNo,
      库存数量: r.quantity,
      有效期: r.expireDate,
      距离到期: r.daysToExpire,
      单品盘点图数: itemPhotoAttachments.value.length,
      合计数量图数: totalPhotoAttachments.value.length,
      最新单品图: latestItemPhoto?.url || '',
      最新合计图: latestTotalPhoto?.url || '',
      导出业务域: exportDomain,
      导出物资类型: exportItemType,
      导出人: exportBy,
      导出时间: exportTime
    }))
  ]
  if (!data.length) {
    message.warning('当前筛选条件下暂无可导出的采购建议')
    return
  }
  exportCsv(data, '库存预警采购建议')
  message.success(`已导出 ${data.length} 条采购建议`)
}

function exportPhotoIndex() {
  if (!photoAttachments.value.length) {
    message.warning('暂无可导出的图片附件')
    return
  }
  exportCsv(
    photoAttachments.value.map((item, index) => ({
      序号: index + 1,
      类型: item.photoType === 'TOTAL' ? '合计数量图' : '单品盘点图',
      文件名: item.name,
      链接: item.url,
      上传时间: item.uploadedAt ? dayjs(item.uploadedAt).format('YYYY-MM-DD HH:mm:ss') : ''
    })),
    '库存预警-图片附件清单'
  )
  message.success(`已导出 ${photoAttachments.value.length} 条附件记录`)
}

function loadAttachments() {
  const attachmentStorageKey = resolveAttachmentStorageKey()
  try {
    const cached = JSON.parse(localStorage.getItem(attachmentStorageKey) || '[]')
    photoAttachments.value = Array.isArray(cached)
      ? cached.filter((item) => item?.url).map((item) => ({
          name: item.name,
          url: item.url,
          photoType: item.photoType === 'TOTAL' ? 'TOTAL' : 'ITEM',
          uploadedAt: item.uploadedAt
        }))
      : []
  } catch {
    photoAttachments.value = []
  }
}

function saveAttachments() {
  const attachmentStorageKey = resolveAttachmentStorageKey()
  localStorage.setItem(attachmentStorageKey, JSON.stringify(photoAttachments.value))
}

function resolveAttachmentStorageKey() {
  const orgId = String(userStore.staffInfo?.orgId || 'unknown-org')
  const staffId = String(userStore.staffInfo?.id || 'unknown-staff')
  return `inventory-alert-photo-attachments:${orgId}:${staffId}`
}

async function uploadPhotoByType(file: File, photoType: 'ITEM' | 'TOTAL') {
  const targetLoading = photoType === 'ITEM' ? uploadingItem : uploadingTotal
  targetLoading.value = true
  try {
    const sameTypeCount = photoAttachments.value.filter((item) => item.photoType === photoType).length
    if (sameTypeCount >= 50) {
      message.warning(`${photoType === 'ITEM' ? '单品盘点图' : '合计数量图'}最多保留 50 张，请先删除旧附件`)
      return false
    }
    const uploaded = await uploadOaFile(file, photoType === 'ITEM' ? 'inventory-alert-photo-item' : 'inventory-alert-photo-total')
    const url = uploaded?.fileUrl || ''
    if (!url) {
      message.error('上传失败：未返回文件地址')
      return false
    }
    photoAttachments.value.unshift({
      name: uploaded.originalFileName || uploaded.fileName || file.name,
      url,
      photoType,
      uploadedAt: new Date().toISOString()
    })
    saveAttachments()
    message.success(photoType === 'ITEM' ? '单品盘点图片已上传' : '合计数量图片已上传')
  } catch (error: any) {
    message.error(error?.message || '上传失败')
  } finally {
    targetLoading.value = false
  }
  return false
}

async function beforeUploadItemPhoto(file: File) {
  return uploadPhotoByType(file, 'ITEM')
}

async function beforeUploadTotalPhoto(file: File) {
  return uploadPhotoByType(file, 'TOTAL')
}

function removePhoto(target: AlertPhotoAttachment) {
  photoAttachments.value = photoAttachments.value.filter((item) => item.url !== target.url || item.photoType !== target.photoType)
  saveAttachments()
}

function clearPhotos() {
  photoAttachments.value = []
  saveAttachments()
  message.success('附件已清空')
}

function displayUploadedAt(uploadedAt?: string) {
  if (!uploadedAt) return ''
  return dayjs(uploadedAt).format('YYYY-MM-DD HH:mm:ss')
}

function suggestQty(record: InventoryAlertItem) {
  return Math.max(Number(record.safetyStock || 0) - Number(record.currentStock || 0), 1)
}

function toStock(record: InventoryAlertItem) {
  router.push({
    path: '/logistics/storage/stock-query',
    query: {
      productId: record.productId,
      keyword: record.productName || '',
      lowStockOnly: '1',
      source: 'inventory_alert'
    }
  })
}

function toPurchase(record: InventoryAlertItem) {
  router.push({
    path: '/logistics/storage/purchase',
    query: {
      autoCreate: '1',
      productId: record.productId,
      quantity: suggestQty(record),
      source: 'inventory_alert',
      sourceRef: record.productName || String(record.productId || '')
    }
  })
}

function toBatchPurchase() {
  if (!lowRowsFiltered.value.length) {
    message.info('当前无低库存数据')
    return
  }
  const validRows = lowRowsFiltered.value.filter((item) => {
    return String(item.productId || '').trim().length > 0
  })
  if (!validRows.length) {
    message.info('低库存记录缺少有效商品ID')
    return
  }
  const productIds = validRows.map((item) => String(item.productId))
  const quantities = validRows.map((item) => suggestQty(item))
  router.push({
    path: '/logistics/storage/purchase',
    query: {
      autoCreate: '1',
      productIds: productIds.join(','),
      quantities: quantities.join(','),
      source: 'inventory_alert_batch',
      sourceRef: `low_count_${validRows.length}`
    }
  })
}

function applyRouteFilters() {
  const query = route.query || {}
  const domainRaw = Array.isArray(query.businessDomain) ? query.businessDomain[0] : query.businessDomain
  const itemTypeRaw = Array.isArray(query.itemType) ? query.itemType[0] : query.itemType
  const focusRaw = Array.isArray(query.focus) ? query.focus[0] : query.focus
  const filterRaw = Array.isArray(query.filter) ? query.filter[0] : query.filter
  const lowStockOnlyRaw = Array.isArray(query.lowStockOnly) ? query.lowStockOnly[0] : query.lowStockOnly
  const expiryDaysRaw = Array.isArray(query.expiryDays) ? query.expiryDays[0] : query.expiryDays
  if (typeof domainRaw === 'string' && domainRaw) {
    filters.businessDomain = domainRaw
  } else {
    filters.businessDomain = undefined
  }
  if (typeof itemTypeRaw === 'string' && itemTypeRaw) {
    filters.itemType = itemTypeRaw
  } else {
    filters.itemType = undefined
  }
  const parsedExpiryDays = Number(expiryDaysRaw)
  expiryDays.value = Number.isFinite(parsedExpiryDays) && parsedExpiryDays > 0 ? parsedExpiryDays : undefined
  const focus = String(focusRaw || filterRaw || '').toLowerCase()
  const lowStockOnly = String(lowStockOnlyRaw || '').toLowerCase() === '1'
    || String(lowStockOnlyRaw || '').toLowerCase() === 'true'
  if (lowStockOnly || focus === 'low' || focus === 'low_stock') {
    activeKey.value = 'low'
  } else if (focus === 'expiry' || focus === 'expiring' || expiryDays.value) {
    activeKey.value = 'expiry'
  } else {
    activeKey.value = 'low'
  }
}

onMounted(() => {
  applyRouteFilters()
  loadAttachments()
  fetchAll()
})

watch(
  () => route.query,
  () => {
    applyRouteFilters()
    fetchAll()
  }
)

watch(
  () => [userStore.staffInfo?.orgId, userStore.staffInfo?.id],
  () => {
    loadAttachments()
  }
)

function matchProductFilters(productId?: Id) {
  const normalizedProductId = String(productId || '').trim()
  if (!normalizedProductId) return true
  const product = productById.value.get(normalizedProductId)
  if (!product) return true
  if (filters.businessDomain && (product.businessDomain || 'BOTH') !== filters.businessDomain) {
    return false
  }
  if (filters.itemType && (product.itemType || '') !== filters.itemType) {
    return false
  }
  return true
}

function domainLabel(value?: string) {
  if (!value) return '全部'
  if (value === 'INTERNAL') return '企业内部'
  if (value === 'MALL') return '商城'
  if (value === 'BOTH') return '双用途'
  return '未设置'
}

function itemTypeLabel(value?: string) {
  if (!value) return '全部'
  if (value === 'ASSET') return '固定资产'
  if (value === 'FOOD') return '食材'
  if (value === 'SERVICE') return '服务'
  if (value === 'CONSUMABLE') return '耗材'
  return '未设置'
}
</script>

<style scoped>
.attach-list {
  margin-top: 10px;
  display: grid;
  gap: 6px;
}
.attach-title {
  font-size: 12px;
  color: #595959;
}
.attach-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 8px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
}
.attach-item-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.attach-time {
  font-size: 12px;
  color: #8c8c8c;
}
</style>
