<template>
  <PageContainer title="合同变更" sub-title="房号、到期日、备注等变更与审批流转">
    <MarketingQuickNav parent-path="/marketing/contracts" />
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline">
        <a-form-item label="合同编号">
          <a-input v-model:value="query.contractNo" allow-clear />
        </a-form-item>
        <a-form-item label="长者姓名">
          <a-input v-model:value="query.elderName" allow-clear />
        </a-form-item>
        <a-form-item label="审批状态">
          <a-select v-model:value="query.status" allow-clear style="width: 150px">
            <a-select-option value="PENDING_APPROVAL">待审批</a-select-option>
            <a-select-option value="APPROVED">已通过</a-select-option>
            <a-select-option value="REJECTED">已驳回</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <MarketingListToolbar :selected-count="selectedRowKeys.length" tip="支持批量删除与批量处理">
        <a-space>
          <a-button :disabled="!selectedSingleRecord" @click="openEditSelected">编辑变更</a-button>
          <a-button :disabled="!selectedSingleRecord" @click="startChangeSelected">发起变更</a-button>
          <a-button :disabled="!selectedSingleRecord" @click="submitApprovalSelected">提交审批</a-button>
          <a-button :disabled="selectedRowKeys.length === 0" @click="batchSubmitApproval">批量提交审批</a-button>
          <a-button :disabled="!selectedSingleRecord" @click="approveSelected">审批通过</a-button>
          <a-button :disabled="selectedRowKeys.length === 0" @click="batchApprove">批量审批通过</a-button>
          <a-button :disabled="!selectedSingleRecord" danger @click="rejectSelected">审批驳回</a-button>
          <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchReject">批量审批驳回</a-button>
        </a-space>
      </MarketingListToolbar>
      <a-table
        :columns="columns"
        :data-source="rows"
        :loading="loading"
        row-key="id"
        :row-selection="rowSelection"
        :pagination="false"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
          </template>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 16px; text-align: right"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>

    <a-drawer v-model:open="editOpen" title="编辑合同变更" width="560">
      <a-form layout="vertical">
        <a-form-item label="合同编号">
          <a-input v-model:value="editForm.contractNo" disabled />
        </a-form-item>
        <a-form-item label="长者姓名">
          <a-input v-model:value="editForm.elderName" disabled />
        </a-form-item>
        <a-form-item label="签约房号">
          <a-input v-model:value="editForm.reservationRoomNo" />
        </a-form-item>
        <a-form-item label="到期日期">
          <a-date-picker v-model:value="editForm.contractExpiryDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="变更说明">
          <a-textarea v-model:value="editForm.remark" :rows="4" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="editOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="saveEdit">保存变更</a-button>
        </a-space>
      </template>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import MarketingQuickNav from './components/MarketingQuickNav.vue'
import MarketingListToolbar from './components/MarketingListToolbar.vue'
import { getContractPage, updateCrmContract } from '../../api/marketing'
import type { CrmContractItem, PageResult } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<CrmContractItem[]>([])
const total = ref(0)
const selectedRowKeys = ref<string[]>([])
const query = reactive({
  contractNo: '',
  elderName: '',
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '合同编号', dataIndex: 'contractNo', key: 'contractNo', width: 180 },
  { title: '长者姓名', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '签约房号', dataIndex: 'reservationRoomNo', key: 'reservationRoomNo', width: 140 },
  { title: '到期日期', dataIndex: 'contractExpiryDate', key: 'contractExpiryDate', width: 140 },
  { title: '审批状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '合同状态', dataIndex: 'contractStatus', key: 'contractStatus', width: 140 },
  { title: '变更备注', dataIndex: 'remark', key: 'remark' }
]

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
  }
}))
const selectedRecords = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(String(item.id))))
const selectedSingleRecord = computed(() => (selectedRecords.value.length === 1 ? selectedRecords.value[0] : null))

const editOpen = ref(false)
const editForm = reactive<Partial<CrmContractItem>>({})

