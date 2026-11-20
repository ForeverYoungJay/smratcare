import { describe, expect, it } from 'vitest'
import type { TwinAsset } from '../../api/digitalTwin'
import { CONCEPT_LAYOUT, DATA_MODE_TAG, TWIN_MODES, filterAssets, layoutBounds, locatableKeys, normalizeMode, shortLabel } from './twinModel'

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
})
