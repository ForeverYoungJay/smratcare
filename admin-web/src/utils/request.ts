import axios from 'axios'
import { message } from 'ant-design-vue'
import router from '../router'
import { getToken, clearToken, clearRoles, clearPermissions, clearPagePermissions } from './auth'
import type { PageResult } from '../types/common'
import { emitLiveSync, inferLiveSyncTopics } from './liveSync'

type RequestExtraConfig = {
  silentError?: boolean
  silent403?: boolean
  forceErrorToast?: boolean
}

const request = axios.create({
  baseURL: '/',
  timeout: 45000
})

let lastForbiddenToastAt = 0
let lastForbiddenToastKey = ''
const FORBIDDEN_TOAST_COOLDOWN_MS = 2500
const GENERIC_FORBIDDEN_PATTERNS = [
  /access denied/i,
  /无权限访问该资源/,
  /当前账号无该操作权限/
]

/** 后端偶发返回英文原文，已知报文在展示前翻译为中文 */
const KNOWN_ERROR_TRANSLATIONS: Array<{ pattern: RegExp; text: string }> = [
  { pattern: /payment exceeds outstanding amount/i, text: '收款金额超过应收余额，请核对后重新登记' },
  { pattern: /access denied/i, text: '当前账号无该操作权限' },
  { pattern: /bad credentials/i, text: '用户名或密码不正确' },
  { pattern: /^elder not found$/i, text: '未找到对应长者档案，可能已被删除' },
  { pattern: /^(department|role|staff|record|bill) not found$/i, text: '未找到对应数据，可能已被删除' },
  { pattern: /internal server error/i, text: '服务器处理异常，请稍后重试' },
  { pattern: /^request failed with status code \d+$/i, text: '请求失败，请稍后重试' }
]

function translateKnownErrorMessage(text: string) {
  const matched = KNOWN_ERROR_TRANSLATIONS.find((item) => item.pattern.test(text))
  return matched ? matched.text : text
}

function resolveErrorMessage(payload: any, fallback = '请求失败') {
  if (!payload) return fallback
  if (typeof payload === 'string') return translateKnownErrorMessage(payload) || fallback
  if (typeof payload?.msg === 'string' && payload.msg) return translateKnownErrorMessage(payload.msg)
  if (typeof payload?.message === 'string' && payload.message) return translateKnownErrorMessage(payload.message)
  if (typeof payload?.error === 'string' && payload.error) return translateKnownErrorMessage(payload.error)
  return fallback
}

function resolveAxiosErrorMessage(error: any) {
  const rawMessage = String(error?.message || '')
  const isTimeout = error?.code === 'ECONNABORTED' || /timeout/i.test(rawMessage)
  if (isTimeout) {
    return '请求超时，系统正在处理中，请稍后重试'
  }
  if (/network error/i.test(rawMessage)) {
    return '网络异常，请检查网络后重试'
  }
  return resolveErrorMessage(error?.response?.data, rawMessage || '请求失败')
}

function normalizeRedirectPath(path?: string | null) {
  const raw = String(path || '').trim()
  if (!raw.startsWith('/')) return '/portal'
  if (raw === '/login') return '/portal'
  if (raw.startsWith('/login?')) {
    const query = raw.split('?')[1] || ''
    const params = new URLSearchParams(query)
    return normalizeRedirectPath(params.get('redirect'))
  }
  return raw
}

function resolveLoginRedirect(fullPath: string) {
  return {
    path: '/login',
    query: {
      redirect: normalizeRedirectPath(fullPath)
    }
  }
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
    const method = String(response.config?.method || 'GET').toUpperCase()
    const url = String(response.config?.url || '')
    const data = response.data
    if (data && typeof data.code !== 'undefined') {
      if (data.code !== 0) {
        const cfg = (response.config || {}) as RequestExtraConfig
        if (!cfg.silentError) {
          message.error(resolveErrorMessage(data))
        }
        return Promise.reject(data)
      }
      if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
        emitLiveSync({
          topics: inferLiveSyncTopics(url),
          method,
          url,
          at: Date.now()
        })
      }
      return data.data
    }
    if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
      emitLiveSync({
        topics: inferLiveSyncTopics(url),
        method,
        url,
        at: Date.now()
      })
    }
    return data
  },
  (error) => {
    const status = error?.response?.status
    const url = String(error?.config?.url || '')
    const cfg = (error?.config || {}) as RequestExtraConfig
    const method = String(error?.config?.method || 'GET').toUpperCase()
    const rawMessage = String(error?.message || '')
    const isTimeoutOrNetwork = error?.code === 'ECONNABORTED' || /timeout|network error/i.test(rawMessage)
    const silentError = Boolean(cfg.silentError)
    const silent403 = Boolean(cfg.silent403 || cfg.silentError)
    const forceErrorToast = Boolean(cfg.forceErrorToast)
    const isLoginRequest = url.includes('/api/auth/login') || url.includes('/api/auth/family/login')
    if (status === 401) {
      clearToken()
      clearRoles()
      clearPermissions()
      clearPagePermissions()
      const currentFullPath = router.currentRoute.value?.fullPath || '/portal'
      router.replace(resolveLoginRedirect(currentFullPath))
      return Promise.reject(error)
    }
    if (isLoginRequest) {
      return Promise.reject(error)
    }
    if (status === 403) {
      if (silent403) {
        return Promise.reject(error)
      }
      const payload = error?.response?.data
      const rawMessage = resolveErrorMessage(payload, '当前账号无该操作权限（403）')
      const isGenericForbidden = GENERIC_FORBIDDEN_PATTERNS.some((pattern) => pattern.test(rawMessage))
      if (isGenericForbidden) {
        return Promise.reject(error)
      }
      const displayMessage = rawMessage
      const now = Date.now()
      const key = `${url}|${displayMessage}`
      if (key !== lastForbiddenToastKey || now - lastForbiddenToastAt > FORBIDDEN_TOAST_COOLDOWN_MS) {
        message.warning(displayMessage)
        lastForbiddenToastAt = now
        lastForbiddenToastKey = key
      }
      return Promise.reject(error)
    }
    if (silentError) {
      return Promise.reject(error)
    }
    // 自动刷新类 GET 请求在超时/弱网下容易形成提示风暴，默认静默处理
    if (!forceErrorToast && method === 'GET' && isTimeoutOrNetwork) {
      return Promise.reject(error)
    }
    const resolvedMessage = resolveAxiosErrorMessage(error)
    // 调用方常用 error.message 再次提示：将“Request failed with status code xxx”替换为业务信息，避免英文原文二次弹出
    if (error && typeof error === 'object') {
      try {
        error.message = resolvedMessage
      } catch {
        // message 只读时忽略，不影响主流程
      }
    }
    message.error(resolvedMessage)
    return Promise.reject(error)
  }
)

export default request

export function fetchPage<T>(
  url: string,
  params?: Record<string, any>,
  config?: Record<string, any>
) {
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
  return request.get<any>(url, { params: merged, ...(config || {}) }).then((res) => {
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
