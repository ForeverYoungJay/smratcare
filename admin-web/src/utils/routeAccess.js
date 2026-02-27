export function resolveRouteAccess(router, roles, path) {
    const resolved = router.resolve(path);
    if (!resolved.matched.length) {
        return { canAccess: false, requiredRoles: [] };
    }
    const roleGroups = resolved.matched
        .map((record) => (record.meta?.roles || []).filter(Boolean))
        .filter((group) => group.length > 0);
    if (roleGroups.length === 0) {
        return { canAccess: true, requiredRoles: [] };
    }
    const canAccess = roleGroups.every((group) => group.some((role) => roles.includes(role)));
    const requiredRoles = Array.from(new Set(roleGroups.flat()));
    return { canAccess, requiredRoles };
}
