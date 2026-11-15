/**
 * 查询表单的筛选状态判定。
 *
 * SearchForm 用它显示「已启用 N 个筛选条件」，DataTable 用它区分
 * 「本来就没有数据」和「被筛选条件挡掉了」——这两种空状态对用户的含义完全不同。
 */

/**
 * 页面普遍把分页和排序参数塞在同一个 query 对象里（pageNo/pageSize 几乎每页都有）。
 * 它们不是用户选的筛选条件，计数和清空都必须跳过，
 * 否则任何一页打开就显示「已启用 2 个筛选条件」。
 */
const NON_FILTER_KEYS = new Set([
  'pageno', 'pagesize', 'page', 'size', 'current', 'total',
  'sortfield', 'sortorder', 'order', 'orderby', 'ascending', 'descending'
])

function isFilterKey(key: string): boolean {
  return !NON_FILTER_KEYS.has(key.toLowerCase())
}

export function isActiveFilterValue(value: unknown): boolean {
  if (Array.isArray(value)) return value.length > 0
  if (typeof value === 'string') return value.trim().length > 0
  return value !== undefined && value !== null && value !== ''
}

export function countActiveFilters(model?: Record<string, any> | null): number {
  return Object.entries(model || {})
    .filter(([key, value]) => isFilterKey(key) && isActiveFilterValue(value))
    .length
}

/**
 * 真正清空筛选条件（保留分页/排序）。
 *
 * 注意这和页面上的「清空」按钮不是一回事：那个按钮走 SearchForm 的 onReset，
 * 恢复的是页面初始值，而初始值本身常常就是筛选条件（比如默认三天的日期范围），
 * 所以点了之后筛选条件数量可能纹丝不动。
 *
 * 返回被清掉的条件数量。
 */
export function clearFilters(model?: Record<string, any> | null): number {
  if (!model) return 0
  let cleared = 0
  Object.keys(model).forEach((key) => {
    if (!isFilterKey(key)) return
    if (!isActiveFilterValue(model[key])) return
    model[key] = Array.isArray(model[key]) ? [] : undefined
    cleared += 1
  })
  return cleared
}
