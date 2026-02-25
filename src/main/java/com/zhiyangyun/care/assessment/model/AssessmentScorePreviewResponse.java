package com.zhiyangyun.care.assessment.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class AssessmentScorePreviewResponse {
  private BigDecimal score;

  private String levelCode;

  private String reason;
}
