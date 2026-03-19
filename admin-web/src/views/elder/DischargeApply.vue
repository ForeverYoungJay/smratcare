<template>
  <PageContainer title="退住申请" subTitle="退住流程前置申请与审核">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="老人姓名">
          <a-select
            v-model:value="query.elderId"
            allow-clear
            show-search
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            style="width: 220px"
            placeholder="请输入老人姓名/拼音首字母"
            @search="searchElders"
            @focus="() => !elderOptions.length && searchElders('')"
          />
        </a-form-item>
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allow-clear placeholder="请输入申请原因/审核备注" style="width: 280px" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 160px" placeholder="请选择状态">
            <a-select-option value="PENDING">待审核</a-select-option>
            <a-select-option value="APPROVED">已通过</a-select-option>
            <a-select-option value="REJECTED">已驳回</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="计划退住日期">
          <a-range-picker v-model:value="query.plannedDateRange" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">搜索</a-button>
            <a-button @click="reset">清空</a-button>
            <a-button @click="exportRows">导出</a-button>
            <a-button type="primary" @click="openCreate">新增申请</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-space style="margin-bottom: 12px" wrap>
        <a-tag color="blue">已选 {{ selectedRowKeys.length }} 条</a-tag>
        <a-button :disabled="!canReviewSelected" @click="reviewSelected('APPROVED')">审核</a-button>
        <a-button danger :disabled="!canReviewSelected" @click="reviewSelected('REJECTED')">驳回</a-button>
        <a-button :disabled="!canOpenFinanceAuditSelected" @click="openSelectedFinanceAudit">退住费用审核</a-button>
        <a-button :disabled="!canOpenSettlementSelected" @click="openSelectedSettlement">退院结算</a-button>
        <a-button danger :disabled="selectedRowKeys.length === 0" @click="deleteSelected">删除</a-button>
      </a-space>
      <a-table
        :data-source="rows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :row-selection="rowSelection"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'proofFileUrl'">
            <a v-if="record.proofFileUrl" :href="record.proofFileUrl" target="_blank" rel="noreferrer">查看附件</a>
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'autoDischargeStatus'">
            <a-tag v-if="record.autoDischargeStatus === 'PENDING_SETTLEMENT'" color="orange">待退院结算</a-tag>
            <a-tag v-else-if="record.autoDischargeStatus === 'SUCCESS'" color="green">成功</a-tag>
            <a-tag v-else-if="record.autoDischargeStatus === 'FAILED'" color="red">失败</a-tag>
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space wrap>
              <a-button type="link" :disabled="record.status !== 'PENDING'" @click="openReview(record, 'APPROVED')">审核</a-button>
              <a-button type="link" danger :disabled="record.status !== 'PENDING'" @click="openReview(record, 'REJECTED')">驳回</a-button>
              <a-button type="link" :disabled="record.status !== 'APPROVED'" @click="goToDischargeFeeAudit(record)">费用审核</a-button>
              <a-button type="link" :disabled="record.status !== 'APPROVED'" @click="goToDischargeSettlement(record)">退院结算</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>

    <a-modal v-model:open="createOpen" title="新增退住申请" @ok="submitCreate" :confirm-loading="submitting" width="520px">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="老人" name="elderId">
          <a-select
            v-model:value="form.elderId"
            show-search
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            placeholder="请输入老人姓名/拼音首字母"
            @search="searchElders"
            @focus="() => !elderOptions.length && searchElders('')"
          />
        </a-form-item>
        <a-form-item label="计划退住日期" name="plannedDischargeDate">
          <a-date-picker v-model:value="form.plannedDischargeDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item name="reason">
          <template #label><span class="required-label">申请原因</span></template>
          <a-textarea v-model:value="form.reason" :rows="4" placeholder="请手动填写退住申请原因" />
        </a-form-item>
        <a-form-item>
          <template #label><span class="required-label">证明附件</span></template>
          <a-space>
            <a-upload :show-upload-list="false" :before-upload="beforeUploadProof">
              <a-button :loading="proofUploading">上传附件</a-button>
            </a-upload>
            <a-typography-text type="secondary">{{ fileLabel(form.proofFileUrl, '未上传') }}</a-typography-text>
            <a-button v-if="form.proofFileUrl" type="link" @click="openFile(form.proofFileUrl)">查看</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="reviewOpen" :title="reviewStatus === 'APPROVED' ? '通过申请' : '驳回申请'" @ok="submitReview" :confirm-loading="reviewSubmitting" width="480px">
      <a-form layout="vertical">
        <a-form-item v-if="reviewStatus === 'APPROVED'" label="审核清单">
          <a-space direction="vertical" style="width: 100%">
            <a-checkbox v-model:checked="reviewChecklist.proofChecked">退住原因与附件已核验</a-checkbox>
            <a-checkbox v-model:checked="reviewChecklist.planChecked">已确认计划退住日期与沟通对象</a-checkbox>
            <a-checkbox v-model:checked="reviewChecklist.financeHandoffReady">确认通过后进入退住费用审核与退院结算</a-checkbox>
          </a-space>
        </a-form-item>
        <a-alert
          v-if="reviewStatus === 'APPROVED'"
          type="info"
          show-icon
          style="margin-bottom: 12px"
          message="申请审核通过后，不再直接退住；需先完成退住费用审核、退院结算，财务退款完成后系统自动释放床位并回写离院状态。"
        />
        <a-form-item label="审核备注">
          <a-textarea
            v-model:value="reviewRemark"
            :rows="3"
            :placeholder="reviewStatus === 'APPROVED' ? '可补充账单核对、家属沟通、特殊风险说明' : '驳回时请填写原因'"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { uploadElderFile } from '../../api/elder'
