<template>
  <PageContainer title="安全场景规则" subTitle="跌倒/SOS/离床/离院/久滞/体征异常等场景告警规则（内置全局规则只读，机构可新增定制）">
    <SearchForm :model="query" @search="onSearch" @reset="onReset">
      <a-form-item label="事件类型">
        <a-select v-model:value="query.eventType" allow-clear style="width: 180px" placeholder="全部">
          <a-select-option v-for="t in eventTypeOptions" :key="t.value" :value="t.value">{{ t.label }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.enabled" allow-clear style="width: 120px" placeholder="全部">
          <a-select-option :value="1">启用</a-select-option>
          <a-select-option :value="0">停用</a-select-option>
        </a-select>
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增规则</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'eventType'">{{ eventTypeText(record.eventType) }}</template>
        <template v-else-if="column.key === 'condition'">
          <span v-if="record.operator === 'PRESENT' || !record.metricKey">事件触发</span>
          <span v-else>{{ record.metricKey }} {{ opText(record.operator) }} {{ record.threshold }}</span>
        </template>
        <template v-else-if="column.key === 'level'">
          <a-tag :color="levelColor(record.level)">{{ levelText(record.level) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'flags'">
          <a-tag :color="record.autoDispatch ? 'blue' : 'default'">{{ record.autoDispatch ? '自动派单' : '不派单' }}</a-tag>
          <a-tag v-if="record.notifyFamily" color="gold">告知家属</a-tag>
        </template>
        <template v-else-if="column.key === 'scope'">
          {{ record.orgId ? '机构定制' : '全局内置' }}
        </template>
        <template v-else-if="column.key === 'enabled'">
          <a-switch :checked="record.enabled === 1" @change="(v) => toggle(record, v)" />
        </template>
        <template v-else-if="column.key === 'action'">
          <div class="row-action-links">
            <a-button type="link" size="small" :disabled="!record.orgId" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" size="small" danger :disabled="!record.orgId" @click="remove(record)">删除</a-button>
          </div>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑规则' : '新增规则'" :confirm-loading="saving" width="680px" @ok="submit">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="规则编码" required><a-input v-model:value="form.ruleCode" /></a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="规则名称" required><a-input v-model:value="form.ruleName" /></a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="事件类型" required>
              <a-select v-model:value="form.eventType" :options="eventTypeOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="判定方式">
              <a-select v-model:value="form.operator" :options="operatorOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="告警等级">
              <a-select v-model:value="form.level" :options="levelOptions" />
            </a-form-item>
          </a-col>
          <template v-if="form.operator !== 'PRESENT'">
            <a-col :span="8">
              <a-form-item label="指标字段"><a-input v-model:value="form.metricKey" placeholder="如 heartRate / spo2" /></a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="阈值"><a-input-number v-model:value="form.threshold" style="width: 100%" /></a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="阈值2（区间）"><a-input-number v-model:value="form.threshold2" style="width: 100%" /></a-form-item>
            </a-col>
          </template>
          <a-col :span="8">
            <a-form-item label="持续时长(秒)"><a-input-number v-model:value="form.durationSec" :min="0" style="width: 100%" /></a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="优先级(小者优先)"><a-input-number v-model:value="form.priority" :min="0" style="width: 100%" /></a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="适用失能等级"><a-input v-model:value="form.disabilityLevelScope" placeholder="如 4,5；空=全部" /></a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="适用护理等级"><a-input v-model:value="form.careLevelScope" placeholder="如 一级,特级；空=全部" /></a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="适用设备类型"><a-input v-model:value="form.deviceType" placeholder="如 MATTRESS；空=全部" /></a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="自动派单">
              <a-switch v-model:checked="autoDispatchBool" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="告知家属">
              <a-switch v-model:checked="notifyFamilyBool" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注"><a-textarea v-model:value="form.remark" :rows="2" /></a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  getSmartRulePage,
  createSmartRule,
  updateSmartRule,
  setSmartRuleEnabled,
  deleteSmartRule
} from '../../api/smartCare'
import type { Id, PageResult, SmartAlertRule } from '../../types'

const eventTypeOptions = [
  { label: '跌倒 FALL', value: 'FALL' },
  { label: 'SOS呼叫', value: 'SOS' },
  { label: '离床超时 BED_EXIT_TIMEOUT', value: 'BED_EXIT_TIMEOUT' },
  { label: '离院防走失 GEO_FENCE', value: 'GEO_FENCE' },
  { label: '久滞 LINGER', value: 'LINGER' },
  { label: '体征异常 VITAL', value: 'VITAL' }
]
// 旧枚举别名（服务端自动归一，仅用于展示存量规则）
const legacyEventTypeText: Record<string, string> = {
  BED_EXIT: '离床超时(旧 BED_EXIT)',
  LEAVE: '离院防走失(旧 LEAVE)',
  STAY: '久滞(旧 STAY)',
  VITAL_ABNORMAL: '体征异常(旧 VITAL_ABNORMAL)'
}
const operatorOptions = [
  { label: '事件触发(PRESENT)', value: 'PRESENT' },
  { label: '大于 >', value: 'GT' },
  { label: '大于等于 ≥', value: 'GE' },
  { label: '小于 <', value: 'LT' },
  { label: '小于等于 ≤', value: 'LE' },
  { label: '等于 =', value: 'EQ' },
  { label: '区间外 RANGE_OUT', value: 'RANGE_OUT' }
]
const levelOptions = [
  { label: '提醒 WARNING', value: 'WARNING' },
  { label: '高 HIGH', value: 'HIGH' },
  { label: '危急 CRITICAL', value: 'CRITICAL' }
]

const loading = ref(false)
const saving = ref(false)
const rows = ref<SmartAlertRule[]>([])
const query = reactive({ eventType: undefined as string | undefined, enabled: undefined as number | undefined, pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '编码', dataIndex: 'ruleCode', key: 'ruleCode', width: 130 },
  { title: '名称', dataIndex: 'ruleName', key: 'ruleName', width: 130 },
  { title: '事件', key: 'eventType', width: 110 },
  { title: '判定条件', key: 'condition', width: 180 },
  { title: '等级', key: 'level', width: 90 },
  { title: '动作', key: 'flags', width: 160 },
  { title: '来源', key: 'scope', width: 90 },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 80 },
  { title: '启用', key: 'enabled', width: 80 },
  { title: '操作', key: 'action', width: 130 }
]

const editOpen = ref(false)
const form = reactive<Partial<SmartAlertRule>>({})
const autoDispatchBool = ref(true)
const notifyFamilyBool = ref(false)

function eventTypeText(t?: string) {
  return eventTypeOptions.find((o) => o.value === t)?.label || legacyEventTypeText[t || ''] || t || '-'
}
function opText(op?: string) {
  return ({ GT: '>', GE: '≥', LT: '<', LE: '≤', EQ: '=', RANGE_OUT: '区间外' } as Record<string, string>)[op || ''] || op || ''
}
function levelText(l?: string) {
  return ({ WARNING: '提醒', HIGH: '高', CRITICAL: '危急' } as Record<string, string>)[l || ''] || l || '-'
}
function levelColor(l?: string) {
  return l === 'CRITICAL' ? 'red' : l === 'HIGH' ? 'orange' : 'blue'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<SmartAlertRule> = await getSmartRulePage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      eventType: query.eventType,
      enabled: query.enabled
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}
function onSearch() {
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}
function onReset() {
  query.eventType = undefined
  query.enabled = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}
function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function openCreate() {
  Object.assign(form, {
    id: undefined, ruleCode: '', ruleName: '', eventType: 'FALL', operator: 'PRESENT',
    metricKey: '', threshold: undefined, threshold2: undefined, durationSec: undefined,
    level: 'HIGH', disabilityLevelScope: '', careLevelScope: '', deviceType: '', priority: 100, remark: ''
  })
  autoDispatchBool.value = true
  notifyFamilyBool.value = false
  editOpen.value = true
}
function openEdit(record: SmartAlertRule) {
  Object.assign(form, record)
  autoDispatchBool.value = record.autoDispatch !== 0
  notifyFamilyBool.value = record.notifyFamily === 1
  editOpen.value = true
}

async function submit() {
  if (!form.ruleCode || !form.ruleName || !form.eventType) {
    message.error('请填写编码、名称与事件类型')
    return
  }
  if (saving.value) return
  saving.value = true
  try {
    const payload: Partial<SmartAlertRule> = {
      ...form,
      autoDispatch: autoDispatchBool.value ? 1 : 0,
      notifyFamily: notifyFamilyBool.value ? 1 : 0,
      enabled: form.enabled == null ? 1 : form.enabled
    }
    if (form.id) {
      await updateSmartRule(form.id, payload)
    } else {
      await createSmartRule(payload)
    }
    message.success('已保存')
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function toggle(record: SmartAlertRule, checked: boolean) {
  await setSmartRuleEnabled(record.id, checked)
  fetchData()
}

function remove(record: SmartAlertRule) {
  Modal.confirm({
    title: '确认删除该规则？',
    onOk: async () => {
      await deleteSmartRule(record.id)
      message.success('已删除')
      fetchData()
    }
  })
}

fetchData()
</script>
