import type { MarketingCallbackType, MarketingLeadMode, MarketingPlanModuleType, MarketingPlanStatus } from '../types'

export const MARKETING_PLAN_STATUS_LABELS: Record<string, string> = {
  DRAFT: '草稿',
  PENDING_APPROVAL: '待审批',
  APPROVED: '审批通过（待发布）',
  REJECTED: '已驳回',
  PUBLISHED: '已发布',
  INACTIVE: '停用',
  ACTIVE: '已发布'
}

export const MARKETING_PLAN_STATUS_COLORS: Record<string, string> = {
  PUBLISHED: 'green',
  ACTIVE: 'green',
  APPROVED: 'blue',
  PENDING_APPROVAL: 'orange',
  REJECTED: 'red'
}

export const MARKETING_PLAN_STATUS_OPTIONS: Array<{ label: string; value: MarketingPlanStatus }> = [
  { label: '草稿', value: 'DRAFT' },
  { label: '待审批', value: 'PENDING_APPROVAL' },
  { label: '审批通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '停用', value: 'INACTIVE' }
]

export const MARKETING_MODULE_LABELS: Record<MarketingPlanModuleType, string> = {
  SPEECH: '营销话术',
  POLICY: '季度政策'
}

export const MARKETING_CALLBACK_TYPE_LABELS: Record<MarketingCallbackType, string> = {
  checkin: '入住后回访',
  trial: '试住回访',
  discharge: '退住回访',
  score: '满意度评分'
}

export const MARKETING_CALLBACK_TYPE_OPTIONS: Array<{ label: string; value: MarketingCallbackType }> = [
  { label: '入住后回访', value: 'checkin' },
  { label: '试住回访', value: 'trial' },
  { label: '退住回访', value: 'discharge' },
  { label: '满意度评分', value: 'score' }
]

export const MARKETING_LEAD_MODE_LABELS: Record<MarketingLeadMode | 'pipeline', string> = {
  consultation: '咨询管理',
  intent: '意向客户',
  reservation: '预订管理',
  invalid: '失效用户',
  callback: '互动跟进',
  pipeline: '销售漏斗'
}

export const MARKETING_SNAPSHOT_TYPE_LABELS: Record<string, string> = {
  MARKETING_WORKBENCH: '工作台快照',
  MARKETING_CONVERSION: '转化率快照',
  MARKETING_CONSULTATION: '咨询统计快照',
  MARKETING_CHANNEL: '渠道评估快照',
  MARKETING_FOLLOWUP: '跟进统计快照',
  MARKETING_CALLBACK: '回访统计快照',
  MARKETING_DATA_QUALITY: '数据质量快照'
}

export const MARKETING_SNAPSHOT_TYPE_OPTIONS = Object.entries(MARKETING_SNAPSHOT_TYPE_LABELS).map(([value, label]) => ({
  value,
  label
}))
