const TOKEN_KEY = 'zhiyangyun_token';
const ROLES_KEY = 'zhiyangyun_roles';
const PERMISSIONS_KEY = 'zhiyangyun_permissions';
export function normalizeRoles(roles) {
    const normalized = new Set();
    (roles || []).forEach((role) => {
        const code = String(role || '').trim().toUpperCase();
        if (code)
            normalized.add(code);
    });
    if (normalized.has('OPERATOR')) {
        normalized.add('MARKETING_EMPLOYEE');
    }
    if (normalized.has('MANAGER')) {
        normalized.add('MARKETING_MINISTER');
    }
    const hasDepartmentRole = Array.from(normalized).some((code) => code.endsWith('_EMPLOYEE') || code.endsWith('_MINISTER'));
    if (hasDepartmentRole) {
        normalized.add('STAFF');
    }
    if (normalized.has('SYS_ADMIN') || normalized.has('DIRECTOR')) {
        normalized.add('ADMIN');
    }
    return Array.from(normalized);
}
export function getToken() {
    return localStorage.getItem(TOKEN_KEY) || '';
}
export function setToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
}
export function clearToken() {
    localStorage.removeItem(TOKEN_KEY);
}
export function getRoles() {
    const raw = localStorage.getItem(ROLES_KEY);
    const roles = raw ? JSON.parse(raw) : [];
    return normalizeRoles(roles);
}
export function setRoles(roles) {
    localStorage.setItem(ROLES_KEY, JSON.stringify(normalizeRoles(roles || [])));
}
export function clearRoles() {
    localStorage.removeItem(ROLES_KEY);
}
export function getPermissions() {
    const raw = localStorage.getItem(PERMISSIONS_KEY);
    return raw ? JSON.parse(raw) : [];
}
export function setPermissions(permissions) {
    localStorage.setItem(PERMISSIONS_KEY, JSON.stringify(permissions || []));
}
export function clearPermissions() {
    localStorage.removeItem(PERMISSIONS_KEY);
}
