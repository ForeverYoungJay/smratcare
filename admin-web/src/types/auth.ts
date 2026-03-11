import type { Id } from './common'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  staffInfo: {
    id: Id
    orgId: Id
    departmentId: number
    staffNo?: string
    username: string
    realName: string
    phone?: string
    directLeaderId?: Id
    indirectLeaderId?: Id
    status: number
  }
  roles: string[]
  permissions?: string[]
}
