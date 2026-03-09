import { toThresholdSnapshot } from './dashboardQuery';
const THRESHOLD_STORAGE_KEY_PREFIX = 'dashboard_threshold_config_v1_';
const THRESHOLD_LOG_KEY_PREFIX = 'dashboard_threshold_log_v1_';
const THRESHOLD_PULSE_KEY = 'dashboard_threshold_pulse_v1';
const THRESHOLD_LOG_LIMIT = 20;
export function thresholdStorageKey(staffId) {
    return `${THRESHOLD_STORAGE_KEY_PREFIX}${String(staffId || 'default')}`;
}
export function thresholdLogKey(staffId) {
    return `${THRESHOLD_LOG_KEY_PREFIX}${String(staffId || 'default')}`;
}
export function thresholdPulseKey() {
    return THRESHOLD_PULSE_KEY;
}
export function loadThresholdSnapshot(staffId) {
    try {
        const raw = localStorage.getItem(thresholdStorageKey(staffId));
        if (!raw)
            return null;
        const parsed = JSON.parse(raw);
        if (!parsed || typeof parsed !== 'object')
            return null;
        return toThresholdSnapshot(parsed);
    }
    catch {
        return null;
    }
}
export function clearThresholdSnapshot(staffId) {
    try {
        localStorage.removeItem(thresholdStorageKey(staffId));
    }
    catch {
    }
}
export function loadThresholdLogs(staffId) {
    try {
        const raw = localStorage.getItem(thresholdLogKey(staffId));
        if (!raw)
            return [];
        const parsed = JSON.parse(raw);
        if (!Array.isArray(parsed))
            return [];
        return parsed;
    }
    catch {
        return [];
    }
}
export function saveThresholdSnapshot(staffId, snapshot, source) {
    const safeSnapshot = toThresholdSnapshot(snapshot);
    try {
        localStorage.setItem(thresholdStorageKey(staffId), JSON.stringify(safeSnapshot));
        const nextLog = {
            ...safeSnapshot,
            source,
            changedAt: new Date().toISOString()
        };
        const logs = [nextLog, ...loadThresholdLogs(staffId)].slice(0, THRESHOLD_LOG_LIMIT);
        localStorage.setItem(thresholdLogKey(staffId), JSON.stringify(logs));
        localStorage.setItem(THRESHOLD_PULSE_KEY, `${Date.now()}`);
        return logs;
    }
    catch {
        return [];
    }
}
