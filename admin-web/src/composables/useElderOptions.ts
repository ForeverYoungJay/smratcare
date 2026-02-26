import { ref } from 'vue'
import { getElderPage } from '../api/elder'
import type { ElderItem, PageResult } from '../types'

export interface ElderOption {
  label: string
  value: number
  name: string
}

interface UseElderOptionsConfig {
  pageSize?: number
}

function toElderOption(item: ElderItem): ElderOption {
  const name = item.fullName || `Elder#${item.id}`
  const suffix = item.elderCode ? ` (${item.elderCode})` : ''
  return {
    label: `${name}${suffix}`,
    value: item.id,
    name
  }
}

export function useElderOptions(config: UseElderOptionsConfig = {}) {
  const pageSize = config.pageSize || 50
  const elderOptions = ref<ElderOption[]>([])
  const elderLoading = ref(false)

  async function searchElders(keyword = '') {
    elderLoading.value = true
    try {
      const res: PageResult<ElderItem> = await getElderPage({
        pageNo: 1,
        pageSize,
        keyword: keyword || undefined
      })
      elderOptions.value = (res.list || []).map(toElderOption)
    } finally {
      elderLoading.value = false
    }
  }

  function findElderName(elderId?: number) {
    if (!elderId) return ''
    const selected = elderOptions.value.find((item) => item.value === elderId)
    return selected?.name || ''
  }

  function ensureSelectedElder(elderId?: number, elderName?: string) {
    if (!elderId || elderOptions.value.some((item) => item.value === elderId)) {
      return
    }
    const name = elderName || `Elder#${elderId}`
    elderOptions.value.unshift({
      label: name,
      value: elderId,
      name
    })
  }

  return {
    elderOptions,
    elderLoading,
    searchElders,
    findElderName,
    ensureSelectedElder
  }
}
