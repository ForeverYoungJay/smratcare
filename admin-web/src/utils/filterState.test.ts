import { describe, expect, it } from 'vitest'
import { clearFilters, countActiveFilters } from './filterState'

describe('countActiveFilters', () => {
  it('ignores empty, null and whitespace-only values', () => {
    expect(countActiveFilters({ keyword: '', status: undefined, name: null, note: '   ' })).toBe(0)
    expect(countActiveFilters({})).toBe(0)
    expect(countActiveFilters(null)).toBe(0)
    expect(countActiveFilters(undefined)).toBe(0)
  })

  it('counts filled scalars and non-empty arrays', () => {
    expect(countActiveFilters({ keyword: '张', status: 'PENDING', range: [] })).toBe(2)
    expect(countActiveFilters({ range: ['2026-08-01', '2026-08-31'] })).toBe(1)
  })

  it('treats 0 and false as real filter values', () => {
    // 状态码 0、开关 false 都是用户主动选的条件，不能当成“没筛”
    expect(countActiveFilters({ status: 0, onlyMine: false })).toBe(2)
  })

  it('never counts pagination or sorting keys as filters', () => {
    // 页面普遍把 pageNo/pageSize 放在同一个 query 对象里；
    // 它们被计入时，任何一页刚打开就会显示“已启用 2 个筛选条件”
    expect(countActiveFilters({ pageNo: 1, pageSize: 10 })).toBe(0)
    expect(countActiveFilters({ pageNo: 1, pageSize: 10, keyword: '张' })).toBe(1)
    expect(countActiveFilters({ page: 2, size: 20, current: 2, sortField: 'id', sortOrder: 'asc' })).toBe(0)
  })
})

describe('clearFilters', () => {
  it('clears filter values but keeps pagination intact', () => {
    const model: Record<string, any> = { keyword: '张', status: 'PENDING', range: ['a', 'b'], pageNo: 3, pageSize: 20 }
    expect(clearFilters(model)).toBe(3)
    expect(model).toEqual({ keyword: undefined, status: undefined, range: [], pageNo: 3, pageSize: 20 })
  })

  it('reports zero when there is nothing to clear', () => {
    expect(clearFilters({ pageNo: 1, keyword: '' })).toBe(0)
    expect(clearFilters(null)).toBe(0)
  })
})
