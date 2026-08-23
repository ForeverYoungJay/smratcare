import type { RouteRecordRaw } from 'vue-router'
import { redirectPreservingLocation } from './routeAliases'

export const hrRoutes: RouteRecordRaw[] = [
  {
        path: 'hr',
        name: 'Hr',
        meta: { title: '人力资源', icon: 'TeamOutlined', navSection: 'support', navOrder: 110, navPinned: true, roles: ['HR_EMPLOYEE', 'HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
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
            redirect: redirectPreservingLocation('/hr/overview'),
            meta: { title: '人事行政工作台（兼容）', roles: ['HR_EMPLOYEE', 'HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true, legacy: true, searchable: false }
          },
          {
            path: 'recruitment',
            name: 'HrRecruitment',
            meta: { title: '招聘管理', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
            redirect: '/hr/recruitment/needs',
            children: [
              {
                path: 'needs',
                name: 'HrRecruitmentNeeds',
                component: () => import('../views/hr/HrRecruitmentNeeds.vue'),
                meta: { title: '招聘办理台', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              },
              {
                path: 'job-posting',
                name: 'HrRecruitmentJobPosting',
                redirect: '/hr/recruitment/needs?scene=job-posting',
                meta: { title: '岗位发布（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true }
              },
              {
                path: 'candidates',
                name: 'HrRecruitmentCandidates',
                redirect: '/hr/recruitment/needs?scene=candidates',
                meta: { title: '候选人库（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true }
              },
              {
                path: 'interviews',
                name: 'HrRecruitmentInterviews',
                redirect: '/hr/recruitment/needs?scene=interviews',
                meta: { title: '面试管理（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true }
              },
              {
                path: 'onboarding',
                name: 'HrRecruitmentOnboarding',
                redirect: '/hr/recruitment/needs?scene=onboarding',
                meta: { title: '入职办理（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true }
              },
              {
                path: 'materials',
                name: 'HrRecruitmentMaterials',
                redirect: '/hr/recruitment/needs?scene=materials',
                meta: { title: '入职资料收集（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true }
              },
              {
                path: 'offboarding',
                name: 'HrRecruitmentOffboarding',
                redirect: '/hr/recruitment/needs?scene=offboarding',
                meta: { title: '退职办理（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true }
              }
            ]
          },
          {
            path: 'profile',
            name: 'HrProfileCenter',
            meta: { title: '员工档案', roles: ['HR_EMPLOYEE', 'HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
            redirect: '/hr/profile/social-security-reminders',
            children: [
              {
                path: 'onboarding',
                name: 'HrProfileOnboarding',
                component: () => import('../views/hr/HrOnboardingWizard.vue'),
                meta: { title: '入职向导', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              },
              {
                path: 'basic',
                name: 'HrProfileBasic',
                component: () => import('../views/hr/Staff.vue'),
                props: { title: '员工基本信息', subTitle: '员工档案中心 / 员工基本信息' },
                meta: { title: '员工基本信息', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              },
              {
                path: 'contracts',
                name: 'HrProfileContracts',
                component: () => import('../views/hr/HrProfileContracts.vue'),
                meta: { title: '劳动合同管理', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              },
              {
                path: 'account-access',
                name: 'HrProfileAccountAccess',
                component: () => import('../views/Staff.vue'),
                props: { title: '账号与领导设置', subTitle: '人事创建账号、初始化密码与上下级监管关系' },
                meta: { title: '账号与领导设置', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              },
              {
                path: 'contract-templates',
                name: 'HrProfileContractTemplates',
                component: () => import('../views/hr/HrProfileTemplates.vue'),
                meta: { title: '合同模板库', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              },
              {
                path: 'contract-print',
                name: 'HrProfileContractPrint',
                component: () => import('../views/hr/HrContractPrint.vue'),
                meta: { title: '合同打印', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              },
              {
                path: 'certificates',
                name: 'HrProfileCertificates',
                component: () => import('../views/hr/HrProfileCertificates.vue'),
                meta: { title: '证书上传', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              },
              {
                path: 'training-records',
                name: 'HrProfileTrainingRecords',
                redirect: '/hr/development/records?scene=records',
                meta: { title: '培训记录（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true }
              },
              {
                path: 'attachments',
                name: 'HrProfileAttachments',
                component: () => import('../views/hr/HrProfileAttachments.vue'),
                meta: { title: '档案附件', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              },
              {
                path: 'contract-reminders',
                name: 'HrProfileContractReminders',
                component: () => import('../views/hr/HrContractReminder.vue'),
                meta: { title: '合同到期提醒', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
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
            meta: { title: '考勤与班组', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
            redirect: '/hr/attendance/schemes',
            children: [
              {
                path: 'schemes',
                name: 'HrAttendanceSchemes',
                component: () => import('../views/hr/HrAttendanceSchemes.vue'),
                meta: { title: '排班管理', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              },
              {
                path: 'groups',
                name: 'HrAttendanceGroups',
                component: () => import('../views/care/CaregiverGroups.vue'),
                meta: { title: '班组设置', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              },
              {
                path: 'calendar',
                name: 'HrAttendanceCalendar',
                component: () => import('../views/care/ShiftCalendar.vue'),
                meta: { title: '排班日历', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              },
              {
                path: 'leave-approval',
                name: 'HrAttendanceLeaveApproval',
                component: () => import('../views/hr/HrAttendanceApprovalPage.vue'),
                meta: { title: '请假审批', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              },
              {
                path: 'shift-change',
                name: 'HrAttendanceShiftChange',
                redirect: '/hr/attendance/leave-approval?scene=shift-change',
                meta: { title: '调班申请（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true }
              },
              {
                path: 'overtime',
                name: 'HrAttendanceOvertime',
                redirect: '/hr/attendance/leave-approval?scene=overtime',
                meta: { title: '加班申请（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true }
              },
              {
                path: 'records',
                name: 'HrAttendanceRecords',
                component: () => import('../views/hr/HrAttendanceRecords.vue'),
                meta: { title: '考勤记录', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              },
              {
                path: 'abnormal',
                name: 'HrAttendanceAbnormal',
                component: () => import('../views/hr/HrAttendanceAbnormal.vue'),
                meta: { title: '考勤异常', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              },
              {
                path: 'access-control',
                name: 'HrAttendanceAccessControl',
                component: () => import('../views/hr/HrAccessControl.vue'),
                meta: { title: '门禁对接', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              },
              {
                path: 'card-sync',
                name: 'HrAttendanceCardSync',
                component: () => import('../views/hr/HrCardSync.vue'),
                meta: { title: '一卡通数据对接', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              }
            ]
          },
          {
            path: 'compliance',
            name: 'HrCompliance',
            meta: { title: '制度与合规', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
            redirect: '/hr/compliance/policies',
            children: [
              { path: 'policies', name: 'HrCompliancePolicies', component: () => import('../views/hr/HrPolicies.vue'), meta: { title: '规章制度库', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] } },
              { path: 'policy-alerts', name: 'HrCompliancePolicyAlerts', component: () => import('../views/hr/HrPolicyAlerts.vue'), meta: { title: '制度更新预警', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] } }
            ]
          },
          {
            path: 'oa',
            name: 'HrOaCompat',
            meta: { title: '行政协同（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true },
            redirect: '/oa/notice',
            children: [
              { path: 'notices', name: 'HrOaNotices', redirect: '/oa/notice', meta: { title: '通知管理（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true } },
              { path: 'tasks', name: 'HrOaTasks', redirect: '/oa/work-execution/task', meta: { title: '任务管理（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true } },
              { path: 'execution', name: 'HrOaExecution', redirect: '/oa/todo', meta: { title: '工作执行（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true } },
              { path: 'activity-plan', name: 'HrOaActivityPlan', redirect: '/oa/activity-plan', meta: { title: '活动计划（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true } },
              { path: 'knowledge', name: 'HrOaKnowledge', redirect: '/oa/knowledge', meta: { title: '知识库（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true } },
              { path: 'groups', name: 'HrOaGroups', redirect: '/oa/group-settings', meta: { title: '分组设置（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true } },
              { path: 'policies', name: 'HrOaPolicies', redirect: '/hr/compliance/policies', meta: { title: '规章制度库（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true } },
              { path: 'policy-alerts', name: 'HrOaPolicyAlerts', redirect: '/hr/compliance/policy-alerts', meta: { title: '制度更新预警（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true } }
            ]
          },
          {
            path: 'expense',
            name: 'HrExpense',
            meta: { title: '薪酬与费用', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
            redirect: '/hr/expense/records?scene=training-reimburse',
            children: [
              { path: 'records', name: 'HrExpenseRecords', component: () => import('../views/hr/HrExpenseScenePage.vue'), meta: { title: '费用报销台', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] } },
              { path: 'training-reimburse', name: 'HrExpenseTrainingReimburse', redirect: '/hr/expense/records?scene=training-reimburse', meta: { title: '外出培训费用报销（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true } },
              { path: 'subsidy', name: 'HrExpenseSubsidy', redirect: '/hr/expense/records?scene=subsidy', meta: { title: '补贴申请（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true } },
              { path: 'dormitory', name: 'HrExpenseDormitory', component: () => import('../views/hr/HrDormitoryManagement.vue'), meta: { title: '员工宿舍', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] } },
              { path: 'dormitory-map', name: 'HrExpenseDormitoryMap', component: () => import('../views/hr/HrDormitoryMap.vue'), meta: { title: '宿舍房态图', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] } },
              { path: 'meal-fee', name: 'HrExpenseMealFee', component: () => import('../views/hr/HrExpenseMealFee.vue'), meta: { title: '员工餐费', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] } },
              { path: 'electricity-fee', name: 'HrExpenseElectricityFee', component: () => import('../views/hr/HrExpenseElectricityFee.vue'), meta: { title: '员工电费', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] } },
              { path: 'salary-subsidy', name: 'HrExpenseSalarySubsidy', redirect: '/hr/expense/records?scene=salary-subsidy', meta: { title: '工资补贴记录（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true } },
              { path: 'approval-flow', name: 'HrExpenseApprovalFlow', component: () => import('../views/hr/HrExpenseApprovalFlow.vue'), meta: { title: '报销审批流', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] } }
            ]
          },
          {
            path: 'development',
            name: 'HrDevelopment',
            meta: { title: '培训发展', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
            redirect: '/hr/development/records',
            children: [
              { path: 'plans', name: 'HrDevelopmentPlans', redirect: '/hr/development/records?scene=plans', meta: { title: '培训计划', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] } },
              { path: 'enrollments', name: 'HrDevelopmentEnrollments', redirect: '/hr/development/records?scene=enrollments', meta: { title: '培训报名（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true } },
              { path: 'signin', name: 'HrDevelopmentSignin', redirect: '/hr/development/records?scene=signin', meta: { title: '培训签到（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true } },
              { path: 'records', name: 'HrDevelopmentRecords', component: () => import('../views/hr/Training.vue'), meta: { title: '培训记录', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] } },
              { path: 'certificates', name: 'HrDevelopmentCertificates', component: () => import('../views/hr/HrProfileCertificates.vue'), meta: { title: '证书管理', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] } },
              { path: 'certificate-reminders', name: 'HrDevelopmentCertificateReminders', component: () => import('../views/hr/HrCertificateReminders.vue'), meta: { title: '证书到期提醒', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] } }
            ]
          },
          {
            path: 'performance',
            name: 'HrPerformanceCenter',
            meta: { title: '绩效管理', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
            redirect: '/hr/performance/reports',
            children: [
              { path: 'nursing', name: 'HrPerformanceNursing', redirect: '/hr/performance/reports?scene=nursing', meta: { title: '护理绩效（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true } },
              { path: 'sales', name: 'HrPerformanceSales', redirect: '/hr/performance/reports?scene=sales', meta: { title: '销售绩效（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true } },
              { path: 'admin', name: 'HrPerformanceAdmin', redirect: '/hr/performance/reports?scene=admin', meta: { title: '行政绩效（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true } },
              { path: 'scoring-rules', name: 'HrPerformanceScoringRules', component: () => import('../views/hr/PointsRule.vue'), meta: { title: '评分规则配置', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] } },
              { path: 'generation', name: 'HrPerformanceGeneration', redirect: '/hr/performance/reports?scene=generation', meta: { title: '绩效生成（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true } },
              { path: 'reports', name: 'HrPerformanceReports', component: () => import('../views/hr/Performance.vue'), meta: { title: '绩效报表', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] } },
              { path: 'reward-punishment', name: 'HrPerformanceRewardPunishment', component: () => import('../views/oa/RewardPunishment.vue'), meta: { title: '奖惩记录', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] } }
            ]
          },
          {
            path: 'staff',
            name: 'HrStaff',
            redirect: '/hr/profile/basic',
            meta: { title: '员工档案（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true }
          },
          {
            path: 'training',
            name: 'HrTraining',
            redirect: '/hr/development/records',
            meta: { title: '培训管理（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true }
          },
          {
            path: 'incentive',
            name: 'HrIncentive',
            meta: { title: '积分激励', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
            redirect: '/hr/incentive/ledger',
            children: [
              {
                path: 'ledger',
                name: 'HrIncentiveLedger',
                component: () => import('../views/hr/Points.vue'),
                meta: { title: '积分台账', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              },
              {
                path: 'rules',
                name: 'HrIncentiveRules',
                component: () => import('../views/hr/PointsRule.vue'),
                meta: { title: '积分规则', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
              }
            ]
          },
          {
            path: 'points',
            name: 'HrPoints',
            redirect: redirectPreservingLocation('/hr/incentive/ledger'),
            meta: { title: '积分管理（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true, legacy: true, searchable: false }
          },
          {
            path: 'points-rule',
            name: 'HrPointsRule',
            redirect: redirectPreservingLocation('/hr/incentive/rules'),
            meta: { title: '积分规则（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true, legacy: true, searchable: false }
          },
          {
            path: 'performance-board',
            name: 'HrPerformanceLegacy',
            component: () => import('../views/hr/Performance.vue'),
            meta: { title: '绩效看板', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true }
          }
        ]
      }
]
