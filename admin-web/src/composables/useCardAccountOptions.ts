import { onMounted, onUnmounted, ref } from 'vue'
import { getCardAccountPage } from '../api/card'
import { subscribeLiveSync } from '../utils/liveSync'
import type { CardAccount, PageResult } from '../types'
import { fuzzyScore, toPinyinInitials } from './entitySearch'

export interface CardAccountOption {
  label: string
  value: number
  cardNo: string
  elderName: string
}

interface UseCardAccountOptionsConfig {
  pageSize?: number
  preloadSize?: number
  status?: string
}

const cardAccountPoolCache = new Map<string, CardAccount[]>()
const cardAccountPoolFetchedAt = new Map<string, number>()
const CARD_ACCOUNT_POOL_CACHE_TTL = 90 * 1000

function toCardAccountOption(item: CardAccount): CardAccountOption {
  const elderName = item.elderName || `长者#${item.elderId || '-'}`
  const cardNo = item.cardNo || `CARD#${item.id}`
  return {
    label: `${cardNo} - ${elderName}`,
    value: Number(item.id),
    cardNo,
    elderName
  }
}

function cardAccountSearchText(item: CardAccount) {
  const text = `${item.cardNo || ''} ${item.elderName || ''} ${item.elderId || ''}`.trim()
  return `${text} ${toPinyinInitials(text)}`
}

function dedupeCardAccounts(rows: CardAccount[]) {
  const map = new Map<number, CardAccount>()
  rows.forEach((item) => {
    const id = Number(item.id)
    if (!Number.isFinite(id)) return
    if (!map.has(id)) map.set(id, item)
  })
  return Array.from(map.values())
}

export function useCardAccountOptions(config: UseCardAccountOptionsConfig = {}) {
  const pageSize = config.pageSize || 120
  const preloadSize = config.preloadSize || Math.max(pageSize * 3, 300)
  const status = String(config.status || 'ACTIVE')
  const cacheKey = `card-account:${status || 'all'}`
  const cardAccountOptions = ref<CardAccountOption[]>([])
  const cardAccountLoading = ref(false)
  const lastKeyword = ref('')

  async function loadBasePool(force = false) {
    const lastAt = cardAccountPoolFetchedAt.get(cacheKey) || 0
    const hasFresh = cardAccountPoolCache.has(cacheKey) && (Date.now() - lastAt < CARD_ACCOUNT_POOL_CACHE_TTL)
    if (!force && hasFresh) return cardAccountPoolCache.get(cacheKey) || []
    const page: PageResult<CardAccount> = await getCardAccountPage({ pageNo: 1, pageSize: preloadSize, status })
    const rows = page.list || []
    cardAccountPoolCache.set(cacheKey, rows)
    cardAccountPoolFetchedAt.set(cacheKey, Date.now())
    return rows
  }

  async function searchCardAccounts(keyword = '') {
    cardAccountLoading.value = true
    try {
      lastKeyword.value = String(keyword || '')
      const text = String(keyword || '').trim()
      const baseRows = await loadBasePool(false)
      let mergedRows = [...baseRows]
      if (text) {
        const page: PageResult<CardAccount> = await getCardAccountPage({
          pageNo: 1,
          pageSize: Math.max(pageSize * 2, 120),
          status,
          keyword: text
        })
        mergedRows = dedupeCardAccounts([...(page.list || []), ...baseRows])
      }
      const finalRows = text
        ? mergedRows
          .map((item) => ({ item, score: fuzzyScore(cardAccountSearchText(item), text) }))
          .filter((row) => row.score >= 0)
          .sort((a, b) => b.score - a.score || String(a.item.cardNo || '').localeCompare(String(b.item.cardNo || ''), 'zh-CN'))
          .slice(0, pageSize)
          .map((row) => row.item)
        : mergedRows.slice(0, pageSize)
      cardAccountOptions.value = finalRows.map(toCardAccountOption)
    } finally {
      cardAccountLoading.value = false
    }
  }

  function ensureSelectedCardAccount(cardAccountId?: number, elderName?: string, cardNo?: string) {
    if (!cardAccountId || cardAccountOptions.value.some((item) => item.value === Number(cardAccountId))) return
    const safeCardNo = cardNo || `CARD#${cardAccountId}`
    const safeElderName = elderName || '-'
    cardAccountOptions.value.unshift({
      value: Number(cardAccountId),
      cardNo: safeCardNo,
      elderName: safeElderName,
      label: `${safeCardNo} - ${safeElderName}`
    })
  }

  function invalidateCardAccountCache() {
    cardAccountPoolCache.delete(cacheKey)
    cardAccountPoolFetchedAt.delete(cacheKey)
  }

  let unsubscribe = () => {}
  onMounted(() => {
    unsubscribe = subscribeLiveSync((payload) => {
      if (!payload.topics.some((topic) => topic === 'finance' || topic === 'elder' || topic === 'system')) return
      invalidateCardAccountCache()
      if (!cardAccountOptions.value.length || cardAccountLoading.value) return
      searchCardAccounts(lastKeyword.value).catch(() => {})
    })
  })

  onUnmounted(() => {
    unsubscribe()
  })

  return {
    cardAccountOptions,
    cardAccountLoading,
    searchCardAccounts,
    ensureSelectedCardAccount,
    invalidateCardAccountCache
  }
}
