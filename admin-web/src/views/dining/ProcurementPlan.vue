<template>
  <PageContainer title="采购计划单" subTitle="生成每月采购计划单与固定消耗品采购单">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="计划月份">
        <a-date-picker v-model:value="query.month" picker="month" />
      </a-form-item>
    </SearchForm>

    <a-row :gutter="16" class="summary-row">
      <a-col :xs="24" :md="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="月采购菜品数" :value="monthlyItems.length" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="月采购总量" :value="monthlyQtyText" />
        </a-card>
      </a-col>
      <a-col :xs="24" :md="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="月采购金额" :value="monthlyCost" :precision="2" suffix="元" />
        </a-card>
      </a-col>
    </a-row>

    <a-tabs v-model:activeKey="activeTab">
      <a-tab-pane key="monthly" tab="每月采购计划单">
        <a-card class="card-elevated" :bordered="false">
          <template #extra>
            <a-space>
              <a-button @click="exportMonthlyPlan">导出CSV</a-button>
              <a-button @click="printMonthlyPlan">打印</a-button>
            </a-space>
          </template>
          <div class="plan-note">按本月食谱排班次数自动汇总下月计划采购量。</div>
          <DataTable rowKey="id" :columns="monthlyColumns" :data-source="monthlyItems" :loading="loading" :pagination="false" />
        </a-card>
      </a-tab-pane>
      <a-tab-pane key="fixed" tab="固定消耗品采购单">
        <a-card class="card-elevated" :bordered="false">
          <template #extra>
            <a-space>
              <a-button @click="exportFixedPlan">导出CSV</a-button>
              <a-button @click="printFixedPlan">打印</a-button>
            </a-space>
          </template>
          <div class="plan-note">按已维护的现行用餐人数与固定采购用量生成固定消耗品清单。</div>
          <DataTable rowKey="id" :columns="fixedColumns" :data-source="fixedItems" :loading="loading" :pagination="false" />
        </a-card>
      </a-tab-pane>
    </a-tabs>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getDiningMealTypeLabel } from '../../constants/dining'
import { getDiningDishAnalytics } from '../../api/dining'
import { exportCsv } from '../../utils/export'
import { printTableReport } from '../../utils/print'
import type { DiningDishAnalyticsResponse } from '../../types'

const loading = ref(false)
const activeTab = ref('monthly')
const query = reactive({
  month: dayjs()
})
const analytics = reactive<DiningDishAnalyticsResponse>({
  month: '',
  totalDishCount: 0,
  totalRecipeCount: 0,
  totalPlannedPurchaseQty: 0,
  totalEstimatedCost: 0,
  items: []
})

const monthlyItems = computed(() => (analytics.items || []).filter((item) => Number(item.monthlyRecipeCount || 0) > 0))
const fixedItems = computed(() => (analytics.items || []).filter((item) => Number(item.purchaseQty || 0) > 0 && Number(item.currentDiningCount || 0) > 0))

const monthlyCost = computed(() => monthlyItems.value.reduce((sum, item) => sum + Number(item.monthlyEstimatedCost || 0), 0))
const monthlyQtyText = computed(() => `${formatNumber(monthlyItems.value.reduce((sum, item) => sum + Number(item.monthlyPlannedPurchaseQty || 0), 0))}斤`)

const monthlyColumns = [
  { title: '菜品', dataIndex: 'dishName', key: 'dishName', width: 140 },
  { title: '分类', dataIndex: 'dishCategory', key: 'dishCategory', width: 120 },
  { title: '餐次', dataIndex: 'mealType', key: 'mealType', width: 100, customRender: ({ text }: any) => getDiningMealTypeLabel(text) },
  { title: '排班次数', dataIndex: 'monthlyRecipeCount', key: 'monthlyRecipeCount', width: 100 },
  { title: '现行用餐人数', dataIndex: 'currentDiningCount', key: 'currentDiningCount', width: 120 },
  { title: '单次采购用量', key: 'purchaseQty', width: 120, customRender: ({ record }: any) => formatQty(record.purchaseQty, record.purchaseUnit) },
  { title: '计划采购量', key: 'monthlyPlannedPurchaseQty', width: 120, customRender: ({ record }: any) => formatQty(record.monthlyPlannedPurchaseQty, record.purchaseUnit) },
  { title: '计划采购金额', key: 'monthlyEstimatedCost', width: 130, customRender: ({ record }: any) => formatCurrency(record.monthlyEstimatedCost) }
]

const fixedColumns = [
  { title: '菜品', dataIndex: 'dishName', key: 'dishName', width: 140 },
  { title: '分类', dataIndex: 'dishCategory', key: 'dishCategory', width: 120 },
  { title: '餐次', dataIndex: 'mealType', key: 'mealType', width: 100, customRender: ({ text }: any) => getDiningMealTypeLabel(text) },
  { title: '现行用餐人数', dataIndex: 'currentDiningCount', key: 'currentDiningCount', width: 120 },
  { title: '固定采购量', key: 'purchaseQty', width: 120, customRender: ({ record }: any) => formatQty(record.purchaseQty, record.purchaseUnit) },
  { title: '固定采购金额', key: 'fixedCost', width: 130, customRender: ({ record }: any) => formatCurrency(Number(record.unitPrice || 0) * Number(record.purchaseQty || 0)) }
]

