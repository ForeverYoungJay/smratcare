import request, { fetchPage } from '../utils/request'
import type { Id } from '../types'

export interface FinanceReminderItem {
  id: Id
  reminderType: string
  reminderTypeText?: string
  title: string
  content?: string
  severity?: string
  bizMonth?: string
  elderId?: Id
  elderName?: string
  roomId?: Id
  amount?: number
  actionPath?: string
  status: string
  statusText?: string
  handledAt?: string
  handleRemark?: string
  createTime?: string
}

export interface FinanceReminderSummary {
  pendingCount: number
  handledCount: number
  pendingByType: Record<string, number>
}

export function getReminderPage(params: {
  pageNo: number
  pageSize: number
  status?: string
  reminderType?: string
}) {
  return fetchPage<FinanceReminderItem>('/api/finance/reminder/page', params)
}

export function getReminderSummary() {
  return request.get<FinanceReminderSummary>('/api/finance/reminder/summary')
}

export function handleReminder(reminderId: Id, remark?: string) {
  return request.post<FinanceReminderItem>(
    `/api/finance/reminder/${reminderId}/handle`,
    null,
    { params: { remark } }
  )
}

export function handleAllReminders(reminderType?: string, remark?: string) {
  return request.post<{ handled: number }>(
    '/api/finance/reminder/handle-all',
    null,
    { params: { reminderType, remark } }
  )
}

export function generateReminders() {
  return request.post<{ careFee: number; deposit: number; electricity: number }>(
    '/api/finance/reminder/generate'
  )
}
