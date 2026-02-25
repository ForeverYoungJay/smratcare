package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FeeAuditReviewRequest {
  @NotBlank
  private String status;
  private String reviewRemark;
}
