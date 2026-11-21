import { getLogisticsWorkbenchSummary } from '../api/logistics'
import type { LogisticsWorkbenchSummary } from '../types'
import type { WorkbenchProfile } from './model'
import { responseData, type RoleDepartmentWorkbenchDetail } from './operational'

export interface LogisticsWorkbenchResult {
  summary: LogisticsWorkbenchSummary
  detail: RoleDepartmentWorkbenchDetail
}

export function buildLogisticsWorkbenchDetail(profile: WorkbenchProfile, data: LogisticsWorkbenchSummary): RoleDepartmentWorkbenchDetail {
  const management = profile.hasManagementView
  return {
    department: 'LOGISTICS',
    employeeTitle: '今日后勤保障',
    managementTitle: '后勤部今日运行',
    scopeLabel: management ? '后勤部运行与风险口径' : '机构当班任务口径（非个人绩效）',
    headline: management ? '先处理逾期工单和库存风险，再协调床位、餐饮与设备。' : '先响应报修，再完成出入库、房态、清洁和送餐任务。',
    description: management ? '风险和任务数量来自后勤汇总接口。' : '当前汇总不支持按责任人过滤，数字仅用于当班协作。',
    dataScopeNotice: management ? undefined : '后端暂无按后勤人员限定的本人任务和最近处理记录接口。',
    actions: [
      { label: '报修工单', icon: '修', path: '/logistics/task-center' },
      { label: '库存预警', icon: '库', path: '/logistics/storage/alerts' },
      { label: '入库管理', icon: '入', path: '/logistics/storage/inbound' },
      { label: '出库管理', icon: '出', path: '/logistics/storage/outbound' },
      { label: '房态床位', icon: '床', path: '/logistics/assets/room-state-map' },
      { label: '送餐计划', icon: '餐', path: '/logistics/dining/delivery-plan' },
      { label: '采购审批', icon: '审', path: '/workbench/approvals', managementOnly: true },
      { label: '设备档案', icon: '设', path: '/logistics/maintenance/assets', managementOnly: true }
    ],
    metrics: management ? [
      { key: 'overdue', label: '逾期维修', value: data.maintenanceOverdueCount, suffix: '项', helper: '超过处理时限的工单', path: '/logistics/task-center', tone: 'danger' },
      { key: 'stock', label: '库存预警', value: data.lowStockCount, suffix: '项', helper: '低于安全库存的物资', path: '/logistics/storage/alerts', tone: 'warning' },
      { key: 'purchase', label: '采购待审批', value: data.purchasePendingApprovalCount, suffix: '项', helper: '需要审核的采购申请', path: '/workbench/approvals', tone: 'warning' },
      { key: 'equipment', label: '设备临期', value: data.equipmentDueSoonCount, suffix: '台', helper: '即将需要保养的设备', path: '/logistics/maintenance/assets', tone: 'pending' }
    ] : [
      { key: 'maintenance', label: '待处理报修', value: data.maintenancePendingCount, suffix: '项', helper: '机构待响应工单', path: '/logistics/task-center', tone: 'warning' },
      { key: 'outbound', label: '今日出库', value: data.todayOutboundQty, suffix: '件', helper: '机构当日出库数量', path: '/logistics/storage/outbound', tone: 'normal' },
      { key: 'cleaning', label: '清洁任务', value: data.todayCleaningTaskCount, suffix: '项', helper: '机构今日清洁任务', path: '/logistics/assets/cleaning-record', tone: 'pending' },
      { key: 'delivery', label: '待送餐', value: data.undeliveredCount, suffix: '项', helper: '机构尚未完成的送餐', path: '/logistics/dining/delivery-plan', tone: 'warning' }
    ],
    steps: [
      { title: '先响应报修', description: '确认地点、紧急程度和影响范围，及时接单或反馈。', path: '/logistics/task-center' },
      { title: '再核对物资与房态', description: '处理库存预警、出入库和清洁维护状态。', path: '/logistics/storage/alerts' },
      { title: '最后完成保障交接', description: '复核送餐、设备和未闭环工单，明确下一责任人。', path: '/logistics/workbench' }
    ],
    managementSummary: management ? `当前后勤风险指数 ${Number(data.riskIndex || 0)}，触发 ${Number(data.riskTriggeredCount || 0)} 项风险信号。` : undefined,
    personnelBreakdownAvailable: false
  }
}

export async function loadLogisticsWorkbench(profile: WorkbenchProfile): Promise<LogisticsWorkbenchResult> {
  const response = await getLogisticsWorkbenchSummary(undefined, { silent403: true, silentError: true })
  const summary = responseData<LogisticsWorkbenchSummary>(response)
  return { summary, detail: buildLogisticsWorkbenchDetail(profile, summary) }
}
