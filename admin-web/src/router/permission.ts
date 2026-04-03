import type { Router } from 'vue-router'
import { getPagePermissions, getRoles, getToken } from '../utils/auth'
import { resolveRouteAccess } from '../utils/routeAccess'

export function setupPermission(router: Router) {
  router.beforeEach((to, _from, next) => {
    const token = getToken()
    const publicPages = ['/home', '/enterprise', '/admin', '/login', '/403']

    if (!token && !publicPages.includes(to.path)) {
      next('/home')
      return
    }

    if (to.path === '/login' && token) {
      const redirect = typeof to.query.redirect === 'string' ? to.query.redirect : ''
      next(redirect || '/portal')
      return
    }

    const roles = getRoles()
    const pagePermissions = getPagePermissions()
    const access = resolveRouteAccess(router, roles, to.path, pagePermissions)
    if (!access.canAccess) {
      next('/403')
      return
    }

    next()
  })

  router.afterEach((to, from) => {
    if (to.path === from.path) {
      return
    }
    window.scrollTo({ top: 0, left: 0, behavior: 'auto' })
    const content = document.querySelector('.app-content') as HTMLElement | null
    if (content) {
      content.scrollTo({ top: 0, left: 0, behavior: 'auto' })
    }
  })
}
