import { defineStore } from 'pinia'
import {
  getPagePermissions,
  getPermissions,
  getRoles,
  getStaffInfo,
  getToken,
  setPagePermissions,
  setPermissions,
  setRoles,
  setStaffInfo,
  setToken,
  clearPagePermissions,
  clearPermissions,
  clearRoles,
  clearStaffInfo,
  clearToken,
  normalizeRoles
} from '../utils/auth'
import { normalizePagePermissions } from '../utils/pageAccess'
import type { LoginResponse } from '../types/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    roles: getRoles(),
    permissions: getPermissions(),
    pagePermissions: getPagePermissions(),
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
      const pagePermissions = normalizePagePermissions(payload.pagePermissions || [])
      this.token = payload.token
      this.roles = roles
      this.permissions = payload.permissions || []
      this.pagePermissions = pagePermissions
      this.setStaffProfile(payload.staffInfo)
      setToken(payload.token)
      setRoles(roles)
      setPermissions(payload.permissions || [])
      setPagePermissions(pagePermissions)
    },
    clear() {
      this.token = ''
      this.roles = []
      this.permissions = []
      this.pagePermissions = []
      this.setStaffProfile(null)
      clearToken()
      clearRoles()
      clearPermissions()
      clearPagePermissions()
    }
  }
})
