import dayjs from 'dayjs'

export interface StatsReportTemplateRecord {
  id: string
  name: string
  summary?: string
  columns: string[]
  payload: Record<string, any>
  updatedAt: string
}

export interface StatsPanelConfig {
  selectedKeys: string[]
  updatedAt?: string
}

export interface StatsFeedbackRecord {
  id: string
  title: string
  detail: string
  urgency: 'LOW' | 'MEDIUM' | 'HIGH'
  status: 'OPEN' | 'TRACKING'
  scope?: string
  createdAt: string
}

function getStorage() {
  if (typeof window === 'undefined') {
    return null
  }
  try {
    return window.localStorage
  } catch {
    return null
  }
}

function buildKey(pageKey: string, suffix: string) {
  return `stats-command:${pageKey}:${suffix}`
}

function safeJsonParse<T>(raw: string | null, fallback: T): T {
  if (!raw) {
    return fallback
  }
  try {
    return JSON.parse(raw) as T
  } catch {
    return fallback
  }
}

function persist<T>(pageKey: string, suffix: string, value: T) {
  const storage = getStorage()
  if (!storage) {
    return
  }
  try {
    storage.setItem(buildKey(pageKey, suffix), JSON.stringify(value))
  } catch {
    // ignore storage failures
  }
}

function uid(prefix: string) {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}

export function listStatsReportTemplates(pageKey: string) {
  const storage = getStorage()
  return safeJsonParse<StatsReportTemplateRecord[]>(storage?.getItem(buildKey(pageKey, 'report-templates')) || null, [])
}

export function saveStatsReportTemplate(
  pageKey: string,
  record: Omit<StatsReportTemplateRecord, 'id' | 'updatedAt'>
) {
  const next: StatsReportTemplateRecord = {
    id: uid('report-template'),
    updatedAt: dayjs().toISOString(),
    ...record
  }
  const rows = [next, ...listStatsReportTemplates(pageKey)].slice(0, 12)
  persist(pageKey, 'report-templates', rows)
  return rows
}

export function removeStatsReportTemplate(pageKey: string, id: string) {
  const rows = listStatsReportTemplates(pageKey).filter((item) => item.id !== id)
  persist(pageKey, 'report-templates', rows)
  return rows
}

export function loadStatsPanelConfig(pageKey: string, fallback: string[] = []) {
  const storage = getStorage()
  const config = safeJsonParse<StatsPanelConfig>(
    storage?.getItem(buildKey(pageKey, 'panel-config')) || null,
    { selectedKeys: fallback }
  )
  return Array.isArray(config.selectedKeys) && config.selectedKeys.length ? config.selectedKeys : fallback
}

export function saveStatsPanelConfig(pageKey: string, selectedKeys: string[]) {
  const payload: StatsPanelConfig = {
    selectedKeys: [...selectedKeys],
    updatedAt: dayjs().toISOString()
  }
  persist(pageKey, 'panel-config', payload)
  return payload
}

export function listStatsFeedback(pageKey: string) {
  const storage = getStorage()
  return safeJsonParse<StatsFeedbackRecord[]>(storage?.getItem(buildKey(pageKey, 'feedback')) || null, [])
}

export function saveStatsFeedback(
  pageKey: string,
  record: Omit<StatsFeedbackRecord, 'id' | 'createdAt' | 'status'>
) {
  const next: StatsFeedbackRecord = {
    id: uid('feedback'),
    createdAt: dayjs().toISOString(),
    status: 'OPEN',
    ...record
  }
  const rows = [next, ...listStatsFeedback(pageKey)].slice(0, 20)
  persist(pageKey, 'feedback', rows)
  return rows
}
