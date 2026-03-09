import { onMounted, onUnmounted, ref } from 'vue';
import { getDepartmentOptionPage } from '../api/rbac';
import { subscribeLiveSync } from '../utils/liveSync';
import { fuzzyScore, toPinyinInitials } from './entitySearch';
const departmentPoolCache = new Map();
const departmentPoolFetchedAt = new Map();
const DEPARTMENT_POOL_CACHE_TTL = 120 * 1000;
function toDepartmentOption(item) {
    const name = item.deptName || `部门#${item.id}`;
    const suffix = item.deptCode ? ` (${item.deptCode})` : '';
    return {
        label: `${name}${suffix}`,
        value: String(item.id),
        name
    };
}
function departmentSearchText(item) {
    const text = `${item.deptName || ''} ${item.deptCode || ''}`.trim();
    return `${text} ${toPinyinInitials(text)}`;
}
function dedupeDepartments(rows) {
    const map = new Map();
    rows.forEach((item) => {
        const key = String(item.id);
        if (!map.has(key))
            map.set(key, item);
    });
    return Array.from(map.values());
}
export function useDepartmentOptions(config = {}) {
    const pageSize = config.pageSize || 120;
    const preloadSize = config.preloadSize || 500;
    const departmentOptions = ref([]);
    const departmentLoading = ref(false);
    const lastKeyword = ref('');
    async function loadBasePool(force = false) {
        const cacheKey = 'department-all';
        const lastAt = departmentPoolFetchedAt.get(cacheKey) || 0;
        const hasFresh = departmentPoolCache.has(cacheKey) && (Date.now() - lastAt < DEPARTMENT_POOL_CACHE_TTL);
        if (!force && hasFresh)
            return departmentPoolCache.get(cacheKey) || [];
        const page = await getDepartmentOptionPage({ pageNo: 1, pageSize: preloadSize, activeOnly: true });
        const rows = page.list || [];
        departmentPoolCache.set(cacheKey, rows);
        departmentPoolFetchedAt.set(cacheKey, Date.now());
        return rows;
    }
    async function searchDepartments(keyword = '') {
        departmentLoading.value = true;
        try {
            lastKeyword.value = String(keyword || '');
            const text = String(keyword || '').trim();
            const baseRows = await loadBasePool(false);
            let mergedRows = [...baseRows];
            if (text) {
                const page = await getDepartmentOptionPage({
                    pageNo: 1,
                    pageSize: Math.max(pageSize * 2, 200),
                    keyword: text,
                    activeOnly: true
                });
                mergedRows = dedupeDepartments([...(page.list || []), ...baseRows]);
            }
            const finalRows = text
                ? mergedRows
                    .map((item) => ({ item, score: fuzzyScore(departmentSearchText(item), text) }))
                    .filter((row) => row.score >= 0)
                    .sort((a, b) => b.score - a.score || String(a.item.deptName || '').localeCompare(String(b.item.deptName || ''), 'zh-CN'))
                    .slice(0, pageSize)
                    .map((row) => row.item)
                : mergedRows.slice(0, pageSize);
            departmentOptions.value = finalRows.map(toDepartmentOption);
        }
        finally {
            departmentLoading.value = false;
        }
    }
    function ensureSelectedDepartment(deptId, deptName) {
        if (deptId == null || deptId === '')
            return;
        const value = String(deptId);
        if (departmentOptions.value.some((item) => item.value === value))
            return;
        const name = deptName || `部门#${value}`;
        departmentOptions.value.unshift({
            label: name,
            value,
            name
        });
    }
    function invalidateDepartmentCache() {
        const cacheKey = 'department-all';
        departmentPoolCache.delete(cacheKey);
        departmentPoolFetchedAt.delete(cacheKey);
    }
    let unsubscribe = () => { };
    onMounted(() => {
        unsubscribe = subscribeLiveSync((payload) => {
            if (!payload.topics.some((topic) => topic === 'system' || topic === 'hr' || topic === 'oa'))
                return;
            invalidateDepartmentCache();
            if (!departmentOptions.value.length || departmentLoading.value)
                return;
            searchDepartments(lastKeyword.value).catch(() => { });
        });
    });
    onUnmounted(() => {
        unsubscribe();
    });
    return {
        departmentOptions,
        departmentLoading,
        searchDepartments,
        ensureSelectedDepartment,
        invalidateDepartmentCache
    };
}
