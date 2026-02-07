import request from '../utils/request'
import type { ApiResult, LoginRequest, LoginResponse } from '../types/api'

export function login(data: LoginRequest) {
  return request.post<ApiResult<LoginResponse>>('/api/auth/login', data)
}

export function getMe() {
  return request.get<ApiResult<LoginResponse['staffInfo']>>('/api/auth/me')
}
