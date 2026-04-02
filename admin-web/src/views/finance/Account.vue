<template>
  <PageContainer title="老人账户" subTitle="余额、预警阈值与账户流水">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="老人姓名">
        <ElderNameAutocomplete v-model:value="query.keyword" placeholder="姓名(编号)" width="220px" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="openWarnings">余额预警</a-button>
          <a-button type="primary" @click="openAdjust">余额调整</a-button>
        </a-space>
      </template>
    </SearchForm>

    <a-alert
      v-if="signedLinkageContext.active"
      style="margin-bottom: 12px;"
      type="success"
      show-icon
      :message="signedLinkageContext.message"
      :description="'当前可直接查看余额、押金/预存与账户流水。'"
    />

    <a-row :gutter="[12, 12]" style="margin-bottom: 12px;">
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="账户总数" :value="rows.length" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="低余额账户" :value="lowBalanceCount" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="账户总余额" :value="totalBalance" suffix="元" :precision="2" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="停用账户" :value="disabledCount" /></a-card></a-col>
      <a-col :xs="24" :xl="8"><a-card class="card-elevated" :bordered="false"><a-statistic title="押金余额合计" :value="totalDepositBalance" suffix="元" :precision="2" /></a-card></a-col>
      <a-col :xs="24" :xl="8"><a-card class="card-elevated" :bordered="false"><a-statistic title="预收余额合计" :value="totalPrepaidBalance" suffix="元" :precision="2" /></a-card></a-col>
      <a-col :xs="24" :xl="8"><a-card class="card-elevated" :bordered="false"><a-statistic title="历史未拆分余额" :value="totalUnclassifiedBalance" suffix="元" :precision="2" /></a-card></a-col>
      <a-col :span="24">
        <a-alert
          :type="moduleSummary.warningMessage ? 'warning' : 'info'"
          show-icon
          :message="moduleSummary.warningMessage || `欠费 ${moduleSummary.totalCount} 人，低余额 ${moduleSummary.pendingCount} 人，合同到期风险 ${moduleSummary.exceptionCount} 人`"
          :description="`今日收款 ${Number(moduleSummary.todayAmount || 0).toFixed(2)} 元，本月欠费 ${Number(moduleSummary.monthAmount || 0).toFixed(2)} 元`"
        />
      </a-col>
    </a-row>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'balance'">
          <a-tag :color="record.balance <= record.warnThreshold ? 'red' : 'green'">
            {{ record.balance ?? 0 }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'pointsBalance'">
          <a-tag :color="record.pointsStatus === 0 ? 'default' : 'blue'">
            {{ record.pointsBalance ?? 0 }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'gray'">
            {{ record.status === 1 ? '启用' : '停用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openLogs(record)">查看流水</a-button>
            <a-button type="link" @click="openConfig(record)">账户设置</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="adjustOpen" title="余额调整" @ok="submitAdjust" :confirm-loading="adjusting">
      <a-form layout="vertical">
        <a-form-item label="老人姓名" required>
          <a-select
            v-model:value="adjustForm.elderId"
            show-search
            placeholder="输入姓名搜索"
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            @search="searchElders"
            @focus="() => !elderOptions.length && searchElders('')"
          />
        </a-form-item>
        <a-form-item label="方向" required>
          <a-select v-model:value="adjustForm.direction" :options="directionOptions" />
        </a-form-item>
        <a-form-item label="资金类型" required>
          <a-select v-model:value="adjustForm.fundType" :options="fundTypeOptions" />
        </a-form-item>
        <a-form-item label="金额" required>
          <a-input-number v-model:value="adjustForm.amount" style="width: 100%" :min="0.01" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="adjustForm.remark" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="warningOpen" title="余额预警" :footer="null" width="720px">
      <a-table :columns="warningColumns" :data-source="warnings" rowKey="id" :pagination="false" />
    </a-modal>

    <a-modal v-model:open="configOpen" title="账户设置" @ok="submitConfig" :confirm-loading="configSaving">
      <a-form layout="vertical">
        <a-form-item label="老人姓名">
          <a-input v-model:value="configForm.elderName" disabled />
        </a-form-item>
        <a-form-item label="信用额度">
          <a-input-number v-model:value="configForm.creditLimit" style="width: 100%" :min="0" />
        </a-form-item>
        <a-form-item label="预警阈值">
          <a-input-number v-model:value="configForm.warnThreshold" style="width: 100%" :min="0" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="configForm.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="configForm.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { getElderAccountPage, adjustElderAccount, getElderAccountWarnings, getFinanceModuleEntrySummary, updateElderAccount } from '../../api/finance'
import type { ElderAccount, FinanceModuleEntrySummary, Id, PageResult } from '../../types'
import router from '../../router'

const route = useRoute()
const loading = ref(false)
const rows = ref<ElderAccount[]>([])
const warnings = ref<ElderAccount[]>([])
const warningOpen = ref(false)
const moduleSummary = ref<FinanceModuleEntrySummary>({
  moduleKey: 'BALANCE_WARN',
  bizDate: '',
  todayAmount: 0,
  monthAmount: 0,
  totalCount: 0,
  pendingCount: 0,
  exceptionCount: 0,
  warningMessage: '',
  topItems: []
})

const query = reactive({
  keyword: '',
  pageNo: 1,
  pageSize: 10
})
const signedLinkageContext = computed(() => {
  const source = routeQueryText(route.query.source).toLowerCase()
  const entryScene = routeQueryText(route.query.entryScene).toLowerCase()
  const elderName = routeQueryText(route.query.keyword || route.query.elderName || route.query.residentName)
  const active = !!elderName && (source === 'contract_signed' || entryScene === 'signed_onboarding')
  return {
    active,
    elderName,
    message: active ? `当前账户中心已定位到新签约长者 ${elderName}` : ''
  }
})

const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const lowBalanceCount = computed(() => rows.value.filter(item => Number(item.balance || 0) <= Number(item.warnThreshold || 0)).length)
const disabledCount = computed(() => rows.value.filter(item => Number(item.status || 0) !== 1).length)
const totalBalance = computed(() => rows.value.reduce((sum, item) => sum + Number(item.balance || 0), 0))
const totalDepositBalance = computed(() => rows.value.reduce((sum, item) => sum + Number(item.depositBalance || 0), 0))
const totalPrepaidBalance = computed(() => rows.value.reduce((sum, item) => sum + Number(item.prepaidBalance || 0), 0))
const totalUnclassifiedBalance = computed(() => rows.value.reduce((sum, item) => sum + Number(item.unclassifiedBalance || 0), 0))

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '余额', dataIndex: 'balance', key: 'balance', width: 120 },
  { title: '押金', dataIndex: 'depositBalance', key: 'depositBalance', width: 120 },
  { title: '预收', dataIndex: 'prepaidBalance', key: 'prepaidBalance', width: 120 },
  { title: '积分余额', dataIndex: 'pointsBalance', key: 'pointsBalance', width: 120 },
  { title: '信用额度', dataIndex: 'creditLimit', key: 'creditLimit', width: 120 },
  { title: '预警阈值', dataIndex: 'warnThreshold', key: 'warnThreshold', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
  { title: '操作', key: 'action', width: 120 }
]

const warningColumns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '余额', dataIndex: 'balance', key: 'balance', width: 120 },
  { title: '预警阈值', dataIndex: 'warnThreshold', key: 'warnThreshold', width: 120 },
  { title: '备注', dataIndex: 'remark', key: 'remark' }
]

const adjustOpen = ref(false)
const adjusting = ref(false)
const configOpen = ref(false)
const configSaving = ref(false)
const configForm = reactive({
  elderId: undefined as Id | undefined,
  elderName: '',
  creditLimit: undefined as number | undefined,
  warnThreshold: undefined as number | undefined,
  status: 1,
  remark: ''
})
const adjustForm = reactive({
  elderId: undefined as Id | undefined,
  direction: 'CREDIT',
  fundType: 'PREPAID' as 'PREPAID' | 'DEPOSIT' | 'AUTO',
  amount: 0,
  remark: ''
})
const { elderOptions, elderLoading, searchElders: searchElderOptions, findElderName } = useElderOptions({ pageSize: 20 })
const directionOptions = [
  { label: '充值', value: 'CREDIT' },
  { label: '扣费', value: 'DEBIT' }
]
const fundTypeOptions = computed(() => adjustForm.direction === 'CREDIT'
  ? [
      { label: '预收', value: 'PREPAID' },
      { label: '押金', value: 'DEPOSIT' }
    ]
  : [
      { label: '自动抵扣', value: 'AUTO' },
      { label: '预收', value: 'PREPAID' },
      { label: '押金', value: 'DEPOSIT' }
    ])
const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

function routeQueryText(value: unknown) {
  if (Array.isArray(value)) {
    return routeQueryText(value[0])
  }
  if (value == null) {
    return ''
  }
  return String(value).trim()
}

function applyRouteFilters() {
  const keyword = routeQueryText(route.query.keyword || route.query.elderName || route.query.residentName)
  query.keyword = keyword
}

async function fetchData() {
  loading.value = true
  try {
    const [res, summary] = await Promise.all([
      getElderAccountPage({
        pageNo: query.pageNo,
        pageSize: query.pageSize,
        keyword: query.keyword
      }) as Promise<PageResult<ElderAccount>>,
      getFinanceModuleEntrySummary({ moduleKey: 'BALANCE_WARN' })
    ])
    rows.value = res.list
    pagination.total = res.total || res.list.length
    moduleSummary.value = summary
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.keyword = ''
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openAdjust() {
  adjustForm.elderId = undefined
  adjustForm.direction = 'CREDIT'
  adjustForm.fundType = 'PREPAID'
  adjustForm.amount = 0
  adjustForm.remark = ''
  searchElderOptions('')
  adjustOpen.value = true
}

async function submitAdjust() {
  if (!adjustForm.elderId) {
    message.error('请选择老人')
    return
  }
  if (!adjustForm.amount || adjustForm.amount <= 0) {
    message.error('请输入金额')
    return
  }
  adjusting.value = true
  try {
    await adjustElderAccount({
      elderId: adjustForm.elderId,
      elderName: findElderName(adjustForm.elderId),
      amount: adjustForm.amount,
      direction: adjustForm.direction as 'DEBIT' | 'CREDIT',
      fundType: adjustForm.fundType,
      remark: adjustForm.remark
    })
    message.success('调整成功')
    adjustOpen.value = false
    fetchData()
  } finally {
    adjusting.value = false
  }
}

function openLogs(record: ElderAccount) {
  router.push({ name: 'FinanceAccountLog', query: { elderId: record.elderId } })
}

function openConfig(record: ElderAccount) {
  configForm.elderId = record.elderId
  configForm.elderName = record.elderName || '未知老人'
  configForm.creditLimit = record.creditLimit
  configForm.warnThreshold = record.warnThreshold
  configForm.status = record.status
  configForm.remark = record.remark || ''
  configOpen.value = true
}

async function submitConfig() {
  if (!configForm.elderId) {
    message.error('请选择老人')
    return
  }
  configSaving.value = true
  try {
    await updateElderAccount({
      elderId: configForm.elderId,
      elderName: configForm.elderName,
      creditLimit: configForm.creditLimit,
      warnThreshold: configForm.warnThreshold,
      status: configForm.status,
      remark: configForm.remark
    })
    message.success('已更新')
    configOpen.value = false
    fetchData()
  } finally {
    configSaving.value = false
  }
}

async function openWarnings() {
  warnings.value = await getElderAccountWarnings()
  warningOpen.value = true
}

async function searchElders(keyword: string) {
  await searchElderOptions(keyword)
}

watch(
  () => adjustForm.direction,
  (direction) => {
    adjustForm.fundType = direction === 'DEBIT' ? 'AUTO' : 'PREPAID'
  }
)

applyRouteFilters()
fetchData()

watch(
  () => route.query,
  () => {
    applyRouteFilters()
    fetchData().catch(() => {})
  },
  { deep: true }
)

useLiveSyncRefresh({
  topics: ['finance', 'elder', 'oa'],
  refresh: () => {
    if (loading.value) return
    fetchData().catch(() => {})
  },
  debounceMs: 900
})
</script>

<style scoped>
</style>
