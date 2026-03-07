import { DEPARTMENT_EMPLOYEE_ROLES, DEPARTMENT_MINISTER_ROLES, SUPER_ROLES } from './roleAccess'

const TOP_SUPER_ROLES = [...SUPER_ROLES, 'DEAN']

type RoleLike = { roleCodes?: string[] }
type CandidateLike = { id?: string | number; departmentId?: string | number; roleCodes?: string[] }
type TargetLike = { id?: string | number; departmentId?: string | number; roleCodes?: string[] }

function toRoleSet(roleCodes?: string[]) {
  return new Set((roleCodes || []).map((item) => String(item || '').trim().toUpperCase()).filter(Boolean))
}

export function hasAnyRoleCode(roleCodes: string[] | undefined, required: string[]) {
  const set = toRoleSet(roleCodes)
  return required.some((role) => set.has(String(role || '').toUpperCase()))
}

export function isDepartmentMinister(roleCodes?: string[]) {
  return hasAnyRoleCode(roleCodes, DEPARTMENT_MINISTER_ROLES)
}

export function isDepartmentEmployee(roleCodes?: string[]) {
  return hasAnyRoleCode(roleCodes, DEPARTMENT_EMPLOYEE_ROLES)
}

export function isSuperManager(roleCodes?: string[]) {
  return hasAnyRoleCode(roleCodes, TOP_SUPER_ROLES)
}

export function supervisionLevel(roleCodes?: string[]) {
  if (isSuperManager(roleCodes)) return 'SUPER'
  if (isDepartmentMinister(roleCodes)) return 'MINISTER'
  if (isDepartmentEmployee(roleCodes)) return 'EMPLOYEE'
  return 'UNKNOWN'
}

export function canBeDirectLeader(target: TargetLike, candidate: CandidateLike) {
  const targetId = String(target.id || '')
  const candidateId = String(candidate.id || '')
  if (!candidateId || targetId === candidateId) return false
  const level = supervisionLevel(target.roleCodes)
  if (level === 'EMPLOYEE') {
    if (isDepartmentMinister(candidate.roleCodes) && String(candidate.departmentId || '') === String(target.departmentId || '')) {
      return true
    }
    return false
  }
  if (level === 'MINISTER') {
    return isSuperManager(candidate.roleCodes)
  }
  if (level === 'SUPER') {
    return isSuperManager(candidate.roleCodes)
  }
  if (isDepartmentMinister(candidate.roleCodes) && String(candidate.departmentId || '') === String(target.departmentId || '')) {
    return true
  }
  return isSuperManager(candidate.roleCodes)
}

export function canBeIndirectLeader(target: TargetLike, candidate: CandidateLike) {
  const targetId = String(target.id || '')
  const candidateId = String(candidate.id || '')
  if (!candidateId || targetId === candidateId) return false
  const level = supervisionLevel(target.roleCodes)
  if (level === 'EMPLOYEE' || level === 'MINISTER') {
    return isSuperManager(candidate.roleCodes)
  }
  if (level === 'SUPER') {
    return isSuperManager(candidate.roleCodes)
  }
  return isSuperManager(candidate.roleCodes)
}

export function ensureSupervisorOrder(directLeaderId?: string | number, indirectLeaderId?: string | number) {
  const direct = String(directLeaderId || '')
  const indirect = String(indirectLeaderId || '')
  if (!direct || !indirect) return true
  return direct !== indirect
}

export function mergeRoleCodes(...items: Array<RoleLike | undefined | null>) {
  const set = new Set<string>()
  items.forEach((item) => {
    ;(item?.roleCodes || []).forEach((code) => {
      const text = String(code || '').trim().toUpperCase()
      if (text) set.add(text)
    })
  })
  return Array.from(set)
}
