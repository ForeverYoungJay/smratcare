export interface CardAccount {
  id: number
  elderId: number
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
  id: number
  cardAccountId?: number
  cardNo?: string
  elderId: number
  elderName?: string
  amount: number
  balanceAfter?: number
  direction?: string
  sourceType?: string
  remark?: string
  createTime?: string
}
