package com.zhiyangyun.care.life.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class DiningStatsProcurementItem {
  private Long dishId;
  private String dishName;
  private String dishCategory;
  private String mealType;
  private int recipeCount;
  private Integer currentDiningCount;
  private BigDecimal purchaseQty;
  private String purchaseUnit;
  private BigDecimal totalPlannedQty;
  private BigDecimal estimatedCost;
}
