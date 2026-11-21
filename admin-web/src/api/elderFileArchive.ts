import request from '../utils/request'
import type { Id } from '../types'

export type ElderFileCategory = 'CONTRACT' | 'MEDICAL' | 'CERTIFICATE' | 'OTHER'

export interface ElderFileArchiveItem {
  id: Id
  elderId: Id
  category: ElderFileCategory | string
  fileName: string
  fileUrl: string
  fileSize?: number
  remark?: string
  uploadedAt?: string
}

export function getElderFileArchives(elderId: Id, category?: string) {
  return request.get<ElderFileArchiveItem[]>('/api/elder/file-archive', { params: { elderId, category } })
}

export function createElderFileArchive(data: {
  elderId: Id
  category: string
  fileName: string
  fileUrl: string
  fileSize?: number
  remark?: string
}) {
  return request.post<ElderFileArchiveItem>('/api/elder/file-archive', data)
}

export function deleteElderFileArchive(archiveId: Id) {
  return request.delete<void>(`/api/elder/file-archive/${archiveId}`)
}
