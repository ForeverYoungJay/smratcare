<template>
  <PageContainer title="盘点调整记录" subTitle="盘盈盘亏历史记录">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
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
        <a-form-item label="仓库">
          <a-select v-model:value="query.warehouseId" :options="warehouseOptions" allow-clear style="width: 180px" />
        </a-form-item>
        <a-form-item label="分类">
          <a-select v-model:value="query.category" :options="categoryOptions" allow-clear style="width: 160px" />
        </a-form-item>
        <a-form-item label="盘点类型">
          <a-select v-model:value="query.inventoryType" allow-clear style="width: 180px">
            <a-select-option v-for="option in inventoryTypeOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </a-select-option>
          </a-select>
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
          <a-button type="primary" @click="openCreateModal">新增盘点记录</a-button>
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
        <vxe-column field="inventoryType" title="盘点类型" width="160">
          <template #default="{ row }">
            {{ inventoryTypeLabel(row.inventoryType) }}
          </template>
        </vxe-column>
        <vxe-column field="adjustType" title="类型" width="120">
          <template #default="{ row }">
            <a-tag :color="row.adjustType === 'GAIN' ? 'green' : 'volcano'">
              {{ row.adjustType === 'GAIN' ? '盘盈' : '盘亏' }}
            </a-tag>
          </template>
        </vxe-column>
        <vxe-column field="adjustQty" title="数量" width="120" />
        <vxe-column field="reason" title="原因" min-width="220">
          <template #default="{ row }">
            {{ adjustmentReasonText(row.reason) }}
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

    <a-modal
      v-model:open="createOpen"
      title="新增盘点记录"
      :confirm-loading="saving"
      width="780px"
      @ok="submitCreate"
    >
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="商品" required>
              <a-select
                v-model:value="createForm.productId"
                :options="productOptions"
                show-search
                option-filter-prop="label"
                placeholder="请选择商品"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="仓库">
              <a-select v-model:value="createForm.warehouseId" :options="warehouseOptions" allow-clear placeholder="请选择仓库" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="盘点类型" required>
              <a-select v-model:value="createForm.inventoryType" :options="inventoryTypeOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="调整类型" required>
              <a-select v-model:value="createForm.adjustType" :options="adjustTypeOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="调整数量" required>
              <a-input-number v-model:value="createForm.adjustQty" :min="1" :precision="0" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="批次号">
              <a-input v-model:value="createForm.batchNo" maxlength="64" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="摆放位置">
              <a-input v-model:value="createForm.warehouseLocation" maxlength="64" placeholder="如：库房A区/办公室" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="效期">
              <a-space>
                <a-switch v-model:checked="createForm.longTermValid" checked-children="长期有效" un-checked-children="填写效期" />
                <a-date-picker
                  v-if="!createForm.longTermValid"
                  v-model:value="createForm.expireDate"
                  value-format="YYYY-MM-DD"
                  placeholder="请选择过期时间"
                />
              </a-space>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="盘点周期">
              <a-select v-model:value="createForm.periodType" :options="periodTypeOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="周期任务标题">
              <a-input v-model:value="createForm.periodTitle" maxlength="120" placeholder="例如：2026年3-4月全院药品盘点" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="24">
            <a-form-item label="盘亏处理意见（亏盘/临期盘点必填）">
              <a-textarea v-model:value="createForm.handlingOpinion" :rows="2" maxlength="255" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="24">
            <a-form-item label="盘点图片">
              <a-upload
                :show-upload-list="true"
                list-type="text"
                :before-upload="beforeUploadFile"
                :max-count="3"
              >
                <a-button>上传图片</a-button>
              </a-upload>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item>
              <a-checkbox v-model:checked="createForm.expiryCheckAsLoss">
                临期/效期盘点按盘亏处理
              </a-checkbox>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="备注">
              <a-input v-model:value="createForm.remark" maxlength="255" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

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
          <template v-if="column.key === 'inventoryType'">
            {{ inventoryTypeLabel(record.inventoryType) }}
          </template>
          <template v-if="column.key === 'diffQty'">
            <a-tag :color="record.diffQty >= 0 ? 'green' : 'volcano'">{{ record.diffQty }}</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { adjustInventory, getInventoryAdjustmentDiffReport, getInventoryAdjustmentPage } from '../../api/materialCenter'
