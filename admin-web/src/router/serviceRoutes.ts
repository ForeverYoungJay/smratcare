import type { RouteRecordRaw } from 'vue-router'

export const serviceRoutes: RouteRecordRaw[] = [
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
      }
]
