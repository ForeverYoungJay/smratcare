import type { Router } from 'vue-router'
import { getPagePermissions, getRoles, getToken } from '../utils/auth'
import { resolveRouteAccess } from '../utils/routeAccess'

function normalizeRedirectPath(path?: string | null) {
  const raw = String(path || '').trim()
  if (!raw.startsWith('/')) return '/portal'
  if (raw === '/login') return '/portal'
  if (raw.startsWith('/login?')) {
    const query = raw.split('?')[1] || ''
    const params = new URLSearchParams(query)
    return normalizeRedirectPath(params.get('redirect'))
  }
  return raw
}

function resolveLoginRedirectTarget(fullPath: string) {
  return {
    path: '/login',
    query: {
      redirect: normalizeRedirectPath(fullPath)
    }
  }
}

export function setupPermission(router: Router) {
  router.beforeEach((to, _from, next) => {
    const token = getToken()
    const publicPages = ['/home', '/enterprise', '/admin', '/login', '/403']

    if (!token && !publicPages.includes(to.path)) {
      next(resolveLoginRedirectTarget(to.fullPath))
      return
    }

    if (to.path === '/login' && token) {
      const redirect = typeof to.query.redirect === 'string' ? to.query.redirect : ''
      next(normalizeRedirectPath(redirect))
      return
    }

    // 公开页（登录/403/官网）不做角色与页面权限校验：
    // 否则 token 失效但本地残留 pagePermissions 时，登录页自身会被拦成 403，用户死锁无法重新登录
    if (publicPages.includes(to.path)) {
      next()
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
