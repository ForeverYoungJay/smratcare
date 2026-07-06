package com.zhiyangyun.care.cockpit.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

/**
 * 经营驾驶舱 BI 趋势项（按日，来自三张每日汇总表）。
 */
@Data
public class CockpitBiTrendItem {
  private LocalDate statDate;
  private BigDecimal occupancyRate;
  private Integer residentCount;
  private Integer admissions;
  private Integer discharges;
  private BigDecimal revenueAmount;
  private BigDecimal paidDailyAmount;
  private BigDecimal collectionRate;
  private BigDecimal careTaskCompletionRate;
  private BigDecimal alertOnTimeRate;
  private BigDecimal satisfactionScore;
}
