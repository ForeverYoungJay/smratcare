import { describe, expect, it } from 'vitest'
import { roomGrid, roomTone } from './interiorScene'
import type { TwinRoom } from '../../api/digitalTwin'

const room = (bedCount: number, occupiedBeds: number) => ({ roomId: '1', roomNo: '101', bedCount, occupiedBeds, beds: [] }) as TwinRoom

describe('interiorScene', () => {
  it('colours rooms by real occupancy, never guessing', () => {
    expect(roomTone(room(0, 0))).toBe('none')
    expect(roomTone(room(2, 0))).toBe('vacant')
    expect(roomTone(room(2, 1))).toBe('partial')
    expect(roomTone(room(2, 2))).toBe('full')
  })

  it('lays out every room exactly once', () => {
    for (const n of [0, 1, 2, 7, 30]) {
      const { cols, rows } = roomGrid(n)
      expect(cols * rows).toBeGreaterThanOrEqual(n)
      expect(cols * (rows - 1)).toBeLessThan(Math.max(n, 1))
    }
  })
})
