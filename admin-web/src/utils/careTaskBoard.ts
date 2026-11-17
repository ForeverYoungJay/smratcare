import dayjs from 'dayjs'
import type { CareTaskItem } from '../types'

export type CareTaskView = 'time' | 'resident'

export function groupCareTasks(tasks: CareTaskItem[], view: CareTaskView) {
  const groups = new Map<string, { key: string; title: string; tasks: CareTaskItem[]; pending: number; risk: number }>()
  const periods = ['凌晨 00:00–06:00', '上午 06:00–12:00', '下午 12:00–18:00', '晚间 18:00–24:00', '未安排时间']
  const sorted = [...tasks].sort((a, b) => {
    const left = dayjs(a.planTime), right = dayjs(b.planTime)
    return (left.isValid() ? left.valueOf() : Infinity) - (right.isValid() ? right.valueOf() : Infinity)
  })
  for (const task of sorted) {
    const time = dayjs(task.planTime)
    const period = time.isValid() ? Math.floor(time.hour() / 6) : 4
    const key = view === 'time' ? String(period) : String(task.elderId || `task-${task.taskDailyId}`)
    if (!groups.has(key)) groups.set(key, {
      key, title: view === 'time' ? periods[period] : `${task.elderName || '未关联长者'} · ${task.roomNo || '未分配房间'}`,
      tasks: [], pending: 0, risk: 0
    })
    const group = groups.get(key)!
    group.tasks.push(task)
    if (task.status === 'PENDING' || task.status === 'PROCESSING') group.pending++
    if (task.overdueFlag || task.suspiciousFlag || task.status === 'EXCEPTION') group.risk++
  }
  return [...groups.values()].sort((a, b) => view === 'time' ? Number(a.key) - Number(b.key) : a.title.localeCompare(b.title, 'zh-CN'))
}
