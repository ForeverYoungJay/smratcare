<template>
  <PageContainer title="问卷填写" subTitle="在线提交与回收">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="form" layout="inline" class="search-bar">
        <a-form-item label="模板">
          <a-select
            v-model:value="form.templateId"
            show-search
            placeholder="请选择模板"
            style="width: 240px"
            :disabled="fromQr"
            @change="onTemplateChange"
          >
            <a-select-option v-for="tpl in templates" :key="tpl.id" :value="tpl.id">{{ tpl.templateName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="对象类型">
          <a-select v-model:value="form.targetType" style="width: 160px" :disabled="fromQr" @change="loadTemplates">
            <a-select-option value="ELDER">老人</a-select-option>
            <a-select-option value="STAFF">员工</a-select-option>
            <a-select-option value="FAMILY">家属</a-select-option>
            <a-select-option value="OTHER">其他</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="对象ID">
          <a-input v-model:value="form.targetId" placeholder="可选" style="width: 160px" />
        </a-form-item>
        <a-form-item label="关联员工">
          <a-input v-model:value="form.relatedStaffId" placeholder="可选" style="width: 160px" />
        </a-form-item>
        <a-form-item label="匿名">
          <a-switch v-model:checked="form.anonymousFlag" :checked-value="1" :un-checked-value="0" :disabled="fromQr" />
        </a-form-item>
      </a-form>
      <a-alert
        v-if="fromQr"
        type="info"
        show-icon
        style="margin-top: 8px"
        message="当前通过家属端二维码进入，模板与对象已自动锁定。"
      />
      <a-alert
        v-if="verifyMessage"
        :type="verifyValid ? 'success' : 'warning'"
        show-icon
        style="margin-top: 8px"
        :message="verifyMessage"
        :description="!verifyValid && fromQr ? '当前链接可能已过期或参数异常，请返回重新扫码进入。' : undefined"
      />
    </a-card>

    <a-card v-if="detail" class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="template-header">
        <div>
          <div class="template-title">{{ detail.templateName }}</div>
          <div class="template-desc">{{ detail.content || detail.description || '暂无描述' }}</div>
        </div>
        <a-tag :color="detail.scoreEnabled === 1 ? 'green' : 'default'">
          {{ detail.scoreEnabled === 1 ? '计分问卷' : '非计分问卷' }}
        </a-tag>
      </div>

      <a-form class="question-list" layout="vertical">
        <div v-for="question in detail.questions" :key="question.questionId" class="question-item">
          <div class="question-title">
            <span class="index">{{ question.sortNo || 0 }}</span>
            <span>{{ question.title }}</span>
            <a-tag v-if="question.requiredFlag === 1" color="red">必答</a-tag>
          </div>
          <div class="question-body">
            <a-radio-group
              v-if="question.questionType === 'SINGLE'"
              v-model:value="answers[question.questionId]"
            >
              <a-radio
                v-for="opt in parseOptions(question.optionsJson)"
                :key="opt.value"
                :value="opt.value"
              >
                {{ opt.label }}
              </a-radio>
            </a-radio-group>

            <a-checkbox-group
              v-else-if="question.questionType === 'MULTI'"
              v-model:value="answers[question.questionId]"
              :options="parseOptions(question.optionsJson).map((o) => ({ label: o.label, value: o.value }))"
            />

            <a-textarea
              v-else-if="question.questionType === 'TEXT'"
              v-model:value="answers[question.questionId]"
              :rows="3"
              placeholder="请输入内容"
            />

            <a-rate
              v-else-if="question.questionType === 'RATING'"
              v-model:value="answers[question.questionId]"
              :count="question.maxScore || 5"
              allow-half
            />

            <a-input-number
              v-else-if="question.questionType === 'SCORE'"
              v-model:value="answers[question.questionId]"
              :min="0"
              :max="question.maxScore || 10"
            />

            <a-input v-else v-model:value="answers[question.questionId]" placeholder="请输入答案" />

            <div v-if="detail.scoreEnabled === 1 && question.scoreEnabled === 1 && question.questionType !== 'RATING' && question.questionType !== 'SCORE'" class="score-input">
              <span>评分：</span>
              <a-input-number v-model:value="scores[question.questionId]" :min="0" :max="question.maxScore || 10" />
            </div>
          </div>
        </div>
      </a-form>

      <div class="actions">
        <a-button type="primary" :loading="submitting" :disabled="!verifyValid" @click="submit">提交问卷</a-button>
      </div>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import {
  getSurveyPublishedTemplateDetail,
  getSurveyPublishedTemplatePage,
  verifySurveyPublishedTemplate,
  submitSurvey
} from '../../api/survey'
import type { SurveyTemplate, SurveyTemplateDetail, SurveySubmissionRequest, SurveySubmissionAnswerItem, PageResult } from '../../types'

const route = useRoute()
const templates = ref<SurveyTemplate[]>([])
const detail = ref<SurveyTemplateDetail | null>(null)
const submitting = ref(false)
const fromQr = ref(false)
const verifyValid = ref(true)
const verifyMessage = ref('')
const templateCodeFromQr = ref<string | undefined>()

const form = reactive({
  templateId: undefined as string | undefined,
  targetType: 'ELDER',
  targetId: undefined as string | undefined,
  relatedStaffId: undefined as string | undefined,
  anonymousFlag: 0
})

const answers = reactive<Record<string, any>>({})
const scores = reactive<Record<string, number | undefined>>({})

const optionCache = new Map<string, { label: string; value: any }[]>()

async function loadTemplates() {
  const res: PageResult<SurveyTemplate> = await getSurveyPublishedTemplatePage({
    pageNo: 1,
    pageSize: 200,
    targetType: form.targetType
  })
  templates.value = (res.list || []).map((item) => ({
    ...item,
    id: String(item.id)
  }))
}

function normalizeId(value: unknown) {
  const text = String(value || '').trim()
  return text ? text : undefined
}

function normalizeTargetType(value: unknown) {
  const text = String(value || '').trim().toUpperCase()
  if (['ELDER', 'STAFF', 'FAMILY', 'OTHER'].includes(text)) {
    return text
  }
  return undefined
}

async function initByQuery() {
  const templateId = normalizeId(route.query.templateId)
  const templateCode = normalizeId(route.query.templateCode)
  const targetType = normalizeTargetType(route.query.targetType)
  const anonymous = String(route.query.anonymous || '').trim()
  const targetId = normalizeId(route.query.targetId)
  const relatedStaffId = normalizeId(route.query.relatedStaffId)
  let shouldReloadTemplates = false
  if (targetType) {
    shouldReloadTemplates = targetType !== form.targetType
    form.targetType = targetType
  }
  if (shouldReloadTemplates) {
    await loadTemplates()
  }
  if (targetId) {
    form.targetId = targetId
  }
  if (relatedStaffId) {
    form.relatedStaffId = relatedStaffId
  }
  if (anonymous === '1' || anonymous.toLowerCase() === 'true') {
    form.anonymousFlag = 1
  }
  if (templateId) {
    form.templateId = templateId
    templateCodeFromQr.value = templateCode
    fromQr.value = true
    await onTemplateChange()
  }
}

async function verifyCurrentTemplate() {
  if (!form.templateId) {
    verifyValid.value = false
    verifyMessage.value = '请选择模板'
    return false
  }
  const verify = await verifySurveyPublishedTemplate(form.templateId, {
    templateCode: templateCodeFromQr.value,
    targetType: form.targetType
  })
  verifyValid.value = !!verify.valid
  verifyMessage.value = verify.message || ''
  return verifyValid.value
}

async function onTemplateChange() {
  if (!form.templateId) return
  try {
    const valid = await verifyCurrentTemplate()
    if (!valid) {
      detail.value = null
      return
    }
    detail.value = await getSurveyPublishedTemplateDetail(form.templateId)
    if (detail.value?.targetType) {
      form.targetType = detail.value.targetType
    }
    Object.keys(answers).forEach((key) => delete answers[key])
    Object.keys(scores).forEach((key) => delete scores[key])
  } catch (error: any) {
    detail.value = null
    verifyValid.value = false
    verifyMessage.value = ''
    message.error(error?.message || '模板加载失败')
  }
}

function parseOptions(optionsJson?: string | null) {
  if (!optionsJson) return []
  if (optionCache.has(optionsJson)) return optionCache.get(optionsJson) || []
  try {
    const parsed = JSON.parse(optionsJson)
    if (Array.isArray(parsed)) {
      const opts = parsed.map((item, idx) => {
        if (typeof item === 'string' || typeof item === 'number') {
          return { label: String(item), value: item }
        }
        if (typeof item === 'object' && item) {
          return { label: String(item.label ?? item.value ?? idx + 1), value: item.value ?? item.label ?? idx + 1 }
        }
        return { label: String(item), value: item }
      })
      optionCache.set(optionsJson, opts)
      return opts
    }
  } catch {
    // ignore
  }
  return []
}

function buildAnswers(): SurveySubmissionAnswerItem[] {
  if (!detail.value) return []
  return detail.value.questions.map((q) => {
    const value = answers[q.questionId]
    const item: SurveySubmissionAnswerItem = { questionId: q.questionId }
    if (q.questionType === 'TEXT') {
      item.answerText = value
    } else if (q.questionType === 'MULTI') {
      item.answerJson = JSON.stringify(value || [])
    } else if (q.questionType === 'SINGLE') {
      item.answerJson = value === undefined ? undefined : JSON.stringify(value)
    } else if (q.questionType === 'RATING' || q.questionType === 'SCORE') {
      item.answerJson = value === undefined ? undefined : JSON.stringify(value)
      item.score = value
    } else {
      item.answerText = value
    }
    if (detail.value?.scoreEnabled === 1 && q.scoreEnabled === 1 && q.questionType !== 'RATING' && q.questionType !== 'SCORE') {
      item.score = scores[String(q.questionId)]
    }
    return item
  })
}

function validateRequired(): boolean {
  if (!detail.value) return false
  for (const q of detail.value.questions) {
    if (q.requiredFlag === 1) {
      const val = answers[String(q.questionId)]
      if (val === undefined || val === null || (Array.isArray(val) && val.length === 0) || (typeof val === 'string' && val.trim() === '')) {
        message.warning(`请完成必答题：${q.title}`)
        return false
      }
    }
  }
  return true
}

async function submit() {
  if (!detail.value || !form.templateId) return
  const valid = await verifyCurrentTemplate()
  if (!valid) {
    message.warning(verifyMessage.value || '问卷当前不可提交')
    return
  }
  if (!validateRequired()) return
  submitting.value = true
  try {
    const payload: SurveySubmissionRequest = {
      templateId: form.templateId,
      templateCode: templateCodeFromQr.value || detail.value.templateCode,
      targetType: form.targetType,
      targetId: normalizeId(form.targetId),
      relatedStaffId: normalizeId(form.relatedStaffId),
      anonymousFlag: form.anonymousFlag,
      answers: buildAnswers()
    }
    const res = await submitSurvey(payload)
    message.success(`提交成功，得分：${res.scoreTotal ?? 0}`)
  } catch (error: any) {
    message.error(error?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadTemplates()
  await initByQuery()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
.template-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.template-title {
  font-size: 18px;
  font-weight: 600;
}
.template-desc {
  color: rgba(0, 0, 0, 0.6);
  margin-top: 4px;
}
.question-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.question-item {
  padding: 16px;
  border-radius: 12px;
  background: rgba(148, 163, 184, 0.08);
}
.question-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  margin-bottom: 12px;
}
.question-title .index {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(14, 165, 233, 0.2);
  color: #0ea5e9;
  display: grid;
  place-items: center;
  font-size: 12px;
}
.score-input {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.actions {
  margin-top: 20px;
  text-align: right;
}
</style>
