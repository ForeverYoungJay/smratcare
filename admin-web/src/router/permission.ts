import type { Router } from 'vue-router'
import { getRoles, getToken } from '../utils/auth'
import { hasRouteAccess } from '../utils/roleAccess'

export function setupPermission(router: Router) {
  router.beforeEach((to, _from, next) => {
    const token = getToken()
    const publicPages = ['/home', '/enterprise', '/admin', '/login']

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
    const required = (to.meta?.roles as string[] | undefined) || []
    if (!hasRouteAccess(roles, required, to.path)) {
      next('/403')
      return
    }

    next()
  })

  router.afterEach(() => {
    window.scrollTo({ top: 0, left: 0, behavior: 'auto' })
    const content = document.querySelector('.app-content') as HTMLElement | null
    if (content) {
      content.scrollTo({ top: 0, left: 0, behavior: 'auto' })
    }
  })
}
