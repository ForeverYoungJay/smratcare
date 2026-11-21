import type { RouteRecordRaw } from 'vue-router'

/**
 * 护理部路由：菜单只保留 5 个主页面（工作台 / 任务中心 / 档案与评估 / 交接班与安全 / 排班与质控）。
 * 任务执行、异常上报、巡视、排班调整都在主页面的标签、抽屉里完成；
 * 原来的单功能页隐藏菜单但保留地址，被主页面嵌入或从链接直达。
 */
const NURSING_MANAGERS = ['NURSING_MINISTER', 'DIRECTOR', 'ADMIN']

/** 旧入口跳到新主页面对应标签，保留原来的筛选参数 */
function moved(path: string, target: string, tab?: string): RouteRecordRaw {
  return { path, redirect: (to) => ({ path: target, query: tab ? { ...to.query, tab } : to.query }), meta: { hidden: true } }
}

export const nursingRoutes: RouteRecordRaw[] = [
  {
        path: 'care',
        name: 'Care',
        meta: { title: '护理部业务', icon: 'ScheduleOutlined', navSection: 'care', navOrder: 35, navPinned: true, roles: ['NURSING_EMPLOYEE', 'NURSING_MINISTER', 'DIRECTOR', 'ADMIN'] },
        redirect: '/care/workbench',
        children: [
          { path: 'workbench', name: 'CareWorkbench', component: () => import('../views/care/center/NursingWorkbench.vue'), meta: { title: '护理工作台' } },
          { path: 'tasks', name: 'CareTaskCenter', component: () => import('../views/care/center/TaskCenter.vue'), meta: { title: '护理任务中心' } },
          { path: 'residents', name: 'CareResidents', component: () => import('../views/care/center/ResidentCareList.vue'), meta: { title: '护理档案与评估' } },
          { path: 'residents/:id', name: 'CareResidentDetail', component: () => import('../views/care/center/ResidentCare360.vue'), meta: { title: '长者护理档案', hidden: true } },
          { path: 'safety', name: 'CareSafety', component: () => import('../views/care/center/SafetyCenter.vue'), meta: { title: '交接班与安全' } },
          { path: 'operations', name: 'CareOperations', component: () => import('../views/care/center/NursingOperations.vue'), meta: { title: '排班与质控' } },
          moved('home', '/care/workbench'),
          {
            path: 'staff',
            name: 'CareStaff',
            meta: { title: '人员管理', hidden: true },
            redirect: '/care/staff/caregiver-info',
            children: [
              {
                path: 'caregiver-info',
                name: 'CaregiverInfo',
                component: () => import('../views/hr/Staff.vue'),
                props: { title: '护工信息', subTitle: '照护团队成员档案与用工信息' },
                meta: { title: '护工信息', hidden: true }
              },
              {
                path: 'caregiver-groups',
                name: 'CaregiverGroups',
                component: () => import('../views/care/CaregiverGroups.vue'),
                meta: { title: '护工小组', hidden: true }
              }
            ]
          },
          {
            path: 'service',
            name: 'CareService',
            meta: { title: '服务管理', hidden: true },
            redirect: '/care/service/service-items',
            children: [
              {
                path: 'service-items',
                name: 'CareServiceItems',
                component: () => import('../views/care/ServiceItems.vue'),
                meta: { title: '服务项目', hidden: true }
              },
              {
                path: 'care-levels',
                name: 'CareLevels',
                component: () => import('../views/care/CareLevels.vue'),
                meta: { title: '护理等级', hidden: true, roles: NURSING_MANAGERS }
              },
              {
                path: 'service-plans',
                name: 'ServicePlans',
                component: () => import('../views/care/ServicePlans.vue'),
                meta: { title: '服务计划', hidden: true, roles: NURSING_MANAGERS }
              },
              {
                path: 'nursing-reports',
                name: 'CareNursingReports',
                component: () => import('../views/care/NursingReports.vue'),
                meta: { title: '护理报表', hidden: true }
              },
              {
                path: 'nursing-records',
                name: 'CareNursingRecords',
                component: () => import('../views/care/NursingRecords.vue'),
                meta: { title: '护理记录', hidden: true }
              },
              {
                path: 'service-bookings',
                name: 'ServiceBookings',
                component: () => import('../views/care/ServiceBookings.vue'),
                meta: { title: '服务预定', hidden: true }
              }
            ]
          },
          {
            path: 'scheduling',
            name: 'CareScheduling',
            meta: { title: '排班管理', hidden: true },
            redirect: '/care/scheduling/shift-templates',
            children: [
              {
                path: 'shift-templates',
                name: 'ShiftTemplates',
                component: () => import('../views/care/ShiftTemplates.vue'),
                meta: { title: '排班方案', hidden: true }
              },
              {
                path: 'shift-calendar',
                name: 'CareShiftCalendar',
                component: () => import('../views/care/ShiftCalendar.vue'),
                meta: { title: '排班', hidden: true }
              },
              {
                path: 'handovers',
                name: 'ShiftHandovers',
                redirect: (to) => ({ path: '/medical-care/handovers', query: to.query }),
                meta: { title: '交接班', hidden: true }
              }
            ]
          },
          {
            path: 'dashboard',
            name: 'CareDashboard',
            component: () => import('../views/care/Dashboard.vue'),
            meta: { title: '护理看板', hidden: true }
          },
          moved('today', '/care/tasks', 'today'),
          {
            path: 'template',
            name: 'CareTemplate',
            component: () => import('../views/care/Template.vue'),
            meta: { title: '护理模板', hidden: true }
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
            meta: { title: '老人套餐', hidden: true, roles: ['NURSING_MINISTER', 'DIRECTOR', 'ADMIN'] }
          },
          {
            path: 'task-generate',
            name: 'CareTaskGenerate',
            component: () => import('../views/care/TaskGenerate.vue'),
            meta: { title: '套餐任务生成', hidden: true }
          },
          moved('exception', '/care/safety', 'exception'),
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
            meta: { title: '长者护理计划', hidden: true }
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
      }
]