import { useElderOptions } from '../../composables/useElderOptions'
import {
  createDischargeApply,
  deleteDischargeApply,
  exportDischargeApply,
  getDischargeApplyPage,
  reviewDischargeApply
} from '../../api/elderResidence'
import type {
  DischargeApplyCreateRequest,
  DischargeApplyItem,
  DischargeApplyStatus,
  Id,
  PageResult
} from '../../types'
import { normalizeResidentId } from '../../utils/id'

const loading = ref(false)
const rows = ref<DischargeApplyItem[]>([])
const total = ref(0)
const createOpen = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const reviewOpen = ref(false)
const reviewSubmitting = ref(false)
const reviewId = ref<Id | undefined>(undefined)
const reviewStatus = ref<'APPROVED' | 'REJECTED'>('APPROVED')
const reviewRemark = ref('')
const reviewChecklist = reactive({
  proofChecked: true,
  planChecked: true,
  financeHandoffReady: true
})
const route = useRoute()
const router = useRouter()
const proofUploading = ref(false)
const { elderOptions, elderLoading, searchElders, ensureSelectedElder } = useElderOptions({ pageSize: 80 })
const selectedRowKeys = ref<Id[]>([])
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((key) => String(key)) as Id[]
  }
}))

const query = reactive({
  elderId: undefined as Id | undefined,
  keyword: undefined as string | undefined,
  status: undefined as DischargeApplyStatus | undefined,
  plannedDateRange: undefined as [string, string] | undefined,
  pageNo: 1,
  pageSize: 10
})

const form = reactive<DischargeApplyCreateRequest>({
  elderId: '' as Id,
  plannedDischargeDate: '',
  reason: '',
  proofFileUrl: ''
})

const rules: FormRules = {
  elderId: [{ required: true, message: '请选择老人' }],
  plannedDischargeDate: [{ required: true, message: '请选择计划退住日期' }],
  reason: [{ required: true, message: '请输入申请原因' }]
}

