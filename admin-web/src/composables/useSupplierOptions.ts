import { ref } from 'vue'
import { getSupplierPage } from '../api/materialCenter'
import type { MaterialSupplierItem, PageResult } from '../types'
import { fuzzyScore, toPinyinInitials } from './entitySearch'

export interface SupplierOption {
  label: string
  value: number
  name: string
}

interface UseSupplierOptionsConfig {
  pageSize?: number
  preloadSize?: number
  enabledOnly?: boolean
}

const supplierPoolCache = new Map<string, MaterialSupplierItem[]>()
const supplierPoolFetchedAt = new Map<string, number>()
const SUPPLIER_POOL_CACHE_TTL = 120 * 1000

function toSupplierOption(item: MaterialSupplierItem): SupplierOption {
  const name = item.supplierName || `供应商#${item.id}`
  const suffix = item.supplierCode ? ` (${item.supplierCode})` : ''
  return {
    label: `${name}${suffix}`,
    value: item.id,
    name
  }
}

function supplierSearchText(item: MaterialSupplierItem) {
  const text = `${item.supplierName || ''} ${item.supplierCode || ''} ${item.contactName || ''} ${item.contactPhone || ''}`.trim()
  return `${text} ${toPinyinInitials(text)}`
}

function dedupeSuppliers(rows: MaterialSupplierItem[]) {
  const map = new Map<number, MaterialSupplierItem>()
  rows.forEach((item) => {
    if (!map.has(item.id)) map.set(item.id, item)
  })
  return Array.from(map.values())
}

export function useSupplierOptions(config: UseSupplierOptionsConfig = {}) {
  const pageSize = config.pageSize || 120
  const preloadSize = config.preloadSize || 300
  const enabledOnly = config.enabledOnly !== false
  const supplierOptions = ref<SupplierOption[]>([])
  const supplierLoading = ref(false)

  async function loadBasePool(force = false) {
    const cacheKey = `supplier:${enabledOnly ? 'enabled' : 'all'}`
    const lastAt = supplierPoolFetchedAt.get(cacheKey) || 0
    const hasFresh = supplierPoolCache.has(cacheKey) && (Date.now() - lastAt < SUPPLIER_POOL_CACHE_TTL)
    if (!force && hasFresh) return supplierPoolCache.get(cacheKey) || []
    const page: PageResult<MaterialSupplierItem> = await getSupplierPage({ pageNo: 1, pageSize: preloadSize, enabledOnly })
    const rows = page.list || []
    supplierPoolCache.set(cacheKey, rows)
    supplierPoolFetchedAt.set(cacheKey, Date.now())
    return rows
  }

  async function searchSuppliers(keyword = '') {
    supplierLoading.value = true
    try {
      const text = String(keyword || '').trim()
      const baseRows = await loadBasePool(false)
      let mergedRows = [...baseRows]
      if (text) {
        const page: PageResult<MaterialSupplierItem> = await getSupplierPage({
          pageNo: 1,
          pageSize: Math.max(pageSize * 2, 180),
          enabledOnly,
          keyword: text
        })
        mergedRows = dedupeSuppliers([...(page.list || []), ...baseRows])
      }
      const finalRows = text
        ? mergedRows
          .map((item) => ({ item, score: fuzzyScore(supplierSearchText(item), text) }))
          .filter((row) => row.score >= 0)
          .sort((a, b) => b.score - a.score || String(a.item.supplierName || '').localeCompare(String(b.item.supplierName || ''), 'zh-CN'))
          .slice(0, pageSize)
          .map((row) => row.item)
        : mergedRows.slice(0, pageSize)
      supplierOptions.value = finalRows.map(toSupplierOption)
    } finally {
      supplierLoading.value = false
    }
  }

  function ensureSelectedSupplier(supplierId?: number, supplierName?: string) {
    if (!supplierId) return
    if (supplierOptions.value.some((item) => item.value === supplierId)) return
    const name = supplierName || `供应商#${supplierId}`
    supplierOptions.value.unshift({
      label: name,
      value: supplierId,
      name
    })
  }

  return {
    supplierOptions,
    supplierLoading,
    searchSuppliers,
    ensureSelectedSupplier
  }
}
