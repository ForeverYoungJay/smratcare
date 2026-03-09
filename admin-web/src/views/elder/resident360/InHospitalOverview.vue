<template>
  <PageContainer title="在院服务总览" subTitle="护理/膳食/医疗/活动/费用一体化视图">
    <template #extra>
      <a-space wrap>
        <a-select
          v-model:value="residentId"
          style="width: 220px"
          placeholder="选择长者"
          show-search
          :filter-option="false"
          :options="residentOptions"
          :loading="residentLoading"
          @search="searchResidentOptions"
          @focus="() => !residentOptions.length && searchResidentOptions('')"
          @change="onResidentChange"
        />
        <a-switch v-model:checked="dutyMode" checked-children="值班模式" un-checked-children="普通模式" />
        <a-switch v-model:checked="showWarningOnly" checked-children="仅预警" un-checked-children="全部" />
        <a-switch v-model:checked="autoRefresh" checked-children="自动刷新" un-checked-children="手动刷新" />
        <a-tag v-if="dutyMode" color="red">值班中：30秒自动刷新 + 仅预警</a-tag>
        <a-tag v-if="lastLoadedAt" color="blue">更新于 {{ lastLoadedAt }}</a-tag>
        <a-button @click="loadModules">立即刷新</a-button>
        <a-button @click="copyShareLink">复制分享链接</a-button>
      </a-space>
    </template>
    <template #stats>
      <a-row :gutter="12">
        <a-col :xs="8" :sm="8" :md="8"><a-statistic title="卡片总数" :value="baseModules.length" /></a-col>
        <a-col :xs="8" :sm="8" :md="8"><a-statistic title="预警卡片" :value="alertCardCount" /></a-col>
        <a-col :xs="8" :sm="8" :md="8"><a-statistic title="预警总量" :value="alertTotalCount" /></a-col>
      </a-row>
    </template>
    <a-alert
      v-if="lifecycleContext.active"
      type="info"
      show-icon
      style="margin-bottom: 12px"
      :message="lifecycleContext.message"
    >
      <template #description>
        <a-space wrap>
          <a-tag color="blue">场景：入住状态变更联动</a-tag>
          <a-button size="small" type="link" @click="go('/elder/status-change')">返回状态变更中心</a-button>
          <a-button size="small" type="link" @click="go('/oa/work-execution/task?source=lifecycle&scene=status-change&quick=overdue')">查看超时任务</a-button>
        </a-space>
      </template>
    </a-alert>
    <a-card class="card-elevated linkage-card" :bordered="false">
      <div class="linkage-head">
        <div class="linkage-title">协同联动</div>
        <div class="linkage-sub-title">
          {{ selectedResidentName ? `已选择：${selectedResidentName}` : '未选择长者，默认展示在院首位长者' }}
        </div>
      </div>
      <a-space wrap>
        <a-button type="primary" @click="openResidentProfile">长者档案</a-button>
        <a-button @click="go('/elder/assessment/ability/archive')">评估档案</a-button>
        <a-button @click="go('/elder/contracts-invoices')">合同与票据</a-button>
        <a-button @click="go('/elder/admission-processing')">入住办理</a-button>
        <a-button @click="go('/elder/status-change')">入住状态变更</a-button>
        <a-button @click="go('/finance/bills/in-resident')">账单中心</a-button>
      </a-space>
    </a-card>
    <LifecycleStageBar
      title="长者履约阶段"
      :subject="residentLifecycleSubject"
      :stage="residentLifecycleStage"
      :generated-at="residentLifecycleGeneratedAt"
      :hint="residentLifecycleHint"
      style="margin-bottom: 12px"
    />
    <StatefulBlock :loading="loading" :error="errorMessage" :empty="!modules.length" empty-text="暂无在院服务数据" @retry="loadModules">
      <a-row :gutter="16">
        <a-col v-for="item in modules" :key="item.title" :xs="24" :sm="24" :lg="12" :xl="12" style="margin-bottom: 12px">
          <a-card :title="item.title" :class="['card-elevated', { 'alert-card': item.alertCount > 0 }]" :bordered="false">
            <template #extra>
              <a-space size="small">
                <a-tag :color="item.alertCount > 0 ? 'red' : item.tagColor">{{ item.tag }}</a-tag>
                <a-badge v-if="item.dangerCount > 0" status="error" :text="`严重 ${item.dangerCount}`" />
                <a-badge v-if="item.warningCount > 0" status="warning" :text="`一般 ${item.warningCount}`" />
              </a-space>
            </template>
            <div class="desc">{{ item.desc }}</div>
            <div
              v-for="line in visibleLines(item)"
              :key="`${item.key}-${line}`"
              :class="['line-item', lineTypeClass(line), { clickable: !!resolveLinePath(item.key, line) }]"
              @click="onLineClick(item.key, line)"
            >
              {{ line }}
            </div>
            <div v-if="item.lines.length > 2" class="expand-link" @click="toggleExpand(item.key)">
              {{ expandedCardKeys.includes(item.key) ? '收起详情' : `展开详情（+${item.lines.length - 2}）` }}
            </div>
            <a-space wrap style="margin-top: 12px">
              <a-button v-for="action in item.actions" :key="action.label" :type="action.primary ? 'primary' : 'default'" @click="go(action.path)">
                {{ action.label }}
              </a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../../components/PageContainer.vue'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import LifecycleStageBar from '../../../components/LifecycleStageBar.vue'
