import type { DepartmentCode } from '../access/policy'
import type { Id } from '../types'
import type { WorkbenchProfile } from './model'
import { loadNursingWorkbench, type NursingWorkbenchDetail } from './nursing'
import { loadMedicalWorkbench, type MedicalWorkbenchDetail } from './medical'
import { loadFinanceWorkbench, type FinanceWorkbenchDetail } from './finance'
import { loadLogisticsWorkbench } from './logistics'
import { loadHrRoleWorkbench } from './hr'
import { loadMarketingRoleWorkbench } from './marketing'
import type { RoleDepartmentWorkbenchDetail } from './operational'

export type WorkbenchDataStatus = 'idle' | 'loading' | 'ready' | 'empty' | 'forbidden' | 'error'

export interface WorkbenchMetricSnapshot {
  key: string
  label: string
  value?: number
  suffix?: string
  helper: string
  path: string
  risk?: boolean
}

export interface DepartmentWorkbenchSnapshot {
  department: DepartmentCode
  sourceLabel: string
  generatedAt?: string
  metrics: WorkbenchMetricSnapshot[]
  nursing?: NursingWorkbenchDetail
  medical?: MedicalWorkbenchDetail
  finance?: FinanceWorkbenchDetail
  logistics?: RoleDepartmentWorkbenchDetail
  hr?: RoleDepartmentWorkbenchDetail
  marketing?: RoleDepartmentWorkbenchDetail
}

export interface WorkbenchDataResult {
  status: Exclude<WorkbenchDataStatus, 'idle' | 'loading'>
  snapshot?: DepartmentWorkbenchSnapshot
  message?: string
}

type DepartmentSource = {
  accessPath: string
  load: (profile: WorkbenchProfile, context: WorkbenchLoadContext) => Promise<DepartmentWorkbenchSnapshot>
}

export interface WorkbenchLoadContext {
  staffId?: Id
  username?: string
}

const today = () => new Date().toLocaleDateString('sv-SE')

