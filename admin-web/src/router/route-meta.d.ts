import 'vue-router'
import type { ActionPermission, DataScope, DepartmentCode, RouteAudience, RoleCode } from '../access/policy'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    icon?: string
    hidden?: boolean
    roles?: Array<RoleCode | string>
    permissions?: string[]
    /**
     * 面向所有登录用户开放的通用页面（功能地图、无权限提示页等）。
     * 这类页面不设角色/权限限制，也不应被后端下发的 pagePermissions 白名单挡住——
     * 否则后端漏播种一条路径，页面就永久 403。
     */
    publicForAuthenticated?: boolean
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
