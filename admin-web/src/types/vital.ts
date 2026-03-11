import type { Id } from './common'

export interface VitalRecordRequest {
  elderId: Id
  type: string
  valueJson: any
  measuredAt: string
  remark?: string
}

export interface VitalRecordItem {
  id: Id
  elderId: Id
  elderName?: string
  type: string
  valueJson: any
  measuredAt: string
  recordedByStaffId?: number
  recordedByStaffName?: string
  abnormalFlag?: boolean
  abnormalReason?: string
}
