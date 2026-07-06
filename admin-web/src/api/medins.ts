import request, { fetchPage } from '../utils/request'
import type { Id } from '../types'

// ===== 医保对接适配层类型 =====
export interface MedinsChannelConfig {
  id: Id
  orgId?: Id | null
  channel: string
  regionCode?: string
  endpoint?: string
  appId?: string
  secretRef?: string
  enabled?: number
  remark?: string
  createTime?: string
}

export interface MedinsChannelOption {
  channel: string
  regionCode?: string
}

export interface MedinsSettlementSheet {
  id: Id
  ltciSettlementId: Id
  elderId: Id
  sheetNo?: string
  settleMonth: string
  totalFee: number
  fundPay: number
  selfPay: number
  sheetStatus: string
  channel?: string
  receiptCode?: string
  rejectReason?: string
  uploadedAt?: string
  receiptedAt?: string
  reconciledAt?: string
  remark?: string
  createTime?: string
}

export interface MedinsUploadTask {
  id: Id
  sheetId: Id
  channel: string
  bizType?: string
  taskStatus: string
  payloadJson?: string
  payloadHash?: string
  receiptCode?: string
  receiptJson?: string
  lastError?: string
  retryCount?: number
  sentAt?: string
  ackedAt?: string
  createTime?: string
}

export interface MedinsEvoucher {
  id: Id
  elderId: Id
  insuredNo?: string
  idCard?: string
  ecardToken?: string
  channel?: string
  bindStatus: string
  verifyStatus?: string
  verifyMessage?: string
  lastVerifyAt?: string
  remark?: string
  createTime?: string
}

// ===== 渠道配置 =====
export function getMedinsChannels() {
  return request.get<MedinsChannelConfig[]>('/api/medins/channels')
}

export function getMedinsChannelOptions() {
  return request.get<MedinsChannelOption[]>('/api/medins/channels/options')
}

export function saveMedinsChannel(data: {
  id?: Id
  channel: string
  regionCode?: string
  endpoint?: string
  appId?: string
  secretRef?: string
  enabled?: number
  remark?: string
}) {
  return request.post<Id>('/api/medins/channels', data)
}

export function toggleMedinsChannel(id: Id, enabled: boolean) {
  return request.post(`/api/medins/channels/${id}/toggle`, null, { params: { enabled } })
}

// ===== 结算清单 =====
export function generateMedinsSheet(params: {
  ltciSettlementId?: Id
  elderId?: Id
  month?: string
}) {
  return request.post<MedinsSettlementSheet>('/api/medins/sheets/generate', null, { params })
}

export function getMedinsSheetPage(params: {
  pageNo?: number
  pageSize?: number
  elderId?: Id
  month?: string
  status?: string
}) {
  return fetchPage<MedinsSettlementSheet>('/api/medins/sheets/page', params)
}

export function getMedinsSheet(id: Id) {
  return request.get<MedinsSettlementSheet>(`/api/medins/sheets/${id}`)
}

export function uploadMedinsSheet(id: Id, channel?: string) {
  return request.post<MedinsUploadTask>(`/api/medins/sheets/${id}/upload`, null, {
    params: { channel }
  })
}

export function reconcileMedinsSheet(id: Id) {
  return request.post(`/api/medins/sheets/${id}/reconcile`)
}

// ===== 上报任务与回执 =====
export function getMedinsTaskPage(params: {
  pageNo?: number
  pageSize?: number
  sheetId?: Id
  status?: string
}) {
  return fetchPage<MedinsUploadTask>('/api/medins/tasks/page', params)
}

export function retryMedinsTask(id: Id) {
  return request.post<MedinsUploadTask>(`/api/medins/tasks/${id}/retry`)
}

export function queryMedinsTaskReceipt(id: Id) {
  return request.post<MedinsUploadTask>(`/api/medins/tasks/${id}/query-receipt`)
}

// ===== 电子凭证 =====
export function bindMedinsEvoucher(data: {
  elderId: Id
  insuredNo?: string
  idCard?: string
  ecardToken?: string
  channel?: string
  remark?: string
}) {
  return request.post<Id>('/api/medins/evouchers', data)
}

export function getMedinsEvoucherPage(params: {
  pageNo?: number
  pageSize?: number
  elderId?: Id
  bindStatus?: string
  verifyStatus?: string
}) {
  return fetchPage<MedinsEvoucher>('/api/medins/evouchers/page', params)
}

export function verifyMedinsEvoucher(id: Id) {
  return request.post<MedinsEvoucher>(`/api/medins/evouchers/${id}/verify`)
}

export function unbindMedinsEvoucher(id: Id) {
  return request.post(`/api/medins/evouchers/${id}/unbind`)
}
