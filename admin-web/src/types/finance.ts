export interface PaymentRecordItem {
  id: number
  billMonthlyId: number
  amount: number
  payMethod: 'CASH' | 'CARD' | 'BANK' | 'WECHAT_OFFLINE' | 'ALIPAY' | 'WECHAT' | 'QR_CODE'
  paidAt: string
  operatorStaffId?: number
  operatorStaffName?: string
  remark?: string
  externalTxnId?: string
  createTime?: string
}

export interface ReconcileDailyItem {
  id?: number
  date?: string
  reconcileDate?: string
  totalReceived: number
  mismatchFlag: boolean | number
  remark?: string
  createTime?: string
}

export interface ReconcileRequest {
  date: string
}

export interface FinanceBillDetail {
  billId: number
  elderId: number
  elderName?: string
  billMonth: string
  totalAmount: number
  paidAmount?: number
  outstandingAmount?: number
  status?: number
  items: FinanceBillItem[]
  payments: PaymentRecordItem[]
  storeOrders: StoreOrderSummary[]
}

export interface FinanceBillItem {
  itemType: string
  itemName: string
  amount: number
  basis?: string
}

export interface StoreOrderSummary {
  id: number
  orderNo: string
  elderId: number
  elderName?: string
  totalAmount: number
  payableAmount: number
  orderStatus: number
  payStatus: number
  payTime?: string
  createTime?: string
}

export interface FinanceReportMonthlyItem {
  month: string
  amount: number
}

export interface FinanceArrearsItem {
  elderId: number
  elderName?: string
  outstandingAmount: number
}

export interface FinanceStoreSalesItem {
  month: string
  amount: number
}

export interface FinanceCategoryTrendItem {
  period: string
  amount: number
  ratio: number
}

export interface FinanceCategoryProfitItem {
  category: string
  totalAmount: number
  totalCost: number
  totalProfit: number
  profitRate: number
}

export interface FinanceCategoryConsumptionAnalysis {
  itemKeyword: string
  from: string
  to: string
  totalAmount: number
  itemAmount: number
  itemRatio: number
  trend: FinanceCategoryTrendItem[]
  categoryProfit: FinanceCategoryProfitItem[]
}

