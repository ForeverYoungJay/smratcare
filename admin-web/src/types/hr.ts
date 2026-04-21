import type { Id } from './common'

export interface HrStaffProfile {
  staffId: string | number
  staffNo?: string
  realName?: string
  phone?: string
  departmentId?: number
  jobTitle?: string
  employmentType?: string
  contractNo?: string
  contractType?: string
  contractStatus?: string
  contractStartDate?: string
  contractEndDate?: string
  remainingDays?: number
  hireDate?: string
  qualificationLevel?: string
  certificateNo?: string
  emergencyContactName?: string
  emergencyContactPhone?: string
  birthday?: string
  status?: number
  leaveDate?: string
  leaveReason?: string
  socialSecurityStatus?: string
  socialSecurityStartDate?: string
  socialSecurityReminderDays?: number
  socialSecurityCompanyApply?: number
  socialSecurityNeedDirectorApproval?: number
  socialSecurityWorkflowStatus?: string
  socialSecurityMonthlyAmount?: number
  socialSecurityApplySubmittedAt?: string
  socialSecurityDirectorDecisionAt?: string
  socialSecurityCompletedAt?: string
  socialSecurityLastBilledMonth?: string
  socialSecurityRemark?: string
  remark?: string
  updateTime?: string
}

export interface HrSocialSecuritySummary {
  dueReminderCount?: number
  upcomingReminderCount?: number
  thisMonthNewParticipantCount?: number
}

export interface HrSocialSecurityReminderItem {
  staffId?: string | number
  staffNo?: string
  staffName?: string
  phone?: string
  departmentId?: number
  jobTitle?: string
  hireDate?: string
  socialSecurityStatus?: string
  socialSecurityStartDate?: string
  socialSecurityReminderDays?: number
  socialSecurityCompanyApply?: number
  socialSecurityNeedDirectorApproval?: number
  socialSecurityWorkflowStatus?: string
  socialSecurityMonthlyAmount?: number
  socialSecurityApplySubmittedAt?: string
  socialSecurityDirectorDecisionAt?: string
  socialSecurityCompletedAt?: string
  socialSecurityLastBilledMonth?: string
  reminderDate?: string
  remainingDays?: number
  reminderScope?: string
  socialSecurityRemark?: string
}

export interface HrSocialSecurityApplyRequest {
  staffId: string | number
  socialSecurityCompanyApply?: number
  socialSecurityNeedDirectorApproval?: number
  socialSecurityMonthlyAmount?: number
  socialSecurityRemark?: string
}

export interface HrSocialSecurityCompleteRequest {
  staffId: string | number
  socialSecurityStartDate?: string
  socialSecurityRemark?: string
}

export interface HrSocialSecurityBillGenerateRequest {
  month?: string
  staffIds?: Array<string | number>
}

export interface StaffTrainingRecord {
  id?: string | number
  sourceTrainingId?: string | number
  trainingScene?: 'PLAN' | 'RECORD' | 'CERTIFICATE'
  trainingYear?: number
  departmentId?: string | number
  departmentName?: string
  staffId?: string | number
  staffNo?: string
  staffName?: string
  trainingName?: string
  trainingType?: string
  provider?: string
  instructor?: string
  startDate?: string
  endDate?: string
  hours?: number
  score?: number
  attendanceStatus?: string
  testResult?: string
  certificateRequired?: number
  certificateStatus?: string
  status?: number
  certificateNo?: string
  attachments?: Array<{
    name?: string
    url?: string
    fileName?: string
    fileUrl?: string
    size?: number
    fileSize?: number
    type?: string
    fileType?: string
  }>
  remark?: string
  createTime?: string
}

export interface StaffPointsAccount {
  staffId: string | number
  pointsBalance: number
  totalEarned: number
  totalDeducted: number
  status: number
}

export interface StaffPointsLog {
  id: string | number
  staffId: string | number
  staffName?: string
  changeType: string
  changePoints: number
  balanceAfter: number
  sourceType?: string
  sourceId?: string | number
  remark?: string
  createTime?: string
}

export interface StaffPerformanceSummary {
  staffId: string | number
  staffName?: string
  taskCount: number
  successCount: number
  failCount: number
  suspiciousCount: number
  avgScore?: number | null
  pointsBalance: number
  pointsEarned: number
  pointsDeducted: number
  redeemableCash?: number
}

export interface StaffPerformanceRankItem {
  rankNo: number
  categoryRankNo?: number
  staffId: string | number
  staffName?: string
  staffCategory?: 'NURSING' | 'ADMINISTRATION' | 'OPERATION'
  medalLevel?: 'GOLD' | 'SILVER' | 'BRONZE' | 'NONE'
  taskCount: number
  successCount: number
  avgScore?: number
  periodPoints: number
  pointsBalance: number
  redeemableCash?: number
}

