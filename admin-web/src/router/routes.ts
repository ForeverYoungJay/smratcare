import type { RouteRecordRaw } from 'vue-router'

export const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('../views/Forbidden.vue'),
    meta: { title: '无权限', hidden: true }
  },
  {
    path: '/',
    name: 'Root',
    component: () => import('../layouts/BasicLayout.vue'),
    redirect: '/portal',
    children: [
      {
        path: 'portal',
        name: 'Portal',
        component: () => import('../views/Portal.vue'),
        meta: { title: '信息门户', icon: 'AppstoreOutlined' }
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: 'Dashboard', icon: 'DashboardOutlined' }
      },
      {
        path: 'elder',
        name: 'Elder',
        meta: { title: '老人管理', icon: 'TeamOutlined' },
        redirect: '/elder/list',
        children: [
          {
            path: 'list',
            name: 'ElderList',
            component: () => import('../views/elder/List.vue'),
            meta: { title: '老人列表' }
          },
          {
            path: 'create',
            name: 'ElderCreate',
            component: () => import('../views/elder/Create.vue'),
            meta: { title: '新增老人', hidden: true }
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
            meta: { title: 'CRM线索' }
          },
          {
            path: 'admission',
            name: 'ElderAdmission',
            component: () => import('../views/elder/Admission.vue'),
            meta: { title: '入院办理' }
          },
          {
            path: 'discharge',
            name: 'ElderDischarge',
            component: () => import('../views/elder/Discharge.vue'),
            meta: { title: '退院办理' }
          },
          {
            path: 'change-log',
            name: 'ElderChangeLog',
            component: () => import('../views/elder/ChangeLog.vue'),
            meta: { title: '变更记录' }
          }
        ]
      },
      {
        path: 'bed',
        name: 'Bed',
        meta: { title: '床位房态', icon: 'HomeOutlined' },
        redirect: '/bed/map',
        children: [
          {
            path: 'map',
            name: 'BedMap',
            component: () => import('../views/bed/Map.vue'),
            meta: { title: '房态图' }
          },
          {
            path: 'manage',
            name: 'BedManage',
            component: () => import('../views/bed/Manage.vue'),
            meta: { title: '床位管理' }
          }
        ]
      },
      {
        path: 'care',
        name: 'Care',
        meta: { title: '护理管理', icon: 'ScheduleOutlined' },
        redirect: '/care/dashboard',
        children: [
          {
            path: 'dashboard',
            name: 'CareDashboard',
            component: () => import('../views/care/Dashboard.vue'),
            meta: { title: '护理看板' }
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
            path: 'service-items',
            name: 'CareServiceItems',
            component: () => import('../views/care/ServiceItems.vue'),
            meta: { title: '服务项目库' }
          },
          {
            path: 'care-packages',
            name: 'CarePackages',
            component: () => import('../views/care/CarePackages.vue'),
            meta: { title: '护理套餐' }
          },
          {
            path: 'package-items',
            name: 'CarePackageItems',
            component: () => import('../views/care/PackageItems.vue'),
            meta: { title: '套餐明细' }
          },
          {
            path: 'elder-packages',
            name: 'CareElderPackages',
            component: () => import('../views/care/ElderPackages.vue'),
            meta: { title: '老人套餐' }
          },
          {
            path: 'task-generate',
            name: 'CareTaskGenerate',
            component: () => import('../views/care/TaskGenerate.vue'),
            meta: { title: '套餐任务生成' }
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
            meta: { title: '防作弊审计' }
          }
        ]
      },
      {
        path: 'store',
        name: 'Store',
        meta: { title: '商城与库存', icon: 'ShopOutlined' },
        children: [
          {
            path: 'product',
            name: 'StoreProduct',
            component: () => import('../views/store/Product.vue'),
            meta: { title: '商品管理' }
          },
          {
            path: 'tag',
            name: 'StoreTag',
            component: () => import('../views/store/Tag.vue'),
            meta: { title: '商品标签' }
          },
          {
            path: 'risk',
            name: 'StoreRisk',
            component: () => import('../views/store/Risk.vue'),
            meta: { title: '禁忌规则' }
          },
          {
            path: 'order',
            name: 'StoreOrder',
            component: () => import('../views/store/Order.vue'),
            meta: { title: '订单管理' }
          },
          {
            path: 'points',
            name: 'StorePoints',
            component: () => import('../views/store/Points.vue'),
            meta: { title: '积分账户' }
          }
        ]
      },
      {
        path: 'inventory',
        name: 'Inventory',
        meta: { title: '库存管理', icon: 'AlertOutlined' },
        children: [
          {
            path: '',
            redirect: '/inventory/overview',
            meta: { hidden: true }
          },
          {
            path: 'overview',
            name: 'InventoryOverview',
            component: () => import('../views/inventory/Overview.vue'),
            meta: { title: '库存总览' }
          },
          {
            path: 'inbound',
            name: 'InventoryInbound',
            component: () => import('../views/inventory/Inbound.vue'),
            meta: { title: '入库管理' }
          },
          {
            path: 'outbound',
            name: 'InventoryOutbound',
            component: () => import('../views/inventory/Outbound.vue'),
            meta: { title: '出库记录' }
          },
          {
            path: 'alerts',
            name: 'InventoryAlerts',
            component: () => import('../views/inventory/Alerts.vue'),
            meta: { title: '预警中心' }
          },
          {
            path: 'adjustments',
            name: 'InventoryAdjustments',
            component: () => import('../views/inventory/Adjustments.vue'),
            meta: { title: '盘点记录' }
          }
        ]
      },
      {
        path: 'finance',
        name: 'Finance',
        meta: { title: '财务管理', icon: 'AccountBookOutlined' },
        redirect: '/finance/bill',
        children: [
          {
            path: 'bill',
            name: 'FinanceBill',
            component: () => import('../views/finance/BillCenter.vue'),
            meta: { title: '月账单中心' }
          },
          {
            path: 'bill/:billId',
            name: 'FinanceBillDetail',
            component: () => import('../views/finance/BillDetail.vue'),
            meta: { title: '账单详情', hidden: true }
          },
          {
            path: 'reconcile',
            name: 'FinanceReconcile',
            component: () => import('../views/finance/Reconcile.vue'),
            meta: { title: '对账中心' }
          },
          {
            path: 'report',
            name: 'FinanceReport',
            component: () => import('../views/finance/Report.vue'),
            meta: { title: '财务报表' }
          },
          {
            path: 'account',
            name: 'FinanceAccount',
            component: () => import('../views/finance/Account.vue'),
            meta: { title: '老人账户' }
          },
          {
            path: 'account-log',
            name: 'FinanceAccountLog',
            component: () => import('../views/finance/AccountLog.vue'),
            meta: { title: '账户流水', hidden: true }
          }
        ]
      },
      {
        path: 'life',
        name: 'Life',
        meta: { title: '生活与健康', icon: 'CoffeeOutlined' },
        redirect: '/life/meal-plan',
        children: [
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
          }
        ]
      },
      {
        path: 'survey',
        name: 'Survey',
        meta: { title: '问卷与持续改进', icon: 'FormOutlined' },
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
      },
      {
        path: 'oa',
        name: 'OA',
        meta: { title: '行政办公', icon: 'ApartmentOutlined' },
        redirect: '/oa/portal',
        children: [
          {
            path: 'portal',
            name: 'OaPortal',
            component: () => import('../views/oa/Portal.vue'),
            meta: { title: '门户与待办' }
          },
          {
            path: 'notice',
            name: 'OaNotice',
            component: () => import('../views/oa/Notice.vue'),
            meta: { title: '公告管理' }
          },
          {
            path: 'todo',
            name: 'OaTodo',
            component: () => import('../views/oa/Todo.vue'),
            meta: { title: '待办事项' }
          },
          {
            path: 'approval',
            name: 'OaApproval',
            component: () => import('../views/oa/Approval.vue'),
            meta: { title: '固定审批' }
          },
          {
            path: 'document',
            name: 'OaDocument',
            component: () => import('../views/oa/Document.vue'),
            meta: { title: '文档管理' }
          },
          {
            path: 'task',
            name: 'OaTask',
            component: () => import('../views/oa/Task.vue'),
            meta: { title: '日程任务' }
          }
        ]
      },
      {
        path: 'hr',
        name: 'Hr',
        meta: { title: '人力与绩效', icon: 'SolutionOutlined', roles: ['ADMIN'] },
        redirect: '/hr/staff',
        children: [
          {
            path: 'staff',
            name: 'HrStaff',
            component: () => import('../views/hr/Staff.vue'),
            meta: { title: '员工档案', roles: ['ADMIN'] }
          },
          {
            path: 'training',
            name: 'HrTraining',
            component: () => import('../views/hr/Training.vue'),
            meta: { title: '培训管理', roles: ['ADMIN'] }
          },
          {
            path: 'points',
            name: 'HrPoints',
            component: () => import('../views/hr/Points.vue'),
            meta: { title: '积分管理', roles: ['ADMIN'] }
          },
          {
            path: 'points-rule',
            name: 'HrPointsRule',
            component: () => import('../views/hr/PointsRule.vue'),
            meta: { title: '积分规则', roles: ['ADMIN'] }
          },
          {
            path: 'performance',
            name: 'HrPerformance',
            component: () => import('../views/hr/Performance.vue'),
            meta: { title: '绩效看板', roles: ['ADMIN'] }
          }
        ]
      },
      {
        path: 'system',
        name: 'System',
        meta: { title: '系统管理', icon: 'SettingOutlined', roles: ['ADMIN'] },
        children: [
          {
            path: 'staff',
            name: 'SystemStaff',
            component: () => import('../views/Staff.vue'),
            meta: { title: '员工管理', roles: ['ADMIN'] }
          },
          {
            path: 'menu-preview',
            name: 'SystemMenuPreview',
            component: () => import('../views/System/MenuPreview.vue'),
            meta: { title: '菜单预览', roles: ['ADMIN'] }
          }
        ]
      },
      {
        path: 'demo',
        name: 'Demo',
        meta: { title: '能力演示', icon: 'ExperimentOutlined' },
        children: [
          { path: 'vxe-table', name: 'DemoVxe', component: () => import('../views/demo/VxeTableDemo.vue'), meta: { title: '高级表格' } },
          { path: 'calendar', name: 'DemoCalendar', component: () => import('../views/demo/CalendarDemo.vue'), meta: { title: '排班日历' } },
          { path: 'charts', name: 'DemoCharts', component: () => import('../views/demo/ChartsDemo.vue'), meta: { title: '报表图表' } },
          { path: 'editor', name: 'DemoEditor', component: () => import('../views/demo/EditorDemo.vue'), meta: { title: '富文本编辑' } },
          { path: 'flow', name: 'DemoFlow', component: () => import('../views/demo/FlowDemo.vue'), meta: { title: '流程图' } },
          { path: 'antdx-chat', name: 'DemoAntdx', component: () => import('../views/demo/AntdxChatDemo.vue'), meta: { title: '智能助手' } }
        ]
      }
    ]
  },
  { path: '/', redirect: '/portal' },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { title: '未找到', hidden: true }
  }
]