export interface ElderAccount {
  id: number
  elderId: number
  elderName?: string
  balance: number
  creditLimit: number
  warnThreshold: number
  status: number
  pointsBalance?: number
  pointsStatus?: number
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface ElderAccountLog {
  id: number
  accountId: number
  elderId: number
  elderName?: string
  amount: number
  balanceAfter: number
  direction: 'DEBIT' | 'CREDIT'
  sourceType: string
  sourceId?: number
  remark?: string
  createTime?: string
}

export interface ElderAccountAdjustRequest {
  elderId: number
  elderName?: string
  amount: number
  direction: 'DEBIT' | 'CREDIT'
  remark?: string
}

export interface ElderAccountUpdateRequest {
  elderId: number
  elderName?: string
  creditLimit?: number
  warnThreshold?: number
  status?: number
  remark?: string
}

export interface FinanceWorkbenchOverview {
  bizDate: string
  cashier: FinanceCashierCard
  risk: FinanceRiskCard
  pending: FinancePendingCard
  revenueStructure: FinanceRevenueStructureCard
  roomOps: FinanceRoomOpsCard
  autoDebit: FinanceAutoDebitCard
  medicalFlow: FinanceMedicalFlowCard
  allocation: FinanceAllocationCard
  reconcile: FinanceReconcileCard
  quickEntries: FinanceQuickEntry[]
}

export interface FinanceCashierCard {
  todayCollectedTotal: number
  todayInvoiceAmount: number
  todayRefundAmount: number
  paymentMethods: FinancePaymentMethodAmount[]
}

export interface FinancePaymentMethodAmount {
  method: string
  methodLabel: string
  amount: number
}

export interface FinanceRiskCard {
  overdueElderCount: number
  overdueAmount: number
  lowBalanceCount: number
  expiringContractCount: number
}

export interface FinancePendingCard {
  pendingDiscountCount: number
  pendingRefundCount: number
  pendingDischargeSettlementCount: number
}

export interface FinanceRevenueStructureCard {
  monthRevenueTotal: number
  categories: FinanceRevenueCategory[]
}

export interface FinanceRevenueCategory {
  code: string
  name: string
  amount: number
  ratio: number
}

export interface FinanceRoomOpsCard {
  floorTop: FinanceFloorRanking[]
  floorBottom: FinanceFloorRanking[]
  roomTop10: FinanceRoomRanking[]
  emptyBedLossEstimate: number
}

export interface FinanceFloorRanking {
  floorNo: string
  income: number
}

export interface FinanceRoomRanking {
  building: string
  floorNo: string
  roomNo: string
  income: number
  cost: number
  netAmount: number
  occupiedBeds: number
  emptyBeds: number
}

export interface FinanceAutoDebitCard {
  shouldDeductCount: number
  successCount: number
  failedCount: number
  pendingHandleCount: number
  failureReasons: FinanceReasonCount[]
}

export interface FinanceReasonCount {
  reason: string
  count: number
}

export interface FinanceMedicalFlowCard {
  todayFlowCount: number
  todayFlowAmount: number
  pendingReviewCount: number
  duplicateBillingCount: number
  missingOrderLinkCount: number
}

export interface FinanceAllocationCard {
  monthGeneratedCount: number
  ungeneratedRoomCount: number
  errorCount: number
}

export interface FinanceReconcileCard {
  billPaidUnmatchedCount: number
  duplicatedOrReversalPendingCount: number
  invoiceUnlinkedCount: number
}

export interface FinanceQuickEntry {
  key: string
  label: string
  path: string
}

export interface FinanceInvoiceReceiptItem {
  paymentId: number
  billId?: number
  elderId?: number
  elderName?: string
  amount: number
  payMethod: string
  payMethodLabel: string
  invoiceStatus: 'LINKED' | 'UNLINKED'
  invoiceStatusLabel: string
  receiptNo: string
  remark?: string
  paidAt: string
}

export interface FinanceAutoDebitExceptionItem {
  billId: number
  elderId?: number
  elderName?: string
  billMonth: string
  outstandingAmount: number
  balance: number
  reasonCode: string
  reasonLabel: string
  suggestion: string
}

export interface FinanceRoomOpsDetailResponse {
  period: string
  building?: string
  room?: string
  totalIncome: number
  totalCost: number
  totalNetAmount: number
  totalOccupiedBeds: number
  totalEmptyBeds: number
  rows: FinanceRoomOpsDetailRow[]
}

export interface FinanceRoomOpsDetailRow {
  building: string
  floorNo: string
  roomNo: string
  income: number
  cost: number
  netAmount: number
  occupiedBeds: number
  emptyBeds: number
}

export interface FinanceAllocationRuleItem {
  id: number
  ruleType: string
  ruleTypeLabel: string
  configKey: string
  configValue: number
  effectiveMonth: string
  status: number
  remark?: string
}

export interface FinanceAllocationTemplateInitResponse {
  month: string
  createdCount: number
  updatedCount: number
  configKeys: string[]
}

export interface FinanceAllocationMeterValidateRowRequest {
  building?: string
  floorNo?: string
  roomNo: string
  previousReading: number
  currentReading: number
  remark?: string
}

export interface FinanceAllocationMeterValidateRequest {
  month?: string
  abnormalThreshold?: number
  rows: FinanceAllocationMeterValidateRowRequest[]
}

export interface FinanceAllocationMeterValidateRowResult {
  rowNo: number
  building?: string
  floorNo?: string
  roomNo?: string
  previousReading?: number
  currentReading?: number
  usage?: number
  valid: boolean
  level: 'OK' | 'WARN' | 'ERROR'
  code: string
  message: string
  remark?: string
}

export interface FinanceAllocationMeterValidateResponse {
  month: string
  abnormalThreshold: number
  totalRows: number
  validRows: number
  invalidRows: number
  warningRows: number
  rows: FinanceAllocationMeterValidateRowResult[]
}

export interface FinanceReconcileExceptionItem {
  exceptionType: string
  exceptionTypeLabel: string
  billId?: number
  paymentId?: number
  elderId?: number
  elderName?: string
  amount: number
  detail: string
  suggestion: string
  occurredAt?: string
}

export interface FinanceMasterDataOverview {
  month: string
  feeSubjectCount: number
  billingRuleCount: number
  paymentChannelCount: number
  pendingApprovalCount: number
  paymentChannels: string[]
  recentConfigs: FinanceMasterConfigEntry[]
}

export interface FinanceMasterConfigEntry {
  configKey: string
  effectiveMonth: string
  valueText: string
  remark?: string
  status: number
}

export interface FinanceBillingConfigEntry {
  id: number
  orgId: number
  configKey: string
  configValue: number
  effectiveMonth: string
  status: number
  remark?: string
}

export interface FinanceBillingConfigUpsertRequest {
  configKey: string
  configValue: number
  effectiveMonth: string
  status?: number
  remark?: string
}

export interface FinanceBillingConfigBatchUpsertRequest {
  items: FinanceBillingConfigUpsertRequest[]
}

export interface FinanceConfigChangeLogItem {
  id: number
  actorName?: string
  actorId?: number
  actionType: string
  entityType: string
  entityId?: number
  detail?: string
  createTime: string
}

export interface FinanceBillingConfigRollbackRequest {
  logId: number
}

export interface FinanceBillingConfigSnapshotItem {
  effectiveMonth: string
  configCount: number
  latestUpdateTime?: string
}

export interface FinanceModuleEntrySummary {
  moduleKey: string
  bizDate: string
  todayAmount: number
  monthAmount: number
  totalCount: number
  pendingCount: number
  exceptionCount: number
  warningMessage?: string
  topItems: FinanceModuleEntryItem[]
}

export interface FinanceModuleEntryItem {
  label: string
  count: number
  amount: number
}

export interface FinanceReportEntrySummary {
  reportKey: string
  periodFrom: string
  periodTo: string
  totalRevenue: number
  totalStoreSales: number
  arrearsTotal: number
  arrearsElderCount: number
  warningMessage?: string
  topCategories: FinanceNameAmountItem[]
  topRooms: FinanceNameAmountItem[]
}

export interface FinanceNameAmountItem {
  label: string
  amount: number
  extra?: string
}
