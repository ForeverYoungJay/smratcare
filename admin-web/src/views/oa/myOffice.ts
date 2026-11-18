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