import { getResidentOverview } from '../../../api/medicalCare'
import { getContractLinkageByElder } from '../../../api/marketing'
import { useElderOptions } from '../../../composables/useElderOptions'
import { useLiveSyncRefresh } from '../../../composables/useLiveSyncRefresh'
import { copyText } from '../../../utils/clipboard'
import { lifecycleStageHint, normalizeLifecycleStage } from '../../../utils/lifecycleStage'
import type { ContractLinkageSummary, MedicalResidentOverview } from '../../../types'

const router = useRouter()
const route = useRoute()
const residentId = ref('')
const loading = ref(false)
const errorMessage = ref('')
const overview = ref<MedicalResidentOverview>()
const contractLinkage = ref<ContractLinkageSummary>()
const expandedCardKeys = ref<string[]>([])
const showWarningOnly = ref(false)
const autoRefresh = ref(true)
const dutyMode = ref(false)
const lastLoadedAt = ref('')
const { elderOptions: residentOptionPool, elderLoading: residentLoading, searchElders, ensureSelectedElder } = useElderOptions({
  pageSize: 200,
  preloadSize: 600,
  inHospitalOnly: true
})
const residentOptions = computed(() =>
  residentOptionPool.value.map((item) => ({
    label: item.label,
    value: String(item.value)
  }))
)
const selectedResidentOption = computed(() =>
  residentOptions.value.find((item) => item.value === String(residentId.value || '').trim())
)
const selectedResidentName = computed(() => {
  const option = selectedResidentOption.value
  if (!option) return ''
  return String(option.label || '').replace(/\s*\([^)]*\)\s*$/, '').trim()
})
const lifecycleContext = computed(() => {
  const source = String(route.query.source || '').trim().toLowerCase()
  const scene = String(route.query.scene || '').trim().toLowerCase()
  const active = source === 'lifecycle' || scene === 'status-change'
  return {
    active,
    message: active ? '当前总览由状态变更中心联动进入，可优先处理跨部门预警事项。' : ''
  }
})
let refreshTimer: number | null = null

const cardKeyOrder = [
  'status-event',
  'risk-level',
  'care-execution',
  'handover-communication',
  'admission-contract',
  'finance-account',
  'medical-summary',
  'service-package',
  'dining-order',
  'activity-album',
  'family-feedback',
  'docs-attachments'
]
const keyPriorityMap = cardKeyOrder.reduce<Record<string, number>>((acc, key, idx) => {
  acc[key] = idx
  return acc
}, {})

