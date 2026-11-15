/**
 * 护理域枚举的中文映射。
 *
 * 参照 marketingEnums.ts 的做法集中维护，避免各页面私建重复映射。
 * 取值来源：后端 NursingRecordServiceImpl（recordType / status）与 ServiceBooking.status。
 */

export const NURSING_RECORD_TYPE_LABELS: Record<string, string> = {
  CARE_TASK: '护理任务',
  SERVICE_BOOKING: '服务预约'
}

export const NURSING_RECORD_STATUS_LABELS: Record<string, string> = {
  DONE: '已完成',
  FAILED: '未完成',
  BOOKED: '已预约',
  PENDING: '待执行',
  IN_PROGRESS: '进行中',
  CANCELLED: '已取消',
  CANCELED: '已取消'
}

export const NURSING_RECORD_STATUS_COLORS: Record<string, string> = {
  DONE: 'green',
  FAILED: 'red',
  CANCELLED: 'default',
  CANCELED: 'default',
  BOOKED: 'blue',
  PENDING: 'orange',
  IN_PROGRESS: 'processing'
}

/** 历史数据里可能已经是中文，映射不到时原样透传，不要显示成“-”把信息弄丢 */
function label(map: Record<string, string>, value?: string | null): string {
  const raw = String(value ?? '').trim()
  if (!raw) return '-'
  return map[raw] || map[raw.toUpperCase()] || raw
}

export function nursingRecordTypeLabel(value?: string | null): string {
  return label(NURSING_RECORD_TYPE_LABELS, value)
}

export function nursingRecordStatusLabel(value?: string | null): string {
  return label(NURSING_RECORD_STATUS_LABELS, value)
}

export function nursingRecordStatusColor(value?: string | null): string {
  const raw = String(value ?? '').trim().toUpperCase()
  return NURSING_RECORD_STATUS_COLORS[raw] || 'default'
}
