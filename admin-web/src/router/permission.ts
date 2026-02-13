import type { Router } from 'vue-router'
import { getRoles, getToken } from '../utils/auth'

export function setupPermission(router: Router) {
  router.beforeEach((to, _from, next) => {
    const token = getToken()
    if (to.path !== '/login' && !token) {
      next('/login')
      return
    }

    if (to.path === '/login' && token) {
      next('/portal')
      return
    }

    const roles = getRoles()
    const required = (to.meta?.roles as string[] | undefined) || []
    if (required.length > 0 && !required.some((r) => roles.includes(r))) {
      next('/403')
      return
    }

    next()
  })
}
