export type LifecycleStageKey = 'PENDING_ASSESSMENT' | 'PENDING_BED_SELECT' | 'PENDING_SIGN' | 'SIGNED'

export const LIFECYCLE_STAGE_ORDER: LifecycleStageKey[] = [
  'PENDING_ASSESSMENT',
  'PENDING_BED_SELECT',
  'PENDING_SIGN',
  'SIGNED'
]

export const LIFECYCLE_STAGE_OPTIONS: Array<{ key: LifecycleStageKey; label: string; description: string }> = [
  { key: 'PENDING_ASSESSMENT', label: '待评估', description: '完成入住评估并确认风险等级' },
  { key: 'PENDING_BED_SELECT', label: '待办理入住', description: '选择楼栋房间床位并校验档案资料' },
  { key: 'PENDING_SIGN', label: '待签署', description: '确认费用条款并触发跨部门交接' },
  { key: 'SIGNED', label: '已签署', description: '合同生效，可持续追踪履约状态' }
]

export function normalizeLifecycleStage(
  flowStage?: string,
  contractStatus?: string
): LifecycleStageKey {
  const stage = normalizeUpper(flowStage)
  if (LIFECYCLE_STAGE_ORDER.includes(stage as LifecycleStageKey)) {
    return stage as LifecycleStageKey
  }

  const status = normalizeUpper(contractStatus)
  if (['SIGNED', 'EFFECTIVE', 'APPROVED'].includes(status) || status.includes('签署') || status.includes('生效')) {
    return 'SIGNED'
  }
  if (status.includes('待签') || status.includes('签约')) {
    return 'PENDING_SIGN'
  }
  if (status.includes('入住') || status.includes('床位')) {
    return 'PENDING_BED_SELECT'
  }
  return 'PENDING_ASSESSMENT'
}

export function lifecycleStageLabel(stage?: string) {
  const normalized = normalizeLifecycleStage(stage)
  return LIFECYCLE_STAGE_OPTIONS.find((item) => item.key === normalized)?.label || '待评估'
}

export function lifecycleStageHint(stage?: string) {
  const normalized = normalizeLifecycleStage(stage)
  if (normalized === 'PENDING_ASSESSMENT') return '建议先完成评估并归档报告，避免入住流程阻塞。'
  if (normalized === 'PENDING_BED_SELECT') return '当前可推进入住办理，完成选床后可进入待签署。'
  if (normalized === 'PENDING_SIGN') return '入住流程已到签署前置阶段，请尽快完成最终签署。'
  return '合同履约流程已闭环，可持续跟踪费用、服务与风险状态。'
}

export function lifecycleStageColor(stage?: string) {
  const normalized = normalizeLifecycleStage(stage)
  if (normalized === 'PENDING_ASSESSMENT') return 'gold'
  if (normalized === 'PENDING_BED_SELECT') return 'blue'
  if (normalized === 'PENDING_SIGN') return 'purple'
  return 'green'
}

function normalizeUpper(value?: string) {
  return String(value || '').trim().toUpperCase()
}
