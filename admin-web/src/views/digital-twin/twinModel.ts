import type { TwinAsset } from '../../api/digitalTwin'

/**
 * 楼栋在三维园区里的示意摆放（局部坐标，单位不具测绘精度，不代表真实方位）。
 * 只摆已接入楼栋主数据的楼栋；高度 = 主数据楼层数 × 层高。
 */
export interface Footprint { x: number; z: number; w: number; d: number; rot?: number }
const SLOTS: Footprint[] = [
  { x: -62, z: -46, w: 40, d: 14 }, { x: -14, z: -56, w: 40, d: 14 }, { x: 34, z: -50, w: 38, d: 14, rot: -0.12 },
  { x: 72, z: -22, w: 14, d: 38 }, { x: -78, z: -4, w: 14, d: 34 }, { x: -30, z: -2, w: 36, d: 14 },
  { x: 22, z: -2, w: 36, d: 14 }, { x: 70, z: 22, w: 14, d: 34 }, { x: -42, z: 46, w: 46, d: 16 },
  { x: 18, z: 48, w: 34, d: 16 }
]
export const FLOOR_HEIGHT = 3.6
/** 楼层数未录入时的统一高度，避免让人读出层数 */
export const UNKNOWN_HEIGHT = 16
export interface PlacedBuilding extends Footprint { key: string; height: number; real: boolean }

/** 超出 10 个示意位后的楼栋，排在园区南侧的示意行里 */
function extraSlot(i: number): Footprint {
  return { x: -96 + (i % 5) * 48, z: 118 + Math.floor(i / 5) * 30, w: 34, d: 14 }
}

export function placeBuildings(assets: TwinAsset[]): PlacedBuilding[] {
  return assets.filter((a) => a.assetType === 'BUILDING' && a.modelNodeKey && a.buildingName).map((a, i) => {
    const floors = a.floorCount || 0
    return { ...(SLOTS[i] ?? extraSlot(i - SLOTS.length)), key: a.modelNodeKey!, real: true,
      height: floors ? Math.min(floors, 18) * FLOOR_HEIGHT : UNKNOWN_HEIGHT }
  })
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
