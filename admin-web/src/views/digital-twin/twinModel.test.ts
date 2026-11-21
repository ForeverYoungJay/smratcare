import { describe, expect, it } from 'vitest'
import type { TwinAsset } from '../../api/digitalTwin'
import { FLOOR_HEIGHT, OVERLAY_EMPTY, OVERLAY_LEGEND, UNKNOWN_HEIGHT, overlayColor, placeBuildings } from './twinModel'

const real = (id: number, floors: number): TwinAsset => ({
  assetId: `GF-BLD-B${id}`, assetType: 'BUILDING', displayName: `楼${id}`, modelNodeKey: `BLD_M_${id}`,
  verificationStatus: 'MASTER_DATA', bound: true, buildingName: `${id}号楼`, floorCount: floors, roomCount: 3
})

describe('digital twin model', () => {
  it('places only master-data buildings, height from real floor count', () => {
    const unbound: TwinAsset = { ...real(9, 4), buildingName: undefined, bound: false }
    const placed = placeBuildings([real(1, 5), real(2, 0), unbound])
    expect(placed.map((p) => p.key)).toEqual(['BLD_M_1', 'BLD_M_2'])
    expect(placed[0].height).toBe(5 * FLOOR_HEIGHT)
    expect(placed[1].height).toBe(UNKNOWN_HEIGHT)
  })

  it('more than ten buildings never overlap', () => {
    const many = placeBuildings(Array.from({ length: 12 }, (_, i) => real(i + 1, 3)))
    expect(many).toHaveLength(12)
    expect(new Set(many.map((p) => `${p.x},${p.z}`)).size).toBe(12)
  })

  it('overlay colours follow thresholds and missing data stays neutral', () => {
    expect(overlayColor('occupancy', null)).toBe(OVERLAY_EMPTY)
    expect(overlayColor('occupancy', 95)).toBe(OVERLAY_LEGEND.occupancy[2].color)
    expect(overlayColor('occupancy', 70)).toBe(OVERLAY_LEGEND.occupancy[1].color)
    expect(overlayColor('facility', 0)).toBe(OVERLAY_LEGEND.facility[0].color)
    expect(overlayColor('facility', 4)).toBe(OVERLAY_LEGEND.facility[2].color)
  })
})
