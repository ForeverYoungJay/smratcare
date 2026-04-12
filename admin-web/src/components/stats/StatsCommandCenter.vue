<template>
  <a-card class="stats-command-center" :bordered="false">
    <div class="command-header">
      <div>
        <div class="command-kicker">Action Center</div>
        <div class="command-title">{{ title }}</div>
        <div class="command-summary">{{ summaryText }}</div>
      </div>
      <a-space wrap>
        <a-button @click="templateOpen = true">报表模板</a-button>
        <a-button @click="panelOpen = true">自定义面板</a-button>
        <a-button @click="feedbackOpen = true">数据纠错</a-button>
      </a-space>
    </div>

    <div class="command-grid">
      <div class="command-block" v-if="actionItems.length">
        <div class="command-label">处置动作</div>
        <a-space wrap>
          <a-button
            v-for="item in actionItems"
            :key="item.key"
            size="small"
            :type="item.tone === 'danger' ? 'primary' : 'default'"
            @click="emit('trigger-action', item)"
          >
            {{ item.label }}
          </a-button>
        </a-space>
        <div class="command-help" v-if="actionItems[0]?.description">{{ actionItems[0].description }}</div>
      </div>

      <div class="command-block" v-if="anomalyRows.length">
        <div class="command-label">异常提示</div>
        <div class="anomaly-list">
          <div v-for="item in anomalyRows" :key="item.key" class="anomaly-item">
            <a-tag :color="resolveAnomalyColor(item.tone)">{{ item.label }}</a-tag>
            <span>{{ item.detail }}</span>
          </div>
        </div>
      </div>

      <div class="command-block" v-if="metricNotes.length">
        <div class="command-label">口径说明</div>
        <a-space wrap>
          <a-tooltip v-for="item in metricNotes" :key="item.key" :title="item.note">
            <a-tag color="blue">{{ item.label }}</a-tag>
          </a-tooltip>
        </a-space>
      </div>
    </div>

    <a-modal v-model:open="templateOpen" title="报表模板" @ok="saveTemplate" ok-text="保存模板" cancel-text="关闭">
      <a-form layout="vertical">
        <a-form-item label="模板名称">
          <a-input v-model:value="templateForm.name" maxlength="24" placeholder="例如：院长周会打印版" />
        </a-form-item>
        <a-form-item label="模板摘要">
          <a-input v-model:value="templateForm.summary" maxlength="60" placeholder="例如：近6个月 + 核心列" />
        </a-form-item>
        <a-form-item label="列选择" v-if="columnOptions.length">
          <a-checkbox-group v-model:value="templateColumns" :options="columnOptions" />
        </a-form-item>
      </a-form>
      <a-divider />
      <a-list :data-source="reportTemplates" :locale="{ emptyText: '暂无报表模板' }" size="small">
        <template #renderItem="{ item }">
          <a-list-item>
            <div class="template-item">
              <div>
                <div class="template-name">{{ item.name }}</div>
                <div class="template-meta">{{ item.summary || '未填写摘要' }} · {{ item.columns.length }} 列</div>
              </div>
              <a-space>
                <a-button size="small" @click="applyTemplate(item)">应用</a-button>
                <a-button size="small" danger @click="removeTemplateRow(item.id)">删除</a-button>
              </a-space>
            </div>
          </a-list-item>
        </template>
      </a-list>
    </a-modal>

    <a-modal v-model:open="panelOpen" title="自定义面板" @ok="savePanel" ok-text="保存" cancel-text="取消">
      <a-checkbox-group v-model:value="localPanelKeys" class="panel-grid">
        <div v-for="item in panelOptions" :key="item.key" class="panel-option">
          <a-checkbox :value="item.key">
            <div class="panel-option-title">{{ item.label }}</div>
            <div class="panel-option-desc">{{ item.description || '常用观察指标' }}</div>
          </a-checkbox>
        </div>
      </a-checkbox-group>
    </a-modal>

    <a-modal v-model:open="feedbackOpen" title="数据纠错" @ok="saveFeedback" ok-text="提交" cancel-text="取消">
      <a-form layout="vertical">
        <a-form-item label="问题标题">
          <a-input v-model:value="feedbackForm.title" maxlength="40" placeholder="例如：某机构月收入异常偏低" />
        </a-form-item>
        <a-form-item label="问题详情">
          <a-textarea v-model:value="feedbackForm.detail" :rows="3" maxlength="200" />
        </a-form-item>
        <a-form-item label="紧急程度">
          <a-select v-model:value="feedbackForm.urgency" :options="urgencyOptions" />
        </a-form-item>
      </a-form>
      <a-divider />
      <a-list :data-source="feedbackRows" :locale="{ emptyText: '暂无纠错记录' }" size="small">
        <template #renderItem="{ item }">
          <a-list-item>
            <div class="feedback-item">
              <div>
                <div class="feedback-title">{{ item.title }}</div>
                <div class="feedback-meta">{{ urgencyLabel(item.urgency) }} · {{ item.scope || '当前页' }} · {{ formatTime(item.createdAt) }}</div>
              </div>
              <a-tag color="orange">{{ item.status === 'OPEN' ? '待处理' : '跟进中' }}</a-tag>
            </div>
          </a-list-item>
        </template>
      </a-list>
    </a-modal>
  </a-card>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  listStatsFeedback,
  listStatsReportTemplates,
  loadStatsPanelConfig,
  removeStatsReportTemplate,
  saveStatsFeedback,
  saveStatsPanelConfig,
  saveStatsReportTemplate,
  type StatsFeedbackRecord,
  type StatsReportTemplateRecord
} from '../../utils/statsCommandCenter'

