<template>
  <PageContainer title="退住申请" subTitle="退住流程前置申请与审核">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="老人姓名">
          <a-select v-model:value="query.elderId" allow-clear style="width: 180px" placeholder="请选择老人姓名">
            <a-select-option v-for="item in elders" :key="item.id" :value="item.id">{{ item.fullName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allow-clear placeholder="请输入老人姓名/申请原因/审核备注" style="width: 280px" />
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
            <a-button type="primary" ghost @click="openCreate">新增申请</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-table :data-source="rows" :columns="columns" :loading="loading" :pagination="false" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'autoDischargeStatus'">
            <a-tag v-if="record.autoDischargeStatus === 'SUCCESS'" color="green">成功</a-tag>
            <a-tag v-else-if="record.autoDischargeStatus === 'FAILED'" color="red">失败</a-tag>
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" :disabled="record.status !== 'PENDING'" @click="openReview(record, 'APPROVED')">通过</a-button>
              <a-button type="link" danger :disabled="record.status !== 'PENDING'" @click="openReview(record, 'REJECTED')">驳回</a-button>
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
          <a-select v-model:value="form.elderId" placeholder="请选择老人">
            <a-select-option v-for="item in elders" :key="item.id" :value="item.id">{{ item.fullName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="计划退住日期" name="plannedDischargeDate">
          <a-date-picker v-model:value="form.plannedDischargeDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="申请原因" name="reason">
          <a-select v-model:value="form.reason" placeholder="请选择退住费用设置">
            <a-select-option v-for="item in dischargeFeeConfigOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="reviewOpen" :title="reviewStatus === 'APPROVED' ? '通过申请' : '驳回申请'" @ok="submitReview" :confirm-loading="reviewSubmitting" width="480px">
      <a-form layout="vertical">
        <a-form-item label="审核备注">
          <a-textarea v-model:value="reviewRemark" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getBaseConfigItemList } from '../../api/baseConfig'
import { getElderPage } from '../../api/elder'
import { createDischargeApply, exportDischargeApply, getDischargeApplyPage, reviewDischargeApply } from '../../api/elderResidence'
import type {
  BaseConfigItem,
  DischargeApplyCreateRequest,
  DischargeApplyItem,
  DischargeApplyStatus,
  ElderItem,
  PageResult
} from '../../types'

const loading = ref(false)
const rows = ref<DischargeApplyItem[]>([])
const total = ref(0)
const elders = ref<ElderItem[]>([])
const createOpen = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const reviewOpen = ref(false)
const reviewSubmitting = ref(false)
const reviewId = ref<number | undefined>(undefined)
const reviewStatus = ref<'APPROVED' | 'REJECTED'>('APPROVED')
const reviewRemark = ref('')
const router = useRouter()
const dischargeFeeConfigOptions = ref<{ label: string; value: string }[]>([])

const query = reactive({
  elderId: undefined as number | undefined,
  keyword: undefined as string | undefined,
  status: undefined as DischargeApplyStatus | undefined,
  plannedDateRange: undefined as [string, string] | undefined,
  pageNo: 1,
  pageSize: 10
})

const form = reactive<DischargeApplyCreateRequest>({
  elderId: 0,
  plannedDischargeDate: '',
  reason: ''
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
  { title: '自动退住状态', key: 'autoDischargeStatus', width: 100 },
  { title: '自动退住说明', dataIndex: 'autoDischargeMessage', key: 'autoDischargeMessage', width: 180 },
  { title: '退住单号', dataIndex: 'linkedDischargeId', key: 'linkedDischargeId', width: 120 },
  { title: '审核人', dataIndex: 'reviewedByName', key: 'reviewedByName', width: 100 },
  { title: '审核备注', dataIndex: 'reviewRemark', key: 'reviewRemark' },
  { title: '审核状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 140 }
]

async function loadElders() {
  const res: PageResult<ElderItem> = await getElderPage({ pageNo: 1, pageSize: 200 })
  elders.value = res.list
}

async function loadDischargeFeeConfigOptions() {
  const options = await getBaseConfigItemList({ configGroup: 'DISCHARGE_FEE_CONFIG', status: 1 })
  dischargeFeeConfigOptions.value = (options || []).map((item: BaseConfigItem) => ({
    label: item.itemName,
    value: item.itemName
  }))
}

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
  form.elderId = 0
  form.plannedDischargeDate = ''
  form.reason = ''
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
  reviewOpen.value = true
}

async function submitReview() {
  if (!reviewId.value) return
  if (reviewStatus.value === 'REJECTED' && !reviewRemark.value.trim()) {
    message.warning('驳回时请填写审核备注')
    return
  }
  reviewSubmitting.value = true
  try {
    await reviewDischargeApply(reviewId.value, { status: reviewStatus.value, reviewRemark: reviewRemark.value || undefined })
    if (reviewStatus.value === 'APPROVED') {
      message.success('审核通过并已触发自动退住登记')
      router.push({ path: '/elder/discharge', query: { highlightApplyId: String(reviewId.value) } })
    } else {
      message.success('审核完成')
    }
    reviewOpen.value = false
    fetchData()
  } catch (error: any) {
    if (reviewStatus.value === 'APPROVED') {
      const msg = error?.msg || error?.message || '自动退住失败，请处理账单后重试'
      message.error(msg)
      fetchData()
    }
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
  await loadDischargeFeeConfigOptions()
  await loadElders()
  await fetchData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
</style>
