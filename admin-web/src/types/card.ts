import type { Id } from './common'

export interface CardAccount {
  id: Id
  elderId: Id
  elderName?: string
  cardNo: string
  status?: string
  lossFlag?: number
  balance?: number
  creditLimit?: number
  openTime?: string
  lastRechargeTime?: string
  remark?: string
}

export interface CardTradeLog {
  id: Id
  cardAccountId?: Id
  cardNo?: string
  elderId: Id
  elderName?: string
  amount: number
  balanceAfter?: number
  direction?: string
  sourceType?: string
  remark?: string
  createTime?: string
}
