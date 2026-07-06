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
        <a-radio-group v-model:value="viewMode" button-style="solid" style="margin-right: 8px">
          <a-radio-button value="board">泳道看板</a-radio-button>
          <a-radio-button value="table">表格</a-radio-button>
        </a-radio-group>
        <a-button @click="doAuto">立即扫描派单</a-button>
      </template>
    </SearchForm>

    <div v-if="viewMode === 'board'" class="dispatch-board">
      <div v-for="lane in statusFlow" :key="lane.value" class="board-lane">
        <div class="lane-header">
          <a-badge :status="statusBadge(lane.value)" :text="lane.label" />
          <span class="lane-count">{{ laneCards(lane.value).length }}</span>
        </div>
        <div class="lane-body">
          <a-card v-for="card in laneCards(lane.value)" :key="card.id" size="small" class="lane-card">
            <div class="card-head">
              <a-tag :color="levelColor(card.level)">{{ levelText(card.level) }}</a-tag>
              <span class="card-id">#{{ card.id }}</span>
            </div>
            <div class="card-line">告警 {{ card.alertId }}<template v-if="card.elderId"> · 长者 {{ card.elderId }}</template></div>
            <div class="card-line">处置人：{{ card.assigneeName || '待指派' }}</div>
            <div v-if="(card.escalationCount || 0) > 0" class="card-line">
              <a-tag color="volcano">升级 {{ card.escalationCount }} 次</a-tag>
              <span v-if="card.escalatedToName">→ {{ card.escalatedToName }}</span>
            </div>
            <div v-if="card.responseDeadline && ['TRIGGERED', 'ASSIGNED'].includes(card.dispatchStatus || '')" class="card-line card-deadline">
              响应时限 {{ card.responseDeadline }}
            </div>
            <div class="row-action-links">
              <a-button v-if="card.dispatchStatus === 'TRIGGERED'" type="link" size="small" @click="openAssign(card)">受理</a-button>
              <a-button v-if="['TRIGGERED', 'ASSIGNED'].includes(card.dispatchStatus || '')" type="link" size="small" @click="act('respond', card)">响应</a-button>
              <a-button v-if="card.dispatchStatus === 'RESPONDED'" type="link" size="small" @click="act('onsite', card)">到场</a-button>
              <a-button v-if="['RESPONDED', 'ONSITE'].includes(card.dispatchStatus || '')" type="link" size="small" @click="openHandle(card)">处置</a-button>
              <a-button v-if="card.dispatchStatus === 'HANDLED'" type="link" size="small" @click="openReview(card)">复盘</a-button>
              <span v-if="card.dispatchStatus === 'REVIEWED'" class="text-muted">已闭环</span>
            </div>
          </a-card>
          <a-empty v-if="laneCards(lane.value).length === 0" :image-style="{ height: '40px' }" description="暂无" />
        </div>
      </div>
    </div>

    <DataTable v-else rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'level'">
          <a-tag :color="levelColor(record.level)">{{ levelText(record.level) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'dispatchStatus'">
          <a-badge :status="statusBadge(record.dispatchStatus)" :text="statusText(record.dispatchStatus)" />
        </template>
        <template v-else-if="column.key === 'escalationCount'">
          <template v-if="record.escalationCount > 0">
            <a-tag color="volcano">升级 {{ record.escalationCount }} 次</a-tag>
            <span v-if="record.escalatedToName">→ {{ record.escalatedToName }}</span>
          </template>
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
        <a-form-item label="一键生成不良事件">
          <a-switch v-model:checked="actionForm.createIncident" />
          <span class="form-tip">开启后自动登记不良事件（危急派单登记为重大事故），无需手工填写事件ID</span>
        </a-form-item>
        <a-form-item v-if="!actionForm.createIncident" label="关联不良事件ID">
          <a-input-number v-model:value="actionForm.incidentId" style="width: 100%" placeholder="选填，如已登记事故" />
        </a-form-item>
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
import { reactive, ref, watch } from 'vue'
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
const viewMode = ref<'board' | 'table'>('board')
const rows = ref<SmartAlertDispatch[]>([])
const boardRows = ref<SmartAlertDispatch[]>([])
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
const actionForm = reactive({
  dispatchId: undefined as Id | undefined,
  note: '',
  incidentId: undefined as number | undefined,
  createIncident: false
})

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
async function fetchBoard() {
  loading.value = true
  try {
    const res: PageResult<SmartAlertDispatch> = await getSmartDispatchPage({
      pageNo: 1,
      pageSize: 200,
      level: query.level
    })
    boardRows.value = res.list
  } finally {
    loading.value = false
  }
}
function refresh() {
  if (viewMode.value === 'board') {
    fetchBoard()
  } else {
    fetchData()
  }
}
function laneCards(status: string) {
  return boardRows.value.filter((d) => d.dispatchStatus === status)
}
watch(viewMode, refresh)
function onSearch() {
  query.pageNo = 1
  pagination.current = 1
  refresh()
}
function onReset() {
  query.status = undefined
  query.level = undefined
  query.pageNo = 1
  pagination.current = 1
  refresh()
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
  refresh()
}

async function act(type: 'respond' | 'onsite', record: SmartAlertDispatch) {
  if (type === 'respond') await respondSmartDispatch(record.id)
  else await onsiteSmartDispatch(record.id)
  message.success('已更新')
  refresh()
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
    refresh()
  } finally {
    saving.value = false
  }
}

function openHandle(record: SmartAlertDispatch) {
  actionForm.dispatchId = record.id
  actionForm.note = ''
  actionForm.incidentId = undefined
  actionForm.createIncident = false
  handleOpen.value = true
}
async function submitHandle() {
  if (!actionForm.note) {
    message.error('请填写处置说明')
    return
  }
  saving.value = true
  try {
    await handleSmartDispatch({
      dispatchId: actionForm.dispatchId as Id,
      note: actionForm.note,
      incidentId: actionForm.createIncident ? undefined : actionForm.incidentId,
      createIncident: actionForm.createIncident || undefined
    })
    message.success(actionForm.createIncident ? '处置完成，已生成不良事件' : '处置完成')
    handleOpen.value = false
    refresh()
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
    refresh()
  } finally {
    saving.value = false
  }
}

refresh()
</script>

<style scoped>
.text-muted { color: #52c41a; }
.form-tip {
  margin-left: 8px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
.dispatch-board {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 8px;
}
.board-lane {
  flex: 1 0 200px;
  min-width: 200px;
  background: #fafafa;
  border-radius: 8px;
  padding: 8px;
}
.lane-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 6px 8px;
  font-weight: 600;
}
.lane-count {
  color: rgba(0, 0, 0, 0.45);
  font-weight: 400;
}
.lane-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 60px;
  max-height: 62vh;
  overflow-y: auto;
}
.lane-card .card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.lane-card .card-id {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
.lane-card .card-line {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.65);
  margin-bottom: 2px;
}
.lane-card .card-deadline {
  color: #fa541c;
}
</style>
