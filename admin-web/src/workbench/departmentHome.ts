import type { DepartmentCode } from '../access/policy'

export type DepartmentHomeDataState = 'loading' | 'ready' | 'empty' | 'forbidden' | 'error'

export interface DepartmentHomeMetric {
  key: string
  label: string
  value?: number | string | null
  suffix?: string
  helper?: string
  path: string
  tone?: 'normal' | 'warning' | 'danger' | 'success'
}

export interface DepartmentHomeStep {
  title: string
  path: string
}

export interface DepartmentHomeExtraGroup {
  key: string
  label: string
  description?: string
  paths: string[]
}

export interface DepartmentHomeConfig {
  department: DepartmentCode
  label: string
  homePath: string
  moduleRoots: string[]
  summary: string
  steps: DepartmentHomeStep[]
  extraGroups?: DepartmentHomeExtraGroup[]
}

export const DEPARTMENT_HOME_CONFIG: Record<DepartmentCode, DepartmentHomeConfig> = {
  NURSING: {
    department: 'NURSING',
    label: '护理部',
    homePath: '/care/home',
    moduleRoots: ['/care'],
    summary: '从计划、执行到交接质检，按当天工作顺序快速处理。',
    steps: [
      { title: '计划与派发', path: '/care/workbench/plan' },
      { title: '护理执行', path: '/care/today' },
      { title: '护理记录', path: '/care/service/nursing-records' },
      { title: '异常处理', path: '/care/exception' },
      { title: '交接与质检', path: '/medical-care/handovers' }
    ],
    extraGroups: [
      {
        key: 'nursing-collaboration',
        label: '医护协作',
        paths: ['/medical-care/care-task-board', '/medical-care/order-executions', '/medical-care/handovers', '/medical-care/nursing-quality']
      }
    ]
  },
  MEDICAL: {
    department: 'MEDICAL',
    label: '医务部',
    homePath: '/medical-care/center',
    moduleRoots: ['/medical-care'],
    summary: '围绕长者健康风险，完成评估、医嘱、执行与随访闭环。',
    steps: [
      { title: '评估与巡诊', path: '/medical-care/rounds' },
      { title: '医嘱', path: '/medical-care/orders' },
      { title: '用药与执行', path: '/medical-care/medication-registration' },
      { title: '异常处置', path: '/medical-care/unified-task-center' },
      { title: '交接随访', path: '/medical-care/handovers' }
    ],
    extraGroups: [
      {
        key: 'medical-resident',
        label: '长者健康',
        paths: ['/elder/resident-360', '/elder/assessment/ability/admission', '/elder/assessment/ability/continuous']
      }
    ]
  },
  FINANCE: {
    department: 'FINANCE',
    label: '财务部',
    homePath: '/finance/workbench',
    moduleRoots: ['/finance'],
    summary: '按账单、收款、对账和结算顺序，优先处理资金风险。',
    steps: [
      { title: '账单审核', path: '/finance/admission-fee-audit' },
      { title: '收费开票', path: '/finance/payments/cashier-desk' },
      { title: '对账', path: '/finance/reconcile/issue-center' },
      { title: '欠费与结算', path: '/finance/bills/follow-up' },
      { title: '财务报表', path: '/finance/reports/overall' }
    ]
  },
  LOGISTICS: {
    department: 'LOGISTICS',
    label: '后勤部',
    homePath: '/logistics/workbench',
    moduleRoots: ['/logistics'],
    summary: '把物资、维修、床位与餐饮保障按执行链路集中处理。',
    steps: [
      { title: '需求与工单', path: '/logistics/task-center' },
      { title: '采购入库', path: '/logistics/storage/inbound' },
      { title: '库存出库', path: '/logistics/storage/outbound' },
      { title: '维修与送餐', path: '/logistics/assets/maintenance-record' },
      { title: '验收分析', path: '/logistics/reports/maintenance-todo-log' }
    ]
  },
  HR: {
    department: 'HR',
    label: '行政人事部',
    homePath: '/hr/overview',
    moduleRoots: ['/hr'],
    summary: '覆盖员工从入职到离职的完整周期，并集中处理考勤与合规事项。',
    steps: [
      { title: '通知公告', path: '/oa/notice' },
      { title: '制度与文档', path: '/oa/document' },
      { title: '我的考勤', path: '/workbench/attendance' },
      { title: '招聘入职', path: '/hr/recruitment/needs' },
      { title: '档案合同', path: '/hr/profile/basic' }
    ],
    extraGroups: [
      {
        key: 'hr-oa',
        label: '行政协同',
        paths: ['/oa/notice', '/oa/document', '/workbench/attendance', '/workbench/approvals']
      }
    ]
  },
  MARKETING: {
    department: 'MARKETING',
    label: '市场部',
    homePath: '/marketing/workbench',
    moduleRoots: ['/marketing'],
    summary: '从线索到签约入住，按客户转化阶段推进每一次跟进。',
    steps: [
      { title: '线索进入', path: '/marketing/leads/all' },
      { title: '跟进评估', path: '/marketing/interactions/today' },
      { title: '预订锁床', path: '/marketing/reservation/records' },
      { title: '签约入住', path: '/marketing/contracts/pending' },
      { title: '签后回访', path: '/marketing/callback/checkin' }
    ]
  }
}

export function getDepartmentHomeConfig(department: DepartmentCode) {
  return DEPARTMENT_HOME_CONFIG[department]
}

export function formatDepartmentHomeMetric(metric: Pick<DepartmentHomeMetric, 'value' | 'suffix'>) {
  if (metric.value === undefined || metric.value === null || metric.value === '') return '--'
  const value = typeof metric.value === 'number' ? metric.value.toLocaleString('zh-CN') : metric.value
  return `${value}${metric.suffix || ''}`
}