const baseModules = computed(() =>
  [...(overview.value?.cards || [])]
    .sort((a, b) => (keyPriorityMap[a.key] ?? 999) - (keyPriorityMap[b.key] ?? 999))
    .map((card) => {
      const lines = card.lines || []
      const dangerCount = card.dangerCount ?? calcSeverityCount(lines, 'danger')
      const warningCount = card.warningCount ?? calcSeverityCount(lines, 'warning')
      return {
        key: card.key,
        title: card.title.replace(/^卡片\d+：/, ''),
        desc: card.description || lines[0] || '暂无说明',
        lines,
        tag: card.tag,
        tagColor: card.tagColor || 'blue',
        actions: card.actions || [],
        dangerCount,
        warningCount,
        alertCount: card.alertCount ?? dangerCount + warningCount
      }
    })
)
const modules = computed(() => {
  if (!effectiveShowWarningOnly.value) return baseModules.value
  return [...baseModules.value]
    .filter((item) => item.alertCount > 0)
    .sort((a, b) => {
      if (b.dangerCount !== a.dangerCount) return b.dangerCount - a.dangerCount
      if (b.warningCount !== a.warningCount) return b.warningCount - a.warningCount
      if (b.alertCount !== a.alertCount) return b.alertCount - a.alertCount
      return (keyPriorityMap[a.key] ?? 999) - (keyPriorityMap[b.key] ?? 999)
    })
})
const alertCardCount = computed(() => baseModules.value.filter((item) => item.alertCount > 0).length)
const alertTotalCount = computed(() => baseModules.value.reduce((sum, item) => sum + item.alertCount, 0))
const effectiveShowWarningOnly = computed(() => dutyMode.value || showWarningOnly.value)
const effectiveAutoRefresh = computed(() => dutyMode.value || autoRefresh.value)
const refreshIntervalMs = computed(() => (dutyMode.value ? 30000 : 60000))
const residentLifecycleStage = computed(() =>
  normalizeLifecycleStage(contractLinkage.value?.flowStage, contractLinkage.value?.contractStatus)
)
const residentLifecycleSubject = computed(() => {
  if (!residentId.value) return '未选择长者'
  const residentLabel = selectedResidentName.value || `长者ID ${residentId.value}`
  const contractNo = contractLinkage.value?.contractNo || '-'
  return `${residentLabel} / 合同 ${contractNo}`
})
const residentLifecycleHint = computed(() => {
  if (!contractLinkage.value) {
    return '未检索到合同联动信息，建议先在合同与票据确认签约状态。'
  }
  const tips = contractLinkage.value.archiveRuleTips || []
  if (tips.length > 0) {
    return tips[0]
  }
  return lifecycleStageHint(residentLifecycleStage.value)
})
const residentLifecycleGeneratedAt = computed(() =>
  String(contractLinkage.value?.generatedAt || overview.value?.generatedAt || '').trim()
)

function go(path: string) {
  const targetPath = appendResidentId(path)
  forceScrollTop()
  router.push(targetPath)
}

function appendResidentId(path: string) {
  const currentResidentId = String(residentId.value || '').trim()
  const currentResidentName = selectedResidentName.value
  let nextPath = path
  if (currentResidentId && !/([?&])(residentId|elderId)=/.test(nextPath)) {
    nextPath = nextPath.includes('?')
      ? `${nextPath}&residentId=${encodeURIComponent(currentResidentId)}`
      : `${nextPath}?residentId=${encodeURIComponent(currentResidentId)}`
  }
  if (currentResidentName && !/([?&])elderName=/.test(nextPath)) {
    nextPath = nextPath.includes('?')
      ? `${nextPath}&elderName=${encodeURIComponent(currentResidentName)}`
      : `${nextPath}?elderName=${encodeURIComponent(currentResidentName)}`
  }
  return nextPath
}

