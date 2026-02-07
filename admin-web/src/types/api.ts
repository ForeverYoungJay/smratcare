export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  staffInfo: {
    id: number
    orgId: number
    departmentId: number
    username: string
    realName: string
    phone?: string
    status: number
  }
  roles: string[]
}

export interface PageResult<T> {
  records: T[]
  total: number
}

export interface ElderItem {
  id: number
  fullName: string
  elderCode?: string
  careLevel?: string
  status?: number
}

export interface RoomItem {
  id: number
  roomNo: string
  building?: string
  floorNo?: string
  roomType?: string
  capacity?: number
  status?: number
}

export interface BedItem {
  id: number
  bedNo: string
  roomId: number
  status?: number
  bedQrCode?: string
  elderId?: number
}

export interface CareTaskItem {
  taskDailyId: number
  elderId: number
  elderName: string
  bedId: number
  roomNo: string
  staffId?: number
  taskName: string
  planTime: string
  status: string
}

export interface ProductItem {
  id: number
  productCode?: string
  productName: string
  price?: number
  pointsPrice: number
  safetyStock?: number
  status: number
}

export interface InventoryItem {
  id: number
  productId: number
  quantity: number
  expireDate?: string
}

export interface InventoryAlertItem {
  id: number
  productName: string
  productCode?: string
  alertType: 'LOW' | 'EXPIRY'
  stock: number
  safetyStock: number
  remark?: string
}

export interface OrderItem {
  id: number
  orderNo?: string
  elderId: number
  totalAmount?: number
  payableAmount?: number
  pointsUsed?: number
  orderStatus?: number
  payStatus?: number
  payTime?: string
}

export interface BillItem {
  id: number
  elderId: number
  billMonth?: string
  totalAmount: number
  paidAmount?: number
  outstandingAmount?: number
  status?: number
}

export interface StaffItem {
  id: number
  username: string
  realName: string
  status: number
  departmentId?: number
  staffNo?: string
  phone?: string
  email?: string
  gender?: number
  password?: string
}

export interface RoleItem {
  id: number
  roleName: string
  roleCode: string
  roleDesc?: string
  status?: number
}

export interface DashboardSummary {
  careTasksToday: number
  abnormalTasksToday: number
  inventoryAlerts: number
  unpaidBills: number
}

export interface DepartmentItem {
  id: number
  deptName: string
  orgId?: number
  status?: number
}
