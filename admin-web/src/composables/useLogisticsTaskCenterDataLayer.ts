import dayjs from 'dayjs'
import type { Ref } from 'vue'
import type {
  DiningDeliveryRecord,
  InventoryAdjustmentItem,
  LogisticsWorkbenchSummary,
  LogisticsWorkbenchSummaryQuery,
  MaintenanceRequest,
  RoomCleaningTask
} from '../types'
import type { LogisticsTaskCenterDataSnapshot } from './useLogisticsTaskCenterRequestLayer'
import { summaryQuerySignature } from './useLogisticsTaskCenterRoute'

export interface TaskCenterDataLayerParams {
  requestData: () => Promise<LogisticsTaskCenterDataSnapshot>
  summaryQuery: Ref<LogisticsWorkbenchSummaryQuery>
  loading: Ref<boolean>
  errorMessage: Ref<string>
  loadedSummarySignature: Ref<string>
  lastLoadedAt: Ref<string>
  summary: Ref<LogisticsWorkbenchSummary | undefined>
  cleaningRows: Ref<RoomCleaningTask[]>
  maintenanceRows: Ref<MaintenanceRequest[]>
  deliveryRows: Ref<DiningDeliveryRecord[]>
  adjustmentRows: Ref<InventoryAdjustmentItem[]>
  selectedCleaningKeys: Ref<Array<number | string>>
  selectedMaintenanceKeys: Ref<Array<number | string>>
  selectedDeliveryKeys: Ref<Array<number | string>>
  onError?: (message: string, error: unknown) => void
}

export function retainTaskCenterSelectionKeys<T extends { id: number | string }>(
  selectedKeys: Array<number | string>,
  rows: T[]
) {
  const idSet = new Set(rows.map((item) => String(item.id)))
  return selectedKeys.filter((id) => idSet.has(String(id)))
}

export function useLogisticsTaskCenterDataLayer(params: TaskCenterDataLayerParams) {
  function applySnapshot(snapshot: LogisticsTaskCenterDataSnapshot) {
    params.summary.value = snapshot.summary
    params.cleaningRows.value = snapshot.cleaningRows
    params.maintenanceRows.value = snapshot.maintenanceRows
    params.deliveryRows.value = snapshot.deliveryRows
    params.adjustmentRows.value = snapshot.adjustmentRows
  }

  function normalizeSelection(preserveSelection: boolean) {
    if (!preserveSelection) {
      params.selectedCleaningKeys.value = []
      params.selectedMaintenanceKeys.value = []
      params.selectedDeliveryKeys.value = []
      return
    }
    params.selectedCleaningKeys.value = retainTaskCenterSelectionKeys(
      params.selectedCleaningKeys.value,
      params.cleaningRows.value
    )
    params.selectedMaintenanceKeys.value = retainTaskCenterSelectionKeys(
      params.selectedMaintenanceKeys.value,
      params.maintenanceRows.value
    )
    params.selectedDeliveryKeys.value = retainTaskCenterSelectionKeys(
      params.selectedDeliveryKeys.value,
      params.deliveryRows.value
    )
  }

  async function loadData(options?: { preserveSelection?: boolean }) {
    params.loading.value = true
    params.errorMessage.value = ''
    const preserveSelection = Boolean(options?.preserveSelection)
    try {
      const snapshot = await params.requestData()
      applySnapshot(snapshot)
      normalizeSelection(preserveSelection)
      params.loadedSummarySignature.value = summaryQuerySignature(params.summaryQuery.value)
      params.lastLoadedAt.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
    } catch (error: any) {
      const normalizedMessage = String(error?.message || '加载后勤任务中心失败')
      params.errorMessage.value = normalizedMessage
      params.onError?.(normalizedMessage, error)
    } finally {
      params.loading.value = false
    }
  }

  return {
    loadData
  }
}
