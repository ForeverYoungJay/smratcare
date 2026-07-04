package com.zhiyangyun.care.ltci.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 对已判级的评估结果发起争议复核。 */
@Data
public class LtciDisputeRequest {
  @NotNull
  private Long assessmentId;
  @NotNull
  private String reason;
  private String applicantName;
  private String applicantPhone;
}
