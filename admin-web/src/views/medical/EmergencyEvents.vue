<template>
  <PageContainer title="120急救事件" subTitle="一键急救、120呼叫登记、送医与转归全流程闭环，含抢救记录单">
    <SearchForm :model="query" @search="onSearch" @reset="onReset">
      <a-form-item label="长者">
        <a-select v-model:value="query.elderId" show-search allow-clear :filter-option="false" :options="elderOptions"
          placeholder="输入姓名搜索" style="width: 200px" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 140px" placeholder="全部">
          <a-select-option v-for="s in statusOptions" :key="s.value" :value="s.value">{{ s.label }}</a-select-option>
        </a-select>
      </a-form-item>
      <template #extra>
        <a-button type="primary" danger @click="openCreate">一键急救</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'eventTime'">
          {{ formatEventTime(record.eventTime) }}
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'outcome'">
          <a-tag v-if="record.outcome" :color="outcomeColor(record.outcome)">{{ outcomeText(record.outcome) }}</a-tag>
          <span v-else class="text-muted">—</span>
        </template>
        <template v-else-if="column.key === 'alertId'">
          <a-tag v-if="record.alertId" color="volcano">SOS告警</a-tag>
          <span v-else class="text-muted">—</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button v-if="record.status === 'INITIATED'" type="link" size="small" @click="doCall(record)">呼叫120</a-button>
            <a-button v-if="record.status === 'CALLED'" type="link" size="small" @click="openDepart(record)">送医</a-button>
            <a-button v-if="record.status === 'TRANSFERRED'" type="link" size="small" @click="openOutcome(record)">转归</a-button>
            <a-button v-if="['RETURNED', 'HOSPITALIZED', 'DECEASED'].includes(record.status)" type="link" size="small" @click="doClose(record)">关闭</a-button>
            <a-button v-if="['INITIATED', 'CALLED'].includes(record.status)" type="link" size="small" danger @click="doCancel(record)">取消</a-button>
            <a-button type="link" size="small" @click="openRescue(record)">抢救记录</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="createOpen" title="一键急救" :confirm-loading="saving" width="600px" @ok="submitCreate">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="长者" required>
              <a-select v-model:value="createForm.elderId" show-search :filter-option="false" :options="elderOptions"
                placeholder="输入姓名搜索" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
            </a-form-item>
          </a-col>
          <a-col :span="12"><a-form-item label="发生位置"><a-input v-model:value="createForm.location" placeholder="如 2号楼301房" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="症状描述" required><a-textarea v-model:value="createForm.symptom" :rows="2" placeholder="如 突发意识不清、跌倒后无法起身" /></a-form-item>
        <a-form-item label="关联告警ID（IoT SOS）"><a-input-number v-model:value="createForm.alertId" style="width: 100%" placeholder="来自 SOS 告警时填写" /></a-form-item>
        <a-form-item label="备注"><a-input v-model:value="createForm.remark" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="departOpen" title="送医登记" :confirm-loading="saving" width="520px" @ok="submitDepart">
      <a-form layout="vertical">
        <a-form-item label="送医医院" required><a-input v-model:value="departForm.hospitalName" placeholder="如 市第一人民医院" /></a-form-item>
        <a-form-item label="陪同人"><a-input v-model:value="departForm.escortName" placeholder="陪同人员姓名" /></a-form-item>
        <a-form-item label="出发时间"><a-date-picker v-model:value="departForm.departTime" show-time value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" placeholder="默认当前时间" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="outcomeOpen" title="转归登记" :confirm-loading="saving" width="520px" @ok="submitOutcome">
      <a-form layout="vertical">
        <a-form-item label="转归结果" required>
          <a-select v-model:value="outcomeForm.outcome">
            <a-select-option value="RETURNED">返回机构</a-select-option>
            <a-select-option value="HOSPITALIZED">住院治疗</a-select-option>
            <a-select-option value="DECEASED">死亡</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="转归说明"><a-textarea v-model:value="outcomeForm.outcomeNote" :rows="2" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="rescueOpen" title="抢救记录单" :confirm-loading="saving" width="720px" @ok="submitRescue">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="开始时间"><a-date-picker v-model:value="rescueForm.startTime" show-time value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="结束时间"><a-date-picker v-model:value="rescueForm.endTime" show-time value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="时间线明细（JSON）">
          <a-textarea v-model:value="rescueForm.timelineJson" :rows="3" placeholder='如 [{"time":"10:02","action":"胸外按压"},{"time":"10:05","action":"静推肾上腺素"}]' />
        </a-form-item>
        <a-form-item label="参与人员"><a-input v-model:value="rescueForm.participants" placeholder="如 张医生、李护士" /></a-form-item>
        <a-form-item label="抢救用药"><a-input v-model:value="rescueForm.drugsUsed" placeholder="如 肾上腺素 1mg 静推" /></a-form-item>
        <a-form-item label="抢救措施"><a-textarea v-model:value="rescueForm.measures" :rows="2" placeholder="如 胸外按压、球囊面罩通气、吸氧" /></a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="抢救结果">
              <a-select v-model:value="rescueForm.result" allow-clear>
                <a-select-option value="SUCCESS">抢救成功</a-select-option>
                <a-select-option value="TRANSFERRED">转院</a-select-option>
                <a-select-option value="FAILED">无效</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12"><a-form-item label="结果说明"><a-input v-model:value="rescueForm.resultNote" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import {
  getEmergencyEventPage,
  createEmergencyEvent,
  transitionEmergencyEvent,
  saveRescueRecord,
  getRescueRecordByEvent,
  type MedicalEmergencyEvent
} from '../../api/medicalEmergency'
import type { Id, PageResult } from '../../types'

