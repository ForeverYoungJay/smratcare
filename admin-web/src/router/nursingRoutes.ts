import type { RouteRecordRaw } from 'vue-router'

export const nursingRoutes: RouteRecordRaw[] = [
  {
        path: 'care',
        name: 'Care',
        meta: { title: '护理部业务', icon: 'ScheduleOutlined', navSection: 'care', navOrder: 35, navPinned: true, roles: ['NURSING_EMPLOYEE', 'NURSING_MINISTER', 'DIRECTOR', 'ADMIN'] },
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
            meta: { title: '异常任务' }
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
            meta: { title: '长者护理计划' }
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