const columns = [
  { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '申请日期', dataIndex: 'applyDate', key: 'applyDate', width: 120 },
  { title: '计划退住日期', dataIndex: 'plannedDischargeDate', key: 'plannedDischargeDate', width: 120 },
  { title: '申请原因', dataIndex: 'reason', key: 'reason' },
  { title: '证明附件', dataIndex: 'proofFileUrl', key: 'proofFileUrl', width: 180 },
  { title: '后续流程状态', key: 'autoDischargeStatus', width: 120 },
  { title: '后续流程说明', dataIndex: 'autoDischargeMessage', key: 'autoDischargeMessage', width: 260 },
  { title: '关联退住单号', dataIndex: 'linkedDischargeId', key: 'linkedDischargeId', width: 120 },
  { title: '审核人', dataIndex: 'reviewedByName', key: 'reviewedByName', width: 100 },
  { title: '审核备注', dataIndex: 'reviewRemark', key: 'reviewRemark' },
  { title: '审核状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 260 }
]
const selectedRecords = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(item.id)))
const canReviewSelected = computed(
  () => selectedRecords.value.length === 1 && selectedRecords.value[0]?.status === 'PENDING'
)
const canOpenFinanceAuditSelected = computed(
  () => selectedRecords.value.length === 1 && selectedRecords.value[0]?.status === 'APPROVED'
)
const canOpenSettlementSelected = computed(
  () => selectedRecords.value.length === 1 && selectedRecords.value[0]?.status === 'APPROVED'
)

async function fetchData() {
  loading.value = true
  try {
    const [plannedDateFrom, plannedDateTo] = query.plannedDateRange || []
    const res: PageResult<DischargeApplyItem> = await getDischargeApplyPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      elderId: query.elderId,
      keyword: query.keyword,
      status: query.status,
      plannedDateFrom,
      plannedDateTo
    })
    rows.value = res.list
    total.value = res.total
    selectedRowKeys.value = selectedRowKeys.value.filter((id) => rows.value.some((item) => item.id === id))
  } finally {
    loading.value = false
  }
}

function reset() {
  query.elderId = undefined
  query.keyword = undefined
  query.status = undefined
  query.plannedDateRange = undefined
  query.pageNo = 1
  fetchData()
}

function openCreate() {
  createOpen.value = true
  form.elderId = '' as Id
  form.plannedDischargeDate = ''
  form.reason = ''
  form.proofFileUrl = ''
}

function tryOpenCreateFromRoute() {
  const elderId = normalizeResidentId(route.query as Record<string, unknown>)
  const openCreateFlag = String(route.query.openCreate || '') === '1'
  if (!elderId) return
  ensureSelectedElder(elderId)
  query.elderId = elderId
  if (!openCreateFlag) return
  createOpen.value = true
  form.elderId = elderId
  form.plannedDischargeDate = dayjs().format('YYYY-MM-DD')
  form.reason = ''
  form.proofFileUrl = ''
}

async function beforeUploadProof(file: File) {
  proofUploading.value = true
  try {
    const uploaded = await uploadElderFile(file, 'elder-discharge-apply-proof')
    const url = uploaded?.fileUrl || ''
    if (!url) {
      message.error('上传失败：未返回文件地址')
      return false
    }
    form.proofFileUrl = url
    message.success('证明附件上传成功')
  } catch (error: any) {
    message.error(error?.message || '上传失败')
  } finally {
    proofUploading.value = false
  }
  return false
}

function fileLabel(url?: string, fallback = '') {
  if (!url) return fallback
  const text = String(url).split('/').pop() || url
  return decodeURIComponent(text)
}

function openFile(url?: string) {
  if (!url) return
  window.open(url, '_blank')
}

async function submitCreate() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    await createDischargeApply(form)
    message.success('退住申请已提交')
    createOpen.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

function openReview(record: DischargeApplyItem, status: 'APPROVED' | 'REJECTED') {
  reviewId.value = record.id
  reviewStatus.value = status
  reviewRemark.value = ''
  reviewChecklist.proofChecked = true
  reviewChecklist.planChecked = true
  reviewChecklist.financeHandoffReady = true
  reviewOpen.value = true
}

