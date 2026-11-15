export const QUICK_CHAT_OPEN_EVENT = 'smartcare:open-quick-chat'

export interface QuickChatPreviewItem {
  roomId: string
  roomName: string
  senderName: string
  content: string
  createdAt: string
  unreadCount: number
}

function previewContent(message: any) {
  if (message?.recalled) return '消息已撤回'
  if (message?.kind === 'IMAGE') return message.fileName ? `[图片] ${message.fileName}` : '[图片]'
  if (message?.kind === 'FILE') return message.fileName ? `[文件] ${message.fileName}` : '[文件]'
  return String(message?.content || '').trim() || '你收到了一条新消息'
}

export function buildQuickChatPreviews(
  source: string | unknown,
  identityAliases: Array<string | number | undefined>,
  limit = 5
): QuickChatPreviewItem[] {
  try {
    const parsed = typeof source === 'string' ? JSON.parse(source || '{}') : source
    const rooms = Array.isArray(parsed) ? parsed : Array.isArray((parsed as any)?.rooms) ? (parsed as any).rooms : []
    const aliases = new Set(identityAliases.map((item) => String(item ?? '').trim()).filter(Boolean))
    aliases.add('me')

    return rooms
      .filter((room: any) => !Array.from(aliases).some((alias) => room?.archivedByUser?.[alias] === true))
      .map((room: any) => {
        const messages = Array.isArray(room?.messages) ? room.messages : []
        const latest = [...messages].sort((a, b) => Date.parse(b?.createdAt || '') - Date.parse(a?.createdAt || ''))[0]
        if (!latest) return null
        const unreadByUser = Array.from(aliases).reduce(
          (max, alias) => Math.max(max, Number(room?.unreadByUser?.[alias] || 0)),
          0
        )
        const unreadCount = Math.max(0, Math.floor(Math.max(unreadByUser, Number(room?.unreadCount || 0))))
        return {
          roomId: String(room?.id || ''),
          roomName: String(room?.name || '快捷聊天'),
          senderName: String(latest?.senderName || '系统'),
          content: previewContent(latest),
          createdAt: String(latest?.createdAt || room?.updatedAt || ''),
          unreadCount
        }
      })
      .filter((item: QuickChatPreviewItem | null): item is QuickChatPreviewItem => Boolean(item?.roomId))
      .sort((a: QuickChatPreviewItem, b: QuickChatPreviewItem) => Date.parse(b.createdAt) - Date.parse(a.createdAt))
      .slice(0, Math.max(0, limit))
  } catch {
    return []
  }
}

