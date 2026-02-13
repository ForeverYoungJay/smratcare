<template>
  <PageContainer title="老人账户" subTitle="余额、预警阈值与账户流水">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="老人姓名">
        <a-input v-model:value="query.keyword" placeholder="输入姓名" allow-clear />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="openWarnings">余额预警</a-button>
          <a-button type="primary" @click="openAdjust">余额调整</a-button>
        </a-space>
      </template>
    </SearchForm>

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
            @search="searchElders"
          />
        </a-form-item>
        <a-form-item label="方向" required>
          <a-select v-model:value="adjustForm.direction" :options="directionOptions" />
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
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getElderAccountPage, adjustElderAccount, getElderAccountWarnings, updateElderAccount } from '../../api/finance'
import { getElderPage } from '../../api/elder'
import type { ElderAccount, PageResult } from '../../types'
import router from '../../router'

const loading = ref(false)
const rows = ref<ElderAccount[]>([])
const warnings = ref<ElderAccount[]>([])
const warningOpen = ref(false)

const query = reactive({
  keyword: '',
  pageNo: 1,
  pageSize: 10
})

const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '余额', dataIndex: 'balance', key: 'balance', width: 120 },
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
  elderId: undefined as number | undefined,
  elderName: '',
  creditLimit: undefined as number | undefined,
  warnThreshold: undefined as number | undefined,
  status: 1,
  remark: ''
})
const adjustForm = reactive({
  elderId: undefined as number | undefined,
  direction: 'CREDIT',
  amount: 0,
  remark: ''
})
const elderOptions = ref<{ label: string; value: number }[]>([])
const directionOptions = [
  { label: '充值', value: 'CREDIT' },
  { label: '扣费', value: 'DEBIT' }
]
const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<ElderAccount> = await getElderAccountPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
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
  adjustForm.amount = 0
  adjustForm.remark = ''
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
    const selected = elderOptions.value.find((opt) => opt.value === adjustForm.elderId)
    await adjustElderAccount({
      elderId: adjustForm.elderId,
      elderName: selected?.label,
      amount: adjustForm.amount,
      direction: adjustForm.direction as 'DEBIT' | 'CREDIT',
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
  if (!keyword) {
    elderOptions.value = []
    return
  }
  const res = await getElderPage({ pageNo: 1, pageSize: 20, keyword })
  elderOptions.value = res.list.map((item: any) => ({
    label: item.fullName || item.elderName || item.name || '未知老人',
    value: item.id
  }))
}

fetchData()
</script>

<style scoped>
</style>