export interface StaffPointsRule {
  id?: string | number
  templateId?: string | number | null
  templateName?: string
  basePoints?: number
  scoreWeight?: number
  suspiciousPenalty?: number
  failPoints?: number
  status?: number
  remark?: string
  updateTime?: string
}

export interface StaffRewardPunishment {
  id?: string | number
  staffId?: string | number
  staffName?: string
  type?: 'REWARD' | 'PUNISH'
  level?: string
  title?: string
  reason?: string
  amount?: number
  points?: number
  occurredDate?: string
  status?: string
  remark?: string
  createTime?: string
}

export interface HrWorkbenchSummary {
  onJobCount?: number
  leftCount?: number
  todayTrainingCount?: number
  pendingLeaveApprovalCount?: number
  attendanceAbnormalCount?: number
  contractExpiringCount?: number
  birthdayTodayCount?: number
  birthdayUpcomingCount?: number
  birthdayTodoCount?: number
  warningDays?: number
}

export interface HrStaffBirthdayItem {
  staffId?: number | string
  staffNo?: string
  realName?: string
  phone?: string
  departmentId?: number
  jobTitle?: string
  status?: number
  birthday?: string
  nextBirthday?: string
  daysUntil?: number
  remark?: string
}

export interface HrContractReminderItem {
  contractId?: Id
  staffId?: Id
  staffNo?: string
  staffName?: string
  phone?: string
  jobTitle?: string
  contractNo?: string
  contractStatus?: string
  contractType?: string
  contractStartDate?: string
  contractEndDate?: string
  remainingDays?: number
}

export interface HrAttendanceAbnormalItem {
  id?: number | string
  staffId?: number | string
  staffName?: string
  checkInTime?: string
  checkOutTime?: string
  status?: string
  reviewed?: number
  reviewRemark?: string
  reviewedAt?: string
}

export interface HrRecruitmentNeedItem {
  id?: number | string
  title?: string
  positionName?: string
  departmentName?: string
  headcount?: number
  requiredDate?: string
  scene?: string
  status?: string
  remark?: string
  applicantName?: string
  createTime?: string
  candidateName?: string
  contactPhone?: string
  resumeUrl?: string
  intentionStatus?: string
  followUpDate?: string
  offerStatus?: string
  onboardDate?: string
  salary?: string
  probationPeriod?: string
  workLocation?: string
  shiftType?: string
  checklistJson?: string
  signedFilesJson?: string
  accountPermissionJson?: string
  issuedItemsJson?: string
  mentorName?: string
  probationGoal?: string
  regularizationStatus?: string
  recommendationNote?: string
  offboardingType?: string
  lastWorkDate?: string
  handoverDeadline?: string
  resignationReason?: string
  resignationReportUrl?: string
  handoverItemsJson?: string
  assetRecoveryJson?: string
  permissionRecycleJson?: string
  financeSettlementNote?: string
  exitArchiveJson?: string
  formData?: string
}

export interface HrPolicyAlertItem {
  documentId?: number | string
  name?: string
  folder?: string
  uploaderName?: string
  uploadedAt?: string
  lastUpdatedDays?: number
  alertLevel?: 'LOW' | 'MEDIUM' | 'HIGH' | string
}

export interface HrProfileContractItem {
  contractId?: Id
  staffId?: Id
  staffNo?: string
  staffName?: string
  phone?: string
  jobTitle?: string
  employmentType?: string
  contractNo?: string
  contractStatus?: string
  contractType?: string
  contractStartDate?: string
  contractEndDate?: string
  remainingDays?: number
}

export interface HrProfileDocumentItem {
  id?: number | string
  name?: string
  folder?: string
  url?: string
  sizeBytes?: number
  uploaderName?: string
  uploadedAt?: string
  remark?: string
}

export interface HrStaffCertificateItem {
  id?: number | string
  staffId?: number | string
  staffName?: string
  certificateName?: string
  certificateNo?: string
  issuingAuthority?: string
  issueDate?: string
  expiryDate?: string
  remainingDays?: number
  status?: number
  remark?: string
}

export interface HrAccessControlRecordItem {
  id?: number | string
  staffId?: number | string
  staffName?: string
  checkInTime?: string
  checkOutTime?: string
  attendanceStatus?: string
  accessResult?: 'PASS' | 'EXCEPTION' | 'UNKNOWN' | string
}

