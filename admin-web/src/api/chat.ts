import request from '../utils/request'

/** 协同消息：服务端会话；所有访问都按当前登录员工的成员身份校验 */
export interface ChatConversation {
  id: string
  type: 'DIRECT' | 'GROUP'
  title: string
  lastMessagePreview?: string
  lastMessageAt?: string
  unread: number | string
  muted?: boolean
  myRole?: string
}
export interface ChatTaskCard { taskId: string; title?: string; status: string; assigneeName?: string; endTime?: string }
export interface ChatMessage {
  id: string
  conversationId: string
  senderId: string
  senderName?: string
  type: 'TEXT' | 'SYSTEM' | 'TASK_CARD'
  body?: string | null
  recalled: boolean
  replyToId?: string | null
  clientMessageId?: string | null
  createTime: string
  linkedTaskCount: number | string
  task?: ChatTaskCard
  /** 仅前端：发送中 / 发送失败 */
  pending?: 'sending' | 'failed'
}
export interface ChatMember { staffId: string; name: string; role: string }
export interface ChatTaskRow {
  taskId: string
  title: string
  status: string
  priority?: string
  assigneeName?: string
  planCategory?: string
  endTime?: string
  updateTime?: string
  mine: boolean
  created: boolean
  conversationId?: string | null
  conversationTitle?: string
  messageId?: string | null
}

const base = '/api/chat'
export const getConversations = () => request.get<ChatConversation[]>(`${base}/conversations`)
export const createConversation = (data: { type: 'DIRECT' | 'GROUP'; title?: string; memberIds: string[] }) =>
  request.post<{ id: string }>(`${base}/conversations`, data)
export const getMessages = (id: string, params: { beforeId?: string; afterId?: string; limit?: number } = {}) =>
  request.get<ChatMessage[]>(`${base}/conversations/${id}/messages`, { params })
export const sendMessage = (id: string, data: { body: string; clientMessageId: string; replyToId?: string }) =>
  request.post<ChatMessage>(`${base}/conversations/${id}/messages`, data)
export const markRead = (id: string, messageId: string) => request.post<void>(`${base}/conversations/${id}/read`, { messageId })
export const getMembers = (id: string) => request.get<ChatMember[]>(`${base}/conversations/${id}/members`)
export const manageMembers = (id: string, data: { add?: string[]; remove?: string[] }) => request.post<void>(`${base}/conversations/${id}/members`, data)
export const recallMessage = (messageId: string) => request.post<void>(`${base}/messages/${messageId}/recall`)
export const searchMessages = (q: string) => request.get<ChatMessage[]>(`${base}/search`, { params: { q } })
export const convertToTask = (messageId: string, data: { title: string; assigneeId: string; endTime?: string; priority?: string; description?: string; planCategory?: string; confirmDuplicate?: boolean }) =>
  request.post<{ duplicate: boolean; existingTaskIds?: string[]; taskId?: string; message?: ChatMessage }>(`${base}/messages/${messageId}/convert-to-task`, data)
export const getChatTasks = (params: { view?: string; status?: string } = {}) => request.get<ChatTaskRow[]>(`${base}/tasks`, { params })
