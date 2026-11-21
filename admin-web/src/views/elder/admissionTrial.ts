// 入住办理中心 · 试住登记：状态文案与「转正式入住」规则（替代原 TrialStay.vue）
import type { TrialStayItem, TrialStayRequest } from '../../types'

export type AdmissionTab = 'admission' | 'trial'

const STATUS_TEXT: Record<string, string> = { REGISTERED: '已登记', FINISHED: '已结束', CONVERTED: '已转签约', CANCELLED: '已取消' }
const STATUS_COLOR: Record<string, string> = { REGISTERED: 'blue', CONVERTED: 'green', CANCELLED: 'red' }
const INTENT_TEXT: Record<string, string> = { HIGH: '高', MEDIUM: '中', LOW: '低' }

export const TRIAL_STATUS_OPTIONS = Object.entries(STATUS_TEXT).map(([value, label]) => ({ value, label }))

export function trialStatusText(status?: string) {
  return STATUS_TEXT[String(status || '')] || '--'
}

export function trialStatusColor(status?: string) {
  return STATUS_COLOR[String(status || '')] || 'default'
}

/** 意向等级既有枚举值也有历史中文文本，统一映射后展示 */
export function intentLevelText(level?: string) {
  return INTENT_TEXT[String(level || '')] || level || '--'
}

export function resolveAdmissionTab(tab: unknown): AdmissionTab {
  return (Array.isArray(tab) ? tab[0] : tab) === 'trial' ? 'trial' : 'admission'
}

/** 已登记/已结束的试住可转正式入住；已转签约、已取消不可重复转。 */
export function canConvertTrial(status?: string) {
  return status === 'REGISTERED' || status === 'FINISHED'
}

/** 转正式入住时回写试住记录：保留原内容，只把状态改为已转签约（后端更新接口要求完整字段）。 */
export function toConvertedTrialRequest(record: TrialStayItem): TrialStayRequest {
  return {
    elderId: record.elderId,
    trialStartDate: record.trialStartDate,
    trialEndDate: record.trialEndDate,
    channel: record.channel || '',
    trialPackage: record.trialPackage || '',
    intentLevel: record.intentLevel || 'MEDIUM',
    status: 'CONVERTED',
    careLevel: record.careLevel || '',
    remark: record.remark || ''
  }
}
