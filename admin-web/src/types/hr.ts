export interface HrStaffProfile {
  staffId: string | number
  staffNo?: string
  realName?: string
  phone?: string
  departmentId?: number
  jobTitle?: string
  employmentType?: string
  hireDate?: string
  qualificationLevel?: string
  certificateNo?: string
  emergencyContactName?: string
  emergencyContactPhone?: string
  birthday?: string
  status?: number
  leaveDate?: string
  leaveReason?: string
  remark?: string
  updateTime?: string
}

export interface StaffTrainingRecord {
  id?: string | number
  staffId?: string | number
  staffName?: string
  trainingName?: string
  trainingType?: string
  provider?: string
  startDate?: string
  endDate?: string
  hours?: number
  score?: number
  status?: number
  certificateNo?: string
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
  contractId?: number | string
  contractNo?: string
  elderName?: string
  contactName?: string
  contactPhone?: string
  effectiveFrom?: string
  effectiveTo?: string
  remainingDays?: number
  status?: string
  contractStatus?: string
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
  contractId?: number | string
  contractNo?: string
  elderName?: string
  contactName?: string
  contactPhone?: string
  status?: string
  contractStatus?: string
  flowStage?: string
  effectiveFrom?: string
  effectiveTo?: string
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
  cardAccountId?: number | string
  elderId?: string
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
