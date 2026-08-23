import 'vue-router'
import type { ActionPermission, DataScope, DepartmentCode, RouteAudience, RoleCode } from '../access/policy'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    icon?: string
    hidden?: boolean
    roles?: Array<RoleCode | string>
    permissions?: string[]
    actionPermissions?: ActionPermission[]
    dataScope?: DataScope
    department?: DepartmentCode
    audience?: RouteAudience[]
    navSection?: string
    navOrder?: number
    navPinned?: boolean
    menuGroup?: string
    defaultForRoles?: Array<RoleCode | string>
    legacy?: boolean
    searchable?: boolean
    breadcrumb?: boolean
    activeMenu?: string
  }
}

export {}
