package com.zhiyangyun.care.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CareTaskReviewRequest {
  @NotNull
  private Long taskDailyId;

  @NotNull
  private Long staffId;

  @NotNull
  @Min(1)
  @Max(5)
  private Integer score;

  private String comment;

  private String reviewerType;

  private String reviewTime;
}