const statusOptions = [
  { label: '已发起', value: 'INITIATED' },
  { label: '已呼叫120', value: 'CALLED' },
  { label: '送医中', value: 'TRANSFERRED' },
  { label: '已返回', value: 'RETURNED' },
  { label: '已住院', value: 'HOSPITALIZED' },
  { label: '已关闭', value: 'CLOSED' }
]

const loading = ref(false)
const saving = ref(false)
const rows = ref<MedicalEmergencyEvent[]>([])
const query = reactive({ elderId: undefined as Id | undefined, status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const { elderOptions, searchElders } = useElderOptions({ pageSize: 50 })

const columns = [
  { title: '长者', dataIndex: 'elderName', key: 'elderName', width: 100 },
  { title: '发起时间', dataIndex: 'eventTime', key: 'eventTime', width: 160 },
  { title: '位置', dataIndex: 'location', key: 'location', width: 120 },
  { title: '症状', dataIndex: 'symptom', key: 'symptom', ellipsis: true },
  { title: '来源', key: 'alertId', width: 90 },
  { title: '送医医院', dataIndex: 'hospitalName', key: 'hospitalName', width: 130 },
  { title: '状态', key: 'status', width: 100 },
  { title: '转归', key: 'outcome', width: 90 },
  { title: '操作', key: 'action', width: 260 }
]

const createOpen = ref(false)
const departOpen = ref(false)
const outcomeOpen = ref(false)
const rescueOpen = ref(false)
const currentEvent = ref<MedicalEmergencyEvent | undefined>()
const createForm = reactive<Record<string, any>>({})
const departForm = reactive<Record<string, any>>({})
const outcomeForm = reactive<Record<string, any>>({})
const rescueForm = reactive<Record<string, any>>({})

function statusText(s?: string) { return statusOptions.find((o) => o.value === s)?.label || s || '-' }
function formatEventTime(value?: string) {
  if (!value) return '-'
  const parsed = dayjs(value)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm') : value
}
function statusColor(s?: string) {
  return ({ INITIATED: 'red', CALLED: 'volcano', TRANSFERRED: 'orange', RETURNED: 'green', HOSPITALIZED: 'blue', CLOSED: 'default' } as Record<string, string>)[s || ''] || 'default'
}
function outcomeText(s?: string) {
  return ({ RETURNED: '返回', HOSPITALIZED: '住院', DECEASED: '死亡', CANCELED: '已取消' } as Record<string, string>)[s || ''] || s || '-'
}
function outcomeColor(s?: string) { return s === 'RETURNED' ? 'green' : s === 'HOSPITALIZED' ? 'blue' : s === 'DECEASED' ? 'red' : 'default' }

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<MedicalEmergencyEvent> = await getEmergencyEventPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      elderId: query.elderId,
      status: query.status
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally { loading.value = false }
}
function onSearch() { query.pageNo = 1; pagination.current = 1; fetchData() }
function onReset() { query.elderId = undefined; query.status = undefined; query.pageNo = 1; pagination.current = 1; fetchData() }
function handleTableChange(p: any) { pagination.current = p.current; pagination.pageSize = p.pageSize; query.pageNo = p.current; query.pageSize = p.pageSize; fetchData() }

function openCreate() {
  Object.assign(createForm, { elderId: undefined, location: '', symptom: '', alertId: undefined, remark: '' })
  createOpen.value = true
}
async function submitCreate() {
  if (saving.value) return
  if (!createForm.elderId || !createForm.symptom) { message.error('请填写长者与症状描述'); return }
  saving.value = true
  try {
    await createEmergencyEvent({ ...createForm })
    message.success('急救事件已发起')
    createOpen.value = false
    fetchData()
  } finally { saving.value = false }
}

function doCall(record: MedicalEmergencyEvent) {
  Modal.confirm({
    title: '确认已拨打 120？',
    content: '将登记 120 呼叫时间为当前时间',
    onOk: async () => { await transitionEmergencyEvent(record.id, { action: 'CALL' }); message.success('已登记 120 呼叫'); fetchData() }
  })
}

function openDepart(record: MedicalEmergencyEvent) {
  currentEvent.value = record
  Object.assign(departForm, { hospitalName: '', escortName: '', departTime: undefined })
  departOpen.value = true
}
async function submitDepart() {
  if (!departForm.hospitalName) { message.error('请填写送医医院'); return }
  if (!currentEvent.value) return
  if (saving.value) return
  saving.value = true
  try {
    await transitionEmergencyEvent(currentEvent.value.id, { action: 'DEPART', ...departForm })
    message.success('送医已登记')
    departOpen.value = false
    fetchData()
  } finally { saving.value = false }
}

function openOutcome(record: MedicalEmergencyEvent) {
  currentEvent.value = record
  Object.assign(outcomeForm, { outcome: 'RETURNED', outcomeNote: '' })
  outcomeOpen.value = true
}
async function submitOutcome() {
  if (!currentEvent.value) return
  if (saving.value) return
  saving.value = true
  try {
    await transitionEmergencyEvent(currentEvent.value.id, { action: 'OUTCOME', ...outcomeForm })
    message.success('转归已登记')
    outcomeOpen.value = false
    fetchData()
  } finally { saving.value = false }
}

function doClose(record: MedicalEmergencyEvent) {
  Modal.confirm({
    title: '确认关闭该急救事件？',
    onOk: async () => { await transitionEmergencyEvent(record.id, { action: 'CLOSE' }); message.success('事件已关闭'); fetchData() }
  })
}
function doCancel(record: MedicalEmergencyEvent) {
  Modal.confirm({
    title: '确认取消该急救事件？',
    content: '适用于误报或长者情况已缓解的场景',
    onOk: async () => { await transitionEmergencyEvent(record.id, { action: 'CANCEL' }); message.success('事件已取消'); fetchData() }
  })
}

async function openRescue(record: MedicalEmergencyEvent) {
  currentEvent.value = record
  Object.assign(rescueForm, {
    startTime: undefined, endTime: undefined, timelineJson: '', participants: '', drugsUsed: '', measures: '', result: undefined, resultNote: ''
  })
  try {
    const existing = await getRescueRecordByEvent(record.id)
    if (existing) {
      Object.assign(rescueForm, {
        startTime: existing.startTime,
        endTime: existing.endTime,
        timelineJson: existing.timelineJson,
        participants: existing.participants,
        drugsUsed: existing.drugsUsed,
        measures: existing.measures,
        result: existing.result,
        resultNote: existing.resultNote
      })
    }
  } catch { /* 无既有记录时忽略 */ }
  rescueOpen.value = true
}
async function submitRescue() {
  if (!currentEvent.value) return
  if (saving.value) return
  saving.value = true
  try {
    await saveRescueRecord({ eventId: currentEvent.value.id, ...rescueForm })
    message.success('抢救记录已保存')
    rescueOpen.value = false
  } finally { saving.value = false }
}

fetchData()
searchElders('')
</script>

<style scoped>
.text-muted { color: #999; }
</style>
