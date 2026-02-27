const TOKEN_KEY = 'zhiyangyun_token';
const ROLES_KEY = 'zhiyangyun_roles';
const PERMISSIONS_KEY = 'zhiyangyun_permissions';
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
    return raw ? JSON.parse(raw) : [];
}
export function setRoles(roles) {
    localStorage.setItem(ROLES_KEY, JSON.stringify(roles || []));
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
