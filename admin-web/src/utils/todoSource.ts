export type TodoSourceReference = {
  label: string
  sourceNo: string
  color: string
  path: string
  query?: Record<string, string>
}

type SourcePattern = {
  pattern: RegExp
  resolve: (match: RegExpMatchArray) => TodoSourceReference
}

const sourcePatterns: SourcePattern[] = [
  {
    pattern: /\[APPROVAL_FLOW:(\d+)\]/,
    resolve: ([, id]) => ({ label: '审批流', sourceNo: `审批单 #${id}`, color: 'blue', path: '/workbench/approvals', query: { focusId: id } })
  },
  {
    pattern: /\[BIRTHDAY_REMINDER:(\d+):([^\]]+)\]/,
    resolve: ([, staffId, date]) => ({
      label: '生日提醒', sourceNo: `员工 #${staffId} · ${date}`, color: 'magenta', path: '/hr/profile/basic',
      query: { staffId, autoOpen: '1', from: 'oa-todo' }
    })
  },
  {
    pattern: /\[SOCIAL_SECURITY_(?:DIRECTOR|FINANCE):(\d+)\]/,
    resolve: ([, staffId]) => ({
      label: '社保事项', sourceNo: `员工 #${staffId}`, color: 'cyan', path: '/hr/profile/social-security-reminders', query: { staffId }
    })
  },
  {
    pattern: /\[SHIFT_DUTY:(\d+)\]/,
    resolve: ([, id]) => ({ label: '值班提醒', sourceNo: `排班 #${id}`, color: 'purple', path: '/workbench/attendance' })
  },
  {
    pattern: /\[SHIFT_SWAP_TARGET:(\d+)\]/,
    resolve: ([, id]) => ({ label: '换班确认', sourceNo: `换班 #${id}`, color: 'purple', path: '/workbench/attendance' })
  }
]

export function resolveTodoSource(content?: string): TodoSourceReference | null {
  const value = String(content || '')
  for (const source of sourcePatterns) {
    const match = value.match(source.pattern)
    if (match) return source.resolve(match)
  }
  return null
}
