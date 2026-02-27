import { getRoles } from '../utils/auth';
export const permission = {
    mounted(el, binding) {
        const needRoles = Array.isArray(binding.value) ? binding.value : [binding.value];
        const roles = getRoles();
        const allowed = needRoles.length === 0 || needRoles.some((r) => roles.includes(r));
        if (!allowed) {
            el.parentNode && el.parentNode.removeChild(el);
        }
    }
};
