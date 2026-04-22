import request, { fetchPage } from '../utils/request'
import type {
  PageResult,
  ElderItem,
  ElderCreateRequest,
  ElderAssignBedRequest,
  ElderUnbindRequest,
  ElderDiseaseUpdateRequest,
  ElderDiseaseItem,
  FamilyBindRequest,
  UploadedFileResult,
  Id
} from '../types'

export function getElderPage(params: any) {
  return fetchPage<ElderItem>('/api/elder/page', params)
}

export function getElderDetail(id: Id) {
  return request.get<ElderItem>(`/api/elder/${id}`)
}

export function createElder(data: ElderCreateRequest) {
  return request.post<ElderItem>('/api/elder', data)
}

export function updateElder(id: Id, data: Partial<ElderCreateRequest> & { clearBed?: boolean }) {
  return request.put<void>(`/api/elder/${id}`, data)
}

export function deleteElder(id: Id) {
  return request.delete<void>(`/api/elder/${id}`)
}

export function assignBed(elderId: Id, bedId: Id, startDate: string) {
  const body: ElderAssignBedRequest = { bedId, startDate }
  return request.post<void>(`/api/elder/${elderId}/assignBed`, body)
}

export function unbindBed(elderId: Id, endDate?: string, reason?: string) {
  const body: ElderUnbindRequest = { endDate, reason }
  return request.post<void>(`/api/elder/${elderId}/unbindBed`, body)
}

export function getElderDiseases(elderId: Id) {
  return request.get<ElderDiseaseItem[]>(`/api/elder/${elderId}/diseases`)
}

export function updateElderDiseases(elderId: Id, diseaseIds: number[]) {
  const body: ElderDiseaseUpdateRequest = { diseaseIds }
  return request.put<void>(`/api/elder/${elderId}/diseases`, body)
}

export function bindFamily(data: FamilyBindRequest) {
  const payload = {
    ...data,
    isPrimary: data.isPrimary === true || Number(data.isPrimary) === 1 ? 1 : 0
  }
  return request.post<void>('/api/admin/family/relations/bind', payload)
}

export function uploadElderFile(file: File, bizType = 'elder-archive') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('bizType', bizType)
  return request.post<UploadedFileResult>('/api/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
