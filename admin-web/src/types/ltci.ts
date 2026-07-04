import type { Id } from './common'

export interface LtciAssessment {
  id: Id
  applyId?: Id
  elderId: Id
  templateId?: Id
  assessorId?: Id
  assessDate?: string
  adlScore?: number
  cognitiveScore?: number
  perceptionScore?: number
  totalScore?: number
  disabilityLevel?: number
  levelLabel?: string
  answersJson?: string
  escalated?: number
  effectiveStart?: string
  effectiveEnd?: string
  assessStatus?: string
  reviewOfId?: Id
  remark?: string
  createTime?: string
}

export interface LtciServicePackage {
  id: Id
  packageCode: string
  packageName: string
  levelScope?: string
  itemsJson?: string
  price?: number
  fundCovered?: number
  status?: number
  remark?: string
}

export interface LtciSettlement {
  id: Id
  elderId: Id
  benefitId?: Id
  settleMonth: string
  serviceDays?: number
  totalFee?: number
  fundPay?: number
  selfPay?: number
  overQuota?: number
  dailyQuota?: number
  payRatio?: number
  settleStatus?: string
  settleNo?: string
  detailJson?: string
  submittedAt?: string
  remark?: string
  createTime?: string
}
