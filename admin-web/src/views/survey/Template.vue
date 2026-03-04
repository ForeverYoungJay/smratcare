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
          <a-button :disabled="!selectedSingleRecord" @click="editSelected">编辑</a-button>
          <a-button :disabled="!selectedSingleRecord" @click="configQuestionsSelected">配置题目</a-button>
          <a-button :disabled="!selectedSingleRecord" @click="openQrSelected">二维码</a-button>
          <a-button :disabled="!canPublishSingle" @click="publishSelected">发布</a-button>
          <a-button :disabled="!canDisableSingle" @click="disableSelected">停用</a-button>
          <a-button :disabled="!selectedSingleRecord" danger @click="removeSelected">删除</a-button>
          <a-button :disabled="selectedRowKeys.length === 0" @click="batchPublish">批量发布</a-button>
          <a-button :disabled="selectedRowKeys.length === 0" @click="batchDisable">批量停用</a-button>
          <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchRemove">批量删除</a-button>
          <span class="selection-tip">已勾选 {{ selectedRowKeys.length }} 条</span>
        </a-space>
      </div>
      <a-table
        :data-source="rows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :row-selection="tableRowSelection"
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
        :row-selection="questionRowSelection"
        :scroll="{ y: 520 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'questionType'">
            {{ typeLabel(record.questionType) }}
          </template>
          <template v-else-if="column.key === 'requiredFlag'">
            <a-switch
              v-model:checked="questionConfig[questionKey(record)].requiredFlag"
              :checked-value="1"
              :un-checked-value="0"
              :disabled="!selectedSet.has(questionKey(record))"
            />
          </template>
          <template v-else-if="column.key === 'weight'">
            <a-input-number
              v-model:value="questionConfig[questionKey(record)].weight"
              :min="0"
              :max="10"
              :step="0.1"
              :disabled="!selectedSet.has(questionKey(record))"
            />
          </template>
        </template>
      </a-table>
    </a-modal>

    <a-modal v-model:open="qrOpen" title="家属端问卷二维码" :footer="null" width="460px">
      <a-space direction="vertical" style="width: 100%" align="center">
        <img v-if="qrDataUrl" :src="qrDataUrl" alt="问卷二维码" class="qr-img" />
        <a-empty v-else description="二维码生成中" />
        <a-typography-text type="secondary">扫码后自动进入家属端问卷填写页</a-typography-text>
        <a-input v-model:value="qrLink" readonly />
        <a-space>
          <a-button @click="copyQrLink">复制链接</a-button>
          <a-button @click="openQrLink">打开链接</a-button>
        </a-space>
      </a-space>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import QRCode from 'qrcode'
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
const selectedRowKeys = ref<string[]>([])

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
const selectedIds = ref<string[]>([])
const selectedSet = ref(new Set<string>())
const questionConfig = reactive<Record<string, { requiredFlag: number; weight?: number | null }>>({})
let activeTemplateId: string | null = null
const qrOpen = ref(false)
const qrDataUrl = ref('')
const qrLink = ref('')

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
  { title: '计分', dataIndex: 'scoreEnabled', key: 'scoreEnabled', width: 100 }
]

const questionColumns = [
  { title: '编码', dataIndex: 'questionCode', key: 'questionCode', width: 120 },
  { title: '题目', dataIndex: 'title', key: 'title' },
  { title: '题型', dataIndex: 'questionType', key: 'questionType', width: 120 },
  { title: '必答', key: 'requiredFlag', width: 120 },
  { title: '权重', key: 'weight', width: 120 }
]

const questionRowSelection = computed(() => ({
  selectedRowKeys: selectedIds.value,
  onChange: (keys: (string | number)[]) => {
    selectedIds.value = keys.map((k) => String(k))
    selectedSet.value = new Set(selectedIds.value)
  }
}))
const tableRowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((k) => String(k))
  }
}))
const selectedRecords = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(String(item.id))))
const selectedSingleRecord = computed(() => (selectedRecords.value.length === 1 ? selectedRecords.value[0] : null))
const canPublishSingle = computed(() => !!selectedSingleRecord.value && selectedSingleRecord.value.status !== 1)
const canDisableSingle = computed(() => !!selectedSingleRecord.value && selectedSingleRecord.value.status === 1)

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

