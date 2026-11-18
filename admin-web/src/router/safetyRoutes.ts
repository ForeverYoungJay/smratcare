import type { RouteRecordRaw } from 'vue-router'
import { FIRE_CENTERS } from '../views/fire/center/fireModel'

/**
 * 消防安全管理：菜单只保留 5 个主页面，原 9 个功能页保留路由（菜单隐藏），作为主页面的标签内容嵌入，
 * 权限仍以各自路由 meta.roles 为准（如视频只给后勤与管理层）。
 */
function fireCenter(key: string, name: string): RouteRecordRaw {
  const config = FIRE_CENTERS[key]
  return { path: key, name, component: () => import('../views/medical/center/TabbedCenter.vue'), props: config, meta: { title: config.title } }
}

export const safetyRoutes: RouteRecordRaw[] = [
  {
        path: 'fire',
        name: 'FireSafety',
        meta: {
          title: '消防安全管理',
          icon: 'SafetyOutlined',
          navSection: 'compliance',
          navOrder: 90,
          navPinned: true,
          roles: ['GUARD', 'LOGISTICS_EMPLOYEE', 'LOGISTICS_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']
        },
        children: [
          {
            path: '',
            name: 'FireSafetyHub',
            redirect: '/fire/overview',
            meta: { title: '消防导航', hidden: true }
          },
          {
            path: 'overview',
            name: 'FireOverview',
            component: () => import('../views/fire/center/FireWorkbench.vue'),
            meta: { title: '消防安全工作台' }
          },
          fireCenter('inspection', 'FireInspection'),
          fireCenter('facilities', 'FireFacilities'),
          fireCenter('control-center', 'FireControlCenter'),
          fireCenter('safety', 'FireSafetyAnalysis'),
          {
            path: 'settings',
            name: 'FireSettings',
            component: () => import('../views/fire/center/FireSettings.vue'),
            meta: { title: '消防系统配置', roles: ['LOGISTICS_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          },
          {
            path: 'video-wall',
            name: 'FireVideoWall',
            component: () => import('../views/fire/center/FireVideoWall.vue'),
            meta: { title: '视频画面', hidden: true, roles: ['LOGISTICS_EMPLOYEE', 'LOGISTICS_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          },
          {
            path: 'video-access',
            name: 'FireVideoAccess',
            component: () => import('../views/fire/center/FireVideoAccess.vue'),
            meta: { title: '录像调阅', hidden: true, roles: ['LOGISTICS_EMPLOYEE', 'LOGISTICS_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          },
          {
            path: 'handover',
            name: 'FireHandover',
            component: () => import('../views/fire/center/FireHandover.vue'),
            meta: { title: '值班交接', hidden: true }
          },
          {
            path: 'report-signing',
            name: 'FireReportSigning',
            component: () => import('../views/fire/center/FireReportSign.vue'),
            meta: { title: '维保报告签署', hidden: true }
          },
          {
            path: 'drills',
            name: 'FireDrills',
            component: () => import('../views/fire/center/FireDrills.vue'),
            meta: { title: '培训与演练', hidden: true }
          },
          {
            path: 'hazards',
            name: 'FireHazards',
            component: () => import('../views/fire/center/FireHazards.vue'),
            meta: { title: '隐患整改与应急', hidden: true }
          },
          {
            path: 'video',
            name: 'FireVideoManagement',
            component: () => import('../views/fire/VideoManagement.vue'),
            meta: { title: '摄像头录像管理', hidden: true, roles: ['LOGISTICS_EMPLOYEE', 'LOGISTICS_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          },
          {
            path: 'facility-management',
            name: 'FireFacilityManagement',
            component: () => import('../views/fire/FacilityManagement.vue'),
            meta: { title: '消防设施管理', hidden: true }
          },
          {
            path: 'control-room-duty',
            name: 'FireControlRoomDuty',
            component: () => import('../views/fire/ControlRoomDuty.vue'),
            meta: { title: '控制室值班记录', hidden: true }
          },
          {
            path: 'monthly-check',
            name: 'FireMonthlyCheck',
            component: () => import('../views/fire/MonthlyCheck.vue'),
            meta: { title: '每月防火检查', hidden: true }
          },
          {
            path: 'day-patrol',
            name: 'FireDayPatrol',
            component: () => import('../views/fire/DayPatrol.vue'),
            meta: { title: '日间防火巡查', hidden: true }
          },
          {
            path: 'night-patrol',
            name: 'FireNightPatrol',
            component: () => import('../views/fire/NightPatrol.vue'),
            meta: { title: '夜间防火巡查', hidden: true }
          },
          {
            path: 'maintenance-report',
            name: 'FireMaintenanceReport',
            component: () => import('../views/fire/MaintenanceReport.vue'),
            meta: { title: '消防设施维护保养报告', hidden: true }
          },
          {
            path: 'fault-maintenance',
            name: 'FireFaultMaintenance',
            component: () => import('../views/fire/FaultMaintenance.vue'),
            meta: { title: '建筑消防设施故障维护', hidden: true }
          },
          {
            path: 'data-stats',
            name: 'FireDataStats',
            component: () => import('../views/fire/DataStats.vue'),
            meta: { title: '数据报表统计', hidden: true }
          }
        ]
      },
  {
        path: 'ltci',
        name: 'Ltci',
        meta: {
          title: '长护险与监管',
          icon: 'SafetyCertificateOutlined',
          navSection: 'compliance',
          navOrder: 85,
          navPinned: true,
          roles: ['NURSING_EMPLOYEE', 'NURSING_MINISTER', 'MEDICAL_MINISTER', 'FINANCE_EMPLOYEE', 'FINANCE_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']
        },
        children: [
          {
            path: '',
            name: 'LtciHub',
            component: () => import('../views/ModuleHub.vue'),
            meta: { title: '长护险导航', hidden: true }
          },
          {
            path: 'assessments',
            name: 'LtciAssessments',
            component: () => import('../views/ltci/AssessmentRecords.vue'),
            meta: {
              title: '失能评估',
              roles: ['NURSING_EMPLOYEE', 'NURSING_MINISTER', 'MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']
            }
          },
          {
            path: 'benefits',
            name: 'LtciBenefits',
            component: () => import('../views/ltci/BenefitManage.vue'),
            meta: {
              title: '待遇管理',
              roles: ['NURSING_MINISTER', 'MEDICAL_MINISTER', 'FINANCE_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']
            }
          },
          {
            path: 'settlements',
            name: 'LtciSettlements',
            component: () => import('../views/ltci/Settlement.vue'),
            meta: {
              title: '结算管理',
              roles: ['FINANCE_EMPLOYEE', 'FINANCE_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']
            }
          },
          {
            path: 'gov-report',
            name: 'GovReportTasks',
            component: () => import('../views/govreport/ReportTasks.vue'),
            meta: {
              title: '监管上报',
              roles: ['FINANCE_MINISTER', 'MEDICAL_MINISTER', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']
            }
          },
          {
            path: 'gov-channels',
            name: 'GovReportChannels',
            component: () => import('../views/govreport/ChannelConfig.vue'),
            meta: {
              title: '上报渠道',
              roles: ['DIRECTOR', 'SYS_ADMIN', 'ADMIN']
            }
          },
          {
            path: 'medins-sheets',
            name: 'MedinsSheets',
            component: () => import('../views/medins/SettlementSheet.vue'),
            meta: {
              title: '医保结算清单',
              roles: ['FINANCE_EMPLOYEE', 'FINANCE_MINISTER', 'MEDICAL_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']
            }
          },
          {
            path: 'medins-evouchers',
            name: 'MedinsEvouchers',
            component: () => import('../views/medins/Evoucher.vue'),
            meta: {
              title: '医保电子凭证',
              roles: ['MEDICAL_MINISTER', 'NURSING_MINISTER', 'FINANCE_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']
            }
          },
          {
            path: 'medins-channels',
            name: 'MedinsChannels',
            component: () => import('../views/medins/ChannelConfig.vue'),
            meta: {
              title: '医保渠道配置',
              roles: ['DIRECTOR', 'SYS_ADMIN', 'ADMIN']
            }
          }
        ]
      }
]
