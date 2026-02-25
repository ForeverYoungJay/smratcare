import request, { fetchPage } from '../utils/request'
import type { CardAccount, CardTradeLog } from '../types'

export function getCardAccountPage(params: any) {
  return fetchPage<CardAccount>('/api/card/account/page', params)
}

export function createCardAccount(data: Partial<CardAccount>) {
  return request.post<CardAccount>('/api/card/account', data)
}

export function updateCardAccount(id: number, data: Partial<CardAccount>) {
  return request.put<CardAccount>(`/api/card/account/${id}`, data)
}

export function deleteCardAccount(id: number) {
  return request.delete<void>(`/api/card/account/${id}`)
}

export function rechargeCard(data: { cardAccountId: number; amount: number; remark?: string }) {
  return request.post('/api/card/account/recharge', data)
}

export function consumeCard(data: { cardAccountId: number; amount: number; remark?: string }) {
  return request.post('/api/card/account/consume', data)
}

export function getCardRechargePage(params: any) {
  return fetchPage<CardTradeLog>('/api/card/account/recharge/page', params)
}

export function getCardConsumePage(params: any) {
  return fetchPage<CardTradeLog>('/api/card/account/consume/page', params)
}
