<template>
  <PageContainer title="财务工作台" subTitle="先处理收款与异常，再核对当天账务。">
    <template #extra>
      <a-button :loading="loading" @click="load">刷新</a-button>
    </template>
    <DepartmentHomeOverview department="FINANCE" :show-header="false" :show-catalog="false"
      :metrics="metrics" :data-state="dataState" :data-message="errorMessage" :updated-at="updatedAt"
      scope-label="机构当日收银口径（非个人业绩）" :period-label="overview?.bizDate ? `业务日期 ${overview.bizDate}` : ''" />
    <WorkQueuePanel :items="queue" />
  </PageContainer>
</template>
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import DepartmentHomeOverview from '../../components/workbench/DepartmentHomeOverview.vue'
import WorkQueuePanel from '../../components/workbench/WorkQueuePanel.vue'
import { useUserStore } from '../../stores/user'
import { getFinanceWorkbenchOverview } from '../../api/finance'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import type { FinanceWorkbenchOverview } from '../../types'
import type { DepartmentHomeDataState, DepartmentHomeMetric } from '../../workbench/departmentHome'
const user = useUserStore()
const management = computed(() => user.roles.some(role => ['FINANCE_MINISTER', 'DIRECTOR', 'ADMIN'].includes(role)))
const overview = ref<FinanceWorkbenchOverview | null>(null)
const loading = ref(false)
const errorMessage = ref('')
const updatedAt = ref('')
const forbidden = ref(false)
const dataState = computed<DepartmentHomeDataState>(() => loading.value ? 'loading' : forbidden.value ? 'forbidden' : errorMessage.value ? 'error' : overview.value ? 'ready' : 'empty')
const metrics = computed<DepartmentHomeMetric[]>(() => overview.value ? [
  { key: 'received', label: '今日收款', value: `¥ ${Number(overview.value.cashier?.todayCollectedTotal || 0).toLocaleString('zh-CN', { maximumFractionDigits: 2 })}`, path: '/finance/payments/records', tone: 'success' },
  { key: 'arrears', label: '欠费长者', value: overview.value.risk?.overdueElderCount, path: '/finance/bills/follow-up', tone: 'warning' },
  { key: 'issues', label: '待处理异常', value: overview.value.pending?.issueTodoCount, path: '/finance/reconcile/issue-center', tone: 'danger' },
  { key: 'refund', label: '待审批退款', value: overview.value.pending?.pendingRefundCount, path: '/finance/payments/refund-reversal', tone: 'warning' }
] : [])
const queue = computed(() => [
  { label: '登记收款', hint: '选择账单，核对金额后收款', path: '/finance/payments/register' },
  { label: '补齐发票与收据', hint: '核对收款记录与票据关联', path: '/finance/fees/payment-and-invoice' },
  { label: '处理财务异常', value: overview.value?.pending?.issueTodoCount, path: '/finance/reconcile/issue-center' },
  { label: '催缴欠费', value: overview.value?.pending?.collectionReminderCount, path: '/finance/bills/follow-up' },
  { label: '日结交班', hint: '核对流水、票据与退款后交班', path: '/finance/payments/shift-close' },
  ...(management.value ? [{ label: '月结核对', hint: '检查账单与对账，按授权关账', path: '/finance/reconcile/month-close' }] : [])
])
async function load() {
  if (loading.value) return
  loading.value = true; errorMessage.value = ''; forbidden.value = false
  try {
    overview.value = await getFinanceWorkbenchOverview() as unknown as FinanceWorkbenchOverview
    updatedAt.value = dayjs().format('HH:mm')
  } catch (error: any) {
    overview.value = null; forbidden.value = Number(error?.response?.status) === 403
    errorMessage.value = error?.message || '财务工作台加载失败，请重试'
  } finally { loading.value = false }
}
onMounted(load)
useLiveSyncRefresh({ topics: ['finance'], refresh: load, debounceMs: 900 })
</script>
