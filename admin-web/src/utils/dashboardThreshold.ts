export interface ThresholdSnapshot {
  abnormalTaskThreshold: number
  inventoryAlertThreshold: number
  bedOccupancyThreshold: number
  revenueDropThreshold: number
}

export interface ThresholdChangeLog extends ThresholdSnapshot {
  changedAt: string
  source: 'dashboard' | 'portal'
}

const THRESHOLD_STORAGE_KEY_PREFIX = 'dashboard_threshold_config_v1_'
const THRESHOLD_LOG_KEY_PREFIX = 'dashboard_threshold_log_v1_'
const THRESHOLD_PULSE_KEY = 'dashboard_threshold_pulse_v1'
const THRESHOLD_LOG_LIMIT = 20

export function thresholdStorageKey(staffId: string | number | undefined) {
  return `${THRESHOLD_STORAGE_KEY_PREFIX}${String(staffId || 'default')}`
}

export function thresholdLogKey(staffId: string | number | undefined) {
  return `${THRESHOLD_LOG_KEY_PREFIX}${String(staffId || 'default')}`
}

export function thresholdPulseKey() {
  return THRESHOLD_PULSE_KEY
}

export function loadThresholdSnapshot(staffId: string | number | undefined) {
  try {
    const raw = localStorage.getItem(thresholdStorageKey(staffId))
    if (!raw) return null
    const parsed = JSON.parse(raw) as Partial<ThresholdSnapshot>
    if (!parsed || typeof parsed !== 'object') return null
    return {
      abnormalTaskThreshold: Number(parsed.abnormalTaskThreshold || 0),
      inventoryAlertThreshold: Number(parsed.inventoryAlertThreshold || 0),
      bedOccupancyThreshold: Number(parsed.bedOccupancyThreshold || 0),
      revenueDropThreshold: Number(parsed.revenueDropThreshold || 0)
    } as ThresholdSnapshot
  } catch {
    return null
  }
}

export function clearThresholdSnapshot(staffId: string | number | undefined) {
  try {
    localStorage.removeItem(thresholdStorageKey(staffId))
  } catch {
  }
}

export function loadThresholdLogs(staffId: string | number | undefined) {
  try {
    const raw = localStorage.getItem(thresholdLogKey(staffId))
    if (!raw) return [] as ThresholdChangeLog[]
    const parsed = JSON.parse(raw) as ThresholdChangeLog[]
    if (!Array.isArray(parsed)) return [] as ThresholdChangeLog[]
    return parsed
  } catch {
    return [] as ThresholdChangeLog[]
  }
}

export function saveThresholdSnapshot(
  staffId: string | number | undefined,
  snapshot: ThresholdSnapshot,
  source: 'dashboard' | 'portal'
) {
  const safeSnapshot: ThresholdSnapshot = {
    abnormalTaskThreshold: Number(snapshot.abnormalTaskThreshold || 0),
    inventoryAlertThreshold: Number(snapshot.inventoryAlertThreshold || 0),
    bedOccupancyThreshold: Number(snapshot.bedOccupancyThreshold || 0),
    revenueDropThreshold: Number(snapshot.revenueDropThreshold || 0)
  }

  try {
    localStorage.setItem(thresholdStorageKey(staffId), JSON.stringify(safeSnapshot))
    const nextLog: ThresholdChangeLog = {
      ...safeSnapshot,
      source,
      changedAt: new Date().toISOString()
    }
    const logs = [nextLog, ...loadThresholdLogs(staffId)].slice(0, THRESHOLD_LOG_LIMIT)
    localStorage.setItem(thresholdLogKey(staffId), JSON.stringify(logs))
    localStorage.setItem(THRESHOLD_PULSE_KEY, `${Date.now()}`)
    return logs
  } catch {
    return [] as ThresholdChangeLog[]
  }
}
