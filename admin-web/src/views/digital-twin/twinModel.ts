import type { TwinAsset, TwinDataMode } from '../../api/digitalTwin'

/** 6 个模式共用同一个三维场景；汇总全部只读，数字来自源业务表，可见范围由服务端按岗位决定 */
export const TWIN_MODES = [
  { key: 'campus', label: '园区总览', hint: '楼栋 · 楼层 · 房间' },
  { key: 'occupancy', label: '楼栋与床位', hint: '入住率着色' },
  { key: 'fire', label: '消防与安全', hint: '点位 · 未闭环' },
  { key: 'facility', label: '设备与运维', hint: '报修着色' },
  { key: 'video', label: '视频与巡检', hint: '摄像头台账' },
  { key: 'analytics', label: '运营分析', hint: '经营指标' }
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

/** 列表搜索：按编码、占位名或主数据楼栋名，不区分大小写 */
export function filterAssets(assets: TwinAsset[], keyword: string): TwinAsset[] {
  const q = keyword.trim().toLowerCase()
  if (!q) return assets
  return assets.filter((a) => [a.assetId, a.displayName, a.buildingName ?? ''].some((v) => v.toLowerCase().includes(q)))
}

/** 只有在概念布局或模型里有节点的楼栋才能在三维中定位 */
export function locatableKeys(assets: TwinAsset[], nodeKeys: Iterable<string>): Set<string> {
  const keys = new Set(nodeKeys)
  return new Set(assets.filter((a) => a.modelNodeKey && keys.has(a.modelNodeKey)).map((a) => a.assetId))
}

export const FLOOR_HEIGHT = 3.6
export interface PlacedBuilding extends Footprint { key: string; height: number; real: boolean }

/** 超出 10 个示意位后的楼栋，排在园区南侧的示意行里 */
function extraSlot(i: number): Footprint {
  return { x: -96 + (i % 5) * 48, z: 118 + Math.floor(i / 5) * 30, w: 34, d: 14 }
}

/**
 * 场景楼栋：已绑定楼栋主数据的楼栋依次占用示意位（高度 = 主数据楼层数 × 层高），
 * 剩余示意位才放概念占位楼栋。位置始终是示意，不代表真实方位。
 */
export function placeBuildings(assets: TwinAsset[]): PlacedBuilding[] {
  const buildings = assets.filter((a) => a.assetType === 'BUILDING' && a.modelNodeKey)
  const real = buildings.filter((a) => a.buildingName)
  const slots = Object.values(CONCEPT_LAYOUT)
  const placed: PlacedBuilding[] = real.map((a, i) => {
    const floors = a.floorCount || 0
    return { ...(slots[i] ?? extraSlot(i - slots.length)), key: a.modelNodeKey!, real: true,
      height: floors ? Math.min(floors, 18) * FLOOR_HEIGHT : CONCEPT_HEIGHT }
  })
  const conceptKeys = Object.keys(CONCEPT_LAYOUT).slice(real.length)
  for (const a of buildings) {
    if (!a.buildingName && conceptKeys.includes(a.modelNodeKey!)) {
      placed.push({ ...CONCEPT_LAYOUT[a.modelNodeKey!], key: a.modelNodeKey!, real: false, height: CONCEPT_HEIGHT })
    }
  }
  return placed
}

/** 列表与详情显示的名称：已绑定的用主数据楼栋名，概念占位用临时名 */
export function assetName(a: TwinAsset): string {
  return a.buildingName || a.displayName
}

/** 着色分级：入住率 ≥90 紧张、≥60 正常、其余偏低；报修 0 正常、1-2 关注、≥3 告警 */
export const OVERLAY_LEGEND = {
  occupancy: [{ color: 0x9ccfc4, label: '< 60%' }, { color: 0x2f8f7a, label: '60–90%' }, { color: 0xd9822b, label: '≥ 90%' }],
  facility: [{ color: 0x8fbf8a, label: '无未完成' }, { color: 0xe0b04a, label: '1–2 单' }, { color: 0xd0533f, label: '≥ 3 单' }]
} as const
export const OVERLAY_EMPTY = 0xc9cdd2
export function overlayColor(mode: keyof typeof OVERLAY_LEGEND, value: number | null): number {
  if (value == null) return OVERLAY_EMPTY
  const legend = OVERLAY_LEGEND[mode]
  const level = mode === 'occupancy' ? (value >= 90 ? 2 : value >= 60 ? 1 : 0) : value >= 3 ? 2 : value >= 1 ? 1 : 0
  return legend[level].color
}

export const DATA_MODE_TAG: Record<TwinDataMode, { label: string; color: string }> = {
  DEMO: { label: '演示数据 · 非实测', color: 'orange' },
  LIVE: { label: '真实数据', color: 'green' },
  NOT_CONNECTED: { label: '尚未接入', color: 'default' },
  UNBOUND: { label: '未绑定 · 不显示数字', color: 'default' }
}

export const VERIFICATION_LABEL: Record<string, string> = {
  CONCEPT: '概念示意（待核实）',
  MASTER_DATA: '楼栋主数据',
  UNVERIFIED: '待核实',
  VERIFIED: '已核实',
  SURVEYED: '已测绘'
}

/** 标签只显示短编码，不编造楼栋名称 */
export function shortLabel(assetId: string): string {
  return assetId.replace(/^GF-BLD-/, '')
}

/** SVG 静态兜底平面图的 viewBox（从布局推出，四周留白） */
export function layoutBounds(layout: Record<string, Footprint> | Footprint[], pad = 12) {
  const items = Array.isArray(layout) ? layout : Object.values(layout)
  if (!items.length) return { x: -100, y: -80, width: 200, height: 160 }
  const minX = Math.min(...items.map((f) => f.x - Math.max(f.w, f.d) / 2)) - pad
  const maxX = Math.max(...items.map((f) => f.x + Math.max(f.w, f.d) / 2)) + pad
  const minZ = Math.min(...items.map((f) => f.z - Math.max(f.w, f.d) / 2)) - pad
  const maxZ = Math.max(...items.map((f) => f.z + Math.max(f.w, f.d) / 2)) + pad
  return { x: minX, y: minZ, width: maxX - minX, height: maxZ - minZ }
}
