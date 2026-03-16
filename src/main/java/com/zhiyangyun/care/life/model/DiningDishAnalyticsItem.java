package com.zhiyangyun.care.life.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class DiningDishAnalyticsItem {
  private Long id;
  private String dishName;
  private String dishCategory;
  private String mealType;
  private BigDecimal unitPrice;
  private Integer currentDiningCount;
  private BigDecimal purchaseQty;
  private String purchaseUnit;
  private long monthlyRecipeCount;
  private long monthlyOrderCount;
  private BigDecimal monthlyPlannedPurchaseQty;
  private BigDecimal monthlyEstimatedCost;
}
