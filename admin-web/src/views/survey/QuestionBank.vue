<template>
  <PageContainer title="问卷题库" subTitle="题库维护与题型管理">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" placeholder="题目标题/编码" allow-clear />
        </a-form-item>
        <a-form-item label="题型">
          <a-select v-model:value="query.questionType" allow-clear style="width: 160px">
            <a-select-option value="SINGLE">单选</a-select-option>
            <a-select-option value="MULTI">多选</a-select-option>
            <a-select-option value="TEXT">文本</a-select-option>
            <a-select-option value="RATING">评分</a-select-option>
            <a-select-option value="SCORE">打分</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 140px">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
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
          <a-button type="primary" @click="openForm()">新增题目</a-button>
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
          <template v-if="column.key === 'questionType'">
            {{ typeLabel(record.questionType) }}
          </template>
          <template v-else-if="column.key === 'requiredFlag'">
            <a-tag :color="record.requiredFlag === 1 ? 'red' : 'default'">
              {{ record.requiredFlag === 1 ? '必答' : '可选' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'scoreEnabled'">
            <a-tag :color="record.scoreEnabled === 1 ? 'green' : 'default'">
              {{ record.scoreEnabled === 1 ? '是' : '否' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'default'">
              {{ record.status === 1 ? '启用' : '停用' }}
            </a-tag>
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

    <a-modal v-model:open="open" title="题目" width="520px" :confirm-loading="submitting" @ok="submit" @cancel="() => (open = false)">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="题目编码" name="questionCode">
          <a-input v-model:value="form.questionCode" placeholder="如 Q_001" />
        </a-form-item>
        <a-form-item label="题目标题" name="title">
          <a-input v-model:value="form.title" />
        </a-form-item>
        <a-form-item label="题型" name="questionType">
          <a-select v-model:value="form.questionType" placeholder="请选择题型">
            <a-select-option value="SINGLE">单选</a-select-option>
            <a-select-option value="MULTI">多选</a-select-option>
            <a-select-option value="TEXT">文本</a-select-option>
            <a-select-option value="RATING">评分</a-select-option>
            <a-select-option value="SCORE">打分</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="选项JSON" name="optionsJson">
          <a-textarea v-model:value="form.optionsJson" :rows="3" placeholder='如 ["非常满意","满意","一般"]' />
        </a-form-item>
        <a-form-item label="必答">
          <a-switch v-model:checked="form.requiredFlag" :checked-value="1" :un-checked-value="0" />
        </a-form-item>
        <a-form-item label="计分">
          <a-switch v-model:checked="form.scoreEnabled" :checked-value="1" :un-checked-value="0" />
        </a-form-item>
        <a-form-item label="最高分" name="maxScore">
          <a-input-number v-model:value="form.maxScore" :min="0" :max="100" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" style="width: 100%">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="form.sortNo" :min="0" :max="9999" style="width: 100%" />
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
import { getSurveyQuestionPage, createSurveyQuestion, updateSurveyQuestion, deleteSurveyQuestion } from '../../api/survey'
import type { SurveyQuestion, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<SurveyQuestion[]>([])
const total = ref(0)

const query = reactive({
  keyword: '',
  questionType: undefined as string | undefined,
  status: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10
})

const open = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<SurveyQuestion>>({
  questionCode: '',
  title: '',
  questionType: 'SINGLE',
  requiredFlag: 0,
  scoreEnabled: 0,
  status: 1,
  sortNo: 0
})
const submitting = ref(false)

const rules: FormRules = {
  questionCode: [{ required: true, message: '请输入题目编码' }],
  title: [{ required: true, message: '请输入题目标题' }],
  questionType: [{ required: true, message: '请选择题型' }]
}

const columns = [
  { title: '题目编码', dataIndex: 'questionCode', key: 'questionCode', width: 140 },
  { title: '题目标题', dataIndex: 'title', key: 'title' },
  { title: '题型', dataIndex: 'questionType', key: 'questionType', width: 120 },
  { title: '必答', dataIndex: 'requiredFlag', key: 'requiredFlag', width: 100 },
  { title: '计分', dataIndex: 'scoreEnabled', key: 'scoreEnabled', width: 100 },
  { title: '最高分', dataIndex: 'maxScore', key: 'maxScore', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 200 }
]

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
    const res: PageResult<SurveyQuestion> = await getSurveyQuestionPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      questionType: query.questionType,
      status: query.status
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reset() {
  query.keyword = ''
  query.questionType = undefined
  query.status = undefined
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

function openForm(row?: SurveyQuestion) {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: undefined, questionCode: '', title: '', questionType: 'SINGLE', optionsJson: '', requiredFlag: 0, scoreEnabled: 0, maxScore: undefined, status: 1, sortNo: 0 })
  }
  open.value = true
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    if (form.id) {
      await updateSurveyQuestion(form.id, form)
    } else {
      await createSurveyQuestion(form)
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
    content: '确认删除该题目吗？',
    async onOk() {
      await deleteSurveyQuestion(id)
      message.success('已删除')
      await fetchData()
    }
  })
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
</style>
