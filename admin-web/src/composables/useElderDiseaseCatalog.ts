import { ref } from 'vue'
import { getElderDiseases } from '../api/elder'
import { getDiseaseList } from '../api/store'
import type { Id } from '../types'

type DiseaseOption = {
  label: string
  value: number
}

function normalizeDiseaseIds(raw: unknown): number[] {
  if (!Array.isArray(raw)) return []
  return raw
    .map((item) => {
      if (typeof item === 'number') return item
      if (typeof item === 'string' && item.trim()) return Number(item)
      if (item && typeof item === 'object') {
        const diseaseId = Number((item as any).diseaseId ?? (item as any).id)
        return Number.isFinite(diseaseId) ? diseaseId : null
      }
      return null
    })
    .filter((item): item is number => Number.isFinite(item as number) && Number(item) > 0)
}

export function useElderDiseaseCatalog() {
  const diseaseOptions = ref<DiseaseOption[]>([])
  const diseaseNameMap = ref<Record<number, string>>({})
  const catalogLoading = ref(false)
  let catalogPromise: Promise<void> | null = null

  async function ensureDiseaseCatalogLoaded(force = false) {
    if (!force && diseaseOptions.value.length) return
    if (catalogPromise) return catalogPromise
    catalogLoading.value = true
    catalogPromise = (async () => {
      const list = await getDiseaseList()
      diseaseOptions.value = (list || []).map((item) => ({
        label: String(item.diseaseName || item.diseaseCode || `疾病#${item.id}`),
        value: Number(item.id)
      }))
      diseaseNameMap.value = diseaseOptions.value.reduce<Record<number, string>>((acc, item) => {
        acc[item.value] = item.label
        return acc
      }, {})
    })()
    try {
      await catalogPromise
    } finally {
      catalogLoading.value = false
      catalogPromise = null
    }
  }

  async function loadDiseaseState(elderId?: Id) {
    await ensureDiseaseCatalogLoaded()
    if (!elderId) {
      return { ids: [] as number[], labels: [] as string[] }
    }
    const raw = await getElderDiseases(elderId as Id)
    const ids = normalizeDiseaseIds(raw)
    const labels = ids.map((item) => diseaseNameMap.value[item] || `疾病#${item}`)
    return { ids, labels }
  }

  async function loadDiseaseSummaryMap(elderIds: Id[]) {
    await ensureDiseaseCatalogLoaded()
    const uniqueIds = Array.from(new Set((elderIds || []).map((item) => String(item || '')).filter(Boolean)))
    const summaryMap: Record<string, string[]> = {}
    const results = await Promise.allSettled(
      uniqueIds.map(async (elderId) => {
        const state = await loadDiseaseState(elderId)
        return { elderId, labels: state.labels }
      })
    )
    results.forEach((result) => {
      if (result.status !== 'fulfilled') return
      summaryMap[result.value.elderId] = result.value.labels
    })
    return summaryMap
  }

  return {
    diseaseOptions,
    diseaseNameMap,
    catalogLoading,
    ensureDiseaseCatalogLoaded,
    loadDiseaseState,
    loadDiseaseSummaryMap
  }
}
