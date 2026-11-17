import type { RouteComponent, RouteRecordRaw } from 'vue-router'
import { legacyMarketingKeys, legacyMarketingRedirect } from './marketingRedirects'

/**
 * 营销模块路由（配置化）
 *
 * 菜单只保留 5 个主页面：市场工作台 / 客户与跟进 / 参观与预订 / 合同与转入住 / 市场运营。
 * 客户详情、跟进、预订、审批都在主页面的标签或抽屉里完成。
 * 旧的线索/互动/漏斗/预订/回访地址跳到新页面的对应标签（见 marketingRedirects.ts）；
 * 合同办理、报表等仍是独立办理页，只隐藏菜单，从主页面链接进入。
 */

type Lazy = RouteComponent
type Props = Record<string, unknown>

const ContractSigning: Lazy = () => import('../views/marketing/ContractSigning.vue')
const MANAGERS = ['MARKETING_MINISTER', 'DIRECTOR', 'ADMIN']

/** ContractSigning 合同清单页（按 statusPreset 区分阶段） */
function contract(path: string, name: string, metaTitle: string, props: Props): RouteRecordRaw {
  return { path, name, component: ContractSigning, props, meta: { title: metaTitle } }
}

/** 独立组件页面 */
function view(path: string, name: string, metaTitle: string, component: Lazy): RouteRecordRaw {
  return { path, name, component, meta: { title: metaTitle } }
}

function hide(route: RouteRecordRaw): RouteRecordRaw {
  return { ...route, meta: { ...route.meta, hidden: true } } as RouteRecordRaw
}

function legacy(key: string): RouteRecordRaw {
  return { path: key, redirect: (to) => legacyMarketingRedirect(key, to.query), meta: { hidden: true } }
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
      view('workbench', 'MarketingWorkbench', '市场工作台', () => import('../views/marketing/Workbench.vue')),
      view('customers', 'MarketingCustomers', '客户与跟进', () => import('../views/marketing/center/CustomerCenter.vue')),
      view('visits', 'MarketingVisits', '参观与预订', () => import('../views/marketing/center/VisitCenter.vue')),
      {
        path: 'contracts',
        name: 'MarketingContracts',
        meta: { title: '合同与转入住' },
        children: [
          { path: '', name: 'MarketingContractCenter', component: () => import('../views/marketing/center/ContractCenter.vue'), meta: { title: '合同与转入住' } },
          ...[
            contract('pending', 'MarketingContractsPending', '待签约合同', { statusPreset: 'pending_sign', title: '待签约合同', subTitle: '待签署合同统一处理：查看、入住办理、最终签署', disableDefaultFlowStagePreset: true }),
            contract('signed', 'MarketingContractsSigned', '已签合同', { statusPreset: 'signed', title: '已签合同', subTitle: '已签署合同集中查询、补录与变更协同' }),
            view('renewal', 'MarketingContractsRenewal', '续签提醒', () => import('../views/marketing/ContractManagement.vue')),
            view('change', 'MarketingContractsChange', '合同变更', () => import('../views/marketing/ContractChange.vue')),
            view('attachments', 'MarketingContractsAttachments', '合同附件', () => import('../views/marketing/ContractAttachments.vue'))
          ]
        ]
      },
      {
        path: 'operations',
        name: 'MarketingOperations',
        component: () => import('../views/marketing/center/OperationsCenter.vue'),
        meta: { title: '市场运营', roles: MANAGERS }
      },
      // —— 以下为收敛前的旧地址，不进菜单 ——
      ...legacyMarketingKeys.filter((key) => key !== 'plan').map(legacy),
      { path: 'plan', name: 'MarketingPlan', redirect: (to) => legacyMarketingRedirect('plan', to.query), meta: { title: '营销方案与审批', hidden: true, roles: MANAGERS } },
      {
        path: 'followup',
        name: 'MarketingFollowupCompat',
        meta: { title: '客户互动中心（兼容）', hidden: true },
        redirect: '/marketing/customers?tab=followup',
        children: ['records', 'due', 'today', 'overdue'].map((entry): RouteRecordRaw => ({
          path: entry, redirect: (to) => legacyMarketingRedirect(`interactions/${entry}`, to.query), meta: { hidden: true }
        }))
      },
      {
        path: 'funnel',
        name: 'MarketingFunnel',
        meta: { title: '线索阶段管理（销售漏斗）', hidden: true },
        children: [
          contract('signing', 'MarketingFunnelSigning', '签约阶段', { statusPreset: 'pending_sign', title: '签约阶段', subTitle: '待签署合同清单与签约推进' }),
          contract('admission', 'MarketingFunnelAdmission', '入住阶段', { statusPreset: 'pending_bed_select', title: '入住阶段', subTitle: '待办理入住合同与选床办理联动' })
        ]
      },
      hide(view('reservation/panorama', 'MarketingReservationPanorama', '床态全景（销售视角）', () => import('../views/marketing/RoomPanorama.vue'))),
      hide(view('callback/score', 'MarketingCallbackScore', '回访质量评分', () => import('../views/marketing/CallbackQuality.vue'))),
      {
        path: 'reports',
        name: 'MarketingReports',
        meta: { title: '经营报表明细', roles: MANAGERS },
        redirect: '/marketing/operations',
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
      {
        path: 'sales',
        name: 'MarketingSalesLegacy',
        meta: { title: '销售跟进（兼容）', hidden: true },
        redirect: '/marketing/customers',
        children: [
          { path: 'pipeline', redirect: '/marketing/customers', meta: { hidden: true } },
          { path: 'consultation', redirect: '/marketing/customers', meta: { hidden: true } },
          { path: 'intent', redirect: '/marketing/customers?tab=intent', meta: { hidden: true } },
          { path: 'reservation', redirect: '/marketing/visits?tab=reservation', meta: { hidden: true } },
          { path: 'invalid', redirect: '/marketing/customers?tab=invalid', meta: { hidden: true } },
          { path: 'callback', redirect: '/marketing/customers?tab=followup&view=due', meta: { hidden: true } }
        ]
      }
    ]
  }
]
