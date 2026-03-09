import type { ThresholdSnapshot } from './dashboardThreshold'

export interface ThresholdPreviewInput {
  abnormalCount: number
  inventoryCount: number
  bedOccupancyRate: number
  revenueGrowthRate: number
}

export interface ThresholdPreviewRow {
  key: 'abnormal' | 'inventory' | 'bed' | 'revenue'
  title: string
  currentText: string
  thresholdText: string
  triggered: boolean
}

function safeNumber(value: unknown) {
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric : 0
}

export function buildThresholdPreviewRows(
  input: ThresholdPreviewInput,
  threshold: ThresholdSnapshot
): ThresholdPreviewRow[] {
  const abnormalCount = Math.max(0, Math.round(safeNumber(input.abnormalCount)))
  const inventoryCount = Math.max(0, Math.round(safeNumber(input.inventoryCount)))
  const bedOccupancyRate = safeNumber(input.bedOccupancyRate)
  const revenueGrowthRate = safeNumber(input.revenueGrowthRate)
  const safeThreshold: ThresholdSnapshot = {
    abnormalTaskThreshold: Math.max(1, Math.round(safeNumber(threshold.abnormalTaskThreshold))),
    inventoryAlertThreshold: Math.max(1, Math.round(safeNumber(threshold.inventoryAlertThreshold))),
    bedOccupancyThreshold: Math.max(1, Math.round(safeNumber(threshold.bedOccupancyThreshold))),
    revenueDropThreshold: Math.max(1, Math.round(safeNumber(threshold.revenueDropThreshold)))
  }

  return [
    {
      key: 'abnormal',
      title: '护理异常任务',
      currentText: `${abnormalCount} 条`,
      thresholdText: `>= ${safeThreshold.abnormalTaskThreshold} 条`,
      triggered: abnormalCount >= safeThreshold.abnormalTaskThreshold
    },
    {
      key: 'inventory',
      title: '库存预警',
      currentText: `${inventoryCount} 项`,
      thresholdText: `>= ${safeThreshold.inventoryAlertThreshold} 项`,
      triggered: inventoryCount >= safeThreshold.inventoryAlertThreshold
    },
    {
      key: 'bed',
      title: '床位高占用',
      currentText: `${bedOccupancyRate.toFixed(2)}%`,
      thresholdText: `>= ${safeThreshold.bedOccupancyThreshold}%`,
      triggered: bedOccupancyRate >= safeThreshold.bedOccupancyThreshold
    },
    {
      key: 'revenue',
      title: '收入环比下滑',
      currentText: `${revenueGrowthRate.toFixed(2)}%`,
      thresholdText: `<= -${safeThreshold.revenueDropThreshold}%`,
      triggered: revenueGrowthRate <= 0 - safeThreshold.revenueDropThreshold
    }
  ]
}
