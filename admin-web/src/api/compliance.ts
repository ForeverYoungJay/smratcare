import request from '../utils/request'
import type { Id } from '../types/common'

// 后端返回 MyBatis-Plus IPage 结构（records/total/current/size）
export interface IPageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export interface SensitiveAccessLog {
  id: Id
  orgId: Id
  actorId: Id
  actorName: string
  actorRole: string
  action: string
  dataCategory: string
  targetType: string
  targetId: Id
  targetName: string
  fields: string
  purpose: string
  result: string
  ip: string
  userAgent: string
  requestId: string
  createTime: string
}

export interface SensitiveAccessSummary {
  days: number
  totalAccess: number
  exportCount: number
  deniedCount: number
  distinctActors: number
  distinctTargets: number
  byCategory: Record<string, number>
  byAction: Record<string, number>
  topActors: Array<{ actorId: Id; actorName: string; count: number }>
}

export function getSensitiveAccessPage(params: {
  actorId?: Id
  dataCategory?: string
  action?: string
  startDate?: string
  endDate?: string
  pageNo?: number
  pageSize?: number
}) {
  return request.get<IPageResult<SensitiveAccessLog>>('/api/compliance/sensitive-access/page', { params })
}

export function getSensitiveAccessSummary(params?: { days?: number }) {
  return request.get<SensitiveAccessSummary>('/api/compliance/sensitive-access/summary', { params })
}
