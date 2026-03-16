package com.zhiyangyun.care.life.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class DiningDishAnalyticsResponse {
  private String month;
  private long totalDishCount;
  private long totalRecipeCount;
  private BigDecimal totalPlannedPurchaseQty;
  private BigDecimal totalEstimatedCost;
  private List<DiningDishAnalyticsItem> items;
}
