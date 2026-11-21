import request, { fetchPage } from '../utils/request'
import type { Id } from '../types'

export interface ElectricityPriceItem {
  id: Id
  unitPrice: number
  effectiveFrom: string
  remark?: string
}

export interface ElectricityReadingItem {
  id: Id
  billMonth: string
  roomId: Id
  building?: string
  floorNo?: string
  roomNo: string
  previousReading: number
  currentReading: number
  usageAmount: number
  unitPrice: number
  feeAmount: number
  shareMode: string
  shareModeText?: string
  residentCount?: number
  perResidentAmount?: number
  payStatus: string
  payStatusText?: string
  paidAt?: string
  payRemark?: string
  remark?: string
}

export interface ElectricityMonthSummary {
  billMonth: string
  unitPrice: number
  roomCount: number
  recordedRoomCount: number
  unrecordedRoomCount: number
  unpaidRoomCount: number
  totalUsage: number
  totalFee: number
  unpaidFee: number
}

export interface ElectricityReadingPayload {
  billMonth: string
  roomId: Id
  previousReading?: number
  currentReading: number
  shareMode?: string
  remark?: string
}

export function getElectricityPrices() {
  return request.get<ElectricityPriceItem[]>('/api/finance/electricity/prices')
}

export function saveElectricityPrice(data: { unitPrice: number; effectiveFrom: string; remark?: string }) {
  return request.post<ElectricityPriceItem>('/api/finance/electricity/prices', data)
}

export function getElectricityReadingPage(params: {
  pageNo: number
  pageSize: number
  billMonth?: string
  building?: string
  roomNo?: string
  payStatus?: string
}) {
  return fetchPage<ElectricityReadingItem>('/api/finance/electricity/page', params)
}

export function getElectricitySummary(billMonth?: string) {
  return request.get<ElectricityMonthSummary>('/api/finance/electricity/summary', { params: { billMonth } })
}

export function getElectricityPreviousReading(params: { billMonth?: string; roomId: Id }) {
  return request.get<{ previousReading: number }>('/api/finance/electricity/previous-reading', { params })
}

export function saveElectricityReading(data: ElectricityReadingPayload) {
  return request.post<ElectricityReadingItem>('/api/finance/electricity/readings', data)
}

export function updateElectricityPayStatus(readingId: Id, paid: boolean, payRemark?: string) {
  return request.post<ElectricityReadingItem>(
    `/api/finance/electricity/readings/${readingId}/pay-status`,
    null,
    { params: { paid, payRemark } }
  )
}
