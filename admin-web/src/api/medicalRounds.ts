import request, { fetchPage } from '../utils/request'
import type { Id } from '../types'

export interface MedicalRoundsPlan {
  id: Id
  orgId?: Id
  planDate?: string
  timeSlot?: string
  doctorId?: Id
  doctorName?: string
  area?: string
  elderScope?: string
  elderIdsJson?: string
  status?: string
  remark?: string
  createTime?: string
}

export interface MedicalRoundsRecord {
  id: Id
  planId?: Id
  elderId?: Id
  elderName?: string
  roundTime?: string
  findings?: string
  handleOpinion?: string
  resultLevel?: string
  emrRecordId?: Id
  medicalOrderId?: Id
  doctorId?: Id
  doctorName?: string
  remark?: string
  createTime?: string
}

export function getRoundsPlanPage(params: {
  pageNo?: number
  pageSize?: number
  doctorId?: Id
  status?: string
  dateFrom?: string
  dateTo?: string
}) {
  return fetchPage<MedicalRoundsPlan>('/api/medical/rounds/plans/page', params)
}

export function createRoundsPlan(data: Partial<MedicalRoundsPlan>) {
  return request.post<MedicalRoundsPlan>('/api/medical/rounds/plans', data)
}

export function updateRoundsPlanStatus(id: Id, status: string) {
  return request.post<MedicalRoundsPlan>(`/api/medical/rounds/plans/${id}/status`, null, { params: { status } })
}

export function getRoundsRecordPage(params: {
  pageNo?: number
  pageSize?: number
  planId?: Id
  elderId?: Id
  resultLevel?: string
}) {
  return fetchPage<MedicalRoundsRecord>('/api/medical/rounds/records/page', params)
}

export function createRoundsRecord(data: Partial<MedicalRoundsRecord> & {
  generateEmr?: number
  diagnosis?: string
  orderContent?: string
  orderType?: string
  orderCategory?: string
  orderDosage?: string
  orderFrequency?: string
}) {
  return request.post<MedicalRoundsRecord>('/api/medical/rounds/records', data)
}
