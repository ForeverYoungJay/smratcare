export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  staffInfo: {
    id: number
    orgId: number
    departmentId: number
    staffNo?: string
    username: string
    realName: string
    phone?: string
    directLeaderId?: number
    indirectLeaderId?: number
    status: number
  }
  roles: string[]
  permissions?: string[]
}
