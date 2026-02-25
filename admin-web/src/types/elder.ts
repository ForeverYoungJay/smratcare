export interface ElderItem {
  id: number
  fullName: string
  idCard?: string
  idCardNo?: string
  phone?: string
  homeAddress?: string
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
  homeAddress?: string
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
  consultantName?: string
  consultantPhone?: string
  elderName?: string
  elderPhone?: string
  gender?: number
  age?: number
  consultDate?: string
  consultType?: string
  mediaChannel?: string
  infoSource?: string
  receptionistName?: string
  homeAddress?: string
  marketerName?: string
  followupStatus?: string
  referralChannel?: string
  invalidTime?: string
  idCardNo?: string
  reservationAmount?: number
  reservationRoomNo?: string
  paymentTime?: string
  refunded?: number
  reservationChannel?: string
  reservationStatus?: string
  orgName?: string
  source?: string
  customerTag?: string
  status?: number
  contractSignedFlag?: number
  contractSignedAt?: string
  contractNo?: string
  contractStatus?: string
  contractExpiryDate?: string
  smsSendCount?: number
  nextFollowDate?: string
  remark?: string
  createTime?: string
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

export type TrialStayStatus = 'REGISTERED' | 'FINISHED' | 'CONVERTED' | 'CANCELLED'
export type DischargeApplyStatus = 'PENDING' | 'APPROVED' | 'REJECTED'
export type OutingStatus = 'OUT' | 'RETURNED'

export interface AdmissionRecordQuery {
  pageNo?: number
  pageSize?: number
  keyword?: string
  contractNo?: string
  elderStatus?: number
  admissionDateStart?: string
  admissionDateEnd?: string
}

export interface ChangeLogQuery {
  pageNo?: number
  pageSize?: number
  elderId?: number
  changeType?: string
  reason?: string
  startTime?: string
  endTime?: string
}

export interface OutingQuery {
  pageNo?: number
  pageSize?: number
  elderId?: number
  status?: OutingStatus
}

export interface TrialStayQuery {
  pageNo?: number
  pageSize?: number
  elderId?: number
  status?: TrialStayStatus
  keyword?: string
  trialStartDateFrom?: string
  trialStartDateTo?: string
}

export interface DischargeApplyQuery {
  pageNo?: number
  pageSize?: number
  elderId?: number
  status?: DischargeApplyStatus
  keyword?: string
  plannedDateFrom?: string
  plannedDateTo?: string
}

export interface AdmissionRecordItem {
  id: number
  elderId: number
  elderName?: string
  contractNo?: string
  admissionDate?: string
  depositAmount?: number
  remark?: string
  elderStatus?: number
  createTime?: string
}

export interface OutingItem {
  id: number
  elderId: number
  elderName?: string
  outingDate: string
  expectedReturnTime?: string
  actualReturnTime?: string
  companion?: string
  reason?: string
  status?: OutingStatus
  remark?: string
  createTime?: string
}

export interface OutingCreateRequest {
  elderId: number
  outingDate: string
  expectedReturnTime?: string
  companion?: string
  reason?: string
  remark?: string
}

export interface OutingReturnRequest {
  actualReturnTime?: string
  remark?: string
}

export interface TrialStayItem {
  id: number
  elderId: number
  elderName?: string
  trialStartDate: string
  trialEndDate: string
  channel?: string
  trialPackage?: string
  intentLevel?: string
  status?: TrialStayStatus
  careLevel?: string
  remark?: string
  createTime?: string
}

export interface TrialStayRequest {
  elderId: number
  trialStartDate: string
  trialEndDate: string
  channel?: string
  trialPackage?: string
  intentLevel?: string
  status?: TrialStayStatus
  careLevel?: string
  remark?: string
}

export interface DischargeApplyItem {
  id: number
  elderId: number
  elderName?: string
  applyDate?: string
  plannedDischargeDate: string
  reason: string
  status?: DischargeApplyStatus
  linkedDischargeId?: number
  autoDischargeStatus?: 'SUCCESS' | 'FAILED'
  autoDischargeMessage?: string
  reviewRemark?: string
  reviewedBy?: number
  reviewedByName?: string
  reviewedTime?: string
  createTime?: string
}

export interface DischargeApplyCreateRequest {
  elderId: number
  plannedDischargeDate: string
  reason: string
}

export interface DischargeApplyReviewRequest {
  status: Exclude<DischargeApplyStatus, 'PENDING'>
  reviewRemark?: string
}
