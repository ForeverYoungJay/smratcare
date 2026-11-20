import { describe, expect, it } from 'vitest'
import { avatarText, mergeMessages, newClientMessageId, taskCounts } from './chatModel'

describe('chat model', () => {
  it('merges fetched messages by id and replaces the local placeholder of the same client id', () => {
    const local = { id: 'local-1', clientMessageId: 'c-1' }
    const old = { id: '100', clientMessageId: null }
    const confirmed = { id: '101', clientMessageId: 'c-1' }
    expect(mergeMessages([old, local], [confirmed, old]).map((m) => m.id)).toEqual(['100', '101'])
    // 大数 ID（雪花 ID）按数值排序
    expect(mergeMessages([{ id: '2122940666840449025' }], [{ id: '999' }]).map((m) => m.id)).toEqual(['999', '2122940666840449025'])
  })

  it('keeps client message ids stable per send and unique across sends', () => {
    expect(newClientMessageId(1, 0.5)).toBe(newClientMessageId(1, 0.5))
    expect(newClientMessageId(1, 0.5)).not.toBe(newClientMessageId(2, 0.5))
  })

  it('counts tasks by unified status and picks avatar text', () => {
    expect(taskCounts([{ status: 'OPEN' }, { status: 'SUBMITTED' }, { status: 'DONE' }, { status: 'OPEN' }])).toEqual({ all: 4, open: 2, submitted: 1, done: 1 })
    expect(avatarText('护理部工作协同群', 'GROUP')).toBe('护')
    expect(avatarText('李雅静', 'DIRECT')).toBe('静')
  })
})
