package com.zhiyangyun.care.survey.model;

import lombok.Data;

@Data
public class SurveyStatsSummaryResponse {
  private Long totalSubmissions;
  private Long scoredSubmissions;
  private Long unscoredSubmissions;
  private Double averageScore;
  private Integer scoreTotal;
}
