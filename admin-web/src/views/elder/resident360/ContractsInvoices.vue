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

    <a-row :gutter="16">
      <a-col :xs="24" :lg="16">
        <a-card class="card-elevated" :bordered="false" title="资料清单">
          <template #extra>
            <a-space wrap>
              <a-button @click="loadAll">刷新</a-button>
              <a-button @click="downloadBundle" :disabled="!filteredDocuments.length">导出清单</a-button>
              <a-button @click="copyQueryLink">复制查询链接</a-button>
              <a-button type="primary" @click="goContractSigning">去上传附件</a-button>
            </a-space>
          </template>

          <a-form layout="inline" class="search-bar" :model="selector">
            <a-form-item label="长者">
              <a-select
                v-model:value="selector.elderId"
                style="width: 220px"
                show-search
                allow-clear
                :filter-option="false"
                :options="elderOptions"
                :loading="elderLoading"
                placeholder="请输入长者姓名/拼音首字母"
                @search="searchElders"
                @focus="() => !elderOptions.length && searchElders('')"
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
            description="请先在营销管理-合同签约上传合同附件；入住评估完成后，评估报告会自动在此展示。"
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
              <a-descriptions-item label="签约状态">{{ linkage?.contractStatus || '-' }}</a-descriptions-item>
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
                <a-tag :color="archiveItemStateTagColor(hasContractAttachment)">
                  合同附件 {{ hasContractAttachment ? '已上传' : '缺失' }}
                </a-tag>
                <a-tag :color="archiveItemStateTagColor(hasInvoiceAttachment)">
                  发票/收据 {{ hasInvoiceAttachment ? '已归档' : '缺失' }}
                </a-tag>
                <a-tag :color="archiveItemStateTagColor(hasIdAttachment)">
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
import { useElderOptions } from '../../../composables/useElderOptions'
import { useLiveSyncRefresh } from '../../../composables/useLiveSyncRefresh'
import { copyText } from '../../../utils/clipboard'
import { lifecycleStageHint, normalizeLifecycleStage } from '../../../utils/lifecycleStage'
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
  ContractLinkageSummary
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
const loading = ref(false)
const errorMessage = ref('')
const linkage = ref<ContractLinkageSummary>()
const assessmentOverview = ref<ContractAssessmentOverview>()
const archiveRuleInfo = ref<ContractArchiveRuleInfo>()
const docRouteSignature = ref('')
const skipNextDocRouteWatch = ref(false)
const DOC_ROUTE_KEYS = ['docElderId', 'docKind', 'docKeyword'] as const
const DOC_ROUTE_OBSERVE_KEYS = ['elderId', 'residentId', 'contractId', 'leadId'] as const
const { elderOptions, elderLoading, searchElders, ensureSelectedElder } = useElderOptions({ pageSize: 120 })
const selector = reactive({
  elderId: undefined as number | undefined
})
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
  const reports = flattenReports(assessmentOverview.value).map((report) => ({
    id: `report-${report.recordId}`,
    kind: 'ASSESSMENT' as DocumentKind,
    type: mapAssessmentType(report.assessmentType),
    name: buildAssessmentReportName(report),
    url: '',
    route: `/elder/assessment/ability/archive?elderId=${encodeURIComponent(String(linkage.value?.elderId || ''))}`,
    size: '-',
    time: report.assessmentDate || '-'
  }))
  return [...attachments, ...reports]
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

const contractAttachmentCount = computed(() => linkage.value?.attachmentCount || 0)
const assessmentReportCount = computed(() => flattenReports(assessmentOverview.value).length)
const resolvedLifecycleStage = computed(() =>
  normalizeLifecycleStage(linkage.value?.flowStage, linkage.value?.contractStatus)
)
const lifecycleSubject = computed(() => {
  if (!linkage.value) return '未匹配到合同信息'
  return `合同 ${linkage.value.contractNo || '-'} / 长者 ${linkage.value.elderName || '-'}`
})
const lifecycleHint = computed(() => {
  if (missingArchiveItems.value.length > 0) {
    return `当前仍有资料缺口：${missingArchiveItemsLabel.value}，建议先补齐后再推进签署。`
  }
  return lifecycleStageHint(resolvedLifecycleStage.value)
})
const hasContractAttachment = computed(() =>
  linkage.value?.hasContractAttachment ?? contractAttachmentCount.value > 0
)
const hasInvoiceAttachment = computed(() =>
  linkage.value?.hasInvoiceAttachment ?? (linkage.value?.attachments || []).some((item) => normalizeAttachmentType(item) === '收据/发票')
)
const hasIdAttachment = computed(() =>
  linkage.value?.hasIdAttachment
    ?? (linkage.value?.attachments || []).some((item) => {
      const type = String(item.attachmentType || '').toUpperCase()
      const fileName = String(item.fileName || '').toLowerCase()
      return type.includes('ID') || type.includes('IDENT') || type === 'HOUSEHOLD' || type === 'MEDICAL_INSURANCE'
        || fileName.includes('身份证') || fileName.includes('户口') || fileName.includes('医保')
    })
)
const hasAssessmentReport = computed(() =>
  linkage.value?.hasAssessmentReport ?? assessmentReportCount.value > 0
)
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
  const elderId = Number(firstRouteQueryText(route.query.docElderId || route.query.elderId || route.query.residentId))
  if (Number.isFinite(elderId) && elderId > 0) {
    selector.elderId = elderId
    ensureSelectedElder(elderId)
  } else {
    selector.elderId = undefined
  }

  const kind = firstRouteQueryText(route.query.docKind).toUpperCase()
  filters.kind = kind === 'ATTACHMENT' || kind === 'ASSESSMENT' ? kind : 'ALL'
  filters.keyword = firstRouteQueryText(route.query.docKeyword)
  searchSnapshot.kind = filters.kind
  searchSnapshot.keyword = filters.keyword
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
  if (linkage.value?.contractNo) {
    router.push(`/marketing/contract-signing?contractNo=${encodeURIComponent(linkage.value.contractNo)}`)
    return
  }
  router.push('/marketing/contract-signing')
}

function goContractManagement() {
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
  const elderId = selector.elderId ? String(selector.elderId) : String(route.query.elderId || '').trim()
  const residentId = String(route.query.residentId || '').trim()
  const contractId = String(route.query.contractId || '').trim()
  const leadId = String(route.query.leadId || '').trim()

  const byElder = elderId || residentId
  if (byElder) {
    const linked = await safeLinkageByElder(byElder)
    if (linked) return linked
  }

  const byContract = contractId
  if (byContract) {
    const linked = await safeLinkageByContract(byContract)
    if (linked) return linked
  }

  const byLead = leadId
  if (byLead) {
    const linked = await safeLinkageByLead(byLead)
    if (linked) return linked
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
    if (!linkage.value) {
      assessmentOverview.value = undefined
      return
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
    const fromRoute = Number(route.query.docElderId || route.query.elderId || route.query.residentId || 0)
    if (fromRoute > 0) {
      ensureSelectedElder(fromRoute)
      selector.elderId = fromRoute
    } else {
      selector.elderId = elderOptions.value[0]?.value
    }
  }
  await syncDocumentQueryToRoute().catch(() => {})
  await loadAll()
})

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
</style>
