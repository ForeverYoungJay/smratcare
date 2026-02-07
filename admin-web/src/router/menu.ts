export interface MenuItem {
  key: string
  label: string
  path: string
  roles?: string[]
}

export const menuItems: MenuItem[] = [
  { key: 'dashboard', label: 'Dashboard', path: '/dashboard' },
  { key: 'elder', label: '老人管理', path: '/elder' },
  { key: 'bed', label: '床位房态', path: '/bed' },
  { key: 'care', label: '护理任务', path: '/care' },
  { key: 'product', label: '商品管理', path: '/store/product' },
  { key: 'inventory', label: '库存预警', path: '/inventory' },
  { key: 'order', label: '订单管理', path: '/store/order' },
  { key: 'bill', label: '月账单', path: '/bill' },
  { key: 'staff', label: '员工管理', path: '/admin/staff', roles: ['ADMIN'] }
]