function reviewSelected(status: 'APPROVED' | 'REJECTED') {
  const record = selectedRecords.value[0]
  if (!record) return
  openReview(record, status)
}

async function submitReview() {
  if (!reviewId.value) return
  if (reviewStatus.value === 'APPROVED') {
    const checklistPassed = reviewChecklist.proofChecked && reviewChecklist.planChecked && reviewChecklist.financeHandoffReady
    if (!checklistPassed) {
      message.warning('通过前请完成审核清单确认')
      return
    }
  }
  if (reviewStatus.value === 'REJECTED' && !reviewRemark.value.trim()) {
    message.warning('驳回时请填写审核备注')
    return
  }
  reviewSubmitting.value = true
  try {
    const approvalNotes = reviewStatus.value === 'APPROVED'
      ? ['资料已核验', '计划日期已确认', '已移交退住费用审核/退院结算']
      : []
    const mergedReviewRemark = [...approvalNotes, reviewRemark.value.trim()].filter(Boolean).join('；')
    await reviewDischargeApply(reviewId.value, {
      status: reviewStatus.value,
      reviewRemark: mergedReviewRemark || undefined
    })
    if (reviewStatus.value === 'APPROVED') {
      message.success('审核通过，已进入退住费用审核与退院结算阶段')
    } else {
      message.success('审核完成')
    }
    reviewOpen.value = false
    fetchData()
  } catch (error: any) {
    const msg = error?.msg || error?.message || '审核失败，请重试'
    message.error(msg)
    fetchData()
  } finally {
    reviewSubmitting.value = false
  }
}

function statusText(status?: string) {
  if (status === 'APPROVED') return '已通过'
  if (status === 'REJECTED') return '已驳回'
  return '待审核'
}

function statusColor(status?: string) {
  if (status === 'APPROVED') return 'green'
  if (status === 'REJECTED') return 'red'
  return 'orange'
}

function goToDischargeFeeAudit(record: DischargeApplyItem) {
  router.push({
    path: '/finance/discharge/review',
    query: {
      elderId: String(record.elderId || ''),
      elderName: String(record.elderName || ''),
      dischargeApplyId: String(record.id || ''),
      openCreate: '1'
    }
  })
}

function goToDischargeSettlement(record: DischargeApplyItem) {
  router.push({
    path: '/finance/discharge/settlement',
    query: {
      elderId: String(record.elderId || ''),
      elderName: String(record.elderName || ''),
      dischargeApplyId: String(record.id || ''),
      openCreate: '1'
    }
  })
}

function openSelectedFinanceAudit() {
  const record = selectedRecords.value[0]
  if (!record) return
  goToDischargeFeeAudit(record)
}

function openSelectedSettlement() {
  const record = selectedRecords.value[0]
  if (!record) return
  goToDischargeSettlement(record)
}

async function exportRows() {
  const [plannedDateFrom, plannedDateTo] = query.plannedDateRange || []
  await exportDischargeApply({
    elderId: query.elderId,
    keyword: query.keyword,
    status: query.status,
    plannedDateFrom,
    plannedDateTo
  })
}

function deleteSelected() {
  if (!selectedRowKeys.value.length) return
  Modal.confirm({
    title: '确认删除退住申请？',
    content: `将删除 ${selectedRowKeys.value.length} 条申请记录（已通过申请不可删除）`,
    okText: '确认删除',
    okType: 'danger',
    async onOk() {
      await Promise.all(selectedRowKeys.value.map((id) => deleteDischargeApply(id)))
      message.success('删除成功')
      selectedRowKeys.value = []
      await fetchData()
    }
  })
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

onMounted(async () => {
  await searchElders('')
  tryOpenCreateFromRoute()
  await fetchData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}

.required-label::before {
  content: "*";
  color: #ff4d4f;
  margin-right: 4px;
  font-weight: 700;
}
</style>
