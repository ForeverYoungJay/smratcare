<template>
  <div class="ib">
    <section class="ib-list">
      <div class="ib-bar">
        <a-input-search v-model:value="filters.keyword" allow-clear placeholder="长者、编号或位置" style="width: 200px" @search="search" />
        <a-select v-model:value="filters.incidentType" allow-clear placeholder="全部类别" style="width: 130px" :options="typeOptions" @change="search" />
        <a-select v-model:value="filters.status" allow-clear placeholder="全部状态" style="width: 120px" :options="statusOptions" @change="search" />
        <a-button @click="exportRows">导出</a-button>
        <a-button type="primary" @click="openCreate()">新增异常事件</a-button>
      </div>
      <a-table row-key="id" size="middle" :loading="loading" :data-source="rows" :columns="columns" :custom-row="rowProps"
        :row-class-name="(r: CareIncidentItem) => (r.id === selected?.id ? 'ib-active' : '')"
        :pagination="{ current: page.pageNo, pageSize: page.pageSize, total: page.total, onChange: (no: number) => { page.pageNo = no; load() } }">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'time'">{{ fmt(record.occurredAt) }}<br><small class="ib-muted">{{ record.incidentNo }}</small></template>
          <template v-else-if="column.key === 'elder'">{{ record.elderName || '--' }}<br><small class="ib-muted">{{ record.location || '' }}</small></template>
          <template v-else-if="column.key === 'type'">{{ INCIDENT_TYPES[record.incidentType] || record.incidentType }}</template>
          <template v-else-if="column.key === 'severity'"><span class="rm-pill" :class="`rm-pill--${INCIDENT_SEVERITY[record.severity]?.tone}`">{{ INCIDENT_SEVERITY[record.severity]?.label || record.severity }}</span></template>
          <template v-else-if="column.key === 'status'"><span class="rm-pill" :class="`rm-pill--${INCIDENT_STATUS[record.status]?.tone}`">{{ INCIDENT_STATUS[record.status]?.label || record.status }}</span></template>
        </template>
      </a-table>
      <p class="ib-muted">事件记录须留存事实、时间、处置人与复核人，不允许删除；写错的内容用「更正说明」补充。</p>
    </section>

    <section class="ib-detail">
      <div v-if="!selected" class="ib-empty">点左侧事件查看处置详情</div>
      <template v-else>
        <div class="ib-head">
          <div><strong>{{ selected.elderName || selected.location || '未关联长者' }} · {{ INCIDENT_TYPES[selected.incidentType] || selected.incidentType }}</strong>
            <small class="ib-muted">{{ selected.incidentNo }} · {{ fmt(selected.occurredAt) }} · {{ selected.location || '--' }}</small></div>
          <span class="rm-pill" :class="`rm-pill--${INCIDENT_STATUS[selected.status]?.tone}`">{{ INCIDENT_STATUS[selected.status]?.label }}</span>
        </div>
        <dl class="ib-facts">
          <dt>发现人员</dt><dd>{{ selected.reporterName || '--' }}</dd>
          <dt>事件描述</dt><dd>{{ selected.description }}</dd>
          <dt>已采取措施</dt><dd>{{ selected.measures || '--' }}</dd>
          <dt>责任人</dt><dd>{{ selected.ownerName || '未指派' }}</dd>
          <dt v-if="selected.escalateTo">已升级至</dt><dd v-if="selected.escalateTo">{{ ESCALATE_TARGETS[selected.escalateTo] || selected.escalateTo }}</dd>
          <dt v-if="selected.reviewNote">复核意见</dt><dd v-if="selected.reviewNote">{{ selected.reviewerName }}：{{ selected.reviewNote }}</dd>
        </dl>
        <div class="rm-section-title">处理进度</div>
        <a-timeline class="ib-timeline">
          <a-timeline-item v-for="item in selected.actions || []" :key="item.id" :color="item.action === 'CORRECT' ? 'orange' : 'green'">
            <strong>{{ fmt(item.createTime) }} {{ INCIDENT_ACTION_LABELS[item.action] || item.action }}</strong>
            <div class="ib-muted">{{ item.operatorName }}{{ item.note ? `：${item.note}` : '' }}</div>
          </a-timeline-item>
        </a-timeline>
        <a-space wrap>
          <a-button v-for="action in actions" :key="action" :type="action === 'CORRECT' ? 'default' : 'primary'" :danger="action === 'REOPEN'" @click="openAction(action)">
            {{ INCIDENT_ACTION_LABELS[action] }}
          </a-button>
        </a-space>
        <a-alert v-if="selected.severity === 'HIGH' && selected.status !== 'CLOSED'" class="ib-gap" type="error" show-icon
          message="高风险事件：涉及医疗处置请立即「升级」到医务部并按应急预案处理，不能只在系统里点处理。" />
      </template>
    </section>

    <a-modal v-model:open="createOpen" title="上报异常事件" :confirm-loading="saving" width="640" @ok="submitCreate">
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="事件类型" required><a-select v-model:value="draft.incidentType" :options="typeOptions" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="风险级别" required><a-radio-group v-model:value="draft.severity"><a-radio v-for="(m, k) in INCIDENT_SEVERITY" :key="k" :value="k">{{ m.label }}</a-radio></a-radio-group></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="长者"><a-select v-model:value="draft.elderId" show-search allow-clear :filter-option="false" :options="elderOptions" placeholder="输入姓名搜索" @search="searchElders" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="地点"><a-input v-model:value="draft.location" placeholder="楼栋/房间/区域" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="发生时间" required><a-date-picker v-model:value="draft.occurredAt" show-time value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="通知对象"><a-input v-model:value="draft.notifyTargets" placeholder="例如：责任护士、家属" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="事实描述" required><a-textarea v-model:value="draft.description" :rows="3" :maxlength="500" show-count placeholder="写发生了什么、当时状态，不写推测" /></a-form-item>
        <a-form-item label="已采取措施"><a-textarea v-model:value="draft.measures" :rows="2" :maxlength="500" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="actionOpen" :title="INCIDENT_ACTION_LABELS[pendingAction]" :confirm-loading="saving" @ok="submitAction">
      <a-form layout="vertical">
        <a-form-item v-if="pendingAction === 'ESCALATE'" label="升级到" required>
          <a-select v-model:value="actionForm.escalateTo" :options="Object.entries(ESCALATE_TARGETS).map(([value, label]) => ({ value, label }))" />
        </a-form-item>
        <a-form-item v-if="pendingAction === 'ASSIGN'" label="责任人" required>
          <a-select v-model:value="actionForm.ownerId" show-search :filter-option="false" :options="staffOptions" placeholder="输入姓名搜索" @search="searchStaff" />
        </a-form-item>
        <a-form-item :label="noteLabel" :required="incidentActionNeedsNote(pendingAction)">
          <a-textarea v-model:value="actionForm.note" :rows="3" :maxlength="500" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { actOnIncident, createIncident, getIncident, getIncidentPage } from '../../../api/nursing'
