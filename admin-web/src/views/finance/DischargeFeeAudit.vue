<template>
  <PageContainer title="退住费用审核" subTitle="审核通过后自动生成退住结算单">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="老人">
        <ElderNameAutocomplete v-model:value="query.keyword" allow-clear placeholder="老人姓名(编号)" width="220px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 180px" :options="statusOptions" />
      </a-form-item>
      <a-form-item label="风险等级">
        <a-select v-model:value="query.riskLevel" allow-clear style="width: 140px">
          <a-select-option value="HIGH">高风险</a-select-option>
          <a-select-option value="MEDIUM">中风险</a-select-option>
          <a-select-option value="LOW">低风险</a-select-option>
        </a-select>
      </a-form-item>
      <template #extra>
        <a-button @click="quickHighRisk">仅看高风险</a-button>
        <a-button type="primary" @click="openCreate">新建审核单</a-button>
      </template>
    </SearchForm>

    <a-row :gutter="[12, 12]" style="margin-bottom: 12px;">
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="审核单数" :value="filteredRows.length" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="待审核" :value="pendingCount" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="高风险单" :value="highRiskCount" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="涉及金额" :value="totalAmount" suffix="元" :precision="2" /></a-card></a-col>
    </a-row>

    <DataTable rowKey="id" :columns="columns" :data-source="filteredRows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 'APPROVED' ? 'green' : record.status === 'REJECTED' ? 'red' : 'orange'">{{ record.status }}</a-tag>
        </template>
        <template v-else-if="column.key === 'riskLevel'">
          <a-tag :color="riskColor(record)">{{ riskText(record) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" :disabled="record.status !== 'PENDING'" @click="review(record, 'APPROVED')">通过</a-button>
            <a-button type="link" danger :disabled="record.status !== 'PENDING'" @click="review(record, 'REJECTED')">驳回</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="createOpen" title="新建退住费用审核" :confirm-loading="creating" @ok="submitCreate">
      <a-form layout="vertical">
        <a-form-item label="老人" required>
          <a-select v-model:value="createForm.elderId" show-search :filter-option="false" :options="elderOptions" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
        </a-form-item>
        <a-form-item label="退住申请ID">
          <a-input v-model:value="createForm.dischargeApplyId" placeholder="请输入退住申请ID" />
        </a-form-item>
        <a-form-item label="应收应付金额" required>
          <a-input-number v-model:value="createForm.payableAmount" :min="0" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="费用项" required>
          <a-select v-model:value="createForm.feeItem" placeholder="请选择费用项">
            <a-select-option v-for="item in feeItemOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="退住费用设置" required>
          <a-select v-model:value="createForm.dischargeFeeConfig" placeholder="请选择退住费用设置">
            <a-select-option v-for="item in dischargeFeeConfigOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="createForm.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { Input, message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { getBaseConfigItemList } from '../../api/baseConfig'
import { createDischargeFeeAudit, getDischargeFeeAuditPage, reviewDischargeFeeAudit } from '../../api/financeFee'
import type { BaseConfigItem, DischargeFeeAuditItem, Id, PageResult } from '../../types'
import { computed } from 'vue'

const loading = ref(false)
const rows = ref<DischargeFeeAuditItem[]>([])
const query = reactive({
  pageNo: 1,
  pageSize: 10,
  status: undefined as string | undefined,
  riskLevel: undefined as 'HIGH' | 'MEDIUM' | 'LOW' | undefined,
  keyword: ''
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const statusOptions = [
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]
const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '申请ID', dataIndex: 'dischargeApplyId', key: 'dischargeApplyId', width: 120 },
  { title: '费用项', dataIndex: 'feeItem', key: 'feeItem', width: 140 },
  { title: '退住费用设置', dataIndex: 'dischargeFeeConfig', key: 'dischargeFeeConfig', width: 160 },
  { title: '应收应付', dataIndex: 'payableAmount', key: 'payableAmount', width: 120 },
  { title: '风险等级', key: 'riskLevel', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '审核备注', dataIndex: 'auditRemark', key: 'auditRemark' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 140 }
]
const filteredRows = computed(() => (rows.value || [])
  .filter(item => !query.riskLevel || riskText(item) === (query.riskLevel === 'HIGH' ? '高' : query.riskLevel === 'MEDIUM' ? '中' : '低')))
const pendingCount = computed(() => filteredRows.value.filter(item => item.status === 'PENDING').length)
const highRiskCount = computed(() => filteredRows.value.filter(item => riskText(item) === '高').length)
const totalAmount = computed(() => filteredRows.value.reduce((sum, item) => sum + Number(item.payableAmount || 0), 0))

const createOpen = ref(false)
const creating = ref(false)
const { elderOptions, searchElders: searchElderOptions } = useElderOptions({ pageSize: 20 })
const feeItemOptions = ref<{ label: string; value: string }[]>([])
const dischargeFeeConfigOptions = ref<{ label: string; value: string }[]>([])
const createForm = reactive({
  elderId: undefined as Id | undefined,
  dischargeApplyId: undefined as Id | undefined,
  payableAmount: 0,
  feeItem: '',
  dischargeFeeConfig: '',
  remark: ''
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<DischargeFeeAuditItem> = await getDischargeFeeAuditPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status,
      keyword: query.keyword || undefined
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.pageNo = 1
  query.status = undefined
  query.riskLevel = undefined
  query.keyword = ''
  pagination.current = 1
  fetchData()
}

function quickHighRisk() {
  query.riskLevel = 'HIGH'
}

function handleTableChange(pag: any) {
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

function openCreate() {
  createForm.elderId = undefined
  createForm.dischargeApplyId = undefined
  createForm.payableAmount = 0
  createForm.feeItem = ''
  createForm.dischargeFeeConfig = ''
  createForm.remark = ''
  createOpen.value = true
}

async function loadOptions() {
  const [feeItems, configs] = await Promise.all([
    getBaseConfigItemList({ configGroup: 'FEE_TYPE', status: 1 }),
    getBaseConfigItemList({ configGroup: 'DISCHARGE_FEE_CONFIG', status: 1 })
  ])
  feeItemOptions.value = (feeItems || []).map((item: BaseConfigItem) => ({ label: item.itemName, value: item.itemName }))
  dischargeFeeConfigOptions.value = (configs || []).map((item: BaseConfigItem) => ({
    label: item.itemName,
    value: item.itemName
  }))
}

async function searchElders(keyword: string) {
  await searchElderOptions(keyword)
}

async function submitCreate() {
  if (!createForm.elderId) {
    message.error('请选择老人')
    return
  }
  if (createForm.payableAmount < 0) {
    message.error('请输入有效金额')
    return
  }
  if (!createForm.feeItem) {
    message.error('请选择费用项')
    return
  }
  if (!createForm.dischargeFeeConfig) {
    message.error('请选择退住费用设置')
    return
  }
  creating.value = true
  try {
    await createDischargeFeeAudit({
      ...createForm,
      elderId: createForm.elderId,
      dischargeApplyId: createForm.dischargeApplyId || undefined,
      feeItem: createForm.feeItem,
      dischargeFeeConfig: createForm.dischargeFeeConfig
    })
    message.success('创建成功')
    createOpen.value = false
    fetchData()
  } finally {
    creating.value = false
  }
}

function review(record: DischargeFeeAuditItem, status: 'APPROVED' | 'REJECTED') {
  if (status === 'REJECTED') {
    let reviewRemark = ''
    Modal.confirm({
      title: '确认驳回该审核单？',
      okText: '确认驳回',
      okButtonProps: { danger: true },
      content: h(Input.TextArea, {
        rows: 4,
        placeholder: '请输入驳回原因（必填）',
        onChange: (event: any) => {
          reviewRemark = event?.target?.value || ''
        }
      }),
      onOk: async () => {
        if (!reviewRemark.trim()) {
          message.error('驳回时请填写审核备注')
          return Promise.reject()
        }
        await reviewDischargeFeeAudit(record.id, { status, reviewRemark: reviewRemark.trim() })
        message.success('已驳回')
        fetchData()
      }
    })
    return
  }
  Modal.confirm({
    title: '确认通过该审核单？',
    onOk: async () => {
      await reviewDischargeFeeAudit(record.id, { status })
      message.success(status === 'APPROVED' ? '审核通过，已自动生成结算单' : '已驳回')
      fetchData()
    }
  })
}

function riskText(item: DischargeFeeAuditItem) {
  const amount = Number(item.payableAmount || 0)
  if (item.status === 'PENDING' && amount >= 2000) return '高'
  if (item.status === 'PENDING') return '中'
  return '低'
}

function riskColor(item: DischargeFeeAuditItem) {
  const level = riskText(item)
  if (level === '高') return 'red'
  if (level === '中') return 'orange'
  return 'green'
}

onMounted(async () => {
  await loadOptions()
  await fetchData()
})
</script>
