import { normalizeLifecycleStage } from './lifecycleStage'

export interface ElderResidenceStatusInput {
  status?: number | null
  lifecycleStage?: string
  lifecycleContractStatus?: string
  flowStage?: string
  contractStatus?: string
}

export function isInHospitalByContract(input: ElderResidenceStatusInput) {
  const stage = normalizeLifecycleStage(
    String(input.lifecycleStage || input.flowStage || '').trim(),
    String(input.lifecycleContractStatus || input.contractStatus || '').trim()
  )
  return stage === 'SIGNED'
}

export function resolveElderResidenceStatus(input: ElderResidenceStatusInput) {
  const raw = Number(input.status ?? NaN)
  const inHospital = isInHospitalByContract(input)
  if (!inHospital) {
    if (raw === 2 || raw === 3) return raw
    return 0
  }
  if (raw === 2 || raw === 3 || raw === 1) return raw
  return 1
}

export function elderResidenceStatusText(input: ElderResidenceStatusInput) {
  const status = resolveElderResidenceStatus(input)
  if (status === 1) return '在院'
  if (status === 2) return '请假'
  if (status === 3) return '离院'
  return '不在院'
}

export function elderResidenceStatusColor(input: ElderResidenceStatusInput) {
  const status = resolveElderResidenceStatus(input)
  if (status === 1) return 'green'
  if (status === 2) return 'orange'
  if (status === 3) return 'red'
  return 'default'
}
