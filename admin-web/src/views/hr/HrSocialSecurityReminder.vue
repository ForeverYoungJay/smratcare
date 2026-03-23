<template>
  <PageContainer title="社保到期/未办理明细" subTitle="按提醒日期跟进待办理、即将提醒和已参保员工">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="姓名/工号/手机号" allow-clear style="width: 240px" />
      </a-form-item>
      <a-form-item label="提醒范围">
        <a-select v-model:value="query.scope" :options="scopeOptions" style="width: 180px" />
      </a-form-item>
      <a-form-item label="社保状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 180px" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button type="primary" :loading="generatingBills" @click="generateCurrentMonthBills()">生成本月社保记账</a-button>
          <a-button @click="onExportCsv">导出 CSV</a-button>
          <a-button @click="onExportExcel">导出 Excel</a-button>
          <a-tag color="blue">当前范围：{{ scopeText(query.scope) }}</a-tag>
        </a-space>
      </template>
    </SearchForm>

    <DataTable
      rowKey="staffId"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      :row-class-name="rowClassName"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'socialSecurityStatus'">
          <a-tag :color="statusColor(record.socialSecurityStatus)">{{ statusText(record.socialSecurityStatus) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'remainingDays'">
          <a-tag v-if="(record.remainingDays || 0) < 0" color="red">已逾期 {{ Math.abs(record.remainingDays || 0) }} 天</a-tag>
          <a-tag v-else-if="(record.remainingDays || 0) === 0" color="volcano">今天提醒</a-tag>
          <a-tag v-else-if="(record.remainingDays || 0) <= 7" color="orange">{{ record.remainingDays }} 天后提醒</a-tag>
          <span v-else>{{ record.remainingDays ?? '-' }} 天</span>
        </template>
        <template v-else-if="column.key === 'reminderScope'">
          <a-tag :color="scopeColor(record.reminderScope)">{{ scopeText(record.reminderScope) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'socialSecurityWorkflowStatus'">
          <a-tag :color="workflowColor(record.socialSecurityWorkflowStatus)">{{ workflowText(record.socialSecurityWorkflowStatus) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'socialSecurityCompanyApply'">
          <a-tag :color="Number(record.socialSecurityCompanyApply || 0) === 1 ? 'green' : 'default'">
            {{ Number(record.socialSecurityCompanyApply || 0) === 1 ? '是' : '否' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'socialSecurityMonthlyAmount'">
          {{ formatMoney(record.socialSecurityMonthlyAmount) }}
        </template>
        <template v-else-if="column.key === 'actions'">
          <a-space wrap>
            <a-button size="small" type="link" @click="openApplyModal(record)">发起申请</a-button>
            <a-button
              v-if="record.socialSecurityWorkflowStatus === 'PENDING_DIRECTOR'"
              size="small"
              type="link"
              @click="directorDecision(record, true)"
            >
              院长通过
            </a-button>
            <a-button
              v-if="record.socialSecurityWorkflowStatus === 'PENDING_DIRECTOR'"
              size="small"
              danger
              type="link"
              @click="directorDecision(record, false)"
            >
              驳回
            </a-button>
            <a-button
              v-if="record.socialSecurityWorkflowStatus === 'PENDING_FINANCE' || record.socialSecurityStatus === 'PROCESSING'"
              size="small"
              type="link"
              @click="completeFlow(record)"
            >
              办理完成
            </a-button>
            <a-button
              v-if="canGenerateBill(record)"
              size="small"
              type="link"
              @click="generateCurrentMonthBills(record)"
            >
              生成本月记账
            </a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal
      v-model:open="applyOpen"
      title="发起企业社保申请"
      :confirm-loading="applySaving"
      @ok="submitApply"
    >
      <a-form layout="vertical">
        <a-form-item label="员工">
          <a-input :value="applyTargetName" disabled />
        </a-form-item>
        <a-form-item label="申请企业购买社保">
          <a-switch v-model:checked="applyForm.socialSecurityCompanyApplyChecked" checked-children="是" un-checked-children="否" />
        </a-form-item>
        <a-form-item label="发送院长审核">
          <a-switch v-model:checked="applyForm.socialSecurityNeedDirectorApprovalChecked" checked-children="发送" un-checked-children="直送财务" />
        </a-form-item>
        <a-form-item label="每月社保费用">
          <a-input-number
            v-model:value="applyForm.socialSecurityMonthlyAmount"
            :min="0"
            :precision="2"
            style="width: 100%"
            placeholder="填写每人每月企业社保费用"
          />
        </a-form-item>
        <a-form-item label="社保备注">
          <a-textarea v-model:value="applyForm.socialSecurityRemark" :rows="3" placeholder="记录参保说明、补缴情形或审批原因" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message, Modal } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  applyHrSocialSecurity,
  completeHrSocialSecurity,
  decideHrSocialSecurityByDirector,
  generateHrSocialSecurityMonthlyBill,
  getHrSocialSecurityReminderPage
} from '../../api/hr'
import type { HrSocialSecurityReminderItem, PageResult } from '../../types'
import { exportCsv, exportExcel } from '../../utils/export'
import { mapByDict } from './hrExportFields'

const route = useRoute()
const query = reactive({
  keyword: undefined as string | undefined,
  scope: 'ALL',
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const scopeOptions = [
  { label: '全部', value: 'ALL' },
  { label: '已到提醒日', value: 'DUE' },
  { label: '7日内将提醒', value: 'UPCOMING' },
  { label: '未办理', value: 'PENDING' },
  { label: '已参保', value: 'COMPLETED' }
]
const statusOptions = [
  { label: '待办理', value: 'PENDING' },
  { label: '办理中', value: 'PROCESSING' },
  { label: '已参保', value: 'COMPLETED' },
  { label: '暂停缴纳', value: 'STOPPED' }
]
const columns = [
  { title: '工号', dataIndex: 'staffNo', key: 'staffNo', width: 120 },
  { title: '员工姓名', dataIndex: 'staffName', key: 'staffName', width: 120 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '岗位', dataIndex: 'jobTitle', key: 'jobTitle', width: 140 },
  { title: '入职日期', dataIndex: 'hireDate', key: 'hireDate', width: 120 },
  { title: '社保状态', dataIndex: 'socialSecurityStatus', key: 'socialSecurityStatus', width: 120 },
  { title: '流程状态', dataIndex: 'socialSecurityWorkflowStatus', key: 'socialSecurityWorkflowStatus', width: 130 },
  { title: '企业购买', dataIndex: 'socialSecurityCompanyApply', key: 'socialSecurityCompanyApply', width: 100 },
  { title: '月费用', dataIndex: 'socialSecurityMonthlyAmount', key: 'socialSecurityMonthlyAmount', width: 120 },
  { title: '参保开始', dataIndex: 'socialSecurityStartDate', key: 'socialSecurityStartDate', width: 120 },
  { title: '最近记账月', dataIndex: 'socialSecurityLastBilledMonth', key: 'socialSecurityLastBilledMonth', width: 120 },
  { title: '提醒天数', dataIndex: 'socialSecurityReminderDays', key: 'socialSecurityReminderDays', width: 110 },
  { title: '提醒日期', dataIndex: 'reminderDate', key: 'reminderDate', width: 120 },
  { title: '提醒范围', dataIndex: 'reminderScope', key: 'reminderScope', width: 120 },
  { title: '剩余天数', dataIndex: 'remainingDays', key: 'remainingDays', width: 120 },
  { title: '备注', dataIndex: 'socialSecurityRemark', key: 'socialSecurityRemark', width: 220 },
  { title: '操作', key: 'actions', width: 260, fixed: 'right' }
]

const rows = ref<HrSocialSecurityReminderItem[]>([])
const loading = ref(false)
const applySaving = ref(false)
const generatingBills = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const applyOpen = ref(false)
const applyForm = reactive({
  staffId: undefined as string | number | undefined,
  socialSecurityCompanyApplyChecked: true,
  socialSecurityNeedDirectorApprovalChecked: true,
  socialSecurityMonthlyAmount: undefined as number | undefined,
  socialSecurityRemark: ''
})
const applyTargetName = computed(() => {
  const row = rows.value.find((item) => String(item.staffId) === String(applyForm.staffId || ''))
  return row ? `${row.staffName || '-'} (${row.staffNo || '-'})` : ''
})

function statusText(status?: string) {
  return statusOptions.find((item) => item.value === status)?.label || status || '待办理'
}

function statusColor(status?: string) {
  switch (status) {
    case 'COMPLETED':
      return 'green'
    case 'PROCESSING':
      return 'blue'
    case 'STOPPED':
      return 'default'
    default:
      return 'orange'
  }
}

function workflowText(value?: string) {
  switch (value) {
    case 'PENDING_DIRECTOR':
      return '待院长审核'
    case 'PENDING_FINANCE':
      return '待财务办理'
    case 'ACTIVE':
      return '已生效'
    case 'REJECTED':
      return '已驳回'
    case 'STOPPED':
      return '已停缴'
    default:
      return '待发起'
  }
}

function workflowColor(value?: string) {
  switch (value) {
    case 'PENDING_DIRECTOR':
      return 'orange'
    case 'PENDING_FINANCE':
      return 'blue'
    case 'ACTIVE':
      return 'green'
    case 'REJECTED':
      return 'red'
    case 'STOPPED':
      return 'default'
    default:
      return 'default'
  }
}

function formatMoney(value?: number | string) {
  const amount = Number(value || 0)
  if (!Number.isFinite(amount) || amount <= 0) return '-'
  return `${amount.toFixed(2)} 元`
}

function scopeText(scope?: string) {
  return scopeOptions.find((item) => item.value === scope)?.label || scope || '全部'
}

function scopeColor(scope?: string) {
  switch (scope) {
    case 'DUE':
      return 'red'
    case 'UPCOMING':
      return 'orange'
    case 'COMPLETED':
      return 'green'
    default:
      return 'blue'
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HrSocialSecurityReminderItem> = await getHrSocialSecurityReminderPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      scope: query.scope,
      status: query.status
    })
    rows.value = res.list || []
    pagination.total = res.total || rows.value.length
  } catch {
    rows.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.keyword = undefined
  query.scope = 'ALL'
  query.status = undefined
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function rowClassName(record: HrSocialSecurityReminderItem) {
  const days = Number(record.remainingDays ?? 9999)
  if ((record.reminderScope || '') === 'DUE' || days < 0) return 'hr-row-danger'
  if ((record.reminderScope || '') === 'UPCOMING' || days <= 7) return 'hr-row-warning'
  return ''
}

function canGenerateBill(record: HrSocialSecurityReminderItem) {
  const amount = Number(record.socialSecurityMonthlyAmount || 0)
  if (!Number.isFinite(amount) || amount <= 0) return false
  return record.socialSecurityWorkflowStatus === 'ACTIVE' || record.socialSecurityStatus === 'COMPLETED'
}

function openApplyModal(record: HrSocialSecurityReminderItem) {
  applyForm.staffId = record.staffId
  applyForm.socialSecurityCompanyApplyChecked = Number(record.socialSecurityCompanyApply || 0) !== 0 || !record.socialSecurityWorkflowStatus
  applyForm.socialSecurityNeedDirectorApprovalChecked = Number(record.socialSecurityNeedDirectorApproval || 0) !== 0
  applyForm.socialSecurityMonthlyAmount = record.socialSecurityMonthlyAmount ? Number(record.socialSecurityMonthlyAmount) : undefined
  applyForm.socialSecurityRemark = record.socialSecurityRemark || ''
  applyOpen.value = true
}

async function submitApply() {
  if (!applyForm.staffId) {
    message.error('缺少员工信息')
    return
  }
  if (!applyForm.socialSecurityCompanyApplyChecked) {
    message.error('请先勾选企业购买社保')
    return
  }
  if (!Number.isFinite(Number(applyForm.socialSecurityMonthlyAmount || 0)) || Number(applyForm.socialSecurityMonthlyAmount || 0) <= 0) {
    message.error('请填写有效的每月社保费用')
    return
  }
  applySaving.value = true
  try {
    await applyHrSocialSecurity({
      staffId: applyForm.staffId,
      socialSecurityCompanyApply: applyForm.socialSecurityCompanyApplyChecked ? 1 : 0,
      socialSecurityNeedDirectorApproval: applyForm.socialSecurityNeedDirectorApprovalChecked ? 1 : 0,
      socialSecurityMonthlyAmount: Number(applyForm.socialSecurityMonthlyAmount),
      socialSecurityRemark: applyForm.socialSecurityRemark
    })
    message.success(applyForm.socialSecurityNeedDirectorApprovalChecked ? '已提交院长审核' : '已发送财务办理')
    applyOpen.value = false
    fetchData()
  } catch {
    message.error('社保申请提交失败')
  } finally {
    applySaving.value = false
  }
}

async function directorDecision(record: HrSocialSecurityReminderItem, approved: boolean) {
  Modal.confirm({
    title: approved ? '确认通过院长审核？' : '确认驳回社保申请？',
    content: `${record.staffName || '-'} 的社保申请将${approved ? '进入财务办理' : '退回人事'}`,
    async onOk() {
      try {
        await decideHrSocialSecurityByDirector(String(record.staffId || ''), approved)
        message.success(approved ? '已通过并发送财务办理' : '已驳回社保申请')
        fetchData()
      } catch {
        message.error(approved ? '审核通过失败' : '驳回失败')
      }
    }
  })
}

async function completeFlow(record: HrSocialSecurityReminderItem) {
  Modal.confirm({
    title: '确认已完成社保办理？',
    content: `${record.staffName || '-'} 将转为已参保，并自动生成 ${dayjs().format('YYYY-MM')} 的社保记账。`,
    async onOk() {
      try {
        await completeHrSocialSecurity(String(record.staffId || ''), {
          staffId: String(record.staffId || ''),
          socialSecurityStartDate: record.socialSecurityStartDate || dayjs().format('YYYY-MM-DD'),
          socialSecurityRemark: record.socialSecurityRemark
        })
        message.success('已完成社保办理并更新记账')
        fetchData()
      } catch {
        message.error('社保办理完成失败')
      }
    }
  })
}

async function generateCurrentMonthBills(record?: HrSocialSecurityReminderItem) {
  generatingBills.value = true
  try {
    const res = await generateHrSocialSecurityMonthlyBill({
      month: dayjs().format('YYYY-MM'),
      staffIds: record?.staffId ? [record.staffId] : undefined
    })
    message.success(res?.message || '社保月度记账已生成')
    fetchData()
  } catch {
    message.error('生成社保记账失败')
  } finally {
    generatingBills.value = false
  }
}

const exportFields = [
  { key: 'staffNo', label: '工号' },
  { key: 'staffName', label: '员工姓名' },
  { key: 'phone', label: '手机号' },
  { key: 'jobTitle', label: '岗位' },
  { key: 'hireDate', label: '入职日期' },
  { key: 'socialSecurityStatusText', label: '社保状态' },
  { key: 'socialSecurityWorkflowStatusText', label: '流程状态' },
  { key: 'socialSecurityCompanyApplyText', label: '企业购买' },
  { key: 'socialSecurityMonthlyAmountText', label: '月费用' },
  { key: 'socialSecurityStartDate', label: '参保开始' },
  { key: 'socialSecurityLastBilledMonth', label: '最近记账月' },
  { key: 'socialSecurityReminderDays', label: '提醒天数' },
  { key: 'reminderDate', label: '提醒日期' },
  { key: 'reminderScopeText', label: '提醒范围' },
  { key: 'remainingDays', label: '剩余天数' },
  { key: 'socialSecurityRemark', label: '备注' }
]

function onExportCsv() {
  exportCsv(mapByDict(rows.value.map((item) => ({
    ...item,
    socialSecurityStatusText: statusText(item.socialSecurityStatus),
    socialSecurityWorkflowStatusText: workflowText(item.socialSecurityWorkflowStatus),
    socialSecurityCompanyApplyText: Number(item.socialSecurityCompanyApply || 0) === 1 ? '是' : '否',
    socialSecurityMonthlyAmountText: formatMoney(item.socialSecurityMonthlyAmount),
    reminderScopeText: scopeText(item.reminderScope)
  })) as Record<string, any>[], exportFields), '社保到期未办理明细-当前筛选')
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value.map((item) => ({
    ...item,
    socialSecurityStatusText: statusText(item.socialSecurityStatus),
    socialSecurityWorkflowStatusText: workflowText(item.socialSecurityWorkflowStatus),
    socialSecurityCompanyApplyText: Number(item.socialSecurityCompanyApply || 0) === 1 ? '是' : '否',
    socialSecurityMonthlyAmountText: formatMoney(item.socialSecurityMonthlyAmount),
    reminderScopeText: scopeText(item.reminderScope)
  })) as Record<string, any>[], exportFields), '社保到期未办理明细-当前筛选')
}

onMounted(() => {
  const scope = String(route.query.scope || '').trim().toUpperCase()
  if (scope) {
    query.scope = scope
  }
  fetchData()
})
</script>

<style scoped>
:deep(.hr-row-danger) {
  background: #fff1f0 !important;
}

:deep(.hr-row-warning) {
  background: #fffbe6 !important;
}
</style>
