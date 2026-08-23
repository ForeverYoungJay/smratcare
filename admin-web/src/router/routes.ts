import type { RouteRecordRaw } from 'vue-router'
import { administrationRoutes } from './administrationRoutes'
import { analyticsRoutes } from './analyticsRoutes'
import { financeRoutes } from './financeRoutes'
import { hrRoutes } from './hrRoutes'
import { legacyModuleRedirects } from './legacyRedirects'
import { logisticsRoutes } from './logisticsRoutes'
import { marketingRoutes } from './marketingRoutes'
import { medicalRoutes } from './medicalRoutes'
import { nursingRoutes } from './nursingRoutes'
import { safetyRoutes } from './safetyRoutes'
import { serviceRoutes } from './serviceRoutes'
import { sharedRoutes } from './sharedRoutes'
import { systemRoutes } from './systemRoutes'
import { getRoles } from '../utils/auth'
import { resolveDefaultHome } from '../access/policy'

export const routes: RouteRecordRaw[] = [
  {
    path: '/enterprise',
    component: () => import('../views/EnterpriseHome.vue'),
    beforeEnter() {
      window.location.replace('/')
      return false
    },
    meta: { title: '企业首页', hidden: true }
  },
  {
    path: '/home',
    component: () => import('../views/EnterpriseHome.vue'),
    beforeEnter() {
      window.location.replace('/')
      return false
    },
    meta: { title: '弋阳龟峰颐养中心', hidden: true }
  },
  {
    path: '/admin',
    redirect: '/login?redirect=/portal',
    meta: { title: '后台入口', hidden: true }
  },
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
    redirect: () => resolveDefaultHome(getRoles()),
    children: [
      ...sharedRoutes,
      ...marketingRoutes,
      ...legacyModuleRedirects,
      ...logisticsRoutes,
      ...nursingRoutes,
      ...financeRoutes,
      ...medicalRoutes,
      ...safetyRoutes,
      ...analyticsRoutes,
      ...serviceRoutes,
      ...administrationRoutes,
      ...hrRoutes,
      ...systemRoutes
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { title: '未找到', hidden: true }
  }
]
