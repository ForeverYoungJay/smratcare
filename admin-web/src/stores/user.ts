import { defineStore } from 'pinia'
import { getRoles, getToken, setRoles, setToken, clearRoles, clearToken } from '../utils/auth'
import type { LoginResponse } from '../types/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    roles: getRoles(),
    staffInfo: null as LoginResponse['staffInfo'] | null
  }),
  actions: {
    setAuth(payload: LoginResponse) {
      this.token = payload.token
      this.roles = payload.roles || []
      this.staffInfo = payload.staffInfo
      setToken(payload.token)
      setRoles(payload.roles || [])
    },
    clear() {
      this.token = ''
      this.roles = []
      this.staffInfo = null
      clearToken()
      clearRoles()
    }
  }
})
