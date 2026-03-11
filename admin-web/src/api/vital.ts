import request from '../utils/request'
import type { Id } from '../types/common'
import type { VitalRecordRequest, VitalRecordItem } from '../types'

export function createVitalRecord(data: VitalRecordRequest) {
  return request.post<void>('/api/vital/record', data)
}

export function getVitalByElder(elderId: Id, params: any) {
  return request.get<VitalRecordItem[]>(`/api/vital/elder/${elderId}`, { params })
}

export function getVitalLatest(elderId: Id) {
  return request.get<VitalRecordItem>(`/api/vital/elder/${elderId}/latest`)
}

export function getVitalAbnormalToday() {
  return request.get<VitalRecordItem[]>('/api/vital/abnormal/today')
}
