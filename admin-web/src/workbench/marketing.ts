import { getMarketingWorkbenchSummary } from '../api/marketing'
import type { MarketingWorkbenchSummary } from '../types'
import type { WorkbenchProfile } from './model'
import { responseData, type RoleDepartmentWorkbenchDetail } from './operational'

export interface MarketingRoleWorkbenchResult { summary: MarketingWorkbenchSummary; detail: RoleDepartmentWorkbenchDetail }

export function buildMarketingRoleWorkbenchDetail(profile: WorkbenchProfile, data: MarketingWorkbenchSummary): RoleDepartmentWorkbenchDetail {
  const management = profile.hasManagementView
  return {
    department: 'MARKETING',
    employeeTitle: '今日客户跟进',
    managementTitle: '市场部今日运行',
    scopeLabel: management ? '市场部转化与风险口径' : '机构客户池口径（非个人业绩）',
    headline: management ? '先处理逾期回访和高意向客户，再推进签约、选床与入住。' : '先完成今日回访，再推进到访、试住、签约和入住。',
    description: management ? '漏斗、回访和业绩数据来自市场汇总接口。' : '当前汇总不支持按销售人员过滤，数字仅用于团队协作。',
    dataScopeNotice: management ? undefined : '后端暂无按当前销售人员限定的本人线索、回访和业绩汇总接口。',
    actions: [
      { label: '新增线索', icon: '新', path: '/marketing/leads/all' },
      { label: '今日跟进', icon: '跟', path: '/marketing/interactions/today' },
      { label: '待回访', icon: '访', path: '/marketing/interactions/due' },
      { label: '到访预约', icon: '约', path: '/marketing/reservation/records' },
      { label: '合同办理', icon: '合', path: '/marketing/contracts/pending' },
      { label: '销售漏斗', icon: '漏', path: '/marketing/funnel/consultation' },
      { label: '渠道报表', icon: '渠', path: '/marketing/reports/channel', managementOnly: true },
      { label: '营销审批', icon: '审', path: '/marketing/plan', managementOnly: true }
    ],
    metrics: management ? [
      { key: 'overdue', label: '逾期未跟进', value: data.followup?.overdue, suffix: '位', helper: '超过计划回访时间', path: '/marketing/interactions/overdue', tone: 'danger' },
      { key: 'intent', label: '高意向客户', value: data.followup?.highIntentCount, suffix: '位', helper: '优先推进评估与到访', path: '/marketing/leads/intent', tone: 'warning' },
      { key: 'sign', label: '待签约', value: data.contract?.pendingSignCount, suffix: '份', helper: '需要推进签署的合同', path: '/marketing/contracts/pending', tone: 'pending' },
      { key: 'deal', label: '本月成交', value: data.funnel?.monthDealCount, suffix: '单', helper: `转化率 ${Number(data.funnel?.monthConversionRate || 0).toFixed(1)}%`, path: '/marketing/reports/conversion', tone: 'normal' }
    ] : [
      { key: 'today', label: '团队今日回访', value: data.followup?.todayDue, suffix: '位', helper: '机构客户池口径', path: '/marketing/interactions/today', tone: 'pending' },
      { key: 'overdue', label: '团队逾期回访', value: data.followup?.overdue, suffix: '位', helper: '机构客户池口径', path: '/marketing/interactions/overdue', tone: 'danger' },
      { key: 'consult', label: '今日新增咨询', value: data.funnel?.todayConsultCount, suffix: '条', helper: '机构新增咨询', path: '/marketing/leads/all', tone: 'normal' },
      { key: 'sign', label: '待签约客户', value: data.funnel?.pendingSignCount, suffix: '位', helper: '团队待推进客户', path: '/marketing/contracts/pending', tone: 'warning' }
    ],
    steps: [
      { title: '先完成今日回访', description: '确认客户当前意向、顾虑和下一次联系时间。', path: '/marketing/interactions/today' },
      { title: '再推进到访与试住', description: '把高意向客户推进到参观、评估、锁床或试住。', path: '/marketing/reservation/records' },
      { title: '最后闭环合同与入住', description: '补齐合同资料并同步床位、财务和入住准备。', path: '/marketing/contracts/pending' }
    ],
    managementSummary: management ? `本月成交 ${Number(data.performance?.monthDealCount || 0)} 单，销售额 ${Number(data.performance?.monthAmount || 0).toLocaleString('zh-CN')} 元。` : undefined,
    personnelBreakdownAvailable: false
  }
}

export async function loadMarketingRoleWorkbench(profile: WorkbenchProfile): Promise<MarketingRoleWorkbenchResult> {
  const response = await getMarketingWorkbenchSummary()
  const summary = responseData<MarketingWorkbenchSummary>(response)
  return { summary, detail: buildMarketingRoleWorkbenchDetail(profile, summary) }
}
