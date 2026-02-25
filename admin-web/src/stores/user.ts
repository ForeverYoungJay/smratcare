import { defineStore } from 'pinia'
import { getPermissions, getRoles, getToken, setPermissions, setRoles, setToken, clearPermissions, clearRoles, clearToken } from '../utils/auth'
import type { LoginResponse } from '../types/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    roles: getRoles(),
    permissions: getPermissions(),
    staffInfo: null as LoginResponse['staffInfo'] | null
  }),
  actions: {
    setAuth(payload: LoginResponse) {
      this.token = payload.token
      this.roles = payload.roles || []
      this.permissions = payload.permissions || []
      this.staffInfo = payload.staffInfo
      setToken(payload.token)
      setRoles(payload.roles || [])
      setPermissions(payload.permissions || [])
    },
    clear() {
      this.token = ''
      this.roles = []
      this.permissions = []
      this.staffInfo = null
      clearToken()
      clearRoles()
      clearPermissions()
    }
  }
})
