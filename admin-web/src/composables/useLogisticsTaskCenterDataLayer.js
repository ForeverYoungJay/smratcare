import dayjs from 'dayjs';
import { summaryQuerySignature } from './useLogisticsTaskCenterRoute';
export function retainTaskCenterSelectionKeys(selectedKeys, rows) {
    const idSet = new Set(rows.map((item) => String(item.id)));
    return selectedKeys.filter((id) => idSet.has(String(id)));
}
export function useLogisticsTaskCenterDataLayer(params) {
    function applySnapshot(snapshot) {
        params.summary.value = snapshot.summary;
        params.cleaningRows.value = snapshot.cleaningRows;
        params.maintenanceRows.value = snapshot.maintenanceRows;
        params.deliveryRows.value = snapshot.deliveryRows;
        params.adjustmentRows.value = snapshot.adjustmentRows;
    }
    function normalizeSelection(preserveSelection) {
        if (!preserveSelection) {
            params.selectedCleaningKeys.value = [];
            params.selectedMaintenanceKeys.value = [];
            params.selectedDeliveryKeys.value = [];
            return;
        }
        params.selectedCleaningKeys.value = retainTaskCenterSelectionKeys(params.selectedCleaningKeys.value, params.cleaningRows.value);
        params.selectedMaintenanceKeys.value = retainTaskCenterSelectionKeys(params.selectedMaintenanceKeys.value, params.maintenanceRows.value);
        params.selectedDeliveryKeys.value = retainTaskCenterSelectionKeys(params.selectedDeliveryKeys.value, params.deliveryRows.value);
    }
    async function loadData(options) {
        params.loading.value = true;
        params.errorMessage.value = '';
        const preserveSelection = Boolean(options?.preserveSelection);
        try {
            const snapshot = await params.requestData();
            applySnapshot(snapshot);
            normalizeSelection(preserveSelection);
            params.loadedSummarySignature.value = summaryQuerySignature(params.summaryQuery.value);
            params.lastLoadedAt.value = dayjs().format('YYYY-MM-DD HH:mm:ss');
        }
        catch (error) {
            const normalizedMessage = String(error?.message || '加载后勤任务中心失败');
            params.errorMessage.value = normalizedMessage;
            params.onError?.(normalizedMessage, error);
        }
        finally {
            params.loading.value = false;
        }
    }
    return {
        loadData
    };
}
