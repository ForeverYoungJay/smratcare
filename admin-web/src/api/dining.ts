import request, { fetchPage } from '../utils/request'
import type {
  DiningDish,
  DiningRecipe,
  DiningPrepZone,
  DiningDeliveryArea,
  DiningMealOrder,
  DiningDeliveryRecord,
  DiningStatsSummary,
  DiningRiskCheckResponse,
  DiningRiskOverride,
  DiningRiskThresholdConfig
} from '../types'

export function getDiningDishPage(params: any) {
  return fetchPage<DiningDish>('/api/life/dining/dish/page', params)
}

export function getDiningDishList(params: any) {
  return request.get<DiningDish[]>('/api/life/dining/dish/list', { params })
}

export function createDiningDish(data: Partial<DiningDish>) {
  return request.post<DiningDish>('/api/life/dining/dish', data)
}

export function updateDiningDish(id: number, data: Partial<DiningDish>) {
  return request.put<DiningDish>(`/api/life/dining/dish/${id}`, data)
}

export function updateDiningDishStatus(id: number, status: string) {
  return request.put<DiningDish>(`/api/life/dining/dish/${id}/status`, null, { params: { status } })
}

export function deleteDiningDish(id: number) {
  return request.delete<void>(`/api/life/dining/dish/${id}`)
}

export function getDiningRecipePage(params: any) {
  return fetchPage<DiningRecipe>('/api/life/dining/recipe/page', params)
}

export function createDiningRecipe(data: Partial<DiningRecipe>) {
  return request.post<DiningRecipe>('/api/life/dining/recipe', data)
}

export function updateDiningRecipe(id: number, data: Partial<DiningRecipe>) {
  return request.put<DiningRecipe>(`/api/life/dining/recipe/${id}`, data)
}

export function deleteDiningRecipe(id: number) {
  return request.delete<void>(`/api/life/dining/recipe/${id}`)
}

export function getDiningPrepZonePage(params: any) {
  return fetchPage<DiningPrepZone>('/api/life/dining/prep-zone/page', params)
}

export function createDiningPrepZone(data: Partial<DiningPrepZone>) {
  return request.post<DiningPrepZone>('/api/life/dining/prep-zone', data)
}

export function updateDiningPrepZone(id: number, data: Partial<DiningPrepZone>) {
  return request.put<DiningPrepZone>(`/api/life/dining/prep-zone/${id}`, data)
}

export function deleteDiningPrepZone(id: number) {
  return request.delete<void>(`/api/life/dining/prep-zone/${id}`)
}

export function getDiningDeliveryAreaPage(params: any) {
  return fetchPage<DiningDeliveryArea>('/api/life/dining/delivery-area/page', params)
}

export function createDiningDeliveryArea(data: Partial<DiningDeliveryArea>) {
  return request.post<DiningDeliveryArea>('/api/life/dining/delivery-area', data)
}

export function updateDiningDeliveryArea(id: number, data: Partial<DiningDeliveryArea>) {
  return request.put<DiningDeliveryArea>(`/api/life/dining/delivery-area/${id}`, data)
}

export function deleteDiningDeliveryArea(id: number) {
  return request.delete<void>(`/api/life/dining/delivery-area/${id}`)
}

export function getDiningMealOrderPage(params: any) {
  return fetchPage<DiningMealOrder>('/api/life/dining/order/page', params)
}

export function createDiningMealOrder(data: Partial<DiningMealOrder>) {
  return request.post<DiningMealOrder>('/api/life/dining/order', data)
}

export function checkDiningMealOrderRisk(data: Partial<DiningMealOrder>) {
  return request.post<DiningRiskCheckResponse>('/api/life/dining/order/risk-check', data)
}

export function applyDiningRiskOverride(data: any) {
  return request.post<DiningRiskOverride>('/api/life/dining/order/risk-override/apply', data)
}

export function reviewDiningRiskOverride(id: number, data: any) {
  return request.put<DiningRiskOverride>(`/api/life/dining/order/risk-override/${id}/review`, data)
}

export function getDiningRiskOverridePage(params: any) {
  return fetchPage<DiningRiskOverride>('/api/life/dining/order/risk-override/page', params)
}

export function updateDiningMealOrder(id: number, data: Partial<DiningMealOrder>) {
  return request.put<DiningMealOrder>(`/api/life/dining/order/${id}`, data)
}

export function updateDiningMealOrderStatus(id: number, status: string) {
  return request.put<DiningMealOrder>(`/api/life/dining/order/${id}/status`, null, { params: { status } })
}

export function deleteDiningMealOrder(id: number) {
  return request.delete<void>(`/api/life/dining/order/${id}`)
}

export function getDiningDeliveryRecordPage(params: any) {
  return fetchPage<DiningDeliveryRecord>('/api/life/dining/delivery-record/page', params)
}

export function createDiningDeliveryRecord(data: Partial<DiningDeliveryRecord>) {
  return request.post<DiningDeliveryRecord>('/api/life/dining/delivery-record', data)
}

export function updateDiningDeliveryRecord(id: number, data: Partial<DiningDeliveryRecord>) {
  return request.put<DiningDeliveryRecord>(`/api/life/dining/delivery-record/${id}`, data)
}

export function deleteDiningDeliveryRecord(id: number) {
  return request.delete<void>(`/api/life/dining/delivery-record/${id}`)
}

export function getDiningStatsSummary(params: any) {
  return request.get<DiningStatsSummary>('/api/life/dining/stats/summary', { params })
}

export function getDiningRiskThresholdList() {
  return request.get<DiningRiskThresholdConfig[]>('/api/life/dining/risk-threshold/list')
}

export function upsertDiningRiskThreshold(data: Partial<DiningRiskThresholdConfig>) {
  return request.post<DiningRiskThresholdConfig>('/api/life/dining/risk-threshold/upsert', data)
}
