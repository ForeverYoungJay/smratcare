<template>
  <PageContainer title="中医体质评估" subTitle="评估中心 > 中医体质评估">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="长者"><a-input v-model:value="query.keyword" placeholder="姓名/建议要点/评估人" allow-clear /></a-form-item>
      <a-form-item label="体质类型">
        <a-select v-model:value="query.constitutionType" allow-clear style="width: 180px" :options="constitutionOptions" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 120px">
          <a-select-option value="DRAFT">草稿</a-select-option>
          <a-select-option value="PUBLISHED">已发布</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="日期"><a-range-picker v-model:value="query.dateRange" /></a-form-item>
      <template #extra>
        <a-space>
          <a-button type="primary" @click="openCreate">新建评估</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="onTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'constitution'">
          {{ constitutionLabel(record.constitutionPrimary) }} / {{ constitutionLabel(record.constitutionSecondary) }}
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="record.status === 'PUBLISHED' ? 'green' : 'blue'">{{ record.status === 'PUBLISHED' ? '已发布' : '草稿' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" @click="publish(record)" :disabled="record.status === 'PUBLISHED'">发布</a-button>
            <a-popconfirm title="确认删除该评估?" @confirm="remove(record)">
              <a-button danger type="link">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑中医体质评估' : '新建中医体质评估'" width="900px" @ok="submit" :confirm-loading="saving">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="长者" required>
              <a-select
                v-model:value="form.elderId"
                show-search
                :filter-option="false"
                :options="elderOptions"
                placeholder="请输入姓名搜索"
                @search="searchElders"
                @focus="() => !elderOptions.length && searchElders('')"
                @change="onElderChange"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8"><a-form-item label="评估日期" required><a-date-picker v-model:value="form.assessmentDate" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="评估人"><a-input v-model:value="form.assessorName" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8"><a-form-item label="评估场景"><a-select v-model:value="form.assessmentScene" :options="sceneOptions" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="主导体质"><a-select v-model:value="form.constitutionPrimary" :options="constitutionOptions" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="次体质"><a-select v-model:value="form.constitutionSecondary" :options="constitutionOptions" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8"><a-form-item label="综合分值"><a-input-number v-model:value="form.score" :min="0" :max="100" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="可信度"><a-input-number v-model:value="form.confidence" :min="0" :max="100" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="8">
            <a-form-item label="自动计算">
              <a-button block @click="autoCompute">根据问卷JSON计算主/次体质</a-button>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="问卷条目(JSON)"><a-textarea v-model:value="form.questionnaireJson" :rows="3" placeholder='示例: {"QI_DEFICIENCY":68,"YANG_DEFICIENCY":52}' /></a-form-item>
        <a-form-item label="体质特征摘要"><a-textarea v-model:value="form.featureSummary" :rows="2" /></a-form-item>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="调养建议-饮食"><a-textarea v-model:value="form.suggestionDiet" :rows="2" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="调养建议-起居"><a-textarea v-model:value="form.suggestionRoutine" :rows="2" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="调养建议-运动"><a-textarea v-model:value="form.suggestionExercise" :rows="2" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="调养建议-情志"><a-textarea v-model:value="form.suggestionEmotion" :rows="2" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="护理提示"><a-textarea v-model:value="form.nursingTip" :rows="2" /></a-form-item>
        <a-form-item label="建议要点"><a-input v-model:value="form.suggestionPoints" /></a-form-item>
        <a-space>
          <a-checkbox v-model:checked="form.isReassessment">是否复评</a-checkbox>
          <a-checkbox v-model:checked="form.familyVisible">是否家属可见</a-checkbox>
          <a-checkbox v-model:checked="form.generateNursingTask">生成护理提示</a-checkbox>
        </a-space>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import {
  createTcmAssessment,
  deleteTcmAssessment,
  getTcmAssessmentPage,
  publishTcmAssessment,
  updateTcmAssessment
} from '../../api/medicalCare'
import type { MedicalTcmAssessment, PageResult } from '../../types'

const route = useRoute()
const loading = ref(false)
const rows = ref<MedicalTcmAssessment[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const query = reactive({ keyword: '', constitutionType: '', status: '', dateRange: [] as any[], pageNo: 1, pageSize: 10 })

const columns = [
  { title: '长者', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '体质结论', key: 'constitution', width: 220 },
  { title: '建议要点', dataIndex: 'suggestionPoints', key: 'suggestionPoints' },
  { title: '评估时间', dataIndex: 'assessmentDate', key: 'assessmentDate', width: 120 },
  { title: '评估人', dataIndex: 'assessorName', key: 'assessorName', width: 120 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 180 }
]

const constitutionOptions = [
  { label: '平和质', value: 'BALANCED' },
  { label: '气虚质', value: 'QI_DEFICIENCY' },
  { label: '阳虚质', value: 'YANG_DEFICIENCY' },
  { label: '阴虚质', value: 'YIN_DEFICIENCY' },
  { label: '痰湿质', value: 'PHLEGM_DAMPNESS' },
  { label: '湿热质', value: 'DAMP_HEAT' },
  { label: '血瘀质', value: 'BLOOD_STASIS' },
  { label: '气郁质', value: 'QI_STAGNATION' },
  { label: '特禀质', value: 'SPECIAL' }
]
const sceneOptions = [
  { label: '入院', value: 'ADMISSION' },
  { label: '复评', value: 'REASSESSMENT' },
  { label: '专项', value: 'SPECIAL' }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive<any>({})
const { elderOptions, searchElders, findElderName, ensureSelectedElder } = useElderOptions({ pageSize: 50 })

function constitutionLabel(value?: string) {
  return constitutionOptions.find((i) => i.value === value)?.label || value || '-'
}

function resetForm() {
  form.id = undefined
  form.elderId = undefined
  form.elderName = ''
  form.assessmentDate = dayjs()
  form.assessorName = ''
  form.assessmentScene = 'ADMISSION'
  form.constitutionPrimary = undefined
  form.constitutionSecondary = undefined
  form.score = undefined
  form.confidence = undefined
  form.questionnaireJson = ''
  form.featureSummary = ''
  form.suggestionDiet = ''
  form.suggestionRoutine = ''
  form.suggestionExercise = ''
  form.suggestionEmotion = ''
  form.nursingTip = ''
  form.suggestionPoints = ''
  form.isReassessment = false
  form.familyVisible = false
  form.generateNursingTask = false
}

function openCreate() {
  resetForm()
  if (route.query.residentId) {
    form.elderId = Number(route.query.residentId)
    ensureSelectedElder(form.elderId, route.query.residentName ? String(route.query.residentName) : undefined)
  }
  if (route.query.residentName) {
    form.elderName = String(route.query.residentName)
  }
  editOpen.value = true
}

function openEdit(record: MedicalTcmAssessment) {
  resetForm()
  Object.assign(form, {
    ...record,
    assessmentDate: record.assessmentDate ? dayjs(record.assessmentDate) : dayjs(),
    isReassessment: record.isReassessment === 1,
    familyVisible: record.familyVisible === 1,
    generateNursingTask: record.generateNursingTask === 1
  })
  ensureSelectedElder(record.elderId, record.elderName)
  editOpen.value = true
}

function onElderChange(elderId?: number) {
  form.elderName = findElderName(elderId)
}

function autoCompute() {
  try {
    const parsed = JSON.parse(form.questionnaireJson || '{}')
    const list = Object.entries(parsed)
      .map(([key, value]) => ({ key, score: Number(value) || 0 }))
      .sort((a, b) => b.score - a.score)
    if (!list.length) {
      message.warning('问卷JSON没有可计算分值')
      return
    }
    form.constitutionPrimary = list[0].key
    form.constitutionSecondary = list[1]?.key
    form.score = list[0].score
    form.confidence = Math.min(100, Math.max(0, list[0].score - (list[1]?.score || 0) + 60))
    message.success('已自动计算主次体质')
  } catch {
    message.error('问卷JSON格式不正确')
  }
}

function buildParams() {
  const params: any = {
    pageNo: query.pageNo,
    pageSize: query.pageSize,
    keyword: query.keyword || undefined,
    constitutionType: query.constitutionType || undefined,
    status: query.status || undefined
  }
  if (query.dateRange?.length === 2) {
    params.dateFrom = dayjs(query.dateRange[0]).format('YYYY-MM-DD')
    params.dateTo = dayjs(query.dateRange[1]).format('YYYY-MM-DD')
  }
  if (route.query.residentId) {
    params.elderId = Number(route.query.residentId)
  }
  return params
}

async function fetchData() {
  loading.value = true
  try {
    const res = (await getTcmAssessmentPage(buildParams())) as PageResult<MedicalTcmAssessment>
    rows.value = res.list || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

function onTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.keyword = ''
  query.constitutionType = ''
  query.status = ''
  query.dateRange = []
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

async function submit() {
  if (!form.elderId || !form.assessmentDate) {
    message.error('请填写长者和评估日期')
    return
  }
  saving.value = true
  try {
    const payload = {
      elderId: form.elderId,
      elderName: findElderName(form.elderId) || form.elderName,
      assessmentDate: dayjs(form.assessmentDate).format('YYYY-MM-DD'),
      assessorName: form.assessorName,
      assessmentScene: form.assessmentScene,
      constitutionPrimary: form.constitutionPrimary,
      constitutionSecondary: form.constitutionSecondary,
      score: form.score,
      confidence: form.confidence,
      questionnaireJson: form.questionnaireJson,
      featureSummary: form.featureSummary,
      suggestionDiet: form.suggestionDiet,
      suggestionRoutine: form.suggestionRoutine,
      suggestionExercise: form.suggestionExercise,
      suggestionEmotion: form.suggestionEmotion,
      nursingTip: form.nursingTip,
      suggestionPoints: form.suggestionPoints,
      isReassessment: form.isReassessment ? 1 : 0,
      familyVisible: form.familyVisible ? 1 : 0,
      generateNursingTask: form.generateNursingTask ? 1 : 0,
      status: 'DRAFT'
    }
    if (form.id) {
      await updateTcmAssessment(form.id, payload)
    } else {
      await createTcmAssessment(payload)
    }
    message.success('保存成功')
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function publish(record: MedicalTcmAssessment) {
  await publishTcmAssessment(record.id)
  message.success('发布成功，已可回写 Resident360 风险卡')
  fetchData()
}

async function remove(record: MedicalTcmAssessment) {
  await deleteTcmAssessment(record.id)
  message.success('删除成功')
  fetchData()
}

onMounted(() => {
  resetForm()
  searchElders('')
  fetchData()
})
</script>
