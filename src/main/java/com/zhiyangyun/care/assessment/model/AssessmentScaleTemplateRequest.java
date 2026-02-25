package com.zhiyangyun.care.assessment.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssessmentScaleTemplateRequest {
  @NotBlank
  private String templateCode;

  @NotBlank
  private String templateName;

  @NotBlank
  private String assessmentType;

  private String description;

  private String scoreRulesJson;

  private String levelRulesJson;

  private Integer status;
}