async function fetchData() {
  loading.value = true
  try {
    const res = await getDiningDishAnalytics({
      month: query.month ? dayjs(query.month).startOf('month').format('YYYY-MM-DD') : undefined
    })
    analytics.month = res.month
    analytics.totalDishCount = res.totalDishCount || 0
    analytics.totalRecipeCount = res.totalRecipeCount || 0
    analytics.totalPlannedPurchaseQty = res.totalPlannedPurchaseQty || 0
    analytics.totalEstimatedCost = res.totalEstimatedCost || 0
    analytics.items = res.items || []
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.month = dayjs()
  fetchData()
}

function formatNumber(value?: number) {
  return Number(value || 0).toFixed(2).replace(/\.00$/, '')
}

function formatQty(value?: number, unit?: string) {
  return `${formatNumber(value)}${unit || '斤'}`
}

function formatCurrency(value?: number) {
  return `${Number(value || 0).toFixed(2)}元`
}

function exportMonthlyPlan() {
  exportCsv(
    monthlyItems.value.map((item) => ({
      菜品: item.dishName,
      分类: item.dishCategory || '',
      餐次: getDiningMealTypeLabel(item.mealType),
      排班次数: item.monthlyRecipeCount,
      现行用餐人数: item.currentDiningCount || 0,
      单次采购用量: formatQty(item.purchaseQty, item.purchaseUnit),
      计划采购量: formatQty(item.monthlyPlannedPurchaseQty, item.purchaseUnit),
      计划采购金额: formatCurrency(item.monthlyEstimatedCost)
    })),
    `每月采购计划单-${dayjs(query.month).format('YYYY-MM')}.csv`
  )
}

function exportFixedPlan() {
  exportCsv(
    fixedItems.value.map((item) => ({
      菜品: item.dishName,
      分类: item.dishCategory || '',
      餐次: getDiningMealTypeLabel(item.mealType),
      现行用餐人数: item.currentDiningCount || 0,
      固定采购量: formatQty(item.purchaseQty, item.purchaseUnit),
      固定采购金额: formatCurrency(Number(item.unitPrice || 0) * Number(item.purchaseQty || 0))
    })),
    `固定消耗品采购单-${dayjs(query.month).format('YYYY-MM')}.csv`
  )
}

function printMonthlyPlan() {
  printTableReport({
    title: '每月采购计划单',
    subtitle: `计划月份：${dayjs(query.month).format('YYYY-MM')}`,
    columns: [
      { key: 'dishName', title: '菜品' },
      { key: 'dishCategory', title: '分类' },
      { key: 'mealTypeLabel', title: '餐次' },
      { key: 'monthlyRecipeCount', title: '排班次数' },
      { key: 'currentDiningCount', title: '现行用餐人数' },
      { key: 'purchaseQtyText', title: '单次采购用量' },
      { key: 'monthlyPlannedPurchaseQtyText', title: '计划采购量' },
      { key: 'monthlyEstimatedCostText', title: '计划采购金额' }
    ],
    rows: monthlyItems.value.map((item) => ({
      ...item,
      mealTypeLabel: getDiningMealTypeLabel(item.mealType),
      purchaseQtyText: formatQty(item.purchaseQty, item.purchaseUnit),
      monthlyPlannedPurchaseQtyText: formatQty(item.monthlyPlannedPurchaseQty, item.purchaseUnit),
      monthlyEstimatedCostText: formatCurrency(item.monthlyEstimatedCost)
    })),
    signatures: ['制单人', '审核人', '采购负责人']
  })
}

function printFixedPlan() {
  printTableReport({
    title: '固定消耗品采购单',
    subtitle: `计划月份：${dayjs(query.month).format('YYYY-MM')}`,
    columns: [
      { key: 'dishName', title: '菜品' },
      { key: 'dishCategory', title: '分类' },
      { key: 'mealTypeLabel', title: '餐次' },
      { key: 'currentDiningCount', title: '现行用餐人数' },
      { key: 'purchaseQtyText', title: '固定采购量' },
      { key: 'fixedCostText', title: '固定采购金额' }
    ],
    rows: fixedItems.value.map((item) => ({
      ...item,
      mealTypeLabel: getDiningMealTypeLabel(item.mealType),
      purchaseQtyText: formatQty(item.purchaseQty, item.purchaseUnit),
      fixedCostText: formatCurrency(Number(item.unitPrice || 0) * Number(item.purchaseQty || 0))
    })),
    signatures: ['制单人', '审核人', '采购负责人']
  })
}

fetchData()
</script>

<style scoped>
.summary-row {
  margin-bottom: 16px;
}

.plan-note {
  margin-bottom: 12px;
  color: var(--muted);
}
</style>
