import request, { fetchPage } from '../utils/request'
import type { Id } from '../types'

export type VideoProvider = 'UNDECIDED' | 'LOCAL' | 'OSS'
export interface VideoCamera {
  id?: Id
  cameraCode: string
  cameraName: string
  location: string
  channelRef: string
  protocol: string
  responsibleName?: string
  enabled: number
  bitrateMbps: number
}
export interface VideoPolicy {
  provider: VideoProvider
  retentionDays: number
  autoCleanup: boolean
  basis?: string
}
export interface VideoRecording {
  id: Id
  cameraId: Id
  provider: VideoProvider
  startedAt: string
  endedAt: string
  expiresAt: string
  sizeBytes: number
  sha256: string
  held: boolean
  holdReason?: string
  status: 'AVAILABLE' | 'DELETED'
}
export interface VideoOverview {
  cameraCount: number
  enabledCount: number
  missingCount: number
  cameraHealth: { cameraId: Id; cameraName: string; state: string; lastRecordedAt?: string }[]
  policy: VideoPolicy
  storageConfigured: boolean
  maxSegmentBytes: number
  estimatedTb: number
  uploadMbps: number
  checkedAt: string
}
const base = '/api/logistics/video'
export const getVideoOverview = () => request.get<VideoOverview>(`${base}/overview`)
export const getVideoCameras = (params: Record<string, unknown>) => fetchPage<VideoCamera>(`${base}/cameras`, params)
export const saveVideoCamera = (data: VideoCamera) => {
  const { id, ...body } = data
  return id ? request.put<VideoCamera>(`${base}/cameras/${id}`, body) : request.post<VideoCamera>(`${base}/cameras`, body)
}
export const saveVideoPolicy = (data: VideoPolicy) => request.put<VideoPolicy>(`${base}/policy`, data)
export const getVideoRecordings = (params: Record<string, unknown>) => fetchPage<VideoRecording>(`${base}/recordings`, params)
export const holdVideoRecording = (id: Id, held: boolean, reason: string) =>
  request.put<VideoRecording>(`${base}/recordings/${id}/hold`, { held, reason })
export const getVideoPlayback = (id: Id) => request.get<{ url: string; provider: VideoProvider }>(`${base}/recordings/${id}/playback`)
export const getVideoContent = (url: string) => request.get<Blob>(url, { responseType: 'blob' })
