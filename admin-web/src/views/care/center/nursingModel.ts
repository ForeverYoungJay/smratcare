import type { LocationQuery } from 'vue-router'

/** 护理部 5 个主页面的标签：key 与路由 query.tab 一致 */
export const TASK_TABS = [
  { key: 'today', label: '今日任务' },
  { key: 'calendar', label: '任务日历' },
  { key: 'records', label: '服务记录' },
  { key: 'stats', label: '执行统计' }
] as const

export const RESIDENT_TABS = [
  { key: 'overview', label: '护理概况' },
  { key: 'assessment', label: '能力与风险评估' },
  { key: 'plan', label: '护理计划' },
  { key: 'records', label: '服务记录' },
  { key: 'incidents', label: '交接与异常' }
] as const

export const SAFETY_TABS = [
  { key: 'handover', label: '交接班记录' },
  { key: 'patrol', label: '巡视记录' },
  { key: 'incident', label: '异常事件' },
  { key: 'exception', label: '任务异常' }
] as const

export const OPERATIONS_TABS = [
  { key: 'roster', label: '人员排班' },
  { key: 'workload', label: '工作负荷' },
  { key: 'quality', label: '质量指标' },
  { key: 'rectify', label: '整改台账' }
] as const

type TabList = readonly { key: string; label: string }[]

/** query.tab → 有效标签，非法值回落到第一个 */
export function resolveTab<T extends TabList>(tabs: T, query: LocationQuery): T[number]['key'] {
  const raw = typeof query.tab === 'string' ? query.tab : ''
  return (tabs.find((tab) => tab.key === raw) || tabs[0]).key
}

// —— 异常事件 ——

export const INCIDENT_TYPES: Record<string, string> = {
  FALL: '跌倒/滑倒', SKIN: '压疮/皮肤异常', CHOKING: '噎食/误吸', WANDER: '走失',
  DELAY: '服务延误', ENVIRONMENT: '环境隐患', OTHER: '其他'
}

export const INCIDENT_SEVERITY: Record<string, { label: string; tone: string }> = {
  HIGH: { label: '高', tone: 'danger' }, MEDIUM: { label: '中', tone: 'warning' }, LOW: { label: '低', tone: '' }
}

export const INCIDENT_STATUS: Record<string, { label: string; tone: string }> = {
  REPORTED: { label: '新上报', tone: 'warning' },
  ACCEPTED: { label: '已受理', tone: 'info' },
  PROCESSING: { label: '处理中', tone: 'info' },
  ESCALATED: { label: '已升级', tone: 'danger' },
  PENDING_REVIEW: { label: '待复核', tone: 'warning' },
  CLOSED: { label: '已闭环', tone: 'success' }
}

export const ESCALATE_TARGETS: Record<string, string> = { MEDICAL: '医务部', LOGISTICS: '后勤部', ADMIN_DUTY: '行政值班' }

export type IncidentAction = 'ACCEPT' | 'PROCESS' | 'ESCALATE' | 'SUBMIT_REVIEW' | 'CLOSE' | 'REOPEN' | 'ASSIGN' | 'CORRECT'

export const INCIDENT_ACTION_LABELS: Record<string, string> = {
  REPORT: '上报', ACCEPT: '受理', PROCESS: '开始处理', ESCALATE: '升级', SUBMIT_REVIEW: '提交复核',
  CLOSE: '复核结案', REOPEN: '退回重办', ASSIGN: '指派责任人', CORRECT: '更正说明'
}

/** 与后端状态机一致：给定状态和是否管理岗，可执行哪些操作（顺序即按钮顺序） */
export function incidentActions(status: string, manager: boolean): IncidentAction[] {
  const flow: Record<string, IncidentAction[]> = {
    REPORTED: ['ACCEPT'],
    ACCEPTED: ['PROCESS', 'ESCALATE'],
    PROCESSING: ['SUBMIT_REVIEW', 'ESCALATE'],
    ESCALATED: ['SUBMIT_REVIEW', 'PROCESS'],
    PENDING_REVIEW: manager ? ['CLOSE', 'REOPEN'] : [],
    CLOSED: []
  }
  const actions = [...(flow[status] || [])]
  if (manager && status !== 'CLOSED') actions.push('ASSIGN')
  actions.push('CORRECT')
  return actions
}

