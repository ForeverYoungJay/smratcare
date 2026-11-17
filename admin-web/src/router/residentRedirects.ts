import type { LocationQuery, RouteLocationRaw } from 'vue-router'

// 长者管理合并为 5 个入口后，旧页面地址统一跳到新位置，并把长者 ID 和"打开新建"带过去
const BED_PAGE = '/elder/bed-panorama'
const FINANCE_EXIT_PAGE = '/elder/status-change/discharge-settlement'
const ADMISSION_PAGE = '/elder/admission-processing'

function text(value: LocationQuery[string]) {
  const raw = Array.isArray(value) ? value[0] : value
  return raw == null ? '' : String(raw).trim()
}

function clean(query: Record<string, string>) {
  return Object.fromEntries(Object.entries(query).filter(([, value]) => value))
}

export function legacyElderId(query: LocationQuery) {
  return text(query.elderId) || text(query.residentId) || text(query.docElderId)
}

function wantsCreate(query: LocationQuery) {
  return text(query.openCreate) === '1' || text(query.create) === '1'
}

export type LegacyResidentPage = 'center' | 'outing' | 'medical-outing' | 'discharge-apply' | 'death-register' | 'trial-stay' | 'contracts-invoices'

export function legacyResidentRedirect(page: LegacyResidentPage, query: LocationQuery): RouteLocationRaw {
  const elderId = legacyElderId(query)
  const create = wantsCreate(query) ? '1' : ''
  switch (page) {
    case 'center':
      return { path: BED_PAGE, query: clean({ elderId }) }
    case 'outing':
    case 'medical-outing':
      return {
        path: BED_PAGE,
        query: clean({
          view: 'records',
          recordType: page === 'outing' ? 'OUTING' : 'MEDICAL_OUTING',
          elderId,
          action: create ? (page === 'outing' ? 'outing' : 'medical') : ''
        })
      }
    case 'discharge-apply':
    case 'death-register':
      return {
        path: FINANCE_EXIT_PAGE,
        query: clean({ tab: 'discharge', departure: page === 'death-register' ? 'DEATH' : '', elderId, create })
      }
    case 'trial-stay':
      return { path: ADMISSION_PAGE, query: clean({ tab: 'trial', elderId, create }) }
    case 'contracts-invoices':
      return elderId
        ? { path: `/elder/detail/${elderId}`, query: { tab: 'contract' } }
        : { path: FINANCE_EXIT_PAGE, query: { tab: 'contract' } }
  }
}