import { getWarehousePage } from '../../api/materialCenter'
import { getProductPage } from '../../api/store'
import { createOaTask, uploadOaFile } from '../../api/oa'
import { useUserStore } from '../../stores/user'
import type { Id, InventoryAdjustmentDiffItem, InventoryAdjustmentItem, MaterialWarehouseItem, PageResult, ProductItem } from '../../types'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const createOpen = ref(false)
const rows = ref<InventoryAdjustmentItem[]>([])
const total = ref(0)
const products = ref<ProductItem[]>([])
const loadingReport = ref(false)
const reportRows = ref<Array<InventoryAdjustmentDiffItem & { key: string }>>([])
const warehouseOptions = ref<Array<{ label: string; value: Id }>>([])
const categoryOptions = ref<Array<{ label: string; value: string }>>([])
const uploadedPhotoUrls = ref<string[]>([])
const productOptions = computed(() =>
  products.value.map((p) => ({
    label: `${p.productName} (ID:${p.idStr || p.id})`,
    value: p.id
  }))
)

const inventoryTypeOptions = [
  { label: '药品盘点', value: 'MEDICINE' },
  { label: '耗材盘点', value: 'CONSUMABLE' },
  { label: '食品与食材盘点', value: 'FOOD' },
  { label: '清洁与消杀用品盘点', value: 'CLEANING' },
  { label: '办公与生活物资盘点', value: 'OFFICE_LIFE' },
  { label: '固定资产盘点', value: 'FIXED_ASSET' },
  { label: '寄存物盘点', value: 'DEPOSIT' },
  { label: '老人个人物品盘点', value: 'ELDER_PERSONAL' },
  { label: '异常盘点', value: 'ABNORMAL' },
  { label: '中心不动产盘点', value: 'ASSET' },
  { label: '物资盘点', value: 'MATERIAL' }
]

const adjustTypeOptions = [
  { label: '盘盈', value: 'GAIN' },
  { label: '盘亏', value: 'LOSS' }
]

const periodTypeOptions = [
  { label: '不创建周期任务', value: 'NONE' },
  { label: '月末盘点', value: 'MONTH_END' },
  { label: '季度盘点', value: 'QUARTER_END' },
  { label: '年终盘点', value: 'YEAR_END' }
]

const query = reactive({
  productId: undefined as Id | undefined,
  warehouseId: undefined as Id | undefined,
  category: undefined as string | undefined,
  inventoryType: undefined as string | undefined,
  adjustType: undefined as 'GAIN' | 'LOSS' | undefined,
  range: undefined as any,
  pageNo: 1,
  pageSize: 10
})

const createForm = reactive({
  productId: undefined as Id | undefined,
  warehouseId: undefined as Id | undefined,
  inventoryType: 'MEDICINE',
  adjustType: 'GAIN' as 'GAIN' | 'LOSS',
  adjustQty: 1,
  batchNo: '',
  warehouseLocation: '',
  longTermValid: false,
  expireDate: undefined as string | undefined,
  periodType: 'NONE' as 'NONE' | 'MONTH_END' | 'QUARTER_END' | 'YEAR_END',
  periodTitle: '',
  handlingOpinion: '',
  expiryCheckAsLoss: false,
  remark: ''
})

const reportColumns = [
  { title: '商品ID', dataIndex: 'productId', key: 'productId', width: 100 },
  { title: '商品名称', dataIndex: 'productName', key: 'productName' },
  { title: '盘点类型', dataIndex: 'inventoryType', key: 'inventoryType', width: 140 },
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
      inventoryType: query.inventoryType,
      adjustType: query.adjustType,
      dateFrom: query.range?.[0]?.format?.('YYYY-MM-DD'),
      dateTo: query.range?.[1]?.format?.('YYYY-MM-DD')
    })
    rows.value = res.list
    total.value = res.total
  } catch (error: any) {
    message.error(error?.message || '加载盘点记录失败')
    rows.value = []
    total.value = 0
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
      inventoryType: query.inventoryType,
      dateFrom: query.range?.[0]?.format?.('YYYY-MM-DD'),
      dateTo: query.range?.[1]?.format?.('YYYY-MM-DD')
    })
    reportRows.value = (res || []).map((it) => ({
      ...it,
      key: `${it.productId || ''}-${it.warehouseId || ''}`
    }))
  } catch (error: any) {
    message.error(error?.message || '加载盘点差异报表失败')
    reportRows.value = []
  } finally {
    loadingReport.value = false
  }
}

