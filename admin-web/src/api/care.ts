import request from '../utils/request'
import type { ApiResult, PageResult, CareTaskItem } from '../types/api'

export function getTodayTasks(params: any) {
  return request.get<any>('/api/care/tasks/today', { params }).then((res) => normalizeTaskResult(res))
}

export function getTaskPage(params: any) {
  return request.get<any>('/api/care/tasks/today', { params }).then((res) => normalizeTaskResult(res))
}

export function generateTodayTasks(date?: string, force?: boolean) {
  const params: any = {}
  if (date) params.date = date
  if (force) params.force = true
  return request.post<any>('/api/care/tasks/generate', null, { params })
}

function normalizeTaskResult(res: any): ApiResult<PageResult<CareTaskItem>> {
  if (Array.isArray(res)) {
    return { code: 0, message: 'OK', data: { records: res, total: res.length } }
  }
  if (res?.data && Array.isArray(res.data)) {
    return { code: 0, message: 'OK', data: { records: res.data, total: res.data.length } }
  }
  if (res?.data?.records) {
    return res
  }
  return { code: 0, message: 'OK', data: { records: [], total: 0 } }
}
