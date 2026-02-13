package com.zhiyangyun.care.survey.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SurveyTemplateRequest {
  @NotBlank
  private String templateCode;

  @NotBlank
  private String templateName;

  private String description;

  @NotBlank
  private String targetType;

  private Integer status;

  private String startDate;

  private String endDate;

  private Integer anonymousFlag;

  private Integer scoreEnabled;

  private Integer totalScore;
}
