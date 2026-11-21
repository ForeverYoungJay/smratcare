import { describe, expect, it } from 'vitest'
import type { TwinAsset } from '../../api/digitalTwin'
import { CONCEPT_HEIGHT, CONCEPT_LAYOUT, DATA_MODE_TAG, FLOOR_HEIGHT, OVERLAY_EMPTY, OVERLAY_LEGEND, TWIN_MODES, filterAssets, layoutBounds, locatableKeys, normalizeMode, overlayColor, placeBuildings, shortLabel } from './twinModel'

const asset = (n: number): TwinAsset => ({
  assetId: `GF-BLD-TMP-${String(n).padStart(3, '0')}`, assetType: 'BUILDING', displayName: `示例楼栋${String(n).padStart(2, '0')}（待核实）`,
  modelNodeKey: `BLD_TMP_${String(n).padStart(3, '0')}`, verificationStatus: 'CONCEPT', bound: false
})

describe('digital twin model', () => {
  it('has six modes and falls back to campus for unknown or tampered mode values', () => {
    expect(TWIN_MODES.map((m) => m.key)).toEqual(['campus', 'occupancy', 'fire', 'facility', 'video', 'analytics'])
    expect(normalizeMode('fire')).toBe('fire')
    expect(normalizeMode('../admin')).toBe('campus')
    expect(normalizeMode(undefined)).toBe('campus')
  })

  it('searches by code or name, case-insensitively', () => {
    const list = [asset(1), asset(2), asset(10)]
    expect(filterAssets(list, ' tmp-010 ').map((a) => a.assetId)).toEqual(['GF-BLD-TMP-010'])
    expect(filterAssets(list, '楼栋02').map((a) => a.assetId)).toEqual(['GF-BLD-TMP-002'])
    expect(filterAssets(list, '')).toHaveLength(3)
  })

  it('locates every seeded concept building in the procedural layout (list ↔ 3D)', () => {
    const seeded = Array.from({ length: 10 }, (_, i) => asset(i + 1))
    expect(locatableKeys(seeded, Object.keys(CONCEPT_LAYOUT)).size).toBe(10)
    expect(locatableKeys([{ ...asset(11) }], Object.keys(CONCEPT_LAYOUT)).size).toBe(0)
  })

  it('labels never invent building names and demo data is always marked', () => {
    expect(shortLabel('GF-BLD-TMP-003')).toBe('TMP-003')
    expect(DATA_MODE_TAG.DEMO.label).toContain('演示数据')
    const b = layoutBounds(CONCEPT_LAYOUT)
    expect(b.width).toBeGreaterThan(0)
    expect(b.height).toBeGreaterThan(0)
  })

  it('real master-data buildings take layout slots first, then concept placeholders fill the rest', () => {
    const real = (id: number, floors: number): TwinAsset => ({
      assetId: `GF-BLD-B${id}`, assetType: 'BUILDING', displayName: `楼${id}`, modelNodeKey: `BLD_M_${id}`,
      verificationStatus: 'MASTER_DATA', bound: true, buildingName: `${id}号楼`, floorCount: floors, roomCount: 3
    })
    const concept = Array.from({ length: 10 }, (_, i) => asset(i + 1))
    const placed = placeBuildings([...concept, real(1, 5), real(2, 0)])
    expect(placed).toHaveLength(10)
    expect(placed[0]).toMatchObject({ key: 'BLD_M_1', real: true, height: 5 * FLOOR_HEIGHT })
    expect(placed[1]).toMatchObject({ key: 'BLD_M_2', height: CONCEPT_HEIGHT })
    // TMP-001、002 的位置被真实楼栋占用，不再摆出来
    expect(placed.map((p) => p.key)).not.toContain('BLD_TMP_001')
    expect(placed.map((p) => p.key)).toContain('BLD_TMP_003')
    // 超过 10 栋的真实楼栋排到额外行，不与示意位重叠
    const many = placeBuildings(Array.from({ length: 12 }, (_, i) => real(i + 1, 3)))
    expect(many).toHaveLength(12)
    expect(new Set(many.map((p) => `${p.x},${p.z}`)).size).toBe(12)
    expect(filterAssets([real(7, 2)], '7号')).toHaveLength(1)
  })

  it('overlay colours follow thresholds and missing data stays neutral', () => {
    expect(overlayColor('occupancy', null)).toBe(OVERLAY_EMPTY)
    expect(overlayColor('occupancy', 95)).toBe(OVERLAY_LEGEND.occupancy[2].color)
    expect(overlayColor('occupancy', 70)).toBe(OVERLAY_LEGEND.occupancy[1].color)
    expect(overlayColor('facility', 0)).toBe(OVERLAY_LEGEND.facility[0].color)
    expect(overlayColor('facility', 4)).toBe(OVERLAY_LEGEND.facility[2].color)
  })
})
