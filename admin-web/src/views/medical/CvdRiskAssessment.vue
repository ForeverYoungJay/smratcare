<template>
  <PageContainer title="心血管风险评估" subTitle="评估中心 > 缺血性心血管病风险评估">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="长者"><a-input v-model:value="query.keyword" placeholder="姓名/风险因子/评估人" allow-clear /></a-form-item>
      <a-form-item label="风险等级"><a-select v-model:value="query.riskLevel" allow-clear style="width: 140px" :options="riskOptions" /></a-form-item>
      <a-form-item label="需要随访">
        <a-select v-model:value="query.needFollowup" allow-clear style="width: 120px">
          <a-select-option :value="1">是</a-select-option>
          <a-select-option :value="0">否</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="日期"><a-range-picker v-model:value="query.dateRange" /></a-form-item>
      <template #extra>
        <a-space>
          <a-button type="primary" @click="openCreate">新建风险评估</a-button>
        </a-space>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="onTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'riskLevel'">
          <a-tag :color="riskColor(record.riskLevel)">{{ riskLabel(record.riskLevel) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="record.status === 'PUBLISHED' ? 'green' : 'blue'">{{ record.status === 'PUBLISHED' ? '已发布' : '草稿' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" @click="openPublish(record)" :disabled="record.status === 'PUBLISHED'">发布</a-button>
            <a-popconfirm title="确认删除该记录?" @confirm="remove(record)">
              <a-button danger type="link">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑心血管风险评估' : '新建心血管风险评估'" width="900px" @ok="submit" :confirm-loading="saving">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="8"><a-form-item label="长者姓名" required><a-input v-model:value="form.elderName" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="评估日期" required><a-date-picker v-model:value="form.assessmentDate" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="评估人"><a-input v-model:value="form.assessorName" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8"><a-form-item label="风险等级" required><a-select v-model:value="form.riskLevel" :options="riskOptions" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="随访天数"><a-input-number v-model:value="form.followupDays" :min="1" :max="365" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="需要随访"><a-switch v-model:checked="form.needFollowup" /></a-form-item></a-col>
        </a-row>
        <a-form-item label="关键风险因子"><a-input v-model:value="form.keyRiskFactors" placeholder="如 高血压、糖尿病、吸烟" /></a-form-item>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="生活方式项(JSON)"><a-textarea v-model:value="form.lifestyleJson" :rows="3" placeholder='{"smoking":true,"exercise":"low"}' /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="风险因子项(JSON)"><a-textarea v-model:value="form.factorJson" :rows="3" placeholder='{"bp":"150/95","bmi":27}' /></a-form-item></a-col>
        </a-row>
        <a-form-item label="风险结论"><a-textarea v-model:value="form.conclusion" :rows="2" /></a-form-item>
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="医嘱建议"><a-textarea v-model:value="form.medicalAdvice" :rows="2" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="护理干预建议"><a-textarea v-model:value="form.nursingAdvice" :rows="2" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal v-model:open="publishOpen" title="发布并联动任务" @ok="confirmPublish" :confirm-loading="publishing">
      <a-space direction="vertical">
        <a-checkbox v-model:checked="publishActions.generateInspectionPlan">生成健康巡检计划</a-checkbox>
        <a-checkbox v-model:checked="publishActions.generateFollowupTask">生成医护随访任务</a-checkbox>
        <a-checkbox v-model:checked="publishActions.suggestMedicalOrder">建议开立医嘱</a-checkbox>
      </a-space>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { createCvdAssessment, deleteCvdAssessment, getCvdAssessmentPage, publishCvdAssessment, updateCvdAssessment } from '../../api/medicalCare'
import type { MedicalCvdAssessment, PageResult } from '../../types'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const rows = ref<MedicalCvdAssessment[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const query = reactive({ keyword: '', riskLevel: '', needFollowup: undefined as number | undefined, dateRange: [] as any[], pageNo: 1, pageSize: 10 })

const columns = [
  { title: '长者', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '风险等级', key: 'riskLevel', width: 120 },
  { title: '关键风险因子', dataIndex: 'keyRiskFactors', key: 'keyRiskFactors' },
  { title: '建议', dataIndex: 'medicalAdvice', key: 'medicalAdvice' },
  { title: '评估人', dataIndex: 'assessorName', key: 'assessorName', width: 120 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 180 }
]

const riskOptions = [
  { label: '低风险', value: 'LOW' },
  { label: '中风险', value: 'MEDIUM' },
  { label: '高风险', value: 'HIGH' },
  { label: '极高风险', value: 'VERY_HIGH' }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive<any>({})

const publishOpen = ref(false)
const publishing = ref(false)
const publishTargetId = ref<number>()
const publishActions = reactive({
  generateInspectionPlan: true,
  generateFollowupTask: true,
  suggestMedicalOrder: false
})

function riskLabel(value?: string) {
  return riskOptions.find((i) => i.value === value)?.label || value || '-'
}

function riskColor(value?: string) {
  if (value === 'VERY_HIGH') return 'red'
  if (value === 'HIGH') return 'volcano'
  if (value === 'MEDIUM') return 'orange'
  if (value === 'LOW') return 'green'
  return 'blue'
}

function resetForm() {
  form.id = undefined
  form.elderName = ''
  form.assessmentDate = dayjs()
  form.assessorName = ''
  form.riskLevel = 'MEDIUM'
  form.followupDays = 30
  form.needFollowup = true
  form.keyRiskFactors = ''
  form.lifestyleJson = ''
  form.factorJson = ''
  form.conclusion = ''
  form.medicalAdvice = ''
  form.nursingAdvice = ''
}

function openCreate() {
  resetForm()
  if (route.query.residentName) {
    form.elderName = String(route.query.residentName)
  }
  editOpen.value = true
}

function openEdit(record: MedicalCvdAssessment) {
  resetForm()
  Object.assign(form, {
    ...record,
    assessmentDate: record.assessmentDate ? dayjs(record.assessmentDate) : dayjs(),
    needFollowup: record.needFollowup === 1
  })
  editOpen.value = true
}

function buildParams() {
  const params: any = {
    pageNo: query.pageNo,
    pageSize: query.pageSize,
    keyword: query.keyword || undefined,
    riskLevel: query.riskLevel || undefined,
    needFollowup: query.needFollowup
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
    const res = (await getCvdAssessmentPage(buildParams())) as PageResult<MedicalCvdAssessment>
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
  query.riskLevel = ''
  query.needFollowup = undefined
  query.dateRange = []
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

async function submit() {
  if (!form.elderName || !form.assessmentDate || !form.riskLevel) {
    message.error('请填写必填项')
    return
  }
  saving.value = true
  try {
    const payload = {
      elderId: route.query.residentId ? Number(route.query.residentId) : undefined,
      elderName: form.elderName,
      assessmentDate: dayjs(form.assessmentDate).format('YYYY-MM-DD'),
      assessorName: form.assessorName,
      riskLevel: form.riskLevel,
      keyRiskFactors: form.keyRiskFactors,
      lifestyleJson: form.lifestyleJson,
      factorJson: form.factorJson,
      conclusion: form.conclusion,
      medicalAdvice: form.medicalAdvice,
      nursingAdvice: form.nursingAdvice,
      followupDays: form.followupDays,
      needFollowup: form.needFollowup ? 1 : 0,
      status: 'DRAFT'
    }
    if (form.id) {
      await updateCvdAssessment(form.id, payload)
    } else {
      await createCvdAssessment(payload)
    }
    message.success('保存成功')
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

function openPublish(record: MedicalCvdAssessment) {
  publishTargetId.value = record.id
  publishActions.generateInspectionPlan = record.riskLevel === 'HIGH' || record.riskLevel === 'VERY_HIGH'
  publishActions.generateFollowupTask = record.riskLevel === 'HIGH' || record.riskLevel === 'VERY_HIGH'
  publishActions.suggestMedicalOrder = false
  publishOpen.value = true
}

async function confirmPublish() {
  if (!publishTargetId.value) return
  publishing.value = true
  try {
    const result = await publishCvdAssessment(publishTargetId.value, {
      generateInspectionPlan: publishActions.generateInspectionPlan ? 1 : 0,
      generateFollowupTask: publishActions.generateFollowupTask ? 1 : 0,
      suggestMedicalOrder: publishActions.suggestMedicalOrder ? 1 : 0
    })
    message.success('发布成功')
    publishOpen.value = false
    fetchData()
    if (publishActions.generateInspectionPlan && result.inspectionRoute) {
      router.push(result.inspectionRoute)
      return
    }
    if (publishActions.generateFollowupTask && result.careTaskRoute) {
      router.push(result.careTaskRoute)
      return
    }
    if (publishActions.suggestMedicalOrder && result.medicalOrderRoute) {
      router.push(result.medicalOrderRoute)
    }
  } finally {
    publishing.value = false
  }
}

async function remove(record: MedicalCvdAssessment) {
  await deleteCvdAssessment(record.id)
  message.success('删除成功')
  fetchData()
}

onMounted(() => {
  resetForm()
  fetchData()
})
</script>
