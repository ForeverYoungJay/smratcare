package com.zhiyangyun.care.oa.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class OaActivityPlanReviewRequest {
  private BigDecimal actualExpense;

  private String effectEvaluation;

  private String elderFeedback;

  private String riskSituation;

  private String reportContent;
}
