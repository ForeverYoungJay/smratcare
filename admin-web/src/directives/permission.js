import { getRoles } from '../utils/auth';
import { hasAnyRole } from '../utils/roleAccess';
export const permission = {
    mounted(el, binding) {
        const needRoles = (Array.isArray(binding.value) ? binding.value : [binding.value]).filter(Boolean);
        const roles = getRoles();
        const allowed = needRoles.length === 0
            || hasAnyRole(roles, needRoles);
        if (!allowed) {
            el.parentNode && el.parentNode.removeChild(el);
        }
    }
};
