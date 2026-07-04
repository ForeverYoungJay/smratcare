package com.zhiyangyun.care.cockpit.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

/**
 * 经营驾驶舱趋势项（按日）。
 */
@Data
public class CockpitTrendItem {
  private LocalDate statDate;
  private BigDecimal occupancyRate;
  private Integer residentCount;
  private BigDecimal revenueAmount;
  private BigDecimal collectionRate;
  private BigDecimal alertRespRate;
}
