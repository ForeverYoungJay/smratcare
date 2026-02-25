<template>
  <PageContainer title="订餐统计" subTitle="查看订单量、金额与送达率">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
    </SearchForm>

    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="6">
        <a-card>
          <a-statistic title="订单总数" :value="summary.totalOrders" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic title="订单金额" :value="summary.totalAmount" :precision="2" suffix="元" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic title="送达订单" :value="summary.deliveredOrders" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic title="送达率" :value="summary.deliveryRate" :precision="2" suffix="%" />
        </a-card>
      </a-col>
    </a-row>

    <DataTable rowKey="mealType" :columns="columns" :data-source="summary.mealTypeStats" :loading="loading" :pagination="false" />
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getDiningStatsSummary } from '../../api/dining'
import type { DiningStatsSummary } from '../../types'

const loading = ref(false)
const query = reactive({ range: undefined as [Dayjs, Dayjs] | undefined })
const summary = reactive<DiningStatsSummary>({
  totalOrders: 0,
  totalAmount: 0,
  deliveredOrders: 0,
  deliveryRate: 0,
  mealTypeStats: []
})

const columns = [
  { title: '餐次', dataIndex: 'mealType', key: 'mealType', width: 120 },
  { title: '订单数', dataIndex: 'orderCount', key: 'orderCount', width: 120 }
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
    summary.totalOrders = res.totalOrders
    summary.totalAmount = res.totalAmount
    summary.deliveredOrders = res.deliveredOrders
    summary.deliveryRate = res.deliveryRate
    summary.mealTypeStats = res.mealTypeStats || []
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.range = undefined
  fetchData()
}

fetchData()
</script>
