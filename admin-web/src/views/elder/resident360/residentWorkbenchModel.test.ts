import { describe, expect, it } from 'vitest'
import type { BedItem, ElderWorkbenchTodoItem } from '../../../types'
import { buildingOccupancy, defaultTodoScope, idleBedCount, todosInScope } from './residentWorkbenchModel'

describe('todo scope', () => {
  const items = [
    { id: 'a', type: 'OUTING_OVERDUE', level: 'urgent', title: 'x', path: '/', mine: false },
    { id: 'b', type: 'CONTRACT_EXPIRING', level: 'high', title: 'y', path: '/', mine: true }
  ] as ElderWorkbenchTodoItem[]

  it('有我负责的待办时默认「我负责的」，否则「全部」', () => {
    expect(defaultTodoScope(items)).toBe('mine')
    expect(defaultTodoScope([items[0]])).toBe('all')
    expect(defaultTodoScope([])).toBe('all')
  })

  it('按范围过滤', () => {
    expect(todosInScope(items, 'mine').map((t) => t.id)).toEqual(['b'])
    expect(todosInScope(items, 'all')).toHaveLength(2)
  })
})

describe('bed helpers', () => {
  const beds = [
    { id: '1', bedNo: 'A1', roomId: '1', building: '松鹤楼', elderId: '9', status: 1 },
    { id: '2', bedNo: 'A2', roomId: '1', building: '松鹤楼', status: 1 },
    { id: '3', bedNo: 'B1', roomId: '2', building: '康宁楼', status: 3 },
    { id: '4', bedNo: 'B2', roomId: '2', building: '康宁楼', status: 1, occupancySource: 'RESERVATION' }
  ] as BedItem[]

  it('按楼栋统计入住率', () => {
    expect(buildingOccupancy(beds)).toEqual([
      { building: '康宁楼', occupied: 0, total: 2, rate: 0 },
      { building: '松鹤楼', occupied: 1, total: 2, rate: 50 }
    ])
  })

  it('空床不含维修与预留', () => {
    expect(idleBedCount(beds)).toBe(1)
  })
})
