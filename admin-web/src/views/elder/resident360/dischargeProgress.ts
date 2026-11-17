import type { DischargeApplyItem } from '../../../types'

export type DischargeStepState = 'done' | 'current' | 'waiting' | 'todo'
export type DischargeNextAction = 'REVIEW' | 'CARE_HANDOVER' | 'ITEM_HANDOVER' | 'SETTLEMENT' | 'ARCHIVE'

export interface DischargeStep {
  key: string
  title: string
  state: DischargeStepState
  stateText: string
  hint?: string
}

/**
 * 进行中的退住：待审批，或已通过但未归档。
 * 已结清（SUCCESS）却没有物品交接记录的，是分步办理上线前已闭环的历史单，视为已完成。
 */
export function isDischargeInProgress(apply: DischargeApplyItem) {
  if (apply.status === 'PENDING') return true
  if (apply.status !== 'APPROVED' || apply.archiveTime) return false
  return !(apply.autoDischargeStatus === 'SUCCESS' && !apply.itemHandoverTime)
}

/** 下一步要做的事；与后端 DischargeProgressRules 的顺序一致。 */
export function nextDischargeAction(apply: DischargeApplyItem): DischargeNextAction | null {
  if (apply.status === 'PENDING') return 'REVIEW'
  if (apply.status !== 'APPROVED') return null
  if (!apply.careHandoverTime) return 'CARE_HANDOVER'
  if (!apply.itemHandoverTime) return 'ITEM_HANDOVER'
  if (apply.autoDischargeStatus !== 'SUCCESS') return 'SETTLEMENT'
  if (!apply.archiveTime) return 'ARCHIVE'
  return null
}

function by(time?: string, name?: string) {
  return time ? `${name || '--'} · ${time.replace('T', ' ').slice(0, 16)}` : undefined
}

/**
 * 六步：审批 → 护理/医务交接 → 物品及房间交接 → 费用结清 → 床位释放 → 档案归档。
 * 物品交接排在费用结清之前：财务退款会同时把长者置为已退住并释放床位，交接必须先完成。
 */
export function buildDischargeSteps(apply: DischargeApplyItem): DischargeStep[] {
  const next = nextDischargeAction(apply)
  const settled = apply.autoDischargeStatus === 'SUCCESS'
  const approved = apply.status === 'APPROVED'
  const pendingState = (action: DischargeNextAction): Pick<DischargeStep, 'state' | 'stateText'> =>
    next === action ? { state: 'current', stateText: '待办理' } : { state: 'todo', stateText: '未开始' }
  const doneState = { state: 'done', stateText: '已完成' } as const

  return [
    {
      key: 'apply',
      title: '申请与审批',
      ...(approved
        ? doneState
        : apply.status === 'REJECTED'
          ? { state: 'todo', stateText: '已驳回' }
          : { state: 'waiting', stateText: '待审批' }),
      hint: approved ? by(apply.reviewedTime, apply.reviewedByName) : apply.reviewRemark
    },
    {
      key: 'care',
      title: '护理/医务交接',
      ...(apply.careHandoverTime ? doneState : pendingState('CARE_HANDOVER')),
      hint: by(apply.careHandoverTime, apply.careHandoverByName)
    },
    {
      key: 'item',
      title: '物品及房间交接',
      ...(apply.itemHandoverTime ? doneState : pendingState('ITEM_HANDOVER')),
      hint: by(apply.itemHandoverTime, apply.itemHandoverByName)
    },
    {
      key: 'fee',
      title: '费用结清',
      ...(settled
        ? doneState
        : next === 'SETTLEMENT'
          ? { state: 'waiting', stateText: '待财务确认' }
          : { state: 'todo', stateText: '未开始' }),
      hint: next === 'SETTLEMENT' ? '退住费用审核 → 结算单前台/护理签字 → 财务退款' : undefined
    },
    {
      key: 'bed',
      title: '床位释放',
      ...(settled ? doneState : { state: 'todo', stateText: '未开始' }),
      hint: settled ? apply.autoDischargeMessage : '财务退款完成后系统自动释放床位并置为已退住'
    },
    {
      key: 'archive',
      title: '档案归档',
      ...(apply.archiveTime ? doneState : pendingState('ARCHIVE')),
      hint: by(apply.archiveTime, apply.archiveByName)
    }
  ]
}
