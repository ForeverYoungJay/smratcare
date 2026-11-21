import dayjs from 'dayjs'
import type { LocationQuery } from 'vue-router'
import { CONTRACT_FLOW_STAGE_LABELS } from '../../../utils/marketingEnums'
import type { CallbackPlanItem, CrmLeadAssignLogItem, CrmStageTransitionLogItem } from '../../../types'

/** 客户与跟进页的标签：key 与路由 query.tab 一致，旧地址经 marketingRedirects.ts 映射到这里 */
export const CUSTOMER_TABS = [
  { key: 'all', label: '全部客户', mode: 'all' },
  { key: 'intent', label: '高意向', mode: 'intent' },
  { key: 'followup', label: '待跟进', mode: 'callback' },
  { key: 'reservation', label: '已预订', mode: 'reservation' },
  { key: 'invalid', label: '已流失', mode: 'invalid' }
] as const

export type CustomerTab = typeof CUSTOMER_TABS[number]['key']
export type FollowupView = 'today' | 'due' | 'overdue'
export type CallbackType = 'checkin' | 'trial' | 'discharge'

export interface CustomerView {
  tab: CustomerTab
  mode: typeof CUSTOMER_TABS[number]['mode']
  scenario: string
  followupView?: FollowupView
  callbackType?: CallbackType
}

const FOLLOWUP_SCENARIO: Record<FollowupView, string> = { today: 'follow_today', due: 'follow_due', overdue: 'follow_overdue' }

function text(value: unknown) {
  return typeof value === 'string' ? value.trim() : ''
}

/** 路由 query → 当前标签、传给线索表的 mode/scenario */
export function resolveCustomerView(query: LocationQuery): CustomerView {
  const found = CUSTOMER_TABS.find((tab) => tab.key === text(query.tab)) || CUSTOMER_TABS[0]
  const view: CustomerView = { tab: found.key, mode: found.mode, scenario: text(query.scenario) }
  if (found.key === 'followup') {
    const requested = text(query.view) as FollowupView
    view.followupView = requested in FOLLOWUP_SCENARIO ? requested : 'due'
    view.scenario = view.scenario || FOLLOWUP_SCENARIO[view.followupView]
    const callbackType = text(query.callbackType) as CallbackType
    if (['checkin', 'trial', 'discharge'].includes(callbackType)) view.callbackType = callbackType
  }
  return view
}

/** 切换标签：保留筛选条件，去掉只属于上一个标签的视图参数 */
export function customerTabQuery(query: LocationQuery, tab: CustomerTab, extra: Record<string, string> = {}): LocationQuery {
  const { view: _view, scenario: _scenario, callbackType: _callbackType, ...rest } = query
  return { ...rest, tab, ...extra }
}

export function maskPhone(value?: string | null) {
  const phone = String(value || '').trim()
  if (phone.length < 7) return phone || '-'
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`
}

const STAGE_LABELS: Record<string, string> = {
  ...CONTRACT_FLOW_STAGE_LABELS,
  ASSESSMENT: '评估部', MARKETING: '营销部', CONSULTATION: '咨询', INTENT: '意向',
  RESERVATION: '预订', INVALID: '失效', SIGNED: '已签约',
  '0': '咨询', '1': '意向', '2': '预订', '3': '失效'
}

export function stageLabel(value?: string | number | null) {
  const key = String(value ?? '').trim()
  return key ? STAGE_LABELS[key] || key : ''
}

/** 系统写入的备注末尾会附带原始 JSON，展示时剥掉 */
export function cleanRemark(value?: string | null) {
  return String(value || '').replace(/\s*\|\s*\{[\s\S]*\}\s*$/, '').trim()
}

export interface TimelineItem {
  key: string
  time: string
  kind: 'followup' | 'plan' | 'stage' | 'assign'
  title: string
  meta: string
  content: string
}

/** 回访记录、阶段流转、负责人分配合并为一条时间线，按时间倒序 */
export function buildCustomerTimeline(
  plans: CallbackPlanItem[] = [],
  stageLogs: CrmStageTransitionLogItem[] = [],
  assignLogs: CrmLeadAssignLogItem[] = []
): TimelineItem[] {
  const items: TimelineItem[] = [
    ...plans.map((plan): TimelineItem => {
      const done = plan.status === 'DONE'
      return {
        key: `plan-${plan.id}`,
        time: (done ? plan.executedTime : plan.planExecuteTime) || plan.createTime || '',
        kind: done ? 'followup' : 'plan',
        title: done ? `${plan.title}${plan.followupResult ? ` · ${plan.followupResult}` : ''}` : `待回访：${plan.title}`,
        meta: done ? plan.executorName || '' : '计划执行',
        content: cleanRemark(plan.followupContent || plan.executeNote)
      }
    }),
    ...stageLogs.map((log): TimelineItem => ({
      key: `stage-${log.id}`,
      time: log.operatedAt || '',
      kind: 'stage',
      title: `阶段：${stageLabel(log.fromStage || log.fromStatus) || '起始'} → ${stageLabel(log.toStage || log.toStatus) || '当前'}`,
      meta: log.operatedByName || '',
      content: cleanRemark(log.remark)
    })),
    ...assignLogs.map((log): TimelineItem => ({
      key: `assign-${log.id}`,
      time: log.assignedAt || '',
      kind: 'assign',
      title: `负责人：${log.fromOwnerStaffName || '未分配'} → ${log.toOwnerStaffName || '未分配'}`,
      meta: log.assignedByName || '',
      content: cleanRemark(log.remark)
    }))
  ]
  return items.sort((a, b) => dayjs(b.time).valueOf() - dayjs(a.time).valueOf())
}

export function formatTime(value?: string | null) {
  if (!value) return '-'
  const parsed = dayjs(value)
  return parsed.isValid() ? parsed.format('MM-DD HH:mm') : value
}
