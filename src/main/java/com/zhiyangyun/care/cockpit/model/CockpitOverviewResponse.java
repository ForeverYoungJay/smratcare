package com.zhiyangyun.care.cockpit.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

/**
 * 经营驾驶舱总览响应：当日核心 KPI + 近 N 日趋势。
 */
@Data
public class CockpitOverviewResponse {
  private LocalDate statDate;

  // 床位与入住
  private Integer totalBeds;
  private Integer occupiedBeds;
  private Integer availableBeds;
  private BigDecimal occupancyRate;
  private Integer residentCount;
  private Integer admissions;
  private Integer discharges;
  private Integer netChange;
  /** 床位周转率（近30日：期间入住/期末在住 * 100） */
  private BigDecimal turnoverRate;

  // 营收与回款
  private BigDecimal revenueAmount;
  private BigDecimal paidAmount;
  private BigDecimal outstandingAmount;
  private BigDecimal collectionRate;

  // 人效
  private Integer staffCount;
  /** 人效：在住长者数 / 在职员工数 */
  private BigDecimal residentPerStaff;

  // 安全告警
  private Integer alertTotal;
  private Integer alertResolved;
  private Integer alertTimeout;
  private BigDecimal alertRespRate;

  private List<CockpitTrendItem> trend;
}
