import request, { fetchPage } from '../utils/request'
import type { Id } from '../types'

export interface MedicalEmergencyEvent {
  id: Id
  elderId?: Id
  elderName?: string
  alertId?: Id
  eventTime?: string
  location?: string
  symptom?: string
  callTime?: string
  callOperatorName?: string
  hospitalName?: string
  escortName?: string
  departTime?: string
  outcome?: string
  outcomeTime?: string
  outcomeNote?: string
  reporterName?: string
  status?: string
  remark?: string
  createTime?: string
}

export interface MedicalRescueRecord {
  id: Id
  eventId?: Id
  elderId?: Id
  timelineJson?: string
  participants?: string
  drugsUsed?: string
  measures?: string
  result?: string
  resultNote?: string
  startTime?: string
  endTime?: string
  recorderName?: string
  remark?: string
}

export function getEmergencyEventPage(params: {
  pageNo?: number
  pageSize?: number
  elderId?: Id
  status?: string
  outcome?: string
}) {
  return fetchPage<MedicalEmergencyEvent>('/api/medical/emergency/events/page', params)
}

export function getEmergencyEvent(id: Id) {
  return request.get<MedicalEmergencyEvent>(`/api/medical/emergency/events/${id}`)
}

export function createEmergencyEvent(data: Partial<MedicalEmergencyEvent>) {
  return request.post<MedicalEmergencyEvent>('/api/medical/emergency/events', data)
}

export function transitionEmergencyEvent(
  id: Id,
  data: {
    action: string
    callTime?: string
    hospitalName?: string
    escortName?: string
    departTime?: string
    outcome?: string
    outcomeNote?: string
    remark?: string
  }
) {
  return request.post<MedicalEmergencyEvent>(`/api/medical/emergency/events/${id}/transition`, data)
}

export function saveRescueRecord(data: Partial<MedicalRescueRecord> & { eventId: Id }) {
  return request.post<MedicalRescueRecord>('/api/medical/emergency/rescue', data)
}

export function getRescueRecordByEvent(eventId: Id) {
  return request.get<MedicalRescueRecord | null>(`/api/medical/emergency/rescue/by-event/${eventId}`)
}