const departmentSources: Record<DepartmentCode, DepartmentSource> = {
  NURSING: {
    accessPath: '/care/today',
    async load(profile, context) {
      const result = await loadNursingWorkbench(profile, { date: today(), staffId: context.staffId })
      const data = result.summary
      return {
        department: 'NURSING', sourceLabel: '今日护理任务', metrics: [
          { key: 'pending', label: '待执行任务', value: data?.pendingCount, helper: '今天尚未完成的护理任务', path: '/care/today' },
          { key: 'overdue', label: '超时任务', value: data?.overdueCount, helper: '需要优先处理并说明原因', path: '/care/today', risk: true },
          ...(profile.hasManagementView
            ? [{ key: 'unassigned', label: '待分配任务', value: data?.unassignedCount, helper: '需要安排护理人员的任务', path: '/care/today', risk: true }]
            : [{ key: 'exception', label: '异常任务', value: data?.exceptionCount, helper: '需要复核或闭环的护理异常', path: '/care/today', risk: true }]),
          { key: 'completion', label: '今日完成率', value: data?.completionRate, suffix: '%', helper: '按今日护理任务统计', path: '/care/today' }
        ],
        nursing: result.detail
      }
    }
  },
  MEDICAL: {
    accessPath: '/medical-care/workbench',
    async load(profile, context) {
      const result = await loadMedicalWorkbench(profile, { date: today(), username: context.username })
      const data = result.summary
      return {
        department: 'MEDICAL', sourceLabel: '医务工作台汇总', generatedAt: data?.generatedAt, metrics: [
          ...(profile.hasManagementView ? [
            { key: 'inspection', label: '今日巡诊待办', value: data?.todayInspectionPendingCount, helper: '今天尚未完成的巡诊任务', path: '/medical-care/inspection' },
            { key: 'orders', label: '待执行医嘱', value: data?.pendingMedicalOrderCount, helper: '需要执行或复核的医嘱', path: '/medical-care/orders' },
            { key: 'medication', label: '今日用药待办', value: data?.todayMedicationPendingCount, helper: '待登记或待确认的用药事项', path: '/medical-care/medication-registration' },
            { key: 'abnormal', label: '未闭环异常', value: data?.unclosedAbnormalCount, helper: '医疗异常和告警事项', path: '/medical-care/unified-task-center', risk: true }
          ] : [
            { key: 'medicationDone', label: '我的今日用药登记', value: data?.medicationDoneCount, helper: '按当前登录账号登记人统计', path: '/medical-care/medication-registration' },
            { key: 'inspection', label: '我的巡诊待办', helper: '后端尚未提供个人巡诊口径', path: '/medical-care/inspection' },
            { key: 'orders', label: '我的医嘱待办', helper: '后端尚未提供个人医嘱口径', path: '/medical-care/order-executions' }
          ]),
        ],
        medical: result.detail
      }
    }
  },
  FINANCE: {
    accessPath: '/finance/workbench',
    async load(profile) {
      const result = await loadFinanceWorkbench(profile)
      const data = result.overview
      const pending = data?.pending
      return {
        department: 'FINANCE', sourceLabel: '财务工作台汇总', generatedAt: data?.bizDate, metrics: [
          { key: 'collected', label: profile.hasManagementView ? '今日实收' : '机构今日实收', value: data?.cashier?.todayCollectedTotal, suffix: '元', helper: profile.hasManagementView ? '今日已确认收款金额' : '机构当日口径，非个人业绩', path: '/finance/payments/cashier-desk' },
          ...(profile.hasManagementView ? [
            { key: 'pending', label: '待审核费用', value: (pending?.pendingDiscountCount || 0) + (pending?.pendingRefundCount || 0), helper: '折扣与退款待审核事项', path: '/workbench/approvals' },
            { key: 'arrears', label: '欠费长者', value: data?.risk?.overdueElderCount, helper: '存在逾期欠费的长者账户', path: '/finance/bills/follow-up', risk: true },
            { key: 'reconcile', label: '对账异常', value: pending?.issueTodoCount, helper: '需要定位和处理的账务问题', path: '/finance/reconcile/issue-center', risk: true }
          ] : [
            { key: 'refund', label: '机构今日退款', value: data?.cashier?.todayRefundAmount, suffix: '元', helper: '当日退款总额，交班前复核', path: '/finance/payments/refund-reversal' },
            { key: 'invoiceGap', label: '票据未关联', value: data?.reconcile?.invoiceUnlinkedCount, helper: '交班前需要补齐的票据', path: '/finance/fees/payment-and-invoice', risk: true },
            { key: 'reconcile', label: '账务异常', value: (data?.reconcile?.billPaidUnmatchedCount || 0) + (data?.reconcile?.duplicatedOrReversalPendingCount || 0), helper: '收款、退款与账单差异', path: '/finance/reconcile/issue-center', risk: true }
          ])
        ],
        finance: result.detail
      }
    }
  },
  LOGISTICS: {
    accessPath: '/logistics/workbench',
    async load(profile) {
      const result = await loadLogisticsWorkbench(profile)
      const data = result.summary
      return {
        department: 'LOGISTICS', sourceLabel: '后勤工作台汇总', generatedAt: data?.generatedAt,
        metrics: result.detail.metrics.map((item) => ({ ...item, risk: item.tone === 'danger' || item.tone === 'warning' })),
        logistics: result.detail
      }
    }
  },
  HR: {
    accessPath: '/hr/overview',
    async load(profile) {
      const result = await loadHrRoleWorkbench(profile)
      return {
        department: 'HR', sourceLabel: '行政人事工作台汇总',
        metrics: result.detail.metrics.map((item) => ({ ...item, risk: item.tone === 'danger' || item.tone === 'warning' })),
        hr: result.detail
      }
    }
  },
  MARKETING: {
    accessPath: '/marketing/workbench',
    async load(profile) {
      const result = await loadMarketingRoleWorkbench(profile)
      return {
        department: 'MARKETING', sourceLabel: '市场工作台汇总',
        metrics: result.detail.metrics.map((item) => ({ ...item, risk: item.tone === 'danger' || item.tone === 'warning' })),
        marketing: result.detail
      }
    }
  }
}

function hasSnapshotValue(snapshot: DepartmentWorkbenchSnapshot) {
  return snapshot.metrics.some((item) => item.value !== null && item.value !== undefined)
}

export async function loadDepartmentWorkbenchSnapshot(
  profile: WorkbenchProfile,
  canAccess: (path: string) => boolean,
  context: WorkbenchLoadContext = {}
): Promise<WorkbenchDataResult> {
  if (!profile.department) return { status: 'empty', message: '当前岗位不使用部门工作台数据源' }
  const source = departmentSources[profile.department]
  if (!canAccess(source.accessPath)) return { status: 'forbidden', message: '当前岗位没有该部门工作台的数据权限' }
  try {
    const snapshot = await source.load(profile, context)
    snapshot.metrics = snapshot.metrics.filter((item) => canAccess(item.path))
    return hasSnapshotValue(snapshot)
      ? { status: 'ready', snapshot }
      : { status: 'empty', snapshot, message: '当前暂无可展示的部门数据' }
  } catch (error: any) {
    if (error?.response?.status === 403) return { status: 'forbidden', message: '当前岗位没有该部门工作台的数据权限' }
    return { status: 'error', message: error?.message || '部门工作台数据加载失败' }
  }
}
