<template>
  <PageContainer title="订餐统计" subTitle="查看楼栋点餐、次月采购成本与准点送率">
    <template #meta>
      <a-space wrap size="small">
        <span class="soft-pill">统计周期：{{ rangeLabel }}</span>
        <span class="soft-pill">楼栋数：{{ summary.buildingStats.length }}</span>
        <span class="selection-pill">追踪配送：{{ summary.trackedDeliveryOrders }} 单</span>
        <span class="soft-pill">采购预估：{{ formatCurrency(summary.nextMonthEstimatedCost) }}</span>
      </a-space>
    </template>

    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
    </SearchForm>

    <a-row :gutter="16" class="stats-row">
      <a-col :xs="24" :md="12" :xl="6">
        <a-card>
          <a-statistic title="订单总数" :value="summary.totalOrders" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12" :xl="6">
        <a-card>
          <a-statistic title="订单金额" :value="summary.totalAmount" :precision="2" suffix="元" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12" :xl="6">
        <a-card>
          <a-statistic title="送达率" :value="summary.deliveryRate" :precision="2" suffix="%" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="12" :xl="6">
        <a-card>
          <a-statistic title="准点送率" :value="summary.onTimeDeliveryRate" :precision="2" suffix="%" />
          <div class="stat-meta">已追踪 {{ summary.trackedDeliveryOrders }} 单，准点 {{ summary.onTimeDeliveredOrders }} 单</div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="stats-row">
      <a-col :xs="24" :xl="10">
        <a-card title="各楼栋点餐统计" class="card-elevated" :bordered="false">
          <DataTable rowKey="buildingName" :columns="buildingColumns" :data-source="summary.buildingStats" :loading="loading" :pagination="false" />
        </a-card>
      </a-col>
      <a-col :xs="24" :xl="14">
        <a-card title="餐次分布" class="card-elevated" :bordered="false">
          <DataTable rowKey="mealType" :columns="mealColumns" :data-source="summary.mealTypeStats" :loading="loading" :pagination="false" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false">
      <template #title>次月买菜成本预估</template>
      <template #extra>预计 {{ formatCurrency(summary.nextMonthEstimatedCost) }}</template>
      <DataTable rowKey="dishId" :columns="procurementColumns" :data-source="summary.procurementItems" :loading="loading" :pagination="false" />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getDiningMealTypeLabel } from '../../constants/dining'
import { getDiningStatsSummary } from '../../api/dining'
import type { DiningStatsSummary } from '../../types'

const loading = ref(false)
const query = reactive({ range: undefined as [Dayjs, Dayjs] | undefined })
const summary = reactive<DiningStatsSummary>({
  totalOrders: 0,
  totalAmount: 0,
  deliveredOrders: 0,
  deliveryRate: 0,
  onTimeDeliveredOrders: 0,
  trackedDeliveryOrders: 0,
  onTimeDeliveryRate: 0,
  nextMonthEstimatedCost: 0,
  mealTypeStats: [],
  buildingStats: [],
  procurementItems: []
})
const rangeLabel = computed(() => {
  if (!query.range) return '默认全量区间'
  return `${query.range[0].format('YYYY-MM-DD')} 至 ${query.range[1].format('YYYY-MM-DD')}`
})

const mealColumns = [
  { title: '餐次', dataIndex: 'mealType', key: 'mealType', width: 120, customRender: ({ text }: any) => getDiningMealTypeLabel(text) },
  { title: '订单数', dataIndex: 'orderCount', key: 'orderCount', width: 120 }
]

const buildingColumns = [
  { title: '楼栋', dataIndex: 'buildingName', key: 'buildingName' },
  { title: '点餐次数', dataIndex: 'orderCount', key: 'orderCount', width: 120 },
  { title: '点餐金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 140, customRender: ({ text }: any) => formatCurrency(text) }
]

const procurementColumns = [
  { title: '菜品', dataIndex: 'dishName', key: 'dishName', width: 140 },
  { title: '分类', dataIndex: 'dishCategory', key: 'dishCategory', width: 120 },
  { title: '餐次', dataIndex: 'mealType', key: 'mealType', width: 100, customRender: ({ text }: any) => getDiningMealTypeLabel(text) },
  { title: '排班次数', dataIndex: 'recipeCount', key: 'recipeCount', width: 100 },
  { title: '现行用餐人数', dataIndex: 'currentDiningCount', key: 'currentDiningCount', width: 120 },
  { title: '单次采购量', key: 'purchaseQty', width: 120, customRender: ({ record }: any) => formatQty(record.purchaseQty, record.purchaseUnit) },
  { title: '次月采购量', key: 'totalPlannedQty', width: 120, customRender: ({ record }: any) => formatQty(record.totalPlannedQty, record.purchaseUnit) },
  { title: '采购成本', key: 'estimatedCost', width: 120, customRender: ({ record }: any) => formatCurrency(record.estimatedCost) }
]

async function fetchData() {
  loading.value = true
  try {
    const params: any = {}
    if (query.range) {
      params.dateFrom = query.range[0].format('YYYY-MM-DD')
      params.dateTo = query.range[1].format('YYYY-MM-DD')
    }
    const res = await getDiningStatsSummary(params)
    summary.totalOrders = res.totalOrders || 0
    summary.totalAmount = res.totalAmount || 0
    summary.deliveredOrders = res.deliveredOrders || 0
    summary.deliveryRate = res.deliveryRate || 0
    summary.onTimeDeliveredOrders = res.onTimeDeliveredOrders || 0
    summary.trackedDeliveryOrders = res.trackedDeliveryOrders || 0
    summary.onTimeDeliveryRate = res.onTimeDeliveryRate || 0
    summary.nextMonthEstimatedCost = res.nextMonthEstimatedCost || 0
    summary.mealTypeStats = res.mealTypeStats || []
    summary.buildingStats = res.buildingStats || []
    summary.procurementItems = res.procurementItems || []
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.range = undefined
  fetchData()
}

function formatCurrency(value?: number) {
  return `${Number(value || 0).toFixed(2)}元`
}

function formatQty(value?: number, unit?: string) {
  return `${Number(value || 0).toFixed(2).replace(/\.00$/, '')}${unit || '斤'}`
}

fetchData()
</script>

<style scoped>
.stats-row {
  margin-bottom: 16px;
}

.stat-meta {
  margin-top: 8px;
  color: var(--muted);
  font-size: 12px;
}
</style>
