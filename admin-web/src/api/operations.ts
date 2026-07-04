import request from '../utils/request'

export type OperationsCapabilityStatus = 'READY' | 'PARTIAL' | 'PLANNED'

export interface OperationsCapabilitySummary {
  label: string
  total: number
  ready: number
  partial: number
  planned: number
}

export interface OperationsDomainCapability {
  key: string
  title: string
  description: string
  status: OperationsCapabilityStatus
  statusText: string
  routePath: string
  completed: string[]
  nextActions: string[]
  evidenceRoutes: string[]
}

export interface OperationsMetricCapability {
  key: string
  name: string
  routePath: string
  source: string
  decisionValue: string
}

export interface OperationsIntelligenceCapability {
  key: string
  title: string
  status: OperationsCapabilityStatus
  routePath: string
  landingSteps: string[]
}

export interface OperationsCapabilityMap {
  version: string
  generatedAt: string
  overallStatus: string
  summary: OperationsCapabilitySummary[]
  domains: OperationsDomainCapability[]
  metrics: OperationsMetricCapability[]
  intelligence: OperationsIntelligenceCapability[]
}

export interface OperationsReportMetric {
  key: string
  label: string
  value: string
  helper: string
  status: OperationsCapabilityStatus
  routePath: string
}

export interface OperationsQualityDomain {
  key: string
  title: string
  owner: string
  status: OperationsCapabilityStatus
  routePath: string
  checkpoints: string[]
  evidence: string[]
}

export interface OperationsComplianceArchive {
  key: string
  title: string
  status: OperationsCapabilityStatus
  routePath: string
  requiredDocuments: string[]
  missingOrNext: string[]
}

export interface OperationsStandardProcess {
  key: string
  title: string
  status: OperationsCapabilityStatus
  routePath: string
  steps: string[]
}

export interface OperationsReportAction {
  title: string
  priority: 'HIGH' | 'MEDIUM' | 'LOW'
  owner: string
  routePath: string
  dueHint: string
}

export interface OperationsServiceReport {
  month: string
  generatedAt: string
  reportTitle: string
  status: string
  metrics: OperationsReportMetric[]
  qualityDomains: OperationsQualityDomain[]
  complianceArchives: OperationsComplianceArchive[]
  standardProcesses: OperationsStandardProcess[]
  monthlyActions: OperationsReportAction[]
}

export interface OperationsFamilyServiceMetric {
  key: string
  label: string
  value: string
  helper: string
  routePath: string
}

export interface OperationsFamilyRuntimeCard {
  key: string
  title: string
  value: string
  status: OperationsCapabilityStatus | 'WARNING' | string
  helper: string
  routePath: string
}

export interface OperationsFamilyServiceFlow {
  key: string
  title: string
  status: OperationsCapabilityStatus
  routePath: string
  familyApi: string
  adminRoute: string
  steps: string[]
}

export interface OperationsFamilyVisibleData {
  key: string
  title: string
  status: OperationsCapabilityStatus
  familyApi: string
  adminRoute: string
  fields: string[]
}

export interface OperationsFamilyServiceAction {
  title: string
  owner: string
  priority: 'HIGH' | 'MEDIUM' | 'LOW'
  routePath: string
  description: string
}

export interface OperationsFamilyService {
  generatedAt: string
  status: string
  metrics: OperationsFamilyServiceMetric[]
  runtimeCards: OperationsFamilyRuntimeCard[]
  flows: OperationsFamilyServiceFlow[]
  visibleData: OperationsFamilyVisibleData[]
  actions: OperationsFamilyServiceAction[]
}

export interface OperationsFamilyOpsHealthSmsStats {
  total: number
  sent: number
  verified: number
  used: number
  expired: number
  failed: number
}

export interface OperationsFamilyOpsHealthNotifyStats {
  total: number
  success: number
  failed: number
  pending: number
  skipped: number
  pendingRetry: number
}

export interface OperationsFamilyOpsHealthRechargeStats {
  total: number
  init: number
  prepayCreated: number
  paid: number
  closed: number
  failed: number
  totalAmount: number
  paidAmount: number
}

export interface OperationsFamilyOpsHealthErrorItem {
  source: string
  reason: string
  count: number
}

