import axios from 'axios'
import { message } from 'ant-design-vue'
import router from '../router'
import { getToken, clearToken, clearRoles } from './auth'

const request = axios.create({
  baseURL: '/',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      clearToken()
      clearRoles()
      router.push('/login')
      return Promise.reject(error)
    }
    const msg = error?.response?.data?.message || error?.message || '请求失败'
    message.error(msg)
    return Promise.reject(error)
  }
)

export default request
