import type { CareIncidentItem, CareTaskItem } from '../../../types'

export interface ExecuteForm {
  result: 'DONE' | 'PARTIAL' | 'NOT_DONE'
  reason: string
  bedQrCode: string
  observation: string
  incidentType: string
  severity: string
  /** 已勾选的服务项目 */
  checkedItems: string[]
}

/**
 * 把执行抽屉的表单换算成要调用的接口：
 * - 已完成/部分完成：扫码执行，勾选情况、原因与观察写进备注；有清单时「已完成」必须全部勾选
 * - 未完成：不标记完成，改为上报「服务延误」异常（除非已选了别的异常类型）
 * - 选了异常类型：同时上报异常事件，关联本任务
 */
export function buildExecution(form: ExecuteForm, task: CareTaskItem): {
  error?: string
  execute?: { bedQrCode: string; remark?: string }
  incident?: Partial<CareIncidentItem>
} {
  const reason = form.reason.trim()
  const observation = form.observation.trim()
  if (form.result !== 'DONE' && !reason) return { error: '部分完成或未完成必须填写原因' }
  if (form.result !== 'NOT_DONE' && !form.bedQrCode.trim()) return { error: '请扫描或输入床位二维码' }
  if (form.incidentType && !observation) return { error: '有异常时请在观察记录里写清楚发生了什么' }
  const items = task.serviceItems || []
  const undone = items.filter((item) => !form.checkedItems.includes(item))
  if (form.result === 'DONE' && undone.length) return { error: '还有服务项目没勾选；没做完请选「部分完成」并写原因' }
  if (form.result === 'PARTIAL' && items.length && !undone.length) return { error: '服务项目都已勾选，请选「已完成」' }

  const checklist = items.length && form.result !== 'NOT_DONE'
    ? [`完成：${items.filter((item) => form.checkedItems.includes(item)).join('、') || '无'}`, undone.length ? `未完成：${undone.join('、')}` : '']
    : []
  const remark = [form.result === 'PARTIAL' ? `【部分完成】${reason}` : '', ...checklist, observation].filter(Boolean).join('；')
  if (remark.length > 1000) return { error: '执行记录太长，请精简观察记录' }
  const execute = form.result === 'NOT_DONE' ? undefined : { bedQrCode: form.bedQrCode.trim(), remark: remark || undefined }
  const incidentType = form.incidentType || (form.result === 'NOT_DONE' ? 'DELAY' : '')
  const incident = incidentType ? {
    incidentType,
    severity: form.incidentType ? form.severity : 'LOW',
    elderId: task.elderId,
    elderName: task.elderName,
    location: task.roomNo,
    relatedTaskId: task.taskDailyId,
    description: [`任务「${task.taskName}」`, form.result === 'NOT_DONE' ? `未完成：${reason}` : '', observation].filter(Boolean).join('；')
  } : undefined
  return { execute, incident }
}
