import type { Id } from './common'

export interface SmartDevice {
  id: Id
  orgId: Id
  elderId?: Id
  deviceCode: string
  deviceName: string
  deviceType: string
  vendor?: string
  model?: string
  location?: string
  protocol?: string
  bindStatus?: string
  onlineStatus: string
  lastHeartbeatAt?: string
  lastEventAt?: string
  enabled: number
  remark?: string
}

export interface SmartAlert {
  id: Id
  orgId: Id
  elderId?: Id
  deviceId?: Id
  sourceEventId?: Id
  alertNo: string
  alertType: string
  level: string
  title: string
  content?: string
  location?: string
  status: string
  firstTriggeredAt: string
  latestTriggeredAt: string
  acknowledgedBy?: Id
  acknowledgedAt?: string
  resolvedBy?: Id
  resolvedAt?: string
  resolutionNote?: string
  escalationCount: number
  notifyFamily: number
}

export interface SmartAlertSummary {
  totalDeviceCount: number
  onlineDeviceCount: number
  offlineDeviceCount: number
  openAlertCount: number
  criticalAlertCount: number
  todayEventCount: number
  levelStats: Array<{ level: string; count: number }>
}

export interface SmartDeviceEventRequest {
  deviceCode: string
  eventType: string
  eventLevel?: string
  eventTime?: string
  payload?: Record<string, any>
  location?: string
  elderId?: Id
}