function questionKey(question: SurveyQuestion) {
  return String(question.id)
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
    rows.value = (res.list || []).map((item) => ({
      ...item,
      id: String(item.id)
    }))
    total.value = res.total
    selectedRowKeys.value = []
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
  } catch (error: any) {
    message.error(error?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

async function remove(id: string | number) {
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

async function quickPublish(row: SurveyTemplate) {
  try {
    const detail = await getSurveyTemplateDetail(row.id)
    if (!detail.questions?.length) {
      message.warning('请先配置题目后再发布')
      return
    }
    await updateSurveyTemplate(row.id, {
      ...row,
      status: 1
    })
    message.success('模板已发布')
    await fetchData()
  } catch (error: any) {
    message.error(error?.message || '发布失败')
  }
}

async function quickDisable(row: SurveyTemplate) {
  try {
    await updateSurveyTemplate(row.id, {
      ...row,
      status: 2
    })
    message.success('模板已停用')
    await fetchData()
  } catch (error: any) {
    message.error(error?.message || '停用失败')
  }
}

function requireSingleSelection(action: string) {
  if (!selectedSingleRecord.value) {
    message.info(`请先勾选 1 条模板后再${action}`)
    return null
  }
  return selectedSingleRecord.value
}

function editSelected() {
  const record = requireSingleSelection('编辑')
  if (!record) return
  openForm(record)
}

async function configQuestionsSelected() {
  const record = requireSingleSelection('配置题目')
  if (!record) return
  await openQuestions(record)
}

async function openQrSelected() {
  const record = requireSingleSelection('查看二维码')
  if (!record) return
  await openQr(record)
}

async function publishSelected() {
  const record = requireSingleSelection('发布')
  if (!record) return
  if (record.status === 1) {
    message.info('所选模板已发布')
    return
  }
  await quickPublish(record)
}

async function disableSelected() {
  const record = requireSingleSelection('停用')
  if (!record) return
  if (record.status !== 1) {
    message.info('所选模板不是发布状态')
    return
  }
  await quickDisable(record)
}

async function removeSelected() {
  const record = requireSingleSelection('删除')
  if (!record) return
  await remove(String(record.id))
}

async function batchPublish() {
  if (selectedRowKeys.value.length === 0) return
  const validRecords = selectedRecords.value.filter((item) => item.status !== 1)
  if (validRecords.length === 0) {
    message.info('勾选项中没有可发布模板')
    return
  }
  let successCount = 0
  for (const record of validRecords) {
    try {
      const detail = await getSurveyTemplateDetail(String(record.id))
      if (!detail.questions?.length) {
        continue
      }
      await updateSurveyTemplate(String(record.id), { ...record, status: 1 })
      successCount += 1
    } catch {
      // ignore single failure
    }
  }
  if (successCount === 0) {
    message.warning('批量发布未成功：请确认模板已配置题目')
    return
  }
  message.success(`批量发布完成，共处理 ${successCount} 条`)
  await fetchData()
}

async function batchDisable() {
  if (selectedRowKeys.value.length === 0) return
  const validRecords = selectedRecords.value.filter((item) => item.status === 1)
  if (validRecords.length === 0) {
    message.info('勾选项中没有可停用模板')
    return
  }
  await Promise.all(validRecords.map((item) => updateSurveyTemplate(String(item.id), { ...item, status: 2 })))
  message.success(`批量停用完成，共处理 ${validRecords.length} 条`)
  await fetchData()
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  Modal.confirm({
    title: '提示',
    content: `确认删除选中的 ${selectedRecords.value.length} 条模板吗？`,
    async onOk() {
      await Promise.all(selectedRecords.value.map((item) => deleteSurveyTemplate(String(item.id))))
      message.success(`批量删除完成，共处理 ${selectedRecords.value.length} 条`)
      await fetchData()
    }
  })
}

async function openQr(row: SurveyTemplate) {
  const base = `${window.location.origin}/#/survey/submit`
  const link = `${base}?templateId=${encodeURIComponent(String(row.id))}&targetType=FAMILY&anonymous=1`
  qrLink.value = link
  qrOpen.value = true
  try {
    qrDataUrl.value = await QRCode.toDataURL(link, {
      width: 280,
      margin: 1
    })
  } catch {
    qrDataUrl.value = ''
    message.error('二维码生成失败，请稍后重试')
  }
}

async function copyQrLink() {
  if (!qrLink.value) return
  try {
    await navigator.clipboard.writeText(qrLink.value)
    message.success('链接已复制')
  } catch {
    message.warning('复制失败，请手动复制链接')
  }
}

function openQrLink() {
  if (!qrLink.value) return
  window.open(qrLink.value, '_blank')
}

async function openQuestions(row: SurveyTemplate) {
  activeTemplateId = String(row.id)
  questionOpen.value = true
  questionKeyword.value = ''
  await loadQuestions()
}

async function loadQuestions() {
  const res: PageResult<SurveyQuestion> = await getSurveyQuestionPage({ pageNo: 1, pageSize: 200, keyword: questionKeyword.value })
  questionList.value = (res.list || []).map((item) => ({
    ...item,
    id: String(item.id)
  }))
  questionList.value.forEach((q) => {
    const key = String(q.id)
    if (!questionConfig[key]) {
      questionConfig[key] = { requiredFlag: q.requiredFlag || 0, weight: undefined }
    }
  })
  if (activeTemplateId) {
    const detail = await getSurveyTemplateDetail(activeTemplateId)
    const selected = detail.questions.map((q) => String(q.questionId))
    selectedIds.value = selected
    selectedSet.value = new Set(selected)
    detail.questions.forEach((q) => {
      const key = String(q.questionId)
      questionConfig[key] = { requiredFlag: q.requiredFlag || 0, weight: q.weight ?? undefined }
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
  } catch (error: any) {
    message.error(error?.message || '保存失败')
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
.selection-tip {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
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
.qr-img {
  width: 280px;
  height: 280px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}
</style>