export interface OperationsFamilyOpsHealth {
  checkedAt: string
  windowHours: number
  level: 'healthy' | 'warning' | 'critical' | string
  summary: string
  sms: OperationsFamilyOpsHealthSmsStats
  notify: OperationsFamilyOpsHealthNotifyStats
  recharge: OperationsFamilyOpsHealthRechargeStats
  topErrors: OperationsFamilyOpsHealthErrorItem[]
  suggestions: string[]
}

export interface OperationsFamilyRechargeLedgerSummary {
  totalCount: number
  pendingCount: number
  paidCount: number
  abnormalCount: number
  autoPayEnabledCount: number
  totalAmount: number
  paidAmount: number
  abnormalAmount: number
}

export interface OperationsFamilyRechargeLedgerItem {
  id: number
  outTradeNo: string
  familyUserId: number
  familyName: string
  familyPhone: string
  elderId: number
  elderName: string
  amount: number
  channel: string
  status: string
  statusText: string
  remark: string
  createTime: string
  paidTime: string
  wxTransactionId: string
}

export interface OperationsFamilyRechargeLedger {
  pageNo: number
  pageSize: number
  total: number
  summary: OperationsFamilyRechargeLedgerSummary
  list: OperationsFamilyRechargeLedgerItem[]
}

export interface OperationsIntelligenceMetric {
  key: string
  label: string
  value: string
  helper: string
  routePath: string
}

export interface OperationsIntelligenceScenario {
  key: string
  title: string
  status: OperationsCapabilityStatus
  routePath: string
  owner: string
  landed: string[]
  nextSteps: string[]
}

export interface OperationsIntelligenceDataSource {
  key: string
  title: string
  status: OperationsCapabilityStatus
  routePath: string
  signals: string[]
}

export interface OperationsIntelligenceAction {
  title: string
  owner: string
  priority: 'HIGH' | 'MEDIUM' | 'LOW'
  routePath: string
  description: string
}

export interface OperationsIntelligence {
  generatedAt: string
  status: string
  metrics: OperationsIntelligenceMetric[]
  scenarios: OperationsIntelligenceScenario[]
  dataSources: OperationsIntelligenceDataSource[]
  actions: OperationsIntelligenceAction[]
}

export interface OperationsSafetyMetric {
  key: string
  label: string
  value: string
  helper: string
  tone: 'normal' | 'warning' | 'danger' | string
  routePath: string
}

export interface OperationsSafetyRiskSource {
  key: string
  title: string
  owner: string
  status: string
  routePath: string
  signals: string[]
  controls: string[]
}

export interface OperationsEmergencyFlow {
  key: string
  title: string
  trigger: string
  routePath: string
  steps: string[]
}

export interface OperationsSafetyAction {
  title: string
  priority: 'HIGH' | 'MEDIUM' | 'LOW'
  owner: string
  routePath: string
  description: string
}

export interface OperationsSafetyRecentEvent {
  id: number
  source: string
  title: string
  level: string
  status: string
  occurredAt: string
  routePath: string
}

export interface OperationsSafetyRisk {
  generatedAt: string
  status: string
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | string
  riskIndex: number
  metrics: OperationsSafetyMetric[]
  riskSources: OperationsSafetyRiskSource[]
  emergencyFlows: OperationsEmergencyFlow[]
  actions: OperationsSafetyAction[]
  recentEvents: OperationsSafetyRecentEvent[]
}

export interface OperationsWorkforceMetric {
  key: string
  label: string
  value: string
  helper: string
  tone: 'normal' | 'warning' | 'danger' | string
  routePath: string
}

export interface OperationsWorkforceRisk {
  key: string
  title: string
  owner: string
  level: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | string
  routePath: string
  signals: string[]
  controls: string[]
}

export interface OperationsWorkforceRoleLoad {
  key: string
  title: string
  coverage: string
  status: OperationsCapabilityStatus | 'WARNING' | string
  routePath: string
  signals: string[]
}

export interface OperationsWorkforceAction {
  title: string
  owner: string
  priority: 'HIGH' | 'MEDIUM' | 'LOW'
  routePath: string
  description: string
}

