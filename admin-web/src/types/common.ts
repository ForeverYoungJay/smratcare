export interface Result<T> {
  code: number
  msg: string
  data: T
}

export type Id = string

export type ApiResult<T> = Result<T>

export interface PageResult<T> {
  list: T[]
  total: number
  pageNo: number
  pageSize: number
}

/** 分页请求通用字段。各业务查询类型继承后再补充自身筛选条件。 */
export interface PageQuery {
  pageNo?: number
  pageSize?: number
  sortField?: string
  sortOrder?: string
}
