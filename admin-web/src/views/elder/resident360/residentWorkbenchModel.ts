import type { BedItem, ElderWorkbenchTodoItem, ElderWorkbenchTodoLevel } from '../../../types'
import { bedStatus } from './bedPanoramaModel'

// 待办由后端 /api/elder/workbench/todos 汇总，这里只做展示映射
export const TODO_LEVEL_META: Record<ElderWorkbenchTodoLevel, { tone: 'danger' | 'warning' | 'info'; text: string }> = {
  urgent: { tone: 'danger', text: '紧急' },
  high: { tone: 'warning', text: '重要' },
  normal: { tone: 'info', text: '一般' }
}

export type TodoScope = 'mine' | 'all'

/** 有我负责的待办时默认看「我负责的」，否则看全部 */
export function defaultTodoScope(items: ElderWorkbenchTodoItem[]): TodoScope {
  return items.some((item) => item.mine) ? 'mine' : 'all'
}

export function todosInScope(items: ElderWorkbenchTodoItem[], scope: TodoScope) {
  return scope === 'mine' ? items.filter((item) => item.mine) : items
}

export type BuildingOccupancy = { building: string; occupied: number; total: number; rate: number }

export function buildingOccupancy(beds: BedItem[]): BuildingOccupancy[] {
  const map = new Map<string, BuildingOccupancy>()
  for (const bed of beds) {
    const building = String(bed.building || '').trim() || '未分配楼栋'
    const row = map.get(building) || { building, occupied: 0, total: 0, rate: 0 }
    row.total += 1
    if (bedStatus(bed) === 'occupied') row.occupied += 1
    map.set(building, row)
  }
  return [...map.values()]
    .map((row) => ({ ...row, rate: row.total ? Math.round((row.occupied / row.total) * 1000) / 10 : 0 }))
    .sort((a, b) => a.building.localeCompare(b.building, 'zh-CN'))
}

export function idleBedCount(beds: BedItem[]) {
  return beds.filter((bed) => bedStatus(bed) === 'idle').length
}
