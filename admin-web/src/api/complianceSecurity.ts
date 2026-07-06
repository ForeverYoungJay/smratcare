import request from '../utils/request'
import type { Id } from '../types/common'
import type { IPageResult } from './compliance'
import type { LoginRequest, LoginResponse } from '../types'

// ===== 机构安全策略（2FA / 密码策略 / 会话超时 / 登录失败锁定 / 脱敏 / 日志留存） =====

export interface SecurityPolicy {
  orgId: Id
  twoFactorEnabled: boolean
  twoFactorRoles: string[]
  passwordMaxDays: number
  loginFailLockCount: number
  loginFailLockMinutes: number
  sessionTimeoutMinutes: number
  maskingEnabled: boolean
  maskingExemptRoles: string[]
  logRetentionDays: number
}

export function getSecurityPolicy() {
  return request.get<SecurityPolicy>('/api/compliance/security-policy')
}

export function updateSecurityPolicy(data: Partial<SecurityPolicy>) {
  return request.put<SecurityPolicy>('/api/compliance/security-policy', data)
}

// ===== 导出二次确认与留痕 =====

export interface ExportLogItem {
  id: Id
  orgId: Id
  actorId: Id
  actorName: string
  actorRole: string
  module: string
  scope: string
  purpose: string
  rowCount: number | null
  exportTicket: string
  status: 'PENDING' | 'USED' | 'EXPIRED'
  expiresAt: string
  usedAt: string
  ip: string
  createTime: string
}

export interface ExportTicket {
  exportTicket: string
  expiresAt: string
}

export function confirmExport(data: {
  module: string
  scope?: string
  purpose: string
  estimatedRows?: number
}) {
  return request.post<ExportTicket>('/api/compliance/export/confirm', data)
}

export function completeExport(data: { exportTicket: string; rowCount?: number }) {
  return request.post<void>('/api/compliance/export/complete', data)
}

export function getExportLogPage(params: {
  module?: string
  status?: string
  startDate?: string
  endDate?: string
  pageNo?: number
  pageSize?: number
}) {
  return request.get<IPageResult<ExportLogItem>>('/api/compliance/export/page', { params })
}

// ===== 管理端 2FA 登录（登录第一步返回 requireTwoFactor + challengeToken 时进入第二步） =====

export type TwoFactorLoginResponse = LoginResponse & {
  requireTwoFactor?: boolean
  challengeToken?: string
  twoFactorPhone?: string
}

export function loginWithTwoFactorSupport(data: LoginRequest) {
  return request.post<TwoFactorLoginResponse>('/api/auth/login', data)
}

export function verifyTwoFactor(data: { challengeToken: string; verifyCode: string }) {
  return request.post<TwoFactorLoginResponse>('/api/auth/2fa/verify', data)
}

export function resendTwoFactorCode(data: { challengeToken: string }) {
  return request.post<{ retryAfterSeconds?: number; message?: string; debugCode?: string }>(
    '/api/auth/2fa/resend',
    data
  )
}
