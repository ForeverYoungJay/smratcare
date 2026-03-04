<template>
  <PageContainer :title="props.moduleName" :subTitle="props.description || '费用模块统一入口，实时展示资金与风险摘要'">
    <a-card :bordered="false" class="card-elevated">
      <StatefulBlock :loading="loading" :error="errorMessage" @retry="loadData">
        <a-row :gutter="[12, 12]">
          <a-col :xs="12" :lg="6">
            <a-statistic title="今日金额" :value="summary.todayAmount" suffix="元" :precision="2" />
          </a-col>
          <a-col :xs="12" :lg="6">
            <a-statistic title="本月金额" :value="summary.monthAmount" suffix="元" :precision="2" />
          </a-col>
          <a-col :xs="12" :lg="6">
            <a-statistic title="待处理" :value="summary.pendingCount" />
          </a-col>
          <a-col :xs="12" :lg="6">
            <a-statistic title="异常数" :value="summary.exceptionCount" />
          </a-col>
        </a-row>
        <a-alert v-if="summary.warningMessage" style="margin-top: 12px;" type="warning" show-icon :message="summary.warningMessage" />
        <a-card style="margin-top: 12px;" size="small" :bordered="false" title="摘要明细">
          <vxe-table border stripe show-overflow :data="summary.topItems || []" height="220">
            <vxe-column field="label" title="维度" min-width="180" />
            <vxe-column field="count" title="数量" width="120" />
            <vxe-column field="amount" title="金额（元）" width="160" />
          </vxe-table>
        </a-card>
        <a-divider />
        <a-space wrap>
          <a-button v-for="item in props.links" :key="item.to" @click="go(item.to)">{{ item.label }}</a-button>
          <a-button type="primary" @click="go('/finance/reports/overall')">进入财务报表</a-button>
          <a-button @click="go('/finance/accounts/list')">进入账户管理</a-button>
        </a-space>
      </StatefulBlock>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref, withDefaults } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { getFinanceModuleEntrySummary } from '../../api/finance'
import type { FinanceModuleEntrySummary } from '../../types'

type QuickLink = {
  label: string
  to: string
}

const props = withDefaults(
  defineProps<{
    moduleName: string
    moduleKey?: string
    description?: string
    links?: QuickLink[]
  }>(),
  {
    moduleKey: 'GENERAL',
    description: '',
    links: () => []
  }
)

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const summary = ref<FinanceModuleEntrySummary>({
  moduleKey: 'GENERAL',
  bizDate: '',
  todayAmount: 0,
  monthAmount: 0,
  totalCount: 0,
  pendingCount: 0,
  exceptionCount: 0,
  warningMessage: '',
  topItems: []
})

function go(path: string) {
  router.push(path)
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    summary.value = await getFinanceModuleEntrySummary({
      moduleKey: props.moduleKey || 'GENERAL'
    })
  } catch (error: any) {
    errorMessage.value = error?.message || '加载费用概览失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
