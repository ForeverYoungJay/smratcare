<template>
  <PageContainer :title="props.moduleName" :subTitle="props.description || '费用模块统一入口，实时展示资金与风险摘要'">
    <a-card :bordered="false" class="card-elevated">
      <StatefulBlock :loading="loading" :error="errorMessage" @retry="loadData">
        <a-row :gutter="[12, 12]">
          <a-col :xs="12" :lg="6">
            <a-statistic title="月收入" :value="currentMonthRevenue" suffix="元" :precision="2" />
          </a-col>
          <a-col :xs="12" :lg="6">
            <a-statistic title="欠费人数" :value="arrearsCount" />
          </a-col>
          <a-col :xs="12" :lg="6">
            <a-statistic title="账户预警" :value="warningCount" />
          </a-col>
          <a-col :xs="12" :lg="6">
            <a-statistic title="最近月收入环比" :value="revenueGrowthRate" suffix="%" :precision="2" />
          </a-col>
        </a-row>
        <a-divider />
        <a-space wrap>
          <a-button v-for="item in props.links" :key="item.to" @click="go(item.to)">{{ item.label }}</a-button>
          <a-button type="primary" @click="go('/finance/report')">进入财务报表</a-button>
          <a-button @click="go('/finance/account')">进入账户管理</a-button>
        </a-space>
      </StatefulBlock>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, withDefaults } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { getElderAccountWarnings, getFinanceArrearsTop, getFinanceMonthlyRevenue } from '../../api/finance'
import type { FinanceReportMonthlyItem } from '../../types'

type QuickLink = {
  label: string
  to: string
}

const props = withDefaults(
  defineProps<{
    moduleName: string
    description?: string
    links?: QuickLink[]
  }>(),
  {
    description: '',
    links: () => []
  }
)

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const monthlyRows = ref<FinanceReportMonthlyItem[]>([])
const arrearsCount = ref(0)
const warningCount = ref(0)

const currentMonthRevenue = computed(() => Number(monthlyRows.value[monthlyRows.value.length - 1]?.amount || 0))
const revenueGrowthRate = computed(() => {
  const rows = monthlyRows.value
  if (rows.length < 2) return 0
  const curr = Number(rows[rows.length - 1]?.amount || 0)
  const prev = Number(rows[rows.length - 2]?.amount || 0)
  if (!prev) return curr > 0 ? 100 : 0
  return ((curr - prev) / prev) * 100
})

function go(path: string) {
  router.push(path)
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [monthly, arrearsTop, warnings] = await Promise.all([
      getFinanceMonthlyRevenue({ months: 6 }),
      getFinanceArrearsTop({ limit: 50 }),
      getElderAccountWarnings()
    ])
    monthlyRows.value = monthly || []
    arrearsCount.value = (arrearsTop || []).length
    warningCount.value = (warnings || []).length
  } catch (error: any) {
    errorMessage.value = error?.message || '加载费用概览失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
