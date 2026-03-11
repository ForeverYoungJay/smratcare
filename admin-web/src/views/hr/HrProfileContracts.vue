<template>
  <PageContainer title="劳动合同管理" subTitle="员工档案中心 / 劳动合同管理">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="姓名/工号/手机号/合同编号" allow-clear style="width: 240px" />
      </a-form-item>
      <a-form-item label="合同状态">
        <a-select v-model:value="query.contractStatus" allow-clear style="width: 180px" :options="statusOptions" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="onExportCsv">导出 CSV</a-button>
          <a-button @click="onExportExcel">导出 Excel</a-button>
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
        <template v-if="column.key === 'contractStatus'">
          <a-tag :color="contractStatusColor(record.contractStatus)">{{ contractStatusText(record.contractStatus) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'remainingDays'">
          <a-tag v-if="record.contractEndDate && (record.remainingDays || 0) < 0" color="red">已过期 {{ Math.abs(record.remainingDays || 0) }} 天</a-tag>
          <a-tag v-else-if="record.contractEndDate && (record.remainingDays || 0) <= 30" color="orange">{{ record.remainingDays }} 天</a-tag>
          <span v-else>{{ record.contractEndDate ? `${record.remainingDays ?? '-'} 天` : '-' }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button type="link" @click="openContractModal(record)">维护合同</a-button>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="modalOpen" title="维护劳动合同" width="560px" @ok="submit" :confirm-loading="saving">
      <a-form :model="form" layout="vertical">
        <a-form-item label="员工">
          <a-input :value="`${form.realName || '-'}${form.staffNo ? ` (${form.staffNo})` : ''}`" disabled />
        </a-form-item>
        <a-form-item label="岗位">
          <a-input :value="form.jobTitle || '-'" disabled />
        </a-form-item>
        <a-form-item label="合同编号">
          <a-input v-model:value="form.contractNo" allow-clear />
        </a-form-item>
        <a-form-item label="合同类型">
          <a-select v-model:value="form.contractType" allow-clear :options="contractTypeOptions" />
        </a-form-item>
        <a-form-item label="合同状态">
          <a-select v-model:value="form.contractStatus" allow-clear :options="statusOptions" />
        </a-form-item>
        <a-form-item label="合同开始日期">
          <a-date-picker v-model:value="form.contractStartDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="合同结束日期">
          <a-date-picker v-model:value="form.contractEndDate" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getHrProfile, getHrStaffPage, upsertHrProfile } from '../../api/hr'
import type { HrStaffProfile, PageResult } from '../../types'
import { exportCsv, exportExcel } from '../../utils/export'
import { mapByDict } from './hrExportFields'

const query = reactive({
  keyword: undefined as string | undefined,
  contractStatus: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const statusOptions = [
  { label: '待签署', value: 'PENDING' },
  { label: '已生效', value: 'ACTIVE' },
  { label: '续签处理中', value: 'RENEWAL_PENDING' },
  { label: '已到期', value: 'EXPIRED' },
  { label: '已终止', value: 'TERMINATED' }
]
const contractTypeOptions = [
  { label: '固定期限', value: 'FIXED_TERM' },
  { label: '无固定期限', value: 'OPEN_ENDED' },
  { label: '实习协议', value: 'INTERNSHIP' },
  { label: '劳务协议', value: 'SERVICE' }
]
const columns = [
  { title: '工号', dataIndex: 'staffNo', key: 'staffNo', width: 120 },
  { title: '员工姓名', dataIndex: 'realName', key: 'realName', width: 120 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '岗位', dataIndex: 'jobTitle', key: 'jobTitle', width: 140 },
  { title: '用工类型', dataIndex: 'employmentType', key: 'employmentType', width: 120 },
  { title: '合同编号', dataIndex: 'contractNo', key: 'contractNo', width: 170 },
  { title: '合同类型', dataIndex: 'contractType', key: 'contractType', width: 120 },
  { title: '合同状态', dataIndex: 'contractStatus', key: 'contractStatus', width: 120 },
  { title: '合同开始', dataIndex: 'contractStartDate', key: 'contractStartDate', width: 120 },
  { title: '合同结束', dataIndex: 'contractEndDate', key: 'contractEndDate', width: 120 },
  { title: '剩余天数', dataIndex: 'remainingDays', key: 'remainingDays', width: 120 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' }
]
const rows = ref<HrStaffProfile[]>([])
const loading = ref(false)
const saving = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const modalOpen = ref(false)
const form = reactive<Partial<HrStaffProfile>>({})

function computeRemainingDays(item: HrStaffProfile) {
  if (!item.contractEndDate) return undefined
  return dayjs(item.contractEndDate).startOf('day').diff(dayjs().startOf('day'), 'day')
}

function contractStatusText(status?: string) {
  return statusOptions.find((item) => item.value === status)?.label || status || '-'
}

function contractStatusColor(status?: string) {
  switch (status) {
    case 'ACTIVE':
      return 'green'
    case 'RENEWAL_PENDING':
      return 'orange'
    case 'EXPIRED':
    case 'TERMINATED':
      return 'red'
    default:
      return 'default'
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HrStaffProfile> = await getHrStaffPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      contractStatus: query.contractStatus
    })
    const list = (res.list || []).map((item) => ({
      ...item,
      contractStatus: item.contractStatus || undefined,
      remainingDays: computeRemainingDays(item)
    }))
    rows.value = list
    pagination.total = res.total || list.length
  } catch {
    rows.value = []
    pagination.total = 0
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
  query.keyword = undefined
  query.contractStatus = undefined
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function rowClassName(record: HrStaffProfile) {
  const days = Number(record.remainingDays ?? 0)
  if (record.contractEndDate && days < 0) return 'hr-row-danger'
  if (record.contractEndDate && days <= 30) return 'hr-row-warning'
  return ''
}

async function openContractModal(record: HrStaffProfile) {
  Object.keys(form).forEach((key) => {
    ;(form as any)[key] = undefined
  })
  try {
    const detail = await getHrProfile(String(record.staffId))
    Object.assign(form, record, detail)
  } catch {
    Object.assign(form, record)
  }
  if (form.contractStartDate && typeof form.contractStartDate === 'string') {
    form.contractStartDate = dayjs(form.contractStartDate)
  }
  if (form.contractEndDate && typeof form.contractEndDate === 'string') {
    form.contractEndDate = dayjs(form.contractEndDate)
  }
  modalOpen.value = true
}

async function submit() {
  if (!form.staffId) {
    message.error('缺少员工信息')
    return
  }
  saving.value = true
  try {
    const payload: any = {
      staffId: form.staffId,
      contractNo: form.contractNo,
      contractType: form.contractType,
      contractStatus: form.contractStatus,
      contractStartDate: null,
      contractEndDate: null
    }
    if (form.contractStartDate && typeof form.contractStartDate === 'object' && 'format' in form.contractStartDate) {
      payload.contractStartDate = (form.contractStartDate as any).format('YYYY-MM-DD')
    }
    if (form.contractEndDate && typeof form.contractEndDate === 'object' && 'format' in form.contractEndDate) {
      payload.contractEndDate = (form.contractEndDate as any).format('YYYY-MM-DD')
    }
    await upsertHrProfile(payload)
    message.success('合同已更新')
    modalOpen.value = false
    fetchData()
  } catch {
    message.error('合同保存失败')
  } finally {
    saving.value = false
  }
}

const exportFields = [
  { key: 'staffNo', label: '工号' },
  { key: 'realName', label: '员工姓名' },
  { key: 'phone', label: '手机号' },
  { key: 'jobTitle', label: '岗位' },
  { key: 'employmentType', label: '用工类型' },
  { key: 'contractNo', label: '合同编号' },
  { key: 'contractType', label: '合同类型' },
  { key: 'contractStatusText', label: '合同状态' },
  { key: 'contractStartDate', label: '合同开始' },
  { key: 'contractEndDate', label: '合同结束' },
  { key: 'remainingDays', label: '剩余天数' }
]

function onExportCsv() {
  exportCsv(mapByDict(rows.value.map((item) => ({
    ...item,
    contractStatusText: contractStatusText(item.contractStatus)
  })) as Record<string, any>[], exportFields), '劳动合同管理-当前筛选')
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value.map((item) => ({
    ...item,
    contractStatusText: contractStatusText(item.contractStatus)
  })) as Record<string, any>[], exportFields), '劳动合同管理-当前筛选')
}

onMounted(fetchData)
</script>

<style scoped>
:deep(.hr-row-danger) {
  background: #fff1f0 !important;
}

:deep(.hr-row-warning) {
  background: #fffbe6 !important;
}
</style>
