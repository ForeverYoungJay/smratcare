import type { LocationQuery, RouteLocationRaw } from 'vue-router'

/**
 * 营销模块收敛为 5 个主页面后，旧菜单地址统一跳到新页面的对应标签。
 * 保留原有查询参数（筛选条件、客户 ID 等），新页面按 tab / scenario / view 打开对应视图。
 */
export type MarketingPage = 'customers' | 'visits' | 'operations'

const LEGACY: Record<string, [MarketingPage, Record<string, string>]> = {
  'leads/all': ['customers', {}],
  'leads/intent': ['customers', { tab: 'intent' }],
  'leads/invalid': ['customers', { tab: 'invalid' }],
  'leads/blacklist': ['customers', { tab: 'invalid', scenario: 'blacklist' }],
  'leads/unknown-source': ['customers', { scenario: 'source_unknown' }],
  'leads/medical-transfer': ['customers', { scenario: 'source_medical' }],
  'interactions/records': ['customers', { tab: 'followup' }],
  'interactions/today': ['customers', { tab: 'followup', view: 'today' }],
  'interactions/due': ['customers', { tab: 'followup', view: 'due' }],
  'interactions/overdue': ['customers', { tab: 'followup', view: 'overdue' }],
  'funnel/consultation': ['customers', {}],
  'funnel/evaluation': ['customers', { tab: 'intent' }],
  'funnel/lost': ['customers', { tab: 'invalid' }],
  'callback/checkin': ['customers', { tab: 'followup', callbackType: 'checkin' }],
  'callback/trial': ['customers', { tab: 'followup', callbackType: 'trial' }],
  'callback/discharge': ['customers', { tab: 'followup', callbackType: 'discharge' }],
  'reservation/visits': ['visits', {}],
  'reservation/records': ['visits', { tab: 'reservation' }],
  'reservation/lock': ['visits', { tab: 'reservation', scenario: 'lock_bed' }],
  'reservation/expiring': ['visits', { tab: 'reservation', scenario: 'expiring_lock' }],
  plan: ['operations', { tab: 'plans' }]
}

export function legacyMarketingRedirect(key: string, query: LocationQuery = {}): RouteLocationRaw {
  const [page, preset] = LEGACY[key] || ['customers', {}]
  // 旧地址自带的参数优先级低于映射出的视图参数，避免旧的 tab 值把视图带偏
  return { path: `/marketing/${page}`, query: { ...query, ...preset } }
}

export const legacyMarketingKeys = Object.keys(LEGACY)
