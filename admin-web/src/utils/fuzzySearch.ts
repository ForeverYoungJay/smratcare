/**
 * 单个词的匹配分：0 = 不匹配。
 * 开头命中 > 包含 > 按顺序但不连续（"护任" 命中 "护理任务"），越紧凑分越高。
 */
export function fuzzyTokenScore(token: string, text: string) {
  if (!token) return 1
  const index = text.indexOf(token)
  if (index === 0) return 100
  if (index > 0) return 80 - Math.min(index, 30)
  let from = 0
  let first = -1
  for (const ch of token) {
    const found = text.indexOf(ch, from)
    if (found < 0) return 0
    if (first < 0) first = found
    from = found + 1
  }
  const span = from - first
  // 字符散落在长描述里不算命中，否则短词几乎能匹配任何长文本
  if (span > token.length * 3) return 0
  return Math.max(1, Math.round((30 * token.length) / span))
}

/**
 * 模糊过滤并按相关度排序。关键词按空格拆分，每个词都要命中；
 * 标题命中的分数翻倍，所以标题匹配排在只命中描述/别名的前面。同分保持原顺序。
 */
export function fuzzyFilter<T>(items: T[], query: string, fields: (item: T) => { title: string; extra?: string }) {
  const tokens = query.trim().toLowerCase().split(/\s+/).filter(Boolean)
  if (!tokens.length) return items
  return items
    .map((item, order) => {
      const { title, extra = '' } = fields(item)
      const titleText = title.toLowerCase()
      const extraText = extra.toLowerCase()
      let score = 0
      for (const token of tokens) {
        const tokenScore = Math.max(fuzzyTokenScore(token, titleText) * 2, fuzzyTokenScore(token, extraText))
        if (!tokenScore) return null
        score += tokenScore
      }
      return { item, order, score }
    })
    .filter((row): row is { item: T; order: number; score: number } => row !== null)
    .sort((a, b) => b.score - a.score || a.order - b.order)
    .map((row) => row.item)
}
