<template>
  <PageContainer title="巡诊工作台" subTitle="医生巡诊排班与逐长者巡诊记录（可联动生成病程记录与医嘱）">
    <SearchForm :model="query" @search="onSearch" @reset="onReset">
      <a-form-item label="日期">
        <a-range-picker v-model:value="query.dateRange" value-format="YYYY-MM-DD" style="width: 240px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 130px" placeholder="全部">
          <a-select-option v-for="s in planStatusOptions" :key="s.value" :value="s.value">{{ s.label }}</a-select-option>
        </a-select>
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button type="primary" @click="openCreatePlan">新建巡诊排班</a-button>
          <a-button @click="openCreateRecord()">登记巡诊记录</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="planColumns" :data-source="plans" :loading="planLoading" :pagination="planPagination" @change="handlePlanTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'elderScope'">{{ scopeText(record.elderScope) }}</template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="planStatusColor(record.status)">{{ planStatusText(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="viewPlanRecords(record)">巡诊记录</a-button>
            <a-button v-if="record.status === 'PLANNED' || record.status === 'IN_PROGRESS'" type="link" size="small" @click="openCreateRecord(record)">登记</a-button>
            <a-button v-if="record.status === 'IN_PROGRESS' || record.status === 'PLANNED'" type="link" size="small" @click="setPlanStatus(record, 'DONE')">完成</a-button>
            <a-button v-if="record.status === 'PLANNED'" type="link" size="small" danger @click="setPlanStatus(record, 'CANCELED')">取消</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-divider>巡诊记录<span v-if="recordQuery.planId" class="text-muted">（当前排班）</span></a-divider>
    <a-space style="margin-bottom: 12px">
      <a-select v-model:value="recordQuery.resultLevel" allow-clear style="width: 150px" placeholder="巡诊结果（全部）" @change="fetchRecords">
        <a-select-option v-for="l in resultLevelOptions" :key="l.value" :value="l.value">{{ l.label }}</a-select-option>
      </a-select>
      <a-button v-if="recordQuery.planId" size="small" @click="clearPlanFilter">查看全部记录</a-button>
    </a-space>
    <DataTable rowKey="id" :columns="recordColumns" :data-source="records" :loading="recordLoading" :pagination="recordPagination" @change="handleRecordTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'resultLevel'">
          <a-tag :color="resultLevelColor(record.resultLevel)">{{ resultLevelText(record.resultLevel) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'linked'">
          <a-space>
            <a-tag v-if="record.emrRecordId" color="blue">病程</a-tag>
            <a-tag v-if="record.medicalOrderId" color="purple">医嘱</a-tag>
            <span v-if="!record.emrRecordId && !record.medicalOrderId" class="text-muted">—</span>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="planOpen" title="新建巡诊排班" :confirm-loading="saving" width="640px" @ok="submitPlan">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="8"><a-form-item label="巡诊日期" required><a-date-picker v-model:value="planForm.planDate" value-format="YYYY-MM-DD" style="width:100%" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="时段"><a-input v-model:value="planForm.timeSlot" placeholder="如 09:00-11:00" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="医生姓名"><a-input v-model:value="planForm.doctorName" placeholder="默认当前登录人" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="楼层/区域"><a-input v-model:value="planForm.area" placeholder="如 2号楼3层" /></a-form-item></a-col>
          <a-col :span="12">
            <a-form-item label="长者范围">
              <a-select v-model:value="planForm.elderScope">
                <a-select-option value="ALL">全院</a-select-option>
                <a-select-option value="AREA">按区域</a-select-option>
                <a-select-option value="CUSTOM">指定长者</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item v-if="planForm.elderScope === 'CUSTOM'" label="长者ID列表（JSON数组）">
          <a-input v-model:value="planForm.elderIdsJson" placeholder="如 [1001,1002]" />
        </a-form-item>
        <a-form-item label="备注"><a-textarea v-model:value="planForm.remark" :rows="2" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="recordOpen" title="登记巡诊记录" :confirm-loading="saving" width="720px" @ok="submitRecord">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="长者" required>
              <a-select v-model:value="recordForm.elderId" show-search :filter-option="false" :options="elderOptions"
                placeholder="输入姓名搜索" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
            </a-form-item>
          </a-col>
          <a-col :span="12"><a-form-item label="巡诊时间"><a-date-picker v-model:value="recordForm.roundTime" show-time value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" placeholder="默认当前时间" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="查体所见" required><a-textarea v-model:value="recordForm.findings" :rows="2" placeholder="如 血压 150/95mmHg，双下肢轻度水肿" /></a-form-item>
        <a-form-item label="处理意见"><a-textarea v-model:value="recordForm.handleOpinion" :rows="2" placeholder="如 建议调整降压方案，加强观察" /></a-form-item>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="巡诊结果">
              <a-select v-model:value="recordForm.resultLevel">
                <a-select-option v-for="l in resultLevelOptions" :key="l.value" :value="l.value">{{ l.label }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="生成病程记录">
              <a-switch v-model:checked="generateEmr" checked-children="是" un-checked-children="否" />
            </a-form-item>
          </a-col>
          <a-col :span="8"><a-form-item v-if="generateEmr" label="诊断"><a-input v-model:value="recordForm.diagnosis" placeholder="写入病程记录" /></a-form-item></a-col>
        </a-row>
        <a-divider orientation="left" plain>医嘱建议（填写内容后同时开立医嘱）</a-divider>
        <a-form-item label="医嘱内容"><a-input v-model:value="recordForm.orderContent" placeholder="如 硝苯地平控释片 30mg 口服（留空则不开立）" /></a-form-item>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="医嘱类型">
              <a-select v-model:value="recordForm.orderType">
                <a-select-option value="TEMPORARY">临时</a-select-option>
                <a-select-option value="LONG_TERM">长期</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8"><a-form-item label="剂量"><a-input v-model:value="recordForm.orderDosage" placeholder="如 30mg" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="频次"><a-select v-model:value="recordForm.orderFrequency" allow-clear :options="freqOptions" placeholder="长期医嘱建议填写" /></a-form-item></a-col>
        </a-row>
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
  getRoundsPlanPage,
  createRoundsPlan,
  updateRoundsPlanStatus,
  getRoundsRecordPage,
  createRoundsRecord,
  type MedicalRoundsPlan,
  type MedicalRoundsRecord
} from '../../api/medicalRounds'
import type { Id, PageResult } from '../../types'

const planStatusOptions = [
  { label: '已排班', value: 'PLANNED' },
  { label: '巡诊中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'DONE' },
  { label: '已取消', value: 'CANCELED' }
]
const resultLevelOptions = [
  { label: '正常', value: 'NORMAL' },
  { label: '需关注', value: 'ATTENTION' },
  { label: '需紧急处理', value: 'URGENT' }
]
const freqOptions = ['QD', 'BID', 'TID', 'QID', 'Q8H', 'Q12H', 'QOD', 'PRN', 'ST'].map((v) => ({ label: v, value: v }))

const planLoading = ref(false)
const recordLoading = ref(false)
const saving = ref(false)
const plans = ref<MedicalRoundsPlan[]>([])
const records = ref<MedicalRoundsRecord[]>([])
const query = reactive({ dateRange: undefined as [string, string] | undefined, status: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const recordQuery = reactive({ planId: undefined as Id | undefined, resultLevel: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const planPagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const recordPagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const { elderOptions, searchElders } = useElderOptions({ pageSize: 50 })

const planColumns = [
  { title: '巡诊日期', dataIndex: 'planDate', key: 'planDate', width: 110 },
  { title: '时段', dataIndex: 'timeSlot', key: 'timeSlot', width: 110 },
  { title: '医生', dataIndex: 'doctorName', key: 'doctorName', width: 100 },
  { title: '楼层/区域', dataIndex: 'area', key: 'area', width: 130 },
  { title: '长者范围', key: 'elderScope', width: 100 },
  { title: '状态', key: 'status', width: 90 },
  { title: '备注', dataIndex: 'remark', key: 'remark', ellipsis: true },
  { title: '操作', key: 'action', width: 220 }
]
const recordColumns = [
  { title: '长者', dataIndex: 'elderName', key: 'elderName', width: 100 },
  { title: '巡诊时间', dataIndex: 'roundTime', key: 'roundTime', width: 160 },
  { title: '查体所见', dataIndex: 'findings', key: 'findings', ellipsis: true },
  { title: '处理意见', dataIndex: 'handleOpinion', key: 'handleOpinion', ellipsis: true },
  { title: '结果', key: 'resultLevel', width: 110 },
  { title: '联动', key: 'linked', width: 110 },
  { title: '医生', dataIndex: 'doctorName', key: 'doctorName', width: 100 }
]

const planOpen = ref(false)
const recordOpen = ref(false)
const generateEmr = ref(false)
const planForm = reactive<Partial<MedicalRoundsPlan>>({})
const recordForm = reactive<Record<string, any>>({})

function scopeText(s?: string) { return ({ ALL: '全院', AREA: '按区域', CUSTOM: '指定长者' } as Record<string, string>)[s || ''] || s || '-' }
function planStatusText(s?: string) { return planStatusOptions.find((o) => o.value === s)?.label || s || '-' }
function planStatusColor(s?: string) { return s === 'PLANNED' ? 'blue' : s === 'IN_PROGRESS' ? 'gold' : s === 'DONE' ? 'green' : 'default' }
function resultLevelText(s?: string) { return resultLevelOptions.find((o) => o.value === s)?.label || s || '-' }
function resultLevelColor(s?: string) { return s === 'URGENT' ? 'red' : s === 'ATTENTION' ? 'orange' : 'green' }

async function fetchPlans() {
  planLoading.value = true
  try {
    const res: PageResult<MedicalRoundsPlan> = await getRoundsPlanPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status,
      dateFrom: query.dateRange?.[0],
      dateTo: query.dateRange?.[1]
    })
    plans.value = res.list
    planPagination.total = res.total || res.list.length
  } finally { planLoading.value = false }
}

async function fetchRecords() {
  recordLoading.value = true
  try {
    const res: PageResult<MedicalRoundsRecord> = await getRoundsRecordPage({
      pageNo: recordQuery.pageNo,
      pageSize: recordQuery.pageSize,
      planId: recordQuery.planId,
      resultLevel: recordQuery.resultLevel
    })
    records.value = res.list
    recordPagination.total = res.total || res.list.length
  } finally { recordLoading.value = false }
}

function onSearch() { query.pageNo = 1; planPagination.current = 1; fetchPlans() }
function onReset() { query.dateRange = undefined; query.status = undefined; query.pageNo = 1; planPagination.current = 1; fetchPlans() }
function handlePlanTableChange(p: any) { planPagination.current = p.current; planPagination.pageSize = p.pageSize; query.pageNo = p.current; query.pageSize = p.pageSize; fetchPlans() }
function handleRecordTableChange(p: any) { recordPagination.current = p.current; recordPagination.pageSize = p.pageSize; recordQuery.pageNo = p.current; recordQuery.pageSize = p.pageSize; fetchRecords() }

function viewPlanRecords(plan: MedicalRoundsPlan) { recordQuery.planId = plan.id; recordQuery.pageNo = 1; recordPagination.current = 1; fetchRecords() }
function clearPlanFilter() { recordQuery.planId = undefined; recordQuery.pageNo = 1; recordPagination.current = 1; fetchRecords() }

function openCreatePlan() {
  Object.assign(planForm, { planDate: undefined, timeSlot: '', doctorName: '', area: '', elderScope: 'AREA', elderIdsJson: '', remark: '' })
  planOpen.value = true
}
async function submitPlan() {
  if (!planForm.planDate) { message.error('请选择巡诊日期'); return }
  if (saving.value) return
  saving.value = true
  try {
    await createRoundsPlan({ ...planForm })
    message.success('巡诊排班已创建')
    planOpen.value = false
    fetchPlans()
  } finally { saving.value = false }
}

function setPlanStatus(plan: MedicalRoundsPlan, status: string) {
  Modal.confirm({
    title: status === 'DONE' ? '确认完成本次巡诊？' : '确认取消该巡诊排班？',
    onOk: async () => { await updateRoundsPlanStatus(plan.id, status); message.success('已更新'); fetchPlans() }
  })
}

function openCreateRecord(plan?: MedicalRoundsPlan) {
  generateEmr.value = false
  Object.assign(recordForm, {
    planId: plan?.id,
    elderId: undefined,
    roundTime: undefined,
    findings: '',
    handleOpinion: '',
    resultLevel: 'NORMAL',
    diagnosis: '',
    orderContent: '',
    orderType: 'TEMPORARY',
    orderDosage: '',
    orderFrequency: undefined,
    remark: ''
  })
  recordOpen.value = true
}
async function submitRecord() {
  if (!recordForm.elderId || !recordForm.findings) { message.error('请填写长者与查体所见'); return }
  if (saving.value) return
  saving.value = true
  try {
    await createRoundsRecord({ ...recordForm, generateEmr: generateEmr.value ? 1 : 0 })
    message.success('巡诊记录已登记')
    recordOpen.value = false
    fetchRecords()
    fetchPlans()
  } finally { saving.value = false }
}

fetchPlans()
fetchRecords()
searchElders('')
</script>

<style scoped>
.text-muted { color: #999; }
</style>
