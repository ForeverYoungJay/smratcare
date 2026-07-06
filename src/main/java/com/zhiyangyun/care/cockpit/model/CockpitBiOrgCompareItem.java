package com.zhiyangyun.care.cockpit.model;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 经营驾驶舱 BI 机构对比项（按月，SYS_ADMIN 跨机构下钻）。
 */
@Data
public class CockpitBiOrgCompareItem {
  private Long orgId;
  private String orgName;
  private BigDecimal occupancyRate;
  private Integer residentCount;
  private BigDecimal revenueAmount;
  private BigDecimal collectionRate;
  private BigDecimal careTaskCompletionRate;
  private BigDecimal alertOnTimeRate;
  private BigDecimal satisfactionScore;
}