export interface HrCardSyncItem {
  cardAccountId?: Id
  elderId?: Id
  elderName?: string
  cardNo?: string
  cardStatus?: string
  lossFlag?: number
  openTime?: string
  lastRechargeTime?: string
  syncStatus?: 'SYNCED' | 'PENDING' | 'CARD_LOST' | 'UNKNOWN' | string
  remark?: string
}

export interface HrExpenseItem {
  id?: number | string
  expenseType?: string
  title?: string
  applicantName?: string
  amount?: number
  status?: string
  applyStartTime?: string
  applyEndTime?: string
  createTime?: string
  remark?: string
}

export interface HrStaffServicePlan {
  id?: Id
  staffId?: Id
  staffNo?: string
  staffName?: string
  departmentId?: Id
  departmentName?: string
  breakfastEnabled?: number
  breakfastUnitPrice?: number
  breakfastDaysPerMonth?: number
  lunchEnabled?: number
  lunchUnitPrice?: number
  lunchDaysPerMonth?: number
  dinnerEnabled?: number
  dinnerUnitPrice?: number
  dinnerDaysPerMonth?: number
  liveInDormitory?: number
  dormitoryBuilding?: string
  dormitoryRoomNo?: string
  dormitoryBedNo?: string
  meterNo?: string
  status?: string
  mealPlanSummary?: string
  remark?: string
  updateTime?: string
}

export interface HrDormitoryOverview {
  staffCount?: number
  liveInDormitoryCount?: number
  assignedBedCount?: number
  pendingAssignCount?: number
  buildingCount?: number
  roomCount?: number
  configuredRoomCount?: number
  configuredBedCapacity?: number
  meterBoundCount?: number
  conflictCount?: number
}

export interface HrDormitoryStaffItem {
  staffId?: Id
  staffNo?: string
  staffName?: string
  departmentId?: Id
  departmentName?: string
  status?: number
  liveInDormitory?: number
  dormitoryBuilding?: string
  dormitoryRoomNo?: string
  dormitoryBedNo?: string
  dormitoryLabel?: string
  meterNo?: string
  mealPlanSummary?: string
  remark?: string
  occupancyConflict?: boolean
  updateTime?: string
}

export interface HrDormitoryRoomConfigItem {
  id?: Id
  building?: string
  floorLabel?: string
  roomNo?: string
  bedCapacity?: number
  status?: string
  sortNo?: number
  remark?: string
  updateTime?: string
}

export interface HrStaffMonthlyFeeBillItem {
  id?: Id
  staffId?: Id
  staffNo?: string
  staffName?: string
  departmentId?: Id
  departmentName?: string
  feeMonth?: string
  feeType?: 'MEAL' | 'ELECTRICITY' | string
  title?: string
  quantity?: number
  unitPrice?: number
  amount?: number
  status?: string
  financeSyncStatus?: string
  financeSyncId?: Id
  financeSyncAt?: string
  financeSyncByName?: string
  dormitoryBuilding?: string
  dormitoryRoomNo?: string
  dormitoryBedNo?: string
  meterNo?: string
  mealPlanSummary?: string
  detailSummary?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface HrStaffFeeGenerateRequest {
  month?: string
}

export interface HrStaffElectricityImportRow {
  staffId?: Id
  staffNo?: string
  staffName?: string
  amount?: number
  dormitoryBuilding?: string
  dormitoryRoomNo?: string
  dormitoryBedNo?: string
  meterNo?: string
  remark?: string
}

export interface HrStaffElectricityImportRequest {
  month?: string
  rows?: HrStaffElectricityImportRow[]
}

export interface HrStaffFeeSyncRequest {
  feeType?: 'MEAL' | 'ELECTRICITY' | string
  month?: string
  ids?: Array<Id>
}

export interface HrBatchActionSummary {
  totalCount?: number
  successCount?: number
  skippedCount?: number
  totalAmount?: number
  message?: string
}

export interface HrExpenseApprovalRequest {
  title?: string
  expenseType?: string
  scene?: string
  amount?: number
  status?: string
  applyStartTime?: string
  applyEndTime?: string
  remark?: string
}

export interface HrGenericApprovalItem {
  id?: number | string
  title?: string
  approvalType?: string
  scene?: string
  applicantName?: string
  status?: string
  amount?: number
  startTime?: string
  endTime?: string
  createTime?: string
  remark?: string
}

export interface HrAttendanceRecordItem {
  id?: number | string
  staffId?: number | string
  staffName?: string
  checkInTime?: string
  checkOutTime?: string
  status?: string
  abnormal?: boolean
  reviewed?: number
  reviewRemark?: string
  reviewedAt?: string
}
