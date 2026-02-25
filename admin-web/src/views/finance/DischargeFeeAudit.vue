<template>
  <PageContainer title="退住费用审核" subTitle="审核通过后自动生成退住结算单">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" allow-clear placeholder="老人/费用项/备注" style="width: 220px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 180px" :options="statusOptions" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新建审核单</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 'APPROVED' ? 'green' : record.status === 'REJECTED' ? 'red' : 'orange'">{{ record.status }}</a-tag>
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
          <a-select v-model:value="createForm.elderId" show-search :filter-option="false" :options="elderOptions" @search="searchElders" />
        </a-form-item>
        <a-form-item label="退住申请ID">
          <a-input-number v-model:value="createForm.dischargeApplyId" :min="1" style="width: 100%" />
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
import { getBaseConfigItemList } from '../../api/baseConfig'
import { getElderPage } from '../../api/elder'
import { createDischargeFeeAudit, getDischargeFeeAuditPage, reviewDischargeFeeAudit } from '../../api/financeFee'
import type { BaseConfigItem, DischargeFeeAuditItem, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<DischargeFeeAuditItem[]>([])
const query = reactive({ pageNo: 1, pageSize: 10, status: undefined as string | undefined, keyword: '' })
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
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '审核备注', dataIndex: 'auditRemark', key: 'auditRemark' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 140 }
]

const createOpen = ref(false)
const creating = ref(false)
const elderOptions = ref<{ label: string; value: number }[]>([])
const feeItemOptions = ref<{ label: string; value: string }[]>([])
const dischargeFeeConfigOptions = ref<{ label: string; value: string }[]>([])
const createForm = reactive({
  elderId: undefined as number | undefined,
  dischargeApplyId: undefined as number | undefined,
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
  query.keyword = ''
  pagination.current = 1
  fetchData()
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
  if (!keyword) {
    elderOptions.value = []
    return
  }
  const res = await getElderPage({ pageNo: 1, pageSize: 20, keyword })
  elderOptions.value = res.list.map((item: any) => ({ label: item.fullName || item.name || '未知老人', value: item.id }))
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

onMounted(async () => {
  await loadOptions()
  await fetchData()
})
</script>
