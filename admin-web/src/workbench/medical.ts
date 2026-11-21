import { getHealthMedicationRegistrationPage } from '../api/health'
import { getMedicalCareWorkbenchSummary, getMedicalUnifiedTaskPage } from '../api/medicalCare'
import type {
  HealthMedicationRegistration,
  Id,
  MedicalCareRiskResident,
  MedicalCareWorkbenchSummary,
  MedicalUnifiedTaskItem
} from '../types'
import type { WorkbenchProfile } from './model'

export type MedicalWorkbenchScope = 'SELF_SAFE' | 'DEPARTMENT'

export interface MedicalWorkbenchTask {
  id: Id
  residentId?: Id
  residentName: string
  title: string
  module: string
  moduleLabel: string
  plannedTime?: string
  assignee?: string
  status?: string
  statusLabel: string
  overdue: boolean
  priority: string
  path: string
}

export interface MedicalWorkbenchResident {
  id?: Id
  name: string
  riskLevel: string
  riskLabel: string
  riskFactors: string
  path: string
}

export interface MedicalWorkbenchRecord {
  id: Id
  elderId?: Id
  elderName: string
  drugName: string
  registerTime: string
  dosage: string
}

export interface MedicalWorkbenchDetail {
  scope: MedicalWorkbenchScope
  scopeLabel: string
  tasks: MedicalWorkbenchTask[]
  riskResidents: MedicalWorkbenchResident[]
  recentMedicationRecords: MedicalWorkbenchRecord[]
  personalTaskDataAvailable: boolean
  personnelBreakdownAvailable: false
}

export interface MedicalWorkbenchContext {
  date: string
  username?: string
}

export interface MedicalWorkbenchResult {
  summary: Partial<MedicalCareWorkbenchSummary>
  detail: MedicalWorkbenchDetail
}

function responseData<T>(response: T | { data: T }): T {
  return response && typeof response === 'object' && 'data' in response
    ? (response as { data: T }).data
    : response as T
}

export function medicalModuleLabel(module?: string) {
  if (module === 'ORDER') return '医嘱用药'
  if (module === 'INSPECTION') return '巡诊巡检'
  if (module === 'NURSING_LOG') return '医护记录'
  if (module === 'HANDOVER') return '医护交接'
  return '医务任务'
}

export function medicalTaskStatusLabel(status?: string) {
  if (status === 'DONE' || status === 'COMPLETED' || status === 'CLOSED') return '已完成'
  if (status === 'ABNORMAL') return '异常待处理'
  if (status === 'FOLLOWING') return '跟进中'
  if (status === 'HANDED_OVER') return '待确认'
  return '待处理'
}

function taskPath(item: MedicalUnifiedTaskItem) {
  if (item.suggestedRoute?.startsWith('/')) return item.suggestedRoute
  if (item.module === 'ORDER') return '/medical-care/medication-registration'
  if (item.module === 'INSPECTION') return '/medical-care/inspection'
  if (item.module === 'HANDOVER') return '/medical-care/handovers'
  return '/medical-care/unified-task-center'
}

function mapTask(item: MedicalUnifiedTaskItem): MedicalWorkbenchTask {
  return {
    id: item.id,
    residentId: item.residentId,
    residentName: item.residentName || '未关联长者',
    title: item.taskTitle || '医务任务',
    module: item.module,
    moduleLabel: medicalModuleLabel(item.module),
    plannedTime: item.plannedTime,
    assignee: item.assignee,
    status: item.status,
    statusLabel: medicalTaskStatusLabel(item.status),
    overdue: Boolean(item.overdue),
    priority: item.priority || 'LOW',
    path: taskPath(item)
  }
}

function riskLevelLabel(level?: string) {
  if (level === 'CRITICAL' || level === 'VERY_HIGH') return '极高风险'
  if (level === 'HIGH') return '高风险'
  if (level === 'MEDIUM') return '中风险'
  return '低风险'
}

function mapRiskResident(item: MedicalCareRiskResident): MedicalWorkbenchResident {
  return {
    id: item.elderId,
    name: item.elderName || '未命名长者',
    riskLevel: item.riskLevel || 'LOW',
    riskLabel: riskLevelLabel(item.riskLevel),
    riskFactors: item.keyRiskFactors || '风险因素待补充',
    path: item.elderId ? `/elder/detail/${item.elderId}` : '/elder/list'
  }
}

function mapMedicationRecord(item: HealthMedicationRegistration): MedicalWorkbenchRecord {
  return {
    id: item.id,
    elderId: item.elderId,
    elderName: item.elderName || '未命名长者',
    drugName: item.drugName || '未命名药品',
    registerTime: item.registerTime,
    dosage: [item.dosageTaken, item.unit].filter((value) => value !== undefined && value !== null && value !== '').join(' ') || '剂量待补充'
  }
}

export function buildMedicalDepartmentDetail(
  summary: Partial<MedicalCareWorkbenchSummary>,
  tasks: MedicalUnifiedTaskItem[]
): MedicalWorkbenchDetail {
  return {
    scope: 'DEPARTMENT',
    scopeLabel: '医务部今日运行口径',
    tasks: tasks.slice(0, 8).map(mapTask),
    riskResidents: (summary.keyResidents || []).slice(0, 5).map(mapRiskResident),
    recentMedicationRecords: [],
    personalTaskDataAvailable: false,
    personnelBreakdownAvailable: false
  }
}

export function buildMedicalEmployeeDetail(records: HealthMedicationRegistration[]): MedicalWorkbenchDetail {
  return {
    scope: 'SELF_SAFE',
    scopeLabel: '仅展示可确认属于我的记录',
    tasks: [],
    riskResidents: [],
    recentMedicationRecords: records.slice(0, 6).map(mapMedicationRecord),
    personalTaskDataAvailable: false,
    personnelBreakdownAvailable: false
  }
}

export async function loadMedicalWorkbench(
  profile: WorkbenchProfile,
  context: MedicalWorkbenchContext
): Promise<MedicalWorkbenchResult> {
  if (profile.audience === 'EMPLOYEE') {
    const username = String(context.username || '').trim()
    if (!username) throw new Error('当前账号缺少用户名，无法安全加载个人医务记录')
    const page = await getHealthMedicationRegistrationPage({
      pageNo: 1,
      pageSize: 6,
      nurseName: username,
      registerFrom: `${context.date} 00:00:00`,
      registerTo: `${context.date} 23:59:59`
    })
    return {
      summary: { medicationDoneCount: page.total },
      detail: buildMedicalEmployeeDetail(page.list || [])
    }
  }

  const [summaryResponse, taskPage] = await Promise.all([
    getMedicalCareWorkbenchSummary({ date: context.date, topResidentLimit: 5 }, { silent403: true, silentError: true }),
    getMedicalUnifiedTaskPage({ pageNo: 1, pageSize: 8, sortBy: 'RISK_SCORE', sortDirection: 'DESC' })
  ])
  const summary = responseData<MedicalCareWorkbenchSummary>(summaryResponse)
  return {
    summary,
    detail: buildMedicalDepartmentDetail(summary, taskPage.list || [])
  }
}
