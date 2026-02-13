export interface VitalRecordRequest {
  elderId: number
  type: string
  valueJson: any
  measuredAt: string
  remark?: string
}

export interface VitalRecordItem {
  id: number
  elderId: number
  elderName?: string
  type: string
  valueJson: any
  measuredAt: string
  recordedByStaffId?: number
  recordedByStaffName?: string
  abnormalFlag?: boolean
  abnormalReason?: string
}
