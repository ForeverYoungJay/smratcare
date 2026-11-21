import dayjs from 'dayjs'

/** 我的办公：前端小工具（与后端规则保持一致） */

/** 总结类型对应的默认周期：日=当天，周=本周一至周日，月=本月，年=本年 */
export function defaultReportPeriod(reportType: string, today = dayjs()): [string, string] {
  const unit = reportType === 'WEEK' ? 'week' : reportType === 'MONTH' ? 'month' : reportType === 'YEAR' ? 'year' : 'day'
  // dayjs 的 week 从周日开始，按周一为一周第一天计算
  if (unit === 'week') {
    const monday = today.subtract((today.day() + 6) % 7, 'day')
    return [monday.format('YYYY-MM-DD'), monday.add(6, 'day').format('YYYY-MM-DD')]
  }
  return [today.startOf(unit).format('YYYY-MM-DD'), today.endOf(unit).format('YYYY-MM-DD')]
}

/** 把本人工作数据整理成草稿文字；只填入编辑框，由员工确认后再提交 */
export function draftFromStats(stats: { tasksDone?: number | string; todosDone?: number | string; attendanceDays?: number | string }, start: string, end: string) {
  const n = (v?: number | string) => Number(v || 0)
  return [
    `【系统统计 ${start} 至 ${end}，请核对后修改】`,
    `· 完成协同任务 ${n(stats.tasksDone)} 项`,
    `· 完成待办事项 ${n(stats.todosDone)} 项`,
    `· 出勤 ${n(stats.attendanceDays)} 天`
  ].join('\n')
}

/** 与后端一致：至少 8 位，同时包含字母和数字 */
export function passwordProblem(password: string) {
  if (password.length < 8) return '新密码至少 8 位'
  if (!/[A-Za-z]/.test(password) || !/\d/.test(password)) return '新密码需同时包含字母和数字'
  return ''
}

/** 与请假审批表单一致的假别 */
export const LEAVE_TYPES: Record<string, string> = { ANNUAL: '年假', SICK: '病假', PERSONAL: '事假', COMPENSATORY: '调休' }

/** 月历格子：周一开头，前面补空格；每格带当天的考勤记录（没有记录为 undefined） */
export function monthGrid<T extends { date: string }>(month: string, days: T[]) {
  const first = dayjs(`${month}-01`)
  const byDate = new Map(days.map((d) => [d.date, d]))
  const lead = (first.day() + 6) % 7
  const cells: Array<{ date: string; day: number; record?: T } | null> = Array.from({ length: lead }, () => null)
  for (let i = 0; i < first.daysInMonth(); i += 1) {
    const date = first.add(i, 'day').format('YYYY-MM-DD')
    cells.push({ date, day: i + 1, record: byDate.get(date) })
  }
  return cells
}

/** 本月到今天为止的工作日（周一至周五）天数，用作出勤率分母；未来月份为 0 */
export function workdaysSoFar(month: string, today = dayjs()) {
  const first = dayjs(`${month}-01`)
  const last = first.endOf('month').isBefore(today, 'day') ? first.endOf('month') : today
  let count = 0
  for (let d = first; !d.isAfter(last, 'day'); d = d.add(1, 'day')) if (d.day() !== 0 && d.day() !== 6) count += 1
  return count
}

/** 已批准加班申请的总小时数（按起止时间计算，保留一位小数） */
export function overtimeHours(items: Array<{ startTime?: string; endTime?: string }>) {
  const minutes = items.reduce((sum, a) => sum + (a.startTime && a.endTime ? Math.max(dayjs(a.endTime).diff(dayjs(a.startTime), 'minute'), 0) : 0), 0)
  return Math.round(minutes / 6) / 10
}
