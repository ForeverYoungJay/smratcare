import { createRouter, createWebHistory } from 'vue-router';
import { routes } from './routes';
import { setupPermission } from './permission';
const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior() {
        return { left: 0, top: 0 };
    }
});
setupPermission(router);
export default router;
