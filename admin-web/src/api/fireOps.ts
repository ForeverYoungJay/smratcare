import request, { fetchPage } from '../utils/request'
import type { Id } from '../types'

/** 消防运营：楼栋点位、检查模板、交接双方确认、报告签署、培训演练、录像调阅、楼栋风险 */

export interface FireLocation {
  id?: Id
  building: string
  floorName?: string
  area?: string
  pointCode?: string
  riskLevel: 'HIGH' | 'MEDIUM' | 'LOW'
  keyPart?: number
  enabled?: number
  remark?: string
}
export interface FireCheckTemplate {
  id?: Id
  recordType: 'DAY_PATROL' | 'NIGHT_PATROL' | 'MONTHLY_CHECK'
  templateName: string
  items: string
  version?: number
  enabled?: number
  updatedByName?: string
  updateTime?: string
}
export interface FireDutyHandover {
  id: Id
  shiftDate: string
  shiftName: string
  handoverStaffId: Id
  handoverName?: string
  takeoverStaffId: Id
  takeoverName?: string
  checklist?: string
  openIssues?: string
  status: 'PENDING' | 'CONFIRMED' | 'REJECTED'
  handoverAt: string
  takeoverAt?: string
  takeoverNote?: string
}
export interface FireReportSignature {
  id: Id
  recordId: Id
  signType: 'SUBMIT' | 'REVIEW'
  signerId: Id
  signerName?: string
  fileUrl?: string
  fileSha256?: string
  comment?: string
  signedAt: string
}
export interface FireTrainingDrill {
  id?: Id
  drillType: 'DRILL' | 'TRAINING'
  topic: string
  plannedDate?: string
  heldDate?: string
  organizerName?: string
  participantCount?: number
  attendees?: string
  summary?: string
  nextPlan?: string
  fileUrl?: string
  status?: 'PLANNED' | 'DONE'
}
export interface FireVideoAccessRequest {
  id: Id
  cameraId: Id
  cameraName?: string
  rangeStart: string
  rangeEnd: string
  reason: string
  applicantId: Id
  applicantName?: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  approverName?: string
  decidedAt?: string
  decisionNote?: string
  expiresAt?: string
  createTime?: string
}
export interface FireVideoAccessLog { id: Id; action: string; staffName?: string; recordingId?: Id; createTime: string }
export interface FireLiveStream { configured: boolean; url?: string; protocol?: string; message?: string }
export interface FireBuildingRisk { building: string; points: number; keyParts: number; openHazards: number; overdue: number; faults: number; level: 'OK' | 'WARNING' | 'DANGER' }
export interface FireHazardStats { total: number; overdue: number; running: number; faults: number }

const base = '/api/fire'

export const getFireLocations = () => request.get<FireLocation[]>(`${base}/locations`)
export const saveFireLocation = (data: FireLocation) => request.post<FireLocation>(`${base}/locations`, data)
export const deleteFireLocation = (id: Id) => request.delete<void>(`${base}/locations/${id}`)
export const getFireTemplates = (params?: { recordType?: string; enabledOnly?: boolean }) => request.get<FireCheckTemplate[]>(`${base}/templates`, { params })
export const saveFireTemplate = (data: FireCheckTemplate) => request.post<FireCheckTemplate>(`${base}/templates`, data)

export const getFireHandovers = (params: Record<string, unknown>) => fetchPage<FireDutyHandover>(`${base}/handovers`, params)
export const getHandoverStaffOptions = (keyword?: string) => request.get<{ value: string; label: string }[]>(`${base}/handovers/staff-options`, { params: { keyword } })
export const createFireHandover = (data: { shiftName: string; shiftDate?: string; takeoverStaffId: Id; checklist?: string; openIssues?: string }) =>
  request.post<FireDutyHandover>(`${base}/handovers`, data)
export const confirmFireHandover = (id: Id, accept: boolean, note?: string) => request.post<FireDutyHandover>(`${base}/handovers/${id}/confirm`, { accept, note })

export const getReportSignatures = (recordId: Id | number) => request.get<FireReportSignature[]>(`${base}/report-signatures`, { params: { recordId } })
export const signFireReport = (recordId: Id | number, signType: 'SUBMIT' | 'REVIEW', comment?: string) =>
  request.post<FireReportSignature>(`${base}/report-signatures`, { recordId, signType, comment })

export const getFireDrills = (params: Record<string, unknown>) => fetchPage<FireTrainingDrill>(`${base}/drills`, params)
export const saveFireDrill = (data: FireTrainingDrill) => request.post<FireTrainingDrill>(`${base}/drills`, data)

export const getFireHazardStats = () => request.get<FireHazardStats>(`${base}/hazards/stats`)
export const getFireBuildingRisk = () => request.get<FireBuildingRisk[]>(`${base}/buildings/risk`)

export const getFireLiveStream = (cameraId: Id) => request.get<FireLiveStream>(`${base}/video/live/${cameraId}`)
export const getVideoAccessRequests = (params: Record<string, unknown>) => fetchPage<FireVideoAccessRequest>(`${base}/video/access-requests`, params)
export const applyVideoAccess = (data: { cameraId: Id; rangeStart: string; rangeEnd: string; reason: string }) =>
  request.post<FireVideoAccessRequest>(`${base}/video/access-requests`, data)
export const decideVideoAccess = (id: Id, approve: boolean, note?: string) =>
  request.post<FireVideoAccessRequest>(`${base}/video/access-requests/${id}/decide`, { approve, note })
export const getAccessRecordings = (id: Id) =>
  request.get<{ id: Id; startedAt: string; endedAt: string; provider: string }[]>(`${base}/video/access-requests/${id}/recordings`)
export const getAccessPlayback = (id: Id, recordingId: Id | number) =>
  request.get<{ url: string; provider: string }>(`${base}/video/access-requests/${id}/recordings/${recordingId}/playback`)
export const getVideoAccessLogs = (id: Id) => request.get<FireVideoAccessLog[]>(`${base}/video/access-requests/${id}/logs`)