function openResidentProfile() {
  const currentResidentId = String(residentId.value || '').trim()
  if (!currentResidentId) {
    message.warning('请先选择长者')
    return
  }
  forceScrollTop()
  router.push(`/elder/detail/${encodeURIComponent(currentResidentId)}`)
}

function forceScrollTop() {
  window.scrollTo({ top: 0, left: 0, behavior: 'auto' })
  const content = document.querySelector('.app-content') as HTMLElement | null
  if (content) content.scrollTo({ top: 0, left: 0, behavior: 'auto' })
}

function lineTypeClass(line: string) {
  if (/(待处理|未闭环|超时|欠费|缺失|异常|红色|预警)/.test(line)) return 'line-danger'
  if (/(提醒|复评|建议|倒计时)/.test(line)) return 'line-warning'
  return 'line-normal'
}

function parseLineCount(line: string) {
  const nums = line.match(/\d+/g)
  if (!nums) return 1
  return nums.map((n) => Number(n)).reduce((sum, item) => sum + item, 0)
}

function calcSeverityCount(lines: string[], severity: 'danger' | 'warning') {
  return lines.reduce((sum, line) => {
    const isDanger = /(待处理|未闭环|超时|欠费|缺失|异常|红色|预警)/.test(line)
    const isWarning = /(提醒|复评|建议|倒计时)/.test(line)
    if (severity === 'danger' && isDanger) return sum + parseLineCount(line)
    if (severity === 'warning' && isWarning && !isDanger) return sum + parseLineCount(line)
    return sum
  }, 0)
}

function visibleLines(item: { key: string; lines: string[] }) {
  if (expandedCardKeys.value.includes(item.key)) return item.lines
  return item.lines.slice(0, 2)
}

function toggleExpand(key: string) {
  if (expandedCardKeys.value.includes(key)) {
    expandedCardKeys.value = expandedCardKeys.value.filter((k) => k !== key)
    return
  }
  expandedCardKeys.value = [...expandedCardKeys.value, key]
}

function resolveLinePath(cardKey: string, line: string) {
  if (!residentId.value) return ''
  if (cardKey === 'status-event' && line.startsWith('最近一次事件')) {
    return `/elder/change-log?residentId=${residentId.value}&filter=latest`
  }
  if (cardKey === 'status-event' && line.startsWith('本月事件')) {
    return `/elder/change-log?residentId=${residentId.value}&filter=this_month`
  }
  if (cardKey === 'status-event' && line.includes('外出超时')) {
    return `/elder/change-log?residentId=${residentId.value}&filter=overdue`
  }
  if (cardKey === 'docs-attachments' && line.includes('缺失提醒')) {
    return `/elder/contracts-invoices?residentId=${residentId.value}&tab=attachments`
  }
  if (cardKey === 'admission-contract' && line.includes('到期倒计时')) {
    return `/elder/contracts-invoices?residentId=${residentId.value}`
  }
  return ''
}

function onLineClick(cardKey: string, line: string) {
  const path = resolveLinePath(cardKey, line)
  if (!path) return
  go(path)
}

async function resolveResidentId() {
  const fromRoute = String(route.query.residentId || route.query.elderId || '').trim()
  const fromRouteName = String(route.query.elderName || '').trim()
  if (fromRoute) {
    residentId.value = fromRoute
    if (/^\d+$/.test(fromRoute)) ensureSelectedElder(Number(fromRoute), fromRouteName || undefined)
    return
  }
  if (residentOptions.value.length > 0) {
    residentId.value = residentOptions.value[0].value
    return
  }
  await searchResidentOptions('')
  residentId.value = residentOptions.value[0]?.value || ''
}

async function searchResidentOptions(keyword = '') {
  await searchElders(keyword)
}

