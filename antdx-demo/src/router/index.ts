import { createRouter, createWebHistory } from 'vue-router';
import Chat from '../pages/Chat.vue';
import Dashboard from '../pages/Dashboard.vue';

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', component: Dashboard },
  { path: '/chat', component: Chat }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
