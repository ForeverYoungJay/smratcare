import dayjs, { type Dayjs } from 'dayjs'
import type { CrmContractItem, DischargeApplyItem } from '../../../types'

/**
 * 退住超时与合同到期提醒的口径（工作台后端统计与此保持一致）：
 * - 审批超时：PENDING 的退住申请，创建时间早于"现在 - 3 天"。
 * - 办理超时：APPROVED 且未归档（archiveTime 为空），计划退住日期早于今天。
 * - 即将到期：生效中合同（SIGNED/EFFECTIVE）到期日在 [今天, 今天 + 30 天] 内。
 * - 已到期：生效中合同到期日早于今天，且同一长者没有到期日更晚的生效合同（未续签）。
 */
export const DISCHARGE_REVIEW_TIMEOUT_DAYS = 3
export const CONTRACT_EXPIRY_WINDOW_DAYS = 30
export const ACTIVE_CONTRACT_STATUSES = ['SIGNED', 'EFFECTIVE']

export type DischargeTimeout = 'REVIEW_TIMEOUT' | 'HANDLING_TIMEOUT'
export type ContractExpiry = 'EXPIRED' | 'EXPIRING'

export const DISCHARGE_TIMEOUT_TEXT: Record<DischargeTimeout, string> = {
  REVIEW_TIMEOUT: '审批超时',
  HANDLING_TIMEOUT: '办理超时'
}
export const CONTRACT_EXPIRY_TEXT: Record<ContractExpiry, string> = {
  EXPIRED: '已到期',
  EXPIRING: '即将到期'
}

export function dischargeTimeout(apply: DischargeApplyItem, now: Dayjs = dayjs()): DischargeTimeout | null {
  if (apply.status === 'PENDING') {
    const created = apply.createTime || apply.applyDate
    return created && dayjs(created).isBefore(now.subtract(DISCHARGE_REVIEW_TIMEOUT_DAYS, 'day')) ? 'REVIEW_TIMEOUT' : null
  }
  if (apply.status === 'APPROVED' && !apply.archiveTime && apply.plannedDischargeDate
    && dayjs(apply.plannedDischargeDate).isBefore(now, 'day')) {
    return 'HANDLING_TIMEOUT'
  }
  return null
}

export function countDischargeTimeouts(applies: DischargeApplyItem[], now: Dayjs = dayjs()) {
  const counts: Record<DischargeTimeout, number> = { REVIEW_TIMEOUT: 0, HANDLING_TIMEOUT: 0 }
  applies.forEach((apply) => {
    const timeout = dischargeTimeout(apply, now)
    if (timeout) counts[timeout] += 1
  })
  return counts
}

const isActiveContract = (contract: CrmContractItem) => ACTIVE_CONTRACT_STATUSES.includes(String(contract.status || ''))

/** `others` 用于判断是否已续签：同一长者存在到期日更晚的生效合同即视为已续签。 */
export function contractExpiry(contract: CrmContractItem, others: CrmContractItem[] = [], now: Dayjs = dayjs()): ContractExpiry | null {
  if (!isActiveContract(contract) || !contract.contractExpiryDate) return null
  const end = dayjs(contract.contractExpiryDate)
  const renewed = contract.elderId != null && others.some((other) =>
    other.id !== contract.id
    && String(other.elderId) === String(contract.elderId)
    && isActiveContract(other)
    && !!other.contractExpiryDate
    && dayjs(other.contractExpiryDate).isAfter(end, 'day'))
  if (renewed) return null
  if (end.isBefore(now, 'day')) return 'EXPIRED'
  return end.isAfter(now.add(CONTRACT_EXPIRY_WINDOW_DAYS, 'day'), 'day') ? null : 'EXPIRING'
}

/** 后端 /api/crm/contracts/page?expiryReminderTo= 的取值：今天 + 30 天。 */
export function contractExpiryReminderTo(now: Dayjs = dayjs()) {
  return now.add(CONTRACT_EXPIRY_WINDOW_DAYS, 'day').format('YYYY-MM-DD')
}
