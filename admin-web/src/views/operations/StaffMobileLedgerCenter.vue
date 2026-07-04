<template>
  <PageContainer title="员工现场执行台账" subTitle="把员工小程序提交的任务、回执、交接和异常上报集中给后台查看与追踪">
    <template #extra>
      <a-space wrap>
        <a-select v-model:value="module" style="width: 170px" @change="reloadAll">
          <a-select-option value="ALL">全部模块</a-select-option>
          <a-select-option value="CARE">护理</a-select-option>
          <a-select-option value="MEDICATION">用药</a-select-option>
          <a-select-option value="INSPECTION">巡检</a-select-option>
          <a-select-option value="LOGISTICS">维修</a-select-option>
          <a-select-option value="MEAL">送餐</a-select-option>
        </a-select>
        <a-button :loading="loading" @click="reloadAll">刷新</a-button>
        <a-button @click="openHealthData()">体征补录</a-button>
        <a-button @click="go('/oa/staff-collaboration')">员工协同</a-button>
        <a-button @click="go('/stats/staff-mobile')">返回员工移动端中心</a-button>
      </a-space>
    </template>

    <section class="summary-grid">
      <div class="summary-card">
        <span>待执行任务</span>
        <strong>{{ taskSummary.pending }}</strong>
        <small>总任务 {{ tasks.length }} · 需证据 {{ taskSummary.evidenceRequired }}</small>
      </div>
      <div class="summary-card">
        <span>回执留痕</span>
        <strong>{{ receipts.length }}</strong>
        <small>含照片 {{ receiptSummary.photo }} · 含语音 {{ receiptSummary.voice }}</small>
      </div>
      <div class="summary-card">
        <span>班次交接</span>
        <strong>{{ handovers.length }}</strong>
        <small>最近一批员工移动端交接记录</small>
      </div>
      <div class="summary-card">
        <span>异常上报</span>
        <strong>{{ incidents.length }}</strong>
        <small>开放 {{ incidentSummary.open }} · 已取证 {{ incidentSummary.evidence }}</small>
      </div>
      <button class="summary-card summary-button" type="button" @click="openHealthData(true)">
        <span>异常体征处理</span>
        <strong>{{ incidentSummary.open }}</strong>
        <small>进入健康数据页筛选员工端今日异常记录继续处理</small>
      </button>
    </section>

    <a-alert
      class="ledger-alert"
      type="info"
      show-icon
      message="员工提交后，后台至少要能看到任务来源、提交时间、照片语音证据和异常状态。"
      description="这里优先承接现场执行台账，后续可以再往审批、绩效和质检复盘联动。"
    />

    <a-tabs v-model:activeKey="activeTab">
      <a-tab-pane key="tasks" tab="待办任务">
        <a-table :columns="taskColumns" :data-source="tasks" :pagination="{ pageSize: 8 }" row-key="id" :scroll="{ x: 1100 }">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
            </template>
            <template v-else-if="column.key === 'priority'">
              <a-tag :color="priorityColor(record.priority)">{{ priorityText(record.priority) }}</a-tag>
            </template>
            <template v-else-if="column.key === 'checklist'">
              <div class="chip-row">
                <span v-for="item in record.checklist" :key="item" class="soft-chip">{{ item }}</span>
              </div>
            </template>
            <template v-else-if="column.key === 'action'">
              <div class="row-action-links">
                <a-button size="small" type="link" @click="go(record.route)">打开业务页</a-button>
                <a-button v-if="record.evidenceRequired" size="small" type="link" @click="activeTab = 'receipts'">查看回执</a-button>
              </div>
            </template>
          </template>
        </a-table>
      </a-tab-pane>

      <a-tab-pane key="receipts" tab="回执记录">
        <a-table :columns="receiptColumns" :data-source="receipts" :pagination="{ pageSize: 8 }" row-key="id" :scroll="{ x: 1200 }">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
            </template>
            <template v-else-if="column.key === 'evidence'">
              <div class="evidence-block">
                <span>照片 {{ record.photos?.length || 0 }}</span>
                <span>语音 {{ record.voiceUrl ? `${record.voiceDurationSec || 0} 秒` : '无' }}</span>
              </div>
            </template>
            <template v-else-if="column.key === 'checkedItems'">
              <div class="cell-prewrap">{{ record.checkedItems || '-' }}</div>
            </template>
            <template v-else-if="column.key === 'remark'">
              <div class="cell-prewrap">{{ record.remark || record.scanText || '-' }}</div>
            </template>
            <template v-else-if="column.key === 'action'">
              <div class="row-action-links">
                <a-button size="small" type="link" @click="go(record.taskRoute || '/stats/staff-mobile-ledger')">任务台账</a-button>
                <a-button size="small" type="link" @click="go(record.adminRoute || '/stats/staff-mobile-ledger')">业务处理</a-button>
              </div>
            </template>
          </template>
        </a-table>
      </a-tab-pane>

      <a-tab-pane key="handovers" tab="交接记录">
        <a-table :columns="handoverColumns" :data-source="handovers" :pagination="{ pageSize: 8 }" row-key="id" :scroll="{ x: 900 }">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'content'">
              <div class="cell-prewrap">{{ record.content }}</div>
            </template>
          </template>
        </a-table>
      </a-tab-pane>

      <a-tab-pane key="incidents" tab="异常上报">
        <a-table :columns="incidentColumns" :data-source="incidents" :pagination="{ pageSize: 8 }" row-key="id" :scroll="{ x: 1200 }">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
            </template>
            <template v-else-if="column.key === 'level'">
              <a-tag :color="incidentLevelColor(record.level)">{{ record.level || '-' }}</a-tag>
            </template>
            <template v-else-if="column.key === 'evidence'">
              <div class="evidence-block">
                <span>照片 {{ record.photos?.length || 0 }}</span>
                <span>语音 {{ record.voiceUrl ? `${record.voiceDurationSec || 0} 秒` : '无' }}</span>
              </div>
            </template>
            <template v-else-if="column.key === 'description'">
              <div class="cell-prewrap">{{ record.description || '-' }}</div>
            </template>
            <template v-else-if="column.key === 'actionTaken'">
              <div class="cell-prewrap">{{ record.actionTaken || '-' }}</div>
            </template>
            <template v-else-if="column.key === 'action'">
              <div class="row-action-links">
                <a-button size="small" type="link" @click="activeTab = 'receipts'">关联回执</a-button>
                <a-button size="small" type="link" @click="go(record.adminRoute || '/life/incident')">异常处理</a-button>
              </div>
            </template>
          </template>
        </a-table>
      </a-tab-pane>
    </a-tabs>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import {
  getOperationsStaffMobileHandovers,
  getOperationsStaffMobileIncidents,
  getOperationsStaffMobileTaskReceipts,
  getOperationsStaffMobileTasks,
  type OperationsStaffMobileHandover,
  type OperationsStaffMobileIncidentView,
  type OperationsStaffMobileTask,
  type OperationsStaffMobileTaskReceiptView
} from '../../api/operations'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('tasks')
const module = ref('ALL')
const tasks = ref<OperationsStaffMobileTask[]>([])
const receipts = ref<OperationsStaffMobileTaskReceiptView[]>([])
const handovers = ref<OperationsStaffMobileHandover[]>([])
const incidents = ref<OperationsStaffMobileIncidentView[]>([])

