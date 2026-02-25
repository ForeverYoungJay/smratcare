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
    username: string
    realName: string
    phone?: string
    status: number
  }
  roles: string[]
  permissions?: string[]
}
