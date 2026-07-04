import type { Id } from './common'

export interface GovReportTask {
  id: Id
  reportType: string
  channel: string
  period?: string
  taskStatus?: string
  triggerType?: string
  recordCount?: number
  lastError?: string
  retryCount?: number
  sentAt?: string
  ackedAt?: string
  remark?: string
  createTime?: string
}

export interface GovReportSnapshot {
  id: Id
  taskId: Id
  payloadJson?: string
  payloadHash?: string
  fieldMappingVersion?: string
  createTime?: string
}

export interface GovChannelConfig {
  id: Id
  channel: string
  cityCode?: string
  endpoint?: string
  appId?: string
  secretRef?: string
  fieldMappingJson?: string
  fieldMappingVersion?: string
  enabled?: number
  remark?: string
}
