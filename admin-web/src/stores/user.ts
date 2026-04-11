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
import type { LoginResponse } from '../types/api'
import { getRecommendedPagePermissions, hasStoredPagePermissionsConfig, normalizePagePermissions, parseRoutePermissionsJson } from '../utils/pageAccess'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    roles: getRoles(),
    permissions: getPermissions(),
    pagePermissions: getPagePermissions() || [],
    staffInfo: getStaffInfo<LoginResponse['staffInfo']>() as LoginResponse['staffInfo'] | null
  }),
  actions: {
    resolveEffectivePagePermissions(payload: LoginResponse) {
      const roleSnapshots = payload.rolePagePermissions || []
      if (!roleSnapshots.length) {
        return normalizePagePermissions(payload.pagePermissions || [])
      }
      return normalizePagePermissions(roleSnapshots.flatMap((item) => {
        if (hasStoredPagePermissionsConfig(item.routePermissionsJson)) {
          return parseRoutePermissionsJson(item.routePermissionsJson)
        }
        return getRecommendedPagePermissions(item.roleCode)
      }))
    },
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
      const effectivePagePermissions = this.resolveEffectivePagePermissions(payload)
      this.token = payload.token
      this.roles = roles
      this.permissions = payload.permissions || []
      this.pagePermissions = effectivePagePermissions
      this.setStaffProfile(payload.staffInfo)
      setToken(payload.token)
      setRoles(roles)
      setPermissions(payload.permissions || [])
      setPagePermissions(effectivePagePermissions)
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
