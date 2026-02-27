import { getPermissions } from '../utils/auth';
export const permissionCode = {
    mounted(el, binding) {
        const needCodes = Array.isArray(binding.value) ? binding.value : [binding.value];
        const permissions = getPermissions();
        const allowed = needCodes.length === 0 || needCodes.some((p) => permissions.includes(p));
        if (!allowed) {
            el.parentNode && el.parentNode.removeChild(el);
        }
    }
};
