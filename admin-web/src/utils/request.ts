import axios from 'axios'
import { message } from 'ant-design-vue'
import router from '../router'
import { getToken, clearToken, clearRoles, clearPermissions } from './auth'
import type { PageResult } from '../types/common'

const request = axios.create({
  baseURL: '/',
  timeout: 15000
})

function resolveErrorMessage(payload: any, fallback = '请求失败') {
  if (!payload) return fallback
  if (typeof payload === 'string') return payload || fallback
  if (typeof payload?.msg === 'string' && payload.msg) return payload.msg
  if (typeof payload?.message === 'string' && payload.message) return payload.message
  if (typeof payload?.error === 'string' && payload.error) return payload.error
  return fallback
}

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  config.headers = config.headers || {}
  if (!config.headers['Content-Type']) {
    config.headers['Content-Type'] = 'application/json;charset=UTF-8'
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data && typeof data.code !== 'undefined') {
      if (data.code !== 0) {
        message.error(resolveErrorMessage(data))
        return Promise.reject(data)
      }
      return data.data
    }
    return data
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      clearToken()
      clearRoles()
      clearPermissions()
      router.push('/login')
      return Promise.reject(error)
    }
    const payload = error?.response?.data
    message.error(resolveErrorMessage(payload, error?.message || '请求失败'))
    return Promise.reject(error)
  }
)

export default request

export function fetchPage<T>(url: string, params?: Record<string, any>) {
  const merged = { ...(params || {}) }
  if (merged.pageNo != null && merged.page == null) {
    merged.page = merged.pageNo
  }
  if (merged.pageSize != null && merged.size == null) {
    merged.size = merged.pageSize
  }
  if (merged.sortOrder != null && merged.order == null) {
    merged.order = merged.sortOrder
  }
  return request.get<any>(url, { params: merged }).then((res) => {
    if (res && Array.isArray(res.list)) {
      return res as PageResult<T>
    }
    if (res && Array.isArray(res.records)) {
      return {
        list: res.records as T[],
        total: Number(res.total || 0),
        pageNo: Number(res.current || 1),
        pageSize: Number(res.size || params?.pageSize || 10)
      } as PageResult<T>
    }
    if (Array.isArray(res)) {
      return {
        list: res as T[],
        total: res.length,
        pageNo: Number(params?.pageNo || 1),
        pageSize: Number(params?.pageSize || res.length || 10)
      } as PageResult<T>
    }
    return {
      list: [],
      total: 0,
      pageNo: Number(params?.pageNo || 1),
      pageSize: Number(params?.pageSize || 10)
    } as PageResult<T>
  })
}
