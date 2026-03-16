package com.zhiyangyun.care.life.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class DiningStatsSummaryResponse {
  private long totalOrders;
  private BigDecimal totalAmount;
  private long deliveredOrders;
  private BigDecimal deliveryRate;
  private long onTimeDeliveredOrders;
  private long trackedDeliveryOrders;
  private BigDecimal onTimeDeliveryRate;
  private BigDecimal nextMonthEstimatedCost;
  private List<DiningStatsMealTypeItem> mealTypeStats;
  private List<DiningStatsBuildingItem> buildingStats;
  private List<DiningStatsProcurementItem> procurementItems;
}
