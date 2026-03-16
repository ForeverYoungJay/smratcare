package com.zhiyangyun.care.life.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class DiningStatsBuildingItem {
  private String buildingName;
  private long orderCount;
  private BigDecimal totalAmount;
}
