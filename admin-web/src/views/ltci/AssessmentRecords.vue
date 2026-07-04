<template>
  <PageContainer title="失能等级评估" subTitle="长护险失能等级评估（国家标准：3 一级 + 17 二级指标，组合法判级 0-5）">
    <SearchForm :model="query" @search="onSearch" @reset="onReset">
      <a-form-item label="长者">
        <a-select
          v-model:value="query.elderId"
          show-search
          allow-clear
          :filter-option="false"
          :options="elderOptions"
          placeholder="输入姓名搜索"
          style="width: 220px"
          @search="searchElders"
          @focus="() => !elderOptions.length && searchElders('')"
        />
      </a-form-item>
      <a-form-item label="失能等级">
        <a-select v-model:value="query.disabilityLevel" allow-clear style="width: 160px" placeholder="全部等级">
          <a-select-option v-for="lv in [0,1,2,3,4,5]" :key="lv" :value="lv">{{ lv }} · {{ levelLabel(lv) }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 160px" placeholder="全部状态">
          <a-select-option v-for="s in statusOptions" :key="s.value" :value="s.value">{{ s.label }}</a-select-option>
        </a-select>
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新建评估</a-button>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'disabilityLevel'">
          <a-tag :color="levelColor(record.disabilityLevel)">
            {{ record.disabilityLevel }} · {{ record.levelLabel || levelLabel(record.disabilityLevel) }}
          </a-tag>
          <a-tag v-if="record.escalated" color="volcano">组合升级</a-tag>
        </template>
        <template v-else-if="column.key === 'scores'">
          ADL {{ fmt(record.adlScore) }} / 认知 {{ fmt(record.cognitiveScore) }} / 感知 {{ fmt(record.perceptionScore) }}
        </template>
        <template v-else-if="column.key === 'assessStatus'">
          <a-tag :color="statusColor(record.assessStatus)">{{ statusText(record.assessStatus) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <div class="row-action-links">
            <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
            <a-button
              v-if="record.assessStatus === 'JUDGED'"
              type="link"
              size="small"
              @click="doNotify(record)"
            >告知生效</a-button>
            <a-button
              v-if="['JUDGED','EFFECTIVE'].includes(record.assessStatus)"
              type="link"
              size="small"
              danger
              @click="openDispute(record)"
            >争议复核</a-button>
          </div>
        </template>
      </template>
    </DataTable>

    <!-- 新建评估：申请 + 打分（一步提交） -->
    <a-modal v-model:open="createOpen" title="新建失能评估" width="760px" :confirm-loading="saving" @ok="submitCreate">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="长者" required>
              <a-select
                v-model:value="form.elderId"
                show-search
                :filter-option="false"
                :options="elderOptions"
                placeholder="输入姓名搜索"
                @search="searchElders"
                @focus="() => !elderOptions.length && searchElders('')"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="评估类型">
              <a-select v-model:value="form.applyType" :options="applyTypeOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="申请人电话">
              <a-input v-model:value="form.applicantPhone" placeholder="选填" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider orientation="left">日常生活活动能力（ADL · Barthel，满分 100）</a-divider>
        <a-row :gutter="16">
          <a-col :span="12" v-for="it in ADL_ITEMS" :key="it.code">
            <a-form-item :label="`${it.name}（0-${it.max}）`">
              <a-input-number v-model:value="answers[it.code]" :min="0" :max="it.max" :step="5" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider orientation="left">认知能力（0 严重 / 1 轻度 / 2 正常）</a-divider>
        <a-row :gutter="16">
          <a-col :span="8" v-for="it in COG_ITEMS" :key="it.code">
            <a-form-item :label="it.name">
              <a-select v-model:value="answers[it.code]" :options="ordinalOptions" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider orientation="left">感知觉与沟通（0 严重 / 1 轻度 / 2 正常）</a-divider>
        <a-row :gutter="16">
          <a-col :span="6" v-for="it in PER_ITEMS" :key="it.code">
            <a-form-item :label="it.name">
              <a-select v-model:value="answers[it.code]" :options="ordinalOptions" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <!-- 详情 -->
    <a-modal v-model:open="detailOpen" title="评估详情" :footer="null" width="640px">
      <a-descriptions v-if="current" bordered :column="2" size="small">
        <a-descriptions-item label="失能等级" :span="2">
          <a-tag :color="levelColor(current.disabilityLevel)">
            {{ current.disabilityLevel }} · {{ current.levelLabel }}
          </a-tag>
          <a-tag v-if="current.escalated" color="volcano">组合升级</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="ADL 得分">{{ fmt(current.adlScore) }}</a-descriptions-item>
        <a-descriptions-item label="认知得分">{{ fmt(current.cognitiveScore) }}</a-descriptions-item>
        <a-descriptions-item label="感知觉得分">{{ fmt(current.perceptionScore) }}</a-descriptions-item>
        <a-descriptions-item label="合计">{{ fmt(current.totalScore) }}</a-descriptions-item>
        <a-descriptions-item label="评估日期">{{ current.assessDate }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ statusText(current.assessStatus) }}</a-descriptions-item>
        <a-descriptions-item label="生效期" :span="2">{{ current.effectiveStart }} ~ {{ current.effectiveEnd }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 争议复核 -->
    <a-modal v-model:open="disputeOpen" title="发起争议复核" :confirm-loading="saving" @ok="submitDispute">
      <a-form layout="vertical">
        <a-form-item label="争议理由" required>
          <a-textarea v-model:value="disputeForm.reason" :rows="3" placeholder="请填写争议/复核理由" />
        </a-form-item>
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
import { useElderOptions } from '../../composables/useElderOptions'
import {
  getLtciAssessmentPage,
  submitLtciApply,
  scoreLtciAssessment,
  notifyLtciAssessment,
  disputeLtciAssessment
} from '../../api/ltci'
import type { Id, LtciAssessment, PageResult } from '../../types'

const ADL_ITEMS = [
  { code: 'adl_feeding', name: '进食', max: 10 },
  { code: 'adl_bathing', name: '洗澡', max: 5 },
  { code: 'adl_grooming', name: '修饰', max: 5 },
  { code: 'adl_dressing', name: '穿衣', max: 10 },
  { code: 'adl_bowel', name: '大便控制', max: 10 },
  { code: 'adl_bladder', name: '小便控制', max: 10 },
  { code: 'adl_toilet', name: '如厕', max: 10 },
  { code: 'adl_transfer', name: '床椅转移', max: 15 },
  { code: 'adl_walking', name: '平地行走', max: 15 },
  { code: 'adl_stairs', name: '上下楼梯', max: 10 }
]
const COG_ITEMS = [
  { code: 'cog_orientation_time', name: '时间与空间定向' },
  { code: 'cog_orientation_person', name: '人物定向' },
  { code: 'cog_memory', name: '记忆(近事)' }
]
const PER_ITEMS = [
  { code: 'per_consciousness', name: '意识水平' },
  { code: 'per_vision', name: '视力' },
  { code: 'per_hearing', name: '听力' },
  { code: 'per_communication', name: '沟通' }
]
const ordinalOptions = [
  { label: '0 · 严重障碍', value: 0 },
  { label: '1 · 轻度障碍', value: 1 },
  { label: '2 · 正常', value: 2 }
]
const applyTypeOptions = [
  { label: '初评', value: 'FIRST' },
  { label: '复评', value: 'REVIEW' }
]
const statusOptions = [
  { label: '已判级', value: 'JUDGED' },
  { label: '已生效', value: 'EFFECTIVE' },
  { label: '争议中', value: 'DISPUTED' },
  { label: '已到期', value: 'EXPIRED' }
]

const loading = ref(false)
const rows = ref<LtciAssessment[]>([])
const query = reactive({
  elderId: undefined as Id | undefined,
  disabilityLevel: undefined as number | undefined,
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const { elderOptions, searchElders } = useElderOptions({ pageSize: 50 })

const columns = [
  { title: '长者ID', dataIndex: 'elderId', key: 'elderId', width: 100 },
  { title: '失能等级', key: 'disabilityLevel', width: 180 },
  { title: '三维得分', key: 'scores' },
  { title: '评估日期', dataIndex: 'assessDate', key: 'assessDate', width: 120 },
  { title: '生效期', dataIndex: 'effectiveEnd', key: 'effectiveEnd', width: 120 },
  { title: '状态', key: 'assessStatus', width: 100 },
  { title: '操作', key: 'action', width: 200 }
]

const createOpen = ref(false)
const detailOpen = ref(false)
const disputeOpen = ref(false)
const saving = ref(false)
const current = ref<LtciAssessment | null>(null)
const form = reactive({
  elderId: undefined as Id | undefined,
  applyType: 'FIRST',
  applicantPhone: ''
})
const answers = reactive<Record<string, number>>({})
const disputeForm = reactive({ assessmentId: undefined as Id | undefined, reason: '' })

function fmt(v?: number) {
  return v == null ? '-' : Number(v).toFixed(2)
}
function levelLabel(lv?: number) {
  if (lv == null) return '-'
  return ['能力完好', '轻度失能', '中度失能', '中度失能', '重度失能', '重度失能'][lv] || '-'
}
function levelColor(lv?: number) {
  if (lv == null) return 'default'
  return ['green', 'blue', 'gold', 'orange', 'red', 'red'][lv] || 'default'
}
function statusText(s?: string) {
  return ({
    APPLIED: '已申请', ACCEPTED: '已受理', ASSIGNED: '已分配', SCORING: '打分中',
    JUDGED: '已判级', NOTIFIED: '已告知', DISPUTED: '争议中', REVIEWING: '复核中',
    EFFECTIVE: '已生效', EXPIRED: '已到期', CANCELLED: '已取消'
  } as Record<string, string>)[s || ''] || s || '-'
}
function statusColor(s?: string) {
  if (s === 'EFFECTIVE') return 'green'
  if (s === 'JUDGED') return 'blue'
  if (s === 'DISPUTED') return 'red'
  if (s === 'EXPIRED') return 'default'
  return 'orange'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<LtciAssessment> = await getLtciAssessmentPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      elderId: query.elderId,
      status: query.status,
      disabilityLevel: query.disabilityLevel
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
  query.elderId = undefined
  query.disabilityLevel = undefined
  query.status = undefined
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

function openCreate() {
  form.elderId = undefined
  form.applyType = 'FIRST'
  form.applicantPhone = ''
  ADL_ITEMS.forEach((it) => (answers[it.code] = 0))
  COG_ITEMS.forEach((it) => (answers[it.code] = 2))
  PER_ITEMS.forEach((it) => (answers[it.code] = 2))
  createOpen.value = true
}

async function submitCreate() {
  if (!form.elderId) {
    message.error('请选择长者')
    return
  }
  saving.value = true
  try {
    const applyId = await submitLtciApply({
      elderId: form.elderId,
      applyType: form.applyType,
      applicantPhone: form.applicantPhone
    })
    const result = await scoreLtciAssessment({
      applyId: applyId as Id,
      elderId: form.elderId,
      answers: { ...answers }
    })
    message.success(`判级完成：${result.disabilityLevel} · ${result.levelLabel}`)
    createOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

function openDetail(record: LtciAssessment) {
  current.value = record
  detailOpen.value = true
}

async function doNotify(record: LtciAssessment) {
  await notifyLtciAssessment(record.id)
  message.success('已告知并生效')
  fetchData()
}

function openDispute(record: LtciAssessment) {
  disputeForm.assessmentId = record.id
  disputeForm.reason = ''
  disputeOpen.value = true
}
async function submitDispute() {
  if (!disputeForm.reason) {
    message.error('请填写争议理由')
    return
  }
  saving.value = true
  try {
    await disputeLtciAssessment({ assessmentId: disputeForm.assessmentId as Id, reason: disputeForm.reason })
    message.success('已发起争议复核')
    disputeOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

fetchData()
searchElders('')
</script>
