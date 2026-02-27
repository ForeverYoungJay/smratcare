<template>
  <PageContainer title="审批流程" subTitle="请假/加班/报销/采购/收入证明/物资申请/用章审批">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="标题/申请人/备注" allow-clear style="width: 240px" />
      </a-form-item>
      <a-form-item label="类型">
        <a-select v-model:value="query.type" :options="typeOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增审批</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" @click="batchApprove">批量通过</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" @click="batchReject">批量驳回</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchRemove">批量删除</a-button>
        <a-button @click="downloadExport">导出CSV</a-button>
      </template>
    </SearchForm>

    <StatefulBlock :loading="summaryLoading" :error="summaryError" :empty="false" @retry="fetchData">
      <a-row :gutter="[12, 12]" style="margin-bottom: 12px">
        <a-col :xs="12" :lg="3"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="总单数" :value="summary.totalCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="3"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="待审批" :value="summary.pendingCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="3"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="已通过" :value="summary.approvedCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="3"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="已驳回" :value="summary.rejectedCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="3"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="超时待审" :value="summary.timeoutPendingCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="3"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="请假待审" :value="summary.leavePendingCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="3"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="报销待审" :value="summary.reimbursePendingCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="3"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="采购待审" :value="summary.purchasePendingCount || 0" /></a-card></a-col>
      </a-row>
      <a-alert
        v-if="(summary.timeoutPendingCount || 0) > 0"
        type="warning"
        show-icon
        style="margin-bottom: 12px"
        :message="`审批提醒：存在 ${summary.timeoutPendingCount || 0} 条超时待审批单。`"
      />
    </StatefulBlock>

    <StatefulBlock :loading="loading" :error="tableError" :empty="!loading && !tableError && rows.length === 0" empty-text="暂无审批记录" @retry="fetchData">
      <DataTable
        rowKey="id"
        :row-selection="rowSelection"
        :columns="columns"
        :data-source="rows"
        :loading="false"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'APPROVED' ? 'green' : record.status === 'REJECTED' ? 'red' : 'orange'">
              {{ statusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" :disabled="record.status !== 'PENDING'" @click="openEdit(record)">编辑</a-button>
              <a-button type="link" :disabled="record.status !== 'PENDING'" @click="approve(record)">通过</a-button>
              <a-button type="link" :disabled="record.status !== 'PENDING'" @click="reject(record)">驳回</a-button>
              <a-button type="link" danger @click="remove(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </DataTable>
    </StatefulBlock>

    <a-modal v-model:open="editOpen" title="审批" @ok="submit" :confirm-loading="saving" width="700px">
      <a-form layout="vertical">
        <a-form-item label="类型" required>
          <a-select v-model:value="form.approvalType" :options="typeOptions" />
        </a-form-item>
        <a-form-item label="标题" required>
          <a-input v-model:value="form.title" />
        </a-form-item>
        <a-form-item label="申请人" required>
          <a-input v-model:value="form.applicantName" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="金额">
              <a-input-number v-model:value="form.amount" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="form.status" :options="editableStatusOptions" disabled />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始时间">
              <a-date-picker v-model:value="form.startTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束时间">
              <a-date-picker v-model:value="form.endTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <template v-if="form.approvalType === 'LEAVE'">
          <a-alert
            type="info"
            show-icon
            message="管理制度：龟峰颐养中心请假申请须知"
            style="margin-bottom: 12px"
          />
          <a-card size="small" class="leave-policy-card" style="margin-bottom: 12px">
            <a-typography-paragraph>
              为规范管理，员工请假须提前在系统提交申请，注明事由及起止时间，经审批通过后方可休假。3天以内由部门主管审批，3天以上报中心负责人审批。紧急情况应先行报备，并于返岗后1个工作日内补办手续。
            </a-typography-paragraph>
            <a-typography-paragraph>
              事假须提供相关证明材料；病假须提交三级甲等及以上医院出具的就诊病例或医嘱单；婚假须为在职期间依法登记结婚，并提供结婚证复印件。产假、护理假按国家及地方规定执行，需提交医院证明；直系亲属去世可申请丧假，并提供相关证明材料。
            </a-typography-paragraph>
            <a-typography-paragraph>
              所有材料须真实有效，未履行审批手续擅自离岗者按旷工处理。请提前做好工作交接。
            </a-typography-paragraph>
          </a-card>
          <a-space style="margin-bottom: 10px" wrap>
            <a-button size="small" @click="openPolicy('/oa/knowledge?category=制度规范')">查看管理制度</a-button>
            <a-button size="small" @click="openPolicy('/oa/document?folder=请假制度')">查看制度文档</a-button>
          </a-space>
          <a-form-item label="请假证明上传">
            <a-upload :show-upload-list="false" :before-upload="beforeUploadLeaveProof">
              <a-button :loading="proofUploading">上传证明</a-button>
            </a-upload>
            <div v-if="leaveProofs.length" class="proof-list">
              <div v-for="(file, idx) in leaveProofs" :key="`${file.url}-${idx}`" class="proof-item">
                <a :href="file.url" target="_blank" rel="noopener noreferrer">{{ file.name }}</a>
                <a-button type="link" danger size="small" @click="removeLeaveProof(idx)">删除</a-button>
              </div>
            </div>
          </a-form-item>
          <a-form-item>
            <a-checkbox v-model:checked="leavePolicyAcknowledged">
              我已阅读并同意制度
            </a-checkbox>
          </a-form-item>
        </template>
        <a-form-item label="表单数据(JSON)">
          <a-textarea v-model:value="form.formData" :rows="3" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import {
  getApprovalSummary,
  getApprovalPage,
  createApproval,
  updateApproval,
  approveApproval,
  rejectApproval,
  deleteApproval,
  batchApproveApproval,
  batchRejectApproval,
  batchDeleteApproval,
  exportApproval,
  uploadOaFile
} from '../../api/oa'
import type { OaApproval, OaApprovalSummary, PageResult } from '../../types'

const loading = ref(false)
const tableError = ref('')
const summaryLoading = ref(false)
const summaryError = ref('')
const rows = ref<OaApproval[]>([])
const route = useRoute()
const query = reactive({
  keyword: '',
  type: undefined as string | undefined,
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<number[]>([])
const summary = reactive<OaApprovalSummary>({
  totalCount: 0,
  pendingCount: 0,
  approvedCount: 0,
  rejectedCount: 0,
  timeoutPendingCount: 0,
  leavePendingCount: 0,
  reimbursePendingCount: 0,
  purchasePendingCount: 0
})

const columns = [
  { title: '类型', dataIndex: 'approvalType', key: 'approvalType', width: 120 },
  { title: '标题', dataIndex: 'title', key: 'title', width: 200 },
  { title: '申请人', dataIndex: 'applicantName', key: 'applicantName', width: 120 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 220 }
]

const editOpen = ref(false)
const saving = ref(false)
const proofUploading = ref(false)
const leaveProofs = ref<Array<{ name: string; url: string }>>([])
const leavePolicyAcknowledged = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  approvalType: 'LEAVE',
  title: '',
  applicantName: '',
  amount: undefined as number | undefined,
  startTime: undefined as any,
  endTime: undefined as any,
  formData: '',
  status: 'PENDING',
  remark: ''
})

const typeOptions = [
  { label: '请假', value: 'LEAVE' },
  { label: '加班', value: 'OVERTIME' },
  { label: '报销', value: 'REIMBURSE' },
  { label: '采购', value: 'PURCHASE' },
  { label: '收入证明', value: 'INCOME_PROOF' },
  { label: '物资申领', value: 'MATERIAL_APPLY' },
  { label: '用章申请', value: 'OFFICIAL_SEAL' }
]
const statusOptions = [
  { label: '待审批', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]
const editableStatusOptions = [{ label: '待审批', value: 'PENDING' }]
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => Number(item))
  }
}))

