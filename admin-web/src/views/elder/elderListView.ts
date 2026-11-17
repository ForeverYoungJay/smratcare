// 长者列表页的纯逻辑：状态页签、状态胶囊配色、写权限判定。

/** 页签值即后端 ElderLifecycleStatus；undefined 表示全部。 */
export const ELDER_STATUS_TABS: Array<{ value?: string; label: string }> = [
  { value: undefined, label: '全部' },
  { value: 'IN_HOSPITAL', label: '在住' },
  { value: 'INTENT', label: '待入住' },
  { value: 'TRIAL', label: '试住' },
  { value: 'OUTING', label: '外出' },
  { value: 'MEDICAL_OUTING', label: '住院就医' },
  { value: 'DISCHARGE_PENDING', label: '退住中' },
  { value: 'DISCHARGED', label: '已退住' },
  { value: 'DECEASED', label: '已身故' }
]

const PILL_BY_STATUS: Record<string, string> = {
  IN_HOSPITAL: 'rm-pill--success',
  TRIAL: 'rm-pill--success',
  INTENT: 'rm-pill--warning',
  OUTING: 'rm-pill--info',
  MEDICAL_OUTING: 'rm-pill--info',
  DISCHARGE_PENDING: 'rm-pill--danger'
}

/** 在院状态胶囊的修饰类；已退住/身故/未知用默认灰色。 */
export function statusPillClass(lifecycleStatus?: string | null): string {
  return PILL_BY_STATUS[String(lifecycleStatus || '').toUpperCase()] || ''
}

// 与后端 ElderAuthz.canWriteElder 保持一致：没有写权限的角色不展示新建/编辑/删除等入口。
const ELDER_WRITE_ROLES = [
  'HR_EMPLOYEE', 'HR_MINISTER',
  'MEDICAL_EMPLOYEE', 'MEDICAL_MINISTER',
  'NURSING_EMPLOYEE', 'NURSING_MINISTER',
  'DIRECTOR', 'SYS_ADMIN', 'ADMIN'
]

export function canWriteElder(roles: string[] | null | undefined): boolean {
  return (roles || []).some((role) => ELDER_WRITE_ROLES.includes(String(role || '').trim().toUpperCase()))
}

/** 头像用姓氏首字。 */
export function avatarText(fullName?: string | null): string {
  return String(fullName || '').trim().charAt(0) || '?'
}
