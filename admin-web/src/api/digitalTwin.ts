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
}

export interface TwinMetric { key: string; label: string; value: number | string | null; unit?: string }
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

export interface TwinBinding {
  id: string
  assetId: string
  entityType: string
  externalEntityId: string
  bindingStatus: 'PENDING' | 'ACTIVE' | 'RETIRED'
  verifiedAt?: string | null
  createTime?: string
  remark?: string | null
}

const base = '/api/digital-twin'
export const getTwinScene = () => request.get<TwinScene>(`${base}/scene`)
export const getTwinAssets = () => request.get<TwinAsset[]>(`${base}/assets`)
export const getTwinSummary = (assetId: string, mode: string) =>
  request.get<TwinSummary>(`${base}/assets/${encodeURIComponent(assetId)}/summary`, { params: { mode } })
export const getTwinBindings = (assetId: string) => request.get<TwinBinding[]>(`${base}/admin/bindings`, { params: { assetId } })
export const createTwinBinding = (data: { assetId: string; entityType: 'BUILDING'; externalEntityId: string; remark?: string }) =>
  request.post<TwinBinding>(`${base}/admin/bindings`, data)
export const publishTwinBinding = (id: string) => request.post<TwinBinding>(`${base}/admin/bindings/${id}/publish`)
export const retireTwinBinding = (id: string) => request.post<TwinBinding>(`${base}/admin/bindings/${id}/retire`)
