<template>
  <PageContainer title="慢病随访管理" subTitle="随访计划（病种/频次/目标指标）与随访记录，下次随访日期自动排程，到期自动生成护理待办">
    <SearchForm :model="query" @search="onSearch" @reset="onReset">
      <a-form-item label="长者">
        <a-select v-model:value="query.elderId" show-search allow-clear :filter-option="false" :options="elderOptions"
          placeholder="输入姓名搜索" style="width: 200px" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
      </a-form-item>
      <a-form-item label="病种">
        <a-select v-model:value="query.diseaseType" allow-clear style="width: 140px" placeholder="全部">
          <a-select-option v-for="d in diseaseOptions" :key="d.value" :value="d.value">{{ d.label }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 120px" placeholder="全部">
          <a-select-option value="ACTIVE">进行中</a-select-option>
          <a-select-option value="PAUSED">已暂停</a-select-option>
          <a-select-option value="CLOSED">已结案</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="仅看到期">
        <a-switch v-model:checked="query.dueOnly" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button type="primary" @click="openCreatePlan">新建随访计划</a-button>
          <a-button :loading="generating" @click="generateReminders">生成到期待办</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="planColumns" :data-source="plans" :loading="planLoading" :pagination="planPagination" @change="handlePlanTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'diseaseType'">
          <a-tag color="blue">{{ diseaseText(record.diseaseType) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'nextFollowupDate'">
          <span :class="{ 'due-date': isDue(record) }">{{ record.nextFollowupDate || '-' }}</span>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="planStatusColor(record.status)">{{ planStatusText(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button v-if="record.status === 'ACTIVE'" type="link" size="small" @click="openCreateRecord(record)">记录随访</a-button>
            <a-button type="link" size="small" @click="viewPlanRecords(record)">历史</a-button>
            <a-button v-if="record.status === 'ACTIVE'" type="link" size="small" @click="openEditPlan(record)">编辑</a-button>
            <a-button v-if="record.status === 'ACTIVE'" type="link" size="small" danger @click="setPlanStatus(record, 'CLOSED')">结案</a-button>
            <a-button v-if="record.status === 'PAUSED'" type="link" size="small" @click="setPlanStatus(record, 'ACTIVE')">恢复</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-divider>随访记录<span v-if="recordQuery.planId" class="text-muted">（当前计划）</span></a-divider>
    <a-space style="margin-bottom: 12px">
      <a-button v-if="recordQuery.planId" size="small" @click="clearPlanFilter">查看全部记录</a-button>
    </a-space>
    <DataTable rowKey="id" :columns="recordColumns" :data-source="records" :loading="recordLoading" :pagination="recordPagination" @change="handleRecordTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'medicationCompliance'">
          <a-tag :color="complianceColor(record.medicationCompliance)">{{ complianceText(record.medicationCompliance) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'assessmentLevel'">
          <a-tag :color="levelColor(record.assessmentLevel)">{{ levelText(record.assessmentLevel) }}</a-tag>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="planOpen" :title="editingPlanId ? '编辑随访计划' : '新建随访计划'" :confirm-loading="saving" width="680px" @ok="submitPlan">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="长者" required>
              <a-select v-model:value="planForm.elderId" show-search :filter-option="false" :options="elderOptions" :disabled="!!editingPlanId"
                placeholder="输入姓名搜索" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="病种" required>
              <a-select v-model:value="planForm.diseaseType">
                <a-select-option v-for="d in diseaseOptions" :key="d.value" :value="d.value">{{ d.label }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8"><a-form-item label="计划名称"><a-input v-model:value="planForm.planName" placeholder="如 高血压二级随访" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="随访频次（天）" required><a-input-number v-model:value="planForm.frequencyDays" :min="1" style="width:100%" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="下次随访日期"><a-date-picker v-model:value="planForm.nextFollowupDate" value-format="YYYY-MM-DD" style="width:100%" placeholder="默认按频次推算" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="目标指标"><a-textarea v-model:value="planForm.targetIndicators" :rows="2" placeholder="如 血压<140/90mmHg；空腹血糖<7.0mmol/L" /></a-form-item>
        <a-form-item label="备注"><a-input v-model:value="planForm.remark" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="recordOpen" title="记录随访" :confirm-loading="saving" width="680px" @ok="submitRecord">
      <a-alert v-if="currentPlan" type="info" show-icon style="margin-bottom: 12px"
        :message="`${currentPlan.elderName || ''} · ${diseaseText(currentPlan.diseaseType)} · 目标：${currentPlan.targetIndicators || '未设置'}`" />
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="随访日期"><a-date-picker v-model:value="recordForm.followupDate" value-format="YYYY-MM-DD" style="width:100%" placeholder="默认今天" /></a-form-item></a-col>
          <a-col :span="12">
            <a-form-item label="用药依从性">
              <a-select v-model:value="recordForm.medicationCompliance">
                <a-select-option value="GOOD">良好</a-select-option>
                <a-select-option value="PARTIAL">部分依从</a-select-option>
                <a-select-option value="POOR">差</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="本次体征值（JSON）"><a-input v-model:value="recordForm.vitalJson" placeholder='如 {"sbp":135,"dbp":85,"fbg":6.2}' /></a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="评估结论级别">
              <a-select v-model:value="recordForm.assessmentLevel">
                <a-select-option value="CONTROLLED">达标</a-select-option>
                <a-select-option value="UNSTABLE">未达标</a-select-option>
                <a-select-option value="WORSENED">恶化</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12"><a-form-item label="下次随访日期"><a-date-picker v-model:value="recordForm.nextFollowupDate" value-format="YYYY-MM-DD" style="width:100%" placeholder="留空按频次自动排程" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="评估结论"><a-textarea v-model:value="recordForm.assessment" :rows="2" /></a-form-item>
        <a-form-item label="备注"><a-input v-model:value="recordForm.remark" /></a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import {
  getFollowupPlanPage,
  createFollowupPlan,
  updateFollowupPlan,
  updateFollowupPlanStatus,
  getFollowupRecordPage,
  createFollowupRecord,
  generateFollowupReminders,
  type MedicalFollowupPlan,
  type MedicalFollowupRecord
} from '../../api/medicalFollowup'
import type { Id, PageResult } from '../../types'

const diseaseOptions = [
  { label: '高血压', value: 'HYPERTENSION' },
  { label: '糖尿病', value: 'DIABETES' },
  { label: '慢阻肺', value: 'COPD' },
  { label: '冠心病', value: 'CHD' },
  { label: '脑卒中', value: 'STROKE' },
  { label: '其他慢病', value: 'OTHER' }
]

const planLoading = ref(false)
const recordLoading = ref(false)
const saving = ref(false)
const generating = ref(false)
const plans = ref<MedicalFollowupPlan[]>([])
const records = ref<MedicalFollowupRecord[]>([])
const query = reactive({
  elderId: undefined as Id | undefined,
  diseaseType: undefined as string | undefined,
  status: undefined as string | undefined,
  dueOnly: false,
  pageNo: 1,
  pageSize: 10
})
const recordQuery = reactive({ planId: undefined as Id | undefined, pageNo: 1, pageSize: 10 })
const planPagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const recordPagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const { elderOptions, searchElders } = useElderOptions({ pageSize: 50 })

const planColumns = [
  { title: '长者', dataIndex: 'elderName', key: 'elderName', width: 100 },
  { title: '病种', key: 'diseaseType', width: 100 },
  { title: '计划名称', dataIndex: 'planName', key: 'planName', ellipsis: true },
  { title: '频次(天)', dataIndex: 'frequencyDays', key: 'frequencyDays', width: 90 },
  { title: '目标指标', dataIndex: 'targetIndicators', key: 'targetIndicators', ellipsis: true },
  { title: '上次随访', dataIndex: 'lastFollowupDate', key: 'lastFollowupDate', width: 110 },
  { title: '下次随访', key: 'nextFollowupDate', width: 110 },
  { title: '状态', key: 'status', width: 90 },
  { title: '操作', key: 'action', width: 230 }
]
const recordColumns = [
  { title: '长者', dataIndex: 'elderName', key: 'elderName', width: 100 },
  { title: '随访日期', dataIndex: 'followupDate', key: 'followupDate', width: 110 },
  { title: '体征值', dataIndex: 'vitalJson', key: 'vitalJson', ellipsis: true },
  { title: '依从性', key: 'medicationCompliance', width: 100 },
  { title: '评估', key: 'assessmentLevel', width: 90 },
  { title: '评估结论', dataIndex: 'assessment', key: 'assessment', ellipsis: true },
  { title: '下次随访', dataIndex: 'nextFollowupDate', key: 'nextFollowupDate', width: 110 },
  { title: '随访人', dataIndex: 'doctorName', key: 'doctorName', width: 100 }
]

const planOpen = ref(false)
const recordOpen = ref(false)
const editingPlanId = ref<Id | undefined>()
const currentPlan = ref<MedicalFollowupPlan | undefined>()
const planForm = reactive<Partial<MedicalFollowupPlan>>({})
const recordForm = reactive<Record<string, any>>({})

function diseaseText(d?: string) { return diseaseOptions.find((o) => o.value === d)?.label || d || '-' }
function planStatusText(s?: string) { return ({ ACTIVE: '进行中', PAUSED: '已暂停', CLOSED: '已结案' } as Record<string, string>)[s || ''] || s || '-' }
function planStatusColor(s?: string) { return s === 'ACTIVE' ? 'green' : s === 'PAUSED' ? 'orange' : 'default' }
function complianceText(s?: string) { return ({ GOOD: '良好', PARTIAL: '部分依从', POOR: '差' } as Record<string, string>)[s || ''] || s || '-' }
function complianceColor(s?: string) { return s === 'GOOD' ? 'green' : s === 'PARTIAL' ? 'orange' : s === 'POOR' ? 'red' : 'default' }
function levelText(s?: string) { return ({ CONTROLLED: '达标', UNSTABLE: '未达标', WORSENED: '恶化' } as Record<string, string>)[s || ''] || s || '-' }
function levelColor(s?: string) { return s === 'CONTROLLED' ? 'green' : s === 'UNSTABLE' ? 'orange' : s === 'WORSENED' ? 'red' : 'default' }
function isDue(plan: MedicalFollowupPlan) {
  return plan.status === 'ACTIVE' && !!plan.nextFollowupDate && plan.nextFollowupDate <= new Date().toISOString().slice(0, 10)
}

async function fetchPlans() {
  planLoading.value = true
  try {
    const res: PageResult<MedicalFollowupPlan> = await getFollowupPlanPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      elderId: query.elderId,
      diseaseType: query.diseaseType,
      status: query.status,
      dueOnly: query.dueOnly || undefined
    })
    plans.value = res.list
    planPagination.total = res.total || res.list.length
  } finally { planLoading.value = false }
}

async function fetchRecords() {
  recordLoading.value = true
  try {
    const res: PageResult<MedicalFollowupRecord> = await getFollowupRecordPage({
      pageNo: recordQuery.pageNo,
      pageSize: recordQuery.pageSize,
      planId: recordQuery.planId
    })
    records.value = res.list
    recordPagination.total = res.total || res.list.length
  } finally { recordLoading.value = false }
}

function onSearch() { query.pageNo = 1; planPagination.current = 1; fetchPlans() }
function onReset() {
  query.elderId = undefined; query.diseaseType = undefined; query.status = undefined; query.dueOnly = false
  query.pageNo = 1; planPagination.current = 1; fetchPlans()
}
function handlePlanTableChange(p: any) { planPagination.current = p.current; planPagination.pageSize = p.pageSize; query.pageNo = p.current; query.pageSize = p.pageSize; fetchPlans() }
function handleRecordTableChange(p: any) { recordPagination.current = p.current; recordPagination.pageSize = p.pageSize; recordQuery.pageNo = p.current; recordQuery.pageSize = p.pageSize; fetchRecords() }

function viewPlanRecords(plan: MedicalFollowupPlan) { recordQuery.planId = plan.id; recordQuery.pageNo = 1; recordPagination.current = 1; fetchRecords() }
function clearPlanFilter() { recordQuery.planId = undefined; recordQuery.pageNo = 1; recordPagination.current = 1; fetchRecords() }

function openCreatePlan() {
  editingPlanId.value = undefined
  Object.assign(planForm, { elderId: undefined, diseaseType: 'HYPERTENSION', planName: '', frequencyDays: 30, targetIndicators: '', nextFollowupDate: undefined, remark: '' })
  planOpen.value = true
}
function openEditPlan(plan: MedicalFollowupPlan) {
  editingPlanId.value = plan.id
  Object.assign(planForm, {
    elderId: plan.elderId,
    diseaseType: plan.diseaseType,
    planName: plan.planName,
    frequencyDays: plan.frequencyDays,
    targetIndicators: plan.targetIndicators,
    nextFollowupDate: plan.nextFollowupDate,
    remark: plan.remark
  })
  planOpen.value = true
}
async function submitPlan() {
  if (!planForm.elderId || !planForm.frequencyDays) { message.error('请填写长者与随访频次'); return }
  if (saving.value) return
  saving.value = true
  try {
    if (editingPlanId.value) {
      await updateFollowupPlan(editingPlanId.value, { ...planForm })
      message.success('随访计划已更新')
    } else {
      await createFollowupPlan({ ...planForm })
      message.success('随访计划已创建')
    }
    planOpen.value = false
    fetchPlans()
  } finally { saving.value = false }
}

function setPlanStatus(plan: MedicalFollowupPlan, status: string) {
  Modal.confirm({
    title: status === 'CLOSED' ? '确认结案该随访计划？' : '确认恢复该随访计划？',
    onOk: async () => { await updateFollowupPlanStatus(plan.id, status); message.success('已更新'); fetchPlans() }
  })
}

function openCreateRecord(plan: MedicalFollowupPlan) {
  currentPlan.value = plan
  Object.assign(recordForm, {
    planId: plan.id,
    followupDate: undefined,
    vitalJson: '',
    medicationCompliance: 'GOOD',
    assessmentLevel: 'CONTROLLED',
    assessment: '',
    nextFollowupDate: undefined,
    remark: ''
  })
  recordOpen.value = true
}
async function submitRecord() {
  if (saving.value) return
  saving.value = true
  try {
    await createFollowupRecord({ ...recordForm })
    message.success('随访已记录，下次随访日期已排程')
    recordOpen.value = false
    fetchRecords()
    fetchPlans()
  } finally { saving.value = false }
}

async function generateReminders() {
  generating.value = true
  try {
    const count = await generateFollowupReminders()
    message.success(`已生成 ${count ?? 0} 条到期随访待办`)
  } finally { generating.value = false }
}

fetchPlans()
fetchRecords()
searchElders('')
</script>

<style scoped>
.text-muted { color: #999; }
.due-date { color: #cf1322; font-weight: 600; }
</style>
