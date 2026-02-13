export interface Result<T> {
  code: number
  msg: string
  data: T
}

export type ApiResult<T> = Result<T>

export interface PageResult<T> {
  list: T[]
  total: number
  pageNo: number
  pageSize: number
}
