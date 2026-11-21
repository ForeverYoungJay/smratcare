export type DischargeStep = 'review' | 'settle' | 'sync'

/** 未指定或无效时停在「结算与退款」，与原退住结算入口一致 */
export function pickDischargeStep(value: unknown): DischargeStep {
  return value === 'review' || value === 'sync' ? value : 'settle'
}
