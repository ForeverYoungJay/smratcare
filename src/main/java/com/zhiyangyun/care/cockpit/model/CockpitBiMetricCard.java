package com.zhiyangyun.care.cockpit.model;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 经营驾驶舱 BI 总览卡片项：本月值 + 环比。
 */
@Data
public class CockpitBiMetricCard {
  private String metricCode;
  private String metricName;
  private String unit;
  /** 本月值 */
  private BigDecimal value;
  /** 上月值 */
  private BigDecimal prevValue;
  /** 环比变化率%（上月为 0 时为 null） */
  private BigDecimal momRate;
}
