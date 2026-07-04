import type { Id } from './common'

export interface EmrRecord {
  id: Id
  elderId: Id
  recordType?: string
  visitDate?: string
  chiefComplaint?: string
  presentIllness?: string
  pastHistory?: string
  physicalExam?: string
  diagnosis?: string
  treatmentPlan?: string
  doctorId?: Id
  doctorName?: string
  status?: string
  remark?: string
  createTime?: string
}
