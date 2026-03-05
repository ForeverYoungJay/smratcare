import { ref } from 'vue'
import { getDepartmentPage } from '../api/rbac'
import type { DepartmentItem, PageResult } from '../types'
import { fuzzyScore, toPinyinInitials } from './entitySearch'

export interface DepartmentOption {
  label: string
  value: string
  name: string
}

interface UseDepartmentOptionsConfig {
  pageSize?: number
  preloadSize?: number
}

const departmentPoolCache = new Map<string, DepartmentItem[]>()
const departmentPoolFetchedAt = new Map<string, number>()
const DEPARTMENT_POOL_CACHE_TTL = 120 * 1000

function toDepartmentOption(item: DepartmentItem): DepartmentOption {
  const name = item.deptName || `部门#${item.id}`
  const suffix = item.deptCode ? ` (${item.deptCode})` : ''
  return {
    label: `${name}${suffix}`,
    value: String(item.id),
    name
  }
}

function departmentSearchText(item: DepartmentItem) {
  const text = `${item.deptName || ''} ${item.deptCode || ''}`.trim()
  return `${text} ${toPinyinInitials(text)}`
}

function dedupeDepartments(rows: DepartmentItem[]) {
  const map = new Map<string, DepartmentItem>()
  rows.forEach((item) => {
    const key = String(item.id)
    if (!map.has(key)) map.set(key, item)
  })
  return Array.from(map.values())
}

export function useDepartmentOptions(config: UseDepartmentOptionsConfig = {}) {
  const pageSize = config.pageSize || 120
  const preloadSize = config.preloadSize || 500
  const departmentOptions = ref<DepartmentOption[]>([])
  const departmentLoading = ref(false)

  async function loadBasePool(force = false) {
    const cacheKey = 'department-all'
    const lastAt = departmentPoolFetchedAt.get(cacheKey) || 0
    const hasFresh = departmentPoolCache.has(cacheKey) && (Date.now() - lastAt < DEPARTMENT_POOL_CACHE_TTL)
    if (!force && hasFresh) return departmentPoolCache.get(cacheKey) || []
    const page: PageResult<DepartmentItem> = await getDepartmentPage({ pageNo: 1, pageSize: preloadSize })
    const rows = page.list || []
    departmentPoolCache.set(cacheKey, rows)
    departmentPoolFetchedAt.set(cacheKey, Date.now())
    return rows
  }

  async function searchDepartments(keyword = '') {
    departmentLoading.value = true
    try {
      const text = String(keyword || '').trim()
      const baseRows = await loadBasePool(false)
      let mergedRows = [...baseRows]
      if (text) {
        const page: PageResult<DepartmentItem> = await getDepartmentPage({ pageNo: 1, pageSize: Math.max(pageSize * 2, 200), keyword: text })
        mergedRows = dedupeDepartments([...(page.list || []), ...baseRows])
      }
      const finalRows = text
        ? mergedRows
          .map((item) => ({ item, score: fuzzyScore(departmentSearchText(item), text) }))
          .filter((row) => row.score >= 0)
          .sort((a, b) => b.score - a.score || String(a.item.deptName || '').localeCompare(String(b.item.deptName || ''), 'zh-CN'))
          .slice(0, pageSize)
          .map((row) => row.item)
        : mergedRows.slice(0, pageSize)
      departmentOptions.value = finalRows.map(toDepartmentOption)
    } finally {
      departmentLoading.value = false
    }
  }

  function ensureSelectedDepartment(deptId?: string | number, deptName?: string) {
    if (deptId == null || deptId === '') return
    const value = String(deptId)
    if (departmentOptions.value.some((item) => item.value === value)) return
    const name = deptName || `部门#${value}`
    departmentOptions.value.unshift({
      label: name,
      value,
      name
    })
  }

  return {
    departmentOptions,
    departmentLoading,
    searchDepartments,
    ensureSelectedDepartment
  }
}
