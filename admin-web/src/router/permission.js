import { getRoles, getToken } from '../utils/auth';
export function setupPermission(router) {
    router.beforeEach((to, _from, next) => {
        const token = getToken();
        if (to.path !== '/login' && !token) {
            next('/login');
            return;
        }
        if (to.path === '/login' && token) {
            next('/portal');
            return;
        }
        const roles = getRoles();
        const required = to.meta?.roles || [];
        if (required.length > 0 && !required.some((r) => roles.includes(r))) {
            next('/403');
            return;
        }
        next();
    });
    router.afterEach(() => {
        window.scrollTo({ top: 0, left: 0, behavior: 'auto' });
        const content = document.querySelector('.app-content');
        if (content) {
            content.scrollTo({ top: 0, left: 0, behavior: 'auto' });
        }
    });
}
