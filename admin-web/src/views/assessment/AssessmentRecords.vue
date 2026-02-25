<template>
  <PageContainer :title="title" :subTitle="subTitle">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" placeholder="老人姓名/评估人/档案号" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 140px">
            <a-select-option value="DRAFT">草稿</a-select-option>
            <a-select-option value="COMPLETED">已完成</a-select-option>
            <a-select-option value="ARCHIVED">已归档</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="评估日期">
          <a-range-picker v-model:value="query.dateRange" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-space>
          <a-button type="primary" @click="openForm()">评估登记</a-button>
        </a-space>
      </div>
      <a-table
        :data-source="rows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :scroll="{ y: 520 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'score'">
            {{ record.score ?? '-' }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="openForm(record)">编辑</a-button>
              <a-button type="link" danger @click="remove(record.id)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>

    <a-modal
      v-model:open="open"
      :title="title + '登记'"
      width="680px"
      :confirm-loading="submitting"
      @ok="submit"
      @cancel="() => (open = false)"
    >
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="老人姓名" name="elderName">
              <a-input v-model:value="form.elderName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="评估等级">
              <a-input v-model:value="form.levelCode" placeholder="如 A/B/C 或 轻/中/重" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="评估日期" name="assessmentDate">
              <a-date-picker v-model:value="form.assessmentDate" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="下次评估日期">
              <a-date-picker v-model:value="form.nextAssessmentDate" value-format="YYYY-MM-DD" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="16">
            <a-form-item label="量表模板">
              <a-select v-model:value="form.templateId" allow-clear placeholder="可选，选择后可自动算分">
                <a-select-option v-for="tpl in templates" :key="tpl.id" :value="tpl.id">
                  {{ tpl.templateName }} ({{ tpl.templateCode }})
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="自动评分">
              <a-switch v-model:checked="scoreAutoChecked" @change="onScoreAutoChange" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="评估分值">
              <a-input-number v-model:value="form.score" :min="0" :max="999" :step="0.5" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="评估状态" name="status">
              <a-select v-model:value="form.status">
                <a-select-option value="DRAFT">草稿</a-select-option>
                <a-select-option value="COMPLETED">已完成</a-select-option>
                <a-select-option value="ARCHIVED">已归档</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="评估人">
              <a-input v-model:value="form.assessorName" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="档案编号">
              <a-input v-model:value="form.archiveNo" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="评估结论">
          <a-textarea v-model:value="form.resultSummary" :rows="3" placeholder="简要结论" />
        </a-form-item>
        <a-form-item label="护理建议">
          <a-textarea v-model:value="form.suggestion" :rows="3" placeholder="后续护理建议" />
        </a-form-item>
        <a-form-item label="量表明细(JSON)">
          <a-textarea v-model:value="form.detailJson" :rows="4" placeholder='如 {"orientation":6,"registration":5}' />
          <div style="margin-top: 8px;">
            <a-space>
              <a-button size="small" @click="previewScore">试算分值</a-button>
              <span class="hint">选择模板并填写明细后，可自动计算分值和等级。</span>
            </a-space>
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import {
  getAssessmentRecordPage,
  createAssessmentRecord,
  updateAssessmentRecord,
  deleteAssessmentRecord,
  getAssessmentTemplateList,
  previewAssessmentScore
} from '../../api/assessment'
import type { AssessmentRecord, AssessmentType, AssessmentScaleTemplate, PageResult } from '../../types'

const props = defineProps<{
  title: string
  subTitle: string
  assessmentType: AssessmentType
}>()

const loading = ref(false)
const rows = ref<AssessmentRecord[]>([])
const total = ref(0)
const templates = ref<AssessmentScaleTemplate[]>([])

const query = reactive({
  keyword: '',
  status: undefined as string | undefined,
  dateRange: undefined as string[] | undefined,
  pageNo: 1,
  pageSize: 10
})

const open = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<AssessmentRecord>>({
  assessmentType: props.assessmentType,
  templateId: undefined,
  elderName: '',
  levelCode: '',
  score: undefined,
  assessmentDate: '',
  nextAssessmentDate: undefined,
  status: 'COMPLETED',
  assessorName: '',
  archiveNo: '',
  resultSummary: '',
  suggestion: '',
  detailJson: '',
  scoreAuto: 1
})
const submitting = ref(false)
const scoreAutoChecked = ref(true)

const rules: FormRules = {
  elderName: [{ required: true, message: '请输入老人姓名' }],
  assessmentDate: [{ required: true, message: '请选择评估日期' }],
  status: [{ required: true, message: '请选择状态' }]
}

const columns = [
  { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '评估日期', dataIndex: 'assessmentDate', key: 'assessmentDate', width: 120 },
  { title: '等级', dataIndex: 'levelCode', key: 'levelCode', width: 100 },
  { title: '分值', dataIndex: 'score', key: 'score', width: 90 },
  { title: '评估人', dataIndex: 'assessorName', key: 'assessorName', width: 110 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '档案编号', dataIndex: 'archiveNo', key: 'archiveNo', width: 140 },
  { title: '评估结论', dataIndex: 'resultSummary', key: 'resultSummary' },
  { title: '操作', key: 'action', width: 140 }
]

function statusLabel(status?: string) {
  if (status === 'DRAFT') return '草稿'
  if (status === 'ARCHIVED') return '已归档'
  return '已完成'
}

function statusColor(status?: string) {
  if (status === 'DRAFT') return 'orange'
  if (status === 'ARCHIVED') return 'blue'
  return 'green'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<AssessmentRecord> = await getAssessmentRecordPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      assessmentType: props.assessmentType,
      status: query.status,
      keyword: query.keyword,
      dateFrom: query.dateRange?.[0],
      dateTo: query.dateRange?.[1]
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function fetchTemplates() {
  templates.value = await getAssessmentTemplateList({ assessmentType: props.assessmentType })
}

function reset() {
  query.keyword = ''
  query.status = undefined
  query.dateRange = undefined
  query.pageNo = 1
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

function openForm(record?: AssessmentRecord) {
  if (record) {
    Object.assign(form, record)
    scoreAutoChecked.value = record.scoreAuto !== 0
  } else {
    Object.assign(form, {
      id: undefined,
      assessmentType: props.assessmentType,
      templateId: undefined,
      elderName: '',
      levelCode: '',
      score: undefined,
      assessmentDate: '',
      nextAssessmentDate: undefined,
      status: 'COMPLETED',
      assessorName: '',
      archiveNo: '',
      resultSummary: '',
      suggestion: '',
      detailJson: '',
      scoreAuto: 1
    })
    scoreAutoChecked.value = true
  }
  open.value = true
}

function onScoreAutoChange(checked: boolean) {
  form.scoreAuto = checked ? 1 : 0
}

async function previewScore() {
  if (!form.templateId) {
    message.warning('请先选择量表模板')
    return
  }
  if (!form.detailJson) {
    message.warning('请先填写量表明细JSON')
    return
  }
  const result = await previewAssessmentScore({
    templateId: form.templateId,
    detailJson: form.detailJson
  })
  form.score = result.score
  if (result.levelCode) {
    form.levelCode = result.levelCode
  }
  message.success('试算完成')
}

async function submit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload = {
      assessmentType: props.assessmentType,
      templateId: form.templateId,
      elderName: form.elderName,
      levelCode: form.levelCode,
      score: form.score,
      assessmentDate: form.assessmentDate,
      nextAssessmentDate: form.nextAssessmentDate,
      status: form.status,
      assessorName: form.assessorName,
      archiveNo: form.archiveNo,
      resultSummary: form.resultSummary,
      suggestion: form.suggestion,
      detailJson: form.detailJson,
      scoreAuto: form.scoreAuto,
      source: 'MANUAL'
    }
    if (form.id) {
      await updateAssessmentRecord(form.id, payload)
      message.success('更新成功')
    } else {
      await createAssessmentRecord(payload)
      message.success('登记成功')
    }
    open.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

function remove(id: number) {
  Modal.confirm({
    title: '确认删除该评估记录？',
    onOk: async () => {
      await deleteAssessmentRecord(id)
      message.success('删除成功')
      fetchData()
    }
  })
}

onMounted(async () => {
  await Promise.all([fetchTemplates(), fetchData()])
})
</script>
