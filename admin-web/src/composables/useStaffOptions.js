import { onMounted, onUnmounted, ref } from 'vue';
import { getStaffOptionPage } from '../api/rbac';
import { subscribeLiveSync } from '../utils/liveSync';
import { fuzzyScore, toPinyinInitials } from './entitySearch';
const staffPoolCache = new Map();
const staffPoolFetchedAt = new Map();
const STAFF_POOL_CACHE_TTL = 90 * 1000;
function safeStaffDisplayName(name, username, fallbackId) {
    const realName = String(name || '').trim();
    if (realName)
        return realName;
    const loginName = String(username || '').trim();
    if (loginName)
        return loginName;
    if (fallbackId != null && String(fallbackId).trim()) {
        return `未识别员工(${String(fallbackId).trim()})`;
    }
    return '未识别员工';
}
function findCachedStaff(staffId) {
    if (staffId == null || staffId === '')
        return undefined;
    const target = String(staffId);
    for (const rows of staffPoolCache.values()) {
        const hit = (rows || []).find((item) => String(item.id) === target);
        if (hit)
            return hit;
    }
    return undefined;
}
function toStaffOption(item) {
    const name = safeStaffDisplayName(item.realName, item.username, item.id);
    const suffix = item.staffNo ? ` (${item.staffNo})` : '';
    return {
        label: `${name}${suffix}`,
        value: String(item.id),
        name,
        username: String(item.username || '').trim() || undefined,
        departmentId: item.departmentId == null ? undefined : String(item.departmentId),
        roleCodes: Array.isArray(item.roleCodes) ? item.roleCodes.map((code) => String(code || '').trim().toUpperCase()).filter(Boolean) : []
    };
}
function staffSearchText(item) {
    const text = `${item.realName || ''} ${item.username || ''} ${item.staffNo || ''} ${item.phone || ''}`.trim();
    return `${text} ${toPinyinInitials(text)}`;
}
function dedupeStaff(rows) {
    const map = new Map();
    rows.forEach((item) => {
        const key = String(item.id);
        if (!map.has(key))
            map.set(key, item);
    });
    return Array.from(map.values());
}
export function useStaffOptions(config = {}) {
    const pageSize = config.pageSize || 80;
    const preloadSize = config.preloadSize || Math.max(pageSize * 4, 300);
    const staffOptions = ref([]);
    const staffLoading = ref(false);
    const lastKeyword = ref('');
    async function loadBasePool(force = false) {
        const cacheKey = 'staff-all';
        const lastAt = staffPoolFetchedAt.get(cacheKey) || 0;
        const hasFresh = staffPoolCache.has(cacheKey) && (Date.now() - lastAt < STAFF_POOL_CACHE_TTL);
        if (!force && hasFresh)
            return staffPoolCache.get(cacheKey) || [];
        const page = await getStaffOptionPage({ pageNo: 1, pageSize: preloadSize, activeOnly: true });
        const rows = page.list || [];
        staffPoolCache.set(cacheKey, rows);
        staffPoolFetchedAt.set(cacheKey, Date.now());
        return rows;
    }
    async function searchStaff(keyword = '') {
        staffLoading.value = true;
        try {
            lastKeyword.value = String(keyword || '');
            const text = String(keyword || '').trim();
            const baseRows = await loadBasePool(false);
            let mergedRows = [...baseRows];
            if (text) {
                const page = await getStaffOptionPage({
                    pageNo: 1,
                    pageSize: Math.max(pageSize * 2, 120),
                    keyword: text,
                    activeOnly: true
                });
                mergedRows = dedupeStaff([...(page.list || []), ...baseRows]);
            }
            const finalRows = text
                ? mergedRows
                    .map((item) => ({ item, score: fuzzyScore(staffSearchText(item), text) }))
                    .filter((row) => row.score >= 0)
                    .sort((a, b) => b.score - a.score || String(a.item.realName || a.item.username || '').localeCompare(String(b.item.realName || b.item.username || ''), 'zh-CN'))
                    .slice(0, pageSize)
                    .map((row) => row.item)
                : mergedRows.slice(0, pageSize);
            staffOptions.value = finalRows.map(toStaffOption);
        }
        finally {
            staffLoading.value = false;
        }
    }
    function ensureSelectedStaff(staffId, staffName) {
        if (staffId == null || staffId === '')
            return;
        const value = String(staffId);
        if (staffOptions.value.some((item) => item.value === value))
            return;
        const cached = findCachedStaff(value);
        const name = safeStaffDisplayName(staffName || cached?.realName, cached?.username, value);
        const suffix = cached?.staffNo ? ` (${cached.staffNo})` : '';
        staffOptions.value.unshift({
            label: `${name}${suffix}`,
            value,
            name,
            username: String(cached?.username || '').trim() || undefined,
            departmentId: cached?.departmentId == null ? undefined : String(cached.departmentId),
            roleCodes: Array.isArray(cached?.roleCodes)
                ? cached.roleCodes.map((code) => String(code || '').trim().toUpperCase()).filter(Boolean)
                : [],
        });
    }
    function findStaffName(staffId) {
        if (staffId == null || staffId === '')
            return '';
        const optionName = staffOptions.value.find((item) => item.value === String(staffId))?.name;
        if (optionName)
            return optionName;
        const cached = findCachedStaff(staffId);
        return safeStaffDisplayName(cached?.realName, cached?.username, staffId);
    }
    function invalidateStaffCache() {
        const cacheKey = 'staff-all';
        staffPoolCache.delete(cacheKey);
        staffPoolFetchedAt.delete(cacheKey);
    }
    let unsubscribe = () => { };
    onMounted(() => {
        unsubscribe = subscribeLiveSync((payload) => {
            if (!payload.topics.some((topic) => topic === 'hr' || topic === 'system' || topic === 'oa'))
                return;
            invalidateStaffCache();
            if (!staffOptions.value.length || staffLoading.value)
                return;
            searchStaff(lastKeyword.value).catch(() => { });
        });
    });
    onUnmounted(() => {
        unsubscribe();
    });
    return {
        staffOptions,
        staffLoading,
        searchStaff,
        ensureSelectedStaff,
        findStaffName,
        invalidateStaffCache
    };
}
