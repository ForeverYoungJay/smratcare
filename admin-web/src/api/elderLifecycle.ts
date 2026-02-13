import request, { fetchPage } from '../utils/request'
import type { AdmissionRequest, DischargeRequest, ChangeLogItem } from '../types'

export function admitElder(data: AdmissionRequest) {
  return request.post<void>('/api/elder/lifecycle/admit', data)
}

export function dischargeElder(data: DischargeRequest) {
  return request.post<void>('/api/elder/lifecycle/discharge', data)
}

export function getChangeLogs(params: any) {
  return fetchPage<ChangeLogItem>('/api/elder/lifecycle/changes', params)
}
