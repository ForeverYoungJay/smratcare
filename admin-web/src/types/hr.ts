export interface HrStaffProfile {
  staffId: number
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
  status?: number
  leaveDate?: string
  leaveReason?: string
  remark?: string
  updateTime?: string
}

export interface StaffTrainingRecord {
  id?: number
  staffId?: number
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
  staffId: number
  pointsBalance: number
  totalEarned: number
  totalDeducted: number
  status: number
}

export interface StaffPointsLog {
  id: number
  staffId: number
  staffName?: string
  changeType: string
  changePoints: number
  balanceAfter: number
  sourceType?: string
  sourceId?: number
  remark?: string
  createTime?: string
}

export interface StaffPerformanceSummary {
  staffId: number
  staffName?: string
  taskCount: number
  successCount: number
  failCount: number
  suspiciousCount: number
  avgScore?: number | null
  pointsBalance: number
  pointsEarned: number
  pointsDeducted: number
}

export interface StaffPerformanceRankItem {
  rankNo: number
  staffId: number
  staffName?: string
  taskCount: number
  successCount: number
  avgScore?: number
  periodPoints: number
  pointsBalance: number
}

export interface StaffPointsRule {
  id?: number
  templateId?: number | null
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
  id?: number
  staffId?: number
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