function statusText(status?: string) {
  if (status === 'PENDING_APPROVAL') return '待审批'
  if (status === 'APPROVED') return '已通过'
  if (status === 'REJECTED') return '已驳回'
  if (status === 'SIGNED') return '已签署'
  return status || '-'
}

function statusColor(status?: string) {
  if (status === 'PENDING_APPROVAL') return 'gold'
  if (status === 'APPROVED' || status === 'SIGNED') return 'green'
  if (status === 'REJECTED') return 'red'
  return 'default'
}

async function fetchData() {
  loading.value = true
  try {
    const page: PageResult<CrmContractItem> = await getContractPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      contractNo: query.contractNo || undefined,
      elderName: query.elderName || undefined,
      status: query.status || undefined
    })
    rows.value = (page.list || []).map((item) => ({ ...item, id: String(item.id) }))
    total.value = page.total || 0
    selectedRowKeys.value = []
  } finally {
    loading.value = false
  }
}

function requireSingle(action: string) {
  if (!selectedSingleRecord.value) {
    message.info(`请先勾选 1 条合同后再${action}`)
    return null
  }
  return selectedSingleRecord.value
}

function openEditSelected() {
  const record = requireSingle('编辑')
  if (!record) return
  Object.assign(editForm, record)
  editOpen.value = true
}

async function saveEdit() {
  if (!editForm.id) return
  saving.value = true
  try {
    await updateCrmContract(String(editForm.id), { ...editForm })
    message.success('变更保存成功')
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function startChangeSelected() {
  const record = requireSingle('发起变更')
  if (!record) return
  await updateCrmContract(String(record.id), {
    ...record,
    contractStatus: '变更中',
    remark: `${record.remark || ''} [变更发起]`.trim()
  })
  message.success('已发起合同变更')
  fetchData()
}

async function submitApprovalSelected() {
  const record = requireSingle('提交审批')
  if (!record) return
  await updateCrmContract(String(record.id), {
    ...record,
    status: 'PENDING_APPROVAL',
    contractStatus: '变更待审批',
    remark: `${record.remark || ''} [已提交审批]`.trim()
  })
  message.success('已提交审批')
  fetchData()
}

async function approveSelected() {
  const record = requireSingle('审批通过')
  if (!record) return
  await updateCrmContract(String(record.id), {
    ...record,
    status: 'APPROVED',
    contractStatus: '变更已生效',
    remark: `${record.remark || ''} [审批通过]`.trim()
  })
  message.success('审批通过')
  fetchData()
}

async function rejectSelected() {
  const record = requireSingle('审批驳回')
  if (!record) return
  await updateCrmContract(String(record.id), {
    ...record,
    status: 'REJECTED',
    contractStatus: '变更驳回',
    remark: `${record.remark || ''} [审批驳回]`.trim()
  })
  message.success('已驳回')
  fetchData()
}

async function batchSetStatus(
  status: CrmContractItem['status'],
  contractStatus: string,
  remarkTag: string,
  actionText: string
) {
  if (!selectedRecords.value.length) {
    message.info(`请先勾选合同后再${actionText}`)
    return
  }
  const targets = selectedRecords.value.filter((item) => String(item.status || '') !== String(status || ''))
  if (!targets.length) {
    message.info(`选中合同状态与目标状态一致，无需${actionText}`)
    return
  }
  await Promise.all(
    targets.map((item) =>
      updateCrmContract(String(item.id), {
        ...item,
        status,
        contractStatus,
        remark: `${item.remark || ''} ${remarkTag}`.trim()
      })
    )
  )
  message.success(`${actionText}完成：共处理 ${targets.length} 条`)
  fetchData()
}

function batchSubmitApproval() {
  batchSetStatus('PENDING_APPROVAL', '变更待审批', '[已提交审批]', '批量提交审批')
}

function batchApprove() {
  batchSetStatus('APPROVED', '变更已生效', '[审批通过]', '批量审批通过')
}

function batchReject() {
  batchSetStatus('REJECTED', '变更驳回', '[审批驳回]', '批量审批驳回')
}

function reset() {
  query.contractNo = ''
  query.elderName = ''
  query.status = undefined
  query.pageNo = 1
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(_current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

fetchData()
</script>
