const PINYIN_INITIAL_LETTERS = 'ABCDEFGHJKLMNOPQRSTWXYZ'
const PINYIN_INITIAL_BOUNDARY_CHARS = '阿芭擦搭蛾发噶哈讥咔垃妈拿哦啪期然撒塌挖昔压匝'

export function normalizeText(text: string) {
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

export function toPinyinInitials(text: string) {
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

export function fuzzyScore(text: string, keyword: string) {
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
