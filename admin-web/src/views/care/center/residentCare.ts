import type { AssessmentRecord } from '../../../types'

/** 每位长者取评估日期最新的一条（草稿不算） */
export function latestAssessmentByElder(records: AssessmentRecord[]) {
  const map = new Map<string, AssessmentRecord>()
  for (const record of records) {
    if (record.elderId == null || record.status === 'DRAFT') continue
    const key = String(record.elderId)
    const current = map.get(key)
    if (!current || String(record.assessmentDate) > String(current.assessmentDate)) map.set(key, record)
  }
  return map
}

/**
 * 评估→计划流程走到第几步（0 起，对应 a-steps 的 current）：
 * 无评估或草稿 → 评估登记/指标评估；已完成评估但无计划 → 制定计划；计划待审核 → 审核发布；计划已发布 → 全部完成
 */
export function assessmentFlowStep(latest: AssessmentRecord | undefined, plans: { approvalStatus?: string }[]) {
  if (!latest) return 0
  if (latest.status === 'DRAFT') return 1
  if (!plans.length) return 3
  if (plans.some((p) => p.approvalStatus === 'PENDING' || p.approvalStatus === 'REJECTED')) return 4
  return 5
}
