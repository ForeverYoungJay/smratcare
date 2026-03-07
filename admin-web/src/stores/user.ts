import { defineStore } from 'pinia'
import {
  getPermissions,
  getRoles,
  getStaffInfo,
  getToken,
  setPermissions,
  setRoles,
  setStaffInfo,
  setToken,
  clearPermissions,
  clearRoles,
  clearStaffInfo,
  clearToken,
  normalizeRoles
} from '../utils/auth'
import type { LoginResponse } from '../types/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    roles: getRoles(),
    permissions: getPermissions(),
    staffInfo: getStaffInfo<LoginResponse['staffInfo']>() as LoginResponse['staffInfo'] | null
  }),
  actions: {
    setStaffProfile(staffInfo: LoginResponse['staffInfo'] | null) {
      this.staffInfo = staffInfo
      if (staffInfo) {
        setStaffInfo(staffInfo)
      } else {
        clearStaffInfo()
      }
    },
    setAuth(payload: LoginResponse) {
      const roles = normalizeRoles(payload.roles || [])
      this.token = payload.token
      this.roles = roles
      this.permissions = payload.permissions || []
      this.setStaffProfile(payload.staffInfo)
      setToken(payload.token)
      setRoles(roles)
      setPermissions(payload.permissions || [])
    },
    clear() {
      this.token = ''
      this.roles = []
      this.permissions = []
      this.setStaffProfile(null)
      clearToken()
      clearRoles()
      clearPermissions()
    }
  }
})
