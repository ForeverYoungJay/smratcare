package com.zhiyangyun.care.life.model;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DiningRiskOverrideReviewRequest {
  @NotBlank
  private String reviewStatus;

  private String reviewRemark;

  private LocalDateTime effectiveUntil;
}
