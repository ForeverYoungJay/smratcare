import { createRouter, createWebHistory } from 'vue-router';
import { routes } from './routes';
import { setupPermission } from './permission';
const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior(to, from, savedPosition) {
        if (savedPosition) {
            return savedPosition;
        }
        if (to.path === from.path) {
            return false;
        }
        return { left: 0, top: 0 };
    }
});
setupPermission(router);
export default router;