function reset() {
  query.productId = undefined
  query.warehouseId = undefined
  query.category = undefined
  query.inventoryType = undefined
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
      批次ID: r.batchId || '',
      盘点类型: inventoryTypeLabel(r.inventoryType),
      类型: r.adjustType === 'GAIN' ? '盘盈' : '盘亏',
      数量: r.adjustQty,
      批次号: parseAdjustmentReason(r.reason).batchNo,
      摆放位置: parseAdjustmentReason(r.reason).warehouseLocation,
      过期时间: parseAdjustmentReason(r.reason).expireDate,
      长期有效: parseAdjustmentReason(r.reason).longTermValid ? '是' : '否',
      临期盘亏: parseAdjustmentReason(r.reason).expiryCheckAsLoss ? '是' : '否',
      处理意见: parseAdjustmentReason(r.reason).handlingOpinion,
      图片: parseAdjustmentReason(r.reason).photoUrls?.join(' | ') || '',
      原因: parseAdjustmentReason(r.reason).reasonText || r.reason || ''
    })),
    '盘点调整记录'
  )
}

function exportDiffCsvData() {
  exportCsv(
    reportRows.value.map((r) => ({
      商品ID: r.productId,
      商品名称: r.productName,
      盘点类型: inventoryTypeLabel(r.inventoryType),
      分类: r.category,
      仓库: r.warehouseName,
      盘盈: r.gainQty,
      盘亏: r.lossQty,
      净差异: r.diffQty
    })),
    '盘点差异报表'
  )
}

function inventoryTypeLabel(type?: string) {
  return inventoryTypeOptions.find((item) => item.value === type)?.label || type || '-'
}

function parseAdjustmentReason(raw?: string) {
  if (!raw) return {} as any
  try {
    const parsed = JSON.parse(raw)
    return typeof parsed === 'object' && parsed ? parsed : ({} as any)
  } catch {
    return { reasonText: raw } as any
  }
}

function adjustmentReasonText(raw?: string) {
  const parsed = parseAdjustmentReason(raw)
  const parts = [
    parsed.reasonText,
    parsed.handlingOpinion ? `处理意见：${parsed.handlingOpinion}` : undefined,
    parsed.batchNo ? `批次：${parsed.batchNo}` : undefined,
    parsed.warehouseLocation ? `位置：${parsed.warehouseLocation}` : undefined
  ].filter(Boolean)
  return parts.join('；') || '-'
}

function resetCreateForm() {
  createForm.productId = undefined
  createForm.warehouseId = undefined
  createForm.inventoryType = 'MEDICINE'
  createForm.adjustType = 'GAIN'
  createForm.adjustQty = 1
  createForm.batchNo = ''
  createForm.warehouseLocation = ''
  createForm.longTermValid = false
  createForm.expireDate = undefined
  createForm.periodType = 'NONE'
  createForm.periodTitle = ''
  createForm.handlingOpinion = ''
  createForm.expiryCheckAsLoss = false
  createForm.remark = ''
  uploadedPhotoUrls.value = []
}

function openCreateModal() {
  resetCreateForm()
  createOpen.value = true
}

async function beforeUploadFile(file: File) {
  try {
    const res = await uploadOaFile(file, 'inventory-adjustment')
    if (res.fileUrl) {
      uploadedPhotoUrls.value = [...uploadedPhotoUrls.value, res.fileUrl]
      message.success(`已上传：${file.name}`)
    }
  } catch (error: any) {
    message.error(error?.message || `上传失败：${file.name}`)
  }
  return false
}

function resolvePeriodicSchedule(periodType: 'MONTH_END' | 'QUARTER_END' | 'YEAR_END') {
  const now = dayjs()
  if (periodType === 'MONTH_END') {
    const dueDate = now.endOf('month').startOf('day')
    return {
      dueDate,
      titleRange: `${dueDate.format('YYYY年M月')}月末`,
      periodDesc: `${dueDate.format('YYYY-MM-DD')} 月末盘点`
    }
  }
  if (periodType === 'QUARTER_END') {
    const quarter = Math.ceil((now.month() + 1) / 3)
    const quarterEndMonth = quarter * 3
    const dueDate = dayjs(`${now.year()}-${String(quarterEndMonth).padStart(2, '0')}-01`).endOf('month').startOf('day')
    return {
      dueDate,
      titleRange: `${now.year()}年Q${quarter}季度末`,
      periodDesc: `${dueDate.format('YYYY-MM-DD')} 季度末盘点`
    }
  }
  const dueDate = dayjs(`${now.year()}-12-31`).startOf('day')
  return {
    dueDate,
    titleRange: `${now.year()}年年末`,
    periodDesc: `${dueDate.format('YYYY-MM-DD')} 年终盘点`
  }
}