export interface OperationsWorkforceFlow {
  key: string
  title: string
  status: OperationsCapabilityStatus
  routePath: string
  steps: string[]
}

export interface OperationsWorkforce {
  generatedAt: string
  status: string
  workforceIndex: number
  workforceLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | string
  metrics: OperationsWorkforceMetric[]
  risks: OperationsWorkforceRisk[]
  roleLoads: OperationsWorkforceRoleLoad[]
  actions: OperationsWorkforceAction[]
  flows: OperationsWorkforceFlow[]
}

export interface OperationsStaffMobileMetric {
  key: string
  label: string
  value: string
  helper: string
  tone: 'normal' | 'warning' | 'danger' | string
  routePath: string
}

export interface OperationsStaffMobileRuntimeCard {
  key: string
  title: string
  value: string
  status: OperationsCapabilityStatus | 'WARNING' | string
  helper: string
  routePath: string
}

export interface OperationsStaffMobileRoleCard {
  key: string
  title: string
  owner: string
  status: OperationsCapabilityStatus | 'WARNING' | string
  routePath: string
  scenarios: string[]
  mobileNeeds: string[]
}

export interface OperationsStaffMobileWorkflow {
  key: string
  title: string
  status: OperationsCapabilityStatus | 'WARNING' | string
  routePath: string
  steps: string[]
}

export interface OperationsStaffMobileAction {
  title: string
  owner: string
  priority: 'HIGH' | 'MEDIUM' | 'LOW'
  routePath: string
  description: string
}

export interface OperationsStaffMobile {
  generatedAt: string
  status: string
  mobileIndex: number
  mobileLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | string
  metrics: OperationsStaffMobileMetric[]
  runtimeCards: OperationsStaffMobileRuntimeCard[]
  roleCards: OperationsStaffMobileRoleCard[]
  workflows: OperationsStaffMobileWorkflow[]
  actions: OperationsStaffMobileAction[]
}

export interface OperationsStaffMobileTask {
  id: string
  module: string
  title: string
  resident: string
  room: string
  time: string
  status: string
  priority: string
  actionText: string
  evidenceRequired: boolean
  checklist: string[]
  route: string
}

export interface OperationsStaffMobileTaskReceiptView {
  id: string
  taskId: string
  module: string
  moduleText: string
  taskTitle: string
  resident: string
  room: string
  remark: string
  scanText: string
  checkedItems: string
  photos: string[]
  voiceUrl: string
  voiceDurationSec: number
  receiptTime: string
  status: string
  adminRoute: string
  taskRoute: string
}

export interface OperationsStaffMobileHandover {
  id: string
  time: string
  title: string
  owner: string
  content: string
}

export interface OperationsStaffMobileIncidentView {
  id: string
  incidentType: string
  level: string
  resident: string
  location: string
  description: string
  actionTaken: string
  scanText: string
  photos: string[]
  voiceUrl: string
  voiceDurationSec: number
  incidentTime: string
  status: string
  adminRoute: string
}

export interface OperationsLogisticsMetric {
  key: string
  label: string
  value: string
  helper: string
  tone: 'normal' | 'warning' | 'danger' | string
  routePath: string
}

export interface OperationsLogisticsRisk {
  key: string
  title: string
  owner: string
  level: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | string
  routePath: string
  signals: string[]
  controls: string[]
}

export interface OperationsLogisticsAction {
  title: string
  owner: string
  priority: 'HIGH' | 'MEDIUM' | 'LOW'
  routePath: string
  description: string
}

export interface OperationsLogisticsFlow {
  key: string
  title: string
  status: OperationsCapabilityStatus
  routePath: string
  steps: string[]
}

export interface OperationsLogistics {
  generatedAt: string
  status: string
  logisticsIndex: number
  logisticsLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | string
  metrics: OperationsLogisticsMetric[]
  risks: OperationsLogisticsRisk[]
  actions: OperationsLogisticsAction[]
  flows: OperationsLogisticsFlow[]
}

export interface OperationsMarketingMetric {
  key: string
  label: string
  value: string
  helper: string
  tone: 'normal' | 'warning' | 'danger' | string
  routePath: string
}

