<template>
  <PageContainer title="合同与票据" subTitle="合同附件、票据与评估报告统一归档">
    <a-row :gutter="16" style="margin-bottom: 12px">
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="card-elevated" :bordered="false" size="small">
          <a-statistic title="合同附件" :value="contractAttachmentCount" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="card-elevated" :bordered="false" size="small">
          <a-statistic title="评估报告" :value="assessmentReportCount" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="card-elevated" :bordered="false" size="small">
          <a-statistic title="总资料数" :value="documents.length" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="card-elevated" :bordered="false" size="small">
          <a-statistic title="账单未收金额" :value="formatAmount(linkage?.billOutstandingAmount)" suffix="元" />
        </a-card>
      </a-col>
    </a-row>
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
          <a-button type="link" size="small" @click="go('/elder/status-change')">返回状态变更中心</a-button>
          <a-button type="link" size="small" @click="go('/finance/bills/in-resident?source=lifecycle&scene=status-change')">联动核对账单</a-button>
        </a-space>
      </template>
    </a-alert>

    <a-row :gutter="16">
      <a-col :xs="24" :lg="16">
        <a-card class="card-elevated" :bordered="false" title="资料清单">
          <template #extra>
            <a-space wrap>
              <a-button @click="loadAll">刷新</a-button>
              <a-button @click="downloadBundle" :disabled="!filteredDocuments.length">导出清单</a-button>
              <a-button @click="copyQueryLink">复制查询链接</a-button>
              <a-button type="primary" @click="goContractSigning">{{ isHistoricalImport ? '去完善历史资料' : '去上传附件' }}</a-button>
            </a-space>
          </template>

          <a-form layout="inline" class="search-bar" :model="selector">
            <a-form-item label="长者">
              <ElderNameAutocomplete
                v-model:value="selectorInput"
                width="220px"
                placeholder="请输入长者姓名/拼音首字母"
                @select="onSelectorPick"
              />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="loadBySelectedElder">加载资料</a-button>
            </a-form-item>
          </a-form>

          <a-form layout="inline" class="search-bar" :model="filters">
            <a-form-item label="资料类型">
              <a-select v-model:value="filters.kind" style="width: 140px">
                <a-select-option value="ALL">全部</a-select-option>
                <a-select-option value="ATTACHMENT">合同附件</a-select-option>
                <a-select-option value="ASSESSMENT">评估报告</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="关键字">
              <a-input v-model:value="filters.keyword" allow-clear placeholder="文件名/类型" style="width: 220px" />
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="applyFilters">搜索</a-button>
                <a-button @click="resetFilters">清空</a-button>
              </a-space>
            </a-form-item>
          </a-form>

          <StatefulBlock :loading="loading" :error="errorMessage" :empty="!filteredDocuments.length" empty-text="暂无合同附件或评估报告" @retry="loadAll">
            <a-table :columns="columns" :data-source="filteredDocuments" row-key="id" :pagination="false" :scroll="{ x: 980 }">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'kind'">
                  <a-tag :color="record.kind === 'ASSESSMENT' ? 'purple' : 'blue'">
                    {{ record.kind === 'ASSESSMENT' ? '评估报告' : '合同附件' }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'type'">
                  <a-tag>{{ record.type }}</a-tag>
                </template>
                <template v-else-if="column.key === 'name'">
                  <a v-if="record.url" :href="record.url" target="_blank" rel="noopener noreferrer">{{ record.name }}</a>
                  <a v-else-if="record.route" @click.prevent="go(record.route)">{{ record.name }}</a>
                  <span v-else>{{ record.name }}</span>
                </template>
              </template>
            </a-table>
          </StatefulBlock>

          <a-alert
            v-if="!loading && !errorMessage && !documents.length"
            type="warning"
            show-icon
            style="margin-top: 12px"
            message="未检索到资料"
            :description="isHistoricalImport ? '请先在老人档案基础信息中上传历史合同附件；入住评估完成后，评估报告会自动在此展示。' : '请先上传合同附件；入住评估完成后，评估报告会自动在此展示。'"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="8">
        <LifecycleStageBar
          title="合同履约阶段"
          :subject="lifecycleSubject"
          :stage="resolvedLifecycleStage"
          :generated-at="linkage?.generatedAt"
          :hint="lifecycleHint"
          style="margin-bottom: 16px"
        />
        <a-card title="合同结构化信息" class="card-elevated" :bordered="false" style="margin-bottom: 16px">
          <StatefulBlock :loading="loading" :error="errorMessage" @retry="loadAll">
            <a-descriptions :column="1" size="small" bordered>
              <a-descriptions-item label="合同号">{{ linkage?.contractNo || '-' }}</a-descriptions-item>
              <a-descriptions-item label="签约状态">{{ contractStatusText(linkage?.contractStatus) }}</a-descriptions-item>
              <a-descriptions-item label="签约时间">{{ linkage?.contractSignedAt || '-' }}</a-descriptions-item>
              <a-descriptions-item label="合同有效期止">{{ linkage?.contractExpiryDate || '-' }}</a-descriptions-item>
              <a-descriptions-item label="入住日期">{{ linkage?.admissionDate || '-' }}</a-descriptions-item>
              <a-descriptions-item label="押金">{{ formatAmount(linkage?.depositAmount) }} 元</a-descriptions-item>
            </a-descriptions>
            <div class="archive-progress-block">
              <div class="archive-progress-head">
                <span>归档完整度（规则引擎）</span>
                <a-space :size="8">
                  <a-tag :color="archiveLevelColor">{{ archiveLevelText }}</a-tag>
                  <span>{{ archiveCompleteScore }}%</span>
                </a-space>
              </div>
              <a-progress :percent="archiveCompleteScore" size="small" :show-info="false" :stroke-color="archiveProgressColor" />
              <a-space direction="vertical" size="small" style="width: 100%; margin-top: 8px">
                <a-tag
                  :color="archiveItemStateTagColor(hasContractAttachment)"
                  class="archive-item-action"
                  @click="goAttachmentUpload('CONTRACT')"
                >
                  合同附件 {{ hasContractAttachment ? '已上传' : '缺失' }}
                </a-tag>
                <a-tag :color="archiveItemStateTagColor(hasInvoiceAttachment)">
                  发票/收据 {{ hasInvoiceAttachment ? '已归档' : '缺失' }}
                </a-tag>
                <a-tag
                  :color="archiveItemStateTagColor(hasIdAttachment)"
                  class="archive-item-action"
                  @click="goAttachmentUpload('HOUSEHOLD')"
                >
                  身份证/证件 {{ hasIdAttachment ? '已归档' : '缺失' }}
                </a-tag>
                <a-tag :color="archiveItemStateTagColor(hasAssessmentReport)">
                  评估报告 {{ hasAssessmentReport ? '已归档' : '待生成' }}
                </a-tag>
              </a-space>
              <a-alert
                v-if="missingArchiveItems.length"
                type="warning"
                show-icon
                style="margin-top: 10px"
                :message="`待补齐：${missingArchiveItemsLabel}`"
              />
              <div v-if="linkage?.generatedAt" class="archive-generated-time">
                规则评估时间：{{ formatGeneratedAt(linkage?.generatedAt) }}
              </div>
            </div>
            <div class="archive-rule-block">
              <div class="archive-rule-head">
                <span>{{ archiveRuleTitleText }}</span>
                <a-tag color="geekblue">版本 {{ archiveRuleVersionText }}</a-tag>
              </div>
              <div class="archive-rule-desc">{{ archiveRuleDescriptionText }}</div>
              <div v-if="archiveRuleRequiredItemsDisplay.length" class="archive-rule-required">
                必需项：{{ archiveRuleRequiredItemsDisplay.join('、') }}
              </div>
              <a-space direction="vertical" size="small" style="width: 100%; margin-top: 8px">
                <a-tag v-for="item in archiveRuleTipsDisplay" :key="item">{{ item }}</a-tag>
              </a-space>
            </div>
          </StatefulBlock>
        </a-card>

        <a-card title="快捷入口" class="card-elevated" :bordered="false">
          <a-space direction="vertical" style="width: 100%">
            <a-button block @click="goContractManagement">合同到期管理</a-button>
            <a-button block @click="goAssessmentArchive">评估档案</a-button>
            <a-button block @click="go('/finance/resident-bill-payment')">费用账单</a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../../components/PageContainer.vue'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import LifecycleStageBar from '../../../components/LifecycleStageBar.vue'
import ElderNameAutocomplete from '../../../components/ElderNameAutocomplete.vue'
import { useElderOptions } from '../../../composables/useElderOptions'
import { useLiveSyncRefresh } from '../../../composables/useLiveSyncRefresh'
import { copyText } from '../../../utils/clipboard'
import { lifecycleStageHint, normalizeLifecycleStage } from '../../../utils/lifecycleStage'
import { getElderDetail } from '../../../api/elder'
import {
  getContractArchiveRule,
  getContractAssessmentOverview,
  getContractLinkageByContract,
  getContractLinkageByElder,
  getContractLinkageByLead
} from '../../../api/marketing'
import type {
  ContractArchiveRuleInfo,
  ContractAssessmentOverview,
  ContractAssessmentReportItem,
  ContractAttachmentItem,
  ContractLinkageSummary,
  ElderItem
} from '../../../types'

type DocumentKind = 'ATTACHMENT' | 'ASSESSMENT'

interface DocumentItem {
  id: string
  kind: DocumentKind
  type: string
  name: string
  url?: string
  route?: string
  size: string
  time: string
}

const ARCHIVE_MISSING_LABEL_MAP: Record<string, string> = {
  CONTRACT_ATTACHMENT: '合同附件',
  INVOICE_ATTACHMENT: '发票/收据',
  ID_ATTACHMENT: '身份证/证件',
  ASSESSMENT_REPORT: '评估报告'
}

const router = useRouter()
const route = useRoute()
const lifecycleContext = computed(() => {
  const source = String(route.query.source || '').trim().toLowerCase()
  const scene = String(route.query.scene || '').trim().toLowerCase()
  const active = source === 'lifecycle' || scene === 'status-change'
  return {
    active,
    message: active ? '当前由状态变更联动进入，建议优先核验合同资料完整度并同步账单。' : ''
  }
})
const loading = ref(false)
const errorMessage = ref('')
const linkage = ref<ContractLinkageSummary>()
const elderProfile = ref<ElderItem | null>(null)
const assessmentOverview = ref<ContractAssessmentOverview>()
const archiveRuleInfo = ref<ContractArchiveRuleInfo>()
const docRouteSignature = ref('')
const skipNextDocRouteWatch = ref(false)
const DOC_ROUTE_KEYS = ['docElderId', 'docKind', 'docKeyword'] as const
const DOC_ROUTE_OBSERVE_KEYS = ['elderId', 'residentId', 'contractId', 'leadId'] as const
const { elderOptions, elderLoading, searchElders, ensureSelectedElder, findElderName } = useElderOptions({
  pageSize: 120,
  inHospitalOnly: false,
  signedOnly: true
})
const selector = reactive({
  elderId: undefined as string | undefined
})
const selectorInput = ref('')
const filters = reactive({
  kind: 'ALL',
  keyword: ''
})
const searchSnapshot = reactive({
  kind: 'ALL',
  keyword: ''
})

const columns = [
  { title: '来源', dataIndex: 'kind', key: 'kind', width: 110 },
  { title: '资料类型', dataIndex: 'type', key: 'type', width: 140 },
  { title: '文件名', dataIndex: 'name', key: 'name' },
  { title: '大小', dataIndex: 'size', key: 'size', width: 110 },
  { title: '时间', dataIndex: 'time', key: 'time', width: 150 }
]

const documents = computed<DocumentItem[]>(() => {
  const attachments = (linkage.value?.attachments || []).map((item) => ({
    id: `att-${item.id}`,
    kind: 'ATTACHMENT' as DocumentKind,
    type: normalizeAttachmentType(item),
    name: item.fileName || '未命名附件',
    url: item.fileUrl,
    route: '',
    size: formatFileSize(item.fileSize),
    time: item.createTime || '-'
  }))
  const historicalContract = elderProfile.value?.historicalContractFileUrl
    ? [{
        id: `elder-contract-${String(elderProfile.value?.id || '')}`,
        kind: 'ATTACHMENT' as DocumentKind,
        type: '历史合同附件',
        name: fileNameFromUrl(elderProfile.value.historicalContractFileUrl) || '历史合同附件',
        url: elderProfile.value.historicalContractFileUrl,
        route: '',
        size: '-',
        time: elderProfile.value?.admissionDate || '-'
      }]
    : []
  const reports = flattenReports(assessmentOverview.value).map((report) => ({
    id: `report-${report.recordId}`,
    kind: 'ASSESSMENT' as DocumentKind,
    type: mapAssessmentType(report.assessmentType),
    name: report.reportFileName || buildAssessmentReportName(report),
    url: report.reportFileUrl || '',
    route: report.reportFileUrl
      ? ''
      : `/elder/assessment/ability/archive?elderId=${encodeURIComponent(String(linkage.value?.elderId || ''))}`,
    size: '-',
    time: report.assessmentDate || '-'
  }))
  return [...historicalContract, ...attachments, ...reports]
})

const filteredDocuments = computed(() => {
  const keyword = searchSnapshot.keyword.trim().toLowerCase()
  return documents.value.filter((item) => {
    const kindMatch = searchSnapshot.kind === 'ALL' ? true : item.kind === searchSnapshot.kind
    const keywordMatch = !keyword
      ? true
      : item.name.toLowerCase().includes(keyword) || item.type.toLowerCase().includes(keyword)
    return kindMatch && keywordMatch
  })
})

const contractAttachmentCount = computed(() => {
  const crmCount = Number(linkage.value?.attachmentCount || 0)
  return crmCount + (elderProfile.value?.historicalContractFileUrl ? 1 : 0)
})
const isHistoricalImport = computed(() => elderProfile.value?.sourceType === 'HISTORICAL_IMPORT')
const assessmentReportCount = computed(() => flattenReports(assessmentOverview.value).length)
const resolvedLifecycleStage = computed(() =>
  normalizeLifecycleStage(linkage.value?.flowStage, linkage.value?.contractStatus)
)
const resolvedLinkageElderName = computed(() => {
  const rawName = String(linkage.value?.elderName || '').trim()
  const elderId = String(linkage.value?.elderId || '').trim()
  const looksLikeId = /^\d+$/.test(rawName)
  if (rawName && !looksLikeId) return rawName
  if (elderId) {
    const fromCache = String(findElderName(elderId) || '').trim()
    if (fromCache) return fromCache
  }
  return rawName || '-'
})
const lifecycleSubject = computed(() => {
  if (!linkage.value) return '未匹配到合同信息'
  return `合同 ${linkage.value.contractNo || '-'} / 长者 ${resolvedLinkageElderName.value}`
})
const lifecycleHint = computed(() => {
  if (missingArchiveItems.value.length > 0) {
    return `当前仍有资料缺口：${missingArchiveItemsLabel.value}，建议先补齐后再推进签署。`
  }
  return lifecycleStageHint(resolvedLifecycleStage.value)
})

function contractStatusText(status?: string) {
  const value = String(status || '').trim().toUpperCase()
  if (!value) return '-'
  if (value === 'DRAFT') return '草稿'
  if (value === 'PENDING_APPROVAL') return '待审批'
  if (value === 'APPROVED') return '已审批'
  if (value === 'REJECTED') return '已驳回'
  if (value === 'PENDING_ASSESSMENT') return '待评估'
  if (value === 'PENDING_BED_SELECT') return '待办理入住'
  if (value === 'PENDING_SIGN') return '待签署'
  if (value === 'SIGNED') return '已签署'
  if (value === 'EFFECTIVE') return '已生效'
  if (value === 'VOID') return '已作废'
  return status || '-'
}
const hasContractAttachment = computed(() => {
  if (linkage.value?.hasContractAttachment !== undefined && linkage.value?.hasContractAttachment !== null) {
    return Boolean(linkage.value.hasContractAttachment)
  }
  return contractAttachmentCount.value > 0 || Boolean(elderProfile.value?.historicalContractFileUrl)
})
const hasInvoiceAttachment = computed(() => {
  if (linkage.value?.hasInvoiceAttachment !== undefined && linkage.value?.hasInvoiceAttachment !== null) {
    return Boolean(linkage.value.hasInvoiceAttachment)
  }
  return (linkage.value?.attachments || []).some((item) => normalizeAttachmentType(item) === '收据/发票')
})
const hasIdAttachment = computed(() => {
  if (linkage.value?.hasIdAttachment !== undefined && linkage.value?.hasIdAttachment !== null) {
    return Boolean(linkage.value.hasIdAttachment)
  }
  return (linkage.value?.attachments || []).some((item) => {
    const type = String(item.attachmentType || '').toUpperCase()
    const fileName = String(item.fileName || '').toLowerCase()
    return type.includes('ID') || type.includes('IDENT') || type === 'HOUSEHOLD' || type === 'MEDICAL_INSURANCE'
      || fileName.includes('身份证') || fileName.includes('户口') || fileName.includes('医保')
  })
})
const hasAssessmentReport = computed(() => {
  if (linkage.value?.hasAssessmentReport !== undefined && linkage.value?.hasAssessmentReport !== null) {
    return Boolean(linkage.value.hasAssessmentReport)
  }
  return assessmentReportCount.value > 0
})
const archiveCompleteScore = computed(() => {
  const score = Number(linkage.value?.archiveScore)
  if (Number.isFinite(score)) return Math.max(0, Math.min(100, Math.round(score)))
  let fallbackScore = 0
  if (hasContractAttachment.value) fallbackScore += 35
  if (hasInvoiceAttachment.value) fallbackScore += 20
  if (hasIdAttachment.value) fallbackScore += 20
  if (hasAssessmentReport.value) fallbackScore += 25
  return fallbackScore
})
const archiveLevelText = computed(() => {
  const level = String(linkage.value?.archiveLevel || '').toUpperCase()
  if (level === 'COMPLETE') return '完整'
  if (level === 'HIGH') return '高'
  if (level === 'MEDIUM') return '中'
  if (level === 'LOW') return '低'
  if (archiveCompleteScore.value >= 95) return '完整'
  if (archiveCompleteScore.value >= 80) return '高'
  if (archiveCompleteScore.value >= 50) return '中'
  return '低'
})
const archiveLevelColor = computed(() => {
  if (archiveLevelText.value === '完整') return 'green'
  if (archiveLevelText.value === '高') return 'blue'
  if (archiveLevelText.value === '中') return 'orange'
  return 'red'
})
const archiveProgressColor = computed(() => {
  if (archiveLevelText.value === '完整') return '#16a34a'
  if (archiveLevelText.value === '高') return '#2563eb'
  if (archiveLevelText.value === '中') return '#d97706'
  return '#dc2626'
})
const missingArchiveItems = computed(() => {
  const fromBackend = linkage.value?.missingRequiredAttachmentTypes || []
  if (fromBackend.length) {
    return fromBackend
  }
  const fallback: string[] = []
  if (!hasContractAttachment.value) fallback.push('CONTRACT_ATTACHMENT')
  if (!hasInvoiceAttachment.value) fallback.push('INVOICE_ATTACHMENT')
  if (!hasIdAttachment.value) fallback.push('ID_ATTACHMENT')
  if (!hasAssessmentReport.value && resolvedLifecycleStage.value !== 'PENDING_ASSESSMENT') fallback.push('ASSESSMENT_REPORT')
  return fallback
})
const missingArchiveItemsLabel = computed(() =>
  missingArchiveItems.value.map((item) => ARCHIVE_MISSING_LABEL_MAP[item] || item).join('、')
)
const archiveRuleVersionText = computed(() => {
  const fromLinkage = String(linkage.value?.archiveRuleVersion || '').trim()
  if (fromLinkage) return fromLinkage
  const fromRule = String(archiveRuleInfo.value?.ruleVersion || '').trim()
  return fromRule || '-'
})
const archiveRuleTitleText = computed(() => {
  const title = String(archiveRuleInfo.value?.title || '').trim()
  return title || '合同归档规则说明'
})
const archiveRuleDescriptionText = computed(() => {
  const text = String(archiveRuleInfo.value?.description || '').trim()
  return text || '规则会按合同阶段和业务进度自动判定必需归档项。'
})
const archiveRuleTipsDisplay = computed(() => {
  const linkageTips = (linkage.value?.archiveRuleTips || []).filter((item) => String(item || '').trim())
  if (linkageTips.length) return linkageTips
  return (archiveRuleInfo.value?.stageNotes || []).filter((item) => String(item || '').trim())
})
const archiveRuleRequiredItemsDisplay = computed(() =>
  (archiveRuleInfo.value?.requiredItems || []).filter((item) => String(item || '').trim())
)

function applyFilters() {
  searchSnapshot.kind = filters.kind
  searchSnapshot.keyword = filters.keyword
  syncDocumentQueryToRoute().catch(() => {})
}

function resetFilters() {
  filters.kind = 'ALL'
  filters.keyword = ''
  applyFilters()
}

function firstRouteQueryText(value: unknown) {
  if (Array.isArray(value)) return firstRouteQueryText(value[0])
  if (value == null) return ''
  return String(value).trim()
}

function flattenRouteQuery(source: Record<string, unknown>) {
  return Object.entries(source || {}).reduce<Record<string, string>>((acc, [key, value]) => {
    const text = firstRouteQueryText(value)
    if (!text) return acc
    acc[key] = text
    return acc
  }, {})
}

function buildDocRouteSignature(source: Record<string, unknown>) {
  return [...DOC_ROUTE_KEYS, ...DOC_ROUTE_OBSERVE_KEYS]
    .sort()
    .map((key) => `${key}:${firstRouteQueryText(source[key])}`)
    .join('|')
}

function applyDocumentQueryFromRoute() {
  const elderId = firstRouteQueryText(route.query.docElderId || route.query.elderId || route.query.residentId)
  const routeElderName = firstRouteQueryText(route.query.elderName)
  if (elderId) {
    selector.elderId = elderId
    selectorInput.value = routeElderName || String(findElderName(elderId) || '').trim()
  } else {
    selector.elderId = undefined
    selectorInput.value = ''
  }

  const kind = firstRouteQueryText(route.query.docKind).toUpperCase()
  filters.kind = kind === 'ATTACHMENT' || kind === 'ASSESSMENT' ? kind : 'ALL'
  filters.keyword = firstRouteQueryText(route.query.docKeyword)
  searchSnapshot.kind = filters.kind
  searchSnapshot.keyword = filters.keyword
}

function onSelectorPick(payload: { elderId: string; elderName: string }) {
  const elderId = String(payload?.elderId || '').trim()
  const elderName = String(payload?.elderName || '').trim()
  if (!elderId) return
  selector.elderId = elderId
  if (elderName) {
    selectorInput.value = elderName
  }
  ensureSelectedElder(elderId, elderName || undefined)
  loadBySelectedElder().catch(() => {})
}

function isSignedLinkage(linked?: ContractLinkageSummary) {
  if (!linked) return false
  const status = String(linked.contractStatus || '').trim().toUpperCase()
  const flowStage = String(linked.flowStage || '').trim().toUpperCase()
  if (status.includes('SIGNED') || status.includes('EFFECTIVE')) return true
  if (flowStage === 'SIGNED') return true
  if (status.includes('已签') || status.includes('生效')) return true
  return false
}

function buildDocumentRouteQuery() {
  const nextQuery: Record<string, string> = {}
  Object.entries(flattenRouteQuery(route.query as Record<string, unknown>)).forEach(([key, value]) => {
    if ((DOC_ROUTE_KEYS as readonly string[]).includes(key)) return
    nextQuery[key] = value
  })
  if (selector.elderId) nextQuery.docElderId = String(selector.elderId)
  if (searchSnapshot.kind !== 'ALL') nextQuery.docKind = searchSnapshot.kind
  if (searchSnapshot.keyword.trim()) nextQuery.docKeyword = searchSnapshot.keyword.trim()
  return nextQuery
}

function hasSameDocumentRouteQuery(nextQuery: Record<string, string>) {
  const currentQuery = flattenRouteQuery(route.query as Record<string, unknown>)
  const currentKeys = Object.keys(currentQuery)
  const nextKeys = Object.keys(nextQuery)
  if (currentKeys.length !== nextKeys.length) return false
  return nextKeys.every((key) => currentQuery[key] === nextQuery[key])
}

async function syncDocumentQueryToRoute() {
  const nextQuery = buildDocumentRouteQuery()
  if (hasSameDocumentRouteQuery(nextQuery)) return
  skipNextDocRouteWatch.value = true
  docRouteSignature.value = buildDocRouteSignature(nextQuery)
  await router.replace({ path: route.path, query: nextQuery })
}

async function copyQueryLink() {
  const href = router.resolve({ path: route.path, query: buildDocumentRouteQuery() }).href
  const fullUrl = /^https?:\/\//i.test(href) ? href : `${window.location.origin}${href}`
  const copied = await copyText(fullUrl)
  if (copied) {
    message.success('查询链接已复制')
    return
  }
  message.warning('复制失败，请手动复制地址栏链接')
}

function go(path: string) {
  router.push(path)
}

function goContractSigning() {
  const elderId = String(linkage.value?.elderId || elderProfile.value?.id || '').trim()
  if (isHistoricalImport.value && elderId) {
    router.push(`/elder/detail/${elderId}?tab=base`)
    return
  }
  if (linkage.value?.contractNo) {
    router.push(`/marketing/contract-signing?contractNo=${encodeURIComponent(linkage.value.contractNo)}`)
    return
  }
  router.push('/marketing/contract-signing')
}

function goAttachmentUpload(attachmentType: 'CONTRACT' | 'HOUSEHOLD' | 'MEDICAL_INSURANCE' | 'MEDICAL_RECORD' | 'OTHER') {
  const elderId = String(linkage.value?.elderId || elderProfile.value?.id || '').trim()
  if (isHistoricalImport.value && elderId) {
    router.push(`/elder/detail/${elderId}?tab=base`)
    return
  }
  const contractNo = String(linkage.value?.contractNo || '').trim()
  const elderName = String(resolvedLinkageElderName.value || '').trim()
  const query: Record<string, string> = {
    openAttachment: '1',
    attachmentType
  }
  if (contractNo) query.contractNo = contractNo
  if (!contractNo && elderName) query.elderName = elderName
  if (!contractNo && !elderName && elderId) query.elderId = elderId
  router.push({ path: '/marketing/contract-signing', query })
}

function goContractManagement() {
  const elderId = String(linkage.value?.elderId || elderProfile.value?.id || '').trim()
  if (isHistoricalImport.value && elderId) {
    router.push(`/elder/detail/${elderId}?tab=base`)
    return
  }
  if (linkage.value?.contractNo) {
    router.push(`/marketing/contract-management?contractNo=${encodeURIComponent(linkage.value.contractNo)}`)
    return
  }
  router.push('/marketing/contract-management')
}

function goAssessmentArchive() {
  const elderId = linkage.value?.elderId
  if (elderId) {
    router.push(`/elder/assessment/ability/archive?elderId=${elderId}`)
    return
  }
  router.push('/elder/assessment/ability/archive')
}

function flattenReports(source?: ContractAssessmentOverview) {
  const contracts = source?.contracts || []
  const list: ContractAssessmentReportItem[] = []
  contracts.forEach((item) => {
    if (item.reports?.length) {
      list.push(...item.reports)
    }
  })
  if (source?.unassignedReports?.length) {
    list.push(...source.unassignedReports)
  }
  return list
}

function normalizeAttachmentType(item: ContractAttachmentItem) {
  const type = (item.attachmentType || '').toUpperCase()
  if (type === 'ASSESSMENT_REPORT') return '评估报告'
  if (type === 'INVOICE') return '收据/发票'
  if (type === 'CONTRACT') return '合同'
  if (type === 'MEDICAL_RECORD') return '病历资料'
  if (type === 'MEDICAL_INSURANCE') return '医保复印件'
  if (type === 'HOUSEHOLD') return '户口复印件'
  const fileName = (item.fileName || '').toLowerCase()
  if (fileName.includes('发票') || fileName.includes('invoice') || fileName.includes('receipt')) return '收据/发票'
  if (fileName.includes('合同') || fileName.includes('contract')) return '合同'
  return item.fileType || '附件'
}

function buildAssessmentReportName(report: ContractAssessmentReportItem) {
  const typeLabel = mapAssessmentType(report.assessmentType)
  const score = report.score === undefined || report.score === null ? '' : `-${report.score}分`
  const level = report.levelCode ? `-${report.levelCode}` : ''
  return `${typeLabel}报告${level}${score}`
}

function mapAssessmentType(type?: string) {
  if (type === 'ADMISSION') return '入住评估'
  if (type === 'REGISTRATION') return '登记评估'
  if (type === 'CONTINUOUS') return '持续评估'
  if (type === 'ARCHIVE') return '评估档案'
  if (type === 'COGNITIVE') return '认知评估'
  if (type === 'SELF_CARE') return '自理能力评估'
  if (type === 'OTHER_SCALE') return '其他量表'
  return '评估'
}

function formatFileSize(size?: number) {
  if (!size || size <= 0) return '-'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / (1024 * 1024)).toFixed(1)} MB`
}

function fileNameFromUrl(url?: string) {
  if (!url) return ''
  const text = String(url).split('/').pop() || url
  return decodeURIComponent(text)
}

function formatAmount(value?: number) {
  return Number(value || 0).toFixed(2)
}

function archiveItemStateTagColor(ready: boolean) {
  return ready ? 'green' : 'red'
}

function formatGeneratedAt(value?: string) {
  const text = String(value || '').trim()
  if (!text) return '-'
  const date = new Date(text)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleString()
}

function downloadBundle() {
  const content = filteredDocuments.value
    .map((item) => `${item.kind === 'ASSESSMENT' ? '评估报告' : '合同附件'},${item.type},${item.name},${item.time}`)
    .join('\n')
  const csv = `来源,资料类型,文件名,时间\n${content}`
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = '合同与票据清单.csv'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

async function safeLinkageByElder(elderId: string) {
  try {
    return await getContractLinkageByElder(elderId)
  } catch {
    return undefined
  }
}

async function safeLinkageByContract(contractId: string) {
  try {
    return await getContractLinkageByContract(contractId)
  } catch {
    return undefined
  }
}

async function safeLinkageByLead(leadId: string) {
  try {
    return await getContractLinkageByLead(leadId)
  } catch {
    return undefined
  }
}

async function resolveLinkage() {
  const elderId = selector.elderId ? String(selector.elderId).trim() : String(route.query.elderId || '').trim()
  const residentId = String(route.query.residentId || '').trim()
  const contractId = String(route.query.contractId || '').trim()
  const leadId = String(route.query.leadId || '').trim()

  const byElder = elderId || residentId
  if (byElder) {
    const linked = await safeLinkageByElder(byElder)
    if (isSignedLinkage(linked)) return linked
  }

  const byContract = contractId
  if (byContract) {
    const linked = await safeLinkageByContract(byContract)
    if (isSignedLinkage(linked)) return linked
  }

  const byLead = leadId
  if (byLead) {
    const linked = await safeLinkageByLead(byLead)
    if (isSignedLinkage(linked)) return linked
  }

  return undefined
}

async function loadBySelectedElder() {
  if (!selector.elderId) {
    message.warning('请先选择长者')
    return
  }
  await syncDocumentQueryToRoute().catch(() => {})
  await loadAll()
}

async function loadArchiveRuleInfo() {
  try {
    archiveRuleInfo.value = await getContractArchiveRule()
  } catch {
    archiveRuleInfo.value = undefined
  }
}

async function loadAll() {
  loading.value = true
  errorMessage.value = ''
  try {
    linkage.value = await resolveLinkage()
    elderProfile.value = null
    if (!linkage.value) {
      assessmentOverview.value = undefined
      return
    }
    const linkageElderId = String(linkage.value.elderId || '').trim()
    const linkageElderName = String(linkage.value.elderName || '').trim()
    const likelyName = linkageElderName && !/^\d+$/.test(linkageElderName) ? linkageElderName : undefined
    if (linkageElderId) {
      if (likelyName) {
        ensureSelectedElder(linkageElderId, likelyName)
        if (!selectorInput.value.trim()) {
          selectorInput.value = likelyName
        }
        try {
          elderProfile.value = await getElderDetail(linkageElderId)
        } catch {
          elderProfile.value = null
        }
      } else {
        try {
          const elder = await getElderDetail(linkageElderId)
          elderProfile.value = elder
          const detailName = String(elder?.fullName || '').trim() || undefined
          ensureSelectedElder(linkageElderId, detailName)
          if (detailName && !selectorInput.value.trim()) {
            selectorInput.value = detailName
          }
        } catch {
          elderProfile.value = null
          ensureSelectedElder(linkageElderId)
        }
      }
    }

    const elderId = linkage.value.elderId
    const leadId = linkage.value.leadId
    if (elderId || leadId) {
      assessmentOverview.value = await getContractAssessmentOverview({
        elderId: elderId ? String(elderId) : undefined,
        leadId: leadId ? String(leadId) : undefined
      })
    } else {
      assessmentOverview.value = undefined
    }
  } catch (error: any) {
    linkage.value = undefined
    assessmentOverview.value = undefined
    errorMessage.value = error?.message || '加载合同与票据失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  applyDocumentQueryFromRoute()
  docRouteSignature.value = buildDocRouteSignature(route.query as Record<string, unknown>)
  await loadArchiveRuleInfo()
  await searchElders('')
  if (!selector.elderId) {
    const fromRoute = firstRouteQueryText(route.query.docElderId || route.query.elderId || route.query.residentId)
    const routeElderName = firstRouteQueryText(route.query.elderName)
    if (fromRoute) {
      const inSignedPool = elderOptions.value.some((item) => String(item.value || '').trim() === fromRoute)
      if (inSignedPool) {
        ensureSelectedElder(fromRoute, routeElderName || undefined)
        selector.elderId = fromRoute
        selectorInput.value = routeElderName || String(findElderName(fromRoute) || '').trim()
      } else {
        selector.elderId = undefined
        selectorInput.value = ''
      }
    } else {
      selector.elderId = elderOptions.value[0]?.value
      if (selector.elderId) {
        selectorInput.value = String(findElderName(selector.elderId) || '').trim()
      }
    }
  }
  await syncDocumentQueryToRoute().catch(() => {})
  await loadAll()
})

watch(
  () => selectorInput.value,
  (value) => {
    if (String(value || '').trim()) return
    selector.elderId = undefined
  }
)

watch(
  () => route.query,
  (queryMap) => {
    const nextSignature = buildDocRouteSignature(queryMap as Record<string, unknown>)
    if (skipNextDocRouteWatch.value) {
      skipNextDocRouteWatch.value = false
      docRouteSignature.value = nextSignature
      return
    }
    if (nextSignature === docRouteSignature.value) return
    docRouteSignature.value = nextSignature
    applyDocumentQueryFromRoute()
    loadAll().catch(() => {})
  },
  { deep: true }
)

useLiveSyncRefresh({
  topics: ['elder', 'marketing', 'finance', 'health', 'oa'],
  refresh: () => {
    if (loading.value) return
    if (!selector.elderId && !linkage.value?.elderId) return
    loadAll().catch(() => {})
  },
  debounceMs: 900
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}

.archive-progress-block {
  margin-top: 12px;
  padding: 10px;
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  background: linear-gradient(130deg, rgba(248, 250, 252, 0.9) 0%, rgba(241, 245, 249, 0.8) 100%);
}

.archive-progress-head {
  margin-bottom: 6px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #334155;
  font-size: 12px;
  font-weight: 600;
}

.archive-generated-time {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
}

.archive-rule-block {
  margin-top: 10px;
  padding: 10px;
  border-radius: 10px;
  border: 1px solid rgba(30, 64, 175, 0.18);
  background: linear-gradient(130deg, rgba(239, 246, 255, 0.88) 0%, rgba(248, 250, 252, 0.88) 100%);
}

.archive-rule-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  color: #1e293b;
  font-size: 12px;
  font-weight: 600;
}

.archive-rule-desc {
  margin-top: 6px;
  color: #475569;
  font-size: 12px;
  line-height: 1.6;
}

.archive-rule-required {
  margin-top: 6px;
  color: #334155;
  font-size: 12px;
}

.archive-item-action {
  cursor: pointer;
}
</style>
