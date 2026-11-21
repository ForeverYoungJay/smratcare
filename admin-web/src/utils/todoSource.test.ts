import { describe, expect, it } from 'vitest'
import { resolveTodoSource } from './todoSource'

describe('resolveTodoSource', () => {
  it.each([
    ['[APPROVAL_FLOW:42] 类型=LEAVE', '审批单 #42', '/workbench/approvals', { focusId: '42' }],
    ['[BIRTHDAY_REMINDER:18:2026-10-20] 员工生日', '员工 #18 · 2026-10-20', '/hr/profile/basic', { staffId: '18', autoOpen: '1', from: 'oa-todo' }],
    ['[SOCIAL_SECURITY_FINANCE:27] 待办理', '员工 #27', '/hr/profile/social-security-reminders', { staffId: '27' }],
    ['[SHIFT_DUTY:31] 夜班', '排班 #31', '/workbench/attendance', undefined],
    ['[SHIFT_SWAP_TARGET:36] 待确认', '换班 #36', '/workbench/attendance', undefined]
  ])('resolves %s', (content, sourceNo, path, query) => {
    expect(resolveTodoSource(content)).toMatchObject({ sourceNo, path })
    expect(resolveTodoSource(content)?.query).toEqual(query)
  })

  it('leaves manual todos without a false source reference', () => {
    expect(resolveTodoSource('普通行政待办')).toBeNull()
  })
})
