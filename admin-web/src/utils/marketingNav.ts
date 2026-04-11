import type { RouteLocationRaw } from 'vue-router'

type LeadTab = 'consultation' | 'intent' | 'callback'
type LeadStage = 'consultation' | 'evaluation' | 'signing' | 'admission' | 'lost'
type FollowupEntry = 'records' | 'due' | 'today' | 'overdue'
type ReservationEntry = 'records' | 'lock' | 'expiring' | 'panorama'
type ContractEntry = 'pending' | 'signed' | 'renewal' | 'change' | 'attachments'
type FunnelEntry = 'consultation' | 'evaluation' | 'signing' | 'admission' | 'lost'
type CallbackEntry = 'checkin' | 'trial' | 'discharge' | 'score'

export type MarketingLeadQuery = {
  tab?: LeadTab
  stage?: LeadStage
  source?: string
  filter?: string
  scenario?: string
  consultDateFrom?: string
  consultDateTo?: string
  elderName?: string
  elderPhone?: string
  marketerName?: string
}

function compactQuery(query: Record<string, unknown>) {
  return Object.fromEntries(
    Object.entries(query).filter(([, value]) => value !== undefined && value !== null && String(value) !== '')
  )
}

export function buildLeadRoute(entry: 'all' | 'intent' | 'invalid' | 'blacklist' | 'unknown-source' | 'medical-transfer', query?: MarketingLeadQuery): RouteLocationRaw {
  return {
    path: `/marketing/leads/${entry}`,
    query: compactQuery(query || {})
  }
}

export function buildFollowupRoute(entry: FollowupEntry, query?: Record<string, unknown>): RouteLocationRaw {
  return {
    path: `/marketing/interactions/${entry}`,
    query: compactQuery(query || {})
  }
}

export function buildReservationRoute(entry: ReservationEntry, query?: Record<string, unknown>): RouteLocationRaw {
  return {
    path: `/marketing/reservation/${entry}`,
    query: compactQuery(query || {})
  }
}

export function buildContractRoute(entry: ContractEntry, query?: Record<string, unknown>): RouteLocationRaw {
  return {
    path: `/marketing/contracts/${entry}`,
    query: compactQuery(query || {})
  }
}

export function buildFunnelRoute(entry: FunnelEntry, query?: Record<string, unknown>): RouteLocationRaw {
  return {
    path: `/marketing/funnel/${entry}`,
    query: compactQuery(query || {})
  }
}

export function buildCallbackRoute(entry: CallbackEntry, query?: Record<string, unknown>): RouteLocationRaw {
  return {
    path: `/marketing/callback/${entry}`,
    query: compactQuery(query || {})
  }
}

export function routeToUnknownSourceOrAll(source?: string): RouteLocationRaw {
  const sourceText = String(source || '').trim()
  if (!sourceText || sourceText === '未标记渠道' || sourceText.includes('不明')) {
    return buildLeadRoute('unknown-source')
  }
  return buildLeadRoute('all', { source: sourceText })
}
