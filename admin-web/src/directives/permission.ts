import type { Directive } from 'vue'
import { getRoles } from '../utils/auth'
import { hasAnyRole, hasMinisterOrHigher } from '../utils/roleAccess'

export const permission: Directive = {
  mounted(el, binding) {
    const needRoles = Array.isArray(binding.value) ? binding.value : [binding.value]
    const roles = getRoles()
    const needAdmin = needRoles.includes('ADMIN')
    const allowed = needRoles.length === 0
      || hasAnyRole(roles, needRoles)
      || (needAdmin && hasMinisterOrHigher(roles))
    if (!allowed) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}
