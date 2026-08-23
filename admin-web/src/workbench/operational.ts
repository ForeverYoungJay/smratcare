export type OperationalTone = 'normal' | 'pending' | 'warning' | 'danger' | 'offline'

export interface OperationalAction {
  label: string
  icon: string
  path: string
  managementOnly?: boolean
}

export interface OperationalMetric {
  key: string
  label: string
  value?: number
  suffix?: string
  helper: string
  path: string
  tone?: OperationalTone
}

export interface OperationalStep {
  title: string
  description: string
  path: string
}

export interface RoleDepartmentWorkbenchDetail {
  department: 'LOGISTICS' | 'HR' | 'MARKETING'
  employeeTitle: string
  managementTitle: string
  scopeLabel: string
  headline: string
  description: string
  dataScopeNotice?: string
  actions: OperationalAction[]
  metrics: OperationalMetric[]
  steps: OperationalStep[]
  managementSummary?: string
  personnelBreakdownAvailable: false
}

export function responseData<T>(response: T | { data: T }): T {
  return response && typeof response === 'object' && 'data' in response
    ? (response as { data: T }).data
    : response as T
}
