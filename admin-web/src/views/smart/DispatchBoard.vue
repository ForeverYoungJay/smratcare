<template>
  <PageContainer title="告警派单看板" subTitle="触发 → 受理 → 响应 → 到场 → 处置 → 复盘（超时未响应自动升级）">
    <SearchForm :model="query" @search="onSearch" @reset="onReset">
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 150px" placeholder="全部">
          <a-select-option v-for="s in statusFlow" :key="s.value" :value="s.value">{{ s.label }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="等级">
        <a-select v-model:value="query.level" allow-clear style="width: 130px" placeholder="全部">
          <a-select-option value="CRITICAL">危急</a-select-option>
          <a-select-option value="HIGH">高</a-select-option>
          <a-select-option value="WARNING">提醒</a-select-option>
        </a-select>
      </a-form-item>
      <template #extra>
        <a-button @click="doAuto">立即扫描派单</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'level'">
          <a-tag :color="levelColor(record.level)">{{ levelText(record.level) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'dispatchStatus'">
          <a-badge :status="statusBadge(record.dispatchStatus)" :text="statusText(record.dispatchStatus)" />
        </template>
        <template v-else-if="column.key === 'escalationCount'">
          <a-tag v-if="record.escalationCount > 0" color="volcano">升级 {{ record.escalationCount }} 次</a-tag>
          <span v-else>-</span>
        </template>
        <template v-else-if="column.key === 'assignee'">{{ record.assigneeName || '-' }}</template>
        <template v-else-if="column.key === 'action'">
          <div class="row-action-links">
            <a-button v-if="record.dispatchStatus === 'TRIGGERED'" type="link" size="small" @click="openAssign(record)">受理</a-button>
            <a-button v-if="['TRIGGERED','ASSIGNED'].includes(record.dispatchStatus)" type="link" size="small" @click="act('respond', record)">响应</a-button>
            <a-button v-if="record.dispatchStatus === 'RESPONDED'" type="link" size="small" @click="act('onsite', record)">到场</a-button>
            <a-button v-if="['RESPONDED','ONSITE'].includes(record.dispatchStatus)" type="link" size="small" @click="openHandle(record)">处置</a-button>
            <a-button v-if="record.dispatchStatus === 'HANDLED'" type="link" size="small" @click="openReview(record)">复盘</a-button>
            <span v-if="record.dispatchStatus === 'REVIEWED'" class="text-muted">已闭环</span>
          </div>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="assignOpen" title="受理并指派" :confirm-loading="saving" @ok="submitAssign">
      <a-form layout="vertical">
        <a-form-item label="处置人姓名"><a-input v-model:value="assignForm.assigneeName" placeholder="值班护理员" /></a-form-item>
        <a-form-item label="处置人ID"><a-input-number v-model:value="assignForm.assigneeId" style="width: 100%" placeholder="选填" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="handleOpen" title="处置完成" :confirm-loading="saving" @ok="submitHandle">
      <a-form layout="vertical">
        <a-form-item label="处置说明" required><a-textarea v-model:value="actionForm.note" :rows="3" placeholder="现场处置过程与结果" /></a-form-item>
        <a-form-item label="关联不良事件ID"><a-input-number v-model:value="actionForm.incidentId" style="width: 100%" placeholder="选填，如已登记事故" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="reviewOpen" title="复盘关闭" :confirm-loading="saving" @ok="submitReview">
      <a-form layout="vertical">
        <a-form-item label="复盘结论" required><a-textarea v-model:value="actionForm.note" :rows="3" placeholder="原因分析、整改建议" /></a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  getSmartDispatchPage,
  autoSmartDispatch,
  assignSmartDispatch,
  respondSmartDispatch,
  onsiteSmartDispatch,
  handleSmartDispatch,
  reviewSmartDispatch
} from '../../api/smartCare'
import type { Id, PageResult, SmartAlertDispatch } from '../../types'

const statusFlow = [
  { label: '待受理', value: 'TRIGGERED' },
  { label: '已受理', value: 'ASSIGNED' },
  { label: '已响应', value: 'RESPONDED' },
  { label: '已到场', value: 'ONSITE' },
  { label: '已处置', value: 'HANDLED' },
  { label: '已复盘', value: 'REVIEWED' }
]

const loading = ref(false)
const saving = ref(false)
const rows = ref<SmartAlertDispatch[]>([])
const query = reactive({ status: undefined as string | undefined, level: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '派单ID', dataIndex: 'id', key: 'id', width: 120 },
  { title: '告警ID', dataIndex: 'alertId', key: 'alertId', width: 120 },
  { title: '长者ID', dataIndex: 'elderId', key: 'elderId', width: 90 },
  { title: '等级', key: 'level', width: 90 },
  { title: '状态', key: 'dispatchStatus', width: 110 },
  { title: '处置人', key: 'assignee', width: 110 },
  { title: '响应时限', dataIndex: 'responseDeadline', key: 'responseDeadline', width: 160 },
  { title: '升级', key: 'escalationCount', width: 110 },
  { title: '操作', key: 'action', width: 220 }
]

const assignOpen = ref(false)
const handleOpen = ref(false)
const reviewOpen = ref(false)
const assignForm = reactive({ dispatchId: undefined as Id | undefined, assigneeName: '', assigneeId: undefined as number | undefined })
const actionForm = reactive({ dispatchId: undefined as Id | undefined, note: '', incidentId: undefined as number | undefined })

function levelText(l?: string) {
  return ({ WARNING: '提醒', HIGH: '高', CRITICAL: '危急' } as Record<string, string>)[l || ''] || l || '-'
}
function levelColor(l?: string) {
  return l === 'CRITICAL' ? 'red' : l === 'HIGH' ? 'orange' : 'blue'
}
function statusText(s?: string) {
  return statusFlow.find((o) => o.value === s)?.label || s || '-'
}
function statusBadge(s?: string) {
  if (s === 'REVIEWED') return 'success'
  if (s === 'TRIGGERED') return 'error'
  if (s === 'HANDLED') return 'processing'
  return 'warning'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<SmartAlertDispatch> = await getSmartDispatchPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status,
      level: query.level
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}
function onSearch() {
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}
function onReset() {
  query.status = undefined
  query.level = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}
function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

async function doAuto() {
  const n = await autoSmartDispatch()
  message.success(`已扫描派单，新增 ${n ?? 0} 条`)
  fetchData()
}

async function act(type: 'respond' | 'onsite', record: SmartAlertDispatch) {
  if (type === 'respond') await respondSmartDispatch(record.id)
  else await onsiteSmartDispatch(record.id)
  message.success('已更新')
  fetchData()
}

function openAssign(record: SmartAlertDispatch) {
  assignForm.dispatchId = record.id
  assignForm.assigneeName = ''
  assignForm.assigneeId = undefined
  assignOpen.value = true
}
async function submitAssign() {
  saving.value = true
  try {
    await assignSmartDispatch({
      dispatchId: assignForm.dispatchId as Id,
      assigneeId: assignForm.assigneeId,
      assigneeName: assignForm.assigneeName
    })
    message.success('已受理')
    assignOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

function openHandle(record: SmartAlertDispatch) {
  actionForm.dispatchId = record.id
  actionForm.note = ''
  actionForm.incidentId = undefined
  handleOpen.value = true
}
async function submitHandle() {
  if (!actionForm.note) {
    message.error('请填写处置说明')
    return
  }
  saving.value = true
  try {
    await handleSmartDispatch({ dispatchId: actionForm.dispatchId as Id, note: actionForm.note, incidentId: actionForm.incidentId })
    message.success('处置完成')
    handleOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

function openReview(record: SmartAlertDispatch) {
  actionForm.dispatchId = record.id
  actionForm.note = ''
  reviewOpen.value = true
}
async function submitReview() {
  if (!actionForm.note) {
    message.error('请填写复盘结论')
    return
  }
  saving.value = true
  try {
    await reviewSmartDispatch({ dispatchId: actionForm.dispatchId as Id, note: actionForm.note })
    message.success('已复盘闭环')
    reviewOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

fetchData()
</script>

<style scoped>
.text-muted { color: #52c41a; }
</style>
