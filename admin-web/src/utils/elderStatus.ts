import dayjs from 'dayjs'
import { normalizeLifecycleStage } from './lifecycleStage'
import type { ElderItem } from '../types/api'

/**
 * 长者「在住生命周期状态」的统一映射。
 *
 * 历史上 statusText / statusTag / 身份证脱敏 / 年龄计算 分散在 List / Detail /
 * Admission / Family 等多个页面各自实现，且数值枚举（1/2/3）与生命周期枚举
 * （IN_HOSPITAL / OUTING …）文案不一致。此处以 List.vue 的完整实现为准收敛，
 * 后续所有页面都应从这里引用，避免同一状态在不同页面显示不同文案。
 */

/** 后端遗留数值状态：1=在住 2=外出 3=离院。 */
export type ResidentStatusCode = number

/** 由数值状态 / 生命周期字段推导出规范化的生命周期状态码。 */
export function resolveResidentLifecycleStatus(item?: Partial<ElderItem>): string {
  const lifecycleStatus = String(item?.lifecycleStatus || '').trim().toUpperCase()
  if (lifecycleStatus) return lifecycleStatus
  if (item?.status === 1) return 'IN_HOSPITAL'
  if (item?.status === 2) return 'OUTING'
  if (item?.status === 3) return item?.departureType === 'DEATH' ? 'DECEASED' : 'DISCHARGED'
  return ''
}

/** 在生命周期状态之上叠加「待办理入住 / 待签署」等履约阶段视图。 */
export function resolveResidentStatusView(item?: Partial<ElderItem>): string {
  const lifecycleStage = normalizeLifecycleStage(item?.lifecycleStage, item?.lifecycleContractStatus)
  if (lifecycleStage === 'PENDING_BED_SELECT') return 'PENDING_BED_SELECT'
  if (lifecycleStage === 'PENDING_SIGN') return 'PENDING_SIGN'
  return resolveResidentLifecycleStatus(item)
}

function resolveStatusKey(item?: Partial<ElderItem> | ResidentStatusCode): string {
  return typeof item === 'number'
    ? resolveResidentLifecycleStatus({ status: item })
    : resolveResidentStatusView(item)
}

/** 状态文案。入参可为长者对象，也可为后端遗留数值状态。 */
export function residentStatusText(item?: Partial<ElderItem> | ResidentStatusCode): string {
  const lifecycleStatus = resolveStatusKey(item)
  if (lifecycleStatus === 'PENDING_BED_SELECT') return '待办理入住'
  if (lifecycleStatus === 'PENDING_SIGN') return '待签署入住'
  if (lifecycleStatus === 'INTENT') return '意向'
  if (lifecycleStatus === 'TRIAL') return '试住'
  if (lifecycleStatus === 'IN_HOSPITAL') return '在住'
  if (lifecycleStatus === 'OUTING') return '外出'
  if (lifecycleStatus === 'MEDICAL_OUTING') return '外出就医'
  if (lifecycleStatus === 'DISCHARGE_PENDING') return '待退住'
  if (lifecycleStatus === 'DISCHARGED') return '已退住'
  if (lifecycleStatus === 'DECEASED') return '已身故'
  return '未知'
}

/** 状态对应的 a-tag 颜色。 */
export function residentStatusTag(item?: Partial<ElderItem> | ResidentStatusCode): string {
  const lifecycleStatus = resolveStatusKey(item)
  if (lifecycleStatus === 'PENDING_BED_SELECT') return 'blue'
  if (lifecycleStatus === 'PENDING_SIGN') return 'purple'
  if (lifecycleStatus === 'INTENT') return 'gold'
  if (lifecycleStatus === 'TRIAL') return 'cyan'
  if (lifecycleStatus === 'IN_HOSPITAL') return 'green'
  if (lifecycleStatus === 'OUTING') return 'orange'
  if (lifecycleStatus === 'MEDICAL_OUTING') return 'volcano'
  if (lifecycleStatus === 'DISCHARGE_PENDING') return 'blue'
  if (lifecycleStatus === 'DISCHARGED') return 'red'
  if (lifecycleStatus === 'DECEASED') return 'magenta'
  return 'default'
}

/** 身份证脱敏：保留前 3 位与后 2 位，中间以 * 填充。 */
export function maskIdCard(value?: string): string {
  const id = String(value || '').trim()
  if (!id) return '—'
  if (id.length <= 6) return `${id.slice(0, 1)}****`
  return `${id.slice(0, 3)}${'*'.repeat(Math.max(4, id.length - 5))}${id.slice(-2)}`
}

/** 由出生日期计算周岁；非法日期或超出合理范围时返回 null。 */
export function ageOf(birthDate?: string): number | null {
  if (!birthDate) return null
  const d = dayjs(birthDate)
  if (!d.isValid()) return null
  const age = dayjs().diff(d, 'year')
  return age >= 0 && age < 130 ? age : null
}
