import type { RouteRecordRaw } from 'vue-router'
import { MEDICAL_CENTERS } from '../views/medical/center/centerTabs'

/**
 * 医护健康服务：菜单只保留 7 个主页面 + 管理员可见的系统配置。
 * 原有 26 个功能页保留路由（菜单隐藏），作为主页面的标签内容嵌入，权限仍以各自路由 meta.roles 为准。
 */
const MEDICAL_DOCTORS = ['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']
const MEDICAL_ADMINS = ['MEDICAL_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']

function center(key: string, name: string, roles?: string[]): RouteRecordRaw {
  const config = MEDICAL_CENTERS[key]
  return {
    path: key,
    name,
    component: () => import('../views/medical/center/TabbedCenter.vue'),
    props: config,
    meta: { title: config.title, ...(roles ? { roles } : {}) }
  }
}

export const medicalRoutes: RouteRecordRaw[] = [
  {
        path: 'health',
        name: 'Health',
        meta: { title: '健康服务', icon: 'HeartOutlined', hidden: true },
        redirect: '/medical-care/workbench',
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
        // 护理岗的任务看板/护理日志/巡检/交接班都在本模块下（多数子路由已授权 NURSING_*），模块级需同步放行，否则护理人员侧栏无任何业务入口
        meta: { title: '医护健康服务', icon: 'MedicineBoxOutlined', navSection: 'care', navOrder: 40, navPinned: true, roles: ['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_EMPLOYEE', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
        redirect: '/medical-care/workbench',
        children: [
          { path: 'workbench', name: 'MedicalWorkbench', component: () => import('../views/medical/center/MedicalWorkbench.vue'), meta: { title: '医务工作台' } },
          { path: 'residents', name: 'MedicalHealthArchive', component: () => import('../views/medical/center/HealthArchive.vue'), meta: { title: '长者健康档案' } },
          center('clinical', 'MedicalClinical', MEDICAL_DOCTORS),
          center('medication', 'MedicalMedication'),
          center('monitoring', 'MedicalMonitoring'),
          center('coordination', 'MedicalCoordination'),
          center('alerts', 'MedicalAlerts'),
          center('settings', 'MedicalSettings', MEDICAL_ADMINS),
          { path: 'center', redirect: (to) => ({ path: '/medical-care/workbench', query: to.query }), meta: { hidden: true } },
          {
            path: 'basic-diseases',
            name: 'MedicalCareResidentDiseases',
            component: () => import('../views/medical/ResidentDiseaseManager.vue'),
            meta: { hidden: true, title: '基础疾病维护' }
          },
          {
            path: 'care-task-board',
            name: 'MedicalCareTaskBoard',
            component: () => import('../views/care/workbench/TaskBoard.vue'),
            // 护理岗核心看板：不继承医护模块的角色限制，护理员工/部长可直接使用
            meta: { hidden: true, title: '护理任务看板', roles: ['NURSING_EMPLOYEE', 'NURSING_MINISTER', 'MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          },
          {
            path: 'unified-task-center',
            name: 'MedicalCareUnifiedTaskCenter',
            component: () => import('../views/medical/UnifiedTaskCenter.vue'),
            meta: { hidden: true, title: '统一任务中心' }
          },
          {
            path: 'inspection',
            name: 'MedicalCareInspection',
            component: () => import('../views/health/Inspection.vue'),
            meta: { hidden: true, title: '健康巡检' }
          },
          {
            path: 'medication-registration',
            name: 'MedicalCareMedicationRegistration',
            component: () => import('../views/health/MedicationRegistration.vue'),
            meta: { hidden: true, title: '用药登记' }
          },
          {
            path: 'nursing-log',
            name: 'MedicalCareNursingLog',
            component: () => import('../views/health/NursingLog.vue'),
            meta: { hidden: true, title: '护理日志' }
          },
          {
            path: 'handovers',
            name: 'MedicalCareHandovers',
            component: () => import('../views/care/Handovers.vue'),
            meta: { hidden: true, title: '交接班' }
          },
          {
            path: 'assessment/tcm',
            name: 'MedicalCareTcmAssessment',
            component: () => import('../views/medical/TcmAssessment.vue'),
            meta: { hidden: true, title: '中医体质评估' }
          },
          {
            path: 'assessment/cvd',
            name: 'MedicalCareCvdAssessment',
            component: () => import('../views/medical/CvdRiskAssessment.vue'),
            meta: { hidden: true, title: '心血管风险评估' }
          },
          {
            path: 'integrated-account',
            name: 'MedicalCareIntegratedAccount',
            component: () => import('../views/medical/IntegratedHealthAccount.vue'),
            meta: { hidden: true, title: '健康服务与医护账户一体化' }
          },
          {
            path: 'order-risk-overview',
            name: 'MedicalCareOrderRiskOverview',
            component: () => import('../views/medical/MedicalOrderCenter.vue'),
            meta: { title: '医嘱执行风险总览', hidden: true, searchable: false }
          },
          {
            path: 'nursing-quality',
            name: 'MedicalCareNursingQuality',
            component: () => import('../views/medical/NursingQualityCenter.vue'),
            meta: { hidden: true, title: '护理与质量中心' }
          },
          {
            path: 'alert-rules',
            name: 'MedicalCareAlertRules',
            component: () => import('../views/medical/AlertRuleConfig.vue'),
            meta: { hidden: true, title: '异常规则配置' }
          },
          {
            path: 'smart-alerts',
            name: 'MedicalCareSmartAlerts',
            component: () => import('../views/medical/SmartDeviceAlerts.vue'),
            meta: { hidden: true, title: '智慧设备告警' }
          },
          {
            path: 'smart-rules',
            name: 'MedicalCareSmartRules',
            component: () => import('../views/smart/AlertRules.vue'),
            meta: { hidden: true, title: '安全场景规则', roles: ['NURSING_MINISTER', 'MEDICAL_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          },
          {
            path: 'smart-dispatch',
            name: 'MedicalCareSmartDispatch',
            component: () => import('../views/smart/DispatchBoard.vue'),
            meta: { hidden: true, title: '告警派单看板', roles: ['NURSING_EMPLOYEE', 'NURSING_MINISTER', 'MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          },
          {
            path: 'smart-device-health',
            name: 'MedicalCareSmartDeviceHealth',
            component: () => import('../views/smart/DeviceHealth.vue'),
            meta: { hidden: true, title: '设备健康监控', roles: ['NURSING_EMPLOYEE', 'NURSING_MINISTER', 'MEDICAL_MINISTER', 'LOGISTICS_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          },
          {
            path: 'emr',
            name: 'MedicalCareEmr',
            component: () => import('../views/emr/EmrRecords.vue'),
            meta: { hidden: true, title: '电子病历', roles: ['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          },
          {
            path: 'orders',
            name: 'MedicalCareOrders',
            component: () => import('../views/medorder/MedicalOrders.vue'),
            meta: { hidden: true, title: '医嘱管理', roles: ['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          },
          {
            path: 'order-executions',
            name: 'MedicalCareOrderExecutions',
            component: () => import('../views/medorder/OrderExecutions.vue'),
            meta: { hidden: true, title: '医嘱执行', roles: ['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_EMPLOYEE', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          },
          {
            path: 'pharmacy',
            name: 'MedicalCarePharmacy',
            component: () => import('../views/pharmacy/Pharmacy.vue'),
            meta: { hidden: true, title: '药事管理', roles: ['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_EMPLOYEE', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          },
          {
            path: 'ai-reports',
            name: 'MedicalCareAiReports',
            component: () => import('../views/medical/AiHealthReport.vue'),
            meta: { hidden: true, title: 'AI健康评估报告' }
          },
          {
            path: 'rounds',
            name: 'MedicalCareRounds',
            component: () => import('../views/medical/RoundsWorkbench.vue'),
            meta: { hidden: true, title: '医生巡诊', roles: ['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          },
          {
            path: 'followup',
            name: 'MedicalCareChronicFollowup',
            component: () => import('../views/medical/ChronicFollowup.vue'),
            meta: { hidden: true, title: '慢病随访', roles: ['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          },
          {
            path: 'emergency',
            name: 'MedicalCareEmergencyEvents',
            component: () => import('../views/medical/EmergencyEvents.vue'),
            meta: { hidden: true, title: '120急救事件', roles: ['MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER', 'NURSING_EMPLOYEE', 'NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          }
        ]
      }
]
