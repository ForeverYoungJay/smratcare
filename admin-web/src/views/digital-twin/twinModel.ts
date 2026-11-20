import type { TwinAsset, TwinDataMode } from '../../api/digitalTwin'

/** 6 个模式共用同一个三维场景；首期只有园区总览完整可用，其余只读或尚未接入 */
export const TWIN_MODES = [
  { key: 'campus', label: '园区总览', ready: true },
  { key: 'occupancy', label: '楼栋与床位', ready: true },
  { key: 'fire', label: '消防与安全', ready: false },
  { key: 'facility', label: '设备与运维', ready: false },
  { key: 'video', label: '视频与巡检', ready: false },
  { key: 'analytics', label: '运营分析', ready: false }
] as const
export type TwinMode = (typeof TWIN_MODES)[number]['key']

export function normalizeMode(value: unknown): TwinMode {
  return TWIN_MODES.some((m) => m.key === value) ? (value as TwinMode) : 'campus'
}

/**
 * 概念示意楼栋的体量与摆放（局部示意坐标，单位不具测绘精度）。
 * 只参考效果图/航拍的「院落围合、坡屋顶」观感，不代表真实楼栋数量、朝向、层数或面积；
 * 高度统一，避免让人读出层数。键是 modelNodeKey，与业务 assetId 分开。
 */
export interface Footprint { x: number; z: number; w: number; d: number; rot?: number }
export const CONCEPT_HEIGHT = 16
export const CONCEPT_LAYOUT: Record<string, Footprint> = {
  BLD_TMP_001: { x: -62, z: -46, w: 40, d: 14 },
  BLD_TMP_002: { x: -14, z: -56, w: 40, d: 14 },
  BLD_TMP_003: { x: 34, z: -50, w: 38, d: 14, rot: -0.12 },
  BLD_TMP_004: { x: 72, z: -22, w: 14, d: 38 },
  BLD_TMP_005: { x: -78, z: -4, w: 14, d: 34 },
  BLD_TMP_006: { x: -30, z: -2, w: 36, d: 14 },
  BLD_TMP_007: { x: 22, z: -2, w: 36, d: 14 },
  BLD_TMP_008: { x: 70, z: 22, w: 14, d: 34 },
  BLD_TMP_009: { x: -42, z: 46, w: 46, d: 16 },
  BLD_TMP_010: { x: 18, z: 48, w: 34, d: 16 }
}

/** 列表搜索：按编码或显示名称，不区分大小写 */
export function filterAssets(assets: TwinAsset[], keyword: string): TwinAsset[] {
  const q = keyword.trim().toLowerCase()
  if (!q) return assets
  return assets.filter((a) => a.assetId.toLowerCase().includes(q) || a.displayName.toLowerCase().includes(q))
}

/** 只有在概念布局或模型里有节点的楼栋才能在三维中定位 */
export function locatableKeys(assets: TwinAsset[], nodeKeys: Iterable<string>): Set<string> {
  const keys = new Set(nodeKeys)
  return new Set(assets.filter((a) => a.modelNodeKey && keys.has(a.modelNodeKey)).map((a) => a.assetId))
}

export const DATA_MODE_TAG: Record<TwinDataMode, { label: string; color: string }> = {
  DEMO: { label: '演示数据 · 非实测', color: 'orange' },
  LIVE: { label: '真实数据', color: 'green' },
  NOT_CONNECTED: { label: '尚未接入', color: 'default' },
  UNBOUND: { label: '未绑定 · 不显示数字', color: 'default' }
}

export const VERIFICATION_LABEL: Record<string, string> = {
  CONCEPT: '概念示意（待核实）',
  UNVERIFIED: '待核实',
  VERIFIED: '已核实',
  SURVEYED: '已测绘'
}

/** 标签只显示短编码，不编造楼栋名称 */
export function shortLabel(assetId: string): string {
  return assetId.replace(/^GF-BLD-/, '')
}

/** SVG 静态兜底平面图的 viewBox（从布局推出，四周留白） */
export function layoutBounds(layout: Record<string, Footprint>, pad = 12) {
  const items = Object.values(layout)
  const minX = Math.min(...items.map((f) => f.x - Math.max(f.w, f.d) / 2)) - pad
  const maxX = Math.max(...items.map((f) => f.x + Math.max(f.w, f.d) / 2)) + pad
  const minZ = Math.min(...items.map((f) => f.z - Math.max(f.w, f.d) / 2)) - pad
  const maxZ = Math.max(...items.map((f) => f.z + Math.max(f.w, f.d) / 2)) + pad
  return { x: minX, y: minZ, width: maxX - minX, height: maxZ - minZ }
}
