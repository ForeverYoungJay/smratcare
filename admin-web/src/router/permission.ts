import type { Router } from 'vue-router'
import { getRoles, getToken } from '../utils/auth'

export function setupPermission(router: Router) {
  router.beforeEach((to, _from, next) => {
    const token = getToken()
    const publicPages = ['/home', '/login']

    if (!token && !publicPages.includes(to.path)) {
      next('/home')
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

  router.afterEach(() => {
    window.scrollTo({ top: 0, left: 0, behavior: 'auto' })
    const content = document.querySelector('.app-content') as HTMLElement | null
    if (content) {
      content.scrollTo({ top: 0, left: 0, behavior: 'auto' })
    }
  })
}
