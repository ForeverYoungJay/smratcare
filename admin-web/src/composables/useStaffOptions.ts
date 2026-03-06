import { onMounted, onUnmounted, ref } from 'vue'
import { getStaffPage } from '../api/rbac'
import type { PageResult, StaffItem } from '../types'
import { subscribeLiveSync } from '../utils/liveSync'
import { fuzzyScore, toPinyinInitials } from './entitySearch'

export interface StaffOption {
  label: string
  value: string
  name: string
  departmentId?: string
}

interface UseStaffOptionsConfig {
  pageSize?: number
  preloadSize?: number
}

const staffPoolCache = new Map<string, StaffItem[]>()
const staffPoolFetchedAt = new Map<string, number>()
const STAFF_POOL_CACHE_TTL = 90 * 1000

function toStaffOption(item: StaffItem): StaffOption {
  const name = item.realName || item.username || `员工#${item.id}`
  const suffix = item.staffNo ? ` (${item.staffNo})` : ''
  return {
    label: `${name}${suffix}`,
    value: String(item.id),
    name,
    departmentId: item.departmentId == null ? undefined : String(item.departmentId)
  }
}

function staffSearchText(item: StaffItem) {
  const text = `${item.realName || ''} ${item.username || ''} ${item.staffNo || ''} ${item.phone || ''}`.trim()
  return `${text} ${toPinyinInitials(text)}`
}

function dedupeStaff(rows: StaffItem[]) {
  const map = new Map<string, StaffItem>()
  rows.forEach((item) => {
    const key = String(item.id)
    if (!map.has(key)) map.set(key, item)
  })
  return Array.from(map.values())
}

export function useStaffOptions(config: UseStaffOptionsConfig = {}) {
  const pageSize = config.pageSize || 80
  const preloadSize = config.preloadSize || Math.max(pageSize * 4, 300)
  const staffOptions = ref<StaffOption[]>([])
  const staffLoading = ref(false)
  const lastKeyword = ref('')

  async function loadBasePool(force = false) {
    const cacheKey = 'staff-all'
    const lastAt = staffPoolFetchedAt.get(cacheKey) || 0
    const hasFresh = staffPoolCache.has(cacheKey) && (Date.now() - lastAt < STAFF_POOL_CACHE_TTL)
    if (!force && hasFresh) return staffPoolCache.get(cacheKey) || []
    const page: PageResult<StaffItem> = await getStaffPage({ pageNo: 1, pageSize: preloadSize })
    const rows = page.list || []
    staffPoolCache.set(cacheKey, rows)
    staffPoolFetchedAt.set(cacheKey, Date.now())
    return rows
  }

  async function searchStaff(keyword = '') {
    staffLoading.value = true
    try {
      lastKeyword.value = String(keyword || '')
      const text = String(keyword || '').trim()
      const baseRows = await loadBasePool(false)
      let mergedRows = [...baseRows]
      if (text) {
        const page: PageResult<StaffItem> = await getStaffPage({ pageNo: 1, pageSize: Math.max(pageSize * 2, 120), keyword: text })
        mergedRows = dedupeStaff([...(page.list || []), ...baseRows])
      }
      const finalRows = text
        ? mergedRows
          .map((item) => ({ item, score: fuzzyScore(staffSearchText(item), text) }))
          .filter((row) => row.score >= 0)
          .sort((a, b) => b.score - a.score || String(a.item.realName || a.item.username || '').localeCompare(String(b.item.realName || b.item.username || ''), 'zh-CN'))
          .slice(0, pageSize)
          .map((row) => row.item)
        : mergedRows.slice(0, pageSize)
      staffOptions.value = finalRows.map(toStaffOption)
    } finally {
      staffLoading.value = false
    }
  }

  function ensureSelectedStaff(staffId?: string | number, staffName?: string) {
    if (staffId == null || staffId === '') return
    const value = String(staffId)
    if (staffOptions.value.some((item) => item.value === value)) return
    const name = staffName || `员工#${value}`
    staffOptions.value.unshift({
      label: name,
      value,
      name
    })
  }

  function findStaffName(staffId?: string | number) {
    if (staffId == null || staffId === '') return ''
    return staffOptions.value.find((item) => item.value === String(staffId))?.name || ''
  }

  function invalidateStaffCache() {
    const cacheKey = 'staff-all'
    staffPoolCache.delete(cacheKey)
    staffPoolFetchedAt.delete(cacheKey)
  }

  let unsubscribe = () => {}
  onMounted(() => {
    unsubscribe = subscribeLiveSync((payload) => {
      if (!payload.topics.some((topic) => topic === 'hr' || topic === 'system' || topic === 'oa')) return
      invalidateStaffCache()
      if (!staffOptions.value.length || staffLoading.value) return
      searchStaff(lastKeyword.value).catch(() => {})
    })
  })

  onUnmounted(() => {
    unsubscribe()
  })

  return {
    staffOptions,
    staffLoading,
    searchStaff,
    ensureSelectedStaff,
    findStaffName,
    invalidateStaffCache
  }
}