async function createPeriodicTaskIfNeeded(inventoryTypeLabelText: string) {
  if (createForm.periodType === 'NONE') {
    return null
  }
  const periodType = createForm.periodType as 'MONTH_END' | 'QUARTER_END' | 'YEAR_END'
  const schedule = resolvePeriodicSchedule(periodType)
  const startTime = schedule.dueDate
  const endTime = schedule.dueDate.endOf('day')
  const title = createForm.periodTitle.trim()
    || `${schedule.titleRange}全院${inventoryTypeLabelText}`
  const productName = products.value.find((item) => String(item.id) === String(createForm.productId))?.productName || '-'
  const trackingNo = `INV-${dayjs().format('YYYYMMDDHHmmss')}-${Math.floor(Math.random() * 900 + 100)}`
  return createOaTask({
    title,
    description: [
      '由盘点记录自动生成的周期盘点任务',
      `追踪号：${trackingNo}`,
      `周期类型：${schedule.periodDesc}`,
      `盘点类型：${inventoryTypeLabelText}`,
      `商品：${productName}`,
      `来源页面：库存盘点调整`
    ].join('\n'),
    startTime: startTime.format('YYYY-MM-DD HH:mm:ss'),
    endTime: endTime.format('YYYY-MM-DD HH:mm:ss'),
    calendarType: 'COLLAB',
    priority: 'HIGH',
    planCategory: 'INVENTORY_CHECK',
    urgency: 'NORMAL',
    assigneeName: userStore.staffInfo?.realName || userStore.staffInfo?.username || undefined
  })
}

async function submitCreate() {
  if (!createForm.productId) {
    message.warning('请选择商品')
    return
  }
  if (!createForm.inventoryType) {
    message.warning('请选择盘点类型')
    return
  }
  if (!createForm.adjustQty || createForm.adjustQty <= 0) {
    message.warning('调整数量必须大于0')
    return
  }
  const forceLoss = createForm.expiryCheckAsLoss
  const adjustType = forceLoss ? 'LOSS' : createForm.adjustType
  if (adjustType === 'LOSS' && !createForm.handlingOpinion.trim()) {
    message.warning('盘亏或临期盘亏需要填写处理意见')
    return
  }
  const reasonPayload = {
    reasonText: createForm.remark.trim() || undefined,
    batchNo: createForm.batchNo.trim() || undefined,
    warehouseLocation: createForm.warehouseLocation.trim() || undefined,
    expireDate: createForm.longTermValid ? undefined : createForm.expireDate,
    longTermValid: createForm.longTermValid,
    handlingOpinion: createForm.handlingOpinion.trim() || undefined,
    expiryCheckAsLoss: createForm.expiryCheckAsLoss,
    periodType: createForm.periodType,
    periodTitle: createForm.periodTitle.trim() || undefined,
    periodAutoTaskEnabled: createForm.periodType !== 'NONE',
    photoUrls: uploadedPhotoUrls.value
  }
  saving.value = true
  try {
    await adjustInventory({
      productId: createForm.productId,
      warehouseId: createForm.warehouseId,
      inventoryType: createForm.inventoryType,
      adjustType,
      adjustQty: createForm.adjustQty,
      reason: JSON.stringify(reasonPayload)
    })
    let periodicTask: any = null
    if (createForm.periodType !== 'NONE') {
      try {
        periodicTask = await createPeriodicTaskIfNeeded(inventoryTypeLabel(createForm.inventoryType))
      } catch (error: any) {
        message.warning(error?.message || '盘点记录已创建，但周期任务创建失败，请手动在协同日历补建')
      }
    }
    createOpen.value = false
    message.success(periodicTask?.id ? `盘点记录已创建，已生成协同日历任务 #${periodicTask.id}` : '盘点记录已创建')
    await fetchData()
    await fetchDiffReport()
  } catch (error: any) {
    message.error(error?.message || '新增盘点记录失败')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const [warehouseRes, productRes] = await Promise.all([
      getWarehousePage({ pageNo: 1, pageSize: 500 }),
      getProductPage({ pageNo: 1, pageSize: 500 })
    ])
    warehouseOptions.value = (warehouseRes?.list || [])
      .map((it: MaterialWarehouseItem) => ({ label: it.warehouseName || `仓库${it.id}`, value: it.id }))
    products.value = productRes?.list || []
    const categorySet = new Set<string>()
    for (const row of products.value) {
      if (row.category) categorySet.add(row.category)
    }
    categoryOptions.value = Array.from(categorySet).map((it) => ({ label: it, value: it }))
  } catch (error: any) {
    warehouseOptions.value = []
    products.value = []
    categoryOptions.value = []
    message.error(error?.message || '盘点基础数据加载失败，请稍后刷新重试')
  } finally {
    await fetchData()
    await fetchDiffReport()
  }
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
