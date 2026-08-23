import type { RouteRecordRaw } from 'vue-router'
import { redirectPreservingLocation } from './routeAliases'

export const administrationRoutes: RouteRecordRaw[] = [
  {
        path: 'oa',
        name: 'OA',
        meta: {
          title: '行政管理',
          icon: 'ApartmentOutlined',
          navSection: 'support',
          navOrder: 100,
          navPinned: true,
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
            path: 'family-service',
            name: 'OaFamilyService',
            component: () => import('../views/operations/FamilyServiceCenter.vue'),
            meta: { title: '家属服务中心' }
          },
          {
            path: 'family-users',
            name: 'OaFamilyUsers',
            component: () => import('../views/elder/Family.vue'),
            meta: { title: '家属账号与绑定' }
          },
          {
            path: 'family-service-health',
            name: 'OaFamilyServiceHealth',
            component: () => import('../views/operations/FamilyServiceHealthCenter.vue'),
            meta: { title: '家属服务健康中心', hidden: true }
          },
          {
            path: 'family-recharge',
            name: 'OaFamilyRechargeLedger',
            component: () => import('../views/operations/FamilyRechargeLedgerCenter.vue'),
            meta: { title: '家属充值台账', hidden: true }
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
                path: 'family-communication',
                name: 'OaFamilyCommunication',
                component: () => import('../views/oa/FamilyCommunication.vue'),
                meta: { title: '家属反馈与沟通' }
              },
              {
                path: 'calendar',
                name: 'OaCalendar',
                redirect: redirectPreservingLocation('/workbench/schedule'),
                meta: { title: '行政日历 / 协同日历', hidden: true, legacy: true, searchable: false }
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
                redirect: redirectPreservingLocation('/oa/activity-center/survey-manage'),
                meta: { title: '问卷管理（兼容）', hidden: true, legacy: true, searchable: false }
              },
              {
                path: 'stats',
                name: 'OaSurveyStats',
                redirect: redirectPreservingLocation('/oa/activity-center/survey-stats'),
                meta: { title: '问卷统计（兼容）', hidden: true, legacy: true, searchable: false }
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
            redirect: redirectPreservingLocation('/oa/activity-center/records'),
            meta: { title: '活动管理（兼容）', hidden: true, legacy: true, searchable: false }
          },
          {
            path: 'activity-plan',
            name: 'OaActivityPlan',
            redirect: redirectPreservingLocation('/oa/activity-center/plan'),
            meta: { title: '活动计划（兼容）', hidden: true, legacy: true, searchable: false }
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
            redirect: redirectPreservingLocation('/workbench'),
            meta: { title: '门户与待办（兼容）', hidden: true, legacy: true, searchable: false }
          },
          {
            path: 'my-info',
            name: 'OaMyInfo',
            redirect: redirectPreservingLocation('/workbench/my-info'),
            meta: { title: '我的信息（兼容）', hidden: true, legacy: true, searchable: false }
          },
          {
            path: 'todo',
            name: 'OaTodo',
            redirect: redirectPreservingLocation('/workbench/todo'),
            meta: { title: '待办事项（兼容）', hidden: true, legacy: true, searchable: false }
          },
          {
            path: 'approval',
            name: 'OaApproval',
            component: () => import('../views/oa/Approval.vue'),
            meta: { title: '审批中心' }
          },
          {
            path: 'staff-collaboration',
            name: 'OaStaffCollaboration',
            component: () => import('../views/oa/StaffCollaborationCenter.vue'),
            meta: { title: '员工协同中心', hidden: true }
          },
          {
            path: 'employee-suggestions',
            name: 'OaEmployeeSuggestions',
            component: () => import('../views/oa/EmployeeSuggestionCenter.vue'),
            meta: { title: '员工建议中心', hidden: true }
          },
          {
            path: 'attendance-leave',
            name: 'OaAttendanceLeave',
            redirect: redirectPreservingLocation('/workbench/attendance'),
            meta: { title: '考勤与请假（兼容）', hidden: true, legacy: true, searchable: false }
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
            redirect: redirectPreservingLocation('/workbench/reports'),
            meta: { title: '工作总结（兼容）', hidden: true, legacy: true, searchable: false }
          }
        ]
      }
]
