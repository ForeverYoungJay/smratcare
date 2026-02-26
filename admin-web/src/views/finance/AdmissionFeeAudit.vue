<template>
  <PageContainer title="入住费用审核" subTitle="登记并审核入住费用与押金信息">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" allow-clear placeholder="老人姓名/备注" style="width: 220px" />
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

    <a-modal v-model:open="createOpen" title="新建入住费用审核" :confirm-loading="creating" @ok="submitCreate">
      <a-form layout="vertical">
        <a-form-item label="老人" required>
          <a-select v-model:value="createForm.elderId" show-search :filter-option="false" :options="elderOptions" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
        </a-form-item>
        <a-form-item label="入院记录ID">
          <a-input-number v-model:value="createForm.admissionId" :min="1" style="width: 100%" />
        </a-form-item>
        <a-form-item label="费用总额" required>
          <a-input-number v-model:value="createForm.totalAmount" :min="0.01" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="押金金额" required>
          <a-input-number v-model:value="createForm.depositAmount" :min="0" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="createForm.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { h, reactive, ref } from 'vue'
import { Input, message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { createAdmissionFeeAudit, getAdmissionFeeAuditPage, reviewAdmissionFeeAudit } from '../../api/financeFee'
import type { AdmissionFeeAuditItem, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<AdmissionFeeAuditItem[]>([])
const query = reactive({ pageNo: 1, pageSize: 10, status: undefined as string | undefined, keyword: '' })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const statusOptions = [
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]
const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '费用总额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120 },
  { title: '押金金额', dataIndex: 'depositAmount', key: 'depositAmount', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '审核备注', dataIndex: 'auditRemark', key: 'auditRemark' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 140 }
]

const createOpen = ref(false)
const creating = ref(false)
const { elderOptions, searchElders: searchElderOptions } = useElderOptions({ pageSize: 20 })
const createForm = reactive({
  elderId: undefined as number | undefined,
  admissionId: undefined as number | undefined,
  totalAmount: 0,
  depositAmount: 0,
  remark: ''
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<AdmissionFeeAuditItem> = await getAdmissionFeeAuditPage({
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
  createForm.admissionId = undefined
  createForm.totalAmount = 0
  createForm.depositAmount = 0
  createForm.remark = ''
  createOpen.value = true
}

async function searchElders(keyword: string) {
  await searchElderOptions(keyword)
}

async function submitCreate() {
  if (!createForm.elderId) {
    message.error('请选择老人')
    return
  }
  if (!createForm.totalAmount || createForm.totalAmount <= 0) {
    message.error('请输入有效费用总额')
    return
  }
  creating.value = true
  try {
    await createAdmissionFeeAudit({ ...createForm, elderId: createForm.elderId })
    message.success('创建成功')
    createOpen.value = false
    fetchData()
  } finally {
    creating.value = false
  }
}

function review(record: AdmissionFeeAuditItem, status: 'APPROVED' | 'REJECTED') {
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
        await reviewAdmissionFeeAudit(record.id, { status, reviewRemark: reviewRemark.trim() })
        message.success('已驳回')
        fetchData()
      }
    })
    return
  }
  Modal.confirm({
    title: '确认通过该审核单？',
    onOk: async () => {
      await reviewAdmissionFeeAudit(record.id, { status })
      message.success('操作成功')
      fetchData()
    }
  })
}

fetchData()
</script>