type ActionItem = {
  key: string
  label: string
  description?: string
  route?: string
  tone?: 'neutral' | 'warning' | 'danger'
}

type MetricNote = {
  key: string
  label: string
  note: string
}

type PanelOption = {
  key: string
  label: string
  description?: string
}

const props = defineProps<{
  pageKey: string
  title: string
  summaryText: string
  shareTitle: string
  currentPayload: Record<string, any>
  metricVersion?: string
  actionItems?: ActionItem[]
  anomalies?: Array<{ key: string; label: string; detail?: string; tone?: 'good' | 'warning' | 'danger' | 'neutral' }>
  metricNotes?: MetricNote[]
  panelOptions?: PanelOption[]
  selectedPanelKeys?: string[]
  templateColumnOptions?: Array<{ label: string; value: string }>
  selectedTemplateColumns?: string[]
  permissionNote?: string
}>()

const emit = defineEmits<{
  (event: 'trigger-action', payload: ActionItem): void
  (event: 'panel-change', payload: string[]): void
  (event: 'apply-template', payload: { payload: Record<string, any>; columns: string[] }): void
}>()

const actionItems = computed(() => props.actionItems || [])
const anomalyRows = computed(() => props.anomalies || [])
const metricNotes = computed(() => props.metricNotes || [])
const panelOptions = computed(() => props.panelOptions || [])
const columnOptions = computed(() => props.templateColumnOptions || [])
const reportTemplates = ref<StatsReportTemplateRecord[]>([])
const feedbackRows = ref<StatsFeedbackRecord[]>([])
const templateOpen = ref(false)
const panelOpen = ref(false)
const feedbackOpen = ref(false)
const templateColumns = ref<string[]>([])
const localPanelKeys = ref<string[]>([])
const templateForm = reactive({
  name: '',
  summary: ''
})
const feedbackForm = reactive({
  title: '',
  detail: '',
  urgency: 'MEDIUM' as 'LOW' | 'MEDIUM' | 'HIGH'
})

const urgencyOptions = [
  { label: '低', value: 'LOW' },
  { label: '中', value: 'MEDIUM' },
  { label: '高', value: 'HIGH' }
]

function reloadTemplates() {
  reportTemplates.value = listStatsReportTemplates(props.pageKey)
}

function reloadFeedback() {
  feedbackRows.value = listStatsFeedback(props.pageKey)
}

function panelKeysEqual(left: string[] = [], right: string[] = []) {
  if (left.length !== right.length) return false
  return left.every((item, index) => item === right[index])
}

function syncPanelKeys() {
  const fallback = props.selectedPanelKeys?.length
    ? props.selectedPanelKeys
    : panelOptions.value.map((item) => item.key)
  const nextKeys = loadStatsPanelConfig(props.pageKey, fallback)
  if (!panelKeysEqual(localPanelKeys.value, nextKeys)) {
    localPanelKeys.value = [...nextKeys]
  }
  if (!panelKeysEqual(props.selectedPanelKeys || [], nextKeys)) {
    emit('panel-change', [...nextKeys])
  }
}

function syncTemplateColumns() {
  templateColumns.value = props.selectedTemplateColumns?.length
    ? [...props.selectedTemplateColumns]
    : columnOptions.value.map((item) => item.value)
}

