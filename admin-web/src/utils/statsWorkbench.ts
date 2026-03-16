import dayjs from 'dayjs'

export interface StatsPresetRecord {
  id: string
  name: string
  summary?: string
  payload: Record<string, any>
  updatedAt: string
}

export interface StatsTargetRecord {
  [key: string]: number
}

export interface StatsSubscriptionRecord {
  id: string
  name: string
  frequency: 'DAILY' | 'WEEKLY' | 'MONTHLY'
  hour: number
  minute: number
  recipient: string
  channel: '站内提醒' | '导出提醒'
  enabled: boolean
  note?: string
  updatedAt: string
}

function safeStorageKey(pageKey: string, suffix: string) {
  return `stats-workbench:${pageKey}:${suffix}`
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

function safeJsonParse<T>(raw: string | null, fallback: T): T {
  if (!raw) return fallback
  try {
    return JSON.parse(raw) as T
  } catch {
    return fallback
  }
}

function persist<T>(key: string, value: T) {
  const storage = getStorage()
  if (!storage) {
    return
  }
  try {
    storage.setItem(key, JSON.stringify(value))
  } catch {
    // ignore storage failures
  }
}

function uid(prefix: string) {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}

export function listStatsPresets(pageKey: string) {
  const storage = getStorage()
  return safeJsonParse<StatsPresetRecord[]>(storage?.getItem(safeStorageKey(pageKey, 'presets')) || null, [])
}

export function saveStatsPreset(pageKey: string, record: Omit<StatsPresetRecord, 'id' | 'updatedAt'>) {
  const next: StatsPresetRecord = {
    id: uid('preset'),
    updatedAt: dayjs().toISOString(),
    ...record
  }
  const rows = [next, ...listStatsPresets(pageKey)].slice(0, 12)
  persist(safeStorageKey(pageKey, 'presets'), rows)
  return rows
}

export function removeStatsPreset(pageKey: string, id: string) {
  const rows = listStatsPresets(pageKey).filter((item) => item.id !== id)
  persist(safeStorageKey(pageKey, 'presets'), rows)
  return rows
}

export function loadStatsTargets(pageKey: string, fallback: StatsTargetRecord = {}) {
  const storage = getStorage()
  return {
    ...fallback,
    ...safeJsonParse<StatsTargetRecord>(storage?.getItem(safeStorageKey(pageKey, 'targets')) || null, {})
  }
}

export function saveStatsTargets(pageKey: string, payload: StatsTargetRecord) {
  persist(safeStorageKey(pageKey, 'targets'), payload)
  return payload
}

export function listStatsSubscriptions(pageKey: string) {
  const storage = getStorage()
  return safeJsonParse<StatsSubscriptionRecord[]>(storage?.getItem(safeStorageKey(pageKey, 'subscriptions')) || null, [])
}

export function saveStatsSubscription(pageKey: string, record: Omit<StatsSubscriptionRecord, 'id' | 'updatedAt'>) {
  const next: StatsSubscriptionRecord = {
    id: uid('subscription'),
    updatedAt: dayjs().toISOString(),
    ...record
  }
  const rows = [next, ...listStatsSubscriptions(pageKey)].slice(0, 8)
  persist(safeStorageKey(pageKey, 'subscriptions'), rows)
  return rows
}

export function removeStatsSubscription(pageKey: string, id: string) {
  const rows = listStatsSubscriptions(pageKey).filter((item) => item.id !== id)
  persist(safeStorageKey(pageKey, 'subscriptions'), rows)
  return rows
}

export function toggleStatsSubscription(pageKey: string, id: string, enabled: boolean) {
  const rows = listStatsSubscriptions(pageKey).map((item) => item.id === id ? { ...item, enabled, updatedAt: dayjs().toISOString() } : item)
  persist(safeStorageKey(pageKey, 'subscriptions'), rows)
  return rows
}

export function nextSubscriptionRunText(record: StatsSubscriptionRecord) {
  const now = dayjs()
  let next = now.hour(record.hour).minute(record.minute).second(0).millisecond(0)
  if (record.frequency === 'DAILY') {
    if (!next.isAfter(now)) next = next.add(1, 'day')
  } else if (record.frequency === 'WEEKLY') {
    if (!next.isAfter(now)) next = next.add(7, 'day')
  } else if (record.frequency === 'MONTHLY') {
    if (!next.isAfter(now)) next = next.add(1, 'month')
  }
  return next.format('MM-DD HH:mm')
}
