import type { RouteRecordRaw } from 'vue-router'

export const systemRoutes: RouteRecordRaw[] = [
  {
        path: 'base-config',
        name: 'BaseConfig',
        meta: { title: '基础数据配置', icon: 'DatabaseOutlined', navSection: 'system', navOrder: 120, navPinned: true, roles: ['SYS_ADMIN', 'ADMIN'] },
        children: [
          {
            path: '',
            name: 'BaseConfigHub',
            component: () => import('../views/ModuleHub.vue'),
            meta: { title: '基础数据导航', hidden: true }
          },
          {
            path: 'elder-type',
            name: 'BaseConfigElderType',
            component: () => import('../views/base-config/Index.vue'),
            props: { title: '老人类别', groupCode: 'ELDER_CATEGORY' },
            meta: { title: '老人类别', roles: ['ADMIN'] }
          },
          {
            path: 'marketing',
            name: 'BaseConfigMarketing',
            meta: { title: '营销', roles: ['ADMIN'] },
            redirect: '/base-config/marketing/customer-tag',
            children: [
              {
                path: 'customer-tag',
                name: 'BaseConfigMarketingCustomerTag',
                component: () => import('../views/base-config/Index.vue'),
                props: { title: '客户标签', groupCode: 'MARKETING_CUSTOMER_TAG' },
                meta: { title: '客户标签', roles: ['ADMIN'] }
              },
              {
                path: 'source-channel',
                name: 'BaseConfigMarketingSourceChannel',
                component: () => import('../views/base-config/Index.vue'),
                props: { title: '来源渠道', groupCode: 'MARKETING_SOURCE_CHANNEL' },
                meta: { title: '来源渠道', roles: ['ADMIN'] }
              }
            ]
          },
          {
            path: 'assessment',
            name: 'BaseConfigAssessment',
            component: () => import('../views/base-config/Index.vue'),
            props: { title: '评估', groupCode: 'ASSESSMENT_TYPE' },
            meta: { title: '评估', roles: ['ADMIN'] }
          },
          {
            path: 'residence',
            name: 'BaseConfigResidence',
            meta: { title: '入住', roles: ['ADMIN'] },
            redirect: '/base-config/residence/bed-type',
            children: [
              {
                path: 'bed-type',
                name: 'BaseConfigResidenceBedType',
                redirect: '/logistics/assets/bed-type-config',
                meta: { title: '床位类型', roles: ['ADMIN'] }
              },
              {
                path: 'room-type',
                name: 'BaseConfigResidenceRoomType',
                redirect: '/logistics/assets/room-type-config',
                meta: { title: '房间类型', roles: ['ADMIN'] }
              },
              {
                path: 'area-settings',
                name: 'BaseConfigResidenceAreaSettings',
                redirect: '/logistics/assets/area-config',
                meta: { title: '区域设置', roles: ['ADMIN'] }
              },
              {
                path: 'building-management',
                name: 'BaseConfigResidenceBuildingManagement',
                redirect: '/logistics/assets/bed-management',
                meta: { title: '楼栋管理', roles: ['ADMIN'], hidden: true }
              }
            ]
          },
          {
            path: 'activity',
            name: 'BaseConfigActivity',
            component: () => import('../views/base-config/Index.vue'),
            props: { title: '活动', groupCode: 'ACTIVITY_TYPE' },
            meta: { title: '活动', roles: ['ADMIN'] }
          },
          {
            path: 'community',
            name: 'BaseConfigCommunity',
            meta: { title: '社区', roles: ['ADMIN'] },
            redirect: '/base-config/community/maintenance-category',
            children: [
              {
                path: 'maintenance-category',
                name: 'BaseConfigCommunityMaintenanceCategory',
                component: () => import('../views/base-config/Index.vue'),
                props: { title: '维修分类', groupCode: 'COMMUNITY_REPAIR_CATEGORY' },
                meta: { title: '维修分类', roles: ['ADMIN'] }
              },
              {
                path: 'task-type-settings',
                name: 'BaseConfigCommunityTaskTypeSettings',
                component: () => import('../views/base-config/Index.vue'),
                props: { title: '任务类型设置', groupCode: 'COMMUNITY_TASK_TYPE' },
                meta: { title: '任务类型设置', roles: ['ADMIN'] }
              }
            ]
          },
          {
            path: 'discharge-fee-settings',
            name: 'BaseConfigDischargeFeeSettings',
            component: () => import('../views/base-config/Index.vue'),
            props: { title: '退住费用设置', groupCode: 'DISCHARGE_FEE_CONFIG' },
            meta: { title: '退住费用设置', roles: ['ADMIN'] }
          },
          {
            path: 'fee',
            name: 'BaseConfigFee',
            component: () => import('../views/base-config/Index.vue'),
            props: { title: '费用', groupCode: 'FEE_TYPE' },
            meta: { title: '费用', roles: ['ADMIN'] }
          },
          {
            path: 'refund-reason',
            name: 'BaseConfigRefundReason',
            component: () => import('../views/base-config/Index.vue'),
            props: { title: '退款原因', groupCode: 'REFUND_REASON' },
            meta: { title: '退款原因', roles: ['ADMIN'] }
          },
          {
            path: 'trial-package',
            name: 'BaseConfigTrialPackage',
            component: () => import('../views/base-config/Index.vue'),
            props: { title: '试住套餐', groupCode: 'TRIAL_STAY_PACKAGE' },
            meta: { title: '试住套餐', roles: ['ADMIN'] }
          },
          {
            path: 'index',
            redirect: '/base-config/elder-type',
            meta: { hidden: true }
          }
        ]
      },
  {
        path: 'system',
        name: 'System',
        meta: { title: '系统管理', icon: 'SettingOutlined', navSection: 'system', navOrder: 130, navPinned: true, roles: ['DIRECTOR', 'SYS_ADMIN', 'ADMIN', 'HR_MINISTER'] },
        children: [
          {
            path: '',
            name: 'SystemHub',
            component: () => import('../views/System/SystemAdminHome.vue'),
            meta: { title: '系统管理首页', hidden: true }
          },
          {
            path: 'site-config',
            name: 'SystemSiteConfigCenter',
            component: () => import('../views/System/SiteConfigCenter.vue'),
            meta: { title: '官网配置中心', roles: ['ADMIN', 'HR_MINISTER'] }
          },
          {
            path: 'org-info',
            name: 'SystemOrgInfo',
            component: () => import('../views/System/OrgInfo.vue'),
            meta: { title: '机构信息', roles: ['DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          },
          {
            path: 'org-manage',
            name: 'SystemOrgManage',
            meta: { title: '机构管理（已下沉）', roles: ['ADMIN'], hidden: true },
            redirect: '/system/site-config?tab=profile',
            children: [
              {
                path: 'intro',
                name: 'SystemOrgIntro',
                redirect: '/system/site-config?tab=profile',
                meta: { title: '机构介绍', roles: ['ADMIN'], hidden: true }
              },
              {
                path: 'news',
                name: 'SystemOrgNews',
                redirect: '/system/site-config?tab=profile',
                meta: { title: '机构动态', roles: ['ADMIN'], hidden: true }
              },
              {
                path: 'life',
                name: 'SystemLifeEntertainment',
                redirect: '/system/site-config?tab=profile',
                meta: { title: '生活娱乐', roles: ['ADMIN'], hidden: true }
              }
            ]
          },
          {
            path: 'role',
            name: 'SystemRoleManage',
            component: () => import('../views/System/RoleManage.vue'),
            meta: { title: '角色与权限', roles: ['SYS_ADMIN'] }
          },
          {
            path: 'department',
            name: 'SystemDepartmentManage',
            component: () => import('../views/System/DepartmentManage.vue'),
            meta: { title: '部门管理', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] }
          },
          {
            path: 'app-version',
            name: 'SystemAppVersion',
            redirect: '/system/site-config?tab=profile',
            meta: { title: 'APP版本管理（已下沉）', roles: ['ADMIN'], hidden: true }
          },
          {
            path: 'dict',
            name: 'SystemDictionary',
            redirect: '/base-config/elder-type',
            meta: { title: '系统字典（并入基础数据配置）', roles: ['ADMIN'], hidden: true }
          },
          {
            path: 'message',
            name: 'SystemMessageManage',
            redirect: '/system/site-config?tab=consult',
            meta: { title: '留言管理', roles: ['ADMIN'], hidden: true }
          },
          {
            path: 'staff',
            name: 'SystemStaff',
            redirect: '/hr/profile/account-access',
            meta: { title: '员工管理（兼容）', roles: ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'], hidden: true }
          },
          {
            path: 'menu-preview',
            name: 'SystemMenuPreview',
            component: () => import('../views/System/MenuPreview.vue'),
            meta: { title: '菜单预览', roles: ['ADMIN'], hidden: true }
          },
          {
            path: 'permission-overview',
            name: 'SystemPermissionOverview',
            redirect: '/system/role',
            meta: { title: '权限总览（已并入角色管理）', roles: ['DIRECTOR', 'SYS_ADMIN', 'ADMIN', 'HR_MINISTER'], hidden: true }
          }
        ]
      },
  {
        path: 'demo',
        name: 'Demo',
        meta: { title: '能力演示', icon: 'ExperimentOutlined', hidden: true },
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
