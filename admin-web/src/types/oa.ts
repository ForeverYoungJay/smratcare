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
}
