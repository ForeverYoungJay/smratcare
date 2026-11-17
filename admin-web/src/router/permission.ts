import type { Router } from 'vue-router'
import { getPagePermissions, getPermissions, getRoles, getToken } from '../utils/auth'
import { resolveRouteAccess } from '../utils/routeAccess'
import { resolveDefaultHome } from '../access/policy'
import { functionWorkspaces } from '../workspaces/catalog'

function normalizeRedirectPath(path?: string | null) {
  const raw = String(path || '').trim()
  if (!raw.startsWith('/') || raw.startsWith('//') || raw.includes('\\')) return ''
  if (raw === '/login') return ''
  if (raw.startsWith('/login?')) {
    const query = raw.split('?')[1] || ''
    const params = new URLSearchParams(query)
    return normalizeRedirectPath(params.get('redirect'))
  }
  return raw
}

export function resolveAuthorizedLoginTarget(router: Router, redirect: string, roles: string[], pagePermissions: string[], permissions: string[] = []) {
  const target = normalizeRedirectPath(redirect)
  const resolved = target ? router.resolve(target) : null
  if (resolved && resolved.matched.length && !resolved.matched.some((record) => record.name === 'NotFound')
    && resolved.path !== '/403'
    && resolveRouteAccess(router, roles, resolved.fullPath, pagePermissions, permissions).canAccess) return target
  const candidates = [resolveDefaultHome(roles), '/workbench/overview', ...functionWorkspaces.map((workspace) => workspace.path)]
  return candidates.find((path) => resolveRouteAccess(router, roles, path, pagePermissions, permissions).canAccess) || '/403'
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
      next(resolveAuthorizedLoginTarget(router, redirect, getRoles(), getPagePermissions(), getPermissions()))
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
    const access = resolveRouteAccess(router, roles, to.fullPath, pagePermissions, getPermissions())
    if (!access.canAccess) {
      if (to.path === resolveDefaultHome(roles)) {
        next(resolveAuthorizedLoginTarget(router, '', roles, pagePermissions, getPermissions()))
        return
      }
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
