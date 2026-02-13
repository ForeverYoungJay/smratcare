import request, { fetchPage } from '../utils/request'
import type {
  PageResult,
  ElderItem,
  ElderCreateRequest,
  ElderAssignBedRequest,
  ElderUnbindRequest,
  ElderDiseaseUpdateRequest,
  ElderDiseaseItem,
  FamilyBindRequest
} from '../types'

export function getElderPage(params: any) {
  return fetchPage<ElderItem>('/api/elder/page', params)
}

export function getElderDetail(id: number) {
  return request.get<ElderItem>(`/api/elder/${id}`)
}

export function createElder(data: ElderCreateRequest) {
  return request.post<ElderItem>('/api/elder', data)
}

export function updateElder(id: number, data: Partial<ElderCreateRequest>) {
  return request.put<void>(`/api/elder/${id}`, data)
}

export function assignBed(elderId: number, bedId: number, startDate: string) {
  const body: ElderAssignBedRequest = { bedId, startDate }
  return request.post<void>(`/api/elder/${elderId}/assignBed`, body)
}

export function unbindBed(elderId: number, endDate?: string, reason?: string) {
  const body: ElderUnbindRequest = { endDate, reason }
  return request.post<void>(`/api/elder/${elderId}/unbindBed`, body)
}

export function getElderDiseases(elderId: number) {
  return request.get<ElderDiseaseItem[]>(`/api/elder/${elderId}/diseases`)
}

export function updateElderDiseases(elderId: number, diseaseIds: number[]) {
  const body: ElderDiseaseUpdateRequest = { diseaseIds }
  return request.put<void>(`/api/elder/${elderId}/diseases`, body)
}

export function bindFamily(data: FamilyBindRequest) {
  return request.post<void>('/api/family/bindElder', data)
}