import { useElderOptions } from '../../../composables/useElderOptions'
import { useStaffOptions } from '../../../composables/useStaffOptions'
import { useUserStore } from '../../../stores/user'
import { exportCsv } from '../../../utils/export'
import type { CareIncidentItem } from '../../../types'
import { resolveCareError } from '../careError'
import {
  ESCALATE_TARGETS, INCIDENT_ACTION_LABELS, INCIDENT_SEVERITY, INCIDENT_STATUS, INCIDENT_TYPES,
  incidentActionNeedsNote, incidentActions, type IncidentAction
} from './nursingModel'
import '../../elder/residentUi.css'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { elderOptions, searchElders, ensureSelectedElder } = useElderOptions({ pageSize: 50, inHospitalOnly: true })
const { staffOptions, searchStaff } = useStaffOptions({ pageSize: 120 })
const manager = computed(() => (userStore.roles || []).some((r) => ['NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'].includes(String(r || '').toUpperCase())))

const loading = ref(false)
const saving = ref(false)
const rows = ref<CareIncidentItem[]>([])
const selected = ref<CareIncidentItem | null>(null)
const page = reactive({ pageNo: 1, pageSize: 10, total: 0 })
const filters = reactive({ keyword: '', incidentType: undefined as string | undefined, status: undefined as string | undefined })
const typeOptions = Object.entries(INCIDENT_TYPES).map(([value, label]) => ({ value, label }))
const statusOptions = Object.entries(INCIDENT_STATUS).map(([value, m]) => ({ value, label: m.label }))
const columns = [
  { title: '时间 / 编号', key: 'time', width: 150 },
  { title: '长者 / 位置', key: 'elder', width: 130 },
  { title: '事件类型', key: 'type', width: 120 },
  { title: '风险', key: 'severity', width: 70 },
  { title: '进度', key: 'status', width: 90 }
]
const actions = computed(() => (selected.value ? incidentActions(selected.value.status, manager.value) : []))

const createOpen = ref(false)
const blankDraft = () => ({ incidentType: 'FALL', severity: 'MEDIUM', elderId: undefined as string | undefined, location: '', occurredAt: dayjs().format('YYYY-MM-DDTHH:mm:ss'), notifyTargets: '', description: '', measures: '' })
const draft = reactive(blankDraft())
const actionOpen = ref(false)
const pendingAction = ref<IncidentAction>('ACCEPT')
const actionForm = reactive({ note: '', escalateTo: undefined as string | undefined, ownerId: undefined as string | undefined })
const noteLabel = computed(() => ({ SUBMIT_REVIEW: '处置结果', CLOSE: '复核意见', REOPEN: '退回原因', CORRECT: '更正说明' } as Record<string, string>)[pendingAction.value] || '说明')

