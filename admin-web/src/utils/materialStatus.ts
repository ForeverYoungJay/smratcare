export const MATERIAL_ORDER_STATUS = {
  DRAFT: 'DRAFT',
  APPROVED: 'APPROVED',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED'
} as const

export type MaterialOrderStatus = (typeof MATERIAL_ORDER_STATUS)[keyof typeof MATERIAL_ORDER_STATUS]

const MATERIAL_ORDER_STATUS_META: Record<MaterialOrderStatus, { label: string; color: string }> = {
  [MATERIAL_ORDER_STATUS.DRAFT]: { label: '草稿', color: 'default' },
  [MATERIAL_ORDER_STATUS.APPROVED]: { label: '已审核', color: 'blue' },
  [MATERIAL_ORDER_STATUS.COMPLETED]: { label: '已完成', color: 'green' },
  [MATERIAL_ORDER_STATUS.CANCELLED]: { label: '已作废', color: 'red' }
}

export const MATERIAL_PURCHASE_STATUS_OPTIONS = (Object.keys(MATERIAL_ORDER_STATUS_META) as MaterialOrderStatus[]).map(
  (status) => ({
    label: MATERIAL_ORDER_STATUS_META[status].label,
    value: status
  })
)

export const MATERIAL_TRANSFER_STATUS_OPTIONS = [
  MATERIAL_ORDER_STATUS.DRAFT,
  MATERIAL_ORDER_STATUS.COMPLETED,
  MATERIAL_ORDER_STATUS.CANCELLED
].map((status) => ({
  label: MATERIAL_ORDER_STATUS_META[status].label,
  value: status
}))

export function materialOrderStatusLabel(status?: string) {
  if (!status) return '-'
  return MATERIAL_ORDER_STATUS_META[status as MaterialOrderStatus]?.label || status
}

export function materialOrderStatusColor(status?: string) {
  if (!status) return 'default'
  return MATERIAL_ORDER_STATUS_META[status as MaterialOrderStatus]?.color || 'default'
}

export const MATERIAL_ENABLE_STATUS_OPTIONS = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

export type MaterialEnableStatus = 0 | 1

export function materialEnableStatusLabel(status?: number) {
  return status === 1 ? '启用' : '停用'
}

export function materialEnableStatusColor(status?: number) {
  return status === 1 ? 'green' : 'default'
}
