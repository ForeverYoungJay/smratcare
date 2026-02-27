import { routes } from '../router/routes';
function joinPath(base, path) {
    if (path.startsWith('/'))
        return path;
    if (!base || base === '/')
        return `/${path}`;
    return `${base}/${path}`.replace(/\/+/g, '/');
}
function hasAccess(route, roles) {
    const required = route.meta?.roles || [];
    if (required.length === 0)
        return true;
    return required.some((r) => roles.includes(r));
}
function buildMenu(routes, roles, basePath = '') {
    return routes
        .filter((r) => !r.meta?.hidden)
        .filter((r) => hasAccess(r, roles))
        .map((r) => {
        const fullPath = r.children ? undefined : joinPath(basePath, r.path || '');
        const node = {
            key: String(r.name || fullPath || r.path),
            label: String(r.meta?.title || r.name || r.path),
            icon: r.meta?.icon,
            roles: r.meta?.roles
        };
        if (r.children && r.children.length > 0) {
            const children = buildMenu(r.children, roles, joinPath(basePath, r.path || ''));
            if (children.length > 0)
                node.children = children;
        }
        else if (fullPath) {
            node.path = fullPath;
        }
        return node;
    })
        .filter((m) => !m.children || m.children.length > 0);
}
export function getMenuTree(roles) {
    const layout = routes.find((r) => r.path === '/' && r.children);
    return buildMenu(layout?.children || [], roles, '');
}
