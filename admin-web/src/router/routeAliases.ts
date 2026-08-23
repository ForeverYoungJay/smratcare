import routeAliases from '../../../src/main/resources/security/route-aliases.json'
import type { RouteLocationGeneric, RouteLocationRaw } from 'vue-router'

export const LEGACY_ROUTE_ALIASES: Readonly<Record<string, string>> = routeAliases

export function normalizeRoutePath(path: string): string {
  const base = String(path || '').split('?')[0].split('#')[0].trim()
  if (!base) return '/'
  const normalized = base.replace(/\/+/g, '/')
  const withoutTrailingSlash = normalized.length > 1 && normalized.endsWith('/') ? normalized.slice(0, -1) : normalized
  return withoutTrailingSlash.startsWith('/') ? withoutTrailingSlash : `/${withoutTrailingSlash}`
}

export function toCanonicalRoutePath(path: string): string {
  let current = normalizeRoutePath(path)
  const seen = new Set<string>()
  while (LEGACY_ROUTE_ALIASES[current] && !seen.has(current)) {
    seen.add(current)
    current = normalizeRoutePath(LEGACY_ROUTE_ALIASES[current])
  }
  return current
}

export function redirectPreservingLocation(canonicalPath: string) {
  return (to: RouteLocationGeneric): RouteLocationRaw => ({
    path: toCanonicalRoutePath(canonicalPath),
    query: to.query,
    hash: to.hash
  })
}
