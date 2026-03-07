#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo "== RBAC 菜单矩阵导出 =="
echo

node --input-type=module <<'NODE'
import { routes } from './admin-web/src/router/routes.js'
import { hasRouteAccess } from './admin-web/src/utils/roleAccess.js'

const roleCases = [
  { name: '护理员工', roles: ['NURSING_EMPLOYEE'] },
  { name: '护理部长', roles: ['NURSING_MINISTER'] },
  { name: '医务员工', roles: ['MEDICAL_EMPLOYEE'] },
  { name: '医务部长', roles: ['MEDICAL_MINISTER'] },
  { name: '财务员工', roles: ['FINANCE_EMPLOYEE'] },
  { name: '财务部长', roles: ['FINANCE_MINISTER'] },
  { name: '后勤员工', roles: ['LOGISTICS_EMPLOYEE'] },
  { name: '后勤部长', roles: ['LOGISTICS_MINISTER'] },
  { name: '市场员工', roles: ['MARKETING_EMPLOYEE'] },
  { name: '市场部长', roles: ['MARKETING_MINISTER'] },
  { name: '人事员工', roles: ['HR_EMPLOYEE'] },
  { name: '人事部长', roles: ['HR_MINISTER'] },
  { name: '院长管理层', roles: ['DIRECTOR'] },
  { name: '系统超管', roles: ['SYS_ADMIN'] }
]

const root = routes.find((item) => item.path === '/')
const topModules = (root?.children || [])
  .filter((item) => !item?.meta?.hidden)
  .map((item) => {
    const path = `/${String(item.path || '').replace(/^\/+/, '')}`
    const required = Array.isArray(item?.meta?.roles) ? item.meta.roles : []
    return {
      title: String(item?.meta?.title || item?.name || item.path || '未知模块'),
      path,
      required
    }
  })

let lines = []
lines.push('| 角色 | 可访问一级菜单 |')
lines.push('| --- | --- |')

for (const roleCase of roleCases) {
  const allowed = topModules
    .filter((menu) => hasRouteAccess(roleCase.roles, menu.required, menu.path))
    .map((menu) => menu.title)
  lines.push(`| ${roleCase.name} | ${allowed.join('、') || '（无）'} |`)
}

console.log(lines.join('\n'))
NODE
