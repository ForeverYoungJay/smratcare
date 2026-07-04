import type { RouteRecordRaw } from 'vue-router'

/**
 * 营销模块路由（配置化）
 *
 * 营销下大量页面是同一组件 + 不同 props 的“场景页”：
 *   - LeadScenarioPage：线索/回访/预定等场景（mode + scenario + callbackType 决定行为）
 *   - ContractSigning：按 statusPreset 呈现不同阶段的合同清单
 * 这里用 lead()/contract() 生成器 + 配置表达，避免几十段重复的路由样板；
 * 非重复的独立组件页面保留字面量，便于查阅。
 */

type Lazy = RouteRecordRaw['component']
type Props = Record<string, unknown>

const LeadScenarioPage: Lazy = () => import('../views/marketing/LeadScenarioPage.vue')
const ContractSigning: Lazy = () => import('../views/marketing/ContractSigning.vue')

/** LeadScenarioPage 场景页：props 里的 title 供页面标题，metaTitle 供菜单/面包屑（少数两者不同） */
function lead(path: string, name: string, metaTitle: string, props: Props): RouteRecordRaw {
  return { path, name, component: LeadScenarioPage, props, meta: { title: metaTitle } }
}

/** ContractSigning 合同清单页（按 statusPreset 区分阶段） */
function contract(path: string, name: string, metaTitle: string, props: Props): RouteRecordRaw {
  return { path, name, component: ContractSigning, props, meta: { title: metaTitle } }
}

/** 独立组件页面 */
function view(path: string, name: string, metaTitle: string, component: Lazy): RouteRecordRaw {
  return { path, name, component, meta: { title: metaTitle } }
}

