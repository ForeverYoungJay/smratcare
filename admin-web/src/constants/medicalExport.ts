export interface MedicalExportColumn<T> {
  key: string
  title: string
  value: (item: T) => string | number
}

export function mapMedicalExportRows<T>(items: T[], columns: MedicalExportColumn<T>[]) {
  return items.map((item) => {
    const row: Record<string, string | number> = {}
    columns.forEach((column) => {
      row[column.title] = column.value(item)
    })
    return row
  })
}

export const medicalOrderTaskExportColumns: MedicalExportColumn<any>[] = [
  { key: 'elderName', title: '长者', value: (item) => item.elderName || '-' },
  { key: 'drugName', title: '药品', value: (item) => item.drugName || '-' },
  { key: 'plannedTime', title: '计划时间', value: (item) => item.plannedTime || '-' },
  { key: 'status', title: '状态', value: (item) => item.status || '-' },
  { key: 'doneTime', title: '完成时间', value: (item) => item.doneTime || '-' },
  { key: 'registrationId', title: '登记ID', value: (item) => item.registrationId || '' }
]

export const aiHealthReportExportColumns: MedicalExportColumn<any>[] = [
  { key: 'type', title: '报告类型', value: (item) => item.type || '-' },
  { key: 'status', title: '状态', value: (item) => item.status || '-' },
  { key: 'rangeText', title: '数据范围', value: (item) => item.rangeText || '-' },
  { key: 'highRiskCount', title: '风险摘要', value: (item) => item.highRiskCount || 0 },
  { key: 'createdAt', title: '生成时间', value: (item) => item.createdAt || '-' }
]

export const tcmAssessmentExportColumns: MedicalExportColumn<any>[] = [
  { key: 'elderName', title: '长者', value: (item) => item.elderName || '-' },
  { key: 'bedNo', title: '床位', value: (item) => item.bedNo || '-' },
  { key: 'constitutionPrimary', title: '主体质', value: (item) => item.constitutionPrimary || '-' },
  { key: 'constitutionSecondary', title: '次体质', value: (item) => item.constitutionSecondary || '-' },
  { key: 'suggestionPoints', title: '建议要点', value: (item) => item.suggestionPoints || '-' },
  { key: 'assessmentDate', title: '评估时间', value: (item) => item.assessmentDate || '-' },
  { key: 'assessorName', title: '评估人', value: (item) => item.assessorName || '-' },
  { key: 'status', title: '状态', value: (item) => item.status || '-' },
  { key: 'confidence', title: '置信度', value: (item) => item.confidence || 0 }
]

export const cvdAssessmentExportColumns: MedicalExportColumn<any>[] = [
  { key: 'elderName', title: '长者', value: (item) => item.elderName || '-' },
  { key: 'bedNo', title: '床位', value: (item) => item.bedNo || '-' },
  { key: 'riskLevel', title: '风险等级', value: (item) => item.riskLevel || '-' },
  { key: 'assessmentDate', title: '评估日期', value: (item) => item.assessmentDate || '-' },
  { key: 'keyRiskFactors', title: '关键风险因子', value: (item) => item.keyRiskFactors || '-' },
  { key: 'medicalAdvice', title: '建议', value: (item) => item.medicalAdvice || '-' },
  { key: 'needFollowup', title: '随访', value: (item) => item.needFollowup || '-' },
  { key: 'assessorName', title: '评估人', value: (item) => item.assessorName || '-' },
  { key: 'status', title: '状态', value: (item) => item.status || '-' }
]
