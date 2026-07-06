package com.zhiyangyun.care.ai.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

/** 风险评分趋势点（某长者某风险类型按日）。 */
@Data
public class AiRiskTrendPointResponse {
  private LocalDate assessDate;
  private BigDecimal score;
  private String riskLevel;
}