/** 这些操作必须写说明：提交复核写处置结果，退回写原因，更正写更正内容，结案写复核意见 */
export function incidentActionNeedsNote(action: IncidentAction) {
  return ['SUBMIT_REVIEW', 'REOPEN', 'CORRECT', 'CLOSE'].includes(action)
}

// —— 质控指标 ——

export interface QualityMetric { key: string; label: string; numerator: number; denominator: number; rate: number | null }

/** 指标点击后定位到支撑数据所在的页面 */
export const METRIC_DRILLDOWN: Record<string, string> = {
  onTime: '/care/tasks?tab=today&metric=overdue',
  recordComplete: '/care/tasks?tab=records',
  handoverConfirm: '/care/safety?tab=handover',
  incidentClosure: '/care/safety?tab=incident',
  rectifyOnTime: '/care/operations?tab=rectify&overdue=true'
}

/** 任务汇总接口的完成率是 0~1 的小数，换成保留一位的百分数 */
export function ratioPercent(ratio?: number | string | null) {
  return ratio == null ? undefined : Math.round(Number(ratio) * 1000) / 10
}

/** 分母为 0 显示「暂无数据」而不是 0% 或 100% */
export function formatRate(metric: Pick<QualityMetric, 'rate' | 'denominator'>) {
  return metric.rate == null || !metric.denominator ? '暂无数据' : `${metric.rate}%`
}

// —— 排班检查 ——

export interface RosterCell { staffId: number | string; staffName?: string; dutyDate: string; shiftCode?: string }

/**
 * 找出同一人同一天被排了多个班（重复分配），以及某天某班次无人（岗位空缺）。
 * requiredShifts 为每天必须有人的班次编码。
 */
export function checkRoster(cells: RosterCell[], days: string[], requiredShifts: string[]) {
  const perStaffDay = new Map<string, RosterCell[]>()
  const covered = new Set<string>()
  for (const cell of cells) {
    if (!cell.shiftCode) continue
    const key = `${cell.staffId}|${cell.dutyDate}`
    perStaffDay.set(key, [...(perStaffDay.get(key) || []), cell])
    covered.add(`${cell.dutyDate}|${cell.shiftCode}`)
  }
  const duplicates = [...perStaffDay.values()].filter((list) => list.length > 1)
    .map((list) => ({ staffName: list[0].staffName || String(list[0].staffId), dutyDate: list[0].dutyDate, shifts: list.map((c) => c.shiftCode!) }))
  const gaps = days.flatMap((day) => requiredShifts.filter((shift) => !covered.has(`${day}|${shift}`)).map((shift) => ({ dutyDate: day, shiftCode: shift })))
  return { duplicates, gaps }
}

// —— 工作台 ——

interface TaskLike { status?: string; overdueFlag?: boolean; planTime?: string; staffName?: string }

/** 今日重点待办：超时 → 异常 → 按计划时间；已完成的不出现 */
export function sortTodo<T extends TaskLike>(tasks: T[]): T[] {
  const rank = (t: T) => (t.overdueFlag ? 0 : t.status === 'EXCEPTION' ? 1 : 2)
  return tasks.filter((t) => t.status !== 'DONE')
    .sort((a, b) => rank(a) - rank(b) || String(a.planTime || '').localeCompare(String(b.planTime || '')))
}

/** 按责任人统计完成进度，未分配的单列一行 */
export function progressByStaff<T extends TaskLike>(tasks: T[]) {
  const groups = new Map<string, { total: number; done: number }>()
  for (const t of tasks) {
    const key = t.staffName || '待分配'
    const g = groups.get(key) || { total: 0, done: 0 }
    g.total += 1
    if (t.status === 'DONE') g.done += 1
    groups.set(key, g)
  }
  return [...groups.entries()].map(([name, g]) => ({ name, ...g, rate: Math.round((g.done / g.total) * 100) }))
    .sort((a, b) => a.rate - b.rate)
}

/** 分时完成趋势：到每个整点为止，应完成（计划时间已到）的任务里已完成的比例 */
export function hourlyCompletion<T extends TaskLike>(tasks: T[], hours = [8, 10, 12, 14, 16, 18]) {
  return hours.map((hour) => {
    const due = tasks.filter((t) => t.planTime && Number(String(t.planTime).slice(11, 13)) < hour)
    const done = due.filter((t) => t.status === 'DONE').length
    return { label: `${String(hour).padStart(2, '0')}:00`, rate: due.length ? Math.round((done / due.length) * 100) : 0 }
  })
}
