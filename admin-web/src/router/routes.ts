import type { RouteRecordRaw } from 'vue-router'

export const routes: RouteRecordRaw[] = [
  {
    path: '/enterprise',
    redirect: '/home',
    meta: { title: '企业首页', hidden: true }
  },
  {
    path: '/admin',
    redirect: '/login?redirect=/portal',
    meta: { title: '后台入口', hidden: true }
  },
  {
    path: '/home',
    name: 'EnterpriseHome',
    component: () => import('../views/EnterpriseHome.vue'),
    meta: { title: '弋阳龟峰颐养中心', hidden: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('../views/Forbidden.vue'),
    meta: { title: '无权限', hidden: true }
  },
  {
    path: '/',
    name: 'Root',
    component: () => import('../layouts/BasicLayout.vue'),
    redirect: '/portal',
    children: [
      {
        path: 'portal',
        name: 'Portal',
        component: () => import('../views/Portal.vue'),
        meta: { title: '首页', icon: 'HomeOutlined' }
      },
      {
        path: 'workbench',
        name: 'Workbench',
        component: () => import('../layouts/RouteView.vue'),
        meta: { title: '工作台', icon: 'AppstoreOutlined' },
        redirect: '/workbench/overview',
        children: [
          {
            path: 'overview',
            name: 'WorkbenchOverview',
            component: () => import('../views/workbench/WorkbenchHome.vue'),
            meta: { title: '我的工作台' }
          },
          {
            path: 'todo',
            name: 'WorkbenchTodo',
            redirect: '/oa/todo',
            meta: { title: '我的待办' }
          },
          {
            path: 'my-info',
            name: 'WorkbenchMyInfo',
            redirect: '/oa/my-info',
            meta: { title: '我的信息' }
          },
          {
            path: 'attendance',
            name: 'WorkbenchAttendance',
            redirect: '/oa/attendance-leave',
            meta: { title: '我的考勤与请假' }
          },
          {
            path: 'reports',
            name: 'WorkbenchReports',
            redirect: '/oa/work-report',
            meta: { title: '我的总结' }
          },
          {
            path: 'approvals',
            name: 'WorkbenchApprovals',
            redirect: '/oa/approval',
            meta: { title: '我的审批' }
          }
        ]
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: 'Dashboard', icon: 'DashboardOutlined', hidden: true }
      },
      {
        path: 'elder',
        name: 'Elder',
        meta: { title: '长者管理', icon: 'TeamOutlined' },
        redirect: '/elder/in-hospital-overview',
        children: [
          {
            path: 'resident-360',
            name: 'ElderResident360',
            component: () => import('../views/elder/resident360/InHospitalOverview.vue'),
            meta: { title: '长者总览', hidden: true }
          },
          {
            path: 'in-hospital-overview',
            name: 'ElderInHospitalOverview',
            component: () => import('../views/elder/resident360/InHospitalOverview.vue'),
            meta: { title: '在院服务总览' }
          },
          {
            path: 'bed-panorama',
            name: 'ElderBedPanorama',
            component: () => import('../views/elder/resident360/BedPanorama.vue'),
            meta: { title: '床态全景' }
          },
          {
            path: 'list',
            name: 'ElderList',
            component: () => import('../views/elder/List.vue'),
            meta: { title: '长者列表' }
          },
          {
            path: 'admission-assessment',
            name: 'ElderAdmissionAssessment',
            redirect: '/elder/assessment/ability/admission',
            meta: { title: '入住评估', hidden: true }
          },
          {
            path: 'assessment',
            name: 'ElderAssessment',
            meta: {
              title: '评估管理',
              roles: ['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_EMPLOYEE', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']
            },
            redirect: '/elder/assessment/ability/admission',
            children: [
              {
                path: 'ability',
                name: 'ElderAssessmentAbility',
                meta: { title: '能力评估管理' },
                redirect: '/elder/assessment/ability/admission',
                children: [
                  {
                    path: 'admission',
                    name: 'ElderAssessmentAdmission',
                    component: () => import('../views/assessment/Admission.vue'),
                    meta: { title: '入住评估' }
                  },
                  {
                    path: 'registration',
                    name: 'ElderAssessmentRegistration',
                    component: () => import('../views/assessment/Registration.vue'),
                    meta: { title: '评估登记' }
                  },
                  {
                    path: 'continuous',
                    name: 'ElderAssessmentContinuous',
                    component: () => import('../views/assessment/Continuous.vue'),
                    meta: { title: '持续评估' }
                  },
                  {
                    path: 'archive',
                    name: 'ElderAssessmentArchive',
                    component: () => import('../views/assessment/Archive.vue'),
                    meta: { title: '评估档案' }
                  }
                ]
              },
              {
                path: 'other-scale',
                name: 'ElderAssessmentOtherScale',
                component: () => import('../views/assessment/OtherScale.vue'),
                meta: { title: '其他量表评估' }
              },
              {
                path: 'template',
                name: 'ElderAssessmentTemplate',
                component: () => import('../views/assessment/Template.vue'),
                meta: { title: '量表模板', roles: ['ADMIN'], hidden: true }
              },
              {
                path: 'cognitive',
                name: 'ElderAssessmentCognitive',
                component: () => import('../views/assessment/Cognitive.vue'),
                meta: { title: '认知能力评估' }
              },
              {
                path: 'self-care',
                name: 'ElderAssessmentSelfCare',
                component: () => import('../views/assessment/SelfCare.vue'),
                meta: { title: '自理能力评估' }
              }
            ]
          },
          {
            path: 'contracts-invoices',
            name: 'ElderContractsInvoices',
            component: () => import('../views/elder/resident360/ContractsInvoices.vue'),
            meta: { title: '合同与票据' }
          },
          {
            path: 'admission-processing',
            name: 'ElderAdmissionProcessing',
            component: () => import('../views/elder/Admission.vue'),
            meta: { title: '入住办理' }
          },
          {
            path: 'status-change',
            name: 'ElderStatusChangeCenter',
            component: () => import('../layouts/RouteView.vue'),
            meta: { title: '入住状态变更' },
            redirect: '/elder/status-change/center',
            children: [
              {
                path: 'center',
                name: 'ElderStatusChangeOverview',
                component: () => import('../views/elder/resident360/StatusChangeCenter.vue'),
                meta: { title: '变更总览' }
              },
              {
                path: 'outing',
                name: 'ElderStatusChangeOuting',
                component: () => import('../views/elder/Outing.vue'),
                meta: { title: '外出登记' }
              },
              {
                path: 'visit-register',
                name: 'ElderStatusChangeVisitRegister',
                component: () => import('../views/elder/VisitRegister.vue'),
                meta: { title: '来访登记' }
              },
              {
                path: 'discharge-apply',
                name: 'ElderStatusChangeDischargeApply',
                component: () => import('../views/elder/DischargeApply.vue'),
                meta: { title: '退住申请' }
              },
              {
                path: 'trial-stay',
                name: 'ElderStatusChangeTrialStay',
                component: () => import('../views/elder/TrialStay.vue'),
                meta: { title: '试住登记' }
              },
              {
                path: 'medical-outing',
                name: 'ElderStatusChangeMedicalOuting',
                component: () => import('../views/elder/MedicalOuting.vue'),
                meta: {
                  title: '外出就医登记',
                  roles: ['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_EMPLOYEE', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']
                }
              },
              {
                path: 'death-register',
                name: 'ElderStatusChangeDeathRegister',
                component: () => import('../views/elder/DeathRegister.vue'),
                meta: {
                  title: '死亡登记',
                  roles: ['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_EMPLOYEE', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']
                }
              },
              {
                path: 'discharge-settlement',
                name: 'ElderStatusChangeDischargeSettlement',
                component: () => import('../views/elder/resident360/DischargeSettlement.vue'),
                meta: { title: '退院结算' }
              }
            ]
          },
          {
            path: 'discharge-settlement',
            name: 'ElderDischargeSettlement',
            redirect: '/elder/status-change/discharge-settlement',
            meta: { hidden: true }
          },
          {
            path: 'create',
            name: 'ElderCreate',
            component: () => import('../views/elder/Create.vue'),
            meta: { title: '新建老人', hidden: true }
          },
          {
            path: 'edit/:id',
            name: 'ElderEdit',
            component: () => import('../views/elder/Edit.vue'),
            meta: { title: '编辑老人', hidden: true }
          },
          {
            path: 'detail/:id',
            name: 'ElderDetail',
            component: () => import('../views/elder/Detail.vue'),
            meta: { title: '老人详情', hidden: true }
          },
          {
            path: 'crm',
            name: 'ElderCrm',
            component: () => import('../views/elder/Crm.vue'),
            meta: { title: 'CRM线索(旧)', hidden: true }
          },
          {
            path: 'admission',
            name: 'ElderAdmission',
            redirect: '/elder/admission-processing',
            meta: { hidden: true }
          },
          {
            path: 'outing',
            name: 'ElderOuting',
            redirect: '/elder/status-change/outing',
            meta: { hidden: true }
          },
          {
            path: 'visit-register',
            name: 'ElderVisitRegister',
            redirect: '/elder/status-change/visit-register',
            meta: { hidden: true }
          },
          {
            path: 'incident',
            name: 'ElderIncident',
            component: () => import('../views/life/Incident.vue'),
            meta: { title: '事故登记', hidden: true }
          },
          {
            path: 'discharge-apply',
            name: 'ElderDischargeApply',
            redirect: '/elder/status-change/discharge-apply',
            meta: { hidden: true }
          },
          {
            path: 'discharge',
            name: 'ElderDischarge',
            component: () => import('../views/elder/Discharge.vue'),
            meta: { title: '退住登记', hidden: true }
          },
          {
            path: 'trial-stay',
            name: 'ElderTrialStay',
            redirect: '/elder/status-change/trial-stay',
            meta: { hidden: true }
          },
          {
            path: 'medical-outing',
            name: 'ElderMedicalOutingRegister',
            redirect: '/elder/status-change/medical-outing',
            meta: { hidden: true }
          },
          {
            path: 'death-register',
            name: 'ElderDeathRegister',
            redirect: '/elder/status-change/death-register',
            meta: { hidden: true }
          },
          {
            path: 'change-log',
            name: 'ElderChangeLog',
            component: () => import('../views/elder/ChangeLog.vue'),
            meta: { title: '变更记录', hidden: true }
          }
        ]
      },
      {
        path: 'marketing',
        name: 'Marketing',
        meta: { title: '营销管理', icon: 'FundProjectionScreenOutlined', roles: ['MARKETING_EMPLOYEE', 'MARKETING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
        redirect: '/marketing/workbench',
        children: [
          {
            path: 'workbench',
            name: 'MarketingWorkbench',
            component: () => import('../views/marketing/Workbench.vue'),
            meta: { title: '销售运营工作台（首页）' }
          },
          {
            path: 'leads',
            name: 'MarketingLeads',
            meta: { title: '客户线索管理' },
            redirect: '/marketing/leads/all',
            children: [
              {
                path: 'all',
                name: 'MarketingLeadsAll',
                component: () => import('../views/marketing/SalesPipeline.vue'),
                meta: { title: '全部线索' }
              },
              {
                path: 'intent',
                name: 'MarketingLeadsIntent',
                component: () => import('../views/marketing/SalesIntent.vue'),
                meta: { title: '意向客户' }
              },
              {
                path: 'invalid',
                name: 'MarketingLeadsInvalid',
                component: () => import('../views/marketing/SalesInvalid.vue'),
                meta: { title: '失效客户' }
              },
              {
                path: 'blacklist',
                name: 'MarketingLeadsBlacklist',
                component: () => import('../views/marketing/LeadScenarioPage.vue'),
                props: { mode: 'invalid', title: '黑名单', subTitle: '黑名单客户集中处置与恢复', scenario: 'blacklist' },
                meta: { title: '黑名单' }
              },
              {
                path: 'unknown-source',
                name: 'MarketingLeadsUnknownSource',
                component: () => import('../views/marketing/LeadScenarioPage.vue'),
                props: { mode: 'consultation', title: '渠道不明客户', subTitle: '缺失渠道的线索预警与补全', scenario: 'source_unknown' },
                meta: { title: '渠道不明客户' }
              },
              {
                path: 'medical-transfer',
                name: 'MarketingLeadsMedicalTransfer',
                component: () => import('../views/marketing/LeadScenarioPage.vue'),
                props: { mode: 'consultation', title: '外来就医转线索', subTitle: '来自医护模块的潜客自动流入', scenario: 'source_medical' },
                meta: { title: '外来就医转线索（来自医护模块）' }
              }
            ]
          },
          {
            path: 'followup',
            name: 'MarketingFollowup',
            meta: { title: '跟进管理' },
            redirect: '/marketing/followup/records',
            children: [
              {
                path: 'records',
                name: 'MarketingFollowupRecords',
                component: () => import('../views/marketing/LeadScenarioPage.vue'),
                props: { mode: 'callback', title: '跟进记录', subTitle: '营销跟进全量记录与回访轨迹' },
                meta: { title: '跟进记录' }
              },
              {
                path: 'due',
                name: 'MarketingFollowupDue',
                component: () => import('../views/marketing/LeadScenarioPage.vue'),
                props: { mode: 'callback', title: '待回访提醒', subTitle: '待执行回访任务清单', scenario: 'follow_today' },
                meta: { title: '待回访提醒' }
              },
              {
                path: 'today',
                name: 'MarketingFollowupToday',
                component: () => import('../views/marketing/LeadScenarioPage.vue'),
                props: { mode: 'callback', title: '今日跟进计划', subTitle: '今日需完成的营销跟进', scenario: 'follow_today' },
                meta: { title: '今日跟进计划' }
              },
              {
                path: 'overdue',
                name: 'MarketingFollowupOverdue',
                component: () => import('../views/marketing/LeadScenarioPage.vue'),
                props: { mode: 'callback', title: '逾期未跟进', subTitle: '逾期未跟进客户风险池', scenario: 'follow_overdue' },
                meta: { title: '逾期未跟进' }
              }
            ]
          },
          {
            path: 'funnel',
            name: 'MarketingFunnel',
            meta: { title: '线索阶段管理（销售漏斗）' },
            redirect: '/marketing/funnel/consultation',
            children: [
              {
                path: 'consultation',
                name: 'MarketingFunnelConsultation',
                component: () => import('../views/marketing/LeadScenarioPage.vue'),
                props: { mode: 'consultation', title: '咨询阶段', subTitle: '咨询线索池：新增咨询、分配跟进与来源分析' },
                meta: { title: '咨询阶段' }
              },
              {
                path: 'evaluation',
                name: 'MarketingFunnelEvaluation',
                component: () => import('../views/marketing/LeadScenarioPage.vue'),
                props: { mode: 'intent', title: '评估阶段', subTitle: '高意向客户评估推进与评估任务闭环' },
                meta: { title: '评估阶段' }
              },
              {
                path: 'signing',
                name: 'MarketingFunnelSigning',
                component: () => import('../views/marketing/ContractSigning.vue'),
                props: { statusPreset: 'pending_sign', title: '签约阶段', subTitle: '待签署合同清单与签约推进' },
                meta: { title: '签约阶段' }
              },
              {
                path: 'admission',
                name: 'MarketingFunnelAdmission',
                component: () => import('../views/marketing/ContractSigning.vue'),
                props: { statusPreset: 'pending_bed_select', title: '入住阶段', subTitle: '待办理入住合同与选床办理联动' },
                meta: { title: '入住阶段' }
              },
              {
                path: 'lost',
                name: 'MarketingFunnelLost',
                component: () => import('../views/marketing/LeadScenarioPage.vue'),
                props: { mode: 'invalid', title: '流失阶段', subTitle: '流失客户归因分析与二次激活' },
                meta: { title: '流失阶段' }
              }
            ]
          },
          {
            path: 'reservation',
            name: 'MarketingReservation',
            meta: { title: '预定与床态联动' },
            redirect: '/marketing/reservation/records',
            children: [
              {
                path: 'records',
                name: 'MarketingReservationRecords',
                component: () => import('../views/marketing/SalesReservation.vue'),
                meta: { title: '床位预定记录' }
              },
              {
                path: 'lock',
                name: 'MarketingReservationLock',
                component: () => import('../views/marketing/LeadScenarioPage.vue'),
                props: { mode: 'reservation', title: '锁床管理', subTitle: '锁床客户与锁床状态跟踪', scenario: 'lock_bed' },
                meta: { title: '锁床管理' }
              },
              {
                path: 'expiring',
                name: 'MarketingReservationExpiring',
                component: () => import('../views/marketing/LeadScenarioPage.vue'),
                props: { mode: 'reservation', title: '预定到期提醒', subTitle: '锁床临期与预定超时预警', scenario: 'expiring_lock' },
                meta: { title: '预定到期提醒' }
              },
              {
                path: 'panorama',
                name: 'MarketingReservationPanorama',
                component: () => import('../views/marketing/RoomPanorama.vue'),
                meta: { title: '床态全景（销售视角）' }
              }
            ]
          },
          {
            path: 'contracts',
            name: 'MarketingContracts',
            meta: { title: '合同管理' },
            redirect: '/marketing/contracts/pending',
            children: [
              {
                path: 'pending',
                name: 'MarketingContractsPending',
                component: () => import('../views/marketing/ContractSigning.vue'),
                props: { statusPreset: 'pending_sign', title: '待签约合同', subTitle: '待签署合同统一处理：查看、入住办理、最终签署', disableDefaultFlowStagePreset: true },
                meta: { title: '待签约合同' }
              },
              {
                path: 'signed',
                name: 'MarketingContractsSigned',
                component: () => import('../views/marketing/ContractSigning.vue'),
                props: { statusPreset: 'signed', title: '已签合同', subTitle: '已签署合同集中查询、补录与变更协同' },
                meta: { title: '已签合同' }
              },
              {
                path: 'renewal',
                name: 'MarketingContractsRenewal',
                component: () => import('../views/marketing/ContractManagement.vue'),
                meta: { title: '续签提醒' }
              },
              {
                path: 'change',
                name: 'MarketingContractsChange',
                component: () => import('../views/marketing/ContractChange.vue'),
                meta: { title: '合同变更' }
              },
              {
                path: 'attachments',
                name: 'MarketingContractsAttachments',
                component: () => import('../views/marketing/ContractAttachments.vue'),
                meta: { title: '合同附件' }
              }
            ]
          },
          {
            path: 'callback',
            name: 'MarketingCallback',
            meta: { title: '回访管理' },
            redirect: '/marketing/callback/checkin',
            children: [
              {
                path: 'checkin',
                name: 'MarketingCallbackCheckin',
                component: () => import('../views/marketing/CallbackScenarioPage.vue'),
                props: { type: 'checkin', title: '入住后回访', subTitle: '入住后客户回访计划、执行与风险跟踪' },
                meta: { title: '入住后回访' }
              },
              {
                path: 'trial',
                name: 'MarketingCallbackTrial',
                component: () => import('../views/marketing/CallbackScenarioPage.vue'),
                props: { type: 'trial', title: '试住回访', subTitle: '试住客户回访安排与转化促进跟踪' },
                meta: { title: '试住回访' }
              },
              {
                path: 'discharge',
                name: 'MarketingCallbackDischarge',
                component: () => import('../views/marketing/CallbackScenarioPage.vue'),
                props: { type: 'discharge', title: '退住回访', subTitle: '退住客户回访与原因沉淀闭环' },
                meta: { title: '退住回访' }
              },
              {
                path: 'score',
                name: 'MarketingCallbackScore',
                component: () => import('../views/marketing/CallbackQuality.vue'),
                meta: { title: '回访质量评分' }
              }
            ]
          },
          {
            path: 'reports',
            name: 'MarketingReports',
            meta: { title: '销售报表中心' },
            redirect: '/marketing/reports/conversion',
            children: [
              {
                path: 'conversion',
                name: 'MarketingReportConversion',
                component: () => import('../views/marketing/report/Conversion.vue'),
                meta: { title: '转化率统计' }
              },
              {
                path: 'consultation',
                name: 'MarketingReportConsultation',
                component: () => import('../views/marketing/report/Consultation.vue'),
                meta: { title: '咨询统计' }
              },
              {
                path: 'unknown-channel',
                name: 'MarketingReportUnknownChannel',
                component: () => import('../views/marketing/report/UnknownChannel.vue'),
                meta: { title: '不明渠道统计' }
              },
              {
                path: 'followup',
                name: 'MarketingReportFollowup',
                component: () => import('../views/marketing/report/Followup.vue'),
                meta: { title: '跟进统计' }
              },
              {
                path: 'callback',
                name: 'MarketingReportCallback',
                component: () => import('../views/marketing/report/Callback.vue'),
                meta: { title: '回访统计' }
              },
              {
                path: 'channel',
                name: 'MarketingReportChannel',
                component: () => import('../views/marketing/report/Channel.vue'),
                meta: { title: '渠道评估' }
              },
              {
                path: 'sales-performance',
                name: 'MarketingReportSalesPerformance',
                component: () => import('../views/marketing/report/SalesPerformance.vue'),
                meta: { title: '销售人员业绩' }
              },
              {
                path: 'channel-rank',
                name: 'MarketingReportChannelRank',
                component: () => import('../views/marketing/report/ChannelRank.vue'),
                meta: { title: '渠道贡献排名' }
              }
            ]
          },
          {
            path: 'contract-signing',
            name: 'MarketingContractSigning',
            component: () => import('../views/marketing/ContractSigning.vue'),
            meta: { title: '合同签约（兼容）', hidden: true }
          },
          {
            path: 'contract-management',
            name: 'MarketingContractManagement',
            component: () => import('../views/marketing/ContractManagement.vue'),
            meta: { title: '合同到期管理（兼容）', hidden: true }
          },
          {
            path: 'room-panorama',
            name: 'MarketingRoomPanorama',
            component: () => import('../views/marketing/RoomPanorama.vue'),
            meta: { title: '房态全景（兼容）', hidden: true }
          },
          {
            path: 'plan',
            name: 'MarketingPlan',
            component: () => import('../views/marketing/MarketingPlan.vue'),
            meta: { title: '营销方案与审批' }
          },
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
              { path: 'callback', redirect: '/marketing/followup/due', meta: { hidden: true } }
            ]
          },
        ]
      },
      {
        path: 'bed',
        name: 'Bed',
        meta: { title: '床位管理', icon: 'HomeOutlined', hidden: true },
        redirect: '/logistics/assets/room-state-map',
        children: [
          {
            path: 'map',
            name: 'BedMap',
            redirect: '/logistics/assets/room-state-map',
            meta: { title: '房态图', hidden: true }
          },
          {
            path: 'manage',
            name: 'BedManage',
            redirect: '/logistics/assets/bed-management',
            meta: { title: '床位管理', hidden: true }
          }
        ]
      },
      {
        path: 'logistics',
        name: 'Logistics',
        meta: { title: '后勤保障', icon: 'ToolOutlined', roles: ['LOGISTICS_EMPLOYEE', 'LOGISTICS_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
        redirect: '/logistics/workbench',
        children: [
          {
            path: 'workbench',
            name: 'LogisticsWorkbench',
            component: () => import('../views/logistics/Workbench.vue'),
            meta: { title: '后勤工作台' }
          },
          {
            path: 'task-center',
            name: 'LogisticsTaskCenter',
            component: () => import('../views/logistics/TaskCenter.vue'),
            meta: { title: '后勤任务中心' }
          },
          {
            path: 'assets',
            name: 'LogisticsAssets',
            meta: { title: '资产与床态管理' },
            redirect: '/logistics/assets/room-state-map',
            children: [
              {
                path: 'building-management',
                name: 'LogisticsBuildingManagement',
                redirect: '/logistics/assets/bed-management',
                meta: { title: '楼栋管理', hidden: true }
              },
              {
                path: 'floor-management',
                name: 'LogisticsFloorManagement',
                redirect: '/logistics/assets/bed-management',
                meta: { title: '楼层管理', hidden: true }
              },
              {
                path: 'room-management',
                name: 'LogisticsRoomManagement',
                redirect: '/logistics/assets/bed-management',
                meta: { title: '房间管理', hidden: true }
              },
              {
                path: 'room-state-map',
                name: 'LogisticsRoomStateMap',
                component: () => import('../views/bed/Map.vue'),
                meta: { title: '房态图' }
              },
              {
                path: 'bed-management',
                name: 'LogisticsBedManagement',
                component: () => import('../views/bed/Manage.vue'),
                props: { initialTab: 'beds' },
                meta: { title: '床位管理' }
              },
              {
                path: 'bed-type-config',
                name: 'LogisticsBedTypeConfig',
                component: () => import('../views/base-config/Index.vue'),
                props: { title: '床位类型', groupCode: 'ADMISSION_BED_TYPE' },
                meta: { title: '床位类型', roles: ['ADMIN'] }
              },
              {
                path: 'room-type-config',
                name: 'LogisticsRoomTypeConfig',
                component: () => import('../views/base-config/Index.vue'),
                props: { title: '房间类型', groupCode: 'ADMISSION_ROOM_TYPE' },
                meta: { title: '房间类型', roles: ['ADMIN'] }
              },
              {
                path: 'area-config',
                name: 'LogisticsAreaConfig',
                component: () => import('../views/base-config/Index.vue'),
                props: { title: '区域设置', groupCode: 'ADMISSION_AREA' },
                meta: { title: '区域设置', roles: ['ADMIN'] }
              },
              {
                path: 'bed-panorama',
                name: 'LogisticsBedPanorama',
                redirect: '/logistics/assets/room-state-map',
                meta: { title: '床态全景', hidden: true }
              },
              {
                path: 'bed-status-record',
                name: 'LogisticsBedStatusRecord',
                component: () => import('../views/elder/ChangeLog.vue'),
                meta: { title: '床位状态记录' }
              },
              {
                path: 'cleaning-record',
                name: 'LogisticsCleaningRecord',
                component: () => import('../views/life/RoomCleaning.vue'),
                meta: { title: '清洁消杀记录' }
              },
              {
                path: 'maintenance-record',
                name: 'LogisticsMaintenanceRecord',
                component: () => import('../views/life/Maintenance.vue'),
                meta: { title: '维修记录' }
              }
            ]
          },
          {
            path: 'storage',
            name: 'LogisticsStorage',
            meta: { title: '物资仓储管理' },
            children: [
              { path: 'warehouse', name: 'LogisticsWarehouse', component: () => import('../views/material/Warehouse.vue'), meta: { title: '仓库设置' } },
              { path: 'supplier', name: 'LogisticsSupplier', component: () => import('../views/material/Supplier.vue'), meta: { title: '供应商管理' } },
              { path: 'purchase', name: 'LogisticsPurchase', component: () => import('../views/material/Purchase.vue'), meta: { title: '采购单' } },
              { path: 'inbound', name: 'LogisticsInbound', component: () => import('../views/inventory/Inbound.vue'), meta: { title: '入库管理' } },
              { path: 'outbound', name: 'LogisticsOutbound', component: () => import('../views/inventory/Outbound.vue'), meta: { title: '出库管理' } },
              { path: 'transfer', name: 'LogisticsTransfer', component: () => import('../views/material/Transfer.vue'), meta: { title: '物资调拨' } },
              { path: 'stock-query', name: 'LogisticsStockQuery', component: () => import('../views/inventory/Overview.vue'), meta: { title: '库存查询' } },
              { path: 'alerts', name: 'LogisticsAlerts', component: () => import('../views/inventory/Alerts.vue'), meta: { title: '库存预警' } },
              { path: 'stock-check', name: 'LogisticsStockCheck', component: () => import('../views/inventory/Adjustments.vue'), meta: { title: '库存盘点' } },
              { path: 'stock-amount', name: 'LogisticsStockAmount', component: () => import('../views/material/StockAmount.vue'), meta: { title: '库存金额' } },
              {
                path: 'item-master',
                name: 'LogisticsItemMaster',
                component: () => import('../views/store/Product.vue'),
                props: {
                  mode: 'storage',
                  title: '物品信息（主数据）',
                  subTitle: '商品主数据与商城共享；仓储侧只读查看并联动库存、出入库流程'
                },
                meta: { title: '物品信息（主数据）' }
              }
            ]
          },
          {
            path: 'dining',
            name: 'LogisticsDining',
            meta: { title: '餐饮管理' },
            children: [
              { path: 'dish', name: 'LogisticsDiningDish', component: () => import('../views/dining/Dish.vue'), meta: { title: '菜品管理' } },
              { path: 'recipe', name: 'LogisticsDiningRecipe', component: () => import('../views/dining/Recipe.vue'), meta: { title: '食谱管理' } },
              { path: 'order', name: 'LogisticsDiningOrder', component: () => import('../views/dining/Order.vue'), meta: { title: '点餐管理（个性化）' } },
              { path: 'stats', name: 'LogisticsDiningStats', component: () => import('../views/dining/Stats.vue'), meta: { title: '订餐统计' } },
              { path: 'procurement-plan', name: 'LogisticsDiningProcurementPlan', component: () => import('../views/dining/ProcurementPlan.vue'), meta: { title: '采购计划单' } },
              { path: 'prep-zone', name: 'LogisticsDiningPrepZone', component: () => import('../views/dining/PrepZone.vue'), meta: { title: '分区备餐' } },
              { path: 'delivery-area', name: 'LogisticsDiningDeliveryArea', component: () => import('../views/dining/DeliveryArea.vue'), meta: { title: '送餐区域' } },
              {
                path: 'delivery-plan',
                name: 'LogisticsDiningDeliveryPlan',
                component: () => import('../views/logistics/DeliveryPlan.vue'),
                meta: { title: '送餐计划' }
              },
              { path: 'cost-stats', name: 'LogisticsDiningCostStats', redirect: '/logistics/dining/stats?metric=cost', meta: { title: '餐饮成本统计' } }
            ]
          },
          {
            path: 'maintenance',
            name: 'LogisticsMaintenance',
            meta: { title: '维修与报障' },
            children: [
              { path: 'report', name: 'LogisticsMaintenanceReport', redirect: '/logistics/assets/maintenance-record', meta: { title: '报修登记' } },
              { path: 'dispatch', name: 'LogisticsMaintenanceDispatch', redirect: '/logistics/assets/maintenance-record?status=OPEN', meta: { title: '维修派单' } },
              { path: 'progress', name: 'LogisticsMaintenanceProgress', redirect: '/logistics/assets/maintenance-record?status=PROCESSING', meta: { title: '维修进度' } },
              {
                path: 'cost',
                name: 'LogisticsMaintenanceCost',
                component: () => import('../views/logistics/MaintenanceCost.vue'),
                meta: { title: '维修成本记录' }
              },
              {
                path: 'assets',
                name: 'LogisticsMaintenanceAssets',
                component: () => import('../views/logistics/EquipmentArchive.vue'),
                meta: { title: '设备档案' }
              }
            ]
          },
          {
            path: 'reports',
            name: 'LogisticsReports',
            meta: { title: '后勤报表中心' },
            children: [
              { path: 'bed-usage', name: 'LogisticsReportBedUsage', redirect: '/stats/org/bed-usage', meta: { title: '床位使用率' } },
              { path: 'stock-amount', name: 'LogisticsReportStockAmount', redirect: '/logistics/storage/stock-amount', meta: { title: '库存金额统计' } },
              { path: 'purchase', name: 'LogisticsReportPurchase', redirect: '/logistics/storage/purchase', meta: { title: '采购统计' } },
              { path: 'consume', name: 'LogisticsReportConsume', redirect: '/logistics/storage/outbound', meta: { title: '物资消耗统计' } },
              { path: 'dining-cost', name: 'LogisticsReportDiningCost', redirect: '/logistics/dining/stats?metric=cost', meta: { title: '餐饮成本统计' } },
              {
                path: 'maintenance-todo-log',
                name: 'LogisticsReportMaintenanceTodoLog',
                component: () => import('../views/logistics/MaintenanceTodoJobLog.vue'),
                meta: { title: '维保待办日志' }
              }
            ]
          },
          {
            path: 'commerce',
            name: 'LogisticsCommerce',
            meta: { title: '商城与商品' },
            children: [
              { path: 'category', name: 'LogisticsCommerceCategory', component: () => import('../views/store/Category.vue'), meta: { title: '商品大类' } },
              { path: 'tag', name: 'LogisticsCommerceTag', component: () => import('../views/store/Tag.vue'), meta: { title: '商品标签' } },
              { path: 'risk', name: 'LogisticsCommerceRisk', component: () => import('../views/store/Risk.vue'), meta: { title: '禁忌规则' } },
              { path: 'product', name: 'LogisticsCommerceProduct', component: () => import('../views/store/Product.vue'), meta: { title: '商品管理' } },
              { path: 'order', name: 'LogisticsCommerceOrder', component: () => import('../views/store/Order.vue'), meta: { title: '订单管理' } },
              { path: 'points', name: 'LogisticsCommercePoints', component: () => import('../views/store/Points.vue'), meta: { title: '积分账户' } }
            ]
          }
        ]
      },
      {
        path: 'care',
        name: 'Care',
        meta: { title: '照护管理', icon: 'ScheduleOutlined', hidden: true, roles: ['NURSING_EMPLOYEE', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
        redirect: '/care/staff/caregiver-info',
        children: [
          {
            path: 'staff',
            name: 'CareStaff',
            meta: { title: '人员管理' },
            redirect: '/care/staff/caregiver-info',
            children: [
              {
                path: 'caregiver-info',
                name: 'CaregiverInfo',
                component: () => import('../views/hr/Staff.vue'),
                props: { title: '护工信息', subTitle: '照护团队成员档案与用工信息' },
                meta: { title: '护工信息' }
              },
              {
                path: 'caregiver-groups',
                name: 'CaregiverGroups',
                component: () => import('../views/care/CaregiverGroups.vue'),
                meta: { title: '护工小组' }
              }
            ]
          },
          {
            path: 'service',
            name: 'CareService',
            meta: { title: '服务管理' },
            redirect: '/care/service/service-items',
            children: [
              {
                path: 'service-items',
                name: 'CareServiceItems',
                component: () => import('../views/care/ServiceItems.vue'),
                meta: { title: '服务项目' }
              },
              {
                path: 'care-levels',
                name: 'CareLevels',
                component: () => import('../views/care/CareLevels.vue'),
                meta: { title: '护理等级' }
              },
              {
                path: 'service-plans',
                name: 'ServicePlans',
                component: () => import('../views/care/ServicePlans.vue'),
                meta: { title: '服务计划' }
              },
              {
                path: 'nursing-reports',
                name: 'CareNursingReports',
                component: () => import('../views/care/NursingReports.vue'),
                meta: { title: '护理报表' }
              },
              {
                path: 'nursing-records',
                name: 'CareNursingRecords',
                component: () => import('../views/care/NursingRecords.vue'),
                meta: { title: '护理记录' }
              },
              {
                path: 'service-bookings',
                name: 'ServiceBookings',
                component: () => import('../views/care/ServiceBookings.vue'),
                meta: { title: '服务预定' }
              }
            ]
          },
          {
            path: 'scheduling',
            name: 'CareScheduling',
            meta: { title: '排班管理' },
            redirect: '/care/scheduling/shift-templates',
            children: [
              {
                path: 'shift-templates',
                name: 'ShiftTemplates',
                component: () => import('../views/care/ShiftTemplates.vue'),
                meta: { title: '排班方案' }
              },
              {
                path: 'shift-calendar',
                name: 'CareShiftCalendar',
                component: () => import('../views/care/ShiftCalendar.vue'),
                meta: { title: '排班' }
              },
              {
                path: 'handovers',
                name: 'ShiftHandovers',
                redirect: (to) => ({ path: '/medical-care/handovers', query: to.query }),
                meta: { title: '交接班' }
              }
            ]
          },
          {
            path: 'dashboard',
            name: 'CareDashboard',
            component: () => import('../views/care/Dashboard.vue'),
            meta: { title: '护理看板', hidden: true }
          },
          {
            path: 'today',
            name: 'CareToday',
            component: () => import('../views/care/Today.vue'),
            meta: { title: '今日任务' }
          },
          {
            path: 'template',
            name: 'CareTemplate',
            component: () => import('../views/care/Template.vue'),
            meta: { title: '护理模板' }
          },
          {
            path: 'care-packages',
            name: 'CarePackages',
            component: () => import('../views/care/CarePackages.vue'),
            meta: { title: '护理套餐', hidden: true }
          },
          {
            path: 'package-items',
            name: 'CarePackageItems',
            component: () => import('../views/care/PackageItems.vue'),
            meta: { title: '套餐明细', hidden: true }
          },
          {
            path: 'elder-packages',
            name: 'CareElderPackages',
            component: () => import('../views/care/ElderPackages.vue'),
            meta: { title: '老人套餐', hidden: true }
          },
          {
            path: 'task-generate',
            name: 'CareTaskGenerate',
            component: () => import('../views/care/TaskGenerate.vue'),
            meta: { title: '套餐任务生成', hidden: true }
          },
          {
            path: 'exception',
            name: 'CareException',
            component: () => import('../views/care/Exception.vue'),
            meta: { title: '异常任务', hidden: true }
          },
          {
            path: 'audit',
            name: 'CareAudit',
            component: () => import('../views/care/Audit.vue'),
            meta: { title: '防作弊审计', hidden: true }
          },
          {
            path: 'workbench/task-board',
            name: 'CareWorkbenchTaskBoard',
            redirect: (to) => ({ path: '/medical-care/care-task-board', query: to.query }),
            meta: { title: '照护任务看板', hidden: true }
          },
          {
            path: 'workbench/plan',
            name: 'CareWorkbenchPlan',
            component: () => import('../views/care/workbench/CarePlan.vue'),
            meta: { title: '照护计划', hidden: true }
          },
          {
            path: 'workbench/qr',
            name: 'CareWorkbenchQr',
            component: () => import('../views/care/workbench/ScanExecute.vue'),
            meta: { title: '护工扫码执行', hidden: true }
          },
          {
            path: 'workbench/task',
            name: 'CareWorkbenchTask',
            component: () => import('../views/care/workbench/ServiceBooking.vue'),
            meta: { title: '服务预约', hidden: true }
          }
        ]
      },
      {
        path: 'material',
        name: 'Material',
        meta: { title: '后勤物资（兼容）', icon: 'DatabaseOutlined', hidden: true },
        children: [
          {
            path: 'info',
            name: 'MaterialInfo',
            redirect: '/logistics/storage/item-master',
            meta: { title: '物资主数据（兼容）', hidden: true }
          },
          {
            path: 'warehouse',
            name: 'MaterialWarehouse',
            redirect: '/logistics/storage/warehouse',
            meta: { title: '仓库设置（兼容）', hidden: true }
          },
          {
            path: 'supplier',
            name: 'MaterialSupplier',
            redirect: '/logistics/storage/supplier',
            meta: { title: '供应商管理（兼容）', hidden: true }
          },
          {
            path: 'inbound',
            name: 'MaterialInbound',
            redirect: '/logistics/storage/inbound',
            meta: { title: '入库管理（兼容）', hidden: true }
          },
          {
            path: 'outbound',
            name: 'MaterialOutbound',
            redirect: '/logistics/storage/outbound',
            meta: { title: '出库管理（兼容）', hidden: true }
          },
          {
            path: 'stock-query',
            name: 'MaterialStockQuery',
            redirect: '/logistics/storage/stock-query',
            meta: { title: '库存查询（兼容）', hidden: true }
          },
          {
            path: 'alerts',
            name: 'MaterialAlerts',
            redirect: '/logistics/storage/alerts',
            meta: { title: '库存预警（兼容）', hidden: true }
          },
          {
            path: 'transfer',
            name: 'MaterialTransfer',
            redirect: '/logistics/storage/transfer',
            meta: { title: '物资调拨（兼容）', hidden: true }
          },
          {
            path: 'stock-amount',
            name: 'MaterialStockAmount',
            redirect: '/logistics/storage/stock-amount',
            meta: { title: '库存金额（兼容）', hidden: true }
          },
          {
            path: 'stock-check',
            name: 'MaterialStockCheck',
            redirect: '/logistics/storage/stock-check',
            meta: { title: '库存盘点（兼容）', hidden: true }
          },
          {
            path: 'purchase',
            name: 'MaterialPurchase',
            redirect: '/logistics/storage/purchase',
            meta: { title: '采购单（兼容）', hidden: true }
          }
        ]
      },
      {
        path: 'store',
        name: 'Store',
        meta: { title: '后勤商城（兼容）', icon: 'ShopOutlined', hidden: true },
        children: [
          {
            path: 'category',
            name: 'StoreCategory',
            redirect: '/logistics/commerce/category',
            meta: { title: '商品大类（兼容）', hidden: true }
          },
          {
            path: 'product',
            name: 'StoreProduct',
            redirect: '/logistics/commerce/product',
            meta: { title: '商品管理（兼容）', hidden: true }
          },
          {
            path: 'tag',
            name: 'StoreTag',
            redirect: '/logistics/commerce/tag',
            meta: { title: '商品标签（兼容）', hidden: true }
          },
          {
            path: 'order',
            name: 'StoreOrder',
            redirect: '/logistics/commerce/order',
            meta: { title: '订单管理（兼容）', hidden: true }
          },
          {
            path: 'points',
            name: 'StorePoints',
            redirect: '/logistics/commerce/points',
            meta: { title: '积分账户（兼容）', hidden: true }
          }
        ]
      },
      {
        path: 'inventory',
        name: 'Inventory',
        meta: { title: '库存管理', icon: 'AlertOutlined', hidden: true },
        children: [
          {
            path: '',
            redirect: '/logistics/storage/stock-query',
            meta: { hidden: true }
          },
          {
            path: 'overview',
            redirect: '/logistics/storage/stock-query',
            meta: { title: '库存总览' }
          },
          {
            path: 'inbound',
            redirect: '/logistics/storage/inbound',
            meta: { title: '入库管理' }
          },
          {
            path: 'outbound',
            redirect: '/logistics/storage/outbound',
            meta: { title: '出库记录' }
          },
          {
            path: 'alerts',
            redirect: '/logistics/storage/alerts',
            meta: { title: '预警中心' }
          },
          {
            path: 'adjustments',
            redirect: '/logistics/storage/stock-check',
            meta: { title: '盘点记录' }
          }
        ]
      },
      {
        path: 'finance',
        name: 'Finance',
        meta: { title: '财务运营中心', icon: 'AccountBookOutlined', roles: ['FINANCE_EMPLOYEE', 'FINANCE_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
        redirect: '/finance/workbench',
        children: [
          {
            path: 'workbench',
            name: 'FinanceWorkbench',
            component: () => import('../views/finance/FinanceWorkbench.vue'),
            meta: { title: '今日工作台' }
          },
          {
            path: 'search',
            name: 'FinanceSearch',
            component: () => import('../views/finance/FinanceSearch.vue'),
            meta: { title: '财务搜索', hidden: true }
          },
          {
            path: 'accounts',
            name: 'FinanceAccounts',
            redirect: '/finance/accounts/list',
            meta: { title: '账户与预存' },
            children: [
              {
                path: 'list',
                name: 'FinanceAccountsList',
                component: () => import('../views/finance/Account.vue'),
                meta: { title: '长者账户列表' }
              },
              {
                path: 'ledger',
                name: 'FinanceAccountsLedger',
                component: () => import('../views/finance/AccountLog.vue'),
                meta: { title: '账户流水' }
              },
              {
                path: 'warning-rules',
                name: 'FinanceAccountsWarningRules',
                component: () => import('../views/finance/BalanceWarningRules.vue'),
                meta: { title: '余额预警规则' }
              }
            ]
          },
          {
            path: 'bills',
            name: 'FinanceBills',
            redirect: '/finance/bills/in-resident',
            meta: { title: '账单与欠费' },
            children: [
              {
                path: 'in-admission',
                name: 'FinanceBillsInAdmission',
                component: () => import('../views/finance/AdmissionBillPayment.vue'),
                meta: { title: '入住账单' }
              },
              {
                path: 'in-resident',
                name: 'FinanceBillsInResident',
                component: () => import('../views/finance/ResidentBillPayment.vue'),
                meta: { title: '在住周期账单' }
              },
              {
                path: 'discharge',
                name: 'FinanceBillsDischarge',
                component: () => import('../views/finance/DischargeSettlement.vue'),
                meta: { title: '退住结算账单' }
              },
              {
                path: 'rules',
                name: 'FinanceBillsRules',
                component: () => import('../views/finance/BillingRulesConfig.vue'),
                meta: { title: '计费规则与账单模板' }
              },
              {
                path: 'auto-deduct',
                name: 'FinanceBillsAutoDeduct',
                component: () => import('../views/finance/AutoDebitManagement.vue'),
                meta: { title: '自动扣费与催缴' }
              },
              {
                path: 'auto-deduct-errors',
                name: 'FinanceBillsAutoDeductErrors',
                component: () => import('../views/finance/AutoDebitManagement.vue'),
                meta: { title: '自动扣费异常' }
              },
              {
                path: 'detail-query',
                name: 'FinanceBillsDetailQuery',
                component: () => import('../views/finance/BillCenter.vue'),
                meta: { title: '账单查询' },
                props: {
                  title: '账单详情查询',
                  subTitle: '按老人、月份检索账单后进入详情页',
                  defaultCurrentMonth: true
                }
              },
              {
                path: 'follow-up',
                name: 'FinanceBillsFollowUp',
                component: () => import('../views/finance/FinanceCollectionFollowUp.vue'),
                meta: { title: '欠费催缴跟进' }
              }
            ]
          },
          {
            path: 'payments',
            name: 'FinancePayments',
            redirect: '/finance/payments/cashier-desk',
            meta: { title: '收款与交班' },
            children: [
              {
                path: 'cashier-desk',
                name: 'FinancePaymentsCashierDesk',
                component: () => import('../views/finance/CashierDesk.vue'),
                meta: { title: '收银台' }
              },
              {
                path: 'register',
                name: 'FinancePaymentsRegister',
                component: () => import('../views/finance/ResidentBillPayment.vue'),
                meta: { title: '收费登记' }
              },
                            {
                                path: 'refund-reversal',
                                name: 'FinancePaymentsRefundReversal',
                                component: () => import('../views/finance/PaymentRefundReversal.vue'),
                                meta: { title: '退款与冲正' }
                            },
              {
                path: 'shift-close',
                name: 'FinancePaymentsShiftClose',
                component: () => import('../views/finance/ShiftClose.vue'),
                meta: { title: '日结与交班' }
              },
                            {
                                path: 'records',
                                name: 'FinancePaymentsRecords',
                                component: () => import('../views/finance/PaymentRecords.vue'),
                                meta: { title: '收款流水' }
                            }
            ]
          },
          {
            path: 'fees',
            name: 'FinanceFees',
            redirect: '/finance/fees/payment-and-invoice',
            meta: { title: '票据管理' },
            children: [
              {
                path: 'payment-and-invoice',
                name: 'FinancePaymentsInvoice',
                component: () => import('../views/finance/InvoiceReceiptManagement.vue'),
                meta: { title: '发票与收据' }
              }
            ]
          },
          {
            path: 'flows',
            name: 'FinanceFlows',
            redirect: '/finance/flows/consumption',
            meta: { title: '流水与调整' },
            children: [
              {
                path: 'consumption',
                name: 'FinanceFlowsConsumption',
                component: () => import('../views/finance/ConsumptionRegister.vue'),
                meta: { title: '消费登记' }
              },
              {
                path: 'medical',
                name: 'FinanceFlowsMedical',
                component: () => import('../views/finance/FlowCenter.vue'),
                meta: { title: '医护费用流水' },
                props: {
                  moduleKey: 'MEDICAL_FLOW',
                  moduleName: '医护费用流水',
                  description: '按医嘱、用药、治疗与检查动作汇总收费流水',
                  category: 'MEDICINE',
                  links: [
                    { label: '查看医护费用异常', to: '/finance/flows/medical-errors' },
                    { label: '查看费用调整单', to: '/finance/flows/adjustments' }
                  ]
                }
              },
              {
                path: 'medical-errors',
                name: 'FinanceFlowsMedicalErrors',
                component: () => import('../views/finance/FlowCenter.vue'),
                meta: { title: '医护费用异常' },
                props: {
                  moduleKey: 'MEDICAL_ERRORS',
                  moduleName: '医护费用异常',
                  description: '重复计费、缺少医嘱关联、异常修正与重算',
                  category: 'MEDICINE',
                  links: [
                    { label: '查看医护费用流水', to: '/finance/flows/medical' },
                    { label: '查看费用调整单', to: '/finance/flows/adjustments' }
                  ]
                }
              },
              {
                path: 'dining',
                name: 'FinanceFlowsDining',
                component: () => import('../views/finance/FlowCenter.vue'),
                meta: { title: '餐饮扣费流水' },
                props: {
                  moduleKey: 'DINING_FLOW',
                  moduleName: '餐饮扣费流水',
                  description: '积分点餐扣费、餐饮补扣与异常追踪',
                  category: 'DINING',
                  links: [
                    { label: '查看消费流水', to: '/finance/flows/consumption' },
                    { label: '查看在住账单', to: '/finance/bills/in-resident' }
                  ]
                }
              },
              {
                path: 'logistics',
                name: 'FinanceFlowsLogistics',
                component: () => import('../views/finance/FlowCenter.vue'),
                meta: { title: '物资/后勤收费流水' },
                props: {
                  moduleKey: 'LOGISTICS_FLOW',
                  moduleName: '物资/后勤收费流水',
                  description: '物资与后勤额外收费项目的自动入账视图',
                  category: 'OTHER',
                  links: [
                    { label: '查看消费流水', to: '/finance/flows/consumption' },
                    { label: '查看经营报表', to: '/finance/reports/overall' }
                  ]
                }
              },
              {
                path: 'adjustments',
                name: 'FinanceFlowsAdjustments',
                component: () => import('../views/finance/FlowCenter.vue'),
                meta: { title: '费用调整单' },
                props: {
                  moduleKey: 'ADJUSTMENTS',
                  moduleName: '费用调整单',
                  description: '减免、补录、改价调整申请与审批跟踪',
                  links: [
                    { label: '查看审批中心', to: '/oa/approval?module=finance&status=pending' },
                    { label: '查看退住审核', to: '/finance/discharge/review?status=pending' }
                  ]
                }
              }
            ]
          },
          {
            path: 'allocation',
            name: 'FinanceAllocation',
            redirect: '/finance/allocation/public-cost',
            meta: { title: '公共费用分摊' },
            children: [
              {
                path: 'subjects',
                name: 'FinanceAllocationSubjects',
                component: () => import('../views/finance/FeeSubjectDictionary.vue'),
                meta: { title: '费用科目管理' }
              },
              {
                path: 'rules',
                name: 'FinanceAllocationRules',
                component: () => import('../views/finance/AllocationRules.vue'),
                meta: { title: '分摊规则' }
              },
              {
                path: 'public-cost',
                name: 'FinanceAllocationPublicCost',
                component: () => import('../views/finance/MonthlyAllocation.vue'),
                meta: { title: '公共费用计算' }
              },
              {
                path: 'tasks',
                name: 'FinanceAllocationTasks',
                component: () => import('../views/finance/MonthlyAllocation.vue'),
                meta: { title: '分摊入账任务' }
              }
            ]
          },
          {
            path: 'discharge',
            name: 'FinanceDischarge',
            redirect: '/finance/discharge/review',
            meta: { title: '退住结算与退款' },
            children: [
              {
                path: 'review',
                name: 'FinanceDischargeReview',
                component: () => import('../views/finance/DischargeFeeAudit.vue'),
                meta: { title: '退住审核' }
              },
              {
                path: 'settlement',
                name: 'FinanceDischargeSettlementCenter',
                component: () => import('../views/finance/DischargeSettlement.vue'),
                meta: { title: '退住结算' }
              },
              {
                path: 'print-sign',
                name: 'FinanceDischargePrintSign',
                component: () => import('../views/finance/DischargeSettlement.vue'),
                meta: { title: '结算单打印' }
              },
              {
                path: 'status-sync',
                name: 'FinanceDischargeStatusSync',
                component: () => import('../views/finance/DischargeStatusSync.vue'),
                meta: { title: '结算状态回写' }
              }
            ]
          },
          {
            path: 'reconcile',
            name: 'FinanceReconcile',
            redirect: '/finance/reconcile/center',
            meta: { title: '对账与月结' },
            children: [
              {
                path: 'center',
                name: 'FinanceReconcileCenter',
                component: () => import('../views/finance/Reconcile.vue'),
                meta: { title: '对账中心' }
              },
              {
                path: 'auto-deduct',
                name: 'FinanceReconcileAutoDeduct',
                redirect: '/finance/bills/auto-deduct',
                meta: { title: '自动扣费对账' }
              },
              {
                path: 'invoice',
                name: 'FinanceReconcileInvoice',
                component: () => import('../views/finance/ReconcileInvoice.vue'),
                meta: { title: '发票对账' }
              },
              {
                path: 'exception',
                name: 'FinanceReconcileException',
                component: () => import('../views/finance/ReconcileException.vue'),
                meta: { title: '对账异常处理' }
              },
              {
                path: 'ledger-health',
                name: 'FinanceReconcileLedgerHealth',
                component: () => import('../views/finance/LedgerHealth.vue'),
                meta: { title: '财务一致性巡检' }
              },
              {
                path: 'issue-center',
                name: 'FinanceReconcileIssueCenter',
                component: () => import('../views/finance/FinanceIssueCenter.vue'),
                meta: { title: '异常修复中心' }
              },
              {
                path: 'month-close',
                name: 'FinanceReconcileMonthClose',
                component: () => import('../views/finance/FinanceMonthClose.vue'),
                meta: { title: '月结进度' }
              }
            ]
          },
          {
            path: 'reports',
            name: 'FinanceReports',
            redirect: '/finance/reports/overall',
            meta: { title: '经营报表' },
            children: [
              {
                path: 'overall',
                name: 'FinanceReportsOverall',
                component: () => import('../views/finance/Report.vue'),
                meta: { title: '总收支' }
              },
              {
                path: 'revenue-structure',
                name: 'FinanceReportsRevenueStructure',
                component: () => import('../views/finance/Report.vue'),
                meta: { title: '费用结构/营收占比' }
              },
              {
                path: 'floor-room-profit',
                name: 'FinanceReportsFloorRoom',
                component: () => import('../views/finance/Report.vue'),
                meta: { title: '楼层/房间收支情况' }
              },
              {
                path: 'room-ops-detail',
                name: 'FinanceReportsRoomOpsDetail',
                component: () => import('../views/finance/RoomOpsDetail.vue'),
                meta: { title: '房间经营详情' }
              },
              {
                path: 'occupancy-consumption',
                name: 'FinanceReportsOccupancyConsumption',
                component: () => import('../views/finance/Report.vue'),
                meta: { title: '入住/床位/消费统计' }
              },
              {
                path: 'monthly-ops',
                name: 'FinanceReportsMonthlyOps',
                component: () => import('../views/finance/Report.vue'),
                meta: { title: '机构月运营' }
              }
            ]
          },
          {
            path: 'config',
            name: 'FinanceConfig',
            redirect: '/finance/config/master-data',
            meta: { title: '规则与配置' },
            children: [
              {
                path: 'master-data',
                name: 'FinanceConfigMasterData',
                component: () => import('../views/finance/FinanceMasterData.vue'),
                meta: { title: '主数据配置中心' }
              },
              {
                path: 'fee-subjects',
                name: 'FinanceConfigFeeSubjects',
                component: () => import('../views/finance/FeeSubjectDictionary.vue'),
                meta: { title: '费用科目字典' }
              },
              {
                path: 'billing-pricing',
                name: 'FinanceConfigBillingPricing',
                redirect: '/finance/bills/rules',
                meta: { title: '计费规则' }
              },
              {
                path: 'payment-channels',
                name: 'FinanceConfigPaymentChannels',
                component: () => import('../views/finance/PaymentChannelConfig.vue'),
                meta: { title: '支付渠道' }
              },
              {
                path: 'approval-flow',
                name: 'FinanceConfigApprovalFlow',
                component: () => import('../views/finance/FinanceApprovalFlowConfig.vue'),
                meta: { title: '审批与权限' }
              },
              {
                path: 'change-log',
                name: 'FinanceConfigChangeLog',
                component: () => import('../views/finance/FinanceConfigChangeLog.vue'),
                meta: { title: '配置变更记录' }
              }
            ]
          },
          {
            path: 'deposit-management',
            name: 'FinanceDepositManagement',
            redirect: '/finance/accounts/list',
            meta: { title: '押金管理（兼容）', hidden: true }
          },
          {
            path: 'admission-fee-audit',
            name: 'FinanceAdmissionFeeAudit',
            component: () => import('../views/finance/AdmissionFeeAudit.vue'),
            meta: { title: '入住费用审核' }
          },
          {
            path: 'admission-bill-payment',
            name: 'FinanceAdmissionBillPayment',
            redirect: '/finance/bills/in-admission',
            meta: { title: '入住账单缴费（兼容）', hidden: true }
          },
          {
            path: 'resident-bill-payment',
            name: 'FinanceResidentBillPayment',
            redirect: '/finance/bills/in-resident',
            meta: { title: '在住账单缴费（兼容）', hidden: true }
          },
          {
            path: 'monthly-allocation',
            name: 'FinanceMonthlyAllocation',
            redirect: '/finance/allocation/public-cost',
            meta: { title: '月分摊费（兼容）', hidden: true }
          },
          {
            path: 'prepaid-recharge',
            name: 'FinancePrepaidRecharge',
            component: () => import('../views/finance/PrepaidRecharge.vue'),
            meta: { title: '预存充值' }
          },
          {
            path: 'discharge-fee-audit',
            name: 'FinanceDischargeFeeAudit',
            redirect: '/finance/discharge/review',
            meta: { title: '退住费用审核（兼容）', hidden: true }
          },
          {
            path: 'finance-settlement',
            name: 'FinanceSettlement',
            redirect: '/finance/reconcile/center',
            meta: { title: '财务结算（兼容）', hidden: true }
          },
          {
            path: 'discharge-settlement',
            name: 'FinanceDischargeSettlement',
            redirect: '/finance/discharge/settlement',
            meta: { title: '退住结算（兼容）', hidden: true }
          },
          {
            path: 'medical-care-ledger',
            name: 'FinanceMedicalCareLedger',
            redirect: '/finance/flows/medical',
            meta: { title: '医护费用流水（兼容）', hidden: true }
          },
          {
            path: 'consumption-register',
            name: 'FinanceConsumptionRegister',
            redirect: '/finance/flows/consumption',
            meta: { title: '消费登记（兼容）', hidden: true }
          },
          {
            path: 'bill/:billId',
            name: 'FinanceBillDetail',
            component: () => import('../views/finance/BillDetail.vue'),
            meta: { title: '账单详情', hidden: true }
          },
          {
            path: 'report',
            name: 'FinanceReport',
            redirect: '/finance/reports/overall',
            meta: { title: '财务报表（兼容）', hidden: true }
          },
          {
            path: 'account',
            name: 'FinanceAccount',
            redirect: '/finance/accounts/list',
            meta: { title: '老人账户（兼容）', hidden: true }
          },
          {
            path: 'account-log',
            name: 'FinanceAccountLog',
            redirect: '/finance/accounts/ledger',
            meta: { title: '账户流水（兼容）', hidden: true }
          },
          {
            path: 'bill',
            redirect: '/finance/bills/in-resident',
            meta: { hidden: true }
          },
          {
            path: 'reconcile-center',
            redirect: '/finance/reconcile/center',
            meta: { hidden: true }
          },
          {
            path: 'reconcile-invoice',
            redirect: '/finance/reconcile/invoice',
            meta: { hidden: true }
          },
          {
            path: 'reconcile-exception',
            redirect: '/finance/reconcile/exception',
            meta: { hidden: true }
          }
        ]
      },
      {
        path: 'health',
        name: 'Health',
        meta: { title: '健康服务', icon: 'HeartOutlined', hidden: true },
        redirect: '/medical-care/center',
        children: [
          {
            path: 'medication',
            name: 'HealthMedication',
            meta: { title: '用药服务' },
            redirect: '/health/medication/drug-dictionary',
            children: [
              {
                path: 'drug-dictionary',
                name: 'HealthDrugDictionary',
                component: () => import('../views/health/DrugDictionary.vue'),
                meta: { title: '药品字典' }
              },
              {
                path: 'drug-deposit',
                name: 'HealthDrugDeposit',
                component: () => import('../views/health/DrugDeposit.vue'),
                meta: { title: '药品缴存' }
              },
              {
                path: 'medication-setting',
                name: 'HealthMedicationSetting',
                component: () => import('../views/health/MedicationSetting.vue'),
                meta: { title: '用药设置' }
              },
              {
                path: 'medication-registration',
                name: 'HealthMedicationRegistration',
                component: () => import('../views/health/MedicationRegistration.vue'),
                meta: { title: '用药登记' }
              },
              {
                path: 'medication-remaining',
                name: 'HealthMedicationRemaining',
                component: () => import('../views/health/MedicationRemaining.vue'),
                meta: { title: '剩余用药' }
              }
            ]
          },
          {
            path: 'management',
            name: 'HealthManagement',
            meta: { title: '健康管理' },
            redirect: '/health/management/archive',
            children: [
              {
                path: 'archive',
                name: 'HealthArchive',
                component: () => import('../views/health/HealthArchive.vue'),
                meta: { title: '健康档案' }
              },
              {
                path: 'data',
                name: 'HealthData',
                component: () => import('../views/health/HealthData.vue'),
                meta: { title: '健康数据' }
              }
            ]
          },
          {
            path: 'inspection',
            name: 'HealthInspection',
            component: () => import('../views/health/Inspection.vue'),
            meta: { title: '健康巡检' }
          },
          {
            path: 'nursing-log',
            name: 'HealthNursingLog',
            component: () => import('../views/health/NursingLog.vue'),
            meta: { title: '护理日志' }
          },
          {
            path: 'drug-dictionary',
            redirect: '/health/medication/drug-dictionary',
            meta: { hidden: true }
          },
          {
            path: 'drug-deposit',
            redirect: '/health/medication/drug-deposit',
            meta: { hidden: true }
          },
          {
            path: 'medication-setting',
            redirect: '/health/medication/medication-setting',
            meta: { hidden: true }
          },
          {
            path: 'medication-registration',
            redirect: '/health/medication/medication-registration',
            meta: { hidden: true }
          },
          {
            path: 'medication-remaining',
            redirect: '/health/medication/medication-remaining',
            meta: { hidden: true }
          },
          {
            path: 'archive',
            redirect: '/health/management/archive',
            meta: { hidden: true }
          },
          {
            path: 'data',
            redirect: '/health/management/data',
            meta: { hidden: true }
          }
        ]
      },
      {
        path: 'medical-care',
        name: 'MedicalCare',
        meta: { title: '医护健康服务', icon: 'MedicineBoxOutlined', roles: ['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
        redirect: '/medical-care/center',
        children: [
          {
            path: 'center',
            name: 'MedicalCareCenter',
            component: () => import('../views/medical/MedicalHealthCenter.vue'),
            meta: { title: '服务中心' }
          },
          {
            path: 'workbench',
            name: 'MedicalCareWorkbench',
            component: () => import('../views/medical/Workbench.vue'),
            meta: { title: '医护照护工作台', hidden: true }
          },
          {
            path: 'residents',
            name: 'MedicalCareResidentList',
            component: () => import('../views/medical/ResidentList.vue'),
            meta: { title: '长者患者列表' }
          },
          {
            path: 'basic-diseases',
            name: 'MedicalCareResidentDiseases',
            component: () => import('../views/medical/ResidentDiseaseManager.vue'),
            meta: { title: '基础疾病维护' }
          },
          {
            path: 'care-task-board',
            name: 'MedicalCareTaskBoard',
            component: () => import('../views/care/workbench/TaskBoard.vue'),
            meta: { title: '护理任务看板' }
          },
          {
            path: 'unified-task-center',
            name: 'MedicalCareUnifiedTaskCenter',
            component: () => import('../views/medical/UnifiedTaskCenter.vue'),
            meta: { title: '统一任务中心' }
          },
          {
            path: 'inspection',
            name: 'MedicalCareInspection',
            component: () => import('../views/health/Inspection.vue'),
            meta: { title: '健康巡检' }
          },
          {
            path: 'medication-registration',
            name: 'MedicalCareMedicationRegistration',
            component: () => import('../views/health/MedicationRegistration.vue'),
            meta: { title: '用药登记' }
          },
          {
            path: 'nursing-log',
            name: 'MedicalCareNursingLog',
            component: () => import('../views/health/NursingLog.vue'),
            meta: { title: '护理日志' }
          },
          {
            path: 'handovers',
            name: 'MedicalCareHandovers',
            component: () => import('../views/care/Handovers.vue'),
            meta: { title: '交接班' }
          },
          {
            path: 'assessment/tcm',
            name: 'MedicalCareTcmAssessment',
            component: () => import('../views/medical/TcmAssessment.vue'),
            meta: { title: '中医体质评估' }
          },
          {
            path: 'assessment/cvd',
            name: 'MedicalCareCvdAssessment',
            component: () => import('../views/medical/CvdRiskAssessment.vue'),
            meta: { title: '心血管风险评估' }
          },
          {
            path: 'integrated-account',
            name: 'MedicalCareIntegratedAccount',
            component: () => import('../views/medical/IntegratedHealthAccount.vue'),
            meta: { title: '健康服务与医护账户一体化' }
          },
          {
            path: 'orders',
            name: 'MedicalCareOrders',
            component: () => import('../views/medical/MedicalOrderCenter.vue'),
            meta: { title: '医嘱管理' }
          },
          {
            path: 'nursing-quality',
            name: 'MedicalCareNursingQuality',
            component: () => import('../views/medical/NursingQualityCenter.vue'),
            meta: { title: '护理与质量中心' }
          },
          {
            path: 'alert-rules',
            name: 'MedicalCareAlertRules',
            component: () => import('../views/medical/AlertRuleConfig.vue'),
            meta: { title: '异常规则配置' }
          },
          {
            path: 'ai-reports',
            name: 'MedicalCareAiReports',
            component: () => import('../views/medical/AiHealthReport.vue'),
            meta: { title: 'AI健康评估报告' }
          }
        ]
      },
      {
        path: 'fire',
        name: 'FireSafety',
        meta: {
          title: '消防安全管理',
          icon: 'SafetyOutlined',
          roles: ['GUARD', 'LOGISTICS_EMPLOYEE', 'LOGISTICS_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']
        },
        redirect: '/fire/facility-management',
        children: [
          {
            path: 'facility-management',
            name: 'FireFacilityManagement',
            component: () => import('../views/fire/FacilityManagement.vue'),
            meta: { title: '消防设施管理' }
          },
          {
            path: 'control-room-duty',
            name: 'FireControlRoomDuty',
            component: () => import('../views/fire/ControlRoomDuty.vue'),
            meta: { title: '控制室值班记录' }
          },
          {
            path: 'monthly-check',
            name: 'FireMonthlyCheck',
            component: () => import('../views/fire/MonthlyCheck.vue'),
            meta: { title: '每月防火检查' }
          },
          {
            path: 'day-patrol',
            name: 'FireDayPatrol',
            component: () => import('../views/fire/DayPatrol.vue'),
            meta: { title: '日间防火巡查' }
          },
          {
            path: 'night-patrol',
            name: 'FireNightPatrol',
            component: () => import('../views/fire/NightPatrol.vue'),
            meta: { title: '夜间防火巡查' }
          },
          {
            path: 'maintenance-report',
            name: 'FireMaintenanceReport',
            component: () => import('../views/fire/MaintenanceReport.vue'),
            meta: { title: '消防设施维护保养报告' }
          },
          {
            path: 'fault-maintenance',
            name: 'FireFaultMaintenance',
            component: () => import('../views/fire/FaultMaintenance.vue'),
            meta: { title: '建筑消防设施故障维护' }
          },
          {
            path: 'data-stats',
            name: 'FireDataStats',
            component: () => import('../views/fire/DataStats.vue'),
            meta: { title: '数据报表统计' }
          }
        ]
      },
      {
        path: 'stats',
        name: 'Stats',
        meta: { title: '统计分析', icon: 'BarChartOutlined' },
        redirect: '/stats/check-in',
        children: [
          {
            path: 'check-in',
            name: 'StatsCheckIn',
            component: () => import('../views/stats/CheckInStats.vue'),
            meta: { title: '入住统计' }
          },
          {
            path: 'consumption',
            name: 'StatsConsumption',
            component: () => import('../views/stats/ConsumptionStats.vue'),
            meta: { title: '消费统计' }
          },
          {
            path: 'elder-info',
            name: 'StatsElderInfo',
            component: () => import('../views/stats/ElderInfoStats.vue'),
            meta: { title: '老人信息统计' }
          },
          {
            path: 'org',
            name: 'StatsOrg',
            meta: { title: '机构统计' },
            redirect: '/stats/org/monthly-operation',
            children: [
              {
                path: 'monthly-operation',
                name: 'StatsOrgMonthlyOperation',
                component: () => import('../views/stats/OrgMonthlyOperation.vue'),
                meta: { title: '机构月运营详情' }
              },
              {
                path: 'elder-flow',
                name: 'StatsOrgElderFlow',
                component: () => import('../views/stats/OrgElderFlow.vue'),
                meta: { title: '老人出入统计' }
              },
              {
                path: 'bed-usage',
                name: 'StatsOrgBedUsage',
                component: () => import('../views/stats/OrgBedUsage.vue'),
                meta: { title: '床位使用统计' }
              }
            ]
          },
          {
            path: 'monthly-revenue',
            name: 'StatsMonthlyRevenue',
            component: () => import('../views/stats/MonthlyRevenueStats.vue'),
            meta: { title: '月运营收入统计' }
          },
          {
            path: 'elder-flow-report',
            name: 'StatsElderFlowReport',
            component: () => import('../views/stats/ElderFlowReport.vue'),
            meta: { title: '老人出入报表' }
          }
        ]
      },
      {
        path: 'card',
        name: 'Card',
        meta: { title: '一卡通管理', icon: 'CreditCardOutlined', hidden: true },
        redirect: '/card/account',
        children: [
          {
            path: 'account',
            name: 'CardAccount',
            component: () => import('../views/card/Account.vue'),
            meta: { title: '卡务管理' }
          },
          {
            path: 'recharge',
            name: 'CardRecharge',
            component: () => import('../views/card/Recharge.vue'),
            meta: { title: '充值记录' }
          },
          {
            path: 'consume',
            name: 'CardConsume',
            component: () => import('../views/card/Consume.vue'),
            meta: { title: '消费记录' }
          }
        ]
      },
      {
        path: 'life',
        name: 'Life',
        meta: { title: '生活与健康', icon: 'CoffeeOutlined', hidden: true },
        redirect: '/life/meal-plan',
        children: [
          {
            path: 'birthday',
            name: 'LifeBirthday',
            component: () => import('../views/life/Birthday.vue'),
            meta: { title: '会员生日' }
          },
          {
            path: 'meal-plan',
            name: 'LifeMealPlan',
            component: () => import('../views/life/MealPlan.vue'),
            meta: { title: '膳食计划' }
          },
          {
            path: 'activity',
            name: 'LifeActivity',
            component: () => import('../views/life/Activity.vue'),
            meta: { title: '活动管理' }
          },
          {
            path: 'incident',
            name: 'LifeIncident',
            component: () => import('../views/life/Incident.vue'),
            meta: { title: '事故登记' }
          },
          {
            path: 'health-basic',
            name: 'LifeHealthBasic',
            component: () => import('../views/life/HealthBasic.vue'),
            meta: { title: '基础健康记录' }
          },
          {
            path: 'room-cleaning',
            name: 'LifeRoomCleaning',
            component: () => import('../views/life/RoomCleaning.vue'),
            meta: { title: '房间打扫' }
          },
          {
            path: 'maintenance',
            name: 'LifeMaintenance',
            component: () => import('../views/life/Maintenance.vue'),
            meta: { title: '维修管理' }
          }
        ]
      },
      {
        path: 'dining',
        name: 'Dining',
        meta: { title: '餐饮管理（兼容）', icon: 'CoffeeOutlined', hidden: true },
        redirect: '/logistics/dining/dish',
        children: [
          {
            path: 'dish',
            name: 'DiningDish',
            redirect: '/logistics/dining/dish',
            meta: { title: '菜品管理' }
          },
          {
            path: 'order',
            name: 'DiningOrder',
            redirect: '/logistics/dining/order',
            meta: { title: '点餐' }
          },
          {
            path: 'stats',
            name: 'DiningStats',
            redirect: '/logistics/dining/stats',
            meta: { title: '订餐统计' }
          },
          {
            path: 'procurement-plan',
            name: 'DiningProcurementPlan',
            redirect: '/logistics/dining/procurement-plan',
            meta: { title: '采购计划单' }
          },
          {
            path: 'recipe',
            name: 'DiningRecipe',
            redirect: '/logistics/dining/recipe',
            meta: { title: '食谱管理' }
          },
          {
            path: 'prep-zone',
            name: 'DiningPrepZone',
            redirect: '/logistics/dining/prep-zone',
            meta: { title: '分区备餐' }
          },
          {
            path: 'delivery-area',
            name: 'DiningDeliveryArea',
            redirect: '/logistics/dining/delivery-area',
            meta: { title: '送餐区域' }
          },
          {
            path: 'delivery-record',
            name: 'DiningDeliveryRecord',
            redirect: '/logistics/dining/delivery-plan',
            meta: { title: '送餐记录' }
          }
        ]
      },
      {
        path: 'assessment',
        name: 'AssessmentLegacy',
        meta: {
          title: '评估管理',
          icon: 'FileSearchOutlined',
          hidden: true,
          roles: ['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_EMPLOYEE', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']
        },
        redirect: '/elder/assessment/ability/admission',
        children: [
          {
            path: 'ability',
            name: 'AssessmentAbilityLegacy',
            meta: { title: '能力评估管理', hidden: true },
            redirect: '/elder/assessment/ability/admission',
            children: [
              {
                path: 'admission',
                name: 'AssessmentAdmissionLegacy',
                redirect: '/elder/assessment/ability/admission',
                meta: { hidden: true }
              },
              {
                path: 'registration',
                name: 'AssessmentRegistrationLegacy',
                redirect: '/elder/assessment/ability/registration',
                meta: { hidden: true }
              },
              {
                path: 'continuous',
                name: 'AssessmentContinuousLegacy',
                redirect: '/elder/assessment/ability/continuous',
                meta: { hidden: true }
              },
              {
                path: 'archive',
                name: 'AssessmentArchiveLegacy',
                redirect: '/elder/assessment/ability/archive',
                meta: { hidden: true }
              }
            ]
          },
          {
            path: 'other-scale',
            name: 'AssessmentOtherScaleLegacy',
            redirect: '/elder/assessment/other-scale',
            meta: { hidden: true }
          },
          {
            path: 'template',
            name: 'AssessmentTemplateLegacy',
            redirect: '/elder/assessment/template',
            meta: { hidden: true }
          },
          {
            path: 'cognitive',
            name: 'AssessmentCognitiveLegacy',
            redirect: '/elder/assessment/cognitive',
            meta: { hidden: true }
          },
          {
            path: 'self-care',
            name: 'AssessmentSelfCareLegacy',
            redirect: '/elder/assessment/self-care',
            meta: { hidden: true }
          },
          {
            path: 'admission',
            redirect: '/elder/assessment/ability/admission',
            meta: { hidden: true }
          },
          {
            path: 'registration',
            redirect: '/elder/assessment/ability/registration',
            meta: { hidden: true }
          },
          {
            path: 'continuous',
            redirect: '/elder/assessment/ability/continuous',
            meta: { hidden: true }
          },
          {
            path: 'archive',
            redirect: '/elder/assessment/ability/archive',
            meta: { hidden: true }
          }
        ]
      },
      {
        path: 'survey',
        name: 'Survey',
        meta: { title: '问卷与持续改进', icon: 'FormOutlined', hidden: true },
        redirect: '/survey/question-bank',
        children: [
          {
            path: 'question-bank',
            name: 'SurveyQuestionBank',
            component: () => import('../views/survey/QuestionBank.vue'),
            meta: { title: '题库管理' }
          },
          {
            path: 'template',
            name: 'SurveyTemplate',
            component: () => import('../views/survey/Template.vue'),
            meta: { title: '模板管理' }
          },
          {
            path: 'submit',
            name: 'SurveySubmit',
            component: () => import('../views/survey/Submit.vue'),
            meta: { title: '问卷填写' }
          },
          {
            path: 'stats',
            name: 'SurveyStats',
            component: () => import('../views/survey/Stats.vue'),
            meta: { title: '问卷统计' }
          },
          {
            path: 'performance',
            name: 'SurveyPerformance',
            component: () => import('../views/survey/Performance.vue'),
            meta: { title: '绩效榜' }
          }
        ]
      },
      {
        path: 'oa',
        name: 'OA',
        meta: {
          title: '行政管理',
          icon: 'ApartmentOutlined',
          roles: [
            'STAFF',
            'HR_EMPLOYEE', 'HR_MINISTER',
            'MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER',
            'NURSING_EMPLOYEE', 'NURSING_MINISTER',
            'FINANCE_EMPLOYEE', 'FINANCE_MINISTER',
            'LOGISTICS_EMPLOYEE', 'LOGISTICS_MINISTER',
            'MARKETING_EMPLOYEE', 'MARKETING_MINISTER',
            'DIRECTOR', 'SYS_ADMIN', 'ADMIN'
          ]
        },
        redirect: '/oa/overview',
        children: [
          {
            path: 'overview',
            name: 'OaOverview',
            component: () => import('../views/oa/AdminCenter.vue'),
            meta: { title: '行政中心' }
          },
          {
            path: 'work-execution',
            name: 'OaWorkExecution',
            meta: { title: '协同办公' },
            redirect: '/oa/work-execution/task',
            children: [
              {
                path: 'task',
                name: 'OaTask',
                component: () => import('../views/oa/Task.vue'),
                meta: { title: '任务管理' }
              },
              {
                path: 'calendar',
                name: 'OaCalendar',
                component: () => import('../views/oa/Calendar.vue'),
                meta: { title: '行政日历 / 协同日历' }
              },
              {
                path: 'daily-report',
                name: 'OaDailyReport',
                component: () => import('../views/oa/DailyReport.vue'),
                meta: { title: '日总结' }
              },
              {
                path: 'weekly-report',
                name: 'OaWeeklyReport',
                component: () => import('../views/oa/WeeklyReport.vue'),
                meta: { title: '周总结' }
              },
              {
                path: 'monthly-report',
                name: 'OaMonthlyReport',
                component: () => import('../views/oa/MonthlyReport.vue'),
                meta: { title: '月总结' }
              },
              {
                path: 'yearly-report',
                name: 'OaYearlyReport',
                component: () => import('../views/oa/YearlyReport.vue'),
                meta: { title: '年总结' }
              },
              {
                path: 'attendance-leave',
                name: 'OaWorkExecutionAttendanceLeave',
                redirect: '/oa/attendance-leave',
                meta: { title: '考勤与请假（兼容）', hidden: true }
              }
            ]
          },
          {
            path: 'life',
            name: 'OaLife',
            meta: { title: '生活服务' },
            redirect: '/oa/life/birthday',
            children: [
              {
                path: 'birthday',
                name: 'OaLifeBirthday',
                component: () => import('../views/life/Birthday.vue'),
                meta: { title: '会员生日' }
              },
              {
                path: 'room-cleaning',
                name: 'OaLifeRoomCleaning',
                component: () => import('../views/life/RoomCleaning.vue'),
                meta: { title: '房间打扫' }
              },
              {
                path: 'maintenance',
                name: 'OaLifeMaintenance',
                component: () => import('../views/life/Maintenance.vue'),
                meta: { title: '维修管理' }
              }
            ]
          },
          {
            path: 'staff',
            name: 'OaStaff',
            redirect: '/hr/profile/basic',
            meta: { title: '员工档案（兼容）', hidden: true }
          },
          {
            path: 'card',
            name: 'OaCard',
            meta: { title: '一卡通' },
            redirect: '/oa/card/account',
            children: [
              {
                path: 'account',
                name: 'OaCardAccount',
                component: () => import('../views/card/Account.vue'),
                meta: { title: '卡务管理' }
              },
              {
                path: 'recharge',
                name: 'OaCardRecharge',
                component: () => import('../views/card/Recharge.vue'),
                meta: { title: '充值记录' }
              },
              {
                path: 'consume',
                name: 'OaCardConsume',
                component: () => import('../views/card/Consume.vue'),
                meta: { title: '消费记录' }
              }
            ]
          },
          {
            path: 'survey',
            name: 'OaSurvey',
            meta: { title: '满意度问卷（兼容）', hidden: true },
            redirect: '/oa/activity-center/survey-manage',
            children: [
              {
                path: 'manage',
                name: 'OaSurveyManage',
                redirect: '/oa/activity-center/survey-manage',
                meta: { title: '问卷管理（兼容）', hidden: true }
              },
              {
                path: 'stats',
                name: 'OaSurveyStats',
                redirect: '/oa/activity-center/survey-stats',
                meta: { title: '问卷统计（兼容）', hidden: true }
              }
            ]
          },
          {
            path: 'knowledge',
            name: 'OaKnowledge',
            component: () => import('../views/oa/Knowledge.vue'),
            meta: { title: '知识库' }
          },
          {
            path: 'group-settings',
            name: 'OaGroupSettings',
            component: () => import('../views/oa/GroupSettings.vue'),
            meta: { title: '分组设置（兼容）', hidden: true }
          },
          {
            path: 'notice',
            name: 'OaNotice',
            component: () => import('../views/oa/Notice.vue'),
            meta: { title: '通知公告' }
          },
          {
            path: 'activity-center',
            name: 'OaActivityCenter',
            meta: { title: '活动与文化' },
            redirect: '/oa/activity-center/plan',
            children: [
              {
                path: 'plan',
                name: 'OaActivityCenterPlan',
                component: () => import('../views/oa/ActivityPlan.vue'),
                meta: { title: '活动中心' }
              },
              {
                path: 'records',
                name: 'OaActivityCenterRecords',
                component: () => import('../views/life/Activity.vue'),
                meta: { title: '活动记录' }
              },
              {
                path: 'survey-manage',
                name: 'OaActivityCenterSurveyManage',
                component: () => import('../views/survey/Template.vue'),
                meta: { title: '满意度调查' }
              },
              {
                path: 'survey-stats',
                name: 'OaActivityCenterSurveyStats',
                component: () => import('../views/survey/Stats.vue'),
                meta: { title: '调查统计' }
              }
            ]
          },
          {
            path: 'activity',
            name: 'OaActivity',
            redirect: '/oa/activity-center/records',
            meta: { title: '活动管理（兼容）', hidden: true }
          },
          {
            path: 'activity-plan',
            name: 'OaActivityPlan',
            redirect: '/oa/activity-center/plan',
            meta: { title: '活动计划（兼容）', hidden: true }
          },
          {
            path: 'reward-punishment',
            name: 'OaRewardPunishment',
            redirect: '/hr/performance/reward-punishment',
            meta: { title: '奖惩管理（兼容）', hidden: true }
          },
          {
            path: 'training',
            name: 'OaTraining',
            redirect: '/hr/development/records',
            meta: { title: '培训管理（兼容）', hidden: true }
          },
          {
            path: 'portal',
            name: 'OaPortal',
            component: () => import('../views/oa/Portal.vue'),
            meta: { title: '门户与待办（兼容）', hidden: true }
          },
          {
            path: 'my-info',
            name: 'OaMyInfo',
            component: () => import('../views/oa/MyInfo.vue'),
            meta: { title: '我的信息（兼容）', hidden: true }
          },
          {
            path: 'todo',
            name: 'OaTodo',
            component: () => import('../views/oa/Todo.vue'),
            meta: { title: '待办事项（兼容）', hidden: true }
          },
          {
            path: 'approval',
            name: 'OaApproval',
            component: () => import('../views/oa/Approval.vue'),
            meta: { title: '审批中心' }
          },
          {
            path: 'attendance-leave',
            name: 'OaAttendanceLeave',
            component: () => import('../views/oa/AttendanceLeave.vue'),
            meta: { title: '考勤与请假（兼容）', hidden: true }
          },
          {
            path: 'document',
            name: 'OaDocument',
            component: () => import('../views/oa/Document.vue'),
            meta: { title: '文档中心' }
          },
          {
            path: 'work-report',
            name: 'OaWorkReport',
            component: () => import('../views/oa/WorkReport.vue'),
            meta: { title: '工作总结（兼容）', hidden: true }
          }
        ]
      },
      {
        path: 'hr',
        name: 'Hr',
        meta: { title: '人力资源', icon: 'TeamOutlined', roles: ['HR_EMPLOYEE', 'HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
        redirect: '/hr/overview',
        children: [
          {
            path: 'overview',
            name: 'HrOverview',
            component: () => import('../views/hr/HrCenter.vue'),
            meta: { title: '人力资源中心', roles: ['HR_EMPLOYEE', 'HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          },
          {
            path: 'workbench',
            name: 'HrWorkbench',
            redirect: '/hr/overview',
            meta: { title: '人事行政工作台（兼容）', roles: ['HR_EMPLOYEE', 'HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true }
          },
          {
            path: 'recruitment',
            name: 'HrRecruitment',
            meta: { title: '招聘管理', roles: ['ADMIN'] },
            redirect: '/hr/recruitment/needs',
            children: [
              {
                path: 'needs',
                name: 'HrRecruitmentNeeds',
                component: () => import('../views/hr/HrRecruitmentNeeds.vue'),
                meta: { title: '招聘办理台', roles: ['ADMIN'] }
              },
              {
                path: 'job-posting',
                name: 'HrRecruitmentJobPosting',
                redirect: '/hr/recruitment/needs?scene=job-posting',
                meta: { title: '岗位发布（兼容）', roles: ['ADMIN'], hidden: true }
              },
              {
                path: 'candidates',
                name: 'HrRecruitmentCandidates',
                redirect: '/hr/recruitment/needs?scene=candidates',
                meta: { title: '候选人库（兼容）', roles: ['ADMIN'], hidden: true }
              },
              {
                path: 'interviews',
                name: 'HrRecruitmentInterviews',
                redirect: '/hr/recruitment/needs?scene=interviews',
                meta: { title: '面试管理（兼容）', roles: ['ADMIN'], hidden: true }
              },
              {
                path: 'onboarding',
                name: 'HrRecruitmentOnboarding',
                redirect: '/hr/recruitment/needs?scene=onboarding',
                meta: { title: '入职办理（兼容）', roles: ['ADMIN'], hidden: true }
              },
              {
                path: 'materials',
                name: 'HrRecruitmentMaterials',
                redirect: '/hr/recruitment/needs?scene=materials',
                meta: { title: '入职资料收集（兼容）', roles: ['ADMIN'], hidden: true }
              },
              {
                path: 'offboarding',
                name: 'HrRecruitmentOffboarding',
                redirect: '/hr/recruitment/needs?scene=offboarding',
                meta: { title: '退职办理（兼容）', roles: ['ADMIN'], hidden: true }
              }
            ]
          },
          {
            path: 'profile',
            name: 'HrProfileCenter',
            meta: { title: '员工档案', roles: ['HR_EMPLOYEE', 'HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
            redirect: '/hr/profile/basic',
            children: [
              {
                path: 'onboarding',
                name: 'HrProfileOnboarding',
                component: () => import('../views/hr/HrOnboardingWizard.vue'),
                meta: { title: '入职向导', roles: ['ADMIN'] }
              },
              {
                path: 'basic',
                name: 'HrProfileBasic',
                component: () => import('../views/hr/Staff.vue'),
                props: { title: '员工基本信息', subTitle: '员工档案中心 / 员工基本信息' },
                meta: { title: '员工基本信息', roles: ['ADMIN'] }
              },
              {
                path: 'contracts',
                name: 'HrProfileContracts',
                component: () => import('../views/hr/HrProfileContracts.vue'),
                meta: { title: '劳动合同管理', roles: ['ADMIN'] }
              },
              {
                path: 'account-access',
                name: 'HrProfileAccountAccess',
                component: () => import('../views/Staff.vue'),
                props: { title: '账号与领导设置', subTitle: '人事创建账号、初始化密码与上下级监管关系' },
                meta: { title: '账号与领导设置', roles: ['HR_EMPLOYEE', 'HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              },
              {
                path: 'contract-templates',
                name: 'HrProfileContractTemplates',
                component: () => import('../views/hr/HrProfileTemplates.vue'),
                meta: { title: '合同模板库', roles: ['ADMIN'] }
              },
              {
                path: 'contract-print',
                name: 'HrProfileContractPrint',
                component: () => import('../views/hr/HrContractPrint.vue'),
                meta: { title: '合同打印', roles: ['ADMIN'] }
              },
              {
                path: 'certificates',
                name: 'HrProfileCertificates',
                component: () => import('../views/hr/HrProfileCertificates.vue'),
                meta: { title: '证书上传', roles: ['ADMIN'] }
              },
              {
                path: 'training-records',
                name: 'HrProfileTrainingRecords',
                redirect: '/hr/development/records?scene=records',
                meta: { title: '培训记录（兼容）', roles: ['ADMIN'], hidden: true }
              },
              {
                path: 'attachments',
                name: 'HrProfileAttachments',
                component: () => import('../views/hr/HrProfileAttachments.vue'),
                meta: { title: '档案附件', roles: ['ADMIN'] }
              },
              {
                path: 'contract-reminders',
                name: 'HrProfileContractReminders',
                component: () => import('../views/hr/HrContractReminder.vue'),
                meta: { title: '合同到期提醒', roles: ['ADMIN'] }
              },
              {
                path: 'social-security-reminders',
                name: 'HrProfileSocialSecurityReminders',
                component: () => import('../views/hr/HrSocialSecurityReminder.vue'),
                meta: { title: '社保到期/未办理', roles: ['HR_EMPLOYEE', 'HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              }
            ]
          },
          {
            path: 'attendance',
            name: 'HrAttendance',
            meta: { title: '考勤与班组', roles: ['ADMIN'] },
            redirect: '/hr/attendance/schemes',
            children: [
              {
                path: 'schemes',
                name: 'HrAttendanceSchemes',
                component: () => import('../views/hr/HrAttendanceSchemes.vue'),
                meta: { title: '排班管理', roles: ['ADMIN'] }
              },
              {
                path: 'groups',
                name: 'HrAttendanceGroups',
                component: () => import('../views/care/CaregiverGroups.vue'),
                meta: { title: '班组设置', roles: ['ADMIN'] }
              },
              {
                path: 'calendar',
                name: 'HrAttendanceCalendar',
                component: () => import('../views/care/ShiftCalendar.vue'),
                meta: { title: '排班日历', roles: ['ADMIN'] }
              },
              {
                path: 'leave-approval',
                name: 'HrAttendanceLeaveApproval',
                component: () => import('../views/hr/HrAttendanceApprovalPage.vue'),
                meta: { title: '请假审批', roles: ['ADMIN'] }
              },
              {
                path: 'shift-change',
                name: 'HrAttendanceShiftChange',
                redirect: '/hr/attendance/leave-approval?scene=shift-change',
                meta: { title: '调班申请（兼容）', roles: ['ADMIN'], hidden: true }
              },
              {
                path: 'overtime',
                name: 'HrAttendanceOvertime',
                redirect: '/hr/attendance/leave-approval?scene=overtime',
                meta: { title: '加班申请（兼容）', roles: ['ADMIN'], hidden: true }
              },
              {
                path: 'records',
                name: 'HrAttendanceRecords',
                component: () => import('../views/hr/HrAttendanceRecords.vue'),
                meta: { title: '考勤记录', roles: ['ADMIN'] }
              },
              {
                path: 'abnormal',
                name: 'HrAttendanceAbnormal',
                component: () => import('../views/hr/HrAttendanceAbnormal.vue'),
                meta: { title: '考勤异常', roles: ['ADMIN'] }
              },
              {
                path: 'access-control',
                name: 'HrAttendanceAccessControl',
                component: () => import('../views/hr/HrAccessControl.vue'),
                meta: { title: '门禁对接', roles: ['ADMIN'] }
              },
              {
                path: 'card-sync',
                name: 'HrAttendanceCardSync',
                component: () => import('../views/hr/HrCardSync.vue'),
                meta: { title: '一卡通数据对接', roles: ['ADMIN'] }
              }
            ]
          },
          {
            path: 'compliance',
            name: 'HrCompliance',
            meta: { title: '制度与合规', roles: ['ADMIN'] },
            redirect: '/hr/compliance/policies',
            children: [
              { path: 'policies', name: 'HrCompliancePolicies', component: () => import('../views/hr/HrPolicies.vue'), meta: { title: '规章制度库', roles: ['ADMIN'] } },
              { path: 'policy-alerts', name: 'HrCompliancePolicyAlerts', component: () => import('../views/hr/HrPolicyAlerts.vue'), meta: { title: '制度更新预警', roles: ['ADMIN'] } }
            ]
          },
          {
            path: 'oa',
            name: 'HrOaCompat',
            meta: { title: '行政协同（兼容）', roles: ['ADMIN'], hidden: true },
            redirect: '/oa/notice',
            children: [
              { path: 'notices', name: 'HrOaNotices', redirect: '/oa/notice', meta: { title: '通知管理（兼容）', roles: ['ADMIN'], hidden: true } },
              { path: 'tasks', name: 'HrOaTasks', redirect: '/oa/work-execution/task', meta: { title: '任务管理（兼容）', roles: ['ADMIN'], hidden: true } },
              { path: 'execution', name: 'HrOaExecution', redirect: '/oa/todo', meta: { title: '工作执行（兼容）', roles: ['ADMIN'], hidden: true } },
              { path: 'activity-plan', name: 'HrOaActivityPlan', redirect: '/oa/activity-plan', meta: { title: '活动计划（兼容）', roles: ['ADMIN'], hidden: true } },
              { path: 'knowledge', name: 'HrOaKnowledge', redirect: '/oa/knowledge', meta: { title: '知识库（兼容）', roles: ['ADMIN'], hidden: true } },
              { path: 'groups', name: 'HrOaGroups', redirect: '/oa/group-settings', meta: { title: '分组设置（兼容）', roles: ['ADMIN'], hidden: true } },
              { path: 'policies', name: 'HrOaPolicies', redirect: '/hr/compliance/policies', meta: { title: '规章制度库（兼容）', roles: ['ADMIN'], hidden: true } },
              { path: 'policy-alerts', name: 'HrOaPolicyAlerts', redirect: '/hr/compliance/policy-alerts', meta: { title: '制度更新预警（兼容）', roles: ['ADMIN'], hidden: true } }
            ]
          },
          {
            path: 'expense',
            name: 'HrExpense',
            meta: { title: '薪酬与费用', roles: ['ADMIN'] },
            redirect: '/hr/expense/records?scene=training-reimburse',
            children: [
              { path: 'records', name: 'HrExpenseRecords', component: () => import('../views/hr/HrExpenseScenePage.vue'), meta: { title: '费用报销台', roles: ['ADMIN'] } },
              { path: 'training-reimburse', name: 'HrExpenseTrainingReimburse', redirect: '/hr/expense/records?scene=training-reimburse', meta: { title: '外出培训费用报销（兼容）', roles: ['ADMIN'], hidden: true } },
              { path: 'subsidy', name: 'HrExpenseSubsidy', redirect: '/hr/expense/records?scene=subsidy', meta: { title: '补贴申请（兼容）', roles: ['ADMIN'], hidden: true } },
              { path: 'meal-fee', name: 'HrExpenseMealFee', component: () => import('../views/hr/HrExpenseMealFee.vue'), meta: { title: '员工餐费', roles: ['ADMIN'] } },
              { path: 'electricity-fee', name: 'HrExpenseElectricityFee', component: () => import('../views/hr/HrExpenseElectricityFee.vue'), meta: { title: '员工电费', roles: ['ADMIN'] } },
              { path: 'salary-subsidy', name: 'HrExpenseSalarySubsidy', redirect: '/hr/expense/records?scene=salary-subsidy', meta: { title: '工资补贴记录（兼容）', roles: ['ADMIN'], hidden: true } },
              { path: 'approval-flow', name: 'HrExpenseApprovalFlow', component: () => import('../views/hr/HrExpenseApprovalFlow.vue'), meta: { title: '报销审批流', roles: ['ADMIN'] } }
            ]
          },
          {
            path: 'development',
            name: 'HrDevelopment',
            meta: { title: '培训发展', roles: ['ADMIN'] },
            redirect: '/hr/development/records',
            children: [
              { path: 'plans', name: 'HrDevelopmentPlans', redirect: '/hr/development/records?scene=plans', meta: { title: '培训计划', roles: ['ADMIN'] } },
              { path: 'enrollments', name: 'HrDevelopmentEnrollments', redirect: '/hr/development/records?scene=enrollments', meta: { title: '培训报名（兼容）', roles: ['ADMIN'], hidden: true } },
              { path: 'signin', name: 'HrDevelopmentSignin', redirect: '/hr/development/records?scene=signin', meta: { title: '培训签到（兼容）', roles: ['ADMIN'], hidden: true } },
              { path: 'records', name: 'HrDevelopmentRecords', component: () => import('../views/hr/Training.vue'), meta: { title: '培训记录', roles: ['ADMIN'] } },
              { path: 'certificates', name: 'HrDevelopmentCertificates', component: () => import('../views/hr/HrProfileCertificates.vue'), meta: { title: '证书管理', roles: ['ADMIN'] } },
              { path: 'certificate-reminders', name: 'HrDevelopmentCertificateReminders', component: () => import('../views/hr/HrCertificateReminders.vue'), meta: { title: '证书到期提醒', roles: ['ADMIN'] } }
            ]
          },
          {
            path: 'performance',
            name: 'HrPerformanceCenter',
            meta: { title: '绩效管理', roles: ['ADMIN'] },
            redirect: '/hr/performance/reports',
            children: [
              { path: 'nursing', name: 'HrPerformanceNursing', redirect: '/hr/performance/reports?scene=nursing', meta: { title: '护理绩效（兼容）', roles: ['ADMIN'], hidden: true } },
              { path: 'sales', name: 'HrPerformanceSales', redirect: '/hr/performance/reports?scene=sales', meta: { title: '销售绩效（兼容）', roles: ['ADMIN'], hidden: true } },
              { path: 'admin', name: 'HrPerformanceAdmin', redirect: '/hr/performance/reports?scene=admin', meta: { title: '行政绩效（兼容）', roles: ['ADMIN'], hidden: true } },
              { path: 'scoring-rules', name: 'HrPerformanceScoringRules', component: () => import('../views/hr/PointsRule.vue'), meta: { title: '评分规则配置', roles: ['ADMIN'] } },
              { path: 'generation', name: 'HrPerformanceGeneration', redirect: '/hr/performance/reports?scene=generation', meta: { title: '绩效生成（兼容）', roles: ['ADMIN'], hidden: true } },
              { path: 'reports', name: 'HrPerformanceReports', component: () => import('../views/hr/Performance.vue'), meta: { title: '绩效报表', roles: ['ADMIN'] } },
              { path: 'reward-punishment', name: 'HrPerformanceRewardPunishment', component: () => import('../views/oa/RewardPunishment.vue'), meta: { title: '奖惩记录', roles: ['ADMIN'] } }
            ]
          },
          {
            path: 'staff',
            name: 'HrStaff',
            redirect: '/hr/profile/basic',
            meta: { title: '员工档案（兼容）', roles: ['ADMIN'], hidden: true }
          },
          {
            path: 'training',
            name: 'HrTraining',
            redirect: '/hr/development/records',
            meta: { title: '培训管理（兼容）', roles: ['ADMIN'], hidden: true }
          },
          {
            path: 'incentive',
            name: 'HrIncentive',
            meta: { title: '积分激励', roles: ['ADMIN'] },
            redirect: '/hr/incentive/ledger',
            children: [
              {
                path: 'ledger',
                name: 'HrIncentiveLedger',
                component: () => import('../views/hr/Points.vue'),
                meta: { title: '积分台账', roles: ['ADMIN'] }
              },
              {
                path: 'rules',
                name: 'HrIncentiveRules',
                component: () => import('../views/hr/PointsRule.vue'),
                meta: { title: '积分规则', roles: ['ADMIN'] }
              }
            ]
          },
          {
            path: 'points',
            name: 'HrPoints',
            redirect: '/hr/incentive/ledger',
            meta: { title: '积分管理（兼容）', roles: ['ADMIN'], hidden: true }
          },
          {
            path: 'points-rule',
            name: 'HrPointsRule',
            redirect: '/hr/incentive/rules',
            meta: { title: '积分规则（兼容）', roles: ['ADMIN'], hidden: true }
          },
          {
            path: 'performance-board',
            name: 'HrPerformanceLegacy',
            component: () => import('../views/hr/Performance.vue'),
            meta: { title: '绩效看板', roles: ['ADMIN'], hidden: true }
          }
        ]
      },
      {
        path: 'base-config',
        name: 'BaseConfig',
        meta: { title: '基础数据配置', icon: 'DatabaseOutlined', roles: ['ADMIN'] },
        redirect: '/base-config/elder-type',
        children: [
          {
            path: 'elder-type',
            name: 'BaseConfigElderType',
            component: () => import('../views/base-config/Index.vue'),
            props: { title: '老人类别', groupCode: 'ELDER_CATEGORY' },
            meta: { title: '老人类别', roles: ['ADMIN'] }
          },
          {
            path: 'marketing',
            name: 'BaseConfigMarketing',
            meta: { title: '营销', roles: ['ADMIN'] },
            redirect: '/base-config/marketing/customer-tag',
            children: [
              {
                path: 'customer-tag',
                name: 'BaseConfigMarketingCustomerTag',
                component: () => import('../views/base-config/Index.vue'),
                props: { title: '客户标签', groupCode: 'MARKETING_CUSTOMER_TAG' },
                meta: { title: '客户标签', roles: ['ADMIN'] }
              },
              {
                path: 'source-channel',
                name: 'BaseConfigMarketingSourceChannel',
                component: () => import('../views/base-config/Index.vue'),
                props: { title: '来源渠道', groupCode: 'MARKETING_SOURCE_CHANNEL' },
                meta: { title: '来源渠道', roles: ['ADMIN'] }
              }
            ]
          },
          {
            path: 'assessment',
            name: 'BaseConfigAssessment',
            component: () => import('../views/base-config/Index.vue'),
            props: { title: '评估', groupCode: 'ASSESSMENT_TYPE' },
            meta: { title: '评估', roles: ['ADMIN'] }
          },
          {
            path: 'residence',
            name: 'BaseConfigResidence',
            meta: { title: '入住', roles: ['ADMIN'] },
            redirect: '/base-config/residence/bed-type',
            children: [
              {
                path: 'bed-type',
                name: 'BaseConfigResidenceBedType',
                redirect: '/logistics/assets/bed-type-config',
                meta: { title: '床位类型', roles: ['ADMIN'] }
              },
              {
                path: 'room-type',
                name: 'BaseConfigResidenceRoomType',
                redirect: '/logistics/assets/room-type-config',
                meta: { title: '房间类型', roles: ['ADMIN'] }
              },
              {
                path: 'area-settings',
                name: 'BaseConfigResidenceAreaSettings',
                redirect: '/logistics/assets/area-config',
                meta: { title: '区域设置', roles: ['ADMIN'] }
              },
              {
                path: 'building-management',
                name: 'BaseConfigResidenceBuildingManagement',
                redirect: '/logistics/assets/bed-management',
                meta: { title: '楼栋管理', roles: ['ADMIN'], hidden: true }
              }
            ]
          },
          {
            path: 'activity',
            name: 'BaseConfigActivity',
            component: () => import('../views/base-config/Index.vue'),
            props: { title: '活动', groupCode: 'ACTIVITY_TYPE' },
            meta: { title: '活动', roles: ['ADMIN'] }
          },
          {
            path: 'community',
            name: 'BaseConfigCommunity',
            meta: { title: '社区', roles: ['ADMIN'] },
            redirect: '/base-config/community/maintenance-category',
            children: [
              {
                path: 'maintenance-category',
                name: 'BaseConfigCommunityMaintenanceCategory',
                component: () => import('../views/base-config/Index.vue'),
                props: { title: '维修分类', groupCode: 'COMMUNITY_REPAIR_CATEGORY' },
                meta: { title: '维修分类', roles: ['ADMIN'] }
              },
              {
                path: 'task-type-settings',
                name: 'BaseConfigCommunityTaskTypeSettings',
                component: () => import('../views/base-config/Index.vue'),
                props: { title: '任务类型设置', groupCode: 'COMMUNITY_TASK_TYPE' },
                meta: { title: '任务类型设置', roles: ['ADMIN'] }
              }
            ]
          },
          {
            path: 'discharge-fee-settings',
            name: 'BaseConfigDischargeFeeSettings',
            component: () => import('../views/base-config/Index.vue'),
            props: { title: '退住费用设置', groupCode: 'DISCHARGE_FEE_CONFIG' },
            meta: { title: '退住费用设置', roles: ['ADMIN'] }
          },
          {
            path: 'fee',
            name: 'BaseConfigFee',
            component: () => import('../views/base-config/Index.vue'),
            props: { title: '费用', groupCode: 'FEE_TYPE' },
            meta: { title: '费用', roles: ['ADMIN'] }
          },
          {
            path: 'refund-reason',
            name: 'BaseConfigRefundReason',
            component: () => import('../views/base-config/Index.vue'),
            props: { title: '退款原因', groupCode: 'REFUND_REASON' },
            meta: { title: '退款原因', roles: ['ADMIN'] }
          },
          {
            path: 'trial-package',
            name: 'BaseConfigTrialPackage',
            component: () => import('../views/base-config/Index.vue'),
            props: { title: '试住套餐', groupCode: 'TRIAL_STAY_PACKAGE' },
            meta: { title: '试住套餐', roles: ['ADMIN'] }
          },
          {
            path: 'index',
            redirect: '/base-config/elder-type',
            meta: { hidden: true }
          }
        ]
      },
      {
        path: 'system',
        name: 'System',
        meta: { title: '系统管理', icon: 'SettingOutlined', roles: ['DIRECTOR', 'SYS_ADMIN', 'ADMIN', 'HR_MINISTER'] },
        redirect: '/system/site-config',
        children: [
          {
            path: 'site-config',
            name: 'SystemSiteConfigCenter',
            component: () => import('../views/System/SiteConfigCenter.vue'),
            meta: { title: '官网配置中心', roles: ['ADMIN', 'HR_MINISTER'] }
          },
          {
            path: 'org-info',
            name: 'SystemOrgInfo',
            component: () => import('../views/System/OrgInfo.vue'),
            meta: { title: '机构信息', roles: ['ADMIN'] }
          },
          {
            path: 'org-manage',
            name: 'SystemOrgManage',
            meta: { title: '机构管理（已下沉）', roles: ['ADMIN'], hidden: true },
            redirect: '/system/site-config?tab=profile',
            children: [
              {
                path: 'intro',
                name: 'SystemOrgIntro',
                redirect: '/system/site-config?tab=profile',
                meta: { title: '机构介绍', roles: ['ADMIN'], hidden: true }
              },
              {
                path: 'news',
                name: 'SystemOrgNews',
                redirect: '/system/site-config?tab=profile',
                meta: { title: '机构动态', roles: ['ADMIN'], hidden: true }
              },
              {
                path: 'life',
                name: 'SystemLifeEntertainment',
                redirect: '/system/site-config?tab=profile',
                meta: { title: '生活娱乐', roles: ['ADMIN'], hidden: true }
              }
            ]
          },
          {
            path: 'role',
            name: 'SystemRoleManage',
            component: () => import('../views/System/RoleManage.vue'),
            meta: { title: '角色管理', roles: ['ADMIN', 'HR_MINISTER'] }
          },
          {
            path: 'department',
            name: 'SystemDepartmentManage',
            component: () => import('../views/System/DepartmentManage.vue'),
            meta: { title: '部门管理', roles: ['ADMIN'] }
          },
          {
            path: 'app-version',
            name: 'SystemAppVersion',
            redirect: '/system/site-config?tab=profile',
            meta: { title: 'APP版本管理（已下沉）', roles: ['ADMIN'], hidden: true }
          },
          {
            path: 'dict',
            name: 'SystemDictionary',
            redirect: '/base-config/elder-type',
            meta: { title: '系统字典（并入基础数据配置）', roles: ['ADMIN'], hidden: true }
          },
          {
            path: 'message',
            name: 'SystemMessageManage',
            redirect: '/system/site-config?tab=consult',
            meta: { title: '留言管理', roles: ['ADMIN'], hidden: true }
          },
          {
            path: 'staff',
            name: 'SystemStaff',
            redirect: '/hr/profile/account-access',
            meta: { title: '员工管理（兼容）', roles: ['ADMIN'], hidden: true }
          },
          {
            path: 'menu-preview',
            name: 'SystemMenuPreview',
            component: () => import('../views/System/MenuPreview.vue'),
            meta: { title: '菜单预览', roles: ['ADMIN'], hidden: true }
          },
          {
            path: 'permission-overview',
            name: 'SystemPermissionOverview',
            component: () => import('../views/System/PermissionOverview.vue'),
            meta: { title: '权限总览', roles: ['DIRECTOR', 'SYS_ADMIN', 'ADMIN', 'HR_MINISTER'] }
          }
        ]
      },
      {
        path: 'demo',
        name: 'Demo',
        meta: { title: '能力演示', icon: 'ExperimentOutlined', hidden: true },
        children: [
          { path: 'vxe-table', name: 'DemoVxe', component: () => import('../views/demo/VxeTableDemo.vue'), meta: { title: '高级表格' } },
          { path: 'calendar', name: 'DemoCalendar', component: () => import('../views/demo/CalendarDemo.vue'), meta: { title: '排班日历' } },
          { path: 'charts', name: 'DemoCharts', component: () => import('../views/demo/ChartsDemo.vue'), meta: { title: '报表图表' } },
          { path: 'editor', name: 'DemoEditor', component: () => import('../views/demo/EditorDemo.vue'), meta: { title: '富文本编辑' } },
          { path: 'flow', name: 'DemoFlow', component: () => import('../views/demo/FlowDemo.vue'), meta: { title: '流程图' } },
          { path: 'antdx-chat', name: 'DemoAntdx', component: () => import('../views/demo/AntdxChatDemo.vue'), meta: { title: '智能助手' } }
        ]
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { title: '未找到', hidden: true }
  }
]
