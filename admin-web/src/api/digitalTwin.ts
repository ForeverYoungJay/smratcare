import request from '../utils/request'

/** 数字孪生中心：只读接口，服务端按登录机构与角色校验；绑定只有 ADMIN/SYS_ADMIN 可配置 */
export type TwinDataMode = 'DEMO' | 'LIVE' | 'NOT_CONNECTED' | 'UNBOUND'

export interface TwinScene {
  version: string
  modelUrl: string | null
  sourceType: string
  verificationStatus: 'CONCEPT' | 'UNVERIFIED' | 'VERIFIED' | 'SURVEYED'
  coordinateNote?: string
  publishedAt?: string
  dataMode: TwinDataMode
  canManageBindings: boolean
  canViewOccupancy: boolean
  /** 当前岗位可看汇总的模式（服务端按角色计算） */
  visibleModes: string[]
}

export interface TwinAsset {
  assetId: string
  assetType: 'CAMPUS' | 'BUILDING' | 'FLOOR' | 'ROOM' | 'BED' | 'DEVICE' | 'AREA'
  displayName: string
  parentAssetId?: string | null
  modelNodeKey?: string | null
  verificationStatus: string
  sourceReference?: string
  verifiedAt?: string | null
  bound: boolean
  /** 已绑定楼栋：以下三项实时读楼栋主数据 */
  buildingName?: string
  floorCount?: number
  roomCount?: number
}

export interface TwinMetric { key: string; label: string; value: number | string | null; unit?: string; tone?: 'warn' | 'danger' }
export interface TwinOverlayItem { assetId: string; value: number | null; text: string }
export interface TwinSummary {
  assetId: string
  mode: string
  dataMode: TwinDataMode
  message?: string
  source?: string
  scope?: string
  updatedAt?: string
  link?: string
  metrics: TwinMetric[]
}


/** 楼栋内部：真实楼层 → 房间 → 床位 → 入住长者（长者信息仅对可查长者档案的岗位返回） */
export interface TwinElder { elderId: string; name: string; gender?: number | null; age?: number | null; careLevel?: string | null; admissionDate?: string | null }
export interface TwinBed { bedId: string; bedNo: string; bedType?: string | null; status: number; elder?: TwinElder }
export interface TwinRoom {
  roomId: string; roomNo: string; roomType?: string | null; orientation?: string | null; capacity?: number | null
  bedCount: number; occupiedBeds: number; beds: TwinBed[]
}
export interface TwinFloor { key: string; floorNo: string; name?: string | null; roomCount: number; bedCount: number; occupiedBeds: number; rooms: TwinRoom[] }
export interface TwinInterior { assetId: string; buildingName: string; canViewElders: boolean; floors: TwinFloor[]; updatedAt: string }

const base = '/api/digital-twin'
export const getTwinScene = () => request.get<TwinScene>(`${base}/scene`)
export const getTwinAssets = () => request.get<TwinAsset[]>(`${base}/assets`)
// 大屏上各岗位看不到的面板会 403，由页面显示「无权查看」，不弹错误提示
export const getTwinSummary = (assetId: string, mode: string) =>
  request.get<TwinSummary>(`${base}/assets/${encodeURIComponent(assetId)}/summary`, { params: { mode }, silent403: true } as Record<string, unknown>)
export const getTwinInterior = (assetId: string) =>
  request.get<TwinInterior>(`${base}/assets/${encodeURIComponent(assetId)}/interior`, { silent403: true } as Record<string, unknown>)
export const getTwinOverlay = (mode: 'occupancy' | 'facility') =>
  request.get<TwinOverlayItem[]>(`${base}/overlay`, { params: { mode }, silent403: true } as Record<string, unknown>)

/** 运营大屏（院长、管理员）：真实业务表只读汇聚；没有数据源的块标 connected=false，不填 0 */
export interface ScreenAlert { type: 'DEVICE' | 'FIRE' | 'MAINTENANCE' | 'HEALTH'; typeLabel: string; title: string; location?: string | null; time?: string | null; link: string }
export interface ScreenTask { key: string; label: string; done: number; total: number | null; link: string }
export interface TwinScreenOverview {
  careLevels: { source: string; link: string; total: number; items: Array<{ name: string; count: number }> }
  alerts: { source: string; openCount: number; items: ScreenAlert[] }
  tasks: { source: string; items: ScreenTask[] }
  workOrders: { source: string; link: string; total: number; completed: number; processing: number; open: number }
  health: { source: string; link: string; measured: number; abnormal: number; abnormalElders: number; hourlyMeasured: number[]; hourlyAbnormal: number[] }
  energy: { source: string; link: string; connected: boolean; month?: string; usageKwh?: number; feeAmount?: number; rooms?: number; byBuilding?: Array<{ name: string; usageKwh: number }> }
  cameras: { source: string; link: string; total: number; enabled: number; items: Array<{ name: string; location?: string | null; enabled: boolean }> }
  updatedAt: string
}
export const getTwinScreenOverview = () =>
  request.get<TwinScreenOverview>(`${base}/screen/overview`, { silent403: true } as Record<string, unknown>)
