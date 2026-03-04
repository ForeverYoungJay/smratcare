export interface ExportFieldDict {
  key: string
  label: string
}

export const HR_EXPENSE_EXPORT_FIELDS: ExportFieldDict[] = [
  { key: 'expenseType', label: '费用类型' },
  { key: 'title', label: '标题' },
  { key: 'applicantName', label: '申请人' },
  { key: 'amount', label: '金额' },
  { key: 'status', label: '审批状态' },
  { key: 'applyStartTime', label: '申请开始时间' },
  { key: 'applyEndTime', label: '申请结束时间' },
  { key: 'createTime', label: '创建时间' },
  { key: 'remark', label: '备注' }
]

export const HR_APPROVAL_EXPORT_FIELDS: ExportFieldDict[] = [
  { key: 'title', label: '标题' },
  { key: 'approvalType', label: '审批类型' },
  { key: 'scene', label: '场景' },
  { key: 'applicantName', label: '申请人' },
  { key: 'status', label: '状态' },
  { key: 'amount', label: '金额' },
  { key: 'startTime', label: '开始时间' },
  { key: 'endTime', label: '结束时间' },
  { key: 'createTime', label: '创建时间' },
  { key: 'remark', label: '备注' }
]

export const HR_ATTENDANCE_EXPORT_FIELDS: ExportFieldDict[] = [
  { key: 'staffName', label: '员工姓名' },
  { key: 'checkInTime', label: '签到时间' },
  { key: 'checkOutTime', label: '签退时间' },
  { key: 'status', label: '考勤状态' },
  { key: 'abnormal', label: '是否异常' }
]

export function mapByDict<T extends Record<string, any>>(rows: T[], dict: ExportFieldDict[]) {
  return rows.map((row) => {
    const mapped: Record<string, any> = {}
    dict.forEach((item) => {
      mapped[item.label] = row[item.key]
    })
    return mapped
  })
}
