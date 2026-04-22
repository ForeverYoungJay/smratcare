import dayjs from 'dayjs';
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
function buildKey(pageKey, suffix) {
    return `stats-command:${pageKey}:${suffix}`;
}
function safeJsonParse(raw, fallback) {
    if (!raw) {
        return fallback;
    }
    try {
        return JSON.parse(raw);
    }
    catch {
        return fallback;
    }
}
function persist(pageKey, suffix, value) {
    const storage = getStorage();
    if (!storage) {
        return;
    }
    try {
        storage.setItem(buildKey(pageKey, suffix), JSON.stringify(value));
    }
    catch {
        // ignore storage failures
    }
}
function uid(prefix) {
    return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
}
export function listStatsReportTemplates(pageKey) {
    const storage = getStorage();
    return safeJsonParse(storage?.getItem(buildKey(pageKey, 'report-templates')) || null, []);
}
export function saveStatsReportTemplate(pageKey, record) {
    const next = {
        id: uid('report-template'),
        updatedAt: dayjs().toISOString(),
        ...record
    };
    const rows = [next, ...listStatsReportTemplates(pageKey)].slice(0, 12);
    persist(pageKey, 'report-templates', rows);
    return rows;
}
export function removeStatsReportTemplate(pageKey, id) {
    const rows = listStatsReportTemplates(pageKey).filter((item) => item.id !== id);
    persist(pageKey, 'report-templates', rows);
    return rows;
}
export function loadStatsPanelConfig(pageKey, fallback = []) {
    const storage = getStorage();
    const config = safeJsonParse(storage?.getItem(buildKey(pageKey, 'panel-config')) || null, { selectedKeys: fallback });
    return Array.isArray(config.selectedKeys) && config.selectedKeys.length ? config.selectedKeys : fallback;
}
export function saveStatsPanelConfig(pageKey, selectedKeys) {
    const payload = {
        selectedKeys: [...selectedKeys],
        updatedAt: dayjs().toISOString()
    };
    persist(pageKey, 'panel-config', payload);
    return payload;
}
export function listStatsFeedback(pageKey) {
    const storage = getStorage();
    return safeJsonParse(storage?.getItem(buildKey(pageKey, 'feedback')) || null, []);
}
export function saveStatsFeedback(pageKey, record) {
    const next = {
        id: uid('feedback'),
        createdAt: dayjs().toISOString(),
        status: 'OPEN',
        ...record
    };
    const rows = [next, ...listStatsFeedback(pageKey)].slice(0, 20);
    persist(pageKey, 'feedback', rows);
    return rows;
}