function statusLabel(status?: string) {
  if (status === 'APPROVED') return '已通过'
  if (status === 'REJECTED') return '已驳回'
  return '待审批'
}

async function fetchData() {
  loading.value = true
  summaryLoading.value = true
  tableError.value = ''
  summaryError.value = ''
  try {
    const params = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status,
      type: query.type,
      keyword: query.keyword || undefined
    }
    const [res, sum] = await Promise.all([
      getApprovalPage(params),
      getApprovalSummary(params)
    ])
    rows.value = res.list
    pagination.total = res.total || res.list.length
    selectedRowKeys.value = []
    Object.assign(summary, sum || {})
  } catch (error: any) {
    const text = error?.message || '加载失败，请稍后重试'
    tableError.value = text
    summaryError.value = text
  } finally {
    loading.value = false
    summaryLoading.value = false
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
  query.status = undefined
  query.type = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.approvalType = (query.type || 'LEAVE') as string
  form.title = ''
  form.applicantName = ''
  form.amount = undefined
  form.startTime = undefined
  form.endTime = undefined
  form.formData = ''
  form.status = 'PENDING'
  form.remark = ''
  leaveProofs.value = []
  leavePolicyAcknowledged.value = false
  editOpen.value = true
}

function openEdit(record: OaApproval) {
  form.id = record.id
  form.approvalType = record.approvalType
  form.title = record.title
  form.applicantName = record.applicantName
  form.amount = record.amount
  form.startTime = record.startTime ? dayjs(record.startTime) : undefined
  form.endTime = record.endTime ? dayjs(record.endTime) : undefined
  form.formData = record.formData || ''
  form.status = record.status || 'PENDING'
  form.remark = record.remark || ''
  leaveProofs.value = extractLeaveProofs(record.formData)
  leavePolicyAcknowledged.value = extractPolicyAcknowledged(record.formData)
  editOpen.value = true
}

