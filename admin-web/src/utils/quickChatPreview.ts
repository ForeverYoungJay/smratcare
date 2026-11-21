export const QUICK_CHAT_OPEN_EVENT = 'smartcare:open-quick-chat'

export interface QuickChatPreviewItem {
  roomId: string
  roomName: string
  senderName: string
  content: string
  createdAt: string
  unreadCount: number
}
