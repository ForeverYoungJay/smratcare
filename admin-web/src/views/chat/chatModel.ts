/** 协同消息：前端展示规则 */

/** 任务状态取自统一任务（oa_task）：OPEN 未分派/处理中，SUBMITTED 待验收，DONE 已完成 */
export const TASK_STATUS: Record<string, { label: string; color: string }> = {
  OPEN: { label: '处理中', color: 'blue' },
  SUBMITTED: { label: '待验收', color: 'orange' },
  DONE: { label: '已完成', color: 'green' },
  UNKNOWN: { label: '无权查看', color: 'default' }
}

export const PRIORITY: Record<string, { label: string; color: string }> = {
  HIGH: { label: '高', color: 'red' },
  NORMAL: { label: '中', color: 'orange' },
  LOW: { label: '低', color: 'green' }
}

/** 会话头像字：群聊取群名首字，单聊取对方姓名最后一个字 */
export function avatarText(title: string, type: string) {
  const t = (title || '?').trim()
  return type === 'GROUP' ? t.slice(0, 1) : t.slice(-1)
}

/** 发送重试用的客户端消息 ID：同一条消息重发时保持不变，服务端据此去重 */
export function newClientMessageId(now = Date.now(), random = Math.random()) {
  return `c-${now.toString(36)}-${random.toString(36).slice(2, 10)}`
}

/** 合并新拉到的消息：按服务端 ID 去重、保持时间顺序，替换同 clientMessageId 的本地占位 */
export function mergeMessages<T extends { id: string; clientMessageId?: string | null }>(current: T[], incoming: T[]) {
  const confirmedClientIds = new Set(incoming.map((m) => m.clientMessageId).filter(Boolean))
  const kept = current.filter((m) => !(m.id.startsWith('local-') && confirmedClientIds.has(m.clientMessageId)))
  const seen = new Set(kept.map((m) => m.id))
  const merged = [...kept, ...incoming.filter((m) => !seen.has(m.id))]
  // 本地占位（id 以 local- 开头）排在最后
  return merged.sort((a, b) => {
    const la = a.id.startsWith('local-')
    const lb = b.id.startsWith('local-')
    if (la !== lb) return la ? 1 : -1
    return la ? 0 : (BigInt(a.id) < BigInt(b.id) ? -1 : 1)
  })
}

/** 聊天待办统计卡 */
export function taskCounts(rows: { status: string }[]) {
  return {
    all: rows.length,
    open: rows.filter((r) => r.status === 'OPEN').length,
    submitted: rows.filter((r) => r.status === 'SUBMITTED').length,
    done: rows.filter((r) => r.status === 'DONE').length
  }
}
