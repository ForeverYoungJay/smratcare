<template>
  <PageContainer title="长护险结算" subTitle="月度结算单：统筹支付 / 个人自付 / 超限额（金额单位：元）">
    <SearchForm :model="query" @search="onSearch" @reset="onReset">
      <a-form-item label="长者">
        <a-select v-model:value="query.elderId" show-search allow-clear :filter-option="false" :options="elderOptions"
          placeholder="输入姓名搜索" style="width: 200px" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
      </a-form-item>
      <a-form-item label="结算月份">
        <a-date-picker v-model:value="query.month" picker="month" value-format="YYYYMM" placeholder="YYYYMM" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 140px" placeholder="全部">
          <a-select-option value="DRAFT">草稿</a-select-option>
          <a-select-option value="SUBMITTED">已提交</a-select-option>
          <a-select-option value="SETTLED">已结算</a-select-option>
          <a-select-option value="REJECTED">已驳回</a-select-option>
        </a-select>
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openGenerate">生成结算草稿</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'totalFee'">{{ yuan(record.totalFee) }}</template>
        <template v-else-if="column.key === 'fundPay'">{{ yuan(record.fundPay) }}</template>
        <template v-else-if="column.key === 'selfPay'">{{ yuan(record.selfPay) }}</template>
        <template v-else-if="column.key === 'overQuota'">
          <span :class="{ 'over-quota': record.overQuota > 0 }">{{ yuan(record.overQuota) }}</span>
        </template>
        <template v-else-if="column.key === 'settleStatus'">
          <a-tag :color="statusColor(record.settleStatus)">{{ statusText(record.settleStatus) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button
            v-if="record.settleStatus === 'DRAFT'"
            type="link"
            size="small"
            @click="doSubmit(record)"
          >提交</a-button>
          <span v-else class="text-muted">—</span>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="genOpen" title="生成结算草稿" :confirm-loading="saving" @ok="submitGenerate">
      <a-form layout="vertical">
        <a-form-item label="长者" required>
          <a-select v-model:value="genForm.elderId" show-search :filter-option="false" :options="elderOptions"
            placeholder="输入姓名搜索" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
        </a-form-item>
        <a-form-item label="结算月份" required>
          <a-date-picker v-model:value="genForm.month" picker="month" value-format="YYYYMM" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { getLtciSettlementPage, generateLtciSettlement, submitLtciSettlement } from '../../api/ltci'
import type { Id, LtciSettlement, PageResult } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<LtciSettlement[]>([])
const query = reactive({
  elderId: undefined as Id | undefined,
  month: undefined as string | undefined,
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const { elderOptions, searchElders } = useElderOptions({ pageSize: 50 })

const columns = [
  { title: '长者ID', dataIndex: 'elderId', key: 'elderId', width: 100 },
  { title: '结算月份', dataIndex: 'settleMonth', key: 'settleMonth', width: 110 },
  { title: '服务天数', dataIndex: 'serviceDays', key: 'serviceDays', width: 90 },
  { title: '费用合计(元)', key: 'totalFee', width: 120 },
  { title: '统筹支付(元)', key: 'fundPay', width: 120 },
  { title: '个人自付(元)', key: 'selfPay', width: 120 },
  { title: '超限额(元)', key: 'overQuota', width: 110 },
  { title: '状态', key: 'settleStatus', width: 100 },
  { title: '操作', key: 'action', width: 90 }
]

const genOpen = ref(false)
const genForm = reactive({ elderId: undefined as Id | undefined, month: undefined as string | undefined })

function yuan(fen?: number) {
  return fen == null ? '0.00' : (Number(fen) / 100).toFixed(2)
}
function statusText(s?: string) {
  return ({ DRAFT: '草稿', SUBMITTED: '已提交', SETTLED: '已结算', REJECTED: '已驳回' } as Record<string, string>)[s || ''] || s || '-'
}
function statusColor(s?: string) {
  if (s === 'SETTLED') return 'green'
  if (s === 'SUBMITTED') return 'blue'
  if (s === 'REJECTED') return 'red'
  return 'orange'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<LtciSettlement> = await getLtciSettlementPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      elderId: query.elderId,
      month: query.month,
      status: query.status
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}
function onSearch() {
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}
function onReset() {
  query.elderId = undefined
  query.month = undefined
  query.status = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}
function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function openGenerate() {
  genForm.elderId = undefined
  genForm.month = undefined
  genOpen.value = true
}
async function submitGenerate() {
  if (!genForm.elderId || !genForm.month) {
    message.error('请选择长者与结算月份')
    return
  }
  if (saving.value) return
  saving.value = true
  try {
    await generateLtciSettlement(genForm.elderId, genForm.month)
    message.success('结算草稿已生成')
    genOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function doSubmit(record: LtciSettlement) {
  await submitLtciSettlement(record.id)
  message.success('结算单已提交')
  fetchData()
}

fetchData()
searchElders('')
</script>

<style scoped>
.over-quota {
  color: #cf1322;
  font-weight: 600;
}
.text-muted {
  color: #999;
}
</style>
