import type { Id } from '../types'

export function normalizeId(value: unknown): Id | undefined {
  const raw = Array.isArray(value) ? value[0] : value
  const text = String(raw ?? '').trim()
  return text ? text : undefined
}

export function normalizeResidentId(routeQuery: Record<string, unknown>): Id | undefined {
  return normalizeId(routeQuery.residentId ?? routeQuery.elderId)
}
