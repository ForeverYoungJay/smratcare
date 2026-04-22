import dayjs from 'dayjs';
function safeStorageKey(pageKey, suffix) {
    return `stats-workbench:${pageKey}:${suffix}`;
}
function getStorage() {
    if (typeof window === 'undefined') {
        return null;
    }
    try {
        return window.localStorage;
    }
    catch {
        return null;
    }
}
function safeJsonParse(raw, fallback) {
    if (!raw)
        return fallback;
    try {
        return JSON.parse(raw);
    }
    catch {
        return fallback;
    }
}
function persist(key, value) {
    const storage = getStorage();
    if (!storage) {
        return;
    }
    try {
        storage.setItem(key, JSON.stringify(value));
    }
    catch {
        // ignore storage failures
    }
}
function uid(prefix) {
    return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
}
export function listStatsPresets(pageKey) {
    const storage = getStorage();
    return safeJsonParse(storage?.getItem(safeStorageKey(pageKey, 'presets')) || null, []);
}
export function saveStatsPreset(pageKey, record) {
    const next = {
        id: uid('preset'),
        updatedAt: dayjs().toISOString(),
        ...record
    };
    const rows = [next, ...listStatsPresets(pageKey)].slice(0, 12);
    persist(safeStorageKey(pageKey, 'presets'), rows);
    return rows;
}
export function removeStatsPreset(pageKey, id) {
    const rows = listStatsPresets(pageKey).filter((item) => item.id !== id);
    persist(safeStorageKey(pageKey, 'presets'), rows);
    return rows;
}
export function loadStatsTargets(pageKey, fallback = {}) {
    const storage = getStorage();
    return {
        ...fallback,
        ...safeJsonParse(storage?.getItem(safeStorageKey(pageKey, 'targets')) || null, {})
    };
}
export function saveStatsTargets(pageKey, payload) {
    persist(safeStorageKey(pageKey, 'targets'), payload);
    return payload;
}
export function listStatsSubscriptions(pageKey) {
    const storage = getStorage();
    return safeJsonParse(storage?.getItem(safeStorageKey(pageKey, 'subscriptions')) || null, []);
}
export function saveStatsSubscription(pageKey, record) {
    const next = {
        id: uid('subscription'),
        updatedAt: dayjs().toISOString(),
        ...record
    };
    const rows = [next, ...listStatsSubscriptions(pageKey)].slice(0, 8);
    persist(safeStorageKey(pageKey, 'subscriptions'), rows);
    return rows;
}
export function removeStatsSubscription(pageKey, id) {
    const rows = listStatsSubscriptions(pageKey).filter((item) => item.id !== id);
    persist(safeStorageKey(pageKey, 'subscriptions'), rows);
    return rows;
}
export function toggleStatsSubscription(pageKey, id, enabled) {
    const rows = listStatsSubscriptions(pageKey).map((item) => item.id === id ? { ...item, enabled, updatedAt: dayjs().toISOString() } : item);
    persist(safeStorageKey(pageKey, 'subscriptions'), rows);
    return rows;
}
export function nextSubscriptionRunText(record) {
    const now = dayjs();
    let next = now.hour(record.hour).minute(record.minute).second(0).millisecond(0);
    if (record.frequency === 'DAILY') {
        if (!next.isAfter(now))
            next = next.add(1, 'day');
    }
    else if (record.frequency === 'WEEKLY') {
        if (!next.isAfter(now))
            next = next.add(7, 'day');
    }
    else if (record.frequency === 'MONTHLY') {
        if (!next.isAfter(now))
            next = next.add(1, 'month');
    }
    return next.format('MM-DD HH:mm');
}
