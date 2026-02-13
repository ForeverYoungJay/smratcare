import request, { fetchPage } from '../utils/request'
import type { NoticeItem } from '../types'

export function getNoticePage(params: any) {
  return fetchPage<NoticeItem>('/api/oa/notice/page', params)
}

export function getNoticeDetail(id: number) {
  return request.get<NoticeItem>(`/api/oa/notice/${id}`)
}

export function createNotice(data: Partial<NoticeItem>) {
  return request.post<void>('/api/oa/notice', data)
}

export function updateNotice(id: number, data: Partial<NoticeItem>) {
  return request.put<void>(`/api/oa/notice/${id}`, data)
}

export function deleteNotice(id: number) {
  return request.delete<void>(`/api/oa/notice/${id}`)
}
