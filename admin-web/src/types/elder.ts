import type { Id } from './common'

export interface ElderItem {
  id: Id
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
  riskPrecommit?: 'RESCUE_FIRST' | 'NOTIFY_FAMILY_FIRST'
  status?: number
  bedId?: Id
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
  riskPrecommit?: 'RESCUE_FIRST' | 'NOTIFY_FAMILY_FIRST'
  status?: number
  remark?: string
  bedId?: Id
  bedStartDate?: string
}

export interface ElderAssignBedRequest {
  bedId: Id
  startDate: string
}

export interface ElderUnbindRequest {
  endDate?: string
  reason?: string
}

export interface ElderDiseaseItem {
  id: Id
  diseaseId: Id
  diseaseName?: string
}

export interface ElderDiseaseUpdateRequest {
  diseaseIds: number[]
}

export interface FamilyBindingItem {
  id: Id
  familyUserId: Id
  familyName?: string
  phone?: string
  relation?: string
  isPrimary?: boolean
}

export interface FamilyUserItem {
  id: Id
  realName?: string
  phone?: string
  idCardNo?: string
  status?: number
  elderCount?: number
}

export interface FamilyRelationItem {
  id: Id
  familyUserId: Id
  realName?: string
  phone?: string
  relation?: string
  isPrimary?: boolean
}

export interface FamilyBindRequest {
  familyUserId: Id
  elderId: Id
  relation: string
  isPrimary?: boolean
}

export interface CrmLeadItem {
  id: Id
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
  reservationBedId?: Id
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
  flowStage?: 'PENDING_ASSESSMENT' | 'PENDING_BED_SELECT' | 'PENDING_SIGN' | 'SIGNED'
  currentOwnerDept?: 'MARKETING' | 'ASSESSMENT'
  contractExpiryDate?: string
  smsSendCount?: number
  nextFollowDate?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface AdmissionRequest {
  elderId: Id
  admissionDate: string
  contractNo?: string
  depositAmount?: number
  bedId?: Id
  bedStartDate?: string
  remark?: string
}

export interface DischargeRequest {
  elderId: Id
  dischargeDate: string
  reason?: string
  settleAmount?: number
  remark?: string
}

export interface ChangeLogItem {
  id: Id
  elderId: Id
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
export type MedicalOutingStatus = 'OUT' | 'RETURNED'
export type DeathRegisterStatus = 'REGISTERED' | 'CANCELLED'

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
  elderId?: Id
  changeType?: string
  reason?: string
  startTime?: string
  endTime?: string
}

export interface OutingQuery {
  pageNo?: number
  pageSize?: number
  elderId?: Id
  status?: OutingStatus
}

export interface TrialStayQuery {
  pageNo?: number
  pageSize?: number
  elderId?: Id
  status?: TrialStayStatus
  keyword?: string
  trialStartDateFrom?: string
  trialStartDateTo?: string
}

export interface DischargeApplyQuery {
  pageNo?: number
  pageSize?: number
  elderId?: Id
  status?: DischargeApplyStatus
  keyword?: string
  plannedDateFrom?: string
  plannedDateTo?: string
}

export interface MedicalOutingQuery {
  pageNo?: number
  pageSize?: number
  elderId?: Id
  status?: MedicalOutingStatus
  keyword?: string
}

export interface DeathRegisterQuery {
  pageNo?: number
  pageSize?: number
  elderId?: Id
  status?: DeathRegisterStatus
  keyword?: string
}

export interface ResidenceStatusSummary {
  intentCount: number
  trialCount: number
  inHospitalCount: number
  outingCount: number
  medicalOutingCount: number
  dischargePendingCount: number
  dischargedCount: number
  deathCount: number
  openIncidentCount: number
  overdueOutingCount: number
}

export interface AdmissionRecordItem {
  id: Id
  elderId: Id
  elderName?: string
  contractNo?: string
  admissionDate?: string
  depositAmount?: number
  remark?: string
  elderStatus?: number
  createTime?: string
}

export interface OutingItem {
  id: Id
  elderId: Id
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
  elderId: Id
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

export interface MedicalOutingItem {
  id: Id
  elderId: Id
  elderName?: string
  outingDate: string
  expectedReturnTime?: string
  actualReturnTime?: string
  hospitalName?: string
  department?: string
  diagnosis?: string
  companion?: string
  reason?: string
  status?: MedicalOutingStatus
  remark?: string
  createTime?: string
}

export interface MedicalOutingCreateRequest {
  elderId: Id
  outingDate: string
  expectedReturnTime?: string
  hospitalName?: string
  department?: string
  diagnosis?: string
  companion?: string
  reason?: string
  remark?: string
}

export interface MedicalOutingReturnRequest {
  actualReturnTime?: string
  remark?: string
}

export interface TrialStayItem {
  id: Id
  elderId: Id
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
  elderId: Id
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
  id: Id
  elderId: Id
  elderName?: string
  applyDate?: string
  plannedDischargeDate: string
  reason: string
  status?: DischargeApplyStatus
  linkedDischargeId?: Id
  autoDischargeStatus?: 'SUCCESS' | 'FAILED'
  autoDischargeMessage?: string
  reviewRemark?: string
  reviewedBy?: number
  reviewedByName?: string
  reviewedTime?: string
  createTime?: string
}

export interface DischargeApplyCreateRequest {
  elderId: Id
  plannedDischargeDate: string
  reason: string
}

export interface DischargeApplyReviewRequest {
  status: Exclude<DischargeApplyStatus, 'PENDING'>
  reviewRemark?: string
}

export interface DeathRegisterItem {
  id: Id
  elderId: Id
  elderName?: string
  deathDate: string
  deathTime?: string
  place?: string
  cause?: string
  certificateNo?: string
  reportedBy?: string
  reportedTime?: string
  beforeStatus?: number
  status?: DeathRegisterStatus
  remark?: string
  updatedBy?: number
  cancelledBy?: number
  cancelledTime?: string
  createTime?: string
}

export interface DeathRegisterCreateRequest {
  elderId: Id
  deathDate: string
  deathTime?: string
  place?: string
  cause?: string
  certificateNo?: string
  reportedBy?: string
  reportedTime?: string
  remark?: string
}

export interface DeathRegisterUpdateRequest {
  deathDate: string
  deathTime?: string
  place?: string
  cause?: string
  certificateNo?: string
  reportedBy?: string
  reportedTime?: string
  remark?: string
}

export interface DeathRegisterCancelRequest {
  remark?: string
}
