import type {
  ContractAssessmentOverview,
  ContractAssessmentReportItem,
  ContractLinkageSummary,
  ElderCareProfile,
  ElderItem,
  Id,
  MedicalResidentRiskCard
} from '../../types'

export const DETAIL_TABS = ['base', 'health', 'plan', 'service', 'family', 'contract', 'archive'] as const
export type DetailTab = (typeof DETAIL_TABS)[number]

function queryText(value: unknown) {
  return String((Array.isArray(value) ? value[0] : value) ?? '').trim()
}

/** ?tab= 兼容旧值：disease（疾病与禁忌）已并入健康评估。 */
export function normalizeDetailTab(tab: unknown): DetailTab {
  const value = queryText(tab)
  if (value === 'disease') return 'health'
  return (DETAIL_TABS as readonly string[]).includes(value) ? (value as DetailTab) : 'base'
}

export interface DetailAlert {
  level: 'danger' | 'warning'
  text: string
}

/** 重点关注提醒：只用接口返回的真实数据，没有就返回空数组。 */
export function buildDetailAlerts(profile?: ElderCareProfile | null, risk?: MedicalResidentRiskCard | null): DetailAlert[] {
  const alerts: DetailAlert[] = (profile?.riskTags || []).map((text) => ({ level: 'danger', text }))
  // 后端把 Long 序列化成字符串，"0" 是真值，必须按数字判断
  const count = (value: unknown) => Number(value || 0)
  const cvd = String(risk?.latestCvdRiskLevel || '').toUpperCase()
  if (cvd === 'HIGH' || cvd === 'VERY_HIGH') alerts.push({ level: 'danger', text: '心血管风险评估：高风险' })
  if (count(risk?.abnormalVital24hCount) > 0) alerts.push({ level: 'danger', text: `近 24 小时体征异常 ${risk?.abnormalVital24hCount} 次` })
  if (count(risk?.openIncidentCount) > 0) alerts.push({ level: 'danger', text: `未结案事件 ${risk?.openIncidentCount} 起` })
  if (count(risk?.abnormalInspectionOpenCount) > 0) alerts.push({ level: 'warning', text: `巡检异常待处理 ${risk?.abnormalInspectionOpenCount} 条` })
  if (count(risk?.pendingCareTaskCount) > 0) alerts.push({ level: 'warning', text: `今日待执行护理任务 ${risk?.pendingCareTaskCount} 项` })
  if (profile?.adjustmentRequired && profile.suggestedCareLevel) {
    alerts.push({ level: 'warning', text: `评估建议调整为${profile.suggestedCareLevel}` })
  }
  return alerts
}

export interface ArchiveCheck {
  ok: boolean
  text: string
}

/** 重要资料状态：来自合同联动汇总（同合同与票据页的判定口径）。 */
export function buildArchiveChecks(linkage?: ContractLinkageSummary | null): ArchiveCheck[] {
  if (!linkage) return []
  return [
    { ok: Boolean(linkage.hasIdAttachment), text: linkage.hasIdAttachment ? '身份证明材料已归档' : '身份证明材料未归档' },
    { ok: Boolean(linkage.hasContractAttachment), text: linkage.hasContractAttachment ? '入住合同已归档' : '入住合同未归档' },
    { ok: Boolean(linkage.hasAssessmentReport), text: linkage.hasAssessmentReport ? '评估报告已归档' : '评估报告未归档' }
  ]
}

export function contractStatusText(status?: string) {
  const value = String(status || '').trim().toUpperCase()
  const map: Record<string, string> = {
    DRAFT: '草稿',
    PENDING_APPROVAL: '待审批',
    APPROVED: '已审批',
    REJECTED: '已驳回',
    PENDING_ASSESSMENT: '待评估',
    PENDING_BED_SELECT: '待办理入住',
    PENDING_SIGN: '待签署',
    SIGNED: '已签署',
    EFFECTIVE: '已生效',
    VOID: '已作废'
  }
  return value ? map[value] || status || '--' : '--'
}

const BED_PAGE = '/elder/bed-panorama'
const FEE_EXIT_PAGE = '/elder/status-change/discharge-settlement'

/** 常用业务操作：直接指向合并后的新页面（参数口径见 router/residentRedirects.ts），不再经过旧地址重定向。 */
export function residentActionLinks(id: Id) {
  const elderId = String(id)
  return [
    { key: 'admission', label: '入住资料', path: '/elder/admission-processing', query: { elderId } },
    { key: 'bed', label: '申请调床', path: BED_PAGE, query: { elderId } },
    { key: 'outing', label: '外出请假', path: BED_PAGE, query: { view: 'records', recordType: 'OUTING', elderId, action: 'outing' } },
    { key: 'medical', label: '住院/返院', path: BED_PAGE, query: { view: 'records', recordType: 'MEDICAL_OUTING', elderId, action: 'medical' } },
    { key: 'fee', label: '费用查询', path: FEE_EXIT_PAGE, query: { tab: 'fee', elderId } },
    { key: 'discharge', label: '退住申请', path: FEE_EXIT_PAGE, query: { tab: 'discharge', elderId, create: '1' } }
  ]
}

/* ---------- 合同资料（原「合同与票据」页并入） ---------- */
export const CONTRACT_DOC_KIND_OPTIONS = [
  { label: '全部', value: 'ALL' },
  { label: '病历资料', value: 'MEDICAL_RECORD' },
  { label: '医保复印件', value: 'MEDICAL_INSURANCE' },
  { label: '户口复印件', value: 'HOUSEHOLD' },
  { label: '合同附件', value: 'CONTRACT' },
  { label: '其他附件', value: 'OTHER' },
  { label: '评估报告', value: 'ASSESSMENT_REPORT' }
]

