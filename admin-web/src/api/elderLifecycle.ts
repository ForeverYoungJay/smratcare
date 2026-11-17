import request, { fetchPage } from '../utils/request'
import { exportCsvByRequest } from '../utils/export'
import type {
  AdmissionRequest,
  DischargeRequest,
  ChangeLogItem,
  AdmissionRecordItem,
  AdmissionRecordSummary,
  ChangeLogQuery,
  AdmissionRecordQuery
} from '../types'

export function admitElder(data: AdmissionRequest) {
  return request.post<void>('/api/elder/lifecycle/admit', data)
}

export function dischargeElder(data: DischargeRequest) {
  return request.post<void>('/api/elder/lifecycle/discharge', data)
}

export function getChangeLogs(params: ChangeLogQuery) {
  return fetchPage<ChangeLogItem>('/api/elder/lifecycle/changes', params)
}

export function getAdmissionRecords(params: AdmissionRecordQuery) {
  return fetchPage<AdmissionRecordItem>('/api/elder/lifecycle/admissions/page', params)
}

export function getAdmissionRecordSummary(params: Omit<AdmissionRecordQuery, 'pageNo' | 'pageSize'>) {
  return request.get<AdmissionRecordSummary>('/api/elder/lifecycle/admissions/summary', { params })
}

export function exportChangeLogs(params: ChangeLogQuery) {
  return exportCsvByRequest('/api/elder/lifecycle/changes/export', params, '变更记录.csv')
}

export function exportAdmissionRecords(params: AdmissionRecordQuery) {
  return exportCsvByRequest('/api/elder/lifecycle/admissions/export', params, '入住办理记录.csv')
}

/** 入住办理中心：暂存草稿 / 当前步骤 / 退回补充（步骤完成情况由前端从既有业务数据推导）。 */
export interface AdmissionProgressItem {
  id: string | number
  elderId: string | number
  contractNo?: string
  currentStep: number
  status: 'DRAFT' | 'RETURNED' | string
  returnReason?: string
  draftJson?: string
  updateTime?: string
}

export function getAdmissionProgress(params: { elderId?: string | number; status?: string }) {
  return request.get<AdmissionProgressItem[]>('/api/elder/admission-progress', { params })
}

export function saveAdmissionProgress(data: { elderId: string | number; contractNo?: string; currentStep: number; status?: string; returnReason?: string; draftJson?: string }) {
  return request.put<AdmissionProgressItem>('/api/elder/admission-progress', data)
}
