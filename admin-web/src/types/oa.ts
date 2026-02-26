export interface OaNotice {
  id: number
  title: string
  content: string
  publisherName?: string
  publishTime?: string
  status?: string
}

export interface OaTodo {
  id: number
  title: string
  content?: string
  dueTime?: string
  status?: string
  assigneeName?: string
}

export interface OaApproval {
  id: number
  approvalType: string
  title: string
  applicantName: string
  amount?: number
  startTime?: string
  endTime?: string
  formData?: string
  status?: string
  remark?: string
}

export interface OaDocument {
  id: number
  name: string
  folder?: string
  url?: string
  sizeBytes?: number
  uploaderName?: string
  uploadedAt?: string
  remark?: string
}

export interface OaAlbum {
  id: number
  title: string
  category?: string
  coverUrl?: string
  photoCount?: number
  shootDate?: string
  status?: string
  remark?: string
}

export interface OaKnowledge {
  id: number
  title: string
  category?: string
  tags?: string
  content?: string
  authorName?: string
  status?: string
  publishedAt?: string
  remark?: string
}

export interface OaGroupSetting {
  id: number
  groupName: string
  groupType?: string
  leaderId?: number
  leaderName?: string
  memberCount?: number
  status?: string
  remark?: string
}

export interface OaActivityPlan {
  id: number
  title: string
  planDate?: string
  startTime?: string
  endTime?: string
  location?: string
  organizer?: string
  participantTarget?: string
  status?: string
  content?: string
  remark?: string
}

export interface OaTask {
  id: number
  title: string
  description?: string
  startTime?: string
  endTime?: string
  priority?: string
  status?: string
  assigneeName?: string
}

export interface OaPortalSummary {
  notices: OaNotice[]
  todos: OaTodo[]
  openTodoCount?: number
  overdueTodoCount?: number
  pendingApprovalCount?: number
  ongoingTaskCount?: number
  submittedReportCount?: number
  todayScheduleCount?: number
  attendanceAbnormalCount?: number
  leavePendingCount?: number
  reimbursePendingCount?: number
  purchasePendingCount?: number
  contractPendingCount?: number
  myExpenseCount?: number
  deptExpenseCount?: number
  invoiceFolderCount?: number
  documentCount?: number
  inventoryLowStockCount?: number
  approvalTimeoutCount?: number
  elderAbnormalCount?: number
  healthAbnormalCount?: number
  incidentOpenCount?: number
  materialTransferDraftCount?: number
  materialPurchaseDraftCount?: number
  inHospitalElderCount?: number
  suggestionCount?: number
  workflowTodos?: OaWorkflowTodoItem[]
  marketingChannels?: OaMarketingChannelItem[]
  collaborationGantt?: OaCollaborationGanttItem[]
  latestSuggestions?: OaSuggestion[]
}

export interface OaWorkReport {
  id: number
  title: string
  reportType: string
  reportDate?: string
  periodStartDate?: string
  periodEndDate?: string
  contentSummary?: string
  completedWork?: string
  riskIssue?: string
  nextPlan?: string
  status?: string
  reporterId?: number
  reporterName?: string
}

export interface OaWorkflowTodoItem {
  code: string
  name: string
  count: number
  route: string
}

export interface OaMarketingChannelItem {
  source: string
  leadCount: number
  contractCount: number
}

export interface OaCollaborationGanttItem {
  taskId: number
  title: string
  status: string
  startTime?: string
  endTime?: string
  progress?: number
}

export interface OaSuggestion {
  id: number
  content: string
  proposerName?: string
  contact?: string
  status?: string
  createTime?: string
}