function syncResidentToRoute() {
  const nextQuery = buildResidentRouteQuery()
  if (hasSameRouteQuery(nextQuery)) return
  router.replace({
    path: route.path,
    query: nextQuery
  })
}

function buildResidentRouteQuery() {
  const nextResident = residentId.value.trim()
  const nextResidentName = selectedResidentName.value
  const nextWarning = showWarningOnly.value ? '1' : ''
  const nextDuty = dutyMode.value ? '1' : ''
  const nextAuto = autoRefresh.value ? '1' : '0'
  const nextQuery: Record<string, any> = { ...route.query }
  if (nextResident) nextQuery.residentId = nextResident
  else delete nextQuery.residentId
  delete nextQuery.elderId
  if (nextResidentName) nextQuery.elderName = nextResidentName
  else delete nextQuery.elderName
  if (nextWarning) nextQuery.warning = nextWarning
  else delete nextQuery.warning
  if (nextDuty) nextQuery.duty = nextDuty
  else delete nextQuery.duty
  nextQuery.auto = nextAuto
  return nextQuery
}

function flattenRouteQuery(source: Record<string, unknown>) {
  return Object.entries(source || {}).reduce<Record<string, string>>((acc, [key, value]) => {
    const text = Array.isArray(value) ? String(value[0] || '').trim() : String(value || '').trim()
    if (!text) return acc
    acc[key] = text
    return acc
  }, {})
}

function hasSameRouteQuery(nextQuery: Record<string, any>) {
  const currentQuery = flattenRouteQuery(route.query as Record<string, unknown>)
  const nextFlatten = flattenRouteQuery(nextQuery)
  const currentKeys = Object.keys(currentQuery)
  const nextKeys = Object.keys(nextFlatten)
  if (currentKeys.length !== nextKeys.length) return false
  return nextKeys.every((key) => currentQuery[key] === nextFlatten[key])
}

async function copyShareLink() {
  const href = router.resolve({ path: route.path, query: buildResidentRouteQuery() }).href
  const fullUrl = /^https?:\/\//i.test(href) ? href : `${window.location.origin}${href}`
  const copied = await copyText(fullUrl)
  if (copied) {
    message.success('分享链接已复制')
    return
  }
  message.warning('复制失败，请手动复制地址栏链接')
}

function onResidentChange() {
  syncResidentToRoute()
  loadModules()
}

function setupAutoRefresh() {
  clearAutoRefresh()
  if (!effectiveAutoRefresh.value) return
  refreshTimer = window.setInterval(() => {
    if (!loading.value) loadModules()
  }, refreshIntervalMs.value)
}

function clearAutoRefresh() {
  if (refreshTimer !== null) {
    window.clearInterval(refreshTimer)
    refreshTimer = null
  }
}

