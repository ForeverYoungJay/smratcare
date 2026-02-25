package com.zhiyangyun.care.assessment.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssessmentScorePreviewRequest {
  @NotNull
  private Long templateId;

  @NotBlank
  private String detailJson;
}