function resolveAnomalyColor(tone?: string) {
  if (tone === 'danger') return 'red'
  if (tone === 'warning') return 'orange'
  if (tone === 'good') return 'green'
  return 'blue'
}

function urgencyLabel(value: StatsFeedbackRecord['urgency']) {
  if (value === 'HIGH') return '高'
  if (value === 'LOW') return '低'
  return '中'
}

function formatTime(value: string) {
  return dayjs(value).format('MM-DD HH:mm')
}

function saveTemplate() {
  const name = templateForm.name.trim()
  if (!name) {
    message.warning('请输入模板名称')
    return
  }
  const columns = templateColumns.value.length
    ? [...templateColumns.value]
    : columnOptions.value.map((item) => item.value)
  saveStatsReportTemplate(props.pageKey, {
    name,
    summary: templateForm.summary.trim(),
    columns,
    payload: { ...(props.currentPayload || {}) }
  })
  templateForm.name = ''
  templateForm.summary = ''
  reloadTemplates()
  templateOpen.value = false
  message.success('报表模板已保存')
}

function applyTemplate(record: StatsReportTemplateRecord) {
  emit('apply-template', {
    payload: { ...(record.payload || {}) },
    columns: [...(record.columns || [])]
  })
  templateOpen.value = false
  message.success(`已应用模板：${record.name}`)
}

function removeTemplateRow(id: string) {
  reportTemplates.value = removeStatsReportTemplate(props.pageKey, id)
}

function savePanel() {
  const nextKeys = localPanelKeys.value.length
    ? [...localPanelKeys.value]
    : panelOptions.value.map((item) => item.key)
  saveStatsPanelConfig(props.pageKey, nextKeys)
  emit('panel-change', nextKeys)
  panelOpen.value = false
  message.success('自定义面板已保存')
}

function saveFeedback() {
  const title = feedbackForm.title.trim()
  const detail = feedbackForm.detail.trim()
  if (!title || !detail) {
    message.warning('请填写完整的问题标题和详情')
    return
  }
  feedbackRows.value = saveStatsFeedback(props.pageKey, {
    title,
    detail,
    urgency: feedbackForm.urgency,
    scope: props.shareTitle
  })
  feedbackForm.title = ''
  feedbackForm.detail = ''
  feedbackForm.urgency = 'MEDIUM'
  feedbackOpen.value = false
  message.success('纠错记录已提交')
}

watch(
  () => props.selectedTemplateColumns,
  () => {
    syncTemplateColumns()
  },
  { deep: true }
)

watch(
  () => props.selectedPanelKeys,
  () => {
    syncPanelKeys()
  },
  { deep: true }
)

onMounted(() => {
  reloadTemplates()
  reloadFeedback()
  syncPanelKeys()
  syncTemplateColumns()
  templateForm.summary = `${props.summaryText} · ${dayjs().format('MM-DD HH:mm')}`
})
</script>

<style scoped>
.stats-command-center {
  margin-top: 16px;
  border-radius: 22px;
  background:
    radial-gradient(circle at bottom left, rgba(19, 194, 194, 0.14), transparent 28%),
    linear-gradient(135deg, rgba(255, 252, 247, 0.98), rgba(244, 248, 252, 0.98));
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.08);
}

.command-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.command-kicker {
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(15, 23, 42, 0.45);
}

.command-title {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
}

.command-summary {
  margin-top: 8px;
  max-width: 720px;
  font-size: 14px;
  line-height: 1.7;
  color: rgba(15, 23, 42, 0.72);
}

.command-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}

.command-block {
  min-height: 124px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.74);
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.command-label {
  margin-bottom: 10px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(15, 23, 42, 0.5);
}

.command-help {
  margin-top: 10px;
  font-size: 12px;
  color: rgba(15, 23, 42, 0.6);
}

.anomaly-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.anomaly-item {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  font-size: 13px;
  color: rgba(15, 23, 42, 0.7);
}

.template-item,
.feedback-item {
  width: 100%;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.template-name,
.feedback-title,
.panel-option-title {
  font-weight: 600;
  color: #0f172a;
}

.template-meta,
.feedback-meta,
.panel-option-desc {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(15, 23, 42, 0.62);
}

.panel-grid {
  width: 100%;
}

.panel-option {
  padding: 10px 0;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}

@media (max-width: 960px) {
  .command-header {
    flex-direction: column;
  }

  .command-grid {
    grid-template-columns: 1fr;
  }
}
</style>
