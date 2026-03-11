import { onMounted, onUnmounted, ref } from 'vue';
import { getCardAccountPage } from '../api/card';
import { subscribeLiveSync } from '../utils/liveSync';
import { fuzzyScore, toPinyinInitials } from './entitySearch';
const cardAccountPoolCache = new Map();
const cardAccountPoolFetchedAt = new Map();
const CARD_ACCOUNT_POOL_CACHE_TTL = 90 * 1000;
function toCardAccountOption(item) {
    const elderName = item.elderName || '未命名长者';
    const cardNo = item.cardNo || `CARD#${item.id}`;
    return {
        label: `${cardNo} - ${elderName}`,
        value: String(item.id),
        cardNo,
        elderName
    };
}
function cardAccountSearchText(item) {
    const text = `${item.cardNo || ''} ${item.elderName || ''}`.trim();
    return `${text} ${toPinyinInitials(text)}`;
}
function dedupeCardAccounts(rows) {
    const map = new Map();
    rows.forEach((item) => {
        const id = String(item.id || '').trim();
        if (!id)
            return;
        if (!map.has(id))
            map.set(id, item);
    });
    return Array.from(map.values());
}
export function useCardAccountOptions(config = {}) {
    const pageSize = config.pageSize || 120;
    const preloadSize = config.preloadSize || Math.max(pageSize * 3, 300);
    const status = String(config.status || 'ACTIVE');
    const cacheKey = `card-account:${status || 'all'}`;
    const cardAccountOptions = ref([]);
    const cardAccountLoading = ref(false);
    const lastKeyword = ref('');
    async function loadBasePool(force = false) {
        const lastAt = cardAccountPoolFetchedAt.get(cacheKey) || 0;
        const hasFresh = cardAccountPoolCache.has(cacheKey) && (Date.now() - lastAt < CARD_ACCOUNT_POOL_CACHE_TTL);
        if (!force && hasFresh)
            return cardAccountPoolCache.get(cacheKey) || [];
        const page = await getCardAccountPage({ pageNo: 1, pageSize: preloadSize, status });
        const rows = page.list || [];
        cardAccountPoolCache.set(cacheKey, rows);
        cardAccountPoolFetchedAt.set(cacheKey, Date.now());
        return rows;
    }
    async function searchCardAccounts(keyword = '') {
        cardAccountLoading.value = true;
        try {
            lastKeyword.value = String(keyword || '');
            const text = String(keyword || '').trim();
            const baseRows = await loadBasePool(false);
            let mergedRows = [...baseRows];
            if (text) {
                const page = await getCardAccountPage({
                    pageNo: 1,
                    pageSize: Math.max(pageSize * 2, 120),
                    status,
                    keyword: text
                });
                mergedRows = dedupeCardAccounts([...(page.list || []), ...baseRows]);
            }
            const finalRows = text
                ? mergedRows
                    .map((item) => ({ item, score: fuzzyScore(cardAccountSearchText(item), text) }))
                    .filter((row) => row.score >= 0)
                    .sort((a, b) => b.score - a.score || String(a.item.cardNo || '').localeCompare(String(b.item.cardNo || ''), 'zh-CN'))
                    .slice(0, pageSize)
                    .map((row) => row.item)
                : mergedRows.slice(0, pageSize);
            cardAccountOptions.value = finalRows.map(toCardAccountOption);
        }
        finally {
            cardAccountLoading.value = false;
        }
    }
    function ensureSelectedCardAccount(cardAccountId, elderName, cardNo) {
        const targetId = String(cardAccountId || '').trim();
        if (!targetId || cardAccountOptions.value.some((item) => item.value === targetId))
            return;
        const safeCardNo = cardNo || `CARD#${targetId}`;
        const safeElderName = elderName || '-';
        cardAccountOptions.value.unshift({
            value: targetId,
            cardNo: safeCardNo,
            elderName: safeElderName,
            label: `${safeCardNo} - ${safeElderName}`
        });
    }
    function invalidateCardAccountCache() {
        cardAccountPoolCache.delete(cacheKey);
        cardAccountPoolFetchedAt.delete(cacheKey);
    }
    let unsubscribe = () => { };
    onMounted(() => {
        unsubscribe = subscribeLiveSync((payload) => {
            if (!payload.topics.some((topic) => topic === 'finance' || topic === 'elder' || topic === 'system'))
                return;
            invalidateCardAccountCache();
            if (!cardAccountOptions.value.length || cardAccountLoading.value)
                return;
            searchCardAccounts(lastKeyword.value).catch(() => { });
        });
    });
    onUnmounted(() => {
        unsubscribe();
    });
    return {
        cardAccountOptions,
        cardAccountLoading,
        searchCardAccounts,
        ensureSelectedCardAccount,
        invalidateCardAccountCache
    };
}