function fmt(value?: string) { return value ? dayjs(value).format('MM-DD HH:mm') : '--' }
function rowProps(record: CareIncidentItem) { return { onClick: () => select(record.id) } }

async function load() {
  loading.value = true
  try {
    const res = await getIncidentPage({ ...page, ...filters, keyword: filters.keyword || undefined })
    rows.value = res.list || []
    page.total = res.total
    if (!selected.value && rows.value.length) await select(rows.value[0].id)
  } catch (error) {
    message.error(resolveCareError(error, '异常事件加载失败'))
  } finally {
    loading.value = false
  }
}

function search() { page.pageNo = 1; load() }

async function select(id: number) {
  selected.value = await getIncident(id)
}

function openCreate(elderId?: string) {
  Object.assign(draft, blankDraft())
  if (elderId) { draft.elderId = elderId; ensureSelectedElder(elderId) }
  createOpen.value = true
}

async function submitCreate() {
  if (!draft.description.trim() || !draft.occurredAt) return message.warning('请填写发生时间和事实描述')
  saving.value = true
  try {
    const elderName = elderOptions.value.find((o) => String(o.value) === String(draft.elderId))?.name
    const created = await createIncident({ ...draft, elderName })
    createOpen.value = false
    message.success('已上报，已发到护士长（或责任人）的待办')
    selected.value = null
    await load()
    await select(created.id)
  } catch (error) {
    message.error(resolveCareError(error, '上报失败'))
  } finally {
    saving.value = false
  }
}

function openAction(action: IncidentAction) {
  pendingAction.value = action
  Object.assign(actionForm, { note: '', escalateTo: undefined, ownerId: undefined })
  actionOpen.value = true
}

async function submitAction() {
  const action = pendingAction.value
  if (incidentActionNeedsNote(action) && !actionForm.note.trim()) return message.warning(`请填写${noteLabel.value}`)
  if (action === 'ESCALATE' && !actionForm.escalateTo) return message.warning('请选择升级对象')
  if (action === 'ASSIGN' && !actionForm.ownerId) return message.warning('请选择责任人')
  saving.value = true
  try {
    const ownerName = staffOptions.value.find((o) => o.value === actionForm.ownerId)?.name
    await actOnIncident(selected.value!.id, { action, note: actionForm.note.trim() || undefined, escalateTo: actionForm.escalateTo, ownerId: actionForm.ownerId ? Number(actionForm.ownerId) : undefined, ownerName })
    actionOpen.value = false
    message.success('已记录')
    await select(selected.value!.id)
    await load()
  } catch (error) {
    message.error(resolveCareError(error, '操作失败'))
  } finally {
    saving.value = false
  }
}

function exportRows() {
  exportCsv(rows.value.map((r) => ({
    编号: r.incidentNo, 发生时间: r.occurredAt, 长者: r.elderName || '', 位置: r.location || '', 类型: INCIDENT_TYPES[r.incidentType] || r.incidentType,
    风险: INCIDENT_SEVERITY[r.severity]?.label || r.severity, 进度: INCIDENT_STATUS[r.status]?.label || r.status, 发现人: r.reporterName || '', 责任人: r.ownerName || '', 描述: r.description
  })), `异常事件-${dayjs().format('YYYYMMDD')}`)
}

onMounted(async () => {
  await load()
  if (route.query.id) await select(Number(route.query.id))
  if (route.query.action === 'create') {
    openCreate(typeof route.query.elderId === 'string' ? route.query.elderId : undefined)
    const { action: _a, elderId: _e, ...rest } = route.query
    router.replace({ query: rest })
  }
})
</script>

<style scoped>
.ib { display: grid; grid-template-columns: minmax(0, 1.4fr) minmax(320px, 1fr); gap: 16px; }
.ib-bar { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.ib-muted { color: #8a968f; font-size: 12px; }
.ib-list :deep(.ib-active) td { background: #eef8f3 !important; }
.ib-list :deep(tbody tr) { cursor: pointer; }
.ib-detail { padding: 16px; border: 1px solid #e7ece8; border-radius: 12px; }
.ib-empty { padding: 40px 0; color: #8a968f; text-align: center; }
.ib-head { display: flex; justify-content: space-between; gap: 8px; margin-bottom: 12px; }
.ib-head > div { display: grid; gap: 4px; }
.ib-facts { display: grid; grid-template-columns: 84px 1fr; gap: 8px 12px; margin: 0 0 12px; padding-top: 12px; border-top: 1px solid #edf0ed; }
.ib-facts dt { color: #7b877e; }
.ib-facts dd { margin: 0; color: #1e3027; white-space: pre-wrap; }
.ib-timeline { margin-top: 12px; }
.ib-gap { margin-top: 12px; }
@media (max-width: 1100px) { .ib { grid-template-columns: 1fr; } }
</style>