const taskColumns = [
  { title: '模块', dataIndex: 'module', key: 'module', width: 100 },
  { title: '任务', dataIndex: 'title', key: 'title', width: 220 },
  { title: '老人', dataIndex: 'resident', key: 'resident', width: 120 },
  { title: '房间', dataIndex: 'room', key: 'room', width: 100 },
  { title: '时间', dataIndex: 'time', key: 'time', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 110 },
  { title: '执行清单', dataIndex: 'checklist', key: 'checklist', width: 240 },
  { title: '操作', key: 'action', width: 170, fixed: 'right' as const }
]

const receiptColumns = [
  { title: '模块', dataIndex: 'moduleText', key: 'moduleText', width: 120 },
  { title: '任务', dataIndex: 'taskTitle', key: 'taskTitle', width: 200 },
  { title: '老人', dataIndex: 'resident', key: 'resident', width: 120 },
  { title: '房间', dataIndex: 'room', key: 'room', width: 100 },
  { title: '回执时间', dataIndex: 'receiptTime', key: 'receiptTime', width: 180 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '勾选项', dataIndex: 'checkedItems', key: 'checkedItems', width: 220 },
  { title: '现场说明', dataIndex: 'remark', key: 'remark', width: 240 },
  { title: '证据', key: 'evidence', width: 150 },
  { title: '操作', key: 'action', width: 210, fixed: 'right' as const }
]

const handoverColumns = [
  { title: '交接时间', dataIndex: 'time', key: 'time', width: 160 },
  { title: '标题', dataIndex: 'title', key: 'title', width: 220 },
  { title: '交接人', dataIndex: 'owner', key: 'owner', width: 140 },
  { title: '交接内容', dataIndex: 'content', key: 'content' }
]

