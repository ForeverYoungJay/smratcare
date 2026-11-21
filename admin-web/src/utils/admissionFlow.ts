export type AdmissionFlowState = 'done' | 'current' | 'pending'

export type AdmissionFlowStep = {
  key: string
  label: string
  owner: string
  state: AdmissionFlowState
}

const FLOW_STAGES = [
  { key: 'intent', label: '意向确认', owner: '市场部' },
  { key: 'assessment', label: '护理评估', owner: '护理部 / 医务部' },
  { key: 'bed', label: '床位确认', owner: '后勤部' },
  { key: 'contract', label: '合同缴费', owner: '市场部 / 财务部' },
  { key: 'admission', label: '正式入住', owner: '护理部' }
] as const

export function resolveAdmissionFlowSteps(flowStage?: string, admitted = false): AdmissionFlowStep[] {
  const currentIndex = admitted ? 4 : ({
    PENDING_ASSESSMENT: 1,
    PENDING_BED_SELECT: 2,
    PENDING_SIGN: 3,
    SIGNED: 4
  } as Record<string, number>)[String(flowStage || '').toUpperCase()] ?? 0

  return FLOW_STAGES.map((step, index) => ({
    ...step,
    state: index < currentIndex || admitted ? 'done' : index === currentIndex ? 'current' : 'pending'
  }))
}

export interface AdmissionContext { elderId?: string | number; contractNo?: string; leadId?: string | number; bedId?: string | number }

export function withAdmissionContext(path: string, context: AdmissionContext): string {
  const [pathname, search] = path.split('?')
  const params = new URLSearchParams(search || '')
  for (const [key, value] of Object.entries(context)) {
    if (value !== undefined && String(value).trim()) params.set(key, String(value).trim())
  }
  const residentId = String(context.elderId ?? '').trim()
  if (residentId) params.set('residentId', residentId)
  return `${pathname}${params.size ? `?${params}` : ''}`
}
