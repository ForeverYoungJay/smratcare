<template>
  <PageContainer title="电费管理" subTitle="按房间登记电表读数，自动核算用电量与电费，跟踪缴费状态">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query">
        <a-form-item label="账期">
          <a-date-picker v-model:value="query.month" picker="month" :allow-clear="false" style="width: 150px" />
        </a-form-item>
        <a-form-item label="楼栋">
          <a-select
            v-model:value="query.building"
            allow-clear
            placeholder="全部楼栋"
            style="width: 150px"
            :options="buildingOptions"
          />
        </a-form-item>
        <a-form-item label="房间号">
          <a-input v-model:value="query.roomNo" allow-clear placeholder="房间号" style="width: 140px" />
        </a-form-item>
        <a-form-item label="缴费状态">
          <a-select v-model:value="query.payStatus" allow-clear placeholder="全部" style="width: 130px">
            <a-select-option value="UNPAID">未缴</a-select-option>
            <a-select-option value="PAID">已缴</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="runSearch">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button type="primary" ghost @click="openReading()">登记读数</a-button>
            <a-button @click="openPrice">电价设置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-alert
      v-if="!Number(summary?.unitPrice || 0)"
      type="warning"
      show-icon
      style="margin-top: 16px;"
      message="尚未配置电价"
      description="电费按「用电量 × 电价」自动核算，请先在「电价设置」里填写元/度单价，否则无法登记读数。"
    />

    <a-row :gutter="[16, 16]" style="margin-top: 16px;">
      <a-col :xs="24" :md="12" :xl="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="当期电价" :value="Number(summary?.unitPrice || 0)" :precision="4" suffix="元/度" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12" :xl="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="总用电量" :value="Number(summary?.totalUsage || 0)" :precision="2" suffix="度" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12" :xl="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="总电费" :value="Number(summary?.totalFee || 0)" :precision="2" suffix="元" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12" :xl="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic
            title="未缴房间"
            :value="Number(summary?.unpaidRoomCount || 0)"
            :value-style="Number(summary?.unpaidRoomCount || 0) > 0 ? { color: '#cf1322' } : undefined"
            suffix="间"
          />
          <div class="stat-hint">
            未缴金额 {{ formatAmount(summary?.unpaidFee) }} 元 · 未登记 {{ summary?.unrecordedRoomCount || 0 }} 间
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <vxe-table
        border
        stripe
        show-overflow="title"
        :loading="loading"
        :data="rows"
        height="520"
        :row-class-name="rowClassName"
      >
        <vxe-column field="building" title="楼栋" width="110" />
        <vxe-column field="floorNo" title="楼层" width="90" />
        <vxe-column field="roomNo" title="房间" width="110" />
        <vxe-column field="previousReading" title="上期读数" width="110" />
        <vxe-column field="currentReading" title="本期读数" width="110" />
        <vxe-column field="usageAmount" title="用电量(度)" width="120" />
        <vxe-column field="unitPrice" title="电价" width="100" />
        <vxe-column field="feeAmount" title="电费(元)" width="120">
          <template #default="{ row }"><strong>{{ formatAmount(row.feeAmount) }}</strong></template>
        </vxe-column>
        <vxe-column field="shareModeText" title="分摊方式" width="150">
          <template #default="{ row }">
            <span>{{ row.shareModeText }}</span>
            <small v-if="row.shareMode === 'PER_RESIDENT'" class="share-hint">
              {{ row.residentCount }} 人 · 人均 {{ formatAmount(row.perResidentAmount) }}
            </small>
          </template>
        </vxe-column>
        <vxe-column field="payStatusText" title="缴费状态" width="110">
          <template #default="{ row }">
            <a-tag :color="row.payStatus === 'PAID' ? 'green' : 'red'">{{ row.payStatusText }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column field="remark" title="备注" min-width="140" />
        <vxe-column title="操作" width="170" fixed="right">
          <template #default="{ row }">
            <a-space>
              <a-button type="link" size="small" @click="openReading(row)">改读数</a-button>
              <a-button
                type="link"
                size="small"
                :danger="row.payStatus === 'PAID'"
                @click="togglePay(row)"
              >{{ row.payStatus === 'PAID' ? '改为未缴' : '标记已缴' }}</a-button>
            </a-space>
          </template>
        </vxe-column>
      </vxe-table>
      <div class="pager-row">
        <a-pagination
          :current="query.pageNo"
          :page-size="query.pageSize"
          :total="total"
          show-size-changer
          @change="onPageChange"
          @showSizeChange="onPageSizeChange"
        />
      </div>
    </a-card>

    <a-modal
      v-model:open="readingOpen"
      :title="`登记电表读数 · ${dayjs(query.month).format('YYYY年MM月')}`"
      width="600"
      :confirm-loading="readingSaving"
      @ok="submitReading"
    >
      <a-form layout="vertical" :model="readingForm" :rules="readingRules" ref="readingFormRef">
        <a-form-item label="房间" name="roomId">
          <a-select
            v-model:value="readingForm.roomId"
            show-search
            :filter-option="true"
            option-filter-prop="label"
            :options="roomOptions"
            placeholder="选择房间"
            style="width: 100%"
            @change="onRoomChange"
          />
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="上期读数" name="previousReading">
              <a-input-number
                v-model:value="readingForm.previousReading"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
              <span class="form-tip">已自动带出上一账期的本期读数，可修正</span>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="本期读数" name="currentReading">
              <a-input-number
                v-model:value="readingForm.currentReading"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="分摊方式">
          <a-radio-group v-model:value="readingForm.shareMode">
            <a-radio value="ROOM">整间计费</a-radio>
            <a-radio value="PER_RESIDENT">按在住人数均摊</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="readingForm.remark" allow-clear />
        </a-form-item>
      </a-form>

      <div class="calc-summary">
        <div class="calc-summary__row">
          <span>用电量</span><strong>{{ formatAmount(previewUsage) }} 度</strong>
        </div>
        <div class="calc-summary__row">
          <span>电价</span><strong>{{ Number(summary?.unitPrice || 0).toFixed(4) }} 元/度</strong>
        </div>
        <div class="calc-summary__row is-total" :class="{ 'is-error': !!readingError }">
          <span>电费</span><strong>{{ formatAmount(previewFee) }} 元</strong>
        </div>
        <div v-if="readingError" class="calc-summary__error">{{ readingError }}</div>
      </div>
    </a-modal>

    <a-modal
      v-model:open="priceOpen"
      title="电价设置"
      width="620"
      :confirm-loading="priceSaving"
      @ok="submitPrice"
    >
      <a-form layout="vertical" :model="priceForm" :rules="priceRules" ref="priceFormRef">
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="电价（元/度）" name="unitPrice">
              <a-input-number v-model:value="priceForm.unitPrice" :min="0.0001" :precision="4" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="生效日期" name="effectiveFrom">
              <a-date-picker v-model:value="priceForm.effectiveFrom" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注">
          <a-input v-model:value="priceForm.remark" allow-clear placeholder="如：2026 年居民电价调整" />
        </a-form-item>
      </a-form>
      <a-divider style="margin: 8px 0" />
      <div class="price-history-title">历史电价（账期按生效日期就近取值）</div>
      <vxe-table border stripe :data="prices" height="200">
        <vxe-column field="effectiveFrom" title="生效日期" width="140" />
        <vxe-column field="unitPrice" title="电价(元/度)" width="140" />
        <vxe-column field="remark" title="备注" min-width="140" />
      </vxe-table>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { getRoomList } from '../../api/bed'
import {
  getElectricityPreviousReading,
  getElectricityPrices,
  getElectricityReadingPage,
  getElectricitySummary,
  saveElectricityPrice,
  saveElectricityReading,
  updateElectricityPayStatus,
  type ElectricityMonthSummary,
  type ElectricityPriceItem,
  type ElectricityReadingItem
} from '../../api/electricityFee'
import type { Id, PageResult, RoomItem } from '../../types'

const loading = ref(false)
const rows = ref<ElectricityReadingItem[]>([])
const total = ref(0)
const summary = ref<ElectricityMonthSummary | null>(null)
const prices = ref<ElectricityPriceItem[]>([])
const roomList = ref<RoomItem[]>([])

const query = reactive({
  month: dayjs().startOf('month') as any,
  building: undefined as string | undefined,
  roomNo: '',
  payStatus: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const monthText = computed(() => dayjs(query.month).format('YYYY-MM'))
const buildingOptions = computed(() => {
  const set = new Set<string>()
  roomList.value.forEach((item) => {
    const building = String((item as any).building || '').trim()
    if (building) set.add(building)
  })
  return Array.from(set).sort().map((item) => ({ label: item, value: item }))
})
const roomOptions = computed(() =>
  roomList.value.map((item) => ({
    label: `${(item as any).building || '-'} / ${(item as any).floorNo || '-'} / ${item.roomNo}`,
    value: item.id
  }))
)

const readingOpen = ref(false)
const readingSaving = ref(false)
const readingFormRef = ref<FormInstance>()
const readingForm = reactive({
  roomId: undefined as Id | undefined,
  previousReading: 0,
  currentReading: 0,
  shareMode: 'ROOM',
  remark: ''
})
const readingRules: FormRules = {
  roomId: [{ required: true, message: '请选择房间' }],
  currentReading: [{ required: true, message: '请输入本期读数' }]
}

const previewUsage = computed(() =>
  round2(Math.max(0, Number(readingForm.currentReading || 0) - Number(readingForm.previousReading || 0)))
)
const previewFee = computed(() => round2(previewUsage.value * Number(summary.value?.unitPrice || 0)))
const readingError = computed(() => {
  if (Number(readingForm.currentReading || 0) < Number(readingForm.previousReading || 0)) {
    return '本期读数不能小于上期读数，请核对抄表数据'
  }
  if (!Number(summary.value?.unitPrice || 0)) {
    return '尚未配置电价，无法核算电费'
  }
  return ''
})

const priceOpen = ref(false)
const priceSaving = ref(false)
const priceFormRef = ref<FormInstance>()
const priceForm = reactive({
  unitPrice: undefined as number | undefined,
  effectiveFrom: dayjs().startOf('month') as any,
  remark: ''
})
const priceRules: FormRules = {
  unitPrice: [{ required: true, message: '请填写电价' }],
  effectiveFrom: [{ required: true, message: '请选择生效日期' }]
}

function round2(value: number) {
  return Math.round((Number(value) || 0) * 100) / 100
}

function formatAmount(value?: number | string) {
  return Number(value || 0).toFixed(2)
}

function rowClassName({ row }: { row: ElectricityReadingItem }) {
  return row?.payStatus === 'PAID' ? '' : 'is-unpaid-row'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<ElectricityReadingItem> = await getElectricityReadingPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      billMonth: monthText.value,
      building: query.building || undefined,
      roomNo: query.roomNo.trim() || undefined,
      payStatus: query.payStatus || undefined
    })
    rows.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function fetchSummary() {
  summary.value = await getElectricitySummary(monthText.value)
}

function runSearch() {
  query.pageNo = 1
  fetchData()
  fetchSummary()
}

function reset() {
  query.pageNo = 1
  query.building = undefined
  query.roomNo = ''
  query.payStatus = undefined
  runSearch()
}

function onPageChange(page: number, pageSize: number) {
  query.pageNo = page
  query.pageSize = pageSize
  fetchData()
}

function onPageSizeChange(_current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

async function openReading(row?: ElectricityReadingItem) {
  readingForm.roomId = row?.roomId
  readingForm.previousReading = Number(row?.previousReading || 0)
  readingForm.currentReading = Number(row?.currentReading || 0)
  readingForm.shareMode = row?.shareMode || 'ROOM'
  readingForm.remark = row?.remark || ''
  readingOpen.value = true
  if (!row?.roomId) return
  await loadPreviousReading(row.roomId, Number(row.previousReading || 0))
}

async function onRoomChange(roomId: Id) {
  if (!roomId) return
  await loadPreviousReading(roomId)
}

async function loadPreviousReading(roomId: Id, fallback?: number) {
  try {
    const res = await getElectricityPreviousReading({ billMonth: monthText.value, roomId })
    const suggested = Number(res?.previousReading || 0)
    // 已登记过的记录保留自己的上期读数，避免覆盖人工修正
    readingForm.previousReading = fallback && fallback > 0 ? fallback : suggested
  } catch {
    readingForm.previousReading = fallback || 0
  }
}

async function submitReading() {
  if (readingSaving.value) return
  await readingFormRef.value?.validate?.()
  if (readingError.value) {
    message.warning(readingError.value)
    return
  }
  readingSaving.value = true
  try {
    await saveElectricityReading({
      billMonth: monthText.value,
      roomId: readingForm.roomId as Id,
      previousReading: round2(readingForm.previousReading),
      currentReading: round2(readingForm.currentReading),
      shareMode: readingForm.shareMode,
      remark: readingForm.remark.trim() || undefined
    })
    message.success('读数已登记，电费已自动核算')
    readingOpen.value = false
    await Promise.all([fetchData(), fetchSummary()])
  } finally {
    readingSaving.value = false
  }
}

async function togglePay(row: ElectricityReadingItem) {
  const paid = row.payStatus !== 'PAID'
  await updateElectricityPayStatus(row.id, paid)
  message.success(paid ? '已标记为已缴' : '已改回未缴')
  await Promise.all([fetchData(), fetchSummary()])
}

async function openPrice() {
  priceOpen.value = true
  priceForm.unitPrice = Number(summary.value?.unitPrice || 0) || undefined
  priceForm.effectiveFrom = dayjs().startOf('month')
  priceForm.remark = ''
  prices.value = await getElectricityPrices()
}

async function submitPrice() {
  if (priceSaving.value) return
  await priceFormRef.value?.validate?.()
  priceSaving.value = true
  try {
    await saveElectricityPrice({
      unitPrice: Number(priceForm.unitPrice),
      effectiveFrom: dayjs(priceForm.effectiveFrom).format('YYYY-MM-DD'),
      remark: priceForm.remark.trim() || undefined
    })
    message.success('电价已保存')
    priceOpen.value = false
    await fetchSummary()
  } finally {
    priceSaving.value = false
  }
}

watch(() => monthText.value, () => runSearch())

onMounted(async () => {
  try {
    roomList.value = await getRoomList()
  } catch {
    roomList.value = []
  }
  await Promise.all([fetchData(), fetchSummary()])
})
</script>

<style scoped>
.pager-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.stat-hint,
.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-tertiary, #999);
}

.share-hint {
  display: block;
  font-size: 12px;
  color: var(--text-tertiary, #999);
}

.price-history-title {
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--text-secondary, #666);
}

.calc-summary {
  padding: 12px 14px;
  border: 1px solid var(--border-soft, #eee);
  border-radius: 8px;
  background: var(--fill-subtle, rgba(0, 0, 0, 0.02));
}

.calc-summary__row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 3px 0;
  font-size: 13px;
}

.calc-summary__row.is-total {
  margin-top: 6px;
  padding-top: 8px;
  border-top: 1px dashed var(--border-soft, #eee);
  font-size: 15px;
}

.calc-summary__row.is-error strong {
  color: #cf1322;
}

.calc-summary__error {
  margin-top: 6px;
  font-size: 12px;
  color: #cf1322;
}

:deep(.vxe-body--row.is-unpaid-row) td {
  background-color: rgba(207, 19, 34, 0.06) !important;
}
</style>
