import request, { fetchPage } from '../utils/request'
import type { Id } from '../types/common'

// ================= 智能排班 =================

export interface AiScheduleProposalItem {
  id: Id
  proposalId: Id
  staffId: Id
  staffName?: string
  dutyDate: string
  shiftCode: string
  startTime?: string
  endTime?: string
  nightShift?: number
  manualAdjusted?: number
  violationNote?: string
  scheduleId?: Id | null
}

export interface AiScheduleProposal {
  id: Id
  title: string
  deptId?: Id
  deptName?: string
  dateFrom: string
  dateTo: string
  shiftCodes?: string
  status: 'DRAFT' | 'GENERATED' | 'ADOPTED'
  scoreJson?: string
  unfilledCount?: number
  unfilledSlots?: string[]
  generatedAt?: string
  adoptedAt?: string
  remark?: string
  createTime?: string
  items?: AiScheduleProposalItem[]
}

export interface AiScheduleConstraint {
  id?: Id
  maxWeeklyHours?: number
  maxConsecutiveDays?: number
  nightRestEnabled?: number
  respectLeave?: number
  qualificationJson?: string
  workloadBalanceWeight?: number
  nightFairnessWeight?: number
  remark?: string
}

export interface AiScheduleGeneratePayload {
  title?: string
  deptId?: Id
  deptName?: string
  dateFrom: string
  dateTo: string
  staffIds?: Id[]
  shiftCodes?: string[]
  remark?: string
}

export function generateAiScheduleProposal(data: AiScheduleGeneratePayload) {
  return request.post<AiScheduleProposal>('/api/ai/schedule/proposals/generate', data)
}

export function getAiScheduleProposalPage(params: { pageNo?: number; pageSize?: number; status?: string }) {
  return fetchPage<AiScheduleProposal>('/api/ai/schedule/proposals/page', params)
}

export function getAiScheduleProposal(id: Id) {
  return request.get<AiScheduleProposal>(`/api/ai/schedule/proposals/${id}`)
}

export function updateAiScheduleProposalItem(
  proposalId: Id,
  itemId: Id,
  data: { staffId?: Id; shiftCode?: string }
) {
  return request.put<AiScheduleProposalItem>(`/api/ai/schedule/proposals/${proposalId}/items/${itemId}`, data)
}

export function deleteAiScheduleProposalItem(proposalId: Id, itemId: Id) {
  return request.delete<void>(`/api/ai/schedule/proposals/${proposalId}/items/${itemId}`)
}

export function adoptAiScheduleProposal(id: Id) {
  return request.post<{ proposalId: Id; createdCount: number; skippedCount: number }>(
    `/api/ai/schedule/proposals/${id}/adopt`
  )
}

export function deleteAiScheduleProposal(id: Id) {
  return request.delete<void>(`/api/ai/schedule/proposals/${id}`)
}

export function getAiScheduleConstraint() {
  return request.get<AiScheduleConstraint>('/api/ai/schedule/constraint')
}

export function saveAiScheduleConstraint(data: AiScheduleConstraint) {
  return request.post<AiScheduleConstraint>('/api/ai/schedule/constraint', data)
}

// ================= 健康风险预测 =================

export interface AiRiskModel {
  id?: Id
  riskType: string
  modelName?: string
  ruleJson?: string
  enabled?: number
  remark?: string
}

export interface AiRiskScoreItem {
  id: Id
  elderId: Id
  elderName?: string
  riskType: string
  score: number
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH'
  factorJson?: string
  assessTime?: string
  assessDate?: string
  source?: string
  alertId?: Id | null
}

export interface AiRiskSummary {
  elderCount: number
  highCount: number
  mediumCount: number
  lowCount: number
  typeStats: Array<{ riskType: string; highCount: number; mediumCount: number; lowCount: number }>
}

export interface AiRiskTrendPoint {
  assessDate: string
  score: number
  riskLevel: string
}

export function getAiRiskModels() {
  return request.get<AiRiskModel[]>('/api/ai/risk/models')
}

export function saveAiRiskModel(data: AiRiskModel) {
  return request.post<AiRiskModel>('/api/ai/risk/models', data)
}

export function recomputeAiRisk(data?: { elderId?: Id; riskType?: string }) {
  return request.post<{ scoreCount: number }>('/api/ai/risk/recompute', data || {})
}

export function getAiRiskScorePage(params: {
  pageNo?: number
  pageSize?: number
  riskType?: string
  riskLevel?: string
  elderId?: Id
  keyword?: string
}) {
  return fetchPage<AiRiskScoreItem>('/api/ai/risk/scores/page', params)
}

export function getAiRiskSummary() {
  return request.get<AiRiskSummary>('/api/ai/risk/scores/summary')
}

export function getAiRiskTrend(params: { elderId: Id; riskType: string; days?: number }) {
  return request.get<AiRiskTrendPoint[]>('/api/ai/risk/scores/trend', { params })
}

// ================= AI 网关配置 =================

export interface AiGatewayConfig {
  id?: Id
  provider?: string
  endpoint?: string
  apiKey?: string
  modelName?: string
  timeoutMs?: number
  enabled?: number
  remark?: string
}

export function getAiGatewayConfig() {
  return request.get<AiGatewayConfig>('/api/ai/gateway/config')
}

export function saveAiGatewayConfig(data: AiGatewayConfig) {
  return request.post<AiGatewayConfig>('/api/ai/gateway/config', data)
}
