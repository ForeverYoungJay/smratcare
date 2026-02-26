package com.zhiyangyun.care.fire.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FireSafetyScanCompleteRequest {
  @NotBlank
  private String qrToken;

  private String inspectorName;

  private String actionTaken;
}