const incidentColumns = [
  { title: '发生时间', dataIndex: 'incidentTime', key: 'incidentTime', width: 180 },
  { title: '类型', dataIndex: 'incidentType', key: 'incidentType', width: 120 },
  { title: '等级', dataIndex: 'level', key: 'level', width: 110 },
  { title: '老人', dataIndex: 'resident', key: 'resident', width: 120 },
  { title: '位置', dataIndex: 'location', key: 'location', width: 130 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '描述', dataIndex: 'description', key: 'description', width: 240 },
  { title: '处置', dataIndex: 'actionTaken', key: 'actionTaken', width: 220 },
  { title: '证据', key: 'evidence', width: 150 },
  { title: '操作', key: 'action', width: 190, fixed: 'right' as const }
]

const normalizedModule = computed(() => (module.value === 'ALL' ? undefined : module.value))
const taskSummary = computed(() => ({
  pending: tasks.value.filter((item) => !isDone(item.status)).length,
  evidenceRequired: tasks.value.filter((item) => item.evidenceRequired).length
}))
const receiptSummary = computed(() => ({
  photo: receipts.value.filter((item) => (item.photos?.length || 0) > 0).length,
  voice: receipts.value.filter((item) => !!item.voiceUrl).length
}))
const incidentSummary = computed(() => ({
  open: incidents.value.filter((item) => item.status === 'OPEN').length,
  evidence: incidents.value.filter((item) => (item.photos?.length || 0) > 0 || !!item.voiceUrl).length
}))

function go(path?: string) {
  if (!path) return
  router.push(path)
}

function openHealthData(abnormalOnly = false) {
  router.push({
    path: '/health/management/data',
    query: {
      source: 'staff-mobile',
      date: 'today',
      ...(abnormalOnly ? { abnormalFlag: '1' } : {})
    }
  })
}

function statusText(status?: string) {
  if (status === 'DONE' || status === 'DELIVERED') return '已完成'
  if (status === 'OPEN') return '待处理'
  if (status === 'PROCESSING') return '处理中'
  if (status === 'PENDING') return '待执行'
  return status || '未知'
}

function statusColor(status?: string) {
  if (status === 'DONE' || status === 'DELIVERED') return 'green'
  if (status === 'OPEN' || status === 'PENDING') return 'orange'
  if (status === 'PROCESSING') return 'blue'
  return 'default'
}

function priorityText(priority?: string) {
  if (priority === 'HIGH') return '高'
  if (priority === 'MEDIUM') return '中'
  return priority || '低'
}

function priorityColor(priority?: string) {
  if (priority === 'HIGH') return 'red'
  if (priority === 'MEDIUM') return 'gold'
  return 'default'
}

function incidentLevelColor(level?: string) {
  if (level === '严重' || level === 'MAJOR') return 'red'
  if (level === '一般' || level === 'MEDIUM') return 'gold'
  return 'blue'
}

function isDone(status?: string) {
  return status === 'DONE' || status === 'DELIVERED' || status === 'CLOSED'
}

async function reloadAll() {
  loading.value = true
  try {
    const [taskRows, receiptRows, handoverRows, incidentRows] = await Promise.all([
      getOperationsStaffMobileTasks(normalizedModule.value),
      getOperationsStaffMobileTaskReceipts(normalizedModule.value),
      getOperationsStaffMobileHandovers(),
      getOperationsStaffMobileIncidents()
    ])
    tasks.value = taskRows || []
    receipts.value = receiptRows || []
    handovers.value = handoverRows || []
    incidents.value = incidentRows || []
  } catch (error: any) {
    message.error(error?.message || '员工现场执行台账加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  reloadAll()
})
</script>

<style scoped>
.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.summary-card {
  display: grid;
  gap: 10px;
  min-height: 128px;
  padding: 18px;
  border: 1px solid var(--border-soft);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
}

.summary-button {
  width: 100%;
  text-align: left;
  cursor: pointer;
}

.summary-card span,
.summary-card small,
.cell-prewrap,
.evidence-block span {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.7;
}

.summary-card strong {
  color: var(--ink);
  font-size: 30px;
  line-height: 1.1;
}

.ledger-alert {
  margin-bottom: 16px;
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.soft-chip {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: var(--surface-2);
  color: var(--muted);
  font-size: 12px;
  line-height: 1;
}

.cell-prewrap {
  white-space: pre-wrap;
}

.evidence-block {
  display: grid;
  gap: 4px;
}

@media (max-width: 1280px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
