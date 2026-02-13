package com.zhiyangyun.care.survey.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SurveyQuestionRequest {
  @NotBlank
  private String questionCode;

  @NotBlank
  private String title;

  @NotBlank
  private String questionType;

  private String optionsJson;

  private Integer requiredFlag;

  private Integer scoreEnabled;

  private Integer maxScore;

  private Integer status;

  private Integer sortNo;
}
