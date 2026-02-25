import request, { fetchPage } from '../utils/request'
import { exportCsvByRequest } from '../utils/export'
import type {
  AdmissionRequest,
  DischargeRequest,
  ChangeLogItem,
  AdmissionRecordItem,
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

export function exportChangeLogs(params: ChangeLogQuery) {
  return exportCsvByRequest('/api/elder/lifecycle/changes/export', params, '变更记录.csv')
}

export function exportAdmissionRecords(params: AdmissionRecordQuery) {
  return exportCsvByRequest('/api/elder/lifecycle/admissions/export', params, '入住签约记录.csv')
}
