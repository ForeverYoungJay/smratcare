import { ref } from 'vue';
import { getElderPage } from '../api/elder';
function toElderOption(item) {
    const name = item.fullName || `Elder#${item.id}`;
    const suffix = item.elderCode ? ` (${item.elderCode})` : '';
    return {
        label: `${name}${suffix}`,
        value: item.id,
        name
    };
}
export function useElderOptions(config = {}) {
    const pageSize = config.pageSize || 50;
    const elderOptions = ref([]);
    const elderLoading = ref(false);
    async function searchElders(keyword = '') {
        elderLoading.value = true;
        try {
            const res = await getElderPage({
                pageNo: 1,
                pageSize,
                keyword: keyword || undefined
            });
            elderOptions.value = (res.list || []).map(toElderOption);
        }
        finally {
            elderLoading.value = false;
        }
    }
    function findElderName(elderId) {
        if (!elderId)
            return '';
        const selected = elderOptions.value.find((item) => item.value === elderId);
        return selected?.name || '';
    }
    function ensureSelectedElder(elderId, elderName) {
        if (!elderId || elderOptions.value.some((item) => item.value === elderId)) {
            return;
        }
        const name = elderName || `Elder#${elderId}`;
        elderOptions.value.unshift({
            label: name,
            value: elderId,
            name
        });
    }
    return {
        elderOptions,
        elderLoading,
        searchElders,
        findElderName,
        ensureSelectedElder
    };
}
