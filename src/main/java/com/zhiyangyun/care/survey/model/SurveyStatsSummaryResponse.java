package com.zhiyangyun.care.survey.model;

import lombok.Data;

@Data
public class SurveyStatsSummaryResponse {
  private Long totalSubmissions;
  private Double averageScore;
  private Integer scoreTotal;
}
