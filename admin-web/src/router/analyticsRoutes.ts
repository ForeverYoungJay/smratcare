import type { RouteRecordRaw } from 'vue-router'
import { DEPARTMENT_ALL_ROLES, ROLE_CODES } from '../access/policy'

export const analyticsRoutes: RouteRecordRaw[] = [
  {
        path: 'stats',
        name: 'Stats',
        meta: {
          title: '统计分析', icon: 'BarChartOutlined', navSection: 'operations', navOrder: 70, navPinned: true,
          roles: [...DEPARTMENT_ALL_ROLES, ROLE_CODES.DIRECTOR, ROLE_CODES.ADMIN, ROLE_CODES.SYS_ADMIN]
        },
        children: [
          {
            path: '',
            name: 'StatsHub',
            component: () => import('../views/ModuleHub.vue'),
            meta: { title: '统计导航', hidden: true }
          },
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
            path: 'operations',
            name: 'StatsOperationsDashboard',
            component: () => import('../views/operations/OperationsDashboard.vue'),
            meta: { title: '管理驾驶舱' }
          },
          {
            path: 'executive-cockpit',
            name: 'StatsExecutiveCockpit',
            component: () => import('../views/cockpit/ExecutiveCockpit.vue'),
            meta: {
              title: '经营驾驶舱',
              roles: ['DIRECTOR', 'SYS_ADMIN', 'ADMIN', 'MEDICAL_MINISTER', 'NURSING_MINISTER', 'FINANCE_MINISTER', 'LOGISTICS_MINISTER', 'MARKETING_MINISTER', 'HR_MINISTER']
            }
          },
          {
            path: 'executive-bi-screen',
            name: 'StatsExecutiveBiScreen',
            component: () => import('../views/cockpit/ExecutiveBiScreen.vue'),
            meta: {
              title: '经营驾驶舱大屏',
              roles: ['DIRECTOR', 'SYS_ADMIN']
            }
          },
          {
            path: 'sensitive-access-audit',
            name: 'StatsSensitiveAccessAudit',
            component: () => import('../views/compliance/SensitiveAccessAudit.vue'),
            meta: {
              title: '敏感数据审计',
              roles: ['SYS_ADMIN', 'DIRECTOR', 'ADMIN']
            }
          },
          {
            path: 'security-policy',
            name: 'StatsSecurityPolicy',
            component: () => import('../views/compliance/SecurityPolicyConfig.vue'),
            meta: {
              title: '安全策略配置',
              roles: ['SYS_ADMIN']
            }
          },
          {
            path: 'export-audit',
            name: 'StatsExportAudit',
            component: () => import('../views/compliance/ExportAudit.vue'),
            meta: {
              title: '导出审计',
              roles: ['SYS_ADMIN', 'DIRECTOR', 'ADMIN']
            }
          },
          {
            path: 'service-compliance',
            name: 'StatsServiceCompliance',
            component: () => import('../views/operations/ServiceComplianceCenter.vue'),
            meta: { title: '服务质量与合规', hidden: true }
          },
          {
            path: 'safety-risk',
            name: 'StatsSafetyRisk',
            component: () => import('../views/operations/SafetyRiskCenter.vue'),
            meta: { title: '安全风险', hidden: true }
          },
          {
            path: 'workforce',
            name: 'StatsWorkforce',
            component: () => import('../views/operations/WorkforceCenter.vue'),
            meta: { title: '员工人力', hidden: true }
          },
          {
            path: 'staff-mobile',
            name: 'StatsStaffMobile',
            component: () => import('../views/operations/StaffMobileCenter.vue'),
            meta: { title: '员工移动端', hidden: true }
          },
          {
            path: 'staff-mobile-ledger',
            name: 'StatsStaffMobileLedger',
            component: () => import('../views/operations/StaffMobileLedgerCenter.vue'),
            meta: { title: '员工现场执行台账', hidden: true }
          },
          {
            path: 'logistics',
            name: 'StatsLogistics',
            component: () => import('../views/operations/LogisticsCenter.vue'),
            meta: { title: '后勤保障', hidden: true }
          },
          {
            path: 'marketing',
            name: 'StatsMarketing',
            component: () => import('../views/operations/MarketingCenter.vue'),
            meta: { title: '营销转化', hidden: true }
          },
          {
            path: 'intelligence',
            name: 'StatsIntelligence',
            component: () => import('../views/operations/IntelligenceCenter.vue'),
            meta: { title: '智能运营', hidden: true }
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
        path: 'ai',
        name: 'AiCenter',
        meta: {
          title: 'AI 智能提效',
          icon: 'RobotOutlined',
          navSection: 'operations',
          navOrder: 72,
          navPinned: true,
          roles: ['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_EMPLOYEE', 'NURSING_MINISTER', 'HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']
        },
        redirect: '/ai/schedule',
        children: [
          {
            path: 'schedule',
            name: 'AiScheduleWorkbench',
            component: () => import('../views/ai/SmartScheduleWorkbench.vue'),
            meta: { title: '智能排班工作台', roles: ['NURSING_MINISTER', 'HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          },
          {
            path: 'risk',
            name: 'AiRiskBoard',
            component: () => import('../views/ai/RiskPredictionBoard.vue'),
            meta: { title: '健康风险预测看板', roles: ['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_EMPLOYEE', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          }
        ]
      }
]
