package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceWorkbenchOverviewResponse {
  private LocalDate bizDate;
  private CashierCard cashier;
  private RiskCard risk;
  private PendingCard pending;
  private RevenueStructureCard revenueStructure;
  private RoomOpsCard roomOps;
  private AutoDebitCard autoDebit;
  private MedicalFlowCard medicalFlow;
  private AllocationCard allocation;
  private ReconcileCard reconcile;
  private List<QuickEntry> quickEntries = new ArrayList<>();

  @Data
  public static class CashierCard {
    private BigDecimal todayCollectedTotal;
    private BigDecimal todayInvoiceAmount;
    private BigDecimal todayRefundAmount;
    private List<PaymentMethodAmount> paymentMethods = new ArrayList<>();
  }

  @Data
  public static class PaymentMethodAmount {
    private String method;
    private String methodLabel;
    private BigDecimal amount;
  }

  @Data
  public static class RiskCard {
    private Long overdueElderCount;
    private BigDecimal overdueAmount;
    private Long lowBalanceCount;
    private Long expiringContractCount;
  }

  @Data
  public static class PendingCard {
    private Long pendingDiscountCount;
    private Long pendingRefundCount;
    private Long pendingDischargeSettlementCount;
    private Long issueTodoCount;
    private Long collectionReminderCount;
    private Long lockedMonthCount;
  }

  @Data
  public static class RevenueStructureCard {
    private BigDecimal monthRevenueTotal;
    private List<RevenueCategory> categories = new ArrayList<>();
  }

  @Data
  public static class RevenueCategory {
    private String code;
    private String name;
    private BigDecimal amount;
    private BigDecimal ratio;
  }

  @Data
  public static class RoomOpsCard {
    private List<FloorRanking> floorTop = new ArrayList<>();
    private List<FloorRanking> floorBottom = new ArrayList<>();
    private List<RoomRanking> roomTop10 = new ArrayList<>();
    private BigDecimal emptyBedLossEstimate;
  }

  @Data
  public static class FloorRanking {
    private String floorNo;
    private BigDecimal income;
  }

  @Data
  public static class RoomRanking {
    private String building;
    private String floorNo;
    private String roomNo;
    private BigDecimal income;
    private BigDecimal cost;
    private BigDecimal netAmount;
    private Integer occupiedBeds;
    private Integer emptyBeds;
  }

  @Data
  public static class AutoDebitCard {
    private Long shouldDeductCount;
    private Long successCount;
    private Long failedCount;
    private Long pendingHandleCount;
    private List<ReasonCount> failureReasons = new ArrayList<>();
  }

  @Data
  public static class ReasonCount {
    private String reason;
    private Long count;
  }

  @Data
  public static class MedicalFlowCard {
    private Long todayFlowCount;
    private BigDecimal todayFlowAmount;
    private Long pendingReviewCount;
    private Long duplicateBillingCount;
    private Long missingOrderLinkCount;
  }

  @Data
  public static class AllocationCard {
    private Long monthGeneratedCount;
    private Long ungeneratedRoomCount;
    private Long errorCount;
  }

  @Data
  public static class ReconcileCard {
    private Long billPaidUnmatchedCount;
    private Long duplicatedOrReversalPendingCount;
    private Long invoiceUnlinkedCount;
  }

  @Data
  public static class QuickEntry {
    private String key;
    private String label;
    private String path;
  }
}
