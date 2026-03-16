<template>
  <a-card class="stats-workspace" :bordered="false">
    <div class="workspace-shell">
      <div class="workspace-copy">
        <div class="workspace-kicker">Stats Workspace</div>
        <div class="workspace-title">{{ title }}</div>
        <div class="workspace-summary">{{ summaryText }}</div>
        <div class="workspace-health">
          <a-tag color="blue">刷新 {{ dataHealth?.freshness || '--' }}</a-tag>
          <a-tag color="cyan">完整度 {{ dataHealth?.completeness || '--' }}</a-tag>
          <a-tag v-if="dataHealth?.issues?.length" color="orange">待关注 {{ dataHealth?.issues?.length }}</a-tag>
          <a-tag v-if="subscriptions.length" color="purple">订阅 {{ subscriptions.length }}</a-tag>
          <a-tag v-if="presets.length" color="geekblue">方案 {{ presets.length }}</a-tag>
        </div>
      </div>

      <div class="workspace-actions">
        <a-space wrap>
          <a-select
            v-model:value="selectedPresetId"
            :options="presetOptions"
            allow-clear
            placeholder="应用已存方案"
            style="width: 220px"
            @change="applySelectedPreset"
          />
          <a-button @click="savePresetOpen = true">保存筛选方案</a-button>
          <a-button @click="targetOpen = true">目标值设置</a-button>
          <a-button @click="subscriptionOpen = true">订阅计划</a-button>
        </a-space>
      </div>
    </div>

    <div class="workspace-row" v-if="presets.length">
      <div class="workspace-label">方案区</div>
      <a-space wrap>
        <a-tag
          v-for="item in presets"
          :key="item.id"
          class="workspace-chip"
          @click="applyPreset(item)"
        >
          {{ item.name }}
          <a-button type="link" size="small" @click.stop="removePresetRow(item.id)">删除</a-button>
        </a-tag>
      </a-space>
    </div>

    <div class="workspace-row" v-if="targetPreviewRows.length">
      <div class="workspace-label">目标区</div>
      <a-space wrap>
        <a-tag v-for="item in targetPreviewRows" :key="item.key" color="green">{{ item.label }} {{ item.value }}</a-tag>
      </a-space>
    </div>

    <div class="workspace-row" v-if="subscriptions.length">
      <div class="workspace-label">订阅区</div>
      <a-space wrap>
        <a-tag v-for="item in subscriptions" :key="item.id" :color="item.enabled ? 'purple' : 'default'">
          {{ item.name }} · {{ item.frequencyLabel }} · 下次 {{ item.nextRunText }}
        </a-tag>
      </a-space>
    </div>

    <div class="workspace-empty" v-if="emptyState?.visible">
      <div class="workspace-empty-title">{{ emptyState.title || '当前筛选下暂无结果' }}</div>
      <div class="workspace-empty-desc">{{ emptyState.description || '建议切换日期范围、检查机构范围或确认数据尚未入库。' }}</div>
      <ul class="workspace-empty-list" v-if="emptyState.hints?.length">
        <li v-for="item in emptyState.hints" :key="item">{{ item }}</li>
      </ul>
    </div>

    <a-modal v-model:open="savePresetOpen" title="保存筛选方案" @ok="savePreset" ok-text="保存" cancel-text="取消">
      <a-form layout="vertical">
        <a-form-item label="方案名称">
          <a-input v-model:value="presetForm.name" placeholder="例如：院长周会版" maxlength="24" />
        </a-form-item>
        <a-form-item label="方案摘要">
          <a-input v-model:value="presetForm.summary" placeholder="例如：近6个月 + 当前机构 + 仅异常关注" maxlength="60" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="targetOpen" title="目标值设置" @ok="saveTargets" ok-text="保存" cancel-text="取消">
      <a-form layout="vertical">
        <a-form-item v-for="item in localTargetFields" :key="item.key" :label="item.label">
          <a-input-number
            v-model:value="item.value"
            :min="item.min ?? 0"
            :max="item.max ?? 999999999"
            :step="item.step ?? 1"
            style="width: 100%;"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="subscriptionOpen" title="订阅计划" @ok="saveSubscription" ok-text="保存" cancel-text="取消">
      <a-form layout="vertical">
        <a-form-item label="计划名称">
          <a-input v-model:value="subscriptionForm.name" placeholder="例如：月经营周报" maxlength="24" />
        </a-form-item>
        <a-form-item label="频率">
          <a-select v-model:value="subscriptionForm.frequency" :options="frequencyOptions" />
        </a-form-item>
        <a-form-item label="发送时间">
          <a-time-picker v-model:value="subscriptionTime" format="HH:mm" value-format="HH:mm" style="width: 100%;" />
        </a-form-item>
        <a-form-item label="接收对象">
          <a-input v-model:value="subscriptionForm.recipient" placeholder="例如：院长/财务负责人" maxlength="32" />
        </a-form-item>
        <a-form-item label="通知方式">
          <a-select v-model:value="subscriptionForm.channel" :options="channelOptions" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="subscriptionForm.note" :rows="2" maxlength="80" />
        </a-form-item>
        <a-form-item>
          <a-switch v-model:checked="subscriptionForm.enabled" checked-children="启用" un-checked-children="停用" />
        </a-form-item>
      </a-form>

      <a-divider />
      <a-list :data-source="subscriptions" :locale="{ emptyText: '暂无订阅计划' }" size="small">
        <template #renderItem="{ item }">
          <a-list-item>
            <div class="subscription-item">
              <div>
                <div class="subscription-name">{{ item.name }}</div>
                <div class="subscription-meta">{{ item.frequencyLabel }} · {{ item.channel }} · {{ item.recipient || '未指定' }} · 下次 {{ item.nextRunText }}</div>
              </div>
              <a-space>
                <a-switch :checked="item.enabled" @change="toggleSubscriptionRow(item.id, $event)" />
                <a-button size="small" danger @click="removeSubscriptionRow(item.id)">删除</a-button>
              </a-space>
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
  listStatsPresets,
  listStatsSubscriptions,
  loadStatsTargets,
  nextSubscriptionRunText,
  removeStatsPreset,
  removeStatsSubscription,
  saveStatsPreset,
  saveStatsSubscription,
  saveStatsTargets,
  toggleStatsSubscription,
  type StatsPresetRecord,
  type StatsSubscriptionRecord
} from '../../utils/statsWorkbench'

