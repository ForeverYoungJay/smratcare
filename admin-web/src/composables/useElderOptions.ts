import { onMounted, onUnmounted, ref } from 'vue'
import { getElderPage } from '../api/elder'
import { subscribeLiveSync } from '../utils/liveSync'
import type { ElderItem, Id, PageResult } from '../types'

export interface ElderOption {
  label: string
  value: Id
  name: string
}

interface UseElderOptionsConfig {
  pageSize?: number
  preloadSize?: number
  inHospitalOnly?: boolean
  status?: number
  signedOnly?: boolean
}

function toElderOption(item: ElderItem): ElderOption {
  const fullName = String(item.fullName || '').trim()
  const name = fullName || (item.elderCode ? `长者${item.elderCode}` : '未命名长者')
  const suffix = item.elderCode ? ` (${item.elderCode})` : ''
  return {
    label: `${name}${suffix}`,
    value: String(item.id),
    name
  }
}

const PINYIN_INITIAL_LETTERS = 'ABCDEFGHJKLMNOPQRSTWXYZ'
const PINYIN_INITIAL_BOUNDARY_CHARS = '阿芭擦搭蛾发噶哈讥咔垃妈拿哦啪期然撒塌挖昔压匝'
const elderPoolCache = new Map<string, ElderItem[]>()
const elderPoolFetchedAt = new Map<string, number>()
const ELDER_POOL_CACHE_TTL = 90 * 1000

function normalizeText(text: string) {
  return String(text || '')
    .toLowerCase()
    .replace(/[\s\-_/|（）()]+/g, '')
}

function getChineseInitial(char: string) {
  const first = char.slice(0, 1)
  if (!/[\u4e00-\u9fa5]/.test(first)) return ''
  for (let i = 0; i < PINYIN_INITIAL_BOUNDARY_CHARS.length; i += 1) {
    const current = PINYIN_INITIAL_BOUNDARY_CHARS[i]
    const next = PINYIN_INITIAL_BOUNDARY_CHARS[i + 1]
    if (!next) return PINYIN_INITIAL_LETTERS[i] || ''
    if (first.localeCompare(current, 'zh-CN') >= 0 && first.localeCompare(next, 'zh-CN') < 0) {
      return PINYIN_INITIAL_LETTERS[i] || ''
    }
  }
  return ''
}

function toPinyinInitials(text: string) {
  let result = ''
  const raw = String(text || '')
  for (const char of raw) {
    if (/[a-zA-Z0-9]/.test(char)) {
      result += char.toLowerCase()
      continue
    }
    const initial = getChineseInitial(char)
    if (initial) result += initial.toLowerCase()
  }
  return result
}

function fuzzyScore(text: string, keyword: string) {
  const target = normalizeText(text)
  const query = normalizeText(keyword)
  if (!query) return 0
  if (!target) return -1
  if (target.includes(query)) {
    const start = target.indexOf(query)
    return 200 - start
  }
  let score = 0
  let cursor = 0
  for (const char of query) {
    const index = target.indexOf(char, cursor)
    if (index < 0) return -1
    score += Math.max(8 - (index - cursor), 1)
    cursor = index + 1
  }
  return score
}

function normalizeElderStatus(config: UseElderOptionsConfig) {
  if (typeof config.status === 'number') return config.status
  if (config.inHospitalOnly === false) return undefined
  return 1
}

function normalizeSignedOnly(config: UseElderOptionsConfig) {
  if (typeof config.signedOnly === 'boolean') return config.signedOnly
  if (config.inHospitalOnly === false) return false
  return true
}

function buildCacheKey(config: UseElderOptionsConfig) {
  const status = normalizeElderStatus(config)
  const signedOnly = normalizeSignedOnly(config)
  return `status:${status == null ? 'all' : status}|signedOnly:${signedOnly ? 1 : 0}`
}

function elderSearchText(item: ElderItem) {
  const full = `${item.fullName || ''} ${item.elderCode || ''} ${item.phone || ''} ${item.bedNo || ''} ${item.roomNo || ''}`.trim()
  return `${full} ${toPinyinInitials(full)}`
}

function dedupeElders(rows: ElderItem[]) {
  const map = new Map<string, ElderItem>()
  rows.forEach((row) => {
    const id = String(row.id || '').trim()
    if (!id) return
    if (!map.has(id)) map.set(id, row)
  })
  return Array.from(map.values())
}

function upgradeOptionWithName(option: ElderOption, elderName?: string) {
  const nextName = String(elderName || '').trim()
  if (!nextName) return option
  const currentName = String(option?.name || '').trim()
  const shouldUpgrade = !currentName || currentName === '未命名长者' || /^\d+$/.test(currentName) || currentName.startsWith('长者E')
  if (!shouldUpgrade || currentName === nextName) return option
  return {
    ...option,
    name: nextName,
    label: nextName
  }
}

