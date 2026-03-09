import { ref } from 'vue';
import { describe, expect, it, vi } from 'vitest';
import { retainTaskCenterSelectionKeys, useLogisticsTaskCenterDataLayer } from './useLogisticsTaskCenterDataLayer';
function createSnapshot() {
    return {
        summary: {
            lowStockCount: 0,
            expiringCount: 0,
            todayOutboundQty: 0,
            inventoryTotalAmount: 0,
            weekTopConsumption: [],
            inventoryAssetQty: 0,
            inventoryConsumableQty: 0,
            inventoryFoodQty: 0,
            inventoryServiceQty: 0,
            lowStockAssetCount: 0,
            lowStockConsumableCount: 0,
            lowStockFoodCount: 0,
            lowStockServiceCount: 0,
            monthPurchaseAssetAmount: 0,
            monthPurchaseConsumableAmount: 0,
            monthPurchaseFoodAmount: 0,
            monthPurchaseServiceAmount: 0,
            todayOutboundAssetQty: 0,
            todayOutboundConsumableQty: 0,
            todayOutboundFoodQty: 0,
            todayOutboundServiceQty: 0,
            purchasePendingApprovalCount: 0,
            purchaseApprovedNotArrivedCount: 0,
            purchaseReceivedNotSettledCount: 0,
            monthPurchaseAmount: 0,
            occupiedBeds: 0,
            freeBeds: 0,
            cleaningBeds: 0,
            maintenanceBeds: 0,
            todayDischargeCount: 0,
            todayAdmissionCount: 0,
            maintenancePendingCount: 0,
            maintenanceProcessingCount: 0,
            maintenanceOverdueCount: 0,
            monthMaintenanceCost: 0,
            todayMealOrderCount: 0,
            personalizedMealCount: 0,
            deliveryCompletionRate: 0,
            undeliveredCount: 0,
            todayDiningCost: 0,
            diabetesMealCount: 0,
            lowSaltMealCount: 0,
            allergyAlertCount: 0,
            specialNutritionMealCount: 0,
            monthConsumeAmount: 0,
            monthTopConsumption: [],
            nursingConsumeAmount: 0,
            diningConsumeAmount: 0,
            todayCleaningTaskCount: 0,
            todayMaintenanceTaskCount: 0,
            todayDeliveryTaskCount: 0,
            todayInventoryCheckTaskCount: 0,
            equipmentTotalCount: 0,
            equipmentMaintainingCount: 0,
            equipmentDueSoonCount: 0,
            maintenanceTodoFailedCount7d: 0
        },
        cleaningRows: [{ id: 1 }, { id: 2 }],
        maintenanceRows: [{ id: 10 }, { id: 12 }],
        deliveryRows: [{ id: 20 }],
        adjustmentRows: [{ id: 30 }]
    };
}
describe('useLogisticsTaskCenterDataLayer', () => {
    it('retains selected keys that still exist in current rows', () => {
        expect(retainTaskCenterSelectionKeys([1, 2, 99], [{ id: 2 }, { id: 8 }])).toEqual([2]);
    });
    it('loads snapshot and preserves valid selections', async () => {
        const summaryQuery = ref({ windowDays: 14, overdueDays: 2 });
        const loading = ref(false);
        const errorMessage = ref('');
        const loadedSummarySignature = ref('');
        const lastLoadedAt = ref('');
        const summary = ref(undefined);
        const cleaningRows = ref([]);
        const maintenanceRows = ref([]);
        const deliveryRows = ref([]);
        const adjustmentRows = ref([]);
        const selectedCleaningKeys = ref([2, 99]);
        const selectedMaintenanceKeys = ref([10, 98]);
        const selectedDeliveryKeys = ref([20, 200]);
        const layer = useLogisticsTaskCenterDataLayer({
            requestData: async () => createSnapshot(),
            summaryQuery,
            loading,
            errorMessage,
            loadedSummarySignature,
            lastLoadedAt,
            summary,
            cleaningRows,
            maintenanceRows,
            deliveryRows,
            adjustmentRows,
            selectedCleaningKeys,
            selectedMaintenanceKeys,
            selectedDeliveryKeys
        });
        await layer.loadData({ preserveSelection: true });
        expect(cleaningRows.value.map((item) => item.id)).toEqual([1, 2]);
        expect(selectedCleaningKeys.value).toEqual([2]);
        expect(selectedMaintenanceKeys.value).toEqual([10]);
        expect(selectedDeliveryKeys.value).toEqual([20]);
        expect(loadedSummarySignature.value).toBe('14-30-2-30');
        expect(lastLoadedAt.value).not.toBe('');
        expect(errorMessage.value).toBe('');
    });
    it('clears selections and reports error when request fails', async () => {
        const onError = vi.fn();
        const layer = useLogisticsTaskCenterDataLayer({
            requestData: async () => {
                throw new Error('network down');
            },
            summaryQuery: ref({}),
            loading: ref(false),
            errorMessage: ref(''),
            loadedSummarySignature: ref(''),
            lastLoadedAt: ref(''),
            summary: ref(undefined),
            cleaningRows: ref([]),
            maintenanceRows: ref([]),
            deliveryRows: ref([]),
            adjustmentRows: ref([]),
            selectedCleaningKeys: ref([1]),
            selectedMaintenanceKeys: ref([2]),
            selectedDeliveryKeys: ref([3]),
            onError
        });
        await layer.loadData({ preserveSelection: false });
        expect(layer).toBeTruthy();
        expect(onError).toHaveBeenCalledTimes(1);
        expect(onError.mock.calls[0]?.[0]).toBe('network down');
    });
});
