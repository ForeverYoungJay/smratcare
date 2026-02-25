package com.zhiyangyun.care.life.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DiningRiskOverrideApplyRequest {
  @NotNull
  private Long elderId;

  private String elderName;

  @NotBlank
  private String dishNames;

  @NotBlank
  private String applyReason;
}
