export interface ElderItem {
  id: number
  fullName: string
  idCard?: string
  idCardNo?: string
  phone?: string
  elderCode?: string
  gender?: number
  birthDate?: string
  admissionDate?: string
  careLevel?: string
  status?: number
  bedId?: number
  bedStartDate?: string
  bedNo?: string
  roomNo?: string
  checkInDate?: string
  diseases?: ElderDiseaseItem[]
  remark?: string
}

export interface ElderCreateRequest {
  fullName: string
  idCard?: string
  idCardNo?: string
  phone?: string
  elderCode?: string
  gender?: number
  birthDate?: string
  admissionDate?: string
  careLevel?: string
  status?: number
  remark?: string
  bedId?: number
  bedStartDate?: string
}

export interface ElderAssignBedRequest {
  bedId: number
  startDate: string
}

export interface ElderUnbindRequest {
  endDate?: string
  reason?: string
}

export interface ElderDiseaseItem {
  id: number
  diseaseId: number
  diseaseName?: string
}

export interface ElderDiseaseUpdateRequest {
  diseaseIds: number[]
}

export interface FamilyBindingItem {
  id: number
  familyUserId: number
  familyName?: string
  phone?: string
  relation?: string
  isPrimary?: boolean
}

export interface FamilyUserItem {
  id: number
  realName?: string
  phone?: string
  idCardNo?: string
  status?: number
  elderCount?: number
}

export interface FamilyRelationItem {
  id: number
  familyUserId: number
  realName?: string
  phone?: string
  relation?: string
  isPrimary?: boolean
}

export interface FamilyBindRequest {
  familyUserId: number
  elderId: number
  relation: string
  isPrimary?: boolean
}

export interface CrmLeadItem {
  id: number
  name: string
  phone?: string
  source?: string
  status?: number
  nextFollowDate?: string
  remark?: string
}

export interface AdmissionRequest {
  elderId: number
  admissionDate: string
  contractNo?: string
  depositAmount?: number
  bedId?: number
  bedStartDate?: string
  remark?: string
}

export interface DischargeRequest {
  elderId: number
  dischargeDate: string
  reason?: string
  settleAmount?: number
  remark?: string
}

export interface ChangeLogItem {
  id: number
  elderId: number
  elderName?: string
  changeType: string
  beforeValue?: string
  afterValue?: string
  reason?: string
  createTime?: string
}