async function loadModules() {
  loading.value = true
  errorMessage.value = ''
  try {
    await resolveResidentId()
    if (!residentId.value.trim()) {
      overview.value = undefined
      contractLinkage.value = undefined
      errorMessage.value = "暂无可用长者，请先创建长者档案"
      return
    }
    const [overviewData, contractData] = await Promise.all([
      getResidentOverview(residentId.value),
      safeContractLinkageByElder(residentId.value)
    ])
    overview.value = overviewData
    contractLinkage.value = contractData
    const generatedAt = String(overview.value?.generatedAt || '').trim()
    lastLoadedAt.value = generatedAt ? new Date(generatedAt).toLocaleString() : new Date().toLocaleString()
    expandedCardKeys.value = []
  } catch (error: any) {
    errorMessage.value = error?.message || '加载在院服务总览失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

async function safeContractLinkageByElder(elderId: string) {
  try {
    return await getContractLinkageByElder(elderId)
  } catch {
    return undefined
  }
}

onMounted(async () => {
  try {
    dutyMode.value = String(route.query.duty || '').trim() === '1'
    showWarningOnly.value = String(route.query.warning || '').trim() === '1'
    autoRefresh.value = String(route.query.auto || '').trim() !== '0'
    const routeResident = String(route.query.residentId || route.query.elderId || '').trim()
    if (routeResident) {
      residentId.value = routeResident
      const routeResidentName = String(route.query.elderName || '').trim()
      if (/^\d+$/.test(routeResident)) {
        ensureSelectedElder(Number(routeResident), routeResidentName || undefined)
      }
    }
    await searchResidentOptions('')
    if (!residentId.value && residentOptions.value.length > 0) {
      residentId.value = residentOptions.value[0].value
      syncResidentToRoute()
    }
  } catch {
    // ignore resident option init error
  }
  await loadModules()
  syncResidentToRoute()
  setupAutoRefresh()
})

watch(autoRefresh, () => {
  syncResidentToRoute()
  setupAutoRefresh()
})
watch(showWarningOnly, () => syncResidentToRoute())
watch(dutyMode, (enabled) => {
  if (enabled) {
    showWarningOnly.value = true
    autoRefresh.value = true
  }
  syncResidentToRoute()
  setupAutoRefresh()
})
watch(
  () => String(route.query.residentId || route.query.elderId || '').trim(),
  (nextResident) => {
    if (!nextResident || nextResident === residentId.value) return
    residentId.value = nextResident
    const nextResidentName = String(route.query.elderName || '').trim()
    if (/^\d+$/.test(nextResident)) {
      ensureSelectedElder(Number(nextResident), nextResidentName || undefined)
    }
    loadModules()
  }
)
watch(
  () => String(route.query.warning || '').trim(),
  (nextWarning) => {
    const enabled = nextWarning === '1'
    if (enabled === showWarningOnly.value) return
    showWarningOnly.value = enabled
  }
)
watch(
  () => String(route.query.duty || '').trim(),
  (nextDuty) => {
    const enabled = nextDuty === '1'
    if (enabled === dutyMode.value) return
    dutyMode.value = enabled
  }
)
watch(
  () => String(route.query.auto || '').trim(),
  (nextAuto) => {
    const enabled = nextAuto === '0' ? false : true
    if (enabled === autoRefresh.value) return
    autoRefresh.value = enabled
  }
)
onBeforeUnmount(() => clearAutoRefresh())

useLiveSyncRefresh({
  topics: ['elder', 'lifecycle', 'bed', 'care', 'health', 'dining', 'finance'],
  refresh: () => {
    if (!effectiveAutoRefresh.value || loading.value) return
    loadModules().catch(() => {})
  },
  debounceMs: 1000
})
</script>

<style scoped>
.desc {
  color: #595959;
  line-height: 1.65;
  min-height: 30px;
  font-size: 13px;
}

.linkage-card {
  margin-bottom: 12px;
  border: 1px solid #e6f4ff;
  background: linear-gradient(132deg, #f6fbff 0%, #f0f7ff 45%, #ffffff 100%);
}

.linkage-head {
  margin-bottom: 10px;
}

.linkage-title {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.linkage-sub-title {
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
}

.line-item {
  margin-top: 6px;
  line-height: 1.65;
  font-size: 13px;
  word-break: break-all;
}

.line-normal {
  color: #434343;
}

.line-warning {
  color: #d48806;
}

.line-danger {
  color: #cf1322;
  font-weight: 600;
}

.clickable {
  cursor: pointer;
  text-decoration: underline;
  text-decoration-style: dashed;
  text-underline-offset: 2px;
}

.expand-link {
  margin-top: 8px;
  color: #1677ff;
  font-size: 12px;
  cursor: pointer;
}

:deep(.alert-card .ant-card-head-title) {
  color: #cf1322;
}

:deep(.alert-card .ant-card-body) {
  border: 1px solid #ffa39e;
  border-radius: 8px;
  background: linear-gradient(180deg, #fff2f0 0%, #fff 56%);
}
</style>
