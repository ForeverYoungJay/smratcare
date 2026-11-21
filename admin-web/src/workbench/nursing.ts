import { getTaskPage, getTaskSummary } from '../api/care'
import type { CareTaskItem, CareTaskSummary, Id } from '../types'
import type { WorkbenchProfile } from './model'

export type NursingWorkbenchScope = 'SELF' | 'DEPARTMENT'

export interface NursingWorkbenchTask {
  id: number
  elderId: Id
  elderName: string
  location: string
  taskName: string
  planTime: string
  staffName?: string
  status: string
  statusLabel: string
  overdue: boolean
  exceptional: boolean
}

export interface NursingWorkbenchElder {
  id: Id
  name: string
  location: string
  pendingCount: number
  riskCount: number
}

export interface NursingWorkbenchDetail {
  scope: NursingWorkbenchScope
  scopeLabel: string
  upcomingTasks: NursingWorkbenchTask[]
  recentRecords: NursingWorkbenchTask[]
  relatedElders: NursingWorkbenchElder[]
  assignedCount?: number
  unassignedCount?: number
  totalCount?: number
  hasPersonnelBreakdown: false
}

export interface NursingWorkbenchLoadContext {
  staffId?: Id
  date: string
}

export interface NursingWorkbenchResult {
  summary: CareTaskSummary
  detail: NursingWorkbenchDetail
}

function responseData<T>(response: T | { data: T }): T {
  return response && typeof response === 'object' && 'data' in response
    ? (response as { data: T }).data
    : response as T
}

function numericStaffId(value?: Id) {
  if (value === undefined || value === null || value === '') return undefined
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : undefined
}

export function buildNursingQuery(profile: WorkbenchProfile, context: NursingWorkbenchLoadContext) {
  const query: Record<string, string | number> = { date: context.date }
  if (profile.audience !== 'EMPLOYEE') return query
  const staffId = numericStaffId(context.staffId)
  if (!staffId) throw new Error('当前账号缺少员工档案，无法安全加载个人护理任务')
  query.staffId = staffId
  return query
}

export function nursingTaskStatusLabel(status?: string) {
  if (status === 'DONE') return '已完成'
  if (status === 'EXCEPTION') return '异常待处理'
  if (status === 'CANCELLED') return '已取消'
  if (status === 'IN_PROGRESS') return '执行中'
  return '待执行'
}

function taskPriority(task: CareTaskItem) {
  if (task.overdueFlag) return 0
  if (task.status === 'EXCEPTION') return 1
  if (task.status === 'IN_PROGRESS') return 2
  return 3
}

function mapTask(task: CareTaskItem): NursingWorkbenchTask {
  return {
    id: task.taskDailyId,
    elderId: task.elderId,
    elderName: task.elderName || '未命名长者',
    location: [task.roomNo, task.careLevel].filter(Boolean).join(' · ') || '位置待补充',
    taskName: task.taskName || '护理任务',
    planTime: task.planTime,
    staffName: task.staffName,
    status: task.status,
    statusLabel: nursingTaskStatusLabel(task.status),
    overdue: Boolean(task.overdueFlag),
    exceptional: task.status === 'EXCEPTION' || Boolean(task.suspiciousFlag)
  }
}

export function buildNursingWorkbenchDetail(
  profile: WorkbenchProfile,
  summary: CareTaskSummary,
  upcomingRows: CareTaskItem[],
  completedRows: CareTaskItem[]
): NursingWorkbenchDetail {
  const activeRows = upcomingRows.filter((task) => task.status !== 'DONE' && task.status !== 'CANCELLED')
  const upcomingTasks = [...activeRows]
    .sort((left, right) => taskPriority(left) - taskPriority(right) || String(left.planTime || '').localeCompare(String(right.planTime || '')))
    .slice(0, 6)
    .map(mapTask)
  const elderMap = new Map<string, NursingWorkbenchElder>()
  for (const task of activeRows) {
    const key = String(task.elderId)
    const current = elderMap.get(key) || {
      id: task.elderId,
      name: task.elderName || '未命名长者',
      location: [task.roomNo, task.careLevel].filter(Boolean).join(' · ') || '位置待补充',
      pendingCount: 0,
      riskCount: 0
    }
    current.pendingCount += task.status === 'DONE' || task.status === 'CANCELLED' ? 0 : 1
    current.riskCount += task.overdueFlag || task.status === 'EXCEPTION' || task.suspiciousFlag ? 1 : 0
    elderMap.set(key, current)
  }
  return {
    scope: profile.audience === 'EMPLOYEE' ? 'SELF' : 'DEPARTMENT',
    scopeLabel: profile.audience === 'EMPLOYEE' ? '仅显示分配给我的任务' : '护理部今日任务',
    upcomingTasks,
    recentRecords: completedRows.slice(0, 5).map(mapTask),
    relatedElders: [...elderMap.values()]
      .sort((left, right) => right.riskCount - left.riskCount || right.pendingCount - left.pendingCount)
      .slice(0, 5),
    assignedCount: summary.assignedCount,
    unassignedCount: summary.unassignedCount,
    totalCount: summary.totalCount,
    // 当前后端没有按护理人员聚合的完成情况接口；不能用任务预览页推算全员绩效。
    hasPersonnelBreakdown: false
  }
}

export async function loadNursingWorkbench(
  profile: WorkbenchProfile,
  context: NursingWorkbenchLoadContext
): Promise<NursingWorkbenchResult> {
  const query = buildNursingQuery(profile, context)
  const [summaryResponse, upcomingPage, completedPage] = await Promise.all([
    getTaskSummary(query),
    getTaskPage({ ...query, pageNo: 1, pageSize: 60 }),
    getTaskPage({ ...query, pageNo: 1, pageSize: 5, status: 'DONE' })
  ])
  const summary = responseData<CareTaskSummary>(summaryResponse)
  return {
    summary: summary || {},
    detail: buildNursingWorkbenchDetail(profile, summary || {}, upcomingPage.list || [], completedPage.list || [])
  }
}
