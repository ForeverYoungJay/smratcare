package com.zhiyangyun.care.nursing.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NursingRectificationReviewRequest {
  @NotNull
  private Boolean passed;
  private String reviewResult;
}
