import type { PageResult } from '../types'

export function toPageResult<T>(list: T[], pageNo = 1, pageSize = 10): PageResult<T> {
  return { list, total: list.length, pageNo, pageSize }
}
