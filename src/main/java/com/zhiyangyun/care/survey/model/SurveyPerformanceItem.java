package com.zhiyangyun.care.survey.model;

import lombok.Data;

@Data
public class SurveyPerformanceItem {
  private Long staffId;
  private String staffName;
  private Long submissions;
  private Double averageScore;
}
