package com.zhiyangyun.care.logistics.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class LogisticsWorkbenchSummaryResponse {
  private long lowStockCount;
  private long expiringCount;
  private long todayOutboundQty;
  private BigDecimal inventoryTotalAmount;
  private List<LogisticsNamedStatItem> weekTopConsumption;

  private long purchasePendingApprovalCount;
  private long purchaseApprovedNotArrivedCount;
  private long purchaseReceivedNotSettledCount;
  private BigDecimal monthPurchaseAmount;

  private long occupiedBeds;
  private long freeBeds;
  private long cleaningBeds;
  private long maintenanceBeds;
  private long todayDischargeCount;
  private long todayAdmissionCount;

  private long maintenancePendingCount;
  private long maintenanceProcessingCount;
  private long maintenanceOverdueCount;
  private BigDecimal monthMaintenanceCost;

  private long todayMealOrderCount;
  private long personalizedMealCount;
  private BigDecimal deliveryCompletionRate;
  private long undeliveredCount;
  private BigDecimal todayDiningCost;

  private long diabetesMealCount;
  private long lowSaltMealCount;
  private long allergyAlertCount;
  private long specialNutritionMealCount;

  private BigDecimal monthConsumeAmount;
  private List<LogisticsNamedStatItem> monthTopConsumption;
  private BigDecimal nursingConsumeAmount;
  private BigDecimal diningConsumeAmount;

  private long todayCleaningTaskCount;
  private long todayMaintenanceTaskCount;
  private long todayDeliveryTaskCount;
  private long todayInventoryCheckTaskCount;

  private long equipmentTotalCount;
  private long equipmentMaintainingCount;
  private long equipmentDueSoonCount;

  private String maintenanceTodoLastStatus;
  private String maintenanceTodoLastTriggerType;
  private LocalDateTime maintenanceTodoLastExecutedAt;
  private Long maintenanceTodoLastCreatedCount;
  private Long maintenanceTodoLastSkippedCount;
  private String maintenanceTodoLastErrorMessage;
  private long maintenanceTodoFailedCount7d;
}
