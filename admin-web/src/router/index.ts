import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '../layouts/BasicLayout.vue'
import Login from '../pages/Login.vue'
import Dashboard from '../pages/Dashboard.vue'
import Elder from '../pages/Elder.vue'
import Bed from '../pages/Bed.vue'
import Care from '../pages/Care.vue'
import Product from '../pages/Product.vue'
import Order from '../pages/Order.vue'
import Bill from '../pages/Bill.vue'
import Staff from '../pages/Staff.vue'
import Inventory from '../pages/Inventory.vue'
import Forbidden from '../pages/Forbidden.vue'
import NotFound from '../pages/NotFound.vue'
import { getToken, getRoles } from '../utils/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    { path: '/403', component: Forbidden },
    {
      path: '/',
      component: BasicLayout,
      children: [
        { path: 'dashboard', component: Dashboard },
        { path: 'elder', component: Elder },
        { path: 'bed', component: Bed },
        { path: 'care', component: Care },
        { path: 'store/product', component: Product },
        { path: 'inventory', component: Inventory },
        { path: 'store/order', component: Order },
        { path: 'bill', component: Bill },
        { path: 'admin/staff', component: Staff, meta: { roles: ['ADMIN'] } }
      ]
    },
    { path: '/', redirect: '/dashboard' },
    { path: '/:pathMatch(.*)*', component: NotFound }
  ]
})

router.beforeEach((to, _from, next) => {
  const token = getToken()
  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }
  const roles = getRoles()
  const required = (to.meta?.roles as string[] | undefined) || []
  if (required.length > 0 && !required.some((r) => roles.includes(r))) {
    next('/403')
    return
  }
  if (to.path === '/login' && token) {
    next('/dashboard')
    return
  }
  next()
})

export default router