type TargetField = {
  key: string
  label: string
  value?: number
  min?: number
  max?: number
  step?: number
}

const props = defineProps<{
  pageKey: string
  title: string
  summaryText: string
  currentPayload: Record<string, any>
  targetFields?: TargetField[]
  dataHealth?: {
    freshness?: string
    completeness?: string
    issues?: string[]
  }
  emptyState?: {
    visible?: boolean
    title?: string
    description?: string
    hints?: string[]
  }
}>()

const emit = defineEmits<{
  (event: 'apply-preset', payload: Record<string, any>): void
  (event: 'targets-change', payload: Record<string, number>): void
}>()

const presets = ref<StatsPresetRecord[]>([])
const subscriptions = ref<Array<StatsSubscriptionRecord & { nextRunText: string; frequencyLabel: string }>>([])
const selectedPresetId = ref<string>()
const savePresetOpen = ref(false)
const targetOpen = ref(false)
const subscriptionOpen = ref(false)
const presetForm = reactive({
  name: '',
  summary: ''
})
const subscriptionForm = reactive({
  name: '',
  frequency: 'WEEKLY' as 'DAILY' | 'WEEKLY' | 'MONTHLY',
  recipient: '',
  channel: '站内提醒' as '站内提醒' | '导出提醒',
  note: '',
  enabled: true
})
const subscriptionTime = ref('09:00')
const localTargetFields = ref<TargetField[]>([])

const frequencyOptions = [
  { label: '每日', value: 'DAILY' },
  { label: '每周', value: 'WEEKLY' },
  { label: '每月', value: 'MONTHLY' }
]
const channelOptions = [
  { label: '站内提醒', value: '站内提醒' },
  { label: '导出提醒', value: '导出提醒' }
]

const presetOptions = computed(() =>
  presets.value.map((item) => ({
    label: item.summary ? `${item.name} · ${item.summary}` : item.name,
    value: item.id
  }))
)

const targetPreviewRows = computed(() =>
  localTargetFields.value
    .filter((item) => item.value != null)
    .map((item) => ({
      key: item.key,
      label: item.label,
      value: Number(item.value || 0)
    }))
)

function reloadPresets() {
  presets.value = listStatsPresets(props.pageKey)
}

function reloadSubscriptions() {
  subscriptions.value = listStatsSubscriptions(props.pageKey).map((item) => ({
    ...item,
    nextRunText: nextSubscriptionRunText(item),
    frequencyLabel: item.frequency === 'DAILY' ? '每日' : item.frequency === 'WEEKLY' ? '每周' : '每月'
  }))
}

function applyPreset(record: StatsPresetRecord) {
  selectedPresetId.value = record.id
  emit('apply-preset', { ...(record.payload || {}) })
  message.success(`已应用方案：${record.name}`)
}

function applySelectedPreset(value?: string) {
  const matched = presets.value.find((item) => item.id === value)
  if (matched) {
    applyPreset(matched)
  }
}

function savePreset() {
  const name = presetForm.name.trim()
  if (!name) {
    message.warning('请输入方案名称')
    return
  }
  saveStatsPreset(props.pageKey, {
    name,
    summary: presetForm.summary.trim(),
    payload: { ...(props.currentPayload || {}) }
  })
  reloadPresets()
  presetForm.name = ''
  presetForm.summary = ''
  savePresetOpen.value = false
  message.success('筛选方案已保存')
}

function removePresetRow(id: string) {
  presets.value = removeStatsPreset(props.pageKey, id)
  if (selectedPresetId.value === id) selectedPresetId.value = undefined
}