export const marketingRoutes: RouteRecordRaw[] = [
  {
    path: 'marketing',
    name: 'Marketing',
    meta: { title: '营销管理', icon: 'FundProjectionScreenOutlined', navSection: 'operations', navOrder: 50, navPinned: true, roles: ['MARKETING_EMPLOYEE', 'MARKETING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
    children: [
      {
        path: '',
        name: 'MarketingHub',
        component: () => import('../views/ModuleHub.vue'),
        meta: { title: '营销导航', hidden: true }
      },
      view('workbench', 'MarketingWorkbench', '销售运营工作台（首页）', () => import('../views/marketing/Workbench.vue')),
      {
        path: 'leads',
        name: 'MarketingLeads',
        meta: { title: '客户线索管理' },
        redirect: '/marketing/leads/all',
        children: [
          view('all', 'MarketingLeadsAll', '全部线索', () => import('../views/marketing/SalesPipeline.vue')),
          view('intent', 'MarketingLeadsIntent', '意向客户', () => import('../views/marketing/SalesIntent.vue')),
          view('invalid', 'MarketingLeadsInvalid', '失效客户', () => import('../views/marketing/SalesInvalid.vue')),
          lead('blacklist', 'MarketingLeadsBlacklist', '黑名单', { mode: 'invalid', title: '黑名单', subTitle: '黑名单客户集中处置与恢复', scenario: 'blacklist' }),
          lead('unknown-source', 'MarketingLeadsUnknownSource', '渠道不明客户', { mode: 'consultation', title: '渠道不明客户', subTitle: '缺失渠道的线索预警与补全', scenario: 'source_unknown' }),
          lead('medical-transfer', 'MarketingLeadsMedicalTransfer', '外来就医转线索（来自医护模块）', { mode: 'consultation', title: '外来就医转线索', subTitle: '来自医护模块的潜客自动流入', scenario: 'source_medical' })
        ]
      },
      {
        path: 'followup',
        name: 'MarketingFollowupCompat',
        meta: { title: '客户互动中心（兼容）', hidden: true },
        redirect: '/marketing/interactions/records',
        children: [
          { path: 'records', redirect: '/marketing/interactions/records', meta: { hidden: true } },
          { path: 'due', redirect: '/marketing/interactions/due', meta: { hidden: true } },
          { path: 'today', redirect: '/marketing/interactions/today', meta: { hidden: true } },
          { path: 'overdue', redirect: '/marketing/interactions/overdue', meta: { hidden: true } }
        ]
      },
      {
        path: 'interactions',
        name: 'MarketingInteractions',
        meta: { title: '客户互动中心' },
        redirect: '/marketing/interactions/records',
        children: [
          lead('records', 'MarketingFollowupRecords', '客户互动记录', { mode: 'callback', title: '客户互动记录', subTitle: '销售跟进、回访计划与执行轨迹统一收口' }),
          lead('due', 'MarketingFollowupDue', '待回访提醒', { mode: 'callback', title: '待回访提醒', subTitle: '待执行回访任务清单', scenario: 'follow_today' }),
          lead('today', 'MarketingFollowupToday', '今日跟进计划', { mode: 'callback', title: '今日跟进计划', subTitle: '今日需完成的营销跟进', scenario: 'follow_today' }),
          lead('overdue', 'MarketingFollowupOverdue', '逾期未跟进', { mode: 'callback', title: '逾期未跟进', subTitle: '逾期未跟进客户风险池', scenario: 'follow_overdue' })
        ]
      },
      {
        path: 'funnel',
        name: 'MarketingFunnel',
        meta: { title: '线索阶段管理（销售漏斗）' },
        redirect: '/marketing/funnel/consultation',
        children: [
          lead('consultation', 'MarketingFunnelConsultation', '咨询阶段', { mode: 'consultation', title: '咨询阶段', subTitle: '咨询线索池：新增咨询、分配跟进与来源分析' }),
          lead('evaluation', 'MarketingFunnelEvaluation', '评估阶段', { mode: 'intent', title: '评估阶段', subTitle: '高意向客户评估推进与评估任务闭环' }),
          contract('signing', 'MarketingFunnelSigning', '签约阶段', { statusPreset: 'pending_sign', title: '签约阶段', subTitle: '待签署合同清单与签约推进' }),
          contract('admission', 'MarketingFunnelAdmission', '入住阶段', { statusPreset: 'pending_bed_select', title: '入住阶段', subTitle: '待办理入住合同与选床办理联动' }),
          lead('lost', 'MarketingFunnelLost', '流失阶段', { mode: 'invalid', title: '流失阶段', subTitle: '流失客户归因分析与二次激活' })
        ]
      },
      {
        path: 'reservation',
        name: 'MarketingReservation',
        meta: { title: '预定与床态联动' },
        redirect: '/marketing/reservation/records',
        children: [
          view('records', 'MarketingReservationRecords', '床位预定记录', () => import('../views/marketing/SalesReservation.vue')),
          lead('lock', 'MarketingReservationLock', '锁床管理', { mode: 'reservation', title: '锁床管理', subTitle: '锁床客户与锁床状态跟踪', scenario: 'lock_bed' }),
          lead('expiring', 'MarketingReservationExpiring', '预定到期提醒', { mode: 'reservation', title: '预定到期提醒', subTitle: '锁床临期与预定超时预警', scenario: 'expiring_lock' }),
          view('panorama', 'MarketingReservationPanorama', '床态全景（销售视角）', () => import('../views/marketing/RoomPanorama.vue'))
        ]
      },
      {
        path: 'contracts',
        name: 'MarketingContracts',
        meta: { title: '合同管理' },
        redirect: '/marketing/contracts/pending',
        children: [
          contract('pending', 'MarketingContractsPending', '待签约合同', { statusPreset: 'pending_sign', title: '待签约合同', subTitle: '待签署合同统一处理：查看、入住办理、最终签署', disableDefaultFlowStagePreset: true }),
          contract('signed', 'MarketingContractsSigned', '已签合同', { statusPreset: 'signed', title: '已签合同', subTitle: '已签署合同集中查询、补录与变更协同' }),
          view('renewal', 'MarketingContractsRenewal', '续签提醒', () => import('../views/marketing/ContractManagement.vue')),
          view('change', 'MarketingContractsChange', '合同变更', () => import('../views/marketing/ContractChange.vue')),
          view('attachments', 'MarketingContractsAttachments', '合同附件', () => import('../views/marketing/ContractAttachments.vue'))
        ]
      },
      {
        path: 'callback',
        name: 'MarketingCallback',
        meta: { title: '签后回访管理' },
        redirect: '/marketing/callback/checkin',
        children: [
          lead('checkin', 'MarketingCallbackCheckin', '入住后回访', { mode: 'callback', callbackType: 'checkin', title: '入住后回访', subTitle: '入住后客户回访计划、执行与风险跟踪' }),
          lead('trial', 'MarketingCallbackTrial', '试住回访', { mode: 'callback', callbackType: 'trial', title: '试住回访', subTitle: '试住客户回访安排与转化促进跟踪' }),
          lead('discharge', 'MarketingCallbackDischarge', '退住回访', { mode: 'callback', callbackType: 'discharge', title: '退住回访', subTitle: '退住客户回访与原因沉淀闭环' }),
          view('score', 'MarketingCallbackScore', '回访质量评分', () => import('../views/marketing/CallbackQuality.vue'))
        ]
      },
      {
        path: 'reports',
        name: 'MarketingReports',
        meta: { title: '销售报表中心' },
        redirect: '/marketing/reports/conversion',
        children: [
          view('conversion', 'MarketingReportConversion', '转化率统计', () => import('../views/marketing/report/Conversion.vue')),
          view('consultation', 'MarketingReportConsultation', '咨询统计', () => import('../views/marketing/report/Consultation.vue')),
          view('unknown-channel', 'MarketingReportUnknownChannel', '不明渠道统计', () => import('../views/marketing/report/UnknownChannel.vue')),
          view('followup', 'MarketingReportFollowup', '跟进统计', () => import('../views/marketing/report/Followup.vue')),
          view('callback', 'MarketingReportCallback', '回访统计', () => import('../views/marketing/report/Callback.vue')),
          view('channel', 'MarketingReportChannel', '渠道评估', () => import('../views/marketing/report/Channel.vue')),
          view('sales-performance', 'MarketingReportSalesPerformance', '销售人员业绩', () => import('../views/marketing/report/SalesPerformance.vue')),
          view('channel-rank', 'MarketingReportChannelRank', '渠道贡献排名', () => import('../views/marketing/report/ChannelRank.vue')),
          view('snapshots', 'MarketingReportSnapshots', '经营快照', () => import('../views/marketing/report/Snapshots.vue'))
        ]
      },
      { path: 'contract-signing', name: 'MarketingContractSigning', component: ContractSigning, meta: { title: '合同签约（兼容）', hidden: true } },
      { path: 'contract-management', name: 'MarketingContractManagement', component: () => import('../views/marketing/ContractManagement.vue'), meta: { title: '合同到期管理（兼容）', hidden: true } },
      { path: 'room-panorama', name: 'MarketingRoomPanorama', component: () => import('../views/marketing/RoomPanorama.vue'), meta: { title: '房态全景（兼容）', hidden: true } },
      view('plan', 'MarketingPlan', '营销方案与审批', () => import('../views/marketing/MarketingPlan.vue')),
      {
        path: 'sales',
        name: 'MarketingSalesLegacy',
        meta: { title: '销售跟进（兼容）', hidden: true },
        redirect: '/marketing/leads/all',
        children: [
          { path: 'pipeline', redirect: '/marketing/leads/all', meta: { hidden: true } },
          { path: 'consultation', redirect: '/marketing/leads/all?tab=consultation', meta: { hidden: true } },
          { path: 'intent', redirect: '/marketing/leads/intent', meta: { hidden: true } },
          { path: 'reservation', redirect: '/marketing/reservation/records', meta: { hidden: true } },
          { path: 'invalid', redirect: '/marketing/leads/invalid', meta: { hidden: true } },
          { path: 'callback', redirect: '/marketing/interactions/due', meta: { hidden: true } }
        ]
      }
    ]
  }
]