export interface ContractDocument {
  id: string
  kind: 'ATTACHMENT' | 'ASSESSMENT'
  typeCode: string
  type: string
  name: string
  url?: string
  route?: string
  size: string
  time: string
}

/** 旧参数 docKind / docKeyword 继续生效；未知 docKind 视为全部。 */
export function contractDocFilterFromQuery(query: Record<string, unknown>) {
  const kind = queryText(query.docKind).toUpperCase()
  return {
    kind: CONTRACT_DOC_KIND_OPTIONS.some((item) => item.value === kind) ? kind : 'ALL',
    keyword: queryText(query.docKeyword)
  }
}

const ATTACHMENT_TYPES = ['ASSESSMENT_REPORT', 'MEDICAL_RECORD', 'MEDICAL_INSURANCE', 'HOUSEHOLD', 'CONTRACT']
const ASSESSMENT_TYPE_TEXT: Record<string, string> = {
  ADMISSION: '入住评估',
  REGISTRATION: '登记评估',
  CONTINUOUS: '持续评估',
  ARCHIVE: '评估档案',
  COGNITIVE: '认知评估',
  SELF_CARE: '自理能力评估',
  OTHER_SCALE: '其他量表'
}

function docKindLabel(code: string) {
  return CONTRACT_DOC_KIND_OPTIONS.find((item) => item.value === code)?.label || '其他附件'
}

function fileSizeText(size?: number) {
  if (!size || size <= 0) return '--'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / (1024 * 1024)).toFixed(1)} MB`
}

function reportsOf(overview?: ContractAssessmentOverview | null): ContractAssessmentReportItem[] {
  return [...(overview?.contracts || []).flatMap((item) => item.reports || []), ...(overview?.unassignedReports || [])]
}

/** 合并历史合同、签约附件与评估报告，口径同原合同与票据页。 */
export function buildContractDocuments(
  linkage?: ContractLinkageSummary | null,
  elder?: ElderItem | null,
  overview?: ContractAssessmentOverview | null
): ContractDocument[] {
  const historical: ContractDocument[] = elder?.historicalContractFileUrl
    ? [{
        id: `elder-contract-${elder.id}`,
        kind: 'ATTACHMENT',
        typeCode: 'CONTRACT',
        type: '历史合同附件',
        name: decodeURIComponent(elder.historicalContractFileUrl.split('/').pop() || '') || '历史合同附件',
        url: elder.historicalContractFileUrl,
        size: '--',
        time: elder.admissionDate || '--'
      }]
    : []
  const attachments: ContractDocument[] = (linkage?.attachments || []).map((item) => {
    const raw = String(item.attachmentType || '').toUpperCase()
    const typeCode = ATTACHMENT_TYPES.includes(raw) ? raw : 'OTHER'
    return {
      id: `att-${item.id}`,
      kind: 'ATTACHMENT',
      typeCode,
      type: docKindLabel(typeCode),
      name: item.fileName || '未命名附件',
      url: item.fileUrl,
      size: fileSizeText(item.fileSize),
      time: item.createTime || '--'
    }
  })
  const elderId = String(linkage?.elderId || elder?.id || '')
  const reports: ContractDocument[] = reportsOf(overview).map((report) => {
    const type = ASSESSMENT_TYPE_TEXT[report.assessmentType || ''] || '评估'
    const level = report.levelCode ? `-${report.levelCode}` : ''
    const score = report.score == null ? '' : `-${report.score}分`
    return {
      id: `report-${report.recordId}`,
      kind: 'ASSESSMENT',
      typeCode: 'ASSESSMENT_REPORT',
      type,
      name: report.reportFileName || `${type}报告${level}${score}`,
      url: report.reportFileUrl || '',
      route: report.reportFileUrl ? '' : `/elder/assessment/ability/archive?elderId=${encodeURIComponent(elderId)}`,
      size: '--',
      time: report.assessmentDate || '--'
    }
  })
  return [...historical, ...attachments, ...reports]
}

export function filterContractDocuments(docs: ContractDocument[], kind: string, keyword: string) {
  const text = keyword.trim().toLowerCase()
  return docs.filter(
    (item) =>
      (kind === 'ALL' || item.typeCode === kind) &&
      (!text || item.name.toLowerCase().includes(text) || item.type.toLowerCase().includes(text))
  )
}

/** 归档完整度：优先用后端规则引擎结果，缺失时按 合同40/证件30/评估30 估算（同原页面）。 */
export function archiveCompleteness(linkage?: ContractLinkageSummary | null) {
  if (!linkage) return null
  const raw = Number(linkage.archiveScore)
  const score = Number.isFinite(raw) && linkage.archiveScore != null
    ? Math.max(0, Math.min(100, Math.round(raw)))
    : (linkage.hasContractAttachment ? 40 : 0) + (linkage.hasIdAttachment ? 30 : 0) + (linkage.hasAssessmentReport ? 30 : 0)
  const levels: Record<string, string> = { COMPLETE: '完整', HIGH: '高', MEDIUM: '中', LOW: '低' }
  const level = levels[String(linkage.archiveLevel || '').toUpperCase()]
    || (score >= 95 ? '完整' : score >= 80 ? '高' : score >= 50 ? '中' : '低')
  return { score, level }
}