async function submit() {
  if (form.approvalType === 'LEAVE' && !leavePolicyAcknowledged.value) {
    message.warning('请先勾选“我已阅读并同意制度”')
    return
  }
  const formDataForSubmit = mergeLeaveProofsIntoFormData(form.formData)
  const payload = {
    approvalType: form.approvalType,
    title: form.title,
    applicantName: form.applicantName,
    amount: form.amount,
    startTime: form.startTime ? dayjs(form.startTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
    endTime: form.endTime ? dayjs(form.endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
    formData: formDataForSubmit,
    status: form.status,
    remark: form.remark
  }
  saving.value = true
  try {
    if (form.id) {
      await updateApproval(form.id, payload)
    } else {
      await createApproval(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

function extractLeaveProofs(formData?: string) {
  if (!formData) return []
  try {
    const parsed = JSON.parse(formData)
    const proofs = Array.isArray(parsed?.leaveProofs) ? parsed.leaveProofs : []
    return proofs
      .filter((item: any) => item && item.url)
      .map((item: any) => ({ name: String(item.name || '请假证明'), url: String(item.url) }))
  } catch {
    return []
  }
}

function extractPolicyAcknowledged(formData?: string) {
  if (!formData) return false
  try {
    const parsed = JSON.parse(formData)
    return Boolean(parsed?.policyAcknowledged)
  } catch {
    return false
  }
}

function mergeLeaveProofsIntoFormData(raw?: string) {
  if (form.approvalType !== 'LEAVE') {
    return raw
  }
  let parsed: Record<string, any> = {}
  if (raw && raw.trim()) {
    try {
      parsed = JSON.parse(raw)
    } catch {
      parsed = { rawText: raw }
    }
  }
  parsed.leaveProofs = leaveProofs.value
  parsed.policyAcknowledged = leavePolicyAcknowledged.value
  return JSON.stringify(parsed)
}

async function beforeUploadLeaveProof(file: File) {
  proofUploading.value = true
  try {
    const uploaded = await uploadOaFile(file, 'oa-leave-proof')
    const url = uploaded?.fileUrl || ''
    if (!url) {
      message.error('上传失败：未返回文件地址')
      return false
    }
    leaveProofs.value.push({
      name: uploaded.originalFileName || uploaded.fileName || file.name,
      url
    })
    message.success('证明上传成功')
  } catch (error: any) {
    message.error(error?.message || '证明上传失败')
  } finally {
    proofUploading.value = false
  }
  return false
}

function removeLeaveProof(index: number) {
  leaveProofs.value.splice(index, 1)
}

function openPolicy(path: string) {
  window.open(path, '_blank')
}

async function approve(record: OaApproval) {
  if (record.status !== 'PENDING') return
  await approveApproval(record.id)
  fetchData()
}

async function reject(record: OaApproval) {
  if (record.status !== 'PENDING') return
  const remark = window.prompt('请输入驳回原因', '未通过')
  if (remark === null) return
  await rejectApproval(record.id, remark)
  fetchData()
}

async function remove(record: OaApproval) {
  await deleteApproval(record.id)
  fetchData()
}

async function batchApprove() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchApproveApproval(selectedRowKeys.value)
  message.success(`批量通过完成，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchReject() {
  if (selectedRowKeys.value.length === 0) return
  const remark = window.prompt('请输入批量驳回原因', '批量未通过')
  if (remark === null) return
  const affected = await batchRejectApproval(selectedRowKeys.value, remark)
  message.success(`批量驳回完成，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchDeleteApproval(selectedRowKeys.value)
  message.success(`批量删除完成，共处理 ${affected || 0} 条`)
  fetchData()
}

async function downloadExport() {
  const blob = await exportApproval({
    keyword: query.keyword || undefined,
    type: query.type,
    status: query.status
  })
  const href = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = href
  link.download = `oa-approval-${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(href)
}

fetchData()

onMounted(() => {
  const type = String(route.query.type || '').toUpperCase()
  const quick = String(route.query.quick || '')
  if (type && typeOptions.some((item) => item.value === type)) {
    query.type = type
    form.approvalType = type
  }
  if (quick === '1') {
    openCreate()
  }
})
</script>

<style scoped>
.proof-list {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.leave-policy-card :deep(.ant-typography) {
  margin-bottom: 8px;
}

.proof-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 4px 8px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
}
</style>
