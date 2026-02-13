<template>
  <PageContainer title="问卷模板" subTitle="模板配置与发布">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" placeholder="模板名称/编码" allow-clear />
        </a-form-item>
        <a-form-item label="对象">
          <a-select v-model:value="query.targetType" allow-clear style="width: 160px">
            <a-select-option value="ELDER">老人</a-select-option>
            <a-select-option value="STAFF">员工</a-select-option>
            <a-select-option value="FAMILY">家属</a-select-option>
            <a-select-option value="OTHER">其他</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 140px">
            <a-select-option :value="0">草稿</a-select-option>
            <a-select-option :value="1">发布</a-select-option>
            <a-select-option :value="2">停用</a-select-option>
          </a-select>
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
          <a-button type="primary" @click="openForm()">新增模板</a-button>
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
          <template v-if="column.key === 'targetType'">
            {{ targetLabel(record.targetType) }}
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="statusTag(record.status)">{{ statusLabel(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'scoreEnabled'">
            <a-tag :color="record.scoreEnabled === 1 ? 'green' : 'default'">
              {{ record.scoreEnabled === 1 ? '是' : '否' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="openForm(record)">编辑</a-button>
              <a-button type="link" @click="openQuestions(record)">配置题目</a-button>
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

    <a-modal v-model:open="open" title="模板" width="560px" :confirm-loading="submitting" @ok="submit" @cancel="() => (open = false)">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="模板编码" name="templateCode">
          <a-input v-model:value="form.templateCode" placeholder="如 T_001" />
        </a-form-item>
        <a-form-item label="模板名称" name="templateName">
          <a-input v-model:value="form.templateName" />
        </a-form-item>
        <a-form-item label="适用对象" name="targetType">
          <a-select v-model:value="form.targetType">
            <a-select-option value="ELDER">老人</a-select-option>
            <a-select-option value="STAFF">员工</a-select-option>
            <a-select-option value="FAMILY">家属</a-select-option>
            <a-select-option value="OTHER">其他</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status">
            <a-select-option :value="0">草稿</a-select-option>
            <a-select-option :value="1">发布</a-select-option>
            <a-select-option :value="2">停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="开始日期">
          <a-date-picker v-model:value="form.startDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="结束日期">
          <a-date-picker v-model:value="form.endDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="匿名">
          <a-switch v-model:checked="form.anonymousFlag" :checked-value="1" :un-checked-value="0" />
        </a-form-item>
        <a-form-item label="计分">
          <a-switch v-model:checked="form.scoreEnabled" :checked-value="1" :un-checked-value="0" />
        </a-form-item>
        <a-form-item label="总分">
          <a-input-number v-model:value="form.totalScore" :min="0" :max="1000" style="width: 100%" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="form.description" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="questionOpen" title="配置题目" width="820px" :confirm-loading="questionSaving" @ok="saveQuestions" @cancel="() => (questionOpen = false)">
      <div class="question-toolbar">
        <a-space>
          <a-input v-model:value="questionKeyword" placeholder="搜索题目标题/编码" allow-clear />
          <a-button @click="filterQuestions">搜索</a-button>
        </a-space>
        <div class="hint">已选 {{ selectedIds.length }} 题</div>
      </div>
      <a-table
        :data-source="questionList"
        :columns="questionColumns"
        :pagination="false"
        row-key="id"
        :row-selection="rowSelection"
        :scroll="{ y: 520 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'questionType'">
            {{ typeLabel(record.questionType) }}
          </template>
          <template v-else-if="column.key === 'requiredFlag'">
            <a-switch
              v-model:checked="questionConfig[record.id].requiredFlag"
              :checked-value="1"
              :un-checked-value="0"
              :disabled="!selectedSet.has(record.id)"
            />
          </template>
          <template v-else-if="column.key === 'weight'">
            <a-input-number
              v-model:value="questionConfig[record.id].weight"
              :min="0"
              :max="10"
              :step="0.1"
              :disabled="!selectedSet.has(record.id)"
            />
          </template>
        </template>
      </a-table>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import {
  getSurveyTemplatePage,
  createSurveyTemplate,
  updateSurveyTemplate,
  deleteSurveyTemplate,
  getSurveyQuestionPage,
  getSurveyTemplateDetail,
  updateSurveyTemplateQuestions
} from '../../api/survey'
import type { SurveyTemplate, SurveyQuestion, SurveyTemplateQuestionItem, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<SurveyTemplate[]>([])
const total = ref(0)

const query = reactive({
  keyword: '',
  targetType: undefined as string | undefined,
  status: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10
})

const open = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<SurveyTemplate>>({
  templateCode: '',
  templateName: '',
  targetType: 'ELDER',
  status: 0,
  anonymousFlag: 0,
  scoreEnabled: 0,
  totalScore: undefined
})
const submitting = ref(false)

const questionOpen = ref(false)
const questionList = ref<SurveyQuestion[]>([])
const questionSaving = ref(false)
const questionKeyword = ref('')
const selectedIds = ref<number[]>([])
const selectedSet = ref(new Set<number>())
const questionConfig = reactive<Record<number, { requiredFlag: number; weight?: number | null }>>({})
let activeTemplateId: number | null = null

const rules: FormRules = {
  templateCode: [{ required: true, message: '请输入模板编码' }],
  templateName: [{ required: true, message: '请输入模板名称' }],
  targetType: [{ required: true, message: '请选择对象' }]
}

const columns = [
  { title: '模板编码', dataIndex: 'templateCode', key: 'templateCode', width: 140 },
  { title: '模板名称', dataIndex: 'templateName', key: 'templateName' },
  { title: '对象', dataIndex: 'targetType', key: 'targetType', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '开始日期', dataIndex: 'startDate', key: 'startDate', width: 140 },
  { title: '结束日期', dataIndex: 'endDate', key: 'endDate', width: 140 },
  { title: '计分', dataIndex: 'scoreEnabled', key: 'scoreEnabled', width: 100 },
  { title: '操作', key: 'action', width: 240 }
]

const questionColumns = [
  { title: '编码', dataIndex: 'questionCode', key: 'questionCode', width: 120 },
  { title: '题目', dataIndex: 'title', key: 'title' },
  { title: '题型', dataIndex: 'questionType', key: 'questionType', width: 120 },
  { title: '必答', key: 'requiredFlag', width: 120 },
  { title: '权重', key: 'weight', width: 120 }
]

const rowSelection = computed(() => ({
  selectedRowKeys: selectedIds.value,
  onChange: (keys: (string | number)[]) => {
    selectedIds.value = keys.map((k) => Number(k))
    selectedSet.value = new Set(selectedIds.value)
  }
}))

function statusLabel(status?: number) {
  if (status === 0) return '草稿'
  if (status === 1) return '发布'
  if (status === 2) return '停用'
  return '-'
}

function statusTag(status?: number) {
  if (status === 1) return 'green'
  if (status === 2) return 'default'
  return 'orange'
}

function targetLabel(type?: string) {
  if (type === 'ELDER') return '老人'
  if (type === 'STAFF') return '员工'
  if (type === 'FAMILY') return '家属'
  if (type === 'OTHER') return '其他'
  return '-'
}

function typeLabel(type?: string) {
  if (type === 'SINGLE') return '单选'
  if (type === 'MULTI') return '多选'
  if (type === 'TEXT') return '文本'
  if (type === 'RATING') return '评分'
  if (type === 'SCORE') return '打分'
  return '-'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<SurveyTemplate> = await getSurveyTemplatePage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      status: query.status,
      targetType: query.targetType
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reset() {
  query.keyword = ''
  query.status = undefined
  query.targetType = undefined
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

function openForm(row?: SurveyTemplate) {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: undefined, templateCode: '', templateName: '', targetType: 'ELDER', status: 0, startDate: '', endDate: '', anonymousFlag: 0, scoreEnabled: 0, totalScore: undefined, description: '' })
  }
  open.value = true
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    if (form.id) {
      await updateSurveyTemplate(form.id, form)
    } else {
      await createSurveyTemplate(form)
    }
    message.success('保存成功')
    open.value = false
    await fetchData()
  } catch {
    message.error('保存失败')
  } finally {
    submitting.value = false
  }
}

async function remove(id: number) {
  Modal.confirm({
    title: '提示',
    content: '确认删除该模板吗？',
    async onOk() {
      await deleteSurveyTemplate(id)
      message.success('已删除')
      await fetchData()
    }
  })
}

async function openQuestions(row: SurveyTemplate) {
  activeTemplateId = row.id
  questionOpen.value = true
  questionKeyword.value = ''
  await loadQuestions()
}

async function loadQuestions() {
  const res: PageResult<SurveyQuestion> = await getSurveyQuestionPage({ pageNo: 1, pageSize: 200, keyword: questionKeyword.value })
  questionList.value = res.list
  questionList.value.forEach((q) => {
    if (!questionConfig[q.id]) {
      questionConfig[q.id] = { requiredFlag: q.requiredFlag || 0, weight: undefined }
    }
  })
  if (activeTemplateId) {
    const detail = await getSurveyTemplateDetail(activeTemplateId)
    const selected = detail.questions.map((q) => q.questionId)
    selectedIds.value = selected
    selectedSet.value = new Set(selected)
    detail.questions.forEach((q) => {
      questionConfig[q.questionId] = { requiredFlag: q.requiredFlag || 0, weight: q.weight ?? undefined }
    })
  }
}

function filterQuestions() {
  loadQuestions()
}

async function saveQuestions() {
  if (!activeTemplateId) return
  questionSaving.value = true
  try {
    const items: SurveyTemplateQuestionItem[] = selectedIds.value.map((id, index) => ({
      questionId: id,
      sortNo: index + 1,
      requiredFlag: questionConfig[id]?.requiredFlag ?? 0,
      weight: questionConfig[id]?.weight ?? undefined
    }))
    await updateSurveyTemplateQuestions(activeTemplateId, items)
    message.success('题目配置已保存')
    questionOpen.value = false
  } catch {
    message.error('保存失败')
  } finally {
    questionSaving.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
.table-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
.question-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.hint {
  color: rgba(0, 0, 0, 0.6);
}
</style>
