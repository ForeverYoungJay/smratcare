import type { RouteRecordRaw } from 'vue-router'
import { DEPARTMENT_MINISTER_ROLES, ROLE_CODES } from '../access/policy'
import { legacyResidentRedirect } from './residentRedirects'

export const sharedRoutes: RouteRecordRaw[] = [
  {
        path: 'portal',
        name: 'Portal',
        component: () => import('../views/Portal.vue'),
        meta: { title: '经营总览', icon: 'HomeOutlined', navSection: 'entry', navOrder: 10, navPinned: true, roles: [ROLE_CODES.DIRECTOR, ROLE_CODES.ADMIN] }
      },
  {
        path: 'function-map',
        name: 'FunctionMap',
        component: () => import('../views/FunctionMap.vue'),
        meta: { title: '全部功能', icon: 'ApartmentOutlined', navSection: 'entry', navOrder: 15, navPinned: true, publicForAuthenticated: true }
      },
  {
        path: 'workbench',
        name: 'Workbench',
        component: () => import('../layouts/RouteView.vue'),
        meta: { title: '工作台', icon: 'AppstoreOutlined', navSection: 'entry', navOrder: 20, navPinned: true },
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
            component: () => import('../views/oa/Todo.vue'),
            meta: { title: '我的待办' }
          },
          {
            path: 'my-info',
            name: 'WorkbenchMyInfo',
            component: () => import('../views/oa/MyInfo.vue'),
            meta: { title: '我的信息' }
          },
          {
            // 协同消息与聊天待办（?tab=tasks）；顶栏「快捷聊天 / 聊天待办」都进这里
            path: 'chat',
            name: 'WorkbenchChat',
            component: () => import('../views/chat/ChatWorkspace.vue'),
            // 聊天对所有登录员工开放，不受角色页面清单限制
            meta: { title: '协同消息', publicForAuthenticated: true }
          },
          {
            path: 'payslips',
            name: 'WorkbenchPayslips',
            component: () => import('../views/hr/MyHrSelf.vue'),
            meta: { title: '我的工资与积分' }
          },
          {
            path: 'attendance',
            name: 'WorkbenchAttendance',
            component: () => import('../views/oa/AttendanceLeave.vue'),
            meta: { title: '考勤与请假' }
          },
          {
            path: 'schedule',
            name: 'WorkbenchSchedule',
            component: () => import('../views/oa/Calendar.vue'),
            meta: { title: '我的日程' }
          },
          {
            path: 'reports',
            name: 'WorkbenchReports',
            component: () => import('../views/oa/WorkReport.vue'),
            meta: { title: '工作总结' }
          },
          {
            path: 'approvals',
            name: 'WorkbenchApprovals',
            component: () => import('../views/oa/Approval.vue'),
            meta: { title: '待我审批', roles: [...DEPARTMENT_MINISTER_ROLES, ROLE_CODES.DIRECTOR, ROLE_CODES.ADMIN] }
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
        meta: { title: '长者管理', icon: 'TeamOutlined', navSection: 'care', navOrder: 30, navPinned: true },
        children: [
          {
            path: '',
            name: 'ElderHub',
            component: () => import('../views/ModuleHub.vue'),
            meta: { title: '长者导航', hidden: true }
          },
          {
            path: 'resident-360',
            name: 'ElderResident360',
            // 一人一档：带长者 ID 时进一体化详情，否则进长者工作台
            redirect: (to) => to.query.residentId
              ? { path: `/elder/detail/${String(to.query.residentId)}`, query: { ...to.query, residentId: undefined } }
              : { path: '/elder/in-hospital-overview' },
            meta: { title: '长者总览', hidden: true }
          },
          {
            path: 'in-hospital-overview',
            name: 'ElderInHospitalOverview',
            component: () => import('../views/elder/resident360/InHospitalOverview.vue'),
            meta: { title: '长者工作台' }
          },
          {
            path: 'bed-panorama',
            name: 'ElderBedPanorama',
            component: () => import('../views/elder/resident360/BedPanorama.vue'),
            meta: { title: '床位与状态管理' }
          },
          {
            path: 'list',
            name: 'ElderList',
            component: () => import('../views/elder/List.vue'),
            meta: { title: '长者列表' }
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
            redirect: (to) => legacyResidentRedirect('contracts-invoices', to.query),
            meta: { title: '合同与票据', hidden: true }
          },
          {
            path: 'admission-processing',
            name: 'ElderAdmissionProcessing',
            component: () => import('../views/elder/Admission.vue'),
            meta: { title: '入住办理中心' }
          },
          {
            path: 'status-change',
            name: 'ElderStatusChangeCenter',
            component: () => import('../layouts/RouteView.vue'),
            meta: { title: '入住状态变更' },
            redirect: '/elder/bed-panorama',
            children: [
              {
                path: 'center',
                name: 'ElderStatusChangeOverview',
                redirect: (to) => legacyResidentRedirect('center', to.query),
                meta: { title: '变更总览', hidden: true }
              },
              {
                path: 'outing',
                name: 'ElderStatusChangeOuting',
                redirect: (to) => legacyResidentRedirect('outing', to.query),
                meta: { title: '外出登记', hidden: true }
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
                redirect: (to) => legacyResidentRedirect('discharge-apply', to.query),
                meta: { title: '退住申请', hidden: true }
              },
              {
                path: 'trial-stay',
                name: 'ElderStatusChangeTrialStay',
                redirect: (to) => legacyResidentRedirect('trial-stay', to.query),
                meta: { title: '试住登记', hidden: true }
              },
              {
                path: 'medical-outing',
                name: 'ElderStatusChangeMedicalOuting',
                redirect: (to) => legacyResidentRedirect('medical-outing', to.query),
                meta: {
                  hidden: true,
                  title: '外出就医登记',
                  roles: ['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_EMPLOYEE', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']
                }
              },
              {
                path: 'death-register',
                name: 'ElderStatusChangeDeathRegister',
                redirect: (to) => legacyResidentRedirect('death-register', to.query),
                meta: {
                  hidden: true,
                  title: '死亡登记',
                  roles: ['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_EMPLOYEE', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']
                }
              },
              {
                path: 'discharge-settlement',
                name: 'ElderStatusChangeDischargeSettlement',
                component: () => import('../views/elder/resident360/DischargeSettlement.vue'),
                meta: { title: '费用与退住' }
              }
            ]
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
            meta: { title: '长者一体化详情', hidden: true }
          },
          {
            path: 'crm',
            name: 'ElderCrm',
            component: () => import('../views/elder/Crm.vue'),
            meta: { title: 'CRM线索(旧)', hidden: true }
          },
          {
            path: 'incident',
            name: 'ElderIncident',
            component: () => import('../views/life/Incident.vue'),
            meta: { title: '事故登记', hidden: true }
          },
          {
            path: 'discharge',
            name: 'ElderDischarge',
            component: () => import('../views/elder/Discharge.vue'),
            meta: { title: '退住登记', hidden: true }
          },
          {
            path: 'change-log',
            name: 'ElderChangeLog',
            component: () => import('../views/elder/ChangeLog.vue'),
            meta: { title: '变更记录', hidden: true }
          }
        ]
      }
]
