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
          <a-button :disabled="!canVerifySingle" @click="verifySelected">发布校验</a-button>
          <a-button :disabled="!selectedSingleRecord" @click="openFamilyPreviewSelected">家属端预览</a-button>
          <a-button :disabled="!selectedSingleRecord" @click="openStatsSelected">行政统计</a-button>
          <a-button :disabled="!selectedSingleRecord" @click="openPerformanceSelected">绩效榜</a-button>
          <a-button :disabled="!canPublishSingle" @click="publishSelected">发布</a-button>
          <a-button :disabled="!canDisableSingle" @click="disableSelected">停用</a-button>
          <a-button :disabled="!selectedSingleRecord" danger @click="removeSelected">删除</a-button>
          <a-button :disabled="selectedRowKeys.length === 0" @click="batchPublish">批量发布</a-button>
          <a-button :disabled="selectedRowKeys.length === 0" @click="batchDisable">批量停用</a-button>
          <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchRemove">批量删除</a-button>
          <span class="selection-tip">已勾选 {{ selectedRowKeys.length }} 条</span>
        </a-space>
      </div>
      <div class="publish-tip">发布后自动同步到家属端问卷列表与行政端问卷统计，可直接扫码填写。</div>
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
            <a-tag :color="isExpiredTemplate(record) ? 'red' : statusTag(record.status)">
              {{ isExpiredTemplate(record) ? '已过期' : statusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'scoreEnabled'">
            <a-tag :color="record.scoreEnabled === 1 ? 'green' : 'default'">
              {{ record.scoreEnabled === 1 ? '是' : '否' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'syncStatus'">
            <a-space size="small">
              <a-tag :color="syncTagColor(record)">{{ syncLabel(record) }}</a-tag>
              <a-typography-text type="secondary" class="sync-msg">{{ syncMessage(record) }}</a-typography-text>
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
            <a-select-option :value="1" disabled>发布（请用列表“发布”按钮）</a-select-option>
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
          <a-switch v-model:checked="form.scoreEnabled" :checked-value="1" :un-checked-value="0" @change="onScoreEnabledChange" />
        </a-form-item>
        <a-form-item v-if="form.scoreEnabled === 1" label="总分">
          <a-input-number v-model:value="form.totalScore" :min="0" :max="1000" style="width: 100%" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="form.description" :rows="3" />
        </a-form-item>
        <a-form-item label="问卷内容">
          <a-textarea v-model:value="form.content" :rows="4" placeholder="用于家属端/行政端展示的问卷说明内容" />
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
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import QRCode from 'qrcode'
import PageContainer from '../../components/PageContainer.vue'
import {
  getSurveyTemplatePage,
  createSurveyTemplate,
  updateSurveyTemplate,
  publishSurveyTemplate,
  disableSurveyTemplate,
  deleteSurveyTemplate,
  verifySurveyPublishedTemplate,
  getSurveyQuestionPage,
  getSurveyTemplateDetail,
  updateSurveyTemplateQuestions
} from '../../api/survey'
import type { SurveyTemplate, SurveyQuestion, SurveyTemplateQuestionItem, PageResult } from '../../types'

const route = useRoute()
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
  description: '',
  content: '',
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
const verifyState = ref<Record<string, { valid: boolean; message: string; checkedAt: string }>>({})

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
  { title: '发布联动校验', key: 'syncStatus', width: 260 },
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
const canVerifySingle = computed(() => !!selectedSingleRecord.value && selectedSingleRecord.value.status === 1)

function statusLabel(status?: number) {
  if (status === 0) return '草稿'
  if (status === 2) return '停用'
  if (status === 1) return '发布'
  return '-'
}

function statusTag(status?: number) {
  if (status === 1) return 'green'
  if (status === 2) return 'default'
  return 'orange'
}

function isExpiredTemplate(row: Partial<SurveyTemplate>) {
  if (row.status !== 1) return false
  if (!row.endDate) return false
  return dayjs(row.endDate).isBefore(dayjs(), 'day')
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
    const visibleIds = new Set(rows.value.map((item) => String(item.id)))
    verifyState.value = Object.fromEntries(
      Object.entries(verifyState.value).filter(([id]) => visibleIds.has(id))
    )
    total.value = res.total
    selectedRowKeys.value = []
  } finally {
    loading.value = false
  }
}

function initByQuery() {
  const statusText = String(route.query.status || '').trim()
  if (statusText) {
    const statusValue = Number(statusText)
    query.status = Number.isNaN(statusValue) ? undefined : statusValue
  }
  const targetType = String(route.query.targetType || '').trim().toUpperCase()
  if (['ELDER', 'STAFF', 'FAMILY', 'OTHER'].includes(targetType)) {
    query.targetType = targetType
  }
  const keyword = String(route.query.keyword || '').trim()
  if (keyword) {
    query.keyword = keyword
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
    Object.assign(form, {
      id: undefined,
      templateCode: '',
      templateName: '',
      targetType: 'ELDER',
      status: 0,
      startDate: undefined,
      endDate: undefined,
      anonymousFlag: 0,
      scoreEnabled: 0,
      totalScore: undefined,
      description: '',
      content: ''
    })
  }
  open.value = true
}

function buildPayload() {
  return {
    ...form,
    startDate: form.startDate || undefined,
    endDate: form.endDate || undefined,
    templateCode: String(form.templateCode || '').trim(),
    templateName: String(form.templateName || '').trim(),
    description: form.description ? String(form.description).trim() : undefined,
    content: form.content ? String(form.content).trim() : undefined
  }
}

function onScoreEnabledChange(enabled: boolean | string | number) {
  if (Number(enabled) !== 1) {
    form.totalScore = undefined
  }
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    const payload = buildPayload()
    if (form.id) {
      await updateSurveyTemplate(form.id, payload)
    } else {
      await createSurveyTemplate(payload)
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
    await publishSurveyTemplate(row.id)
    await verifyTemplate(row, false)
    message.success('模板已发布')
    await fetchData()
  } catch (error: any) {
    message.error(error?.message || '发布失败')
  }
}

async function quickDisable(row: SurveyTemplate) {
  try {
    await disableSurveyTemplate(row.id)
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

async function verifySelected() {
  const record = requireSingleSelection('发布校验')
  if (!record) return
  if (record.status !== 1) {
    message.info('仅发布状态模板支持校验')
    return
  }
  await verifyTemplate(record, true)
}

function openFamilyPreviewSelected() {
  const record = requireSingleSelection('家属端预览')
  if (!record) return
  const link = `${window.location.origin}/#/survey/submit?templateId=${encodeURIComponent(String(record.id))}&templateCode=${encodeURIComponent(String(record.templateCode || ''))}&targetType=${encodeURIComponent(String(record.targetType || 'FAMILY'))}&anonymous=${encodeURIComponent(String(record.anonymousFlag ?? 1))}&from=preview`
  window.open(link, '_blank')
}

function openStatsSelected() {
  const record = requireSingleSelection('查看统计')
  if (!record) return
  const link = `${window.location.origin}/#/survey/stats?templateId=${encodeURIComponent(String(record.id))}`
  window.open(link, '_blank')
}

function openPerformanceSelected() {
  const record = requireSingleSelection('查看绩效榜')
  if (!record) return
  const link = `${window.location.origin}/#/survey/performance?templateId=${encodeURIComponent(String(record.id))}`
  window.open(link, '_blank')
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
  let failedCount = 0
  let firstError = ''
  for (const record of validRecords) {
    try {
      await publishSurveyTemplate(String(record.id))
      successCount += 1
    } catch (error: any) {
      failedCount += 1
      if (!firstError) {
        firstError = error?.message || ''
      }
    }
  }
  if (successCount === 0) {
    message.warning(firstError || '批量发布未成功：请确认模板已配置题目且日期有效')
    return
  }
  if (failedCount > 0) {
    message.warning(`批量发布完成：成功 ${successCount} 条，失败 ${failedCount} 条${firstError ? `（${firstError}）` : ''}`)
  } else {
    message.success(`批量发布完成，共处理 ${successCount} 条`)
  }
  await fetchData()
}

async function batchDisable() {
  if (selectedRowKeys.value.length === 0) return
  const validRecords = selectedRecords.value.filter((item) => item.status === 1)
  if (validRecords.length === 0) {
    message.info('勾选项中没有可停用模板')
    return
  }
  const settled = await Promise.allSettled(validRecords.map((item) => disableSurveyTemplate(String(item.id))))
  const successCount = settled.filter((item) => item.status === 'fulfilled').length
  const failedCount = settled.length - successCount
  if (failedCount > 0) {
    message.warning(`批量停用完成：成功 ${successCount} 条，失败 ${failedCount} 条`)
  } else {
    message.success(`批量停用完成，共处理 ${successCount} 条`)
  }
  await fetchData()
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  Modal.confirm({
    title: '提示',
    content: `确认删除选中的 ${selectedRecords.value.length} 条模板吗？`,
    async onOk() {
      const settled = await Promise.allSettled(selectedRecords.value.map((item) => deleteSurveyTemplate(String(item.id))))
      const successCount = settled.filter((item) => item.status === 'fulfilled').length
      const failedCount = settled.length - successCount
      if (failedCount > 0) {
        message.warning(`批量删除完成：成功 ${successCount} 条，失败 ${failedCount} 条`)
      } else {
        message.success(`批量删除完成，共处理 ${successCount} 条`)
      }
      await fetchData()
    }
  })
}

async function openQr(row: SurveyTemplate) {
  const verify = await verifyTemplate(row, false)
  if (!verify.valid) {
    message.warning(verify.message || '当前模板不可生成二维码')
    return
  }
  const detail = await getSurveyTemplateDetail(row.id)
  if (!detail.questions?.length) {
    message.warning('模板未配置题目，二维码暂不可用')
    return
  }
  const base = `${window.location.origin}/#/survey/submit`
  const link = `${base}?templateId=${encodeURIComponent(String(row.id))}&templateCode=${encodeURIComponent(String(row.templateCode || ''))}&targetType=${encodeURIComponent(String(row.targetType || 'FAMILY'))}&anonymous=${encodeURIComponent(String(row.anonymousFlag ?? 1))}&from=qr`
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

async function verifyTemplate(row: SurveyTemplate, showMessage: boolean) {
  const verify = await verifySurveyPublishedTemplate(row.id, {
    templateCode: row.templateCode || undefined,
    targetType: row.targetType || undefined
  })
  verifyState.value[String(row.id)] = {
    valid: !!verify.valid,
    message: verify.message || (verify.valid ? '家属端与行政端均可访问' : '发布校验失败'),
    checkedAt: dayjs().format('MM-DD HH:mm')
  }
  if (showMessage) {
    if (verify.valid) {
      message.success(verify.message || '校验通过：家属端与行政端已可访问')
    } else {
      message.warning(verify.message || '校验失败，请检查模板配置与发布时间')
    }
  }
  return verify
}

function syncLabel(record: SurveyTemplate) {
  const state = verifyState.value[String(record.id)]
  if (record.status !== 1) return '未发布'
  if (!state) return '待校验'
  return state.valid ? '已同步' : '异常'
}

function syncMessage(record: SurveyTemplate) {
  const state = verifyState.value[String(record.id)]
  if (record.status !== 1) return '发布后可校验家属端/行政端联动'
  if (!state) return '点击“发布校验”验证二维码与填写页'
  return `${state.message}${state.checkedAt ? `（${state.checkedAt}）` : ''}`
}

function syncTagColor(record: SurveyTemplate) {
  const state = verifyState.value[String(record.id)]
  if (record.status !== 1) return 'default'
  if (!state) return 'blue'
  return state.valid ? 'green' : 'red'
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

onMounted(async () => {
  initByQuery()
  await fetchData()
})
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
.publish-tip {
  margin-bottom: 10px;
  color: rgba(0, 0, 0, 0.65);
  font-size: 12px;
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
.sync-msg {
  max-width: 180px;
}
.qr-img {
  width: 280px;
  height: 280px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}
</style>
