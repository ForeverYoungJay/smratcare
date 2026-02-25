package com.zhiyangyun.care.elder.model.lifecycle;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DischargeApplyReviewRequest {
  @NotBlank
  private String status;
  private String reviewRemark;
}
