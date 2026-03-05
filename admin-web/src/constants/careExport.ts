export interface CareExportColumn<T> {
  key: string
  title: string
  value: (item: T) => string | number
}

export function mapCareExportRows<T>(items: T[], columns: CareExportColumn<T>[]) {
  return items.map((item) => {
    const row: Record<string, string | number> = {}
    columns.forEach((column) => {
      row[column.title] = column.value(item)
    })
    return row
  })
}

export const careTemplateExportColumns: CareExportColumn<any>[] = [
  { key: 'taskName', title: '任务名称', value: (item) => item.taskName || '' },
  { key: 'frequencyPerDay', title: '频次', value: (item) => item.frequencyPerDay ?? '' },
  { key: 'careLevelRequired', title: '护理等级', value: (item) => item.careLevelRequired || '' },
  { key: 'chargeAmount', title: '收费', value: (item) => item.chargeAmount ?? 0 },
  { key: 'enabled', title: '状态', value: (item) => item.enabled ? '启用' : '停用' }
]

export const careExceptionExportColumns: CareExportColumn<any>[] = [
  { key: 'elderName', title: '老人', value: (item) => item.elderName || '' },
  { key: 'roomNo', title: '房间', value: (item) => item.roomNo || '' },
  { key: 'taskName', title: '任务', value: (item) => item.taskName || '' },
  { key: 'planTime', title: '计划时间', value: (item) => item.planTime || '' }
]

export const careAuditExportColumns: CareExportColumn<any>[] = [
  { key: 'elderName', title: '老人', value: (item) => item.elderName || '' },
  { key: 'roomNo', title: '房间', value: (item) => item.roomNo || '' },
  { key: 'taskName', title: '任务', value: (item) => item.taskName || '' },
  { key: 'planTime', title: '执行时间', value: (item) => item.planTime || '' },
  { key: 'suspiciousFlag', title: '可疑', value: (item) => item.suspiciousFlag ? '是' : '否' }
]

export const careTodayTaskExportColumns: CareExportColumn<any>[] = [
  { key: 'elderName', title: '老人', value: (item) => item.elderName || '' },
  { key: 'roomNo', title: '房间', value: (item) => item.roomNo || '' },
  { key: 'taskName', title: '任务', value: (item) => item.taskName || '' },
  { key: 'planTime', title: '计划时间', value: (item) => item.planTime || '' },
  { key: 'staffName', title: '护工', value: (item) => item.staffName || '' },
  { key: 'status', title: '状态', value: (item) => item.status || '' },
  { key: 'overdueFlag', title: '是否逾期', value: (item) => item.overdueFlag ? '是' : '否' },
  { key: 'suspiciousFlag', title: '是否可疑', value: (item) => item.suspiciousFlag ? '是' : '否' }
]

export const careTodayLogExportColumns: CareExportColumn<any>[] = [
  { key: 'taskDailyId', title: '任务ID', value: (item) => item.taskDailyId ?? '' },
  { key: 'elderName', title: '老人', value: (item) => item.elderName || '' },
  { key: 'roomNo', title: '房间', value: (item) => item.roomNo || '' },
  { key: 'executeTime', title: '执行时间', value: (item) => item.executeTime || '-' },
  { key: 'staffName', title: '护工', value: (item) => item.staffName || '' },
  { key: 'resultStatus', title: '结果', value: (item) => Number(item.resultStatus) === 1 ? '成功' : '失败' },
  { key: 'suspiciousFlag', title: '是否可疑', value: (item) => item.suspiciousFlag ? '是' : '否' },
  { key: 'bedQrCode', title: '床位码', value: (item) => item.bedQrCode || '' },
  { key: 'remark', title: '备注', value: (item) => item.remark || '' }
]