function saveTargets() {
  const payload = localTargetFields.value.reduce<Record<string, number>>((result, item) => {
    result[item.key] = Number(item.value || 0)
    return result
  }, {})
  saveStatsTargets(props.pageKey, payload)
  emit('targets-change', payload)
  targetOpen.value = false
  message.success('目标值已保存')
}

function saveSubscription() {
  const name = subscriptionForm.name.trim()
  if (!name) {
    message.warning('请输入订阅名称')
    return
  }
  const [hourText, minuteText] = String(subscriptionTime.value || '09:00').split(':')
  saveStatsSubscription(props.pageKey, {
    name,
    frequency: subscriptionForm.frequency,
    hour: Number(hourText || 9),
    minute: Number(minuteText || 0),
    recipient: subscriptionForm.recipient.trim(),
    channel: subscriptionForm.channel,
    enabled: subscriptionForm.enabled,
    note: subscriptionForm.note.trim()
  })
  subscriptionForm.name = ''
  subscriptionForm.frequency = 'WEEKLY'
  subscriptionForm.recipient = ''
  subscriptionForm.channel = '站内提醒'
  subscriptionForm.note = ''
  subscriptionForm.enabled = true
  subscriptionTime.value = '09:00'
  reloadSubscriptions()
  message.success('订阅计划已保存')
}

function removeSubscriptionRow(id: string) {
  subscriptions.value = removeStatsSubscription(props.pageKey, id).map((item) => ({
    ...item,
    nextRunText: nextSubscriptionRunText(item),
    frequencyLabel: item.frequency === 'DAILY' ? '每日' : item.frequency === 'WEEKLY' ? '每周' : '每月'
  }))
}

function toggleSubscriptionRow(id: string, checked: boolean) {
  subscriptions.value = toggleStatsSubscription(props.pageKey, id, checked).map((item) => ({
    ...item,
    nextRunText: nextSubscriptionRunText(item),
    frequencyLabel: item.frequency === 'DAILY' ? '每日' : item.frequency === 'WEEKLY' ? '每周' : '每月'
  }))
}

function buildTargetPayload(targetFields: TargetField[]) {
  return targetFields.reduce<Record<string, number>>((result, item) => {
    result[item.key] = Number(item.value || 0)
    return result
  }, {})
}

function targetsEqual(left: Record<string, number>, right: Record<string, number>) {
  const keys = Array.from(new Set([...Object.keys(left || {}), ...Object.keys(right || {})]))
  return keys.every((key) => Number(left[key] || 0) === Number(right[key] || 0))
}

function syncTargetFields() {
  const fallback = buildTargetPayload(props.targetFields || [])
  const stored = loadStatsTargets(props.pageKey, fallback)
  localTargetFields.value = (props.targetFields || []).map((item) => ({
    ...item,
    value: stored[item.key] ?? Number(item.value || 0)
  }))
  if (!targetsEqual(fallback, stored)) {
    emit('targets-change', stored)
  }
}

watch(
  () => props.targetFields,
  () => {
    syncTargetFields()
  },
  { deep: true }
)

onMounted(() => {
  reloadPresets()
  reloadSubscriptions()
  syncTargetFields()
  presetForm.summary = `${props.summaryText} · ${dayjs().format('MM-DD HH:mm')}`
})
</script>

<style scoped>
.stats-workspace {
  margin-top: 16px;
  overflow: hidden;
  border-radius: 22px;
  background:
    radial-gradient(circle at top right, rgba(22, 119, 255, 0.14), transparent 36%),
    linear-gradient(135deg, rgba(250, 251, 253, 0.98), rgba(240, 244, 248, 0.98));
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.09);
}

.workspace-shell {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.workspace-copy {
  flex: 1;
}

.workspace-kicker {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: rgba(15, 23, 42, 0.45);
}

.workspace-title {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.workspace-summary {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.7;
  color: rgba(15, 23, 42, 0.72);
}

.workspace-health {
  margin-top: 12px;
}

.workspace-actions {
  min-width: 320px;
  display: flex;
  justify-content: flex-end;
}

.workspace-row {
  margin-top: 16px;
}

.workspace-label {
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(15, 23, 42, 0.48);
}

.workspace-chip {
  padding: 4px 10px;
  border-radius: 999px;
  cursor: pointer;
}

.workspace-empty {
  margin-top: 18px;
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.68);
  border: 1px dashed rgba(15, 23, 42, 0.15);
}

.workspace-empty-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.workspace-empty-desc {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(15, 23, 42, 0.72);
}

.workspace-empty-list {
  margin: 10px 0 0;
  padding-left: 18px;
  color: rgba(15, 23, 42, 0.72);
}

.subscription-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.subscription-name {
  font-weight: 600;
  color: #0f172a;
}

.subscription-meta {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(15, 23, 42, 0.62);
}

@media (max-width: 960px) {
  .workspace-shell {
    flex-direction: column;
  }

  .workspace-actions {
    min-width: auto;
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