export interface OperationsMarketingFunnelStage {
  key: string
  title: string
  value: string
  status: OperationsCapabilityStatus | 'WARNING' | string
  routePath: string
  signals: string[]
}

export interface OperationsMarketingRisk {
  key: string
  title: string
  owner: string
  level: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | string
  routePath: string
  signals: string[]
  controls: string[]
}

export interface OperationsMarketingChannel {
  source: string
  leadCount: string
  contractRate: string
  routePath: string
}

export interface OperationsMarketingAction {
  title: string
  owner: string
  priority: 'HIGH' | 'MEDIUM' | 'LOW'
  routePath: string
  description: string
}

export interface OperationsMarketingFlow {
  key: string
  title: string
  status: OperationsCapabilityStatus
  routePath: string
  steps: string[]
}

export interface OperationsMarketing {
  generatedAt: string
  status: string
  conversionIndex: number
  conversionLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | string
  metrics: OperationsMarketingMetric[]
  funnelStages: OperationsMarketingFunnelStage[]
  risks: OperationsMarketingRisk[]
  channels: OperationsMarketingChannel[]
  actions: OperationsMarketingAction[]
  flows: OperationsMarketingFlow[]
}

export function getOperationsCapabilityMap(config?: Record<string, any>) {
  return request.get<OperationsCapabilityMap>('/api/operations/capability-map', config)
}

export function getOperationsServiceReport(params?: { month?: string }, config?: Record<string, any>) {
  return request.get<OperationsServiceReport>('/api/operations/service-report', { params, ...(config || {}) })
}

export function getOperationsFamilyService(config?: Record<string, any>) {
  return request.get<OperationsFamilyService>('/api/operations/family-service', config)
}

export function getOperationsFamilyOpsHealth(hours = 24, config?: Record<string, any>) {
  return request.get<OperationsFamilyOpsHealth>('/api/admin/family/ops/health', {
    params: { hours },
    ...(config || {})
  })
}

export function getOperationsFamilyRechargeLedger(
  params?: {
    pageNo?: number
    pageSize?: number
    status?: string
    channel?: string
    keyword?: string
  },
  config?: Record<string, any>
) {
  return request.get<OperationsFamilyRechargeLedger>('/api/admin/family/ops/recharge-ledger', {
    params,
    ...(config || {})
  })
}

export function getOperationsIntelligence(config?: Record<string, any>) {
  return request.get<OperationsIntelligence>('/api/operations/intelligence', config)
}

export function getOperationsSafetyRisk(config?: Record<string, any>) {
  return request.get<OperationsSafetyRisk>('/api/operations/safety-risk', config)
}

export function getOperationsWorkforce(config?: Record<string, any>) {
  return request.get<OperationsWorkforce>('/api/operations/workforce', config)
}

export function getOperationsStaffMobile(config?: Record<string, any>) {
  return request.get<OperationsStaffMobile>('/api/operations/staff-mobile', config)
}

export function getOperationsStaffMobileTasks(module?: string, config?: Record<string, any>) {
  return request.get<OperationsStaffMobileTask[]>('/api/operations/staff-mobile/tasks', {
    params: { module },
    ...(config || {})
  })
}

export function getOperationsStaffMobileTaskReceipts(module?: string, config?: Record<string, any>) {
  return request.get<OperationsStaffMobileTaskReceiptView[]>('/api/operations/staff-mobile/receipts', {
    params: { module },
    ...(config || {})
  })
}

export function getOperationsStaffMobileHandovers(config?: Record<string, any>) {
  return request.get<OperationsStaffMobileHandover[]>('/api/operations/staff-mobile/handovers', config)
}

export function getOperationsStaffMobileIncidents(
  params?: { status?: string; level?: string },
  config?: Record<string, any>
) {
  return request.get<OperationsStaffMobileIncidentView[]>('/api/operations/staff-mobile/incidents', {
    params,
    ...(config || {})
  })
}

export function getOperationsLogistics(config?: Record<string, any>) {
  return request.get<OperationsLogistics>('/api/operations/logistics', config)
}

export function getOperationsMarketing(config?: Record<string, any>) {
  return request.get<OperationsMarketing>('/api/operations/marketing', config)
}
