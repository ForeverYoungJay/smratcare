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
  derivedHealthAlertCount: number
  derivedHealthGeneratedCount: number
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

export interface SmartAlertRule {
  id: Id
  orgId?: Id
  ruleCode: string
  ruleName: string
  eventType: string
  deviceType?: string
  metricKey?: string
  operator?: string
  threshold?: number
  threshold2?: number
  durationSec?: number
  level?: string
  disabilityLevelScope?: string
  autoDispatch?: number
  notifyFamily?: number
  priority?: number
  enabled?: number
  remark?: string
}

export interface SmartAlertDispatch {
  id: Id
  alertId: Id
  elderId?: Id
  deviceId?: Id
  ruleId?: Id
  level?: string
  dispatchStatus?: string
  assigneeId?: Id
  assigneeName?: string
  triggeredAt?: string
  assignedAt?: string
  respondedAt?: string
  onsiteAt?: string
  handledAt?: string
  reviewedAt?: string
  responseDeadline?: string
  escalationCount?: number
  handleNote?: string
  reviewNote?: string
  incidentId?: Id
}
