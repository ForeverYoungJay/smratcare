import dayjs from 'dayjs'
import type { CrmContractItem } from '../../../types'

/** 合同与转入住看板的纯逻辑：签约流程进度、移交前置条件、状态标签。 */

export type StepState = 'done' | 'current' | 'todo'
export interface FlowStep { key: string; title: string; state: StepState; hint?: string }

/** 财务确认：true 已审核通过，false 未通过/未提交，null 当前账号无权查看财务数据 */
export type FinanceState = boolean | null

export interface ContractContext {
  /** 已有专业评估报告（来自 getContractAssessmentOverview） */
  assessed?: boolean
  finance: FinanceState
  /** 已在长者管理办理入住（按合同号/长者匹配入住记录） */
  admitted: boolean
  admittedAt?: string
}

export function isSigned(c: CrmContractItem) {
  return c.flowStage === 'SIGNED' || c.status === 'SIGNED' || c.status === 'EFFECTIVE'
}

const STATUS_TAGS: Record<string, [string, string]> = {
  DRAFT: ['草稿', 'default'],
  PENDING_APPROVAL: ['合同审核', 'orange'],
  APPROVED: ['待签约', 'gold'],
  REJECTED: ['已驳回', 'red'],
  SIGNED: ['已签署', 'green'],
  EFFECTIVE: ['已生效', 'green'],
  VOID: ['已作废', 'default']
}
const STAGE_TAGS: Record<string, [string, string]> = {
  PENDING_ASSESSMENT: ['待评估', 'blue'],
  PENDING_BED_SELECT: ['待选床报价', 'cyan'],
  PENDING_SIGN: ['待签约', 'gold'],
  SIGNED: ['已签署', 'green']
}

/** 办理状态标签：作废/驳回/审核中优先于流程阶段 */
export function contractStatusTag(c: CrmContractItem): { label: string; color: string } {
  const status = String(c.status || '')
  const [label, color] = ['VOID', 'REJECTED', 'PENDING_APPROVAL'].includes(status)
    ? STATUS_TAGS[status]
    : STAGE_TAGS[String(c.flowStage || '')] || STATUS_TAGS[status] || [c.contractStatus || '办理中', 'default']
  return { label, color }
}

export function buildFlowSteps(c: CrmContractItem, ctx: ContractContext): FlowStep[] {
  const stage = c.flowStage
  const signed = isSigned(c)
  const assessed = ctx.assessed || (stage !== 'PENDING_ASSESSMENT' && !!stage)
  const priced = signed || stage === 'PENDING_SIGN'
  const financeOk = ctx.finance === true || ctx.admitted
  const raw: Array<Omit<FlowStep, 'state'> & { done: boolean }> = [
    { key: 'intent', title: '意向确认与房型选择', done: true },
    { key: 'assessment', title: '健康评估与护理等级', done: assessed, hint: '护理等级与评估结论由护理/医务部门出具，市场部只读' },
    { key: 'pricing', title: '收费方案审核', done: priced, hint: '核对床位、护理等级对应的收费项目，按已审批收费标准报价' },
    {
      key: 'sign', title: '合同条款审核 / 签署', done: signed,
      hint: c.status === 'PENDING_APPROVAL' ? '合同审核中：服务内容、护理等级、服务期限、收费及退费条款' : '待核对：服务内容、护理等级、服务期限、收费及退费条款，并提示签约人重要条款'
    },
    {
      key: 'finance', title: '财务确认', done: financeOk,
      hint: ctx.finance === null ? '款项以财务实际到账和入住费用审核为准（当前账号无权查看，入住办理时系统会校验）' : '待财务完成入住费用审核，市场部不能修改实收状态'
    },
    { key: 'handover', title: '移交入住', done: ctx.admitted, hint: '在长者管理完成入住登记与床位核验' }
  ]
  const currentIndex = raw.findIndex((step) => !step.done)
  return raw.map(({ done, ...step }, index) => ({
    ...step,
    state: done ? 'done' : index === currentIndex ? 'current' : 'todo'
  }))
}

/** 移交入住前置条件：返回禁用原因，空数组表示可以移交 */
export function handoverBlockers(c: CrmContractItem, ctx: ContractContext): string[] {
  if (ctx.admitted) return ['已完成入住登记']
  const reasons: string[] = []
  if (c.status === 'VOID') reasons.push('合同已作废')
  if (!isSigned(c)) reasons.push('合同未签署')
  if (!String(c.elderName || '').trim()) reasons.push('缺少长者姓名')
  if (!String(c.elderPhone || c.phone || '').trim()) reasons.push('缺少联系电话')
  // 无权查看财务数据时不拦截：入住办理接口会校验入住费用审核
  if (ctx.finance === false) reasons.push('财务未完成入住费用审核')
  return reasons
}

export interface HandoverCounts { waiting: number; doneThisMonth: number; incomplete: number }

export function handoverCounts(signed: CrmContractItem[], ctxOf: (c: CrmContractItem) => ContractContext, today = dayjs()): HandoverCounts {
  const counts: HandoverCounts = { waiting: 0, doneThisMonth: 0, incomplete: 0 }
  for (const c of signed) {
    const ctx = ctxOf(c)
    if (ctx.admitted) {
      if (ctx.admittedAt && dayjs(ctx.admittedAt).isSame(today, 'month')) counts.doneThisMonth += 1
      continue
    }
    if (handoverBlockers(c, ctx).length) counts.incomplete += 1
    else counts.waiting += 1
  }
  return counts
}
