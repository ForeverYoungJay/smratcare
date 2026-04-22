import { ref } from 'vue';
import { getElderDiseases } from '../api/elder';
import { getDiseaseList } from '../api/store';
function normalizeDiseaseIds(raw) {
    if (!Array.isArray(raw))
        return [];
    return raw
        .map((item) => {
        if (typeof item === 'number')
            return item;
        if (typeof item === 'string' && item.trim())
            return Number(item);
        if (item && typeof item === 'object') {
            const diseaseId = Number(item.diseaseId ?? item.id);
            return Number.isFinite(diseaseId) ? diseaseId : null;
        }
        return null;
    })
        .filter((item) => Number.isFinite(item) && Number(item) > 0);
}
export function useElderDiseaseCatalog() {
    const diseaseOptions = ref([]);
    const diseaseNameMap = ref({});
    const catalogLoading = ref(false);
    let catalogPromise = null;
    async function ensureDiseaseCatalogLoaded(force = false) {
        if (!force && diseaseOptions.value.length)
            return;
        if (catalogPromise)
            return catalogPromise;
        catalogLoading.value = true;
        catalogPromise = (async () => {
            const list = await getDiseaseList();
            diseaseOptions.value = (list || []).map((item) => ({
                label: String(item.diseaseName || item.diseaseCode || `疾病#${item.id}`),
                value: Number(item.id)
            }));
            diseaseNameMap.value = diseaseOptions.value.reduce((acc, item) => {
                acc[item.value] = item.label;
                return acc;
            }, {});
        })();
        try {
            await catalogPromise;
        }
        finally {
            catalogLoading.value = false;
            catalogPromise = null;
        }
    }
    async function loadDiseaseState(elderId) {
        await ensureDiseaseCatalogLoaded();
        if (!elderId) {
            return { ids: [], labels: [] };
        }
        const raw = await getElderDiseases(elderId);
        const ids = normalizeDiseaseIds(raw);
        const labels = ids.map((item) => diseaseNameMap.value[item] || `疾病#${item}`);
        return { ids, labels };
    }
    async function loadDiseaseSummaryMap(elderIds) {
        await ensureDiseaseCatalogLoaded();
        const uniqueIds = Array.from(new Set((elderIds || []).map((item) => String(item || '')).filter(Boolean)));
        const summaryMap = {};
        const results = await Promise.allSettled(uniqueIds.map(async (elderId) => {
            const state = await loadDiseaseState(elderId);
            return { elderId, labels: state.labels };
        }));
        results.forEach((result) => {
            if (result.status !== 'fulfilled')
                return;
            summaryMap[result.value.elderId] = result.value.labels;
        });
        return summaryMap;
    }
    return {
        diseaseOptions,
        diseaseNameMap,
        catalogLoading,
        ensureDiseaseCatalogLoaded,
        loadDiseaseState,
        loadDiseaseSummaryMap
    };
}
