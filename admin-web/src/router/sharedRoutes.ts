import type { RouteRecordRaw } from 'vue-router'
import { DEPARTMENT_MINISTER_ROLES, ROLE_CODES } from '../access/policy'

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
        meta: { title: '全部功能', icon: 'ApartmentOutlined', navSection: 'entry', navOrder: 15, navPinned: true }
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
            path: 'attendance',
            name: 'WorkbenchAttendance',
            component: () => import('../views/oa/AttendanceLeave.vue'),
            meta: { title: '我的考勤与请假' }
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
            meta: { title: '我的总结' }
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
            component: () => import('../views/elder/resident360/Resident360.vue'),
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