export function useElderOptions(config: UseElderOptionsConfig = {}) {
  const pageSize = config.pageSize || 80
  const preloadSize = config.preloadSize || Math.max(pageSize * 4, 300)
  const elderOptions = ref<ElderOption[]>([])
  const elderLoading = ref(false)
  const lastKeyword = ref('')
  const cacheKey = buildCacheKey(config)

  function upgradeExistingOptionsWithRows(rows: ElderItem[]) {
    if (!rows?.length || !elderOptions.value.length) return
    const nameMap = new Map<string, string>()
    rows.forEach((row) => {
      const id = String(row.id || '').trim()
      const name = String(row.fullName || '').trim()
      if (!id || !name) return
      if (!nameMap.has(id)) nameMap.set(id, name)
    })
    if (!nameMap.size) return
    elderOptions.value = elderOptions.value.map((item) => {
      const id = String(item.value || '').trim()
      return upgradeOptionWithName(item, nameMap.get(id))
    })
  }

  async function loadBasePool(force = false) {
    const lastAt = elderPoolFetchedAt.get(cacheKey) || 0
    const hasFresh = elderPoolCache.has(cacheKey) && (Date.now() - lastAt < ELDER_POOL_CACHE_TTL)
    if (!force && hasFresh) {
      return elderPoolCache.get(cacheKey) || []
    }
    const status = normalizeElderStatus(config)
    const signedOnly = normalizeSignedOnly(config)
    const page: PageResult<ElderItem> = await getElderPage({
      pageNo: 1,
      pageSize: preloadSize,
      status,
      elderStatus: status,
      signedOnly
    })
    const rows = page.list || []
    upgradeExistingOptionsWithRows(rows)
    elderPoolCache.set(cacheKey, rows)
    elderPoolFetchedAt.set(cacheKey, Date.now())
    return rows
  }

  async function searchElders(keyword = '') {
    elderLoading.value = true
    try {
      lastKeyword.value = String(keyword || '')
      const status = normalizeElderStatus(config)
      const signedOnly = normalizeSignedOnly(config)
      const baseRows = await loadBasePool(false)
      let mergedRows = [...baseRows]
      const text = String(keyword || '').trim()
      if (text) {
        const remotePage: PageResult<ElderItem> = await getElderPage({
          pageNo: 1,
          pageSize: Math.max(pageSize * 2, 120),
          status,
          elderStatus: status,
          signedOnly,
          keyword: text
        })
        mergedRows = dedupeElders([...(remotePage.list || []), ...baseRows])
      }
      const finalRows = text
        ? mergedRows
          .map((item) => ({ item, score: fuzzyScore(elderSearchText(item), text) }))
          .filter((row) => row.score >= 0)
          .sort((a, b) => b.score - a.score || String(a.item.fullName || '').localeCompare(String(b.item.fullName || ''), 'zh-CN'))
          .slice(0, pageSize)
          .map((row) => row.item)
        : mergedRows.slice(0, pageSize)
      elderOptions.value = finalRows.map(toElderOption)
    } finally {
      elderLoading.value = false
    }
  }

  function findElderName(elderId?: Id) {
    if (!elderId) return ''
    const target = String(elderId)
    const selected = elderOptions.value.find((item) => String(item.value) === target)
    if (selected?.name) return selected.name
    for (const rows of elderPoolCache.values()) {
      const matched = rows.find((row) => String(row.id || '').trim() === target)
      const matchedName = String(matched?.fullName || '').trim()
      if (matchedName) return matchedName
    }
    return ''
  }

  function ensureSelectedElder(elderId?: Id, elderName?: string) {
    const targetId = String(elderId || '').trim()
    if (!targetId) {
      return
    }
    const resolvedName = String(elderName || '').trim()
    const existingIndex = elderOptions.value.findIndex((item) => String(item.value) === targetId)
    if (existingIndex >= 0) {
      if (!resolvedName) return
      const current = elderOptions.value[existingIndex]
      const next = upgradeOptionWithName(current, resolvedName)
      if (next === current) return
      elderOptions.value.splice(existingIndex, 1, next)
      return
    }
    const name = resolvedName || '未命名长者'
    elderOptions.value.unshift({ label: name, value: targetId, name })
  }

  function invalidateElderCache() {
    elderPoolCache.delete(cacheKey)
    elderPoolFetchedAt.delete(cacheKey)
  }

  let unsubscribe = () => {}
  onMounted(() => {
    unsubscribe = subscribeLiveSync((payload) => {
      if (!payload.topics.some((topic) => topic === 'elder' || topic === 'lifecycle' || topic === 'bed')) return
      invalidateElderCache()
      if (!elderOptions.value.length || elderLoading.value) return
      searchElders(lastKeyword.value).catch(() => {})
    })
  })

  onUnmounted(() => {
    unsubscribe()
  })

  return {
    elderOptions,
    elderLoading,
    searchElders,
    findElderName,
    ensureSelectedElder,
    invalidateElderCache
  }
}
