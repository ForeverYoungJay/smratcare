package com.zhiyangyun.care.health.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class HealthMedicationRemainingItem {
  private Long elderId;
  private String elderName;
  private Long drugId;
  private String drugName;
  private String unit;
  private BigDecimal depositQty;
  private BigDecimal usedQty;
  private BigDecimal remainQty;
  private BigDecimal minRemainQty;
  private Integer lowStock;
}
