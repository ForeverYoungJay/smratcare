<template>
  <PageContainer title="维修管理" subTitle="报修受理与维修闭环">
    <template #extra>
      <a-button type="primary" @click="openCreate">新增报修</a-button>
    </template>

    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="房间/类型/报修人" allow-clear @pressEnter="fetchData" />
      </a-form-item>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      :row-class-name="rowClassName"
      @change="handleTableChange"
    >
      <template #toolbar>
        <div class="ticket-toolbar">
          <div class="status-chips" role="tablist" aria-label="按工单状态筛选">
            <button
              type="button"
              class="status-chip"
              :class="{ 'is-active': !query.status && !overdueOnly }"
              @click="applyStatusChip(undefined)"
            >全部</button>
            <button
              v-for="opt in statusOptions"
              :key="opt.value"
              type="button"
              class="status-chip"
              :class="{ 'is-active': query.status === opt.value && !overdueOnly }"
              @click="applyStatusChip(opt.value)"
            >{{ opt.label }}</button>
            <button
              type="button"
              class="status-chip status-chip--overdue"
              :class="{ 'is-active': overdueOnly }"
              @click="toggleOverdue"
            >逾期未完（{{ overdueDays }}天+）</button>
          </div>
        </div>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 'COMPLETED' ? 'green' : record.status === 'PROCESSING' ? 'blue' : record.status === 'CANCELLED' ? 'default' : 'orange'">
            {{ statusLabel(record.status) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'priority'">
          <a-tag :color="record.priority === 'HIGH' ? 'red' : record.priority === 'LOW' ? 'blue' : 'gold'">
            {{ record.priority === 'HIGH' ? '高' : record.priority === 'LOW' ? '低' : '普通' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'totalCost'">
          {{ Number(record.totalCost || 0).toFixed(2) }}
        </template>
        <template v-else-if="column.key === 'reportedAt'">
          {{ formatReportedAt(record.reportedAt) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <div class="row-action-links">
            <a-button
              v-if="record.status === 'OPEN'"
              type="link"
              size="small"
              class="action-strong"
              @click="startProcessing(record)"
            >接单</a-button>
            <a-button
              v-if="record.status === 'OPEN' || record.status === 'PROCESSING'"
              type="link"
              size="small"
              class="action-strong"
              @click="complete(record)"
            >完工</a-button>
            <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" danger size="small" @click="remove(record)">删除</a-button>
          </div>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑报修' : '新增报修'" @ok="submit" :confirm-loading="saving" width="680px">
      <a-form ref="formRef" layout="vertical" :model="form" :rules="rules">
        <a-form-item label="房间">
          <a-select
            v-model:value="form.roomId"
            show-search
            allow-clear
            :options="roomOptions"
            placeholder="可选"
            option-filter-prop="label"
          />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="报修人" required>
              <a-input v-model:value="form.reporterName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="维修负责人">
              <a-input v-model:value="form.assigneeName" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="故障类型" required>
              <a-input v-model:value="form.issueType" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="优先级">
              <a-select v-model:value="form.priority" :options="priorityOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="故障描述" required>
          <a-textarea v-model:value="form.description" :rows="3" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="人工成本(元)">
              <a-input-number
                v-model:value="form.laborCost"
                :min="0"
                :precision="2"
                style="width: 100%"
                @change="recalcTotalCost"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="物料成本(元)">
              <a-input-number
                v-model:value="form.materialCost"
                :min="0"
                :precision="2"
                style="width: 100%"
                @change="recalcTotalCost"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="总成本(元)">
              <a-input-number v-model:value="form.totalCost" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import type { FormInstance } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getRoomList } from '../../api/bed'
import { getMaintenancePage, createMaintenance, updateMaintenance, completeMaintenance, deleteMaintenance } from '../../api/life'
import type { MaintenanceRequest, PageResult } from '../../types'

const route = useRoute()
const loading = ref(false)
const rows = ref<MaintenanceRequest[]>([])
const roomOptions = ref<any[]>([])
const query = reactive({ status: undefined as string | undefined, keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const overdueOnly = ref(false)
const overdueDays = ref(2)
const columns = [
  { title: '房间号', dataIndex: 'roomNo', key: 'roomNo', width: 120 },
  { title: '故障类型', dataIndex: 'issueType', key: 'issueType', width: 140 },
  { title: '报修人', dataIndex: 'reporterName', key: 'reporterName', width: 120 },
  { title: '维修负责人', dataIndex: 'assigneeName', key: 'assigneeName', width: 120 },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 90 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '总成本(元)', dataIndex: 'totalCost', key: 'totalCost', width: 120 },
  { title: '报修时间', dataIndex: 'reportedAt', key: 'reportedAt', width: 170 },
  { title: '操作', key: 'action', width: 180 }
]

const statusOptions = [
  { label: '待处理', value: 'OPEN' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]
const priorityOptions = [
  { label: '低', value: 'LOW' },
  { label: '普通', value: 'NORMAL' },
  { label: '高', value: 'HIGH' }
]

const formRef = ref<FormInstance>()
const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  roomId: undefined as number | undefined,
  reporterName: '',
  assigneeName: '',
  issueType: '',
  description: '',
  laborCost: 0,
  materialCost: 0,
  totalCost: 0,
  priority: 'NORMAL',
  status: 'OPEN',
  remark: ''
})

const rules = {
  reporterName: [{ required: true, message: '请输入报修人', trigger: 'blur' }],
  issueType: [{ required: true, message: '请输入故障类型', trigger: 'blur' }],
  description: [{ required: true, message: '请输入故障描述', trigger: 'blur' }]
}

function statusLabel(status?: string) {
  if (status === 'PROCESSING') return '处理中'
  if (status === 'COMPLETED') return '已完成'
  if (status === 'CANCELLED') return '已取消'
  return '待处理'
}

function formatReportedAt(value?: string) {
  if (!value) return '-'
  const parsed = dayjs(value)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm') : value
}

function applyStatusChip(value?: string) {
  query.status = value
  overdueOnly.value = false
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function toggleOverdue() {
  overdueOnly.value = !overdueOnly.value
  if (overdueOnly.value) query.status = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function rowClassName(record: MaintenanceRequest) {
  if (record.priority === 'HIGH' && record.status !== 'COMPLETED' && record.status !== 'CANCELLED') {
    return 'row-priority-high'
  }
  return ''
}

async function loadRooms() {
  const rooms = await getRoomList()
  roomOptions.value = rooms.map((r: any) => ({ label: `${r.roomNo}`, value: r.id }))
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<MaintenanceRequest> = await getMaintenancePage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status,
      keyword: query.keyword || undefined,
      overdueOnly: overdueOnly.value || undefined,
      overdueDays: overdueDays.value
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
  query.status = undefined
  query.keyword = ''
  overdueOnly.value = false
  overdueDays.value = 2
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function applyRouteFilters() {
  const statusRaw = Array.isArray(route.query.status) ? route.query.status[0] : route.query.status
  const filterRaw = Array.isArray(route.query.filter) ? route.query.filter[0] : route.query.filter
  const overdueDaysRaw = Array.isArray(route.query.overdueDays) ? route.query.overdueDays[0] : route.query.overdueDays
  query.status = typeof statusRaw === 'string' && statusRaw ? statusRaw : undefined
  overdueOnly.value = String(filterRaw || '').toLowerCase() === 'overdue'
  const parsedOverdueDays = Number(overdueDaysRaw)
  overdueDays.value = Number.isFinite(parsedOverdueDays) && parsedOverdueDays > 0 ? parsedOverdueDays : 2
}

function openCreate() {
  form.id = undefined
  form.roomId = undefined
  form.reporterName = ''
  form.assigneeName = ''
  form.issueType = ''
  form.description = ''
  form.laborCost = 0
  form.materialCost = 0
  form.totalCost = 0
  form.priority = 'NORMAL'
  form.status = 'OPEN'
  form.remark = ''
  editOpen.value = true
  formRef.value?.clearValidate?.()
}

function openEdit(record: MaintenanceRequest) {
  form.id = record.id
  form.roomId = record.roomId
  form.reporterName = record.reporterName
  form.assigneeName = record.assigneeName || ''
  form.issueType = record.issueType
  form.description = record.description
  form.laborCost = Number(record.laborCost || 0)
  form.materialCost = Number(record.materialCost || 0)
  form.totalCost = Number(record.totalCost || 0)
  form.priority = record.priority || 'NORMAL'
  form.status = record.status || 'OPEN'
  form.remark = record.remark || ''
  editOpen.value = true
  formRef.value?.clearValidate?.()
}

function toNumber(value?: number | null) {
  const n = Number(value || 0)
  if (!Number.isFinite(n) || n < 0) {
    return 0
  }
  return Math.round(n * 100) / 100
}

function recalcTotalCost() {
  form.totalCost = Math.round((toNumber(form.laborCost) + toNumber(form.materialCost)) * 100) / 100
}

async function submit() {
  try {
    await formRef.value?.validate()
    const payload = {
      roomId: form.roomId,
      reporterName: form.reporterName,
      assigneeName: form.assigneeName,
      issueType: form.issueType,
      description: form.description,
      laborCost: toNumber(form.laborCost),
      materialCost: toNumber(form.materialCost),
      totalCost: toNumber(form.totalCost),
      priority: form.priority,
      status: form.status,
      remark: form.remark
    }
    if (saving.value) return
    saving.value = true
    if (form.id) {
      await updateMaintenance(form.id, payload)
      message.success('报修单已更新')
    } else {
      await createMaintenance(payload)
      message.success('报修单已创建')
    }
    editOpen.value = false
    await fetchData()
  } catch (error: any) {
    if (error?.errorFields) {
      return
    }
    message.error(error?.message || '保存报修单失败')
  } finally {
    saving.value = false
  }
}

async function startProcessing(record: MaintenanceRequest) {
  try {
    // PUT 为整单更新，带上原字段避免局部提交把其他字段清空
    await updateMaintenance(record.id!, {
      roomId: record.roomId,
      reporterName: record.reporterName,
      assigneeName: record.assigneeName,
      issueType: record.issueType,
      description: record.description,
      laborCost: record.laborCost,
      materialCost: record.materialCost,
      totalCost: record.totalCost,
      priority: record.priority,
      remark: record.remark,
      status: 'PROCESSING'
    })
    message.success('已接单，工单进入处理中')
    await fetchData()
  } catch (error: any) {
    message.error(error?.message || '接单失败')
  }
}

async function complete(record: MaintenanceRequest) {
  Modal.confirm({
    title: '确认标记该维修单为已完工？',
    content: `故障类型：${record.issueType || '未填写'}；报修人：${record.reporterName || '未填写'}`,
    okText: '确认完工',
    cancelText: '取消',
    async onOk() {
      try {
        await completeMaintenance(record.id)
        message.success('维修单已标记完工')
        await fetchData()
      } catch (error: any) {
        message.error(error?.message || '完工操作失败')
      }
    }
  })
}

async function remove(record: MaintenanceRequest) {
  Modal.confirm({
    title: '确认删除报修单？',
    content: `删除后将无法恢复，故障类型：${record.issueType || '未填写'}`,
    okText: '删除',
    okButtonProps: { danger: true },
    cancelText: '取消',
    async onOk() {
      try {
        await deleteMaintenance(record.id)
        message.success('报修单已删除')
        await fetchData()
      } catch (error: any) {
        message.error(error?.message || '删除报修单失败')
      }
    }
  })
}

onMounted(async () => {
  applyRouteFilters()
  await loadRooms()
  await fetchData()
})

watch(
  () => route.query,
  () => {
    applyRouteFilters()
    fetchData()
  }
)
</script>

<style scoped>
.ticket-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  padding-bottom: 12px;
}

.status-chips {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.status-chip {
  min-height: 32px;
  padding: 4px 14px;
  border: 1px solid var(--border);
  border-radius: 999px;
  background: #ffffff;
  color: var(--muted);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
}

.status-chip:hover {
  border-color: rgba(var(--primary-rgb), 0.4);
  color: var(--primary);
}

.status-chip.is-active {
  border-color: rgba(var(--primary-rgb), 0.4);
  background: var(--primary-soft);
  color: var(--primary-strong);
}

.status-chip--overdue:hover {
  border-color: rgba(var(--warning-rgb), 0.5);
  color: var(--warning);
}

.status-chip--overdue.is-active {
  border-color: rgba(var(--warning-rgb), 0.5);
  background: rgba(var(--warning-rgb), 0.12);
  color: var(--warning);
}

.row-action-links {
  display: flex;
  align-items: center;
  gap: 2px;
}

.action-strong {
  font-weight: 700;
}

/* 高优先级未完工单整行提示 */
:deep(.row-priority-high > td) {
  background: rgba(var(--danger-rgb), 0.05) !important;
}

:deep(.row-priority-high > td:first-child) {
  box-shadow: inset 3px 0 0 rgba(var(--danger-rgb), 0.55);
}
</style>
