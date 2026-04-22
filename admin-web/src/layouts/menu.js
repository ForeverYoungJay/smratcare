import { routes } from '../router/routes';
import { canAccessPath } from '../utils/routeAccess';
function joinPath(base, path) {
    if (path.startsWith('/'))
        return path;
    if (!base || base === '/')
        return `/${path}`;
    return `${base}/${path}`.replace(/\/+/g, '/');
}
function hasAccess(route, roles, pagePermissions, fullPath) {
    const required = route.meta?.roles || [];
    return canAccessPath(roles, required, fullPath, pagePermissions);
}
function buildMenu(routes, roles, pagePermissions, basePath = '') {
    return (routes
        .filter((r) => !r.meta?.hidden)
        .map((r) => {
        const routePath = joinPath(basePath, r.path || '');
        const selfAccessible = hasAccess(r, roles, pagePermissions, routePath);
        const children = r.children?.length
            ? buildMenu(r.children, roles, pagePermissions, routePath)
            : [];
        if (!selfAccessible && children.length === 0) {
            return null;
        }
        const fullPath = r.children ? undefined : joinPath(basePath, r.path || '');
        const node = {
            key: String(r.name || fullPath || r.path),
            label: String(r.meta?.title || r.name || r.path),
            icon: r.meta?.icon,
            roles: r.meta?.roles
        };
        if (children.length > 0) {
            node.children = children;
        }
        else if (fullPath && selfAccessible) {
            node.path = fullPath;
        }
        return node;
    })
        .filter(Boolean)
        .filter((m) => !m?.children || m.children.length > 0));
}
export function getMenuTree(roles, pagePermissions = []) {
    const layout = routes.find((r) => r.path === '/' && r.children);
    return buildMenu(layout?.children || [], roles, pagePermissions, '');
}
