import request from '../utils/request'
import type { LoginRequest, LoginResponse } from '../types'

export function login(data: LoginRequest) {
  return request.post<LoginResponse>('/api/auth/login', data)
}

export function logout() {
  return request.post<void>('/api/auth/logout')
}

export function getMe() {
  return request.get<LoginResponse>('/api/auth/me')
}

export function familyLogin(data: { phone: string; code: string }) {
  return request.post<LoginResponse>('/api/auth/family/login', data)
}
