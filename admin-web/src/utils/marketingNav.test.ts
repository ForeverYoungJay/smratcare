import { describe, expect, it } from 'vitest'
import { buildFollowupRoute, followupEntryFromPath } from './marketingNav'

describe('marketing follow-up navigation', () => {
  it('keeps filters when switching the in-page view', () => {
    expect(buildFollowupRoute('overdue', { source: '社区', marketerName: '张三' })).toEqual({
      path: '/marketing/interactions/overdue',
      query: { source: '社区', marketerName: '张三' }
    })
  })

  it('recognizes legacy follow-up routes and safely defaults to today', () => {
    expect(followupEntryFromPath('/marketing/interactions/due')).toBe('due')
    expect(followupEntryFromPath('/marketing/interactions/overdue?source=社区')).toBe('overdue')
    expect(followupEntryFromPath('/marketing/interactions/unknown')).toBe('today')
  })
})
