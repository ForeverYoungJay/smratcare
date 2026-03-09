import { computed, type Ref } from 'vue'
import dayjs from 'dayjs'
import {
  getLogisticsWorkbenchSummary
} from '../api/logistics'
import { completeMaintenance, completeRoomCleaning, getMaintenancePage, getRoomCleaningPage } from '../api/life'
import { getDiningDeliveryRecordPage, redispatchDiningDeliveryRecord, updateDiningDeliveryRecord } from '../api/dining'
import { getInventoryAdjustmentPage } from '../api/materialCenter'
import type {
  DiningDeliveryRecord,
  InventoryAdjustmentItem,
  LogisticsWorkbenchSummary,
  LogisticsWorkbenchSummaryQuery,
  MaintenanceRequest,
  RoomCleaningTask
} from '../types'
import { normalizeLogisticsWorkbenchQuery } from '../utils/logisticsWorkbenchQuery'

export interface LogisticsTaskCenterDataSnapshot {
  summary: LogisticsWorkbenchSummary
  cleaningRows: RoomCleaningTask[]
  maintenanceRows: MaintenanceRequest[]
  deliveryRows: DiningDeliveryRecord[]
  adjustmentRows: InventoryAdjustmentItem[]
}

export function buildTaskCenterSummaryRequest(summaryQuery: LogisticsWorkbenchSummaryQuery) {
  if (Object.keys(summaryQuery || {}).length === 0) {
    return { lite: true }
  }
  return { ...normalizeLogisticsWorkbenchQuery(summaryQuery), lite: true }
}

export async function requestTaskCenterSnapshot(
  summaryQuery: LogisticsWorkbenchSummaryQuery
): Promise<LogisticsTaskCenterDataSnapshot> {
  const today = dayjs().format('YYYY-MM-DD')
  const summaryParams = buildTaskCenterSummaryRequest(summaryQuery)
  const [summaryData, cleaningPage, maintenancePage, deliveryPage, adjustmentPage] = await Promise.all([
    getLogisticsWorkbenchSummary(summaryParams),
    getRoomCleaningPage({ pageNo: 1, pageSize: 500 }),
    getMaintenancePage({ pageNo: 1, pageSize: 500, dateFrom: today, dateTo: today }),
    getDiningDeliveryRecordPage({ pageNo: 1, pageSize: 500, dateFrom: today, dateTo: today }),
    getInventoryAdjustmentPage({ pageNo: 1, pageSize: 200, dateFrom: today, dateTo: today })
  ])
  return {
    summary: summaryData,
    cleaningRows: cleaningPage?.list || [],
    maintenanceRows: maintenancePage?.list || [],
    deliveryRows: deliveryPage?.list || [],
    adjustmentRows: adjustmentPage?.list || []
  }
}

export function useLogisticsTaskCenterRequestLayer(summaryQuery: Ref<LogisticsWorkbenchSummaryQuery>) {
  const resolvedSummaryQuery = computed(() =>
    normalizeLogisticsWorkbenchQuery(summaryQuery.value)
  )
  const summaryWindowHint = computed(
    () =>
      `窗口 ${resolvedSummaryQuery.value.windowDays} 天 / 临期 ${resolvedSummaryQuery.value.expiryDays} 天 / 维修超时 ${resolvedSummaryQuery.value.overdueDays} 天 / 维保 ${resolvedSummaryQuery.value.maintenanceDueDays} 天`
  )
  async function requestData() {
    return requestTaskCenterSnapshot(summaryQuery.value)
  }
  return {
    resolvedSummaryQuery,
    summaryWindowHint,
    requestData
  }
}

export const taskCenterRequestActions = {
  completeRoomCleaning,
  completeMaintenance,
  updateDiningDeliveryRecord,
  redispatchDiningDeliveryRecord
}
