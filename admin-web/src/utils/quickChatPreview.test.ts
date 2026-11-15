import { describe, expect, it } from 'vitest'
import { buildQuickChatPreviews } from './quickChatPreview'

describe('buildQuickChatPreviews', () => {
  it('returns recent active conversations with the current user unread count', () => {
    const previews = buildQuickChatPreviews({ rooms: [
      {
        id: 'older', name: '护理部', updatedAt: '2026-10-21T08:00:00Z', unreadCount: 9,
        unreadByUser: { '12': 1 }, messages: [{ senderName: '王主任', content: '交班已完成', createdAt: '2026-10-21T08:00:00Z' }]
      },
      {
        id: 'newer', name: '系统消息', updatedAt: '2026-10-22T08:00:00Z', unreadCount: 0,
        unreadByUser: { '12': 2 }, messages: [{ senderName: '系统', kind: 'FILE', fileName: '排班表.xlsx', createdAt: '2026-10-22T08:00:00Z' }]
      },
      {
        id: 'archived', archivedByUser: { '12': true },
        messages: [{ content: '不应展示', createdAt: '2026-10-23T08:00:00Z' }]
      }
    ] }, ['12'], 5)

    expect(previews.map((item) => item.roomId)).toEqual(['newer', 'older'])
    expect(previews[0]).toMatchObject({ content: '[文件] 排班表.xlsx', unreadCount: 2 })
  })

  it('returns an empty list for invalid persisted state', () => {
    expect(buildQuickChatPreviews('{broken', ['12'])).toEqual([])
  })
})
